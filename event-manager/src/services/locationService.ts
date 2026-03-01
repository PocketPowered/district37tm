import { Location } from '../types/Location';
import { supabase } from '../lib/supabase';
import { toLocation, toLocationInsert } from './supabaseMappers';

export const locationService = {
  async getAllLocations(conferenceId: number): Promise<Location[]> {
    const { data, error } = await supabase
      .from('locations')
      .select('id, location_name, location_images')
      .eq('conference_id', conferenceId)
      .order('location_name', { ascending: true });
    if (error) throw error;
    return (data || []).map(toLocation);
  },

  async createLocation(location: Location, conferenceId: number): Promise<Location> {
    const { data, error } = await supabase
      .from('locations')
      .insert({ ...toLocationInsert(location), conference_id: conferenceId })
      .select('id, location_name, location_images')
      .single();
    if (error) throw error;
    return toLocation(data);
  },

  async updateLocation(id: string, location: Location, conferenceId: number): Promise<Location> {
    const { data, error } = await supabase
      .from('locations')
      .update(toLocationInsert(location))
      .eq('id', id)
      .eq('conference_id', conferenceId)
      .select('id, location_name, location_images')
      .single();
    if (error) throw error;
    return toLocation(data);
  },

  async deleteLocation(id: string, conferenceId: number): Promise<void> {
    const { error } = await supabase
      .from('locations')
      .delete()
      .eq('id', id)
      .eq('conference_id', conferenceId);
    if (error) throw error;
  },
};
