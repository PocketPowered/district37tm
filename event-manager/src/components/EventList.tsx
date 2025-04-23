import React, { useEffect, useState } from 'react';
import {
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Typography,
  Box,
  CircularProgress,
  Alert,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  IconButton,
  TableSortLabel,
  Button,
  Stack,
} from '@mui/material';
import { useNavigate } from 'react-router-dom';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import AddIcon from '@mui/icons-material/Add';
import { Event, formatTimeRange } from '../types/Event';
import { eventService } from '../services/api';
import { dateService } from '../services/dateService';

type SortConfig = {
  key: keyof Event;
  direction: 'asc' | 'desc';
};

type DateEvents = {
  [dateKey: string]: Event[];
};

const EventList: React.FC = () => {
  const navigate = useNavigate();
  const [dateEvents, setDateEvents] = useState<DateEvents>({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [sortConfig, setSortConfig] = useState<SortConfig>({ key: 'time', direction: 'asc' });
  const [availableDates, setAvailableDates] = useState<number[]>([]);
  const [expandedDate, setExpandedDate] = useState<string | null>(null);

  useEffect(() => {
    loadAvailableDates();
  }, []);

  useEffect(() => {
    if (availableDates.length > 0) {
      // Set the first date as expanded by default
      const firstDate = availableDates[0].toString();
      setExpandedDate(firstDate);
      // Load events for the first date immediately
      loadEventsForDate(parseInt(firstDate));
    }
  }, [availableDates]);

  const loadAvailableDates = async () => {
    try {
      const dates = await dateService.getAvailableDates();
      setAvailableDates(dates);
      setError(null);
    } catch (err) {
      setError('Failed to load available dates');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const loadEventsForDate = async (timestamp: number) => {
    try {
      const events = await eventService.getEventsByDate(timestamp);
      setDateEvents(prev => ({
        ...prev,
        [timestamp.toString()]: events
      }));
    } catch (error) {
      console.error(`Error loading events for date ${timestamp}:`, error);
      setError(`Failed to load events for ${new Date(timestamp).toLocaleDateString()}. Please try again.`);
    }
  };

  const handleSort = (key: keyof Event) => {
    setSortConfig(current => ({
      key,
      direction: current.key === key && current.direction === 'asc' ? 'desc' : 'asc'
    }));
  };

  const handleDelete = async (id: number) => {
    if (window.confirm('Are you sure you want to delete this event?')) {
      try {
        await eventService.deleteEvent(id);
        // Reload events for all dates after deletion
        availableDates.forEach(loadEventsForDate);
      } catch (error) {
        setError('Failed to delete event. Please try again.');
        console.error('Error deleting event:', error);
      }
    }
  };

  const getSortedEvents = (events: Event[]) => {
    return [...events].sort((a, b) => {
      const aValue = a[sortConfig.key];
      const bValue = b[sortConfig.key];
      
      // Handle undefined values
      if (aValue === undefined && bValue === undefined) return 0;
      if (aValue === undefined) return 1;
      if (bValue === undefined) return -1;
      
      if (sortConfig.key === 'time') {
        // Special handling for time sorting
        const aTime = a.time.startTime;
        const bTime = b.time.startTime;
        return sortConfig.direction === 'asc' ? aTime - bTime : bTime - aTime;
      }
      
      if (sortConfig.direction === 'asc') {
        return aValue > bValue ? 1 : -1;
      }
      return aValue < bValue ? 1 : -1;
    });
  };

  const handleAccordionChange = (timestamp: string) => (event: React.SyntheticEvent, isExpanded: boolean) => {
    setExpandedDate(isExpanded ? timestamp : null);
    if (isExpanded) {
      loadEventsForDate(parseInt(timestamp));
    }
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', p: 3 }}>
        <CircularProgress />
      </Box>
    );
  }

  if (error) {
    return (
      <Alert severity="error" sx={{ m: 2 }}>
        {error}
      </Alert>
    );
  }

  // Check if there are any events across all dates
  const hasEvents = availableDates.some(timestamp => {
    const events = dateEvents[timestamp.toString()] || [];
    return events.length > 0;
  });

  if (!hasEvents) {
    return (
      <Box sx={{ p: 2, textAlign: 'center' }}>
        <Typography variant="h5" gutterBottom>
          No Events Found
        </Typography>
        <Typography variant="body1" color="text.secondary" paragraph>
          There are no events scheduled yet. Click the button below to create your first event.
        </Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => navigate('/events/new')}
          sx={{ mt: 2 }}
        >
          Create New Event
        </Button>
      </Box>
    );
  }

  return (
    <Box sx={{ p: 2 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <Typography variant="h4">
          Event Manager
        </Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => navigate('/events/new')}
        >
          Create New Event
        </Button>
      </Box>
      {availableDates.map((timestamp, index) => {
        const events = dateEvents[timestamp.toString()] || [];
        const sortedEvents = getSortedEvents(events);

        return (
          <Accordion 
            key={timestamp} 
            expanded={expandedDate === timestamp.toString()}
            onChange={handleAccordionChange(timestamp.toString())}
            sx={{ mb: 2 }}
          >
            <AccordionSummary expandIcon={<ExpandMoreIcon />}>
              <Typography variant="h6">
                {new Date(timestamp).toLocaleDateString('en-US', {
                  month: 'long',
                  day: 'numeric',
                  year: 'numeric'
                })}
              </Typography>
            </AccordionSummary>
            <AccordionDetails>
              {sortedEvents.length === 0 ? (
                <Typography color="text.secondary">
                  No events scheduled for this date.
                </Typography>
              ) : (
                <TableContainer component={Paper}>
                  <Table>
                    <TableHead>
                      <TableRow>
                        <TableCell>
                          <TableSortLabel
                            active={sortConfig.key === 'id'}
                            direction={sortConfig.key === 'id' ? sortConfig.direction : 'asc'}
                            onClick={() => handleSort('id')}
                          >
                            ID
                          </TableSortLabel>
                        </TableCell>
                        <TableCell>
                          <TableSortLabel
                            active={sortConfig.key === 'title'}
                            direction={sortConfig.key === 'title' ? sortConfig.direction : 'asc'}
                            onClick={() => handleSort('title')}
                          >
                            Title
                          </TableSortLabel>
                        </TableCell>
                        <TableCell>
                          <TableSortLabel
                            active={sortConfig.key === 'time'}
                            direction={sortConfig.key === 'time' ? sortConfig.direction : 'asc'}
                            onClick={() => handleSort('time')}
                          >
                            Time
                          </TableSortLabel>
                        </TableCell>
                        <TableCell>
                          <TableSortLabel
                            active={sortConfig.key === 'locationInfo'}
                            direction={sortConfig.key === 'locationInfo' ? sortConfig.direction : 'asc'}
                            onClick={() => handleSort('locationInfo')}
                          >
                            Location
                          </TableSortLabel>
                        </TableCell>
                        <TableCell>Actions</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {sortedEvents.map((event) => (
                        <TableRow key={event.id}>
                          <TableCell>{event.id}</TableCell>
                          <TableCell>{event.title}</TableCell>
                          <TableCell>{formatTimeRange(event.time.startTime, event.time.endTime)}</TableCell>
                          <TableCell>{event.locationInfo}</TableCell>
                          <TableCell>
                            <IconButton onClick={() => navigate(`/events/${event.id}/edit`)}>
                              <EditIcon />
                            </IconButton>
                            <IconButton onClick={() => event.id && handleDelete(event.id)}>
                              <DeleteIcon />
                            </IconButton>
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              )}
            </AccordionDetails>
          </Accordion>
        );
      })}
    </Box>
  );
};

export default EventList; 