export interface ApiConfig {
  supabaseUrl: string;
  isProduction: boolean;
}

const LOCAL_SUPABASE_URL = process.env.REACT_APP_SUPABASE_URL_DEV || 'https://yarbshxeeufpgquawcuy.supabase.co';
const PROD_SUPABASE_URL = process.env.REACT_APP_SUPABASE_URL || 'https://yarbshxeeufpgquawcuy.supabase.co';

export const getEnvironment = (): boolean => {
  if (typeof window === 'undefined') return true;

  try {
    const stored = window.localStorage.getItem('apiEnvironment');
    if (!stored) return true;

    const parsed = JSON.parse(stored);
    return typeof parsed === 'boolean' ? parsed : true;
  } catch {
    // Recover from stale/corrupted localStorage values that can crash app bootstrap.
    window.localStorage.removeItem('apiEnvironment');
    return true;
  }
};

export const getSupabaseUrl = (isProduction: boolean): string =>
  isProduction ? PROD_SUPABASE_URL : LOCAL_SUPABASE_URL;

export const apiConfig: ApiConfig = {
  supabaseUrl: getSupabaseUrl(getEnvironment()),
  isProduction: getEnvironment()
};

export const toggleEnvironment = () => {
  const newIsProduction = !apiConfig.isProduction;
  window.localStorage.setItem('apiEnvironment', JSON.stringify(newIsProduction));
  apiConfig.isProduction = newIsProduction;
  apiConfig.supabaseUrl = getSupabaseUrl(newIsProduction);
  window.location.reload(); // Reload to apply changes
}; 
