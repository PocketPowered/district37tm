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
  getAllEvents: () => api.get('/events/all').then(res => res.data),
  getEvent: (id: number) => api.get(`/event/${id}`).then(res => res.data),
  createEvent: (event: any) => api.post('/events', event).then(res => res.data),
  updateEvent: (id: number, event: any) => api.put(`/event/${id}`, event).then(res => res.data),
  deleteEvent: (id: number) => api.delete(`/event/${id}`).then(res => res.data)
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