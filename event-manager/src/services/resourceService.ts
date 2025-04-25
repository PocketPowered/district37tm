import { apiConfig } from '../config/api';
import { BackendExternalLink } from '../types/BackendExternalLink';

const API_BASE_URL = apiConfig.baseUrl;

export const resourceService = {
  async getAllResources(): Promise<BackendExternalLink[]> {
    const response = await fetch(`${API_BASE_URL}/resources`);
    if (!response.ok) {
      throw new Error('Failed to fetch resources');
    }
    return response.json();
  },

  async createResource(resource: BackendExternalLink): Promise<BackendExternalLink> {
    const response = await fetch(`${API_BASE_URL}/resources`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(resource),
    });
    if (!response.ok) {
      throw new Error('Failed to create resource');
    }
    return response.json();
  },

  async updateResource(id: string, resource: BackendExternalLink): Promise<BackendExternalLink> {
    const response = await fetch(`${API_BASE_URL}/resources/${id}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(resource),
    });
    if (!response.ok) {
      throw new Error('Failed to update resource');
    }
    return response.json();
  },

  async deleteResource(id: string): Promise<void> {
    const response = await fetch(`${API_BASE_URL}/resources/${id}`, {
      method: 'DELETE',
    });
    if (!response.ok) {
      throw new Error('Failed to delete resource');
    }
  },
}; 