import { Event } from '../types/Event';
import { supabase } from '../lib/supabase';
import { toEvent, toEventInsert } from './supabaseMappers';

export const eventService = {
  getAllEvents: async (): Promise<Event[]> => {
    const { data, error } = await supabase
      .from('events')
      .select('*')
      .order('start_time', { ascending: true });
    if (error) throw error;
    return (data || []).map(toEvent);
  },

  getEventsByDate: async (date: number): Promise<Event[]> => {
    const { data, error } = await supabase
      .from('events')
      .select('*')
      .eq('date_key', date)
      .order('start_time', { ascending: true });
    if (error) throw error;
    return (data || []).map(toEvent);
  },

  getEvent: async (id: number): Promise<Event> => {
    const { data, error } = await supabase.from('events').select('*').eq('id', id).single();
    if (error) throw error;
    return toEvent(data);
  },

  createEvent: async (event: Event): Promise<Event> => {
    const { data, error } = await supabase.from('events').insert(toEventInsert(event)).select().single();
    if (error) throw error;
    return toEvent(data);
  },

  updateEvent: async (id: number, event: Event): Promise<Event> => {
    const { data, error } = await supabase
      .from('events')
      .update(toEventInsert({ ...event, id }))
      .eq('id', id)
      .select()
      .single();
    if (error) throw error;
    return toEvent(data);
  },

  deleteEvent: async (id: number): Promise<void> => {
    const { error } = await supabase.from('events').delete().eq('id', id);
    if (error) throw error;
  },
};
