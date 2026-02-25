import { supabase } from '../lib/supabase';

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
    const { data, error } = await supabase
      .from('conference_dates')
      .select('date_key')
      .order('date_key', { ascending: true });
    if (error) throw error;
    return (data || []).map((row) => row.date_key as number);
  },

  async addDate(timestamp: number): Promise<number> {
    const { error } = await supabase.from('conference_dates').insert({ date_key: timestamp });
    if (error) throw error;
    return timestamp;
  },

  async removeDate(timestamp: number): Promise<void> {
    const { error: deleteEventsError } = await supabase.from('events').delete().eq('date_key', timestamp);
    if (deleteEventsError) throw deleteEventsError;

    const { error } = await supabase.from('conference_dates').delete().eq('date_key', timestamp);
    if (error) throw error;
  },

  async getAvailableDates(): Promise<number[]> {
    return this.getDates();
  },
};

