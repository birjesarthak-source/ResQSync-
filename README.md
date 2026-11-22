# ResQSync AI

**ResQSync AI** is an emergency disaster response and off-grid coordination platform. It connects victims and first responders across an offline peer-to-peer smartphone mesh network, transmitting emergency incident reports to cloud emergency dispatch command centers once internet connectivity is restored.

---

## Workspace Structure

The project is organized into three distinct, decoupled directories:

```
D:\ResQSync/
├── android-app/             # Native Kotlin Android Application
│   ├── app/                 # Jetpack Compose UI, Room DB, Nearby mesh, Supabase sync
│   └── build.gradle.kts     # Standalone Gradle build root
│
├── rescue-dashboard/        # React.js + TypeScript Web Dashboard
│   ├── src/                 # Live incident map, urgency filters & Supabase Realtime
│   └── package.json         # Standalone npm project
│
├── documentation/           # Project Documentation & Database Schemas
│   ├── architecture.md      # Full system architecture & data flow diagrams
│   ├── supabase-schema.sql  # Shared Supabase schema, RLS policies & seed data
│   └── development-guide.md # Step-by-step build & run guide
│
└── .vscode/                 # Antigravity Task Automations (Tasks: Run Task)
```

---

## Quick Start

### 1. Web Dashboard (`rescue-dashboard`)
```powershell
cd rescue-dashboard
npm install
cp .env.example .env    # Configure your Supabase credentials
npm start               # Opens at http://localhost:3000
```

### 2. Android Mobile App (`android-app`)
You can develop either in **Antigravity** or **Android Studio**:
```powershell
cd android-app
./gradlew assembleDebug # Compiles the debug APK
./gradlew test          # Runs unit tests
```

### 3. Shared Supabase Backend
Both applications share the same PostgreSQL backend.
1. Open your [Supabase Project](https://supabase.com).
2. Run the script in [`documentation/supabase-schema.sql`](file:///D:/ResQSync/documentation/supabase-schema.sql).
3. Both the Android field devices and the Web Dashboard will now synchronize with live real-time updates.

---

## Single Development Environment (Antigravity)

You can manage both projects directly within Antigravity:
- **Build Android:** Run task `Android: Build Debug APK` (`Ctrl+Shift+P` -> `Tasks: Run Task`)
- **Run Android Tests:** Run task `Android: Run Unit Tests`
- **Launch Dashboard:** Run task `Dashboard: Start Dev Server`
- **Build Dashboard:** Run task `Dashboard: Build Production`
