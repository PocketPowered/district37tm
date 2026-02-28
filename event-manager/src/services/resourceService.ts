import { BackendExternalLink } from '../types/BackendExternalLink';
import { supabase } from '../lib/supabase';
import { toResource, toResourceInsert } from './supabaseMappers';

const fields = 'id, resource_type, display_name, url, description';

const applyExcludedTypes = <T extends { neq: (column: string, value: string) => T }>(
  query: T,
  excludedTypes: string[]
): T => {
  let nextQuery = query;
  excludedTypes.forEach((type) => {
    nextQuery = nextQuery.neq('resource_type', type);
  });
  return nextQuery;
};

export const resourceService = {
  async getAllResources(excludedTypes: string[] = []): Promise<BackendExternalLink[]> {
    let query = supabase
      .from('resources')
      .select(fields)
      .order('resource_type', { ascending: true })
      .order('created_at', { ascending: false });

    query = applyExcludedTypes(query, excludedTypes);

    const { data, error } = await query;
    if (error) throw error;
    return (data || []).map(toResource);
  },

  async getResourceCategories(excludedTypes: string[] = []): Promise<string[]> {
    let query = supabase
      .from('resources')
      .select('resource_type')
      .order('resource_type', { ascending: true });

    query = applyExcludedTypes(query, excludedTypes);

    const { data, error } = await query;
    if (error) throw error;

    return Array.from(new Set((data || []).map((row) => row.resource_type).filter(Boolean)));
  },

  async createResource(resource: BackendExternalLink, resourceType: string): Promise<BackendExternalLink> {
    const { data, error } = await supabase
      .from('resources')
      .insert(toResourceInsert(resource, resourceType))
      .select(fields)
      .single();
    if (error) throw error;
    return toResource(data);
  },

  async updateResource(id: string, resource: BackendExternalLink, resourceType: string): Promise<BackendExternalLink> {
    const { data, error } = await supabase
      .from('resources')
      .update(toResourceInsert(resource, resourceType))
      .eq('id', id)
      .select(fields)
      .single();
    if (error) throw error;
    return toResource(data);
  },

  async deleteResource(id: string): Promise<void> {
    const { error } = await supabase.from('resources').delete().eq('id', id);
    if (error) throw error;
  },
};
