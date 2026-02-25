export interface ApiConfig {
  supabaseUrl: string;
  isProduction: boolean;
}

const LOCAL_SUPABASE_URL = process.env.REACT_APP_SUPABASE_URL_DEV || 'https://yarbshxeeufpgquawcuy.supabase.co';
const PROD_SUPABASE_URL = process.env.REACT_APP_SUPABASE_URL || 'https://yarbshxeeufpgquawcuy.supabase.co';

// Get the current environment from localStorage or default to production
const getEnvironment = (): boolean => {
  const stored = localStorage.getItem('apiEnvironment');
  return stored ? JSON.parse(stored) : true; // Default to production
};

export const apiConfig: ApiConfig = {
  supabaseUrl: getEnvironment() ? PROD_SUPABASE_URL : LOCAL_SUPABASE_URL,
  isProduction: getEnvironment()
};

export const toggleEnvironment = () => {
  const newIsProduction = !apiConfig.isProduction;
  localStorage.setItem('apiEnvironment', JSON.stringify(newIsProduction));
  apiConfig.isProduction = newIsProduction;
  apiConfig.supabaseUrl = newIsProduction ? PROD_SUPABASE_URL : LOCAL_SUPABASE_URL;
  window.location.reload(); // Reload to apply changes
}; 
