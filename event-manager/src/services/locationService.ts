import { Location } from '../types/Location';
import { supabase } from '../lib/supabase';
import { toLocation, toLocationInsert } from './supabaseMappers';

export const locationService = {
  async getAllLocations(): Promise<Location[]> {
    const { data, error } = await supabase
      .from('locations')
      .select('id, location_name, location_images')
      .order('location_name', { ascending: true });
    if (error) throw error;
    return (data || []).map(toLocation);
  },

  async createLocation(location: Location): Promise<Location> {
    const { data, error } = await supabase
      .from('locations')
      .insert(toLocationInsert(location))
      .select('id, location_name, location_images')
      .single();
    if (error) throw error;
    return toLocation(data);
  },

  async updateLocation(id: string, location: Location): Promise<Location> {
    const { data, error } = await supabase
      .from('locations')
      .update(toLocationInsert(location))
      .eq('id', id)
      .select('id, location_name, location_images')
      .single();
    if (error) throw error;
    return toLocation(data);
  },

  async deleteLocation(id: string): Promise<void> {
    const { error } = await supabase.from('locations').delete().eq('id', id);
    if (error) throw error;
  },
};

