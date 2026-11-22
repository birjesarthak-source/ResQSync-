# ResQSync AI - Rescue Team Web Dashboard

The Rescue Team Dashboard is a web application built with **React.js** and **TypeScript**. It aggregates emergency incident reports sent from offline mobile devices across the mesh network once synced to the shared **Supabase** cloud backend.

---

## Key Features

1. **Live Incident Map & Listing:** Visualizes incoming disaster reports sorted by urgency and timestamp.
2. **Supabase Realtime Sync:** Subscribes to real-time Postgres changes so incidents uploaded from field devices appear instantly without page refresh.
3. **Multi-criteria Filtering:** Filter emergency incidents by incident type (Flood, Fire, Earthquake, Landslide, Collapse, Medical) and urgency level (Critical, High, Medium, Low).
4. **AI Incident Digest:** Shows Gemini AI summary, affected count, and recommended emergency dispatch actions.
5. **Offline/Demo Fallback:** Seamlessly operates with mock data if Supabase credentials are not configured yet.

---

## Getting Started

### 1. Environment Configuration

Copy `.env.example` to `.env`:

```bash
cp .env.example .env
```

Set your Supabase project credentials in `.env`:

```env
REACT_APP_SUPABASE_URL=https://your-project-id.supabase.co
REACT_APP_SUPABASE_ANON_KEY=your-anon-public-key
```

*(See `../documentation/supabase-schema.sql` to initialize your database tables).*

---

### 2. Install Dependencies

```bash
npm install
```

---

### 3. Run Development Server

```bash
npm start
```

The dashboard will open automatically at [http://localhost:3000](http://localhost:3000).

---

### 4. Build for Production

```bash
npm run build
```
Generates an optimized production bundle in the `build/` folder.
