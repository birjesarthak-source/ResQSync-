-- =============================================================================
-- ResQSync AI: Shared Supabase Database Schema (Production Security Hardened)
-- =============================================================================
-- Execute this script in your Supabase SQL Editor:
-- https://supabase.com/dashboard/project/_/sql
--
-- This script is strictly idempotent (safe to re-run multiple times).
-- It preserves existing tables, columns, indexes, and data.
-- Every CREATE POLICY statement is protected against duplicate-policy errors (42710).
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. Table Definitions
-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS public.emergency_reports (
    report_id TEXT PRIMARY KEY,
    reporter_device_id TEXT NOT NULL,
    incident_type TEXT NOT NULL,
    description TEXT NOT NULL,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    urgency TEXT NOT NULL DEFAULT 'MEDIUM',
    people_affected INTEGER DEFAULT 0,
    verification_status TEXT NOT NULL DEFAULT 'UNVERIFIED',
    ai_summary TEXT,
    created_at BIGINT NOT NULL,
    expires_at BIGINT,
    synced_at TIMESTAMPTZ DEFAULT TIMEZONE('utc'::text, NOW())
);

CREATE TABLE IF NOT EXISTS public.network_messages (
    message_id TEXT PRIMARY KEY,
    source_device_id TEXT NOT NULL,
    destination_device_id TEXT,
    hop_count INTEGER DEFAULT 0,
    payload TEXT,
    created_at BIGINT NOT NULL,
    synced_at TIMESTAMPTZ DEFAULT TIMEZONE('utc'::text, NOW())
);

CREATE TABLE IF NOT EXISTS public.user_roles (
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE PRIMARY KEY,
    role TEXT NOT NULL DEFAULT 'coordinator' CHECK (role IN ('admin', 'coordinator', 'field_responder')),
    sector TEXT DEFAULT 'Sector 1',
    created_at TIMESTAMPTZ DEFAULT TIMEZONE('utc'::text, NOW())
);

-- Safely apply data integrity constraints if not already present
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'chk_incident_type') THEN
        ALTER TABLE public.emergency_reports ADD CONSTRAINT chk_incident_type CHECK (
            incident_type IN ('FLOOD', 'FIRE', 'EARTHQUAKE', 'LANDSLIDE', 'BUILDING_COLLAPSE', 'MEDICAL_EMERGENCY', 'OTHER')
        );
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'chk_urgency') THEN
        ALTER TABLE public.emergency_reports ADD CONSTRAINT chk_urgency CHECK (
            urgency IN ('CRITICAL', 'HIGH', 'MEDIUM', 'LOW')
        );
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'chk_verification_status') THEN
        ALTER TABLE public.emergency_reports ADD CONSTRAINT chk_verification_status CHECK (
            verification_status IN ('UNVERIFIED', 'CORROBORATED', 'HUMAN_REVIEWED', 'DISPUTED')
        );
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'chk_latitude') THEN
        ALTER TABLE public.emergency_reports ADD CONSTRAINT chk_latitude CHECK (
            latitude IS NULL OR (latitude >= -90.0 AND latitude <= 90.0)
        );
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'chk_longitude') THEN
        ALTER TABLE public.emergency_reports ADD CONSTRAINT chk_longitude CHECK (
            longitude IS NULL OR (longitude >= -180.0 AND longitude <= 180.0)
        );
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'chk_people_affected') THEN
        ALTER TABLE public.emergency_reports ADD CONSTRAINT chk_people_affected CHECK (
            people_affected >= 0
        );
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'chk_hop_count') THEN
        ALTER TABLE public.network_messages ADD CONSTRAINT chk_hop_count CHECK (
            hop_count >= 0 AND hop_count <= 20
        );
    END IF;
END $$;

-- -----------------------------------------------------------------------------
-- 2. Performance Indexes
-- -----------------------------------------------------------------------------

CREATE INDEX IF NOT EXISTS idx_reports_urgency ON public.emergency_reports (urgency);
CREATE INDEX IF NOT EXISTS idx_reports_incident_type ON public.emergency_reports (incident_type);
CREATE INDEX IF NOT EXISTS idx_reports_created_at ON public.emergency_reports (created_at DESC);
CREATE INDEX IF NOT EXISTS idx_reports_verification_status ON public.emergency_reports (verification_status);
CREATE INDEX IF NOT EXISTS idx_messages_created_at ON public.network_messages (created_at DESC);

-- -----------------------------------------------------------------------------
-- 3. Enable Row Level Security (RLS)
-- -----------------------------------------------------------------------------

ALTER TABLE public.emergency_reports ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.network_messages ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.user_roles ENABLE ROW LEVEL SECURITY;

-- -----------------------------------------------------------------------------
-- 4. Helper Function for Role-Based Access Control (RBAC)
-- -----------------------------------------------------------------------------

CREATE OR REPLACE FUNCTION public.get_current_user_role()
RETURNS TEXT
LANGUAGE sql
STABLE
SECURITY DEFINER
SET search_path = public
AS $$
    SELECT COALESCE(
        (SELECT role FROM public.user_roles WHERE user_id = auth.uid()),
        COALESCE(auth.jwt() -> 'app_metadata' ->> 'role', 'coordinator')
    );
$$;

-- -----------------------------------------------------------------------------
-- 5. Global Policy Cleanup (Legacy & Historical Naming Variants)
-- Removes obsolete policy names from previous iterations without touching unrelated tables
-- -----------------------------------------------------------------------------

-- emergency_reports legacy variants
DROP POLICY IF EXISTS "Allow public read access to emergency reports" ON public.emergency_reports;
DROP POLICY IF EXISTS "Allow public read access to emergency_reports" ON public.emergency_reports;
DROP POLICY IF EXISTS "Allow authenticated staff to read emergency reports" ON public.emergency_reports;
DROP POLICY IF EXISTS "Allow authenticated rescue staff to read emergency reports" ON public.emergency_reports;
DROP POLICY IF EXISTS "Allow authenticated rescue personnel to read emergency reports" ON public.emergency_reports;
DROP POLICY IF EXISTS "Allow authorized staff to read emergency reports" ON public.emergency_reports;
DROP POLICY IF EXISTS "Allow insert access to emergency reports" ON public.emergency_reports;
DROP POLICY IF EXISTS "Allow authenticated rescue staff to update reports" ON public.emergency_reports;
DROP POLICY IF EXISTS "Allow authenticated staff to update emergency reports" ON public.emergency_reports;

-- network_messages legacy variants
DROP POLICY IF EXISTS "Allow public read access to network messages" ON public.network_messages;
DROP POLICY IF EXISTS "Allow authenticated staff to read network messages" ON public.network_messages;
DROP POLICY IF EXISTS "Allow authorized staff to read network messages" ON public.network_messages;
DROP POLICY IF EXISTS "Allow insert access to network messages" ON public.network_messages;

-- -----------------------------------------------------------------------------
-- 6. Hardened RLS Policies: emergency_reports
-- Every policy is directly preceded by DROP POLICY IF EXISTS to guarantee idempotency
-- -----------------------------------------------------------------------------

-- Policy 1: SELECT - Restricted to authorized rescue roles (Admin, Coordinator, Field Responder)
DROP POLICY IF EXISTS "Allow authorized roles to read emergency reports" ON public.emergency_reports;
CREATE POLICY "Allow authorized roles to read emergency reports"
ON public.emergency_reports
FOR SELECT
TO authenticated
USING (
    public.get_current_user_role() IN ('admin', 'coordinator', 'field_responder')
);

-- Policy 2: INSERT - Field devices sync from mesh; enforces verification_status = 'UNVERIFIED'
DROP POLICY IF EXISTS "Allow field devices to insert emergency reports" ON public.emergency_reports;
CREATE POLICY "Allow field devices to insert emergency reports"
ON public.emergency_reports
FOR INSERT
TO anon, authenticated
WITH CHECK (
    report_id IS NOT NULL AND
    reporter_device_id IS NOT NULL AND
    incident_type IS NOT NULL AND
    description IS NOT NULL AND
    created_at > 0 AND
    verification_status = 'UNVERIFIED'
);

-- Policy 3: UPDATE - Only Coordinators and Admins can update triage status or assign units
DROP POLICY IF EXISTS "Allow authorized staff to update emergency reports" ON public.emergency_reports;
CREATE POLICY "Allow authorized staff to update emergency reports"
ON public.emergency_reports
FOR UPDATE
TO authenticated
USING (
    public.get_current_user_role() IN ('admin', 'coordinator')
)
WITH CHECK (
    public.get_current_user_role() IN ('admin', 'coordinator')
);

-- Policy 4: DELETE - Strictly restricted to System Administrators
DROP POLICY IF EXISTS "Allow authenticated admins to delete emergency reports" ON public.emergency_reports;
CREATE POLICY "Allow authenticated admins to delete emergency reports"
ON public.emergency_reports
FOR DELETE
TO authenticated
USING (
    public.get_current_user_role() = 'admin'
);

-- -----------------------------------------------------------------------------
-- 7. Hardened RLS Policies: network_messages
-- Every policy is directly preceded by DROP POLICY IF EXISTS to guarantee idempotency
-- -----------------------------------------------------------------------------

-- Policy 1: SELECT - Only Coordinators and Admins can inspect mesh network telemetry
DROP POLICY IF EXISTS "Allow authorized roles to read network messages" ON public.network_messages;
CREATE POLICY "Allow authorized roles to read network messages"
ON public.network_messages
FOR SELECT
TO authenticated
USING (
    public.get_current_user_role() IN ('admin', 'coordinator')
);

-- Policy 2: INSERT - Field devices can log mesh message transit and hop telemetry
DROP POLICY IF EXISTS "Allow field devices to insert network messages" ON public.network_messages;
CREATE POLICY "Allow field devices to insert network messages"
ON public.network_messages
FOR INSERT
TO anon, authenticated
WITH CHECK (
    message_id IS NOT NULL AND
    source_device_id IS NOT NULL AND
    created_at > 0
);

-- -----------------------------------------------------------------------------
-- 8. Hardened RLS Policies: user_roles
-- Every policy is directly preceded by DROP POLICY IF EXISTS to guarantee idempotency
-- -----------------------------------------------------------------------------

-- Policy 1: SELECT - Authenticated users can view user roles
DROP POLICY IF EXISTS "Allow authenticated users to read roles" ON public.user_roles;
CREATE POLICY "Allow authenticated users to read roles"
ON public.user_roles
FOR SELECT
TO authenticated
USING (true);

-- Policy 2: ALL - Only Admins can create, update, or remove user roles
DROP POLICY IF EXISTS "Allow admins to manage roles" ON public.user_roles;
CREATE POLICY "Allow admins to manage roles"
ON public.user_roles
FOR ALL
TO authenticated
USING (
    public.get_current_user_role() = 'admin'
);

-- -----------------------------------------------------------------------------
-- 9. Realtime Publication Registration (Safe & Idempotent)
-- -----------------------------------------------------------------------------

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_publication_tables
        WHERE pubname = 'supabase_realtime'
          AND schemaname = 'public'
          AND tablename = 'emergency_reports'
    ) THEN
        ALTER PUBLICATION supabase_realtime ADD TABLE public.emergency_reports;
    END IF;
END $$;
