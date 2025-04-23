import axios from 'axios';
import { Event } from '../types/Event';

const API_BASE_URL = 'http://localhost:8080'; // Update this with your actual backend URL

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
  },
  withCredentials: true, // This is important for CORS
});

// Add a response interceptor for logging
api.interceptors.response.use(
  (response) => {
    console.log('API Response:', response);
    return response;
  },
  (error) => {
    console.error('API Error:', error);
    return Promise.reject(error);
  }
);

export const eventService = {
  getAllEvents: async () => {
    try {
      console.log('Fetching all events...');
      const response = await api.get<Event[]>('/events');
      console.log('Events fetched:', response.data);
      return response.data;
    } catch (error) {
      console.error('Error fetching events:', error);
      throw error;
    }
  },

  getEvent: async (id: number) => {
    try {
      const response = await api.get<Event>(`/event/${id}`);
      return response.data;
    } catch (error) {
      console.error(`Error fetching event ${id}:`, error);
      throw error;
    }
  },

  createEvent: async (event: Omit<Event, 'id'>) => {
    try {
      // Ensure all required fields are present
      const eventToCreate = {
        ...event,
        title: event.title || '',
        time: event.time || '',
        locationInfo: event.locationInfo || '',
        description: event.description || '',
        images: event.images || [],
        agenda: event.agenda || [],
        additionalLinks: event.additionalLinks || [],
        dateKey: event.dateKey || '',
      };
      const response = await api.post<Event>('/events', eventToCreate);
      return response.data;
    } catch (error) {
      console.error('Error creating event:', error);
      throw error;
    }
  },

  updateEvent: async (id: number, event: Partial<Event>) => {
    try {
      const response = await api.put<Event>(`/event/${id}`, event);
      return response.data;
    } catch (error) {
      console.error(`Error updating event ${id}:`, error);
      throw error;
    }
  },

  deleteEvent: async (id: number) => {
    try {
      await api.delete(`/event/${id}`);
    } catch (error) {
      console.error(`Error deleting event ${id}:`, error);
      throw error;
    }
  },
}; 