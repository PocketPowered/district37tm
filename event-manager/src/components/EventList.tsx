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
  const [initialLoadComplete, setInitialLoadComplete] = useState(false);

  useEffect(() => {
    loadAvailableDates();
  }, []);

  useEffect(() => {
    if (availableDates.length > 0) {
      // Set the first date as expanded by default
      const firstDate = availableDates[0].toString();
      setExpandedDate(firstDate);
      // Load events for all dates
      Promise.all(availableDates.map(loadEventsForDate))
        .then(() => {
          setInitialLoadComplete(true);
          setLoading(false);
        })
        .catch((error) => {
          console.error('Error loading events:', error);
          setError('Failed to load events. Please try again.');
          setLoading(false);
        });
    } else if (availableDates.length === 0 && !loading) {
      setInitialLoadComplete(true);
    }
  }, [availableDates]);

  const loadAvailableDates = async () => {
    try {
      const dates = await dateService.getAvailableDates();
      setAvailableDates(dates);
      setError(null);
      if (dates.length === 0) {
        setLoading(false);
      }
    } catch (err) {
      setError('Failed to load available dates');
      console.error(err);
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

  if (loading || !initialLoadComplete) {
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

  if (!hasEvents && initialLoadComplete) {
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
    <Box sx={{ 
      p: { xs: 1, sm: 2 }, 
      width: '100%',
      maxWidth: '100%',
      overflowX: 'hidden'
    }}>
      <Box sx={{ 
        display: 'flex', 
        flexDirection: { xs: 'column', sm: 'row' },
        justifyContent: 'space-between', 
        alignItems: { xs: 'stretch', sm: 'center' }, 
        mb: 2,
        gap: 2,
        width: '100%'
      }}>
        <Typography variant="h4" sx={{ 
          fontSize: { xs: '1.5rem', sm: '2rem' },
          overflow: 'hidden',
          textOverflow: 'ellipsis'
        }}>
          Event Manager
        </Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => navigate('/events/new')}
          sx={{ 
            alignSelf: { xs: 'stretch', sm: 'auto' },
            whiteSpace: 'nowrap'
          }}
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
            sx={{ 
              mb: 2,
              width: '100%'
            }}
          >
            <AccordionSummary 
              expandIcon={<ExpandMoreIcon />}
              sx={{
                width: '100%',
                '& .MuiAccordionSummary-content': {
                  overflow: 'hidden'
                }
              }}
            >
              <Typography 
                variant="h6" 
                sx={{ 
                  fontSize: { xs: '1rem', sm: '1.25rem' },
                  overflow: 'hidden',
                  textOverflow: 'ellipsis'
                }}
              >
                {new Date(timestamp).toLocaleDateString('en-US', {
                  weekday: 'long',
                  month: 'long',
                  day: 'numeric',
                  year: 'numeric'
                })}
              </Typography>
            </AccordionSummary>
            <AccordionDetails>
              {sortedEvents.length === 0 ? (
                <Typography color="text.secondary" sx={{ p: 2 }}>
                  No events scheduled for this date.
                </Typography>
              ) : (
                <Box sx={{ 
                  width: '100%',
                  overflowX: 'auto',
                  WebkitOverflowScrolling: 'touch',
                  '-webkit-overflow-scrolling': 'touch',
                  msOverflowStyle: 'none',
                  scrollbarWidth: 'none',
                  '&::-webkit-scrollbar': {
                    display: 'none'
                  }
                }}>
                  <Table size="small" sx={{ 
                    minWidth: { xs: '800px', sm: '100%' },
                    '& th, & td': { 
                      px: { xs: 1, sm: 2 },
                      py: 1,
                      whiteSpace: 'nowrap'
                    },
                    '& th': {
                      fontWeight: 'bold'
                    }
                  }}>
                    <TableHead>
                      <TableRow>
                        <TableCell width="40%">
                          <TableSortLabel
                            active={sortConfig.key === 'title'}
                            direction={sortConfig.key === 'title' ? sortConfig.direction : 'asc'}
                            onClick={() => handleSort('title')}
                          >
                            Title
                          </TableSortLabel>
                        </TableCell>
                        <TableCell width="30%">
                          <TableSortLabel
                            active={sortConfig.key === 'time'}
                            direction={sortConfig.key === 'time' ? sortConfig.direction : 'asc'}
                            onClick={() => handleSort('time')}
                          >
                            Time
                          </TableSortLabel>
                        </TableCell>
                        <TableCell width="20%">
                          <TableSortLabel
                            active={sortConfig.key === 'locationInfo'}
                            direction={sortConfig.key === 'locationInfo' ? sortConfig.direction : 'asc'}
                            onClick={() => handleSort('locationInfo')}
                          >
                            Location
                          </TableSortLabel>
                        </TableCell>
                        <TableCell width="10%" align="right">Actions</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {sortedEvents.map((event) => (
                        <TableRow 
                          key={event.id}
                          onClick={() => navigate(`/events/${event.id}/edit`)}
                          sx={{ 
                            cursor: 'pointer',
                            '&:hover': {
                              backgroundColor: 'action.hover'
                            }
                          }}
                        >
                          <TableCell>{event.title}</TableCell>
                          <TableCell>{formatTimeRange(event.time.startTime, event.time.endTime)}</TableCell>
                          <TableCell>{event.locationInfo}</TableCell>
                          <TableCell align="right">
                            <Box sx={{ 
                              display: 'flex', 
                              justifyContent: 'flex-end', 
                              gap: 1,
                              minWidth: 'max-content'
                            }}>
                              <IconButton 
                                size="small"
                                onClick={(e) => {
                                  e.stopPropagation();
                                  navigate(`/events/${event.id}/edit`);
                                }}
                              >
                                <EditIcon fontSize="small" />
                              </IconButton>
                              <IconButton 
                                size="small"
                                onClick={(e) => {
                                  e.stopPropagation();
                                  event.id && handleDelete(event.id);
                                }}
                              >
                                <DeleteIcon fontSize="small" />
                              </IconButton>
                            </Box>
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </Box>
              )}
            </AccordionDetails>
          </Accordion>
        );
      })}
    </Box>
  );
};

export default EventList; 