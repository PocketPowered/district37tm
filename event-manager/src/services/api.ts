import { Event } from '../types/Event';
import { BackendExternalLink } from '../types/BackendExternalLink';
import { supabase } from '../lib/supabase';
import { toEvent, toEventInsert, toResource, toResourceInsert } from './supabaseMappers';

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

export const notificationService = {
  sendNotification: async (title: string, body: string, topic: string) => {
    const { data: inserted, error: insertError } = await supabase
      .from('notifications')
      .insert({
        title,
        body,
        topic: topic || 'GENERAL',
      })
      .select('id')
      .single();
    if (insertError) throw insertError;

    const { data, error } = await supabase.functions.invoke('send-notification', {
      body: {
        notificationId: inserted.id,
      },
    });
    if (error) throw error;
    return data;
  },
};

export const referenceService = {
  getAllReferences: async (): Promise<BackendExternalLink[]> => {
    const { data, error } = await supabase
      .from('resources')
      .select('id, display_name, url, description')
      .eq('resource_type', 'general')
      .order('created_at', { ascending: false });
    if (error) throw error;
    return (data || []).map(toResource);
  },

  createReference: async (reference: Omit<BackendExternalLink, 'id'>) => {
    const { data, error } = await supabase
      .from('resources')
      .insert(toResourceInsert({ ...reference, id: null }, 'general'))
      .select('id, display_name, url, description')
      .single();
    if (error) throw error;
    return toResource(data);
  },

  updateReference: async (id: string, reference: Omit<BackendExternalLink, 'id'>) => {
    const { data, error } = await supabase
      .from('resources')
      .update(toResourceInsert({ ...reference, id: null }, 'general'))
      .eq('id', id)
      .eq('resource_type', 'general')
      .select('id, display_name, url, description')
      .single();
    if (error) throw error;
    return toResource(data);
  },

  deleteReference: async (id: string) => {
    const { error } = await supabase.from('resources').delete().eq('id', id).eq('resource_type', 'general');
    if (error) throw error;
    return true;
  },
};

