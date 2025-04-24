import { apiConfig } from '../config/api';
import { BackendExternalLink } from '../types/BackendExternalLink';

const API_BASE_URL = apiConfig.baseUrl;

export const referenceService = {
  async getAllReferences(): Promise<BackendExternalLink[]> {
    const response = await fetch(`${API_BASE_URL}/references`);
    if (!response.ok) {
      throw new Error('Failed to fetch references');
    }
    return response.json();
  },

  async createReference(reference: BackendExternalLink): Promise<BackendExternalLink> {
    const response = await fetch(`${API_BASE_URL}/references`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(reference),
    });
    if (!response.ok) {
      throw new Error('Failed to create reference');
    }
    return response.json();
  },

  async updateReference(id: string, reference: BackendExternalLink): Promise<BackendExternalLink> {
    const response = await fetch(`${API_BASE_URL}/references/${id}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(reference),
    });
    if (!response.ok) {
      throw new Error('Failed to update reference');
    }
    return response.json();
  },

  async deleteReference(id: string): Promise<void> {
    const response = await fetch(`${API_BASE_URL}/references/${id}`, {
      method: 'DELETE',
    });
    if (!response.ok) {
      throw new Error('Failed to delete reference');
    }
  },
}; 