import { createClient } from '@supabase/supabase-js';
import { getEnvironment, getSupabaseUrl } from '../config/api';

const SUPABASE_URL = getSupabaseUrl(getEnvironment());
const SUPABASE_PUBLISHABLE_KEY =
  process.env.REACT_APP_SUPABASE_PUBLISHABLE_KEY || 'sb_publishable_pnaLVfP6H6Kxi5wCPhSO2A_aK4zo24t';

export const supabase = createClient(SUPABASE_URL, SUPABASE_PUBLISHABLE_KEY);
