import { createClient } from '@supabase/supabase-js';

const SUPABASE_URL = process.env.REACT_APP_SUPABASE_URL || 'https://yarbshxeeufpgquawcuy.supabase.co';
const SUPABASE_PUBLISHABLE_KEY =
  process.env.REACT_APP_SUPABASE_PUBLISHABLE_KEY || 'sb_publishable_pnaLVfP6H6Kxi5wCPhSO2A_aK4zo24t';

export const supabase = createClient(SUPABASE_URL, SUPABASE_PUBLISHABLE_KEY);

