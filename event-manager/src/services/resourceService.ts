import { BackendExternalLink } from '../types/BackendExternalLink';
import { ResourceCategory } from '../types/ResourceCategory';
import { supabase } from '../lib/supabase';
import { toResource, toResourceInsert } from './supabaseMappers';

const resourceFields = 'id, resource_type, display_name, url, description';
const categoryFields = 'key, display_name';

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
  async getAllResources(excludedTypes: string[] = []): Promise<BackendExternalLink[]> {
    let query = supabase
      .from('resources')
      .select(resourceFields)
      .order('resource_type', { ascending: true })
      .order('created_at', { ascending: false });

    query = applyResourceTypeExclusions(query, excludedTypes);

    const { data, error } = await query;
    if (error) throw error;
    return (data || []).map(toResource);
  },

  async getResourceCategories(excludedTypes: string[] = []): Promise<ResourceCategory[]> {
    let categoriesQuery = supabase
      .from('resource_categories')
      .select(categoryFields)
      .order('display_name', { ascending: true });

    categoriesQuery = applyCategoryKeyExclusions(categoriesQuery, excludedTypes);

    const { data: categoriesData, error: categoriesError } = await categoriesQuery;

    // Backward-compatible fallback if resource_categories table isn't available yet.
    if (categoriesError) {
      let resourceTypeQuery = supabase
        .from('resources')
        .select('resource_type')
        .order('resource_type', { ascending: true });

      resourceTypeQuery = applyResourceTypeExclusions(resourceTypeQuery, excludedTypes);

      const { data: resourceTypeData, error: resourceTypeError } = await resourceTypeQuery;
      if (resourceTypeError) throw resourceTypeError;

      const uniqueKeys = Array.from(
        new Set((resourceTypeData || []).map((row) => row.resource_type).filter(Boolean))
      );
      return uniqueKeys.map((key) => ({ key, displayName: formatCategoryLabel(key) }));
    }

    return (categoriesData || []).map(toResourceCategory);
  },

  async createResourceCategory(displayName: string): Promise<ResourceCategory> {
    const trimmedDisplayName = displayName.trim();
    const key = normalizeResourceCategoryKey(trimmedDisplayName);

    if (!trimmedDisplayName || !key) {
      throw new Error('Category name must include letters or numbers');
    }

    const { data, error } = await supabase
      .from('resource_categories')
      .upsert(
        {
          key,
          display_name: trimmedDisplayName,
        },
        {
          onConflict: 'key',
        }
      )
      .select(categoryFields)
      .single();

    if (error) throw error;
    return toResourceCategory(data);
  },

  async createResource(resource: BackendExternalLink, resourceType: string): Promise<BackendExternalLink> {
    const normalizedType = normalizeResourceCategoryKey(resourceType);
    const { data, error } = await supabase
      .from('resources')
      .insert(toResourceInsert(resource, normalizedType))
      .select(resourceFields)
      .single();
    if (error) throw error;
    return toResource(data);
  },

  async updateResource(id: string, resource: BackendExternalLink, resourceType: string): Promise<BackendExternalLink> {
    const normalizedType = normalizeResourceCategoryKey(resourceType);
    const { data, error } = await supabase
      .from('resources')
      .update(toResourceInsert(resource, normalizedType))
      .eq('id', id)
      .select(resourceFields)
      .single();
    if (error) throw error;
    return toResource(data);
  },

  async deleteResource(id: string): Promise<void> {
    const { error } = await supabase.from('resources').delete().eq('id', id);
    if (error) throw error;
  },
};
