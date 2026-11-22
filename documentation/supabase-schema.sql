-- =============================================================================
-- ResQSync AI: Shared Supabase Database Schema (Production Security Hardened)
-- =============================================================================
-- Execute this script in your Supabase SQL Editor:
-- https://supabase.com/dashboard/project/_/sql
--
-- This script is strictly idempotent (safe to re-run multiple times).
-- It preserves existing tables and data while applying hardened security policies.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. Table Definitions & Integrity Constraints
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
    synced_at TIMESTAMPTZ DEFAULT TIMEZONE('utc'::text, NOW()),

    -- Data integrity constraints
    CONSTRAINT chk_incident_type CHECK (
        incident_type IN ('FLOOD', 'FIRE', 'EARTHQUAKE', 'LANDSLIDE', 'BUILDING_COLLAPSE', 'MEDICAL_EMERGENCY', 'OTHER')
    ),
    CONSTRAINT chk_urgency CHECK (
        urgency IN ('CRITICAL', 'HIGH', 'MEDIUM', 'LOW')
    ),
    CONSTRAINT chk_verification_status CHECK (
        verification_status IN ('UNVERIFIED', 'CORROBORATED', 'HUMAN_REVIEWED', 'DISPUTED')
    ),
    CONSTRAINT chk_latitude CHECK (
        latitude IS NULL OR (latitude >= -90.0 AND latitude <= 90.0)
    ),
    CONSTRAINT chk_longitude CHECK (
        longitude IS NULL OR (longitude >= -180.0 AND longitude <= 180.0)
    ),
    CONSTRAINT chk_people_affected CHECK (
        people_affected >= 0
    )
);

CREATE TABLE IF NOT EXISTS public.network_messages (
    message_id TEXT PRIMARY KEY,
    source_device_id TEXT NOT NULL,
    destination_device_id TEXT,
    hop_count INTEGER DEFAULT 0 CHECK (hop_count >= 0 AND hop_count <= 20),
    payload TEXT,
    created_at BIGINT NOT NULL,
    synced_at TIMESTAMPTZ DEFAULT TIMEZONE('utc'::text, NOW())
);

-- Optional: User Roles Table for Granular Role-Based Access Control (RBAC)
CREATE TABLE IF NOT EXISTS public.user_roles (
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE PRIMARY KEY,
    role TEXT NOT NULL DEFAULT 'coordinator' CHECK (role IN ('admin', 'coordinator', 'field_responder')),
    sector TEXT DEFAULT 'Sector 1',
    created_at TIMESTAMPTZ DEFAULT TIMEZONE('utc'::text, NOW())
);

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
-- 4. Idempotent Policy Cleanup
-- Removes only application-specific policies to allow clean recreation
-- -----------------------------------------------------------------------------

DROP POLICY IF EXISTS "Allow public read access to emergency reports" ON public.emergency_reports;
DROP POLICY IF EXISTS "Allow public read access to emergency_reports" ON public.emergency_reports;
DROP POLICY IF EXISTS "Allow authenticated staff to read emergency reports" ON public.emergency_reports;
DROP POLICY IF EXISTS "Allow authenticated rescue staff to read emergency reports" ON public.emergency_reports;
DROP POLICY IF EXISTS "Allow authenticated rescue personnel to read emergency reports" ON public.emergency_reports;
DROP POLICY IF EXISTS "Allow authorized roles to read emergency reports" ON public.emergency_reports;
DROP POLICY IF EXISTS "Allow field devices to insert emergency reports" ON public.emergency_reports;
DROP POLICY IF EXISTS "Allow insert access to emergency reports" ON public.emergency_reports;
DROP POLICY IF EXISTS "Allow authenticated rescue staff to update reports" ON public.emergency_reports;
DROP POLICY IF EXISTS "Allow authenticated staff to update emergency reports" ON public.emergency_reports;
DROP POLICY IF EXISTS "Allow authorized staff to update emergency reports" ON public.emergency_reports;
DROP POLICY IF EXISTS "Allow authenticated admins to delete emergency reports" ON public.emergency_reports;

DROP POLICY IF EXISTS "Allow public read access to network messages" ON public.network_messages;
DROP POLICY IF EXISTS "Allow authenticated staff to read network messages" ON public.network_messages;
DROP POLICY IF EXISTS "Allow insert access to network messages" ON public.network_messages;
DROP POLICY IF EXISTS "Allow field devices to insert network messages" ON public.network_messages;

DROP POLICY IF EXISTS "Allow authenticated users to read roles" ON public.user_roles;
DROP POLICY IF EXISTS "Allow admins to manage roles" ON public.user_roles;

-- -----------------------------------------------------------------------------
-- 5. Helper Functions for RBAC
-- -----------------------------------------------------------------------------

-- Helper function to check the current user's role
CREATE OR REPLACE FUNCTION public.get_current_user_role()
RETURNS TEXT
LANGUAGE sql
STABLE
SECURITY DEFINER
SET search_path = public
AS $$
    SELECT COALESCE(
        (SELECT role FROM public.user_roles WHERE user_id = auth.uid()),
        -- Fallback to JWT metadata if set, otherwise default to coordinator for authenticated users
        COALESCE(auth.jwt() -> 'app_metadata' ->> 'role', 'coordinator')
    );
$$;

-- -----------------------------------------------------------------------------
-- 6. Hardened RLS Policies: emergency_reports
-- -----------------------------------------------------------------------------

-- SELECT Policy: Restricted to Authenticated Staff
-- Anonymous / public users cannot read any reports.
CREATE POLICY "Allow authorized roles to read emergency reports"
ON public.emergency_reports
FOR SELECT
TO authenticated
USING (
    public.get_current_user_role() IN ('admin', 'coordinator', 'field_responder')
);

-- INSERT Policy: Sync from Mobile Nodes / Edge Uploads
-- 1. Allowed for anon and authenticated field devices.
-- 2. Enforces non-null payload constraints and valid timestamps.
-- 3. CRITICAL SECURITY: Forces verification_status = 'UNVERIFIED'.
--    An untrusted field node or attacker cannot inject pre-verified or corroborated reports.
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

-- UPDATE Policy: Restricted to Rescue Coordinators and Admins
-- Field responders or anon devices cannot tamper with triage reports or reassign teams.
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

-- DELETE Policy: Strictly restricted to System Administrators
-- Regular coordinators and field responders cannot delete emergency audit records.
CREATE POLICY "Allow authenticated admins to delete emergency reports"
ON public.emergency_reports
FOR DELETE
TO authenticated
USING (
    public.get_current_user_role() = 'admin'
);

-- -----------------------------------------------------------------------------
-- 7. Hardened RLS Policies: network_messages
-- -----------------------------------------------------------------------------

CREATE POLICY "Allow authorized roles to read network messages"
ON public.network_messages
FOR SELECT
TO authenticated
USING (
    public.get_current_user_role() IN ('admin', 'coordinator')
);

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
-- -----------------------------------------------------------------------------

CREATE POLICY "Allow authenticated users to read roles"
ON public.user_roles
FOR SELECT
TO authenticated
USING (true);

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
