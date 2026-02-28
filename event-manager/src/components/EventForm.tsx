import React, { useEffect, useMemo, useState } from 'react';
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
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  FormHelperText,
  Chip,
  Switch,
  FormControlLabel,
} from '@mui/material';
import { useNavigate, useParams } from 'react-router-dom';
import { Event, ExternalLink, EventTag } from '../types/Event';
import { Location } from '../types/Location';
import { eventService } from '../services/eventService';
import { dateService } from '../services/dateService';
import { locationService } from '../services/locationService';
import AddIcon from '@mui/icons-material/Add';
import DeleteIcon from '@mui/icons-material/Delete';
import LinkIcon from '@mui/icons-material/Link';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import ArrowForwardIcon from '@mui/icons-material/ArrowForward';
import ArrowUpwardIcon from '@mui/icons-material/ArrowUpward';
import ArrowDownwardIcon from '@mui/icons-material/ArrowDownward';
import { DateTimePicker } from '@mui/x-date-pickers/DateTimePicker';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';
import { formatDateKey, getUtcDateKeyParts, mergeDateKeyWithLocalTime } from '../utils/dateKey';
import { handleImageLoadError } from '../utils/imageFallback';

const ONE_HOUR_MS = 3_600_000;
const DEFAULT_EVENT_NOTIFICATION_LEAD_MINUTES = 15;
const MAX_EVENT_NOTIFICATION_LEAD_MINUTES = 24 * 60;

const createDefaultTimeRange = (dateTimestamp: number) => {
  const { year, month, day } = getUtcDateKeyParts(dateTimestamp);
  const start = new Date(year, month, day, 9, 0, 0, 0);

  return {
    startTime: start.getTime(),
    endTime: start.getTime() + ONE_HOUR_MS,
  };
};

const shiftTimestampToDate = (timestamp: number, targetDateKey: string): number => {
  return mergeDateKeyWithLocalTime(targetDateKey, timestamp);
};

const isValidHttpUrl = (value: string): boolean => {
  try {
    const parsed = new URL(value);
    return parsed.protocol === 'http:' || parsed.protocol === 'https:';
  } catch {
    return false;
  }
};

const sanitizeEventNotificationLeadMinutes = (value: unknown): number => {
  if (typeof value === 'number' && Number.isFinite(value)) {
    const parsed = Math.floor(value);
    if (parsed > 0 && parsed <= MAX_EVENT_NOTIFICATION_LEAD_MINUTES) {
      return parsed;
    }
  }

  if (typeof value === 'string') {
    const parsed = Number.parseInt(value, 10);
    if (Number.isFinite(parsed) && parsed > 0 && parsed <= MAX_EVENT_NOTIFICATION_LEAD_MINUTES) {
      return parsed;
    }
  }

  return DEFAULT_EVENT_NOTIFICATION_LEAD_MINUTES;
};

const EventForm: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const [availableDates, setAvailableDates] = useState<number[]>([]);
  const [availableLocations, setAvailableLocations] = useState<Location[]>([]);
  const [event, setEvent] = useState<Event>({
    id: 0,
    title: '',
    time: {
      startTime: Date.now(),
      endTime: Date.now() + ONE_HOUR_MS,
    },
    locationInfo: '',
    description: '',
    notifyBefore: false,
    notificationLeadMinutes: DEFAULT_EVENT_NOTIFICATION_LEAD_MINUTES,
    additionalLinks: [],
    dateKey: '',
    images: [],
    tag: EventTag.NORMAL,
  });

  const [loadingEvent, setLoadingEvent] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [validationError, setValidationError] = useState<string | null>(null);
  const [submitAttempted, setSubmitAttempted] = useState(false);
  const [imageUrl, setImageUrl] = useState('');

  useEffect(() => {
    const loadAvailableDates = async () => {
      try {
        const dates = await dateService.getAvailableDates();
        setAvailableDates(dates);

        if (dates.length > 0 && !id) {
          setEvent((prev) => {
            if (prev.dateKey) {
              return prev;
            }

            return {
              ...prev,
              dateKey: dates[0].toString(),
              time: createDefaultTimeRange(dates[0]),
            };
          });
        }
      } catch (loadError) {
        console.error('Error loading available dates:', loadError);
        setError('Failed to load available dates. Please try again.');
      }
    };

    const loadAvailableLocations = async () => {
      try {
        const locations = await locationService.getAllLocations();
        setAvailableLocations(locations);
      } catch (loadError) {
        console.error('Error loading available locations:', loadError);
        setError('Failed to load locations. Please try again.');
      }
    };

    const loadEvent = async () => {
      if (!id) {
        return;
      }

      try {
        setLoadingEvent(true);
        setError(null);
        const data = await eventService.getEvent(Number(id));
        setEvent(data);
      } catch (loadError) {
        setError('Failed to load event. Please try again.');
        console.error('Error loading event:', loadError);
      } finally {
        setLoadingEvent(false);
      }
    };

    void loadAvailableDates();
    void loadAvailableLocations();
    void loadEvent();
  }, [id]);

  const clearFeedback = () => {
    setSuccess(null);
    setError(null);
    setValidationError(null);
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    clearFeedback();
    setEvent((prev) => ({ ...prev, [name]: value }));
  };

  const handleDateChange = (newDateKey: string) => {
    clearFeedback();

    setEvent((prev) => {
      if (!newDateKey || prev.dateKey === newDateKey) {
        return { ...prev, dateKey: newDateKey };
      }

      return {
        ...prev,
        dateKey: newDateKey,
        time: {
          startTime: shiftTimestampToDate(prev.time.startTime, newDateKey),
          endTime: shiftTimestampToDate(prev.time.endTime, newDateKey),
        },
      };
    });
  };

  const handleTimeChange = (field: 'startTime' | 'endTime', value: Date | null) => {
    if (!value) {
      return;
    }

    clearFeedback();

    setEvent((prev) => ({
      ...prev,
      time: {
        ...prev.time,
        [field]: value.getTime(),
      },
    }));
  };

  const handleLinkChange = (index: number, field: keyof ExternalLink, value: string) => {
    clearFeedback();

    setEvent((prev) => {
      const nextLinks = [...prev.additionalLinks];
      nextLinks[index] = { ...nextLinks[index], [field]: value };
      return { ...prev, additionalLinks: nextLinks };
    });
  };

  const addLink = () => {
    clearFeedback();

    setEvent((prev) => ({
      ...prev,
      additionalLinks: [...prev.additionalLinks, { displayName: '', url: '' }],
    }));
  };

  const removeLink = (index: number) => {
    clearFeedback();

    setEvent((prev) => ({
      ...prev,
      additionalLinks: prev.additionalLinks.filter((_, i) => i !== index),
    }));
  };

  const moveLinkItem = (fromIndex: number, toIndex: number) => {
    clearFeedback();

    setEvent((prev) => {
      const nextLinks = [...prev.additionalLinks];
      const [movedItem] = nextLinks.splice(fromIndex, 1);
      nextLinks.splice(toIndex, 0, movedItem);
      return { ...prev, additionalLinks: nextLinks };
    });
  };

  const moveImage = (fromIndex: number, toIndex: number) => {
    if (toIndex < 0 || toIndex >= event.images.length) {
      return;
    }

    clearFeedback();

    setEvent((prev) => {
      const nextImages = [...prev.images];
      const [movedImage] = nextImages.splice(fromIndex, 1);
      nextImages.splice(toIndex, 0, movedImage);
      return { ...prev, images: nextImages };
    });
  };

  const handleAddImage = () => {
    if (!imageUrl.trim()) {
      return;
    }

    clearFeedback();

    setEvent((prev) => ({
      ...prev,
      images: [...prev.images, imageUrl.trim()],
    }));
    setImageUrl('');
  };

  const handleRemoveImage = (index: number) => {
    clearFeedback();

    setEvent((prev) => ({
      ...prev,
      images: prev.images.filter((_, i) => i !== index),
    }));
  };

  const handleImageKeyDown = (index: number, e: React.KeyboardEvent) => {
    if (e.key === 'ArrowLeft') {
      e.preventDefault();
      moveImage(index, index - 1);
    }

    if (e.key === 'ArrowRight') {
      e.preventDefault();
      moveImage(index, index + 1);
    }
  };

  const validateEvent = (): string | null => {
    if (!event.dateKey) {
      return 'Select a date before saving.';
    }

    if (!event.title.trim()) {
      return 'Event title is required.';
    }

    if (!event.locationInfo.trim()) {
      return 'Select a location for this event.';
    }

    if (event.time.endTime <= event.time.startTime) {
      return 'Event end time must be after the start time.';
    }

    if (
      event.notifyBefore &&
      (event.notificationLeadMinutes < 1 || event.notificationLeadMinutes > MAX_EVENT_NOTIFICATION_LEAD_MINUTES)
    ) {
      return `Reminder lead time is invalid. Use 1-${MAX_EVENT_NOTIFICATION_LEAD_MINUTES} minutes.`;
    }

    const incompleteLinkIndex = event.additionalLinks.findIndex((link) => {
      const hasLabel = Boolean(link.displayName.trim());
      const hasUrl = Boolean(link.url.trim());
      return hasLabel !== hasUrl;
    });

    if (incompleteLinkIndex >= 0) {
      return `Additional link ${incompleteLinkIndex + 1} must include both display name and URL.`;
    }

    const invalidLinkIndex = event.additionalLinks.findIndex(
      (link) => link.url.trim() && !isValidHttpUrl(link.url.trim()),
    );

    if (invalidLinkIndex >= 0) {
      return `Additional link ${invalidLinkIndex + 1} needs a valid http(s) URL.`;
    }

    return null;
  };

  const timeRangeInvalid = event.time.endTime <= event.time.startTime;

  const invalidLinksCount = useMemo(() => {
    return event.additionalLinks.filter((link) => link.url.trim() && !isValidHttpUrl(link.url.trim())).length;
  }, [event.additionalLinks]);
  const hasSelectedDate = availableDates.some((timestamp) => timestamp.toString() === event.dateKey);
  const hasSelectedLocation = availableLocations.some(
    (location) => location.locationName === event.locationInfo,
  );

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitAttempted(true);
    setSaving(true);
    setError(null);
    setSuccess(null);

    const validationIssue = validateEvent();
    if (validationIssue) {
      setValidationError(validationIssue);
      setSaving(false);
      return;
    }

    setValidationError(null);

    try {
      const eventToSave: Event = {
        ...event,
        title: event.title.trim(),
        locationInfo: event.locationInfo,
        description: event.description.trim(),
        notifyBefore: event.notifyBefore === true,
        notificationLeadMinutes: sanitizeEventNotificationLeadMinutes(event.notificationLeadMinutes),
        additionalLinks: event.additionalLinks,
        dateKey: event.dateKey,
        images: event.images,
        tag: event.tag || EventTag.NORMAL,
      };

      if (id) {
        await eventService.updateEvent(Number(id), { ...eventToSave, id: Number(id) });
        setSuccess('Event updated successfully.');
      } else {
        const created = await eventService.createEvent(eventToSave);
        setEvent(created);
        setSuccess('Event created successfully.');
        navigate(`/events/${created.id}/edit`, { replace: true });
      }
    } catch (saveError) {
      setError('Failed to save event. Please try again.');
      console.error('Error saving event:', saveError);
    } finally {
      setSaving(false);
    }
  };

  if (loadingEvent) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', p: 3 }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <LocalizationProvider dateAdapter={AdapterDateFns}>
      <Paper sx={{ p: { xs: 2, sm: 3 } }}>
        <Stack
          direction={{ xs: 'column', sm: 'row' }}
          justifyContent="space-between"
          alignItems={{ xs: 'flex-start', sm: 'center' }}
          spacing={2}
          sx={{ mb: 3 }}
        >
          <Box>
            <Typography variant="h5">{id ? 'Edit Event' : 'Create Event'}</Typography>
            <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
              Keep event details accurate for attendees and app users.
            </Typography>
          </Box>
          <Button startIcon={<ArrowBackIcon />} onClick={() => navigate('/')} variant="outlined">
            Back to List
          </Button>
        </Stack>

        <Stack direction="row" spacing={1} flexWrap="wrap" sx={{ mb: 3 }}>
          <Chip
            size="small"
            color={event.notifyBefore ? 'primary' : 'default'}
            label={event.notifyBefore ? `Reminder ${event.notificationLeadMinutes} min before` : 'Reminder disabled'}
          />
          <Chip size="small" variant="outlined" label={`${event.additionalLinks.length} link${event.additionalLinks.length === 1 ? '' : 's'}`} />
          <Chip size="small" variant="outlined" label={`${event.images.length} image${event.images.length === 1 ? '' : 's'}`} />
        </Stack>

        {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
        {validationError && <Alert severity="warning" sx={{ mb: 2 }}>{validationError}</Alert>}
        {!availableDates.length && (
          <Alert severity="info" sx={{ mb: 2 }}>
            No event dates are available yet. Add a date in Date Manager before creating events.
          </Alert>
        )}
        {!availableLocations.length && (
          <Alert severity="info" sx={{ mb: 2 }}>
            No locations are available yet. Add a location in Locations Manager before creating events.
          </Alert>
        )}

        <form onSubmit={handleSubmit}>
          <Stack spacing={2.5}>
            <Paper variant="outlined" sx={{ p: 2.5 }}>
              <Typography variant="h6" sx={{ mb: 2 }}>
                Event Basics
              </Typography>

              <Stack spacing={2}>
                <FormControl fullWidth required error={submitAttempted && !event.dateKey}>
                  <InputLabel>Date</InputLabel>
                  <Select
                    value={hasSelectedDate ? event.dateKey : ''}
                    label="Date"
                    onChange={(e) => handleDateChange(e.target.value)}
                    sx={{ textAlign: 'left' }}
                  >
                    {!hasSelectedDate && event.dateKey && (
                      <MenuItem value={event.dateKey} disabled sx={{ textAlign: 'left' }}>
                        Date no longer available
                      </MenuItem>
                    )}
                    {availableDates.map((timestamp) => (
                      <MenuItem key={timestamp} value={timestamp.toString()} sx={{ textAlign: 'left' }}>
                        {formatDateKey(timestamp, {
                          weekday: 'long',
                          month: 'long',
                          day: 'numeric',
                          year: 'numeric',
                        })}
                      </MenuItem>
                    ))}
                  </Select>
                  {submitAttempted && !event.dateKey && <FormHelperText>Select a date.</FormHelperText>}
                </FormControl>

                <TextField
                  label="Title"
                  name="title"
                  value={event.title}
                  onChange={handleChange}
                  required
                  fullWidth
                  error={submitAttempted && !event.title.trim()}
                  helperText={submitAttempted && !event.title.trim() ? 'Enter an event title.' : ' '}
                />

                <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
                  <DateTimePicker
                    label="Start Time"
                    value={new Date(event.time.startTime)}
                    onChange={(value) => handleTimeChange('startTime', value)}
                    slotProps={{
                      textField: {
                        fullWidth: true,
                      },
                    }}
                  />
                  <DateTimePicker
                    label="End Time"
                    value={new Date(event.time.endTime)}
                    onChange={(value) => handleTimeChange('endTime', value)}
                    slotProps={{
                      textField: {
                        fullWidth: true,
                        error: submitAttempted && timeRangeInvalid,
                        helperText: submitAttempted && timeRangeInvalid ? 'End time must be after start time.' : ' ',
                      },
                    }}
                  />
                </Stack>

                <FormControl fullWidth required error={submitAttempted && !event.locationInfo}>
                  <InputLabel>Location</InputLabel>
                  <Select
                    value={hasSelectedLocation ? event.locationInfo : ''}
                    label="Location"
                    onChange={(e) => {
                      clearFeedback();
                      setEvent((prev) => ({ ...prev, locationInfo: e.target.value }));
                    }}
                    sx={{ textAlign: 'left' }}
                  >
                    {!hasSelectedLocation && event.locationInfo && (
                      <MenuItem value={event.locationInfo} disabled sx={{ textAlign: 'left' }}>
                        {event.locationInfo} (not in locations list)
                      </MenuItem>
                    )}
                    {availableLocations.map((location) => (
                      <MenuItem key={location.id} value={location.locationName} sx={{ textAlign: 'left' }}>
                        {location.locationName}
                      </MenuItem>
                    ))}
                  </Select>
                  {submitAttempted && !event.locationInfo && <FormHelperText>Select a location.</FormHelperText>}
                </FormControl>

                <TextField
                  label="Description"
                  name="description"
                  value={event.description}
                  onChange={handleChange}
                  multiline
                  rows={4}
                  fullWidth
                  helperText="Optional but recommended."
                />

                <FormControl fullWidth>
                  <InputLabel>Event Tag</InputLabel>
                  <Select
                    value={event.tag}
                    label="Event Tag"
                    onChange={(e) => {
                      clearFeedback();
                      setEvent((prev) => ({ ...prev, tag: e.target.value as EventTag }));
                    }}
                    sx={{ textAlign: 'left' }}
                  >
                    <MenuItem value={EventTag.NORMAL} sx={{ textAlign: 'left' }}>
                      Normal
                    </MenuItem>
                    <MenuItem value={EventTag.HIGHLIGHTED} sx={{ textAlign: 'left' }}>
                      Highlighted
                    </MenuItem>
                    <MenuItem value={EventTag.BREAK} sx={{ textAlign: 'left' }}>
                      Break
                    </MenuItem>
                  </Select>
                </FormControl>

                <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} alignItems={{ sm: 'center' }}>
                  <FormControlLabel
                    control={
                      <Switch
                        checked={event.notifyBefore}
                        onChange={(e) => {
                          clearFeedback();
                          setEvent((prev) => ({ ...prev, notifyBefore: e.target.checked }));
                        }}
                      />
                    }
                    label="Send reminder before this event"
                  />
                  <TextField
                    type="number"
                    label="Minutes Before Start"
                    value={event.notificationLeadMinutes}
                    onChange={(e) => {
                      clearFeedback();
                      setEvent((prev) => ({
                        ...prev,
                        notificationLeadMinutes: sanitizeEventNotificationLeadMinutes(e.target.value),
                      }));
                    }}
                    disabled={!event.notifyBefore}
                    sx={{ minWidth: { xs: '100%', sm: 220 } }}
                    inputProps={{ min: 1, max: MAX_EVENT_NOTIFICATION_LEAD_MINUTES, step: 1 }}
                    error={
                      submitAttempted &&
                      event.notifyBefore &&
                      (event.notificationLeadMinutes < 1 ||
                        event.notificationLeadMinutes > MAX_EVENT_NOTIFICATION_LEAD_MINUTES)
                    }
                    helperText={
                      submitAttempted &&
                      event.notifyBefore &&
                      (event.notificationLeadMinutes < 1 ||
                        event.notificationLeadMinutes > MAX_EVENT_NOTIFICATION_LEAD_MINUTES)
                        ? `Use 1-${MAX_EVENT_NOTIFICATION_LEAD_MINUTES}.`
                        : ' '
                    }
                  />
                </Stack>
              </Stack>
            </Paper>

            <Paper variant="outlined" sx={{ p: 2.5 }}>
              <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 2 }}>
                <Typography variant="h6">Additional Links</Typography>
                <Button startIcon={<LinkIcon />} onClick={addLink} variant="outlined">
                  Add Link
                </Button>
              </Stack>

              {!!invalidLinksCount && (
                <Alert severity="warning" sx={{ mb: 2 }}>
                  {invalidLinksCount} link{invalidLinksCount > 1 ? 's have' : ' has'} an invalid URL format.
                </Alert>
              )}

              {!event.additionalLinks.length ? (
                <Typography variant="body2" color="text.secondary">
                  No additional links yet.
                </Typography>
              ) : (
                event.additionalLinks.map((link, index) => (
                  <Paper key={index} variant="outlined" sx={{ p: 2, mb: 2 }}>
                    <Stack direction="row" justifyContent="flex-end" spacing={1}>
                      <IconButton
                        onClick={() => moveLinkItem(index, index - 1)}
                        disabled={index === 0}
                        size="small"
                        aria-label="Move link up"
                      >
                        <ArrowUpwardIcon />
                      </IconButton>
                      <IconButton
                        onClick={() => moveLinkItem(index, index + 1)}
                        disabled={index === event.additionalLinks.length - 1}
                        size="small"
                        aria-label="Move link down"
                      >
                        <ArrowDownwardIcon />
                      </IconButton>
                      <IconButton onClick={() => removeLink(index)} aria-label="Delete link">
                        <DeleteIcon />
                      </IconButton>
                    </Stack>

                    <Stack spacing={2}>
                      <TextField
                        label="Display Name"
                        value={link.displayName}
                        onChange={(e) => handleLinkChange(index, 'displayName', e.target.value)}
                        fullWidth
                      />
                      <TextField
                        label="URL"
                        type="url"
                        value={link.url}
                        onChange={(e) => handleLinkChange(index, 'url', e.target.value)}
                        fullWidth
                        placeholder="https://example.com"
                      />
                    </Stack>
                  </Paper>
                ))
              )}
            </Paper>

            <Paper variant="outlined" sx={{ p: 2.5 }}>
              <Stack
                direction={{ xs: 'column', sm: 'row' }}
                justifyContent="space-between"
                alignItems={{ xs: 'stretch', sm: 'center' }}
                spacing={1}
                sx={{ mb: 2 }}
              >
                <Typography variant="h6">Images</Typography>
                <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1}>
                  <TextField
                    size="small"
                    placeholder="Image URL"
                    value={imageUrl}
                    onChange={(e) => setImageUrl(e.target.value)}
                    sx={{ width: { xs: '100%', sm: 320 } }}
                  />
                  <Button startIcon={<AddIcon />} onClick={handleAddImage} variant="outlined" disabled={!imageUrl.trim()}>
                    Add Image
                  </Button>
                </Stack>
              </Stack>

              <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                Tip: Use arrow controls or keyboard arrows to reorder images.
              </Typography>

              <Box
                sx={{
                  display: 'grid',
                  gridTemplateColumns: 'repeat(auto-fill, minmax(220px, 1fr))',
                  gap: 2,
                  width: '100%',
                  minHeight: '120px',
                }}
              >
                {event.images.map((image, index) => (
                  <Paper
                    key={index}
                    tabIndex={0}
                    onKeyDown={(e) => handleImageKeyDown(index, e)}
                    sx={{
                      p: 1,
                      display: 'flex',
                      flexDirection: 'column',
                      gap: 1,
                      position: 'relative',
                      transition: 'transform 0.2s ease, box-shadow 0.2s ease',
                      '&:focus': {
                        outline: 'none',
                        transform: 'scale(1.01)',
                        boxShadow: (theme) => theme.shadows[4],
                      },
                    }}
                  >
                    <Box
                      sx={{
                        display: 'flex',
                        gap: 0.5,
                        position: 'absolute',
                        top: 8,
                        left: 8,
                        zIndex: 1,
                      }}
                    >
                      <IconButton
                        size="small"
                        disabled={index === 0}
                        onClick={() => moveImage(index, index - 1)}
                        sx={{
                          backgroundColor: 'rgba(255, 255, 255, 0.9)',
                          border: '1px solid rgba(0, 0, 0, 0.2)',
                        }}
                        aria-label="Move image left"
                      >
                        <ArrowBackIcon fontSize="small" />
                      </IconButton>
                      <IconButton
                        size="small"
                        disabled={index === event.images.length - 1}
                        onClick={() => moveImage(index, index + 1)}
                        sx={{
                          backgroundColor: 'rgba(255, 255, 255, 0.9)',
                          border: '1px solid rgba(0, 0, 0, 0.2)',
                        }}
                        aria-label="Move image right"
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
                        borderRadius: 1,
                      }}
                      onError={(e) => {
                        handleImageLoadError(e.currentTarget);
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
                      }}
                      aria-label="Delete image"
                    >
                      <DeleteIcon fontSize="small" />
                    </IconButton>
                  </Paper>
                ))}
              </Box>
            </Paper>

            <Paper
              variant="outlined"
              sx={{
                p: 2,
                position: 'sticky',
                bottom: 0,
                zIndex: 1,
                backgroundColor: 'background.paper',
              }}
            >
              <Stack
                direction={{ xs: 'column', sm: 'row' }}
                justifyContent="space-between"
                alignItems={{ xs: 'stretch', sm: 'center' }}
                spacing={1.5}
              >
                {success ? (
                  <Alert
                    severity="success"
                    sx={{ flex: 1 }}
                    action={
                      <Button color="inherit" size="small" onClick={() => navigate('/')}>
                        Back to List
                      </Button>
                    }
                  >
                    {success}
                  </Alert>
                ) : (
                  <Typography variant="body2" color="text.secondary">
                    Review fields, then save your changes.
                  </Typography>
                )}

                <Stack direction="row" spacing={1} justifyContent="flex-end">
                  <Button variant="text" onClick={() => navigate('/')} disabled={saving}>
                    Cancel
                  </Button>
                  <Button
                    type="submit"
                    variant="contained"
                    color="primary"
                    disabled={saving || !availableDates.length || !availableLocations.length}
                  >
                    {saving ? <CircularProgress size={24} color="inherit" /> : id ? 'Save Event' : 'Create Event'}
                  </Button>
                </Stack>
              </Stack>
            </Paper>
          </Stack>
        </form>
      </Paper>
    </LocalizationProvider>
  );
};

export default EventForm;
