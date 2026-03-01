import { supabase } from '../lib/supabase';
import { Conference } from '../types/Conference';

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

const ONE_DAY_MS = 86_400_000;

const dateIsoToDateKey = (dateIso: string): number => {
  return new Date(`${dateIso}T00:00:00.000Z`).getTime();
};

const dateKeyToIso = (dateKey: number): string => {
  return new Date(dateKey).toISOString().slice(0, 10);
};

const generateDateKeys = (startDateIso: string, endDateIso: string): number[] => {
  const start = dateIsoToDateKey(startDateIso);
  const end = dateIsoToDateKey(endDateIso);
  if (!Number.isFinite(start) || !Number.isFinite(end) || end < start) {
    return [];
  }

  const dates: number[] = [];
  for (let current = start; current <= end; current += ONE_DAY_MS) {
    dates.push(current);
  }
  return dates;
};

export const dateService = {
  async getDates(conference: Conference | null = null): Promise<number[]> {
    if (!conference?.startDate || !conference.endDate) {
      return [];
    }
    return generateDateKeys(conference.startDate, conference.endDate);
  },

  async addDate(timestamp: number, conference: Conference | null = null): Promise<number> {
    if (!conference) {
      throw new Error('Select a conference before adding dates.');
    }

    const targetIso = dateKeyToIso(timestamp);
    const nextStartDate =
      !conference.startDate || targetIso < conference.startDate ? targetIso : conference.startDate;
    const nextEndDate =
      !conference.endDate || targetIso > conference.endDate ? targetIso : conference.endDate;

    const { error } = await supabase
      .from('conferences')
      .update({
        start_date: nextStartDate,
        end_date: nextEndDate,
        updated_at: new Date().toISOString(),
      })
      .eq('id', conference.id);
    if (error) throw error;

    return timestamp;
  },

  async removeDate(timestamp: number, conference: Conference | null = null): Promise<void> {
    if (!conference) {
      throw new Error('Select a conference before removing dates.');
    }

    const targetIso = dateKeyToIso(timestamp);

    const { error: deleteEventsError } = await supabase
      .from('events')
      .delete()
      .eq('conference_id', conference.id)
      .eq('date_key', timestamp);
    if (deleteEventsError) throw deleteEventsError;

    if (!conference.startDate || !conference.endDate) {
      return;
    }

    let nextStartDate: string | null = conference.startDate;
    let nextEndDate: string | null = conference.endDate;

    if (conference.startDate === conference.endDate && conference.startDate === targetIso) {
      nextStartDate = null;
      nextEndDate = null;
    } else if (targetIso === conference.startDate) {
      nextStartDate = dateKeyToIso(dateIsoToDateKey(conference.startDate) + ONE_DAY_MS);
    } else if (targetIso === conference.endDate) {
      nextEndDate = dateKeyToIso(dateIsoToDateKey(conference.endDate) - ONE_DAY_MS);
    } else {
      throw new Error('Only boundary dates can be removed when using conference date ranges.');
    }

    const { error: conferenceError } = await supabase
      .from('conferences')
      .update({
        start_date: nextStartDate || null,
        end_date: nextEndDate || null,
        updated_at: new Date().toISOString(),
      })
      .eq('id', conference.id);
    if (conferenceError) throw conferenceError;
  },

  async getAvailableDates(conference: Conference | null = null): Promise<number[]> {
    return this.getDates(conference);
  },
};
