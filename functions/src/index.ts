import * as admin from "firebase-admin";
import { onRequest } from "firebase-functions/v2/https";

admin.initializeApp();

const db = admin.firestore();

// ---------------------------------------------------------------------------
// Phase 5: onCompletionCreated
//
// Triggered when a user marks their daily pushup goal as complete.
// Reads all participants, fans out FCM notifications to everyone except
// the completing user, then updates totalCompletions + currentStreak
// in a Firestore transaction.
//
// export const onCompletionCreated = onDocumentCreated(
//   {
//     document: "challenges/{challengeId}/completions/{completionId}",
//     region: "europe-west1",
//   },
//   async (event) => {
//     const snap = event.data;
//     if (!snap) return;
//
//     const { challengeId } = event.params;
//     const completion = snap.data() as CompletionDto;
//
//     // 1. Load challenge to get participantIds + name
//     const challengeDoc = await db.doc(`challenges/${challengeId}`).get();
//     const challenge = challengeDoc.data() as ChallengeDto;
//
//     // 2. Build FCM messages for all OTHER participants
//     const otherParticipantIds = challenge.participantIds.filter(
//       (id: string) => id !== completion.userId
//     );
//
//     const tokens = await Promise.all(
//       otherParticipantIds.map(async (uid: string) => {
//         const userDoc = await db.doc(`users/${uid}`).get();
//         return userDoc.data()?.fcmToken as string | undefined;
//       })
//     );
//
//     const validTokens = tokens.filter((t): t is string => !!t);
//
//     if (validTokens.length > 0) {
//       await admin.messaging().sendEachForMulticast({
//         tokens: validTokens,
//         notification: {
//           title: `${completion.displayName} crushed their pushups! 💪`,
//           body: `They completed ${completion.goalReps} reps in "${challenge.name}". Your turn!`,
//         },
//         data: {
//           type: "completion",
//           challengeId,
//         },
//         android: {
//           priority: "high",
//           notification: { channelId: "completions" },
//         },
//       });
//     }
//
//     // 3. Update streak + totalCompletions in a transaction
//     const participantRef = db.doc(
//       `challenges/${challengeId}/participants/${completion.userId}`
//     );
//
//     await db.runTransaction(async (tx) => {
//       const participantSnap = await tx.get(participantRef);
//       const participant = participantSnap.data() ?? {};
//
//       const yesterday = new Date(completion.date);
//       yesterday.setDate(yesterday.getDate() - 1);
//       const yesterdayKey = `${completion.userId}_${yesterday.toISOString().split("T")[0]}`;
//       const yesterdayRef = db.doc(
//         `challenges/${challengeId}/completions/${yesterdayKey}`
//       );
//       const yesterdaySnap = await tx.get(yesterdayRef);
//
//       const newStreak = yesterdaySnap.exists
//         ? (participant.currentStreak ?? 0) + 1
//         : 1;
//
//       tx.update(participantRef, {
//         totalCompletions: (participant.totalCompletions ?? 0) + 1,
//         currentStreak: newStreak,
//       });
//     });
//   }
// );

// ---------------------------------------------------------------------------
// Phase 5: sendDailyReminders
//
// Scheduled to run every evening. Sends a reminder FCM to participants
// who have not yet completed today's goal.
//
// export const sendDailyReminders = onSchedule(
//   { schedule: "every day 18:00", timeZone: "Europe/Berlin", region: "europe-west1" },
//   async (_event) => {
//     const today = new Date().toISOString().split("T")[0];
//     const challengesSnap = await db
//       .collection("challenges")
//       .where("status", "==", "active")
//       .get();
//
//     for (const challengeDoc of challengesSnap.docs) {
//       const challenge = challengeDoc.data();
//       for (const userId of challenge.participantIds as string[]) {
//         const completionId = `${userId}_${today}`;
//         const completionSnap = await db
//           .doc(`challenges/${challengeDoc.id}/completions/${completionId}`)
//           .get();
//
//         if (!completionSnap.exists) {
//           const userSnap = await db.doc(`users/${userId}`).get();
//           const fcmToken = userSnap.data()?.fcmToken;
//           if (fcmToken) {
//             await admin.messaging().send({
//               token: fcmToken,
//               notification: {
//                 title: "Don't break your streak! 🔥",
//                 body: `You still have pushups to do in "${challenge.name}" today.`,
//               },
//               data: { type: "reminder", challengeId: challengeDoc.id },
//               android: {
//                 priority: "normal",
//                 notification: { channelId: "reminders" },
//               },
//             });
//           }
//         }
//       }
//     }
//   }
// );

// ---------------------------------------------------------------------------
// Healthcheck — useful for verifying deployment during development
export const healthCheck = onRequest(
  { region: "europe-west1" },
  (_req, res) => {
    res.json({ status: "ok", version: "1.0.0", service: "liegestuetz-functions" });
  }
);

// Keep db reference used so TypeScript doesn't complain about unused import
void db;
