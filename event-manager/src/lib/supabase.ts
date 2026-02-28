import { createClient } from '@supabase/supabase-js';
import { SUPABASE_PUBLISHABLE_KEY, SUPABASE_URL } from '../config/supabase';
import { inMemoryAuthLock } from './supabaseLock';

export const supabase = createClient(SUPABASE_URL, SUPABASE_PUBLISHABLE_KEY, {
  auth: {
    // HashRouter uses URL fragments for routes, so OAuth must avoid fragment tokens.
    flowType: 'pkce',
    detectSessionInUrl: true,
    persistSession: true,
    autoRefreshToken: true,
    lock: inMemoryAuthLock,
  },
});
