# Liegestuetz — Pushup Challenge App

An Android app for running pushup challenges with friends. Create a challenge, set a daily goal with an optional increment, share an invite code, and get notified every time a friend completes their reps.

## Features

- Create challenges with configurable starting reps and daily increment
- Invite friends via a 6-character code
- Mark your daily goal as complete
- Live leaderboard — updates in real time when friends finish their reps
- Push notifications when someone in your challenge completes their pushups
- Daily reminder notifications at 18:00 Europe/Berlin
- Personal 14-day completion heatmap

---

## Prerequisites

| Tool | Version |
|---|---|
| Android Studio | Hedgehog or newer |
| JDK | 17 |
| Android SDK | API 35 (compile), API 26 (min) |
| Node.js | 20 LTS (for Cloud Functions) |
| Firebase CLI | Latest (`npm install -g firebase-tools`) |

---

## Firebase Setup (required before first build)

### 1. Create a Firebase project

1. Go to [console.firebase.google.com](https://console.firebase.google.com) and create a new project.
2. Enable **Google Analytics** if you want (optional).

### 2. Register the Android app

1. In your Firebase project, click **Add app → Android**.
2. Use package name: `com.liegestuetz`
3. Enter a nickname, e.g. "Liegestuetz Android".
4. Download `google-services.json` and place it at `app/google-services.json` (replacing the placeholder).

### 3. Enable Authentication

1. In the Firebase console, go to **Authentication → Sign-in method**.
2. Enable **Email/Password**.
3. *(Optional)* Enable **Google** sign-in — requires adding your app's SHA-1 fingerprint.

To get your debug SHA-1:
```bash
./gradlew signingReport
```

### 4. Create Firestore database

1. Go to **Firestore Database → Create database**.
2. Start in **production mode** (the security rules in this repo will be deployed).
3. Choose a region — `europe-west1` (Belgium) is recommended if your users are in Europe.

### 5. Deploy Firestore security rules and indexes

```bash
firebase login
firebase use --add          # select your project
firebase deploy --only firestore
```

### 6. Set up Cloud Functions

```bash
cd functions
npm install
cd ..
firebase deploy --only functions
```

The functions require a **Blaze (pay-as-you-go)** Firebase plan to make outbound network calls.

### 7. Enable Firebase Cloud Messaging

FCM is enabled automatically when you add the `firebase-messaging` dependency. No extra console step is needed.

---

## Building the app

```bash
# Generate Gradle wrapper (first time only, requires Gradle installed globally)
gradle wrapper

# Build debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew test

# Run lint
./gradlew lint
```

The debug APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

---

## Project structure

```
liegestuetz-app/
├── app/                          # Application entry point (Hilt, navigation, FCM service)
├── core/
│   ├── core-common/              # Result<T>, date utilities (pure JVM)
│   ├── core-domain/              # Domain models, repository interfaces, use cases (pure JVM)
│   ├── core-data/                # Firestore/Firebase repository implementations, Hilt DI
│   └── core-ui/                  # Material3 theme, shared Compose components
├── features/
│   ├── feature-auth/             # Splash, Login, Register screens
│   ├── feature-home/             # Home (challenge list), Create, Join screens
│   ├── feature-challenge/        # Challenge detail, leaderboard, completion
│   └── feature-profile/          # Profile and sign-out
├── functions/                    # Firebase Cloud Functions (TypeScript)
│   └── src/index.ts              # onCompletionCreated, sendDailyReminders
├── firestore.rules               # Firestore security rules
├── firestore.indexes.json        # Firestore composite indexes
└── firebase.json                 # Firebase project config
```

### Architecture

```
feature → core-domain (use cases) → core-data (repositories → Firestore)
feature → core-ui    (theme, components)
feature → core-common (Result<T>, extensions)
```

- **Presentation**: Jetpack Compose + `@HiltViewModel` + `StateFlow<UiState>`
- **Domain**: pure Kotlin/JVM use cases and repository interfaces — no Android deps
- **Data**: Firestore snapshot listeners wrapped in `callbackFlow` → `Flow<Result<T>>`

---

## CI / CD

GitHub Actions runs on every push and pull request to `main`:

- Lint (`./gradlew lint`)
- Unit tests (`./gradlew test`)
- Assemble debug APK

See `.github/workflows/ci.yml`.

To supply `google-services.json` in CI, store it as a repository secret:

1. **Settings → Secrets and variables → Actions → New repository secret**
2. Name: `GOOGLE_SERVICES_JSON`, value: paste the full JSON content.
3. Update the CI workflow step to write the secret to the file:
   ```yaml
   - name: Write google-services.json
     run: echo '${{ secrets.GOOGLE_SERVICES_JSON }}' > app/google-services.json
   ```

---

## Firestore data model

| Collection | Document ID | Key fields |
|---|---|---|
| `users` | `{uid}` | displayName, email, fcmToken |
| `challenges` | auto | name, startingReps, dailyIncrement, durationDays, inviteCode, participantIds, status |
| `challenges/{id}/participants` | `{uid}` | totalCompletions, currentStreak |
| `challenges/{id}/completions` | `{uid}_{YYYY-MM-DD}` | goalReps, completedAt, note |

**Today's goal** is always computed client-side: `startingReps + dailyIncrement × dayIndex`

---

## Notification channels

| Channel ID | Purpose |
|---|---|
| `completions` | Fired when a friend marks their goal complete |
| `reminders` | Daily reminder at 18:00 if you haven't completed yet |

---

## Adding Google Sign-In (optional)

1. Add your debug and release SHA-1 fingerprints to the Firebase console under **Project settings → Your apps → Android app → Add fingerprint**.
2. Re-download `google-services.json`.
3. Add the Credential Manager dependencies to `app/build.gradle.kts`:
   ```kotlin
   implementation("androidx.credentials:credentials:1.3.0")
   implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
   ```
4. Implement the Google Sign-In button in `LoginScreen.kt` using `GetGoogleIdOption` and `CredentialManager`.
