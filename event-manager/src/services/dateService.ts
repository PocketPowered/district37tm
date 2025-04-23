import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
  },
  withCredentials: true,
});

export interface DateResponse {
  timestamp: number;
}

export interface DateInfo {
  displayName: string;
  dateKey: string;
}

export interface TabInfo {
  displayName: string;
  dateKey: string;
}

export const dateService = {
  async getDates(): Promise<number[]> {
    const response = await api.get<number[]>('/dates');
    return response.data;
  },

  async addDate(timestamp: number): Promise<number> {
    const response = await api.post<number>('/dates', timestamp);
    return response.data;
  },

  async removeDate(timestamp: number): Promise<void> {
    await api.delete(`/dates/${timestamp}`);
  },

  async getAvailableDates(): Promise<number[]> {
    return this.getDates();
  }
}; 