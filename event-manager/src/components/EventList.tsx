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
} from '@mui/material';
import { useNavigate } from 'react-router-dom';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import { Event } from '../types/Event';
import { eventService, TabInfo } from '../services/api';

type SortConfig = {
  key: keyof Event;
  direction: 'asc' | 'desc';
};

const EventList: React.FC = () => {
  const navigate = useNavigate();
  const [events, setEvents] = useState<Event[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [sortConfig, setSortConfig] = useState<SortConfig>({ key: 'time', direction: 'asc' });
  const [availableTabs, setAvailableTabs] = useState<TabInfo[]>([]);

  useEffect(() => {
    loadEvents();
    loadAvailableTabs();
  }, []);

  const loadAvailableTabs = async () => {
    try {
      const tabs = await eventService.getAvailableTabs();
      setAvailableTabs(tabs);
    } catch (error) {
      console.error('Error loading available tabs:', error);
    }
  };

  const loadEvents = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await eventService.getAllEvents();
      setEvents(data);
    } catch (error) {
      setError('Failed to load events. Please try again.');
      console.error('Error loading events:', error);
    } finally {
      setLoading(false);
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
        setEvents(events.filter(event => event.id !== id));
      } catch (error) {
        setError('Failed to delete event. Please try again.');
        console.error('Error deleting event:', error);
      }
    }
  };

  const getEventsForTab = (tab: TabInfo) => {
    return events
      .filter(event => event.dateKey === tab.dateKey)
      .sort((a, b) => {
        const aValue = a[sortConfig.key];
        const bValue = b[sortConfig.key];
        
        // Handle undefined values
        if (aValue === undefined && bValue === undefined) return 0;
        if (aValue === undefined) return 1;
        if (bValue === undefined) return -1;
        
        if (sortConfig.direction === 'asc') {
          return aValue > bValue ? 1 : -1;
        }
        return aValue < bValue ? 1 : -1;
      });
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

  return (
    <Box sx={{ p: 2 }}>
      <Typography variant="h4" gutterBottom>
        Event Manager
      </Typography>
      {availableTabs.map((tab, index) => {
        const tabEvents = getEventsForTab(tab);
        if (tabEvents.length === 0) return null;

        return (
          <Accordion key={tab.dateKey} defaultExpanded={index === 0} sx={{ mb: 2 }}>
            <AccordionSummary expandIcon={<ExpandMoreIcon />}>
              <Typography variant="h6">{tab.displayName}</Typography>
            </AccordionSummary>
            <AccordionDetails>
              <TableContainer component={Paper}>
                <Table>
                  <TableHead>
                    <TableRow>
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
                    {tabEvents.map((event) => (
                      <TableRow key={event.id}>
                        <TableCell>{event.title}</TableCell>
                        <TableCell>{event.time}</TableCell>
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
            </AccordionDetails>
          </Accordion>
        );
      })}
    </Box>
  );
};

export default EventList; 