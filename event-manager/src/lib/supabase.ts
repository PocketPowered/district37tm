import { createClient } from '@supabase/supabase-js';
import { SUPABASE_PUBLISHABLE_KEY, SUPABASE_URL } from '../config/supabase';

export const supabase = createClient(SUPABASE_URL, SUPABASE_PUBLISHABLE_KEY);
