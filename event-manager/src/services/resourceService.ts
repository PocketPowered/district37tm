import { BackendExternalLink } from '../types/BackendExternalLink';
import { ResourceCategory } from '../types/ResourceCategory';
import { supabase } from '../lib/supabase';
import { toResource, toResourceInsert } from './supabaseMappers';

const resourceFields = 'id, resource_type, display_name, url, description';
const categoryFields = 'conference_id, key, display_name';

export const normalizeResourceCategoryKey = (value: string): string => {
  return value
    .trim()
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '_')
    .replace(/^_+|_+$/g, '')
    .replace(/_+/g, '_');
};

const formatCategoryLabel = (key: string): string => {
  return key
    .split('_')
    .filter(Boolean)
    .map((segment) => segment.charAt(0).toUpperCase() + segment.slice(1))
    .join(' ');
};

const applyResourceTypeExclusions = <T extends { neq: (column: string, value: string) => T }>(
  query: T,
  excludedTypes: string[]
): T => {
  let nextQuery = query;
  excludedTypes.forEach((type) => {
    nextQuery = nextQuery.neq('resource_type', type);
  });
  return nextQuery;
};

const applyCategoryKeyExclusions = <T extends { neq: (column: string, value: string) => T }>(
  query: T,
  excludedTypes: string[]
): T => {
  let nextQuery = query;
  excludedTypes.forEach((type) => {
    nextQuery = nextQuery.neq('key', type);
  });
  return nextQuery;
};

const toResourceCategory = (row: { key: string; display_name: string | null }): ResourceCategory => ({
  key: row.key,
  displayName: row.display_name?.trim() || formatCategoryLabel(row.key),
});

export const resourceService = {
  async getAllResources(conferenceId: number, excludedTypes: string[] = []): Promise<BackendExternalLink[]> {
    let query = supabase
      .from('resources')
      .select(resourceFields)
      .eq('conference_id', conferenceId)
      .order('resource_type', { ascending: true })
      .order('created_at', { ascending: false });

    query = applyResourceTypeExclusions(query, excludedTypes);

    const { data, error } = await query;
    if (error) throw error;
    return (data || []).map(toResource);
  },

  async getResourceCategories(conferenceId: number, excludedTypes: string[] = []): Promise<ResourceCategory[]> {
    let categoriesQuery = supabase
      .from('resource_categories')
      .select(categoryFields)
      .eq('conference_id', conferenceId)
      .order('display_name', { ascending: true });

    categoriesQuery = applyCategoryKeyExclusions(categoriesQuery, excludedTypes);

    const { data: categoriesData, error: categoriesError } = await categoriesQuery;
    if (categoriesError) throw categoriesError;

    return (categoriesData || []).map(toResourceCategory);
  },

  async createResourceCategory(displayName: string, conferenceId: number): Promise<ResourceCategory> {
    const trimmedDisplayName = displayName.trim();
    const key = normalizeResourceCategoryKey(trimmedDisplayName);

    if (!trimmedDisplayName || !key) {
      throw new Error('Category name must include letters or numbers');
    }

    const { data, error } = await supabase
      .from('resource_categories')
      .upsert(
        {
          conference_id: conferenceId,
          key,
          display_name: trimmedDisplayName,
        },
        {
          onConflict: 'conference_id,key',
        }
      )
      .select(categoryFields)
      .single();

    if (error) throw error;
    return toResourceCategory(data);
  },

  async createResource(resource: BackendExternalLink, resourceType: string, conferenceId: number): Promise<BackendExternalLink> {
    const normalizedType = normalizeResourceCategoryKey(resourceType);
    const { data, error } = await supabase
      .from('resources')
      .insert({ ...toResourceInsert(resource, normalizedType), conference_id: conferenceId })
      .select(resourceFields)
      .single();
    if (error) throw error;
    return toResource(data);
  },

  async updateResource(
    id: string,
    resource: BackendExternalLink,
    resourceType: string,
    conferenceId: number
  ): Promise<BackendExternalLink> {
    const normalizedType = normalizeResourceCategoryKey(resourceType);
    const { data, error } = await supabase
      .from('resources')
      .update(toResourceInsert(resource, normalizedType))
      .eq('id', id)
      .eq('conference_id', conferenceId)
      .select(resourceFields)
      .single();
    if (error) throw error;
    return toResource(data);
  },

  async deleteResource(id: string, conferenceId: number): Promise<void> {
    const { error } = await supabase.from('resources').delete().eq('id', id).eq('conference_id', conferenceId);
    if (error) throw error;
  },
};
