import { Event } from '../types/Event';
import { supabase } from '../lib/supabase';
import { toEvent, toEventInsert } from './supabaseMappers';

export const eventService = {
  getAllEvents: async (conferenceId: number): Promise<Event[]> => {
    const { data, error } = await supabase
      .from('events')
      .select('*')
      .eq('conference_id', conferenceId)
      .order('start_time', { ascending: true });
    if (error) throw error;
    return (data || []).map(toEvent);
  },

  getEventsByDate: async (date: number, conferenceId: number): Promise<Event[]> => {
    const { data, error } = await supabase
      .from('events')
      .select('*')
      .eq('conference_id', conferenceId)
      .eq('date_key', date)
      .order('start_time', { ascending: true });
    if (error) throw error;
    return (data || []).map(toEvent);
  },

  getEvent: async (id: number, conferenceId: number): Promise<Event> => {
    const { data, error } = await supabase
      .from('events')
      .select('*')
      .eq('id', id)
      .eq('conference_id', conferenceId)
      .single();
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
      .eq('conference_id', event.conferenceId)
      .select()
      .single();
    if (error) throw error;
    return toEvent(data);
  },

  deleteEvent: async (id: number, conferenceId: number): Promise<void> => {
    const { error } = await supabase.from('events').delete().eq('id', id).eq('conference_id', conferenceId);
    if (error) throw error;
  },
};
