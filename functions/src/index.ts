import * as admin from "firebase-admin";
import { onDocumentCreated } from "firebase-functions/v2/firestore";
import { onSchedule } from "firebase-functions/v2/scheduler";
import { onRequest } from "firebase-functions/v2/https";

admin.initializeApp();
const db = admin.firestore();
const messaging = admin.messaging();

const REGION = "europe-west1";

// ── Types ────────────────────────────────────────────────────────────────────

interface CompletionData {
  userId: string;
  challengeId: string;
  date: string; // "YYYY-MM-DD"
  goalReps: number;
  displayName?: string;
}

interface ChallengeData {
  name: string;
  participantIds: string[];
  status: string;
}

interface UserData {
  fcmToken?: string;
  displayName: string;
}

// ── onCompletionCreated ───────────────────────────────────────────────────────
//
// Triggered when a user writes their daily completion document.
// 1. Fans out FCM notifications to all other participants.
// 2. Updates totalCompletions and currentStreak in a transaction.

export const onCompletionCreated = onDocumentCreated(
  {
    document: "challenges/{challengeId}/completions/{completionId}",
    region: REGION,
  },
  async (event) => {
    const snap = event.data;
    if (!snap) return;

    const { challengeId } = event.params;
    const completion = snap.data() as CompletionData;

    // 1. Load challenge metadata
    const challengeDoc = await db.doc(`challenges/${challengeId}`).get();
    if (!challengeDoc.exists) return;
    const challenge = challengeDoc.data() as ChallengeData;
    if (challenge.status !== "active") return;

    // 2. Load the completing user's display name
    const completingUserDoc = await db.doc(`users/${completion.userId}`).get();
    const completingUser = completingUserDoc.data() as UserData | undefined;
    const displayName = completingUser?.displayName ?? "Someone";

    // 3. Fan-out FCM to all other participants
    const otherIds = challenge.participantIds.filter((id) => id !== completion.userId);
    const tokenDocs = await Promise.all(
      otherIds.map((uid) => db.doc(`users/${uid}`).get())
    );
    const tokens = tokenDocs
      .map((d) => (d.data() as UserData | undefined)?.fcmToken)
      .filter((t): t is string => !!t);

    if (tokens.length > 0) {
      await messaging.sendEachForMulticast({
        tokens,
        notification: {
          title: `${displayName} crushed their pushups! 💪`,
          body: `${completion.goalReps} reps in "${challenge.name}". Your turn!`,
        },
        data: {
          type: "completion",
          challengeId,
        },
        android: {
          priority: "high",
          notification: { channelId: "completions" },
        },
      });
    }

    // 4. Update streak + totalCompletions in a transaction
    const participantRef = db.doc(
      `challenges/${challengeId}/participants/${completion.userId}`
    );

    await db.runTransaction(async (tx) => {
      const participantSnap = await tx.get(participantRef);
      const participant = participantSnap.data() ?? {};

      // Check if user completed yesterday (streak logic)
      const yesterday = new Date(completion.date);
      yesterday.setDate(yesterday.getDate() - 1);
      const yDate = yesterday.toISOString().split("T")[0];
      const yesterdayRef = db.doc(
        `challenges/${challengeId}/completions/${completion.userId}_${yDate}`
      );
      const yesterdaySnap = await tx.get(yesterdayRef);
      const newStreak = yesterdaySnap.exists
        ? ((participant.currentStreak as number) ?? 0) + 1
        : 1;

      tx.update(participantRef, {
        totalCompletions: ((participant.totalCompletions as number) ?? 0) + 1,
        currentStreak: newStreak,
      });
    });
  }
);

// ── sendDailyReminders ────────────────────────────────────────────────────────
//
// Runs every evening. Sends a nudge to participants who haven't completed yet.

export const sendDailyReminders = onSchedule(
  { schedule: "every day 18:00", timeZone: "Europe/Berlin", region: REGION },
  async () => {
    const today = new Date().toISOString().split("T")[0];

    const challengesSnap = await db
      .collection("challenges")
      .where("status", "==", "active")
      .get();

    const sends: Promise<unknown>[] = [];

    for (const challengeDoc of challengesSnap.docs) {
      const challenge = challengeDoc.data() as ChallengeData;

      for (const userId of challenge.participantIds) {
        const completionId = `${userId}_${today}`;
        const completionSnap = await db
          .doc(`challenges/${challengeDoc.id}/completions/${completionId}`)
          .get();

        if (!completionSnap.exists) {
          const userSnap = await db.doc(`users/${userId}`).get();
          const token = (userSnap.data() as UserData | undefined)?.fcmToken;
          if (token) {
            sends.push(
              messaging.send({
                token,
                notification: {
                  title: "Don't break your streak! 🔥",
                  body: `You still have push-ups to do in "${challenge.name}" today.`,
                },
                data: { type: "reminder", challengeId: challengeDoc.id },
                android: {
                  priority: "normal",
                  notification: { channelId: "reminders" },
                },
              })
            );
          }
        }
      }
    }

    await Promise.allSettled(sends);
  }
);

// ── healthCheck ───────────────────────────────────────────────────────────────

export const healthCheck = onRequest({ region: REGION }, (_req, res) => {
  res.json({ status: "ok", version: "1.0.0", service: "liegestuetz-functions" });
});
