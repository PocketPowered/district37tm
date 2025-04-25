import { apiConfig } from '../config/api';
import { Location } from '../types/Location';

const API_BASE_URL = apiConfig.baseUrl;

export const locationService = {
  async getAllLocations(): Promise<Location[]> {
    const response = await fetch(`${API_BASE_URL}/locations`);
    if (!response.ok) {
      throw new Error('Failed to fetch locations');
    }
    return response.json();
  },

  async createLocation(location: Location): Promise<Location> {
    const response = await fetch(`${API_BASE_URL}/locations`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(location),
    });
    if (!response.ok) {
      throw new Error('Failed to create location');
    }
    return response.json();
  },

  async updateLocation(id: string, location: Location): Promise<Location> {
    const response = await fetch(`${API_BASE_URL}/locations/${id}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(location),
    });
    if (!response.ok) {
      throw new Error('Failed to update location');
    }
    return response.json();
  },

  async deleteLocation(id: string): Promise<void> {
    const response = await fetch(`${API_BASE_URL}/locations/${id}`, {
      method: 'DELETE',
    });
    if (!response.ok) {
      throw new Error('Failed to delete location');
    }
  },
}; 