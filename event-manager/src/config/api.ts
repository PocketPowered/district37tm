export interface ApiConfig {
  baseUrl: string;
  isProduction: boolean;
}

const LOCAL_API_URL = 'http://localhost:8080';
const PROD_API_URL = 'https://district37tm-dve3cncpajdnh5h3.eastus2-01.azurewebsites.net';

// Get the current environment from localStorage or default to production
const getEnvironment = (): boolean => {
  const stored = localStorage.getItem('apiEnvironment');
  return stored ? JSON.parse(stored) : true; // Default to production
};

export const apiConfig: ApiConfig = {
  baseUrl: getEnvironment() ? PROD_API_URL : LOCAL_API_URL,
  isProduction: getEnvironment()
};

export const toggleEnvironment = () => {
  const newIsProduction = !apiConfig.isProduction;
  localStorage.setItem('apiEnvironment', JSON.stringify(newIsProduction));
  apiConfig.isProduction = newIsProduction;
  apiConfig.baseUrl = newIsProduction ? PROD_API_URL : LOCAL_API_URL;
  window.location.reload(); // Reload to apply changes
}; 