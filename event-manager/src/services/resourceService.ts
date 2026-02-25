import { BackendExternalLink } from '../types/BackendExternalLink';
import { supabase } from '../lib/supabase';
import { toResource, toResourceInsert } from './supabaseMappers';

const fields = 'id, display_name, url, description';

export const resourceService = {
  async getAllResources(): Promise<BackendExternalLink[]> {
    const { data, error } = await supabase
      .from('resources')
      .select(fields)
      .eq('resource_type', 'general')
      .order('created_at', { ascending: false });
    if (error) throw error;
    return (data || []).map(toResource);
  },

  async createResource(resource: BackendExternalLink): Promise<BackendExternalLink> {
    const { data, error } = await supabase
      .from('resources')
      .insert(toResourceInsert(resource, 'general'))
      .select(fields)
      .single();
    if (error) throw error;
    return toResource(data);
  },

  async updateResource(id: string, resource: BackendExternalLink): Promise<BackendExternalLink> {
    const { data, error } = await supabase
      .from('resources')
      .update(toResourceInsert(resource, 'general'))
      .eq('id', id)
      .eq('resource_type', 'general')
      .select(fields)
      .single();
    if (error) throw error;
    return toResource(data);
  },

  async deleteResource(id: string): Promise<void> {
    const { error } = await supabase.from('resources').delete().eq('id', id).eq('resource_type', 'general');
    if (error) throw error;
  },

  async getAllFirstTimerResources(): Promise<BackendExternalLink[]> {
    const { data, error } = await supabase
      .from('resources')
      .select(fields)
      .eq('resource_type', 'first_timer')
      .order('created_at', { ascending: false });
    if (error) throw error;
    return (data || []).map(toResource);
  },

  async createFirstTimerResource(resource: BackendExternalLink): Promise<BackendExternalLink> {
    const { data, error } = await supabase
      .from('resources')
      .insert(toResourceInsert(resource, 'first_timer'))
      .select(fields)
      .single();
    if (error) throw error;
    return toResource(data);
  },

  async updateFirstTimerResource(id: string, resource: BackendExternalLink): Promise<BackendExternalLink> {
    const { data, error } = await supabase
      .from('resources')
      .update(toResourceInsert(resource, 'first_timer'))
      .eq('id', id)
      .eq('resource_type', 'first_timer')
      .select(fields)
      .single();
    if (error) throw error;
    return toResource(data);
  },

  async deleteFirstTimerResource(id: string): Promise<void> {
    const { error } = await supabase
      .from('resources')
      .delete()
      .eq('id', id)
      .eq('resource_type', 'first_timer');
    if (error) throw error;
  },
};

