import React, { useCallback, useEffect, useState } from 'react';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
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
  TextField,
  InputAdornment,
  Chip,
  Card,
  CardContent,
  Snackbar,
  useMediaQuery,
  useTheme,
} from '@mui/material';
import { useNavigate } from 'react-router-dom';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import AddIcon from '@mui/icons-material/Add';
import SearchIcon from '@mui/icons-material/Search';
import RefreshIcon from '@mui/icons-material/Refresh';
import { Event, formatTimeRange } from '../types/Event';
import { eventService } from '../services/eventService';
import { dateService } from '../services/dateService';
import { formatDateKey } from '../utils/dateKey';
import { useConference } from '../contexts/ConferenceContext';

type SortConfig = {
  key: keyof Event;
  direction: 'asc' | 'desc';
};

type DateEvents = {
  [dateKey: string]: Event[];
};

const formatDate = (timestamp: number): string => {
  return formatDateKey(timestamp, {
    weekday: 'long',
    month: 'long',
    day: 'numeric',
    year: 'numeric',
  });
};

const EventList: React.FC = () => {
  const navigate = useNavigate();
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));
  const { selectedConference, loading: conferenceLoading } = useConference();

  const [dateEvents, setDateEvents] = useState<DateEvents>({});
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [sortConfig, setSortConfig] = useState<SortConfig>({ key: 'time', direction: 'asc' });
  const [availableDates, setAvailableDates] = useState<number[]>([]);
  const [expandedDate, setExpandedDate] = useState<string | null>(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [deleteSuccess, setDeleteSuccess] = useState<string | null>(null);

  const loadAllData = useCallback(async (manualRefresh = false) => {
    if (!selectedConference) {
      setDateEvents({});
      setAvailableDates([]);
      setExpandedDate(null);
      setError(null);
      setLoading(false);
      setRefreshing(false);
      return;
    }

    if (manualRefresh) {
      setRefreshing(true);
    } else {
      setLoading(true);
    }

    try {
      const dates = await dateService.getAvailableDates(selectedConference);
      setAvailableDates(dates);
      setError(null);

      if (!dates.length) {
        setDateEvents({});
        setExpandedDate(null);
        return;
      }

      setExpandedDate((prev) => prev ?? dates[0].toString());

      const nextDateEvents: DateEvents = {};
      dates.forEach((timestamp) => {
        nextDateEvents[timestamp.toString()] = [];
      });

      const conferenceEvents = await eventService.getAllEvents(selectedConference.id);
      conferenceEvents.forEach((event) => {
        const timestamp = Number.parseInt(event.dateKey, 10);
        if (!Number.isFinite(timestamp) || !nextDateEvents[timestamp.toString()]) {
          return;
        }

        nextDateEvents[timestamp.toString()].push(event);
      });

      setDateEvents(nextDateEvents);
    } catch (loadError) {
      console.error('Error loading events:', loadError);
      setError('Failed to load events. Please try again.');
    } finally {
      if (manualRefresh) {
        setRefreshing(false);
      } else {
        setLoading(false);
      }
    }
  }, [selectedConference]);

  useEffect(() => {
    if (conferenceLoading) {
      return;
    }
    void loadAllData();
  }, [conferenceLoading, loadAllData]);

  const handleSort = (key: keyof Event) => {
    setSortConfig((current) => ({
      key,
      direction: current.key === key && current.direction === 'asc' ? 'desc' : 'asc',
    }));
  };

  const handleDelete = async (id: number, title: string) => {
    if (!window.confirm('Are you sure you want to delete this event?')) {
      return;
    }

    if (!selectedConference) {
      return;
    }

    try {
      await eventService.deleteEvent(id, selectedConference.id);
      setDateEvents((prev) => {
        const next = { ...prev };
        Object.keys(next).forEach((dateKey) => {
          next[dateKey] = next[dateKey].filter((event) => event.id !== id);
        });
        return next;
      });
      setDeleteSuccess(`Deleted "${title}".`);
    } catch (deleteError) {
      setError('Failed to delete event. Please try again.');
      console.error('Error deleting event:', deleteError);
    }
  };

  const getSortedEvents = (events: Event[]) => {
    return [...events].sort((a, b) => {
      const aValue = a[sortConfig.key];
      const bValue = b[sortConfig.key];

      if (aValue === undefined && bValue === undefined) {
        return 0;
      }
      if (aValue === undefined) {
        return 1;
      }
      if (bValue === undefined) {
        return -1;
      }

      if (sortConfig.key === 'time') {
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

  const getFilteredEvents = (events: Event[]) => {
    const normalizedQuery = searchQuery.trim().toLowerCase();
    if (!normalizedQuery) {
      return events;
    }

    return events.filter((event) => {
      const haystack = [event.title, event.locationInfo, event.description]
        .filter(Boolean)
        .join(' ')
        .toLowerCase();

      return haystack.includes(normalizedQuery);
    });
  };

  const totalEventsCount = availableDates.reduce((total, timestamp) => {
    return total + (dateEvents[timestamp.toString()]?.length ?? 0);
  }, 0);

  const filteredEventsCount = availableDates.reduce((total, timestamp) => {
    const events = dateEvents[timestamp.toString()] ?? [];
    return total + getFilteredEvents(events).length;
  }, 0);

  const handleAccordionChange =
    (timestamp: string) => (_event: React.SyntheticEvent, isExpanded: boolean) => {
      setExpandedDate(isExpanded ? timestamp : null);
    };

  if (conferenceLoading || loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', p: 3 }}>
        <CircularProgress />
      </Box>
    );
  }

  if (!selectedConference) {
    return (
      <Stack spacing={2} sx={{ m: 2 }}>
        <Alert severity="warning">
          No conference selected. Choose a conference first.
        </Alert>
        <Box>
          <Button variant="contained" onClick={() => navigate('/conferences')}>
            Go to Conferences
          </Button>
        </Box>
      </Stack>
    );
  }

  if (error) {
    return (
      <Stack spacing={2} sx={{ m: 2 }}>
        <Alert severity="error">{error}</Alert>
        <Box>
          <Button variant="outlined" startIcon={<RefreshIcon />} onClick={() => void loadAllData(true)}>
            Retry
          </Button>
        </Box>
      </Stack>
    );
  }

  if (!availableDates.length) {
    return (
      <Stack spacing={2} sx={{ m: 2 }}>
        <Alert severity="warning">
          The selected conference has no date range. Set conference start and end dates to schedule events.
        </Alert>
        <Box>
          <Button variant="contained" onClick={() => navigate('/conferences')}>
            Go to Conferences
          </Button>
        </Box>
      </Stack>
    );
  }

  if (!totalEventsCount) {
    return (
      <Box sx={{ p: 2, textAlign: 'center' }}>
        <Typography variant="h5" gutterBottom>
          No Events Found
        </Typography>
        <Typography variant="body1" color="text.secondary" paragraph>
          There are no events scheduled yet for {selectedConference.name}. Click below to create the first event.
        </Typography>
        <Button variant="contained" startIcon={<AddIcon />} onClick={() => navigate('/events/new')} sx={{ mt: 2 }}>
          Create New Event
        </Button>
      </Box>
    );
  }

  return (
    <Box
      sx={{
        p: { xs: 1, sm: 2 },
        width: '100%',
        maxWidth: '100%',
        textAlign: 'left',
      }}
    >
      <Stack spacing={2.5}>
        <Stack
          direction={{ xs: 'column', sm: 'row' }}
          justifyContent="flex-end"
          alignItems={{ xs: 'stretch', sm: 'center' }}
          spacing={1.5}
        >
          <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1}>
            <Button
              variant="outlined"
              startIcon={refreshing ? <CircularProgress size={16} color="inherit" /> : <RefreshIcon />}
              onClick={() => void loadAllData(true)}
              disabled={refreshing}
            >
              Refresh
            </Button>
            <Button variant="contained" startIcon={<AddIcon />} onClick={() => navigate('/events/new')}>
              Create New Event
            </Button>
          </Stack>
        </Stack>

        <Stack
          direction={{ xs: 'column', md: 'row' }}
          spacing={1.5}
          alignItems={{ xs: 'stretch', md: 'center' }}
          sx={{ mb: 1 }}
        >
          <TextField
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Search by title, location, or description"
            size="small"
            fullWidth
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <SearchIcon fontSize="small" />
                </InputAdornment>
              ),
            }}
          />

          <Stack direction="row" spacing={1}>
            <Chip
              label={`${totalEventsCount} total`}
              variant="outlined"
              sx={{
                color: 'primary.dark',
                borderColor: 'primary.main',
                backgroundColor: (muiTheme) => muiTheme.palette.primary.main + '14',
                fontWeight: 600,
              }}
            />
            <Chip
              color={searchQuery.trim() ? 'secondary' : 'default'}
              variant="outlined"
              label={`${filteredEventsCount} shown`}
              sx={{
                color: searchQuery.trim() ? 'secondary.dark' : 'text.primary',
                borderColor: searchQuery.trim() ? 'secondary.main' : 'divider',
                backgroundColor: (muiTheme) =>
                  searchQuery.trim()
                    ? muiTheme.palette.secondary.main + '14'
                    : muiTheme.palette.action.hover,
                fontWeight: 600,
              }}
            />
          </Stack>
        </Stack>

        {filteredEventsCount === 0 && (
          <Alert
            severity="info"
            action={
              <Button color="inherit" size="small" onClick={() => setSearchQuery('')}>
                Clear
              </Button>
            }
          >
            No events match your search.
          </Alert>
        )}

        <Box sx={{ mt: 1.5 }}>
          {availableDates.map((timestamp) => {
            const events = dateEvents[timestamp.toString()] || [];
            const filteredEvents = getFilteredEvents(events);
            const sortedEvents = getSortedEvents(filteredEvents);

            return (
              <Accordion
                key={timestamp}
                expanded={expandedDate === timestamp.toString()}
                onChange={handleAccordionChange(timestamp.toString())}
              >
              <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                <Stack
                  direction={{ xs: 'column', sm: 'row' }}
                  spacing={1}
                  alignItems={{ xs: 'flex-start', sm: 'center' }}
                  sx={{ width: '100%', pr: 2 }}
                >
                  <Typography variant="h6" sx={{ fontSize: { xs: '1rem', sm: '1.2rem' } }}>
                    {formatDate(timestamp)}
                  </Typography>
                  <Chip
                    size="small"
                    label={`${sortedEvents.length} shown${searchQuery.trim() ? ` / ${events.length} total` : ''}`}
                    variant="outlined"
                    sx={{
                      color: 'common.white',
                      borderColor: 'rgba(255,255,255,0.65)',
                      backgroundColor: 'rgba(255,255,255,0.1)',
                      '& .MuiChip-label': {
                        fontWeight: 600,
                      },
                    }}
                  />
                </Stack>
              </AccordionSummary>

              <AccordionDetails>
                {!sortedEvents.length ? (
                  <Typography color="text.secondary" sx={{ p: 1 }}>
                    {searchQuery.trim()
                      ? 'No events for this date match your search.'
                      : 'No events scheduled for this date.'}
                  </Typography>
                ) : isMobile ? (
                  <Stack spacing={1.5}>
                    {sortedEvents.map((event) => (
                      <Card
                        key={event.id}
                        variant="outlined"
                        sx={{ cursor: 'pointer' }}
                        onClick={() => navigate(`/events/${event.id}/edit`)}
                      >
                        <CardContent>
                          <Typography variant="subtitle1" sx={{ fontWeight: 600, mb: 0.5 }}>
                            {event.title}
                          </Typography>
                          <Typography variant="body2" color="text.secondary">
                            {formatTimeRange(event.time.startTime, event.time.endTime)}
                          </Typography>
                          <Typography variant="body2" color="text.secondary" sx={{ mb: 1.5 }}>
                            {event.locationInfo}
                          </Typography>

                          <Stack direction="row" spacing={1}>
                            <Button
                              size="small"
                              startIcon={<EditIcon fontSize="small" />}
                              onClick={(e) => {
                                e.stopPropagation();
                                navigate(`/events/${event.id}/edit`);
                              }}
                            >
                              Edit
                            </Button>
                            <Button
                              size="small"
                              color="error"
                              startIcon={<DeleteIcon fontSize="small" />}
                              onClick={(e) => {
                                e.stopPropagation();
                                if (event.id) {
                                  void handleDelete(event.id, event.title);
                                }
                              }}
                            >
                              Delete
                            </Button>
                          </Stack>
                        </CardContent>
                      </Card>
                    ))}
                  </Stack>
                ) : (
                  <Box
                    sx={{
                      width: '100%',
                      overflowX: 'auto',
                    }}
                  >
                    <Table
                      size="small"
                      sx={{
                        minWidth: 700,
                        '& th, & td': {
                          px: 1.5,
                          py: 1,
                          whiteSpace: 'nowrap',
                        },
                        '& th': {
                          fontWeight: 'bold',
                        },
                      }}
                    >
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
                          <TableCell width="10%" align="right">
                            Actions
                          </TableCell>
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
                                backgroundColor: 'action.hover',
                              },
                            }}
                          >
                            <TableCell>{event.title}</TableCell>
                            <TableCell>{formatTimeRange(event.time.startTime, event.time.endTime)}</TableCell>
                            <TableCell>{event.locationInfo}</TableCell>
                            <TableCell align="right">
                              <Stack direction="row" spacing={0.5} justifyContent="flex-end">
                                <IconButton
                                  size="small"
                                  onClick={(e) => {
                                    e.stopPropagation();
                                    navigate(`/events/${event.id}/edit`);
                                  }}
                                  aria-label={`Edit ${event.title}`}
                                >
                                  <EditIcon fontSize="small" />
                                </IconButton>
                                <IconButton
                                  size="small"
                                  onClick={(e) => {
                                    e.stopPropagation();
                                    if (event.id) {
                                      void handleDelete(event.id, event.title);
                                    }
                                  }}
                                  aria-label={`Delete ${event.title}`}
                                >
                                  <DeleteIcon fontSize="small" />
                                </IconButton>
                              </Stack>
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
      </Stack>

      <Snackbar
        open={Boolean(deleteSuccess)}
        autoHideDuration={3200}
        onClose={() => setDeleteSuccess(null)}
        message={deleteSuccess}
      />
    </Box>
  );
};

export default EventList;
