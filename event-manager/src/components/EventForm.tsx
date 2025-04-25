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
  IconButton,
  Divider,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
} from '@mui/material';
import { useNavigate, useParams } from 'react-router-dom';
import { Event, AgendaItem, ExternalLink, TimeRange, EventTag } from '../types/Event';
import { Location } from '../types/Location';
import { eventService } from '../services/api';
import { dateService } from '../services/dateService';
import { locationService } from '../services/locationService';
import AddIcon from '@mui/icons-material/Add';
import DeleteIcon from '@mui/icons-material/Delete';
import LinkIcon from '@mui/icons-material/Link';
import ImageIcon from '@mui/icons-material/Image';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import ArrowForwardIcon from '@mui/icons-material/ArrowForward';
import ArrowUpwardIcon from '@mui/icons-material/ArrowUpward';
import ArrowDownwardIcon from '@mui/icons-material/ArrowDownward';
import { DateTimePicker } from '@mui/x-date-pickers/DateTimePicker';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';

const EventForm: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [availableDates, setAvailableDates] = useState<number[]>([]);
  const [availableLocations, setAvailableLocations] = useState<Location[]>([]);
  const [event, setEvent] = useState<Event>({
    id: 0,
    title: '',
    time: {
      startTime: availableDates[0] || Date.now(),
      endTime: (availableDates[0] || Date.now()) + 3600000 // 1 hour later
    },
    locationInfo: '',
    description: '',
    agenda: [],
    additionalLinks: [],
    dateKey: availableDates[0]?.toString() || '',
    images: [],
    tag: EventTag.NORMAL
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [imageUrl, setImageUrl] = useState('');

  useEffect(() => {
    if (id) {
      loadEvent();
    }
    loadAvailableDates();
    loadAvailableLocations();
  }, [id]);

  useEffect(() => {
    if (event.dateKey) {
      const selectedDate = parseInt(event.dateKey);
      setEvent(prev => ({
        ...prev,
        time: {
          startTime: selectedDate,
          endTime: selectedDate + 3600000 // 1 hour later
        }
      }));
    }
  }, [event.dateKey]);

  const loadAvailableDates = async () => {
    try {
      const dates = await dateService.getAvailableDates();
      setAvailableDates(dates);
      if (dates.length > 0 && !id) {
        setEvent(prev => ({
          ...prev,
          time: {
            startTime: dates[0],
            endTime: dates[0] + 3600000 // 1 hour later
          },
          dateKey: dates[0].toString()
        }));
      }
    } catch (error) {
      console.error('Error loading available dates:', error);
    }
  };

  const loadAvailableLocations = async () => {
    try {
      const locations = await locationService.getAllLocations();
      setAvailableLocations(locations);
    } catch (error) {
      console.error('Error loading available locations:', error);
    }
  };

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

  const handleTimeChange = (field: 'startTime' | 'endTime', value: Date | null) => {
    if (!value) return;
    setEvent(prev => ({
      ...prev,
      time: {
        ...prev.time,
        [field]: value.getTime()
      }
    }));
  };

  const handleAgendaItemChange = (index: number, field: keyof AgendaItem, value: string) => {
    setEvent(prev => {
      const newAgenda = [...prev.agenda];
      newAgenda[index] = { ...newAgenda[index], [field]: value };
      return { ...prev, agenda: newAgenda };
    });
  };

  const handleAgendaTimeChange = (index: number, field: 'startTime' | 'endTime', value: Date | null) => {
    if (!value) return;
    setEvent(prev => {
      const newAgenda = [...prev.agenda];
      newAgenda[index] = {
        ...newAgenda[index],
        time: {
          ...newAgenda[index].time,
          [field]: value.getTime()
        }
      };
      return { ...prev, agenda: newAgenda };
    });
  };

  const addAgendaItem = () => {
    const selectedDate = new Date(parseInt(event.dateKey));
    
    const startTime = new Date(selectedDate);
    startTime.setHours(12, 0, 0, 0);
    
    const endTime = new Date(selectedDate);
    endTime.setHours(13, 0, 0, 0);

    setEvent(prev => ({
      ...prev,
      agenda: [...prev.agenda, {
        title: '',
        description: '',
        time: { 
          startTime: startTime.getTime(),
          endTime: endTime.getTime()
        },
        locationInfo: ''
      }]
    }));
  };

  const removeAgendaItem = (index: number) => {
    setEvent(prev => ({
      ...prev,
      agenda: prev.agenda.filter((_, i) => i !== index)
    }));
  };

  const moveAgendaItem = (fromIndex: number, toIndex: number) => {
    setEvent(prev => {
      const newAgenda = [...prev.agenda];
      const [movedItem] = newAgenda.splice(fromIndex, 1);
      newAgenda.splice(toIndex, 0, movedItem);
      return { ...prev, agenda: newAgenda };
    });
  };

  const handleLinkChange = (index: number, field: keyof ExternalLink, value: string) => {
    setEvent(prev => {
      const newLinks = [...prev.additionalLinks];
      newLinks[index] = { ...newLinks[index], [field]: value };
      return { ...prev, additionalLinks: newLinks };
    });
  };

  const addLink = () => {
    setEvent(prev => ({
      ...prev,
      additionalLinks: [...prev.additionalLinks, { displayName: '', url: '' }]
    }));
  };

  const removeLink = (index: number) => {
    setEvent(prev => ({
      ...prev,
      additionalLinks: prev.additionalLinks.filter((_, i) => i !== index)
    }));
  };

  const moveLinkItem = (fromIndex: number, toIndex: number) => {
    setEvent(prev => {
      const newLinks = [...prev.additionalLinks];
      const [movedItem] = newLinks.splice(fromIndex, 1);
      newLinks.splice(toIndex, 0, movedItem);
      return { ...prev, additionalLinks: newLinks };
    });
  };

  const handleAddImage = () => {
    if (imageUrl.trim()) {
      setEvent(prev => ({
        ...prev,
        images: [...prev.images, imageUrl.trim()]
      }));
      setImageUrl('');
    }
  };

  const handleRemoveImage = (index: number) => {
    setEvent(prev => ({
      ...prev,
      images: prev.images.filter((_, i) => i !== index)
    }));
  };

  const handleKeyDown = (index: number, e: React.KeyboardEvent) => {
    if (!event.images) return;
    
    if (e.key === 'ArrowLeft' && index > 0) {
      e.preventDefault();
      const newImages = [...event.images];
      [newImages[index], newImages[index - 1]] = [newImages[index - 1], newImages[index]];
      setEvent(prev => ({ ...prev, images: newImages }));
    } else if (e.key === 'ArrowRight' && index < event.images.length - 1) {
      e.preventDefault();
      const newImages = [...event.images];
      [newImages[index], newImages[index + 1]] = [newImages[index + 1], newImages[index]];
      setEvent(prev => ({ ...prev, images: newImages }));
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setSuccess(null);

    try {
      const eventToSave: Event = {
        ...event,
        title: event.title || '',
        time: event.time || { startTime: Date.now(), endTime: Date.now() + 3600000 },
        locationInfo: event.locationInfo || '',
        description: event.description || '',
        agenda: event.agenda || [],
        additionalLinks: event.additionalLinks || [],
        dateKey: event.dateKey || '',
        images: event.images || [],
        tag: event.tag || EventTag.NORMAL
      };

      // Ensure locationInfo is set from the selected location
      if (event.locationInfo) {
        const selectedLocation = availableLocations.find(loc => loc.locationName === event.locationInfo);
        if (selectedLocation) {
          eventToSave.locationInfo = selectedLocation.locationName;
        }
      }

      if (id) {
        await eventService.updateEvent(parseInt(id), { ...eventToSave, id: parseInt(id) });
        setSuccess('Event updated successfully!');
      } else {
        await eventService.createEvent(eventToSave);
        setSuccess('Event created successfully!');
      }
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
    <LocalizationProvider dateAdapter={AdapterDateFns}>
      <Paper sx={{ p: 3 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
          <Typography variant="h5">
            {id ? 'Edit Event' : 'Create Event'}
          </Typography>
          <Button
            startIcon={<ArrowBackIcon />}
            onClick={() => navigate('/')}
            variant="outlined"
          >
            Back to List
          </Button>
        </Box>

        {success && (
          <Alert 
            severity="success" 
            sx={{ mb: 3 }}
            action={
              <Button 
                color="inherit" 
                size="small"
                onClick={() => navigate('/')}
              >
                Back to List
              </Button>
            }
          >
            {success}
          </Alert>
        )}
        {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
        
        <form onSubmit={handleSubmit}>
          <Stack spacing={2}>
            <FormControl fullWidth required>
              <InputLabel>Date</InputLabel>
              <Select
                value={event.dateKey}
                label="Date"
                onChange={(e) => setEvent({ ...event, dateKey: e.target.value })}
                sx={{ textAlign: 'left' }}
              >
                {availableDates.map((timestamp) => (
                  <MenuItem key={timestamp} value={timestamp.toString()} sx={{ textAlign: 'left' }}>
                    {new Date(timestamp).toLocaleDateString('en-US', {
                      weekday: 'long',
                      month: 'long',
                      day: 'numeric',
                      year: 'numeric'
                    })}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
            <TextField
              label="Title"
              name="title"
              value={event.title}
              onChange={handleChange}
              required
              fullWidth
            />
            <Box sx={{ display: 'flex', gap: 2 }}>
              <DateTimePicker
                label="Start Time"
                value={new Date(event.time.startTime)}
                onChange={(value) => handleTimeChange('startTime', value)}
                sx={{ flex: 1 }}
              />
              <DateTimePicker
                label="End Time"
                value={new Date(event.time.endTime)}
                onChange={(value) => handleTimeChange('endTime', value)}
                sx={{ flex: 1 }}
              />
            </Box>
            <FormControl fullWidth required>
              <InputLabel>Location</InputLabel>
              <Select
                value={event.locationInfo}
                label="Location"
                onChange={(e) => setEvent({ ...event, locationInfo: e.target.value })}
                sx={{ textAlign: 'left' }}
              >
                {availableLocations.map((location) => (
                  <MenuItem key={location.id} value={location.locationName} sx={{ textAlign: 'left' }}>
                    {location.locationName}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
            <TextField
              label="Description"
              name="description"
              value={event.description}
              onChange={handleChange}
              multiline
              rows={4}
              fullWidth
            />
            <FormControl fullWidth>
              <InputLabel>Event Tag</InputLabel>
              <Select
                value={event.tag}
                label="Event Tag"
                onChange={(e) => setEvent({ ...event, tag: e.target.value as EventTag })}
              >
                <MenuItem value={EventTag.NORMAL}>Normal</MenuItem>
                <MenuItem value={EventTag.HIGHLIGHTED}>Highlighted</MenuItem>
                <MenuItem value={EventTag.BREAK}>Break</MenuItem>
              </Select>
            </FormControl>

            <Box>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                <Typography variant="h6">Agenda Items</Typography>
                <Button
                  startIcon={<AddIcon />}
                  onClick={addAgendaItem}
                  variant="outlined"
                >
                  Add Agenda Item
                </Button>
              </Box>
              {event.agenda.map((item, index) => (
                <Paper key={index} sx={{ p: 2, mb: 2 }}>
                  <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 1 }}>
                    <IconButton 
                      onClick={() => moveAgendaItem(index, index - 1)}
                      disabled={index === 0}
                      size="small"
                    >
                      <ArrowUpwardIcon />
                    </IconButton>
                    <IconButton 
                      onClick={() => moveAgendaItem(index, index + 1)}
                      disabled={index === event.agenda.length - 1}
                      size="small"
                    >
                      <ArrowDownwardIcon />
                    </IconButton>
                    <IconButton onClick={() => removeAgendaItem(index)}>
                      <DeleteIcon />
                    </IconButton>
                  </Box>
                  <Stack spacing={2}>
                    <TextField
                      label="Title"
                      value={item.title}
                      onChange={(e) => handleAgendaItemChange(index, 'title', e.target.value)}
                      fullWidth
                    />
                    <Box sx={{ display: 'flex', gap: 2 }}>
                      <DateTimePicker
                        label="Start Time"
                        value={new Date(item.time.startTime)}
                        onChange={(value) => handleAgendaTimeChange(index, 'startTime', value)}
                        sx={{ flex: 1 }}
                      />
                      <DateTimePicker
                        label="End Time"
                        value={new Date(item.time.endTime)}
                        onChange={(value) => handleAgendaTimeChange(index, 'endTime', value)}
                        sx={{ flex: 1 }}
                      />
                    </Box>
                    <TextField
                      label="Location"
                      value={item.locationInfo}
                      onChange={(e) => handleAgendaItemChange(index, 'locationInfo', e.target.value)}
                      fullWidth
                    />
                    <TextField
                      label="Description"
                      value={item.description}
                      onChange={(e) => handleAgendaItemChange(index, 'description', e.target.value)}
                      multiline
                      rows={2}
                      fullWidth
                    />
                  </Stack>
                </Paper>
              ))}
            </Box>

            <Box>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                <Typography variant="h6">Additional Links</Typography>
                <Button
                  startIcon={<LinkIcon />}
                  onClick={addLink}
                  variant="outlined"
                >
                  Add Link
                </Button>
              </Box>
              {event.additionalLinks.map((link, index) => (
                <Paper key={index} sx={{ p: 2, mb: 2 }}>
                  <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 1 }}>
                    <IconButton 
                      onClick={() => moveLinkItem(index, index - 1)}
                      disabled={index === 0}
                      size="small"
                    >
                      <ArrowUpwardIcon />
                    </IconButton>
                    <IconButton 
                      onClick={() => moveLinkItem(index, index + 1)}
                      disabled={index === event.additionalLinks.length - 1}
                      size="small"
                    >
                      <ArrowDownwardIcon />
                    </IconButton>
                    <IconButton onClick={() => removeLink(index)}>
                      <DeleteIcon />
                    </IconButton>
                  </Box>
                  <Stack spacing={2}>
                    <TextField
                      label="Display Name"
                      value={link.displayName}
                      onChange={(e) => handleLinkChange(index, 'displayName', e.target.value)}
                      fullWidth
                    />
                    <TextField
                      label="URL"
                      value={link.url}
                      onChange={(e) => handleLinkChange(index, 'url', e.target.value)}
                      fullWidth
                    />
                  </Stack>
                </Paper>
              ))}
            </Box>

            <Box>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                <Typography variant="h6">Images</Typography>
                <Box sx={{ display: 'flex', gap: 1 }}>
                  <TextField
                    size="small"
                    placeholder="Image URL"
                    value={imageUrl}
                    onChange={(e) => setImageUrl(e.target.value)}
                    sx={{ width: 300 }}
                  />
                  <Button
                    startIcon={<AddIcon />}
                    onClick={handleAddImage}
                    variant="outlined"
                    disabled={!imageUrl.trim()}
                  >
                    Add Image
                  </Button>
                </Box>
              </Box>
              
              <Box
                sx={{
                  display: 'grid',
                  gridTemplateColumns: 'repeat(auto-fill, minmax(250px, 1fr))',
                  gap: 2,
                  width: '100%',
                  minHeight: '200px'
                }}
              >
                {event.images.map((image, index) => (
                  <Paper
                    key={index}
                    tabIndex={0}
                    onKeyDown={(e) => handleKeyDown(index, e)}
                    sx={{
                      p: 1,
                      display: 'flex',
                      flexDirection: 'column',
                      gap: 1,
                      position: 'relative',
                      transition: 'transform 0.2s ease, box-shadow 0.2s ease',
                      '&:focus': {
                        outline: 'none',
                        transform: 'scale(1.02)',
                        boxShadow: (theme) => theme.shadows[4]
                      }
                    }}
                  >
                    <Box sx={{ 
                      display: 'flex', 
                      gap: 0.5,
                      position: 'absolute',
                      top: 8,
                      left: 8,
                      zIndex: 1
                    }}>
                      <IconButton
                        size="small"
                        disabled={index === 0}
                        onClick={() => handleKeyDown(index, { key: 'ArrowLeft', preventDefault: () => {} } as React.KeyboardEvent)}
                        sx={{ 
                          backgroundColor: 'rgba(255, 255, 255, 0.9)',
                          border: '1px solid rgba(0, 0, 0, 0.2)',
                          '&:hover': {
                            backgroundColor: 'rgba(255, 255, 255, 1)',
                            border: '1px solid rgba(0, 0, 0, 0.4)'
                          },
                          '&.Mui-disabled': {
                            backgroundColor: 'rgba(255, 255, 255, 0.5)',
                            border: '1px solid rgba(0, 0, 0, 0.1)'
                          }
                        }}
                      >
                        <ArrowBackIcon fontSize="small" />
                      </IconButton>
                      <IconButton
                        size="small"
                        disabled={index === event.images.length - 1}
                        onClick={() => handleKeyDown(index, { key: 'ArrowRight', preventDefault: () => {} } as React.KeyboardEvent)}
                        sx={{ 
                          backgroundColor: 'rgba(255, 255, 255, 0.9)',
                          border: '1px solid rgba(0, 0, 0, 0.2)',
                          '&:hover': {
                            backgroundColor: 'rgba(255, 255, 255, 1)',
                            border: '1px solid rgba(0, 0, 0, 0.4)'
                          },
                          '&.Mui-disabled': {
                            backgroundColor: 'rgba(255, 255, 255, 0.5)',
                            border: '1px solid rgba(0, 0, 0, 0.1)'
                          }
                        }}
                      >
                        <ArrowForwardIcon fontSize="small" />
                      </IconButton>
                    </Box>
                    <Box
                      component="img"
                      src={image}
                      alt={`Event image ${index + 1}`}
                      sx={{
                        width: '100%',
                        height: 150,
                        objectFit: 'cover',
                        borderRadius: 1
                      }}
                      onError={(e) => {
                        const target = e.target as HTMLImageElement;
                        target.src = 'https://via.placeholder.com/150?text=Image+Not+Found';
                      }}
                    />
                    <Typography
                      variant="caption"
                      sx={{
                        wordBreak: 'break-all',
                        fontSize: '0.7rem',
                        color: 'text.secondary',
                        width: '100%',
                        whiteSpace: 'pre-wrap',
                        overflow: 'visible'
                      }}
                    >
                      {image}
                    </Typography>
                    <IconButton
                      size="small"
                      onClick={() => handleRemoveImage(index)}
                      sx={{
                        position: 'absolute',
                        top: 8,
                        right: 8,
                        backgroundColor: 'rgba(255, 255, 255, 0.9)',
                        border: '1px solid rgba(0, 0, 0, 0.2)',
                        '&:hover': {
                          backgroundColor: 'rgba(255, 255, 255, 1)',
                          border: '1px solid rgba(0, 0, 0, 0.4)'
                        }
                      }}
                    >
                      <DeleteIcon fontSize="small" />
                    </IconButton>
                  </Paper>
                ))}
              </Box>
            </Box>

            <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 2, mt: 2 }}>
              <Button type="submit" variant="contained" color="primary" disabled={loading}>
                {loading ? <CircularProgress size={24} /> : 'Save Changes'}
              </Button>
            </Box>
          </Stack>
        </form>
      </Paper>
    </LocalizationProvider>
  );
};

export default EventForm; 