import { createClient, SupabaseClient } from '@supabase/supabase-js';

// Retrieve Supabase environment variables configured in .env.local
const supabaseUrl = import.meta.env.VITE_SUPABASE_URL as string | undefined;
const supabasePublishableKey = import.meta.env.VITE_SUPABASE_PUBLISHABLE_KEY as string | undefined;

export interface SupabaseConfigStatus {
  isValid: boolean;
  missingVars: string[];
  errorMessage?: string;
}

/**
 * Validates the presence of required Supabase environment variables.
 */
export const validateSupabaseConfig = (): SupabaseConfigStatus => {
  const missing: string[] = [];

  if (!supabaseUrl || supabaseUrl.trim() === '' || supabaseUrl.includes('your-project-id')) {
    missing.push('VITE_SUPABASE_URL');
  }

  if (
    !supabasePublishableKey ||
    supabasePublishableKey.trim() === '' ||
    supabasePublishableKey.includes('your-publishable-key')
  ) {
    missing.push('VITE_SUPABASE_PUBLISHABLE_KEY');
  }

  if (missing.length > 0) {
    return {
      isValid: false,
      missingVars: missing,
      errorMessage: `Supabase configuration missing in .env.local: ${missing.join(', ')}`
    };
  }

  return {
    isValid: true,
    missingVars: []
  };
};

const configStatus = validateSupabaseConfig();

if (!configStatus.isValid) {
  console.warn(
    `[ResQSync Supabase] ${configStatus.errorMessage}. Dashboard will operate in UI Development Mode.`
  );
}

/**
 * Initialized Supabase client.
 * Returns null if environment variables are not configured in .env.local.
 */
export const supabase: SupabaseClient | null = configStatus.isValid
  ? createClient(supabaseUrl!, supabasePublishableKey!)
  : null;

export interface ConnectionTestResult {
  isConfigured: boolean;
  isConnected: boolean;
  isTableFound: boolean;
  tableChecked: string;
  errorMessage: string | null;
  diagnosticDetails: string;
  checkedAt: number;
}

/**
 * Safely tests Supabase connectivity by attempting a 1-row read on 'emergency_reports'.
 * Does not mutate data, insert records, or create duplicate tables.
 */
export const testSupabaseConnection = async (): Promise<ConnectionTestResult> => {
  const timestamp = Date.now();
  const config = validateSupabaseConfig();

  if (!config.isValid || !supabase) {
    return {
      isConfigured: false,
      isConnected: false,
      isTableFound: false,
      tableChecked: 'emergency_reports',
      errorMessage: config.errorMessage || 'Supabase environment variables not found.',
      diagnosticDetails:
        'Client cannot initialise because VITE_SUPABASE_URL and/or VITE_SUPABASE_PUBLISHABLE_KEY are missing or unset in .env.local.',
      checkedAt: timestamp
    };
  }

  try {
    // Attempt a safe, non-mutating read limited to 1 column and 1 row
    const { error } = await supabase
      .from('emergency_reports')
      .select('report_id')
      .limit(1);

    if (error) {
      // Table does not exist error codes (PostgREST PGRST204 or PostgreSQL 42P01)
      if (
        error.code === 'PGRST204' ||
        error.code === 'PGRST205' ||
        error.code === '42P01' ||
        error.message?.toLowerCase().includes('relation') ||
        error.message?.toLowerCase().includes('does not exist')
      ) {
        return {
          isConfigured: true,
          isConnected: true, // Reached Supabase successfully, but schema is not applied
          isTableFound: false,
          tableChecked: 'emergency_reports',
          errorMessage: error.message,
          diagnosticDetails:
            "Successfully contacted your Supabase project, but the 'emergency_reports' table was not found. Please apply the schema by running 'documentation/supabase-schema.sql' in your Supabase SQL Editor.",
          checkedAt: timestamp
        };
      }

      // Permission / RLS denial
      if (error.code === '42501' || error.message?.toLowerCase().includes('policy')) {
        return {
          isConfigured: true,
          isConnected: true,
          isTableFound: true,
          tableChecked: 'emergency_reports',
          errorMessage: error.message,
          diagnosticDetails:
            "Contacted Supabase, but Row Level Security (RLS) prevented reading. Verify SELECT policies in 'documentation/supabase-schema.sql'.",
          checkedAt: timestamp
        };
      }

      // Other API / Auth / Network errors
      return {
        isConfigured: true,
        isConnected: false,
        isTableFound: false,
        tableChecked: 'emergency_reports',
        errorMessage: error.message,
        diagnosticDetails: `Supabase request failed: ${error.message} (Code: ${error.code || 'unknown'})`,
        checkedAt: timestamp
      };
    }

    // Success: client connected, table exists, and RLS allowed read
    return {
      isConfigured: true,
      isConnected: true,
      isTableFound: true,
      tableChecked: 'emergency_reports',
      errorMessage: null,
      diagnosticDetails:
        "Full read access confirmed: Successfully reached Supabase and verified read permissions on the 'emergency_reports' table.",
      checkedAt: timestamp
    };
  } catch (err: any) {
    return {
      isConfigured: true,
      isConnected: false,
      isTableFound: false,
      tableChecked: 'emergency_reports',
      errorMessage: err?.message || 'Network exception during connection probe',
      diagnosticDetails: `Failed to contact Supabase endpoint at ${supabaseUrl}. Verify your internet connection and Supabase URL.`,
      checkedAt: timestamp
    };
  }
};
