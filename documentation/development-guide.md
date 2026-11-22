# ResQSync AI - Development & Build Guide

This monorepo contains both the **Android Mobile Application** and the **Rescue Coordination Web Dashboard**, sharing a **Supabase** backend. Both projects can be developed, built, tested, and run independently or together inside **Antigravity**.

---

## Workspace Structure

```
D:\ResQSync
├── android-app/             # Native Kotlin Android Application
├── rescue-dashboard/        # React.js + TypeScript Web Dashboard
├── documentation/           # Architecture, SQL schema & guides
└── .vscode/                 # Antigravity Task Configurations
```

---

## 1. Managing with Antigravity

Antigravity provides integrated tasks accessible via the Command Palette (`Ctrl+Shift+P` -> `Tasks: Run Task`):

- **`Android: Build Debug APK`**: Compiles the Android Kotlin app without opening Android Studio.
- **`Android: Run Unit Tests`**: Executes local unit tests in `android-app`.
- **`Android: Clean`**: Cleans the Gradle build cache.
- **`Dashboard: Install Dependencies`**: Runs `npm install` inside `rescue-dashboard`.
- **`Dashboard: Start Dev Server`**: Launches React local development server on `http://localhost:3000`.
- **`Dashboard: Build Production`**: Creates an optimized static build in `rescue-dashboard/build/`.

---

## 2. Working on `android-app`

### Standalone CLI Commands (Powershell or Bash)
Navigate to `android-app`:
```powershell
cd D:\ResQSync\android-app
```

- **Build Debug APK:**
  ```powershell
  ./gradlew assembleDebug
  ```
- **Run Unit Tests:**
  ```powershell
  ./gradlew test
  ```
- **Install to Connected Android Device:**
  ```powershell
  ./gradlew installDebug
  ```

### Android Studio Compatibility
You can open `D:\ResQSync\android-app` directly in **Android Studio** at any time. Because `settings.gradle.kts` and `gradlew` reside inside `android-app`, Android Studio recognizes it as a native, self-contained project with zero configuration conflicts.

---

## 3. Working on `rescue-dashboard`

### Standalone CLI Commands
Navigate to `rescue-dashboard`:
```powershell
cd D:\ResQSync\rescue-dashboard
```

- **Install Dependencies:**
  ```powershell
  npm install
  ```
- **Configure Supabase Credentials:**
  Create `.env` based on `.env.example`:
  ```env
  REACT_APP_SUPABASE_URL=https://your-project.supabase.co
  REACT_APP_SUPABASE_ANON_KEY=your-anon-key
  ```
- **Start Development Server:**
  ```powershell
  npm start
  ```
  Opens at `http://localhost:3000`.
- **Build Production Bundle:**
  ```powershell
  npm run build
  ```

---

## 4. Setting Up the Shared Supabase Backend

1. Create a project on [Supabase](https://supabase.com).
2. Go to the **SQL Editor** in your Supabase dashboard.
3. Open `D:\ResQSync\documentation\supabase-schema.sql`, copy its contents, and execute.
   - This creates `emergency_reports` and `network_messages` tables.
   - Configures Row Level Security (RLS) policies.
   - Enables Realtime replication.
   - Seeds sample test data.
4. Copy your **Project URL** and **Anon Public API Key** from **Project Settings -> API**:
   - For `rescue-dashboard`: Add to `rescue-dashboard/.env`.
   - For `android-app`: Add to your environment or `local.properties`.
