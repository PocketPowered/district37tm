import React, { useEffect, useState } from 'react';
import {
  Box,
  TextField,
  Button,
  Typography,
  Paper,
  CircularProgress,
  Alert,
  Stack,
} from '@mui/material';
import { useNavigate, useParams } from 'react-router-dom';
import { Event } from '../types/Event';
import { eventService } from '../services/api';

const EventForm: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [event, setEvent] = useState<Partial<Event>>({
    title: '',
    time: '',
    locationInfo: '',
    description: '',
    agenda: '',
    additionalLinks: [],
    dateKey: '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (id) {
      loadEvent();
    }
  }, [id]);

  const loadEvent = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await eventService.getEvent(parseInt(id!));
      setEvent(data);
    } catch (error) {
      setError('Failed to load event. Please try again.');
      console.error('Error loading event:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setEvent((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      // Ensure all required fields are present
      const eventToSave = {
        ...event,
        title: event.title || '',
        time: event.time || '',
        locationInfo: event.locationInfo || '',
        description: event.description || '',
        agenda: event.agenda || '',
        additionalLinks: event.additionalLinks || [],
        dateKey: event.dateKey || '',
      };

      if (id) {
        await eventService.updateEvent(parseInt(id), eventToSave);
      } else {
        await eventService.createEvent(eventToSave as Omit<Event, 'id'>);
      }
      navigate('/');
    } catch (error) {
      setError('Failed to save event. Please try again.');
      console.error('Error saving event:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading && !event.id) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', p: 3 }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box sx={{ p: 3 }}>
      <Paper sx={{ p: 3 }}>
        <Typography variant="h5" gutterBottom>
          {id ? 'Edit Event' : 'Create New Event'}
        </Typography>
        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}
        <form onSubmit={handleSubmit}>
          <Stack spacing={2}>
            <TextField
              fullWidth
              label="Title"
              name="title"
              value={event.title || ''}
              onChange={handleChange}
              required
            />
            <TextField
              fullWidth
              label="Time"
              name="time"
              value={event.time || ''}
              onChange={handleChange}
              required
            />
            <TextField
              fullWidth
              label="Location"
              name="locationInfo"
              value={event.locationInfo || ''}
              onChange={handleChange}
              required
            />
            <TextField
              fullWidth
              label="Description"
              name="description"
              value={event.description || ''}
              onChange={handleChange}
              multiline
              rows={4}
            />
            <TextField
              fullWidth
              label="Agenda"
              name="agenda"
              value={event.agenda || ''}
              onChange={handleChange}
              multiline
              rows={4}
            />
            <TextField
              fullWidth
              label="Date Key"
              name="dateKey"
              value={event.dateKey || ''}
              onChange={handleChange}
              helperText="Format: MMDDYY (e.g., 050225 for May 2, 2025)"
            />
            <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 2 }}>
              <Button variant="outlined" onClick={() => navigate('/')}>
                Cancel
              </Button>
              <Button type="submit" variant="contained" color="primary">
                {id ? 'Update' : 'Create'} Event
              </Button>
            </Box>
          </Stack>
        </form>
      </Paper>
    </Box>
  );
};

export default EventForm; 