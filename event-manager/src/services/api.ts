import axios from 'axios';
import { Event } from '../types/Event';
import { apiConfig } from '../config/api';

const api = axios.create({
  baseURL: apiConfig.baseUrl,
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
  getAllEvents: async (): Promise<Event[]> => {
    const response = await fetch(`${apiConfig.baseUrl}/events/all`);
    if (!response.ok) {
      throw new Error('Failed to fetch events');
    }
    return response.json();
  },

  getEventsByDate: async (date: number): Promise<Event[]> => {
    const response = await fetch(`${apiConfig.baseUrl}/events?date=${date}`);
    if (!response.ok) {
      throw new Error('Failed to fetch events for date');
    }
    return response.json();
  },

  getEvent: async (id: number): Promise<Event> => {
    const response = await fetch(`${apiConfig.baseUrl}/event/${id}`);
    if (!response.ok) {
      throw new Error('Failed to fetch event');
    }
    return response.json();
  },

  createEvent: (event: Event) => api.post('/events', event, {
    headers: {
      'Content-Type': 'application/json'
    }
  }).then(res => res.data),
  updateEvent: (id: number, event: Event) => api.put(`/event/${id}`, event, {
    headers: {
      'Content-Type': 'application/json'
    }
  }).then(res => res.data),
  deleteEvent: (id: number) => api.delete(`/event/${id}`).then(res => res.data),
};

export const notificationService = {
  sendNotification: (title: string, body: string, topic: string) =>
    api.post('/notifications', {
      title,
      body,
      topic
    }, {
      headers: {
        'Content-Type': 'application/json'
      }
    }).then(res => res.data)
}; 