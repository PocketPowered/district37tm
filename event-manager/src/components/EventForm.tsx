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
import UploadFileIcon from '@mui/icons-material/UploadFile';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import ArrowForwardIcon from '@mui/icons-material/ArrowForward';
import ArrowUpwardIcon from '@mui/icons-material/ArrowUpward';
import ArrowDownwardIcon from '@mui/icons-material/ArrowDownward';
import { DateTimePicker } from '@mui/x-date-pickers/DateTimePicker';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';
import { formatDateKey, getUtcDateKeyParts, mergeDateKeyWithLocalTime } from '../utils/dateKey';
import { handleImageLoadError } from '../utils/imageFallback';
import { useConference } from '../contexts/ConferenceContext';
import {
  ACCEPTED_IMAGE_FILE_INPUT,
  imageUploadService,
} from '../services/imageUploadService';
import { eventReminderService, EventReminderConfig } from '../services/eventReminderService';

const ONE_HOUR_MS = 3_600_000;
const DEFAULT_EVENT_NOTIFICATION_LEAD_MINUTES = 15;
const MAX_EVENT_NOTIFICATION_LEAD_MINUTES = 24 * 60;
const MAX_EVENT_NOTIFICATION_CHANNEL_LENGTH = 900;
const EVENT_NOTIFICATION_CHANNEL_PATTERN = /^[A-Za-z0-9\-_.~%]+$/;
const DEFAULT_REMINDER_BODY_TEMPLATE = '';

type EventReminderDraft = EventReminderConfig;

const createDefaultReminder = (eventId = 0): EventReminderDraft => ({
  eventId,
  isEnabled: true,
  leadMinutes: DEFAULT_EVENT_NOTIFICATION_LEAD_MINUTES,
  channel: '',
  bodyTemplate: DEFAULT_REMINDER_BODY_TEMPLATE,
  sortOrder: 0,
});

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
    if (parsed < 0) return 0;
    if (parsed > MAX_EVENT_NOTIFICATION_LEAD_MINUTES) return MAX_EVENT_NOTIFICATION_LEAD_MINUTES;
    return parsed;
  }

  if (typeof value === 'string') {
    const trimmed = value.trim();
    if (!trimmed) return 0;
    const parsed = Number.parseInt(trimmed, 10);
    if (Number.isFinite(parsed)) {
      if (parsed < 0) return 0;
      if (parsed > MAX_EVENT_NOTIFICATION_LEAD_MINUTES) return MAX_EVENT_NOTIFICATION_LEAD_MINUTES;
      return parsed;
    }
  }

  return DEFAULT_EVENT_NOTIFICATION_LEAD_MINUTES;
};

const sanitizeNotificationChannel = (value: string): string => {
  return value.trim();
};

const isValidNotificationChannel = (value: string): boolean => {
  if (!value) {
    return true;
  }

  return value.length <= MAX_EVENT_NOTIFICATION_CHANNEL_LENGTH && EVENT_NOTIFICATION_CHANNEL_PATTERN.test(value);
};

const EventForm: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { selectedConference, loading: conferenceLoading } = useConference();

  const [availableDates, setAvailableDates] = useState<number[]>([]);
  const [availableLocations, setAvailableLocations] = useState<Location[]>([]);
  const [event, setEvent] = useState<Event>({
    id: 0,
    conferenceId: selectedConference?.id || 0,
    title: '',
    time: {
      startTime: Date.now(),
      endTime: Date.now() + ONE_HOUR_MS,
    },
    locationInfo: '',
    description: '',
    notifyBefore: false,
    notificationLeadMinutes: DEFAULT_EVENT_NOTIFICATION_LEAD_MINUTES,
    notificationChannel: '',
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
  const [uploadingImage, setUploadingImage] = useState(false);
  const [remindersEnabled, setRemindersEnabled] = useState(false);
  const [reminders, setReminders] = useState<EventReminderDraft[]>([]);

  useEffect(() => {
    const loadAvailableDates = async () => {
      if (!selectedConference) {
        setAvailableDates([]);
        return;
      }

      try {
        const dates = await dateService.getAvailableDates(selectedConference);
        setAvailableDates(dates);

        if (!id) {
          setEvent((prev) => {
            const hasCurrentDateInRange = dates.some((timestamp) => timestamp.toString() === prev.dateKey);

            if (!dates.length) {
              return {
                ...prev,
                conferenceId: selectedConference.id,
                dateKey: '',
              };
            }

            if (hasCurrentDateInRange) {
              return {
                ...prev,
                conferenceId: selectedConference.id,
              };
            }

            return {
              ...prev,
              conferenceId: selectedConference.id,
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
        if (!selectedConference) {
          setAvailableLocations([]);
          return;
        }
        const locations = await locationService.getAllLocations(selectedConference.id);
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
      if (!selectedConference) {
        return;
      }

      try {
        setLoadingEvent(true);
        setError(null);
        const data = await eventService.getEvent(Number(id), selectedConference.id);
        setEvent(data);

        const loadedReminders = await eventReminderService.getEventReminders(data.id);
        if (loadedReminders.length > 0) {
          setRemindersEnabled(true);
          setReminders(
            loadedReminders.map((reminder, index) => ({
              ...reminder,
              eventId: data.id,
              sortOrder: index,
            })),
          );
        } else if (data.notifyBefore) {
          setRemindersEnabled(true);
          setReminders([
            {
              ...createDefaultReminder(data.id),
              leadMinutes: sanitizeEventNotificationLeadMinutes(data.notificationLeadMinutes),
              channel: sanitizeNotificationChannel(data.notificationChannel),
            },
          ]);
        } else {
          setRemindersEnabled(false);
          setReminders([]);
        }
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
  }, [id, selectedConference]);

  const clearFeedback = () => {
    setSuccess(null);
    setError(null);
    setValidationError(null);
  };

  const updateReminder = (index: number, patch: Partial<EventReminderDraft>) => {
    clearFeedback();
    setReminders((prev) => prev.map((item, itemIndex) => (itemIndex === index ? { ...item, ...patch } : item)));
  };

  const handleAddReminder = () => {
    clearFeedback();
    setReminders((prev) => [
      ...prev,
      {
        ...createDefaultReminder(event.id),
        sortOrder: prev.length,
      },
    ]);
  };

  const handleRemoveReminder = (index: number) => {
    clearFeedback();
    setReminders((prev) => prev.filter((_, itemIndex) => itemIndex !== index));
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

  const handleUploadImage = async (file: File) => {
    if (!selectedConference) {
      setError('Select a conference before uploading images.');
      return;
    }

    clearFeedback();
    setUploadingImage(true);

    try {
      const uploadedUrl = await imageUploadService.uploadAdminImage({
        file,
        conferenceId: selectedConference.id,
        category: 'events',
      });

      setEvent((prev) => ({
        ...prev,
        images: [...prev.images, uploadedUrl],
      }));
    } catch (uploadError) {
      const message = uploadError instanceof Error ? uploadError.message : 'Failed to upload image.';
      setError(message);
    } finally {
      setUploadingImage(false);
    }
  };

  const handleImageFileSelection = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    e.target.value = '';
    if (!file) {
      return;
    }
    void handleUploadImage(file);
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
    if (!availableDates.length) {
      return 'Set the selected conference date range before saving events.';
    }

    if (!event.dateKey) {
      return 'Select a date before saving.';
    }

    if (!availableDates.some((timestamp) => timestamp.toString() === event.dateKey)) {
      return 'Select a date within the selected conference range.';
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

    if (remindersEnabled) {
      if (!reminders.length) {
        return 'Add at least one reminder or turn off reminder notifications.';
      }

      const enabledReminders = reminders.filter((reminder) => reminder.isEnabled);
      if (!enabledReminders.length) {
        return 'Enable at least one reminder or turn off reminder notifications.';
      }

      const invalidLeadIndex = reminders.findIndex(
        (reminder) =>
          reminder.leadMinutes < 0 || reminder.leadMinutes > MAX_EVENT_NOTIFICATION_LEAD_MINUTES,
      );
      if (invalidLeadIndex >= 0) {
        return `Reminder ${invalidLeadIndex + 1} has an invalid lead time. Use 0-${MAX_EVENT_NOTIFICATION_LEAD_MINUTES} minutes.`;
      }

      const invalidChannelIndex = reminders.findIndex(
        (reminder) => !isValidNotificationChannel(reminder.channel.trim()),
      );
      if (invalidChannelIndex >= 0) {
        return `Reminder ${invalidChannelIndex + 1} channel is invalid. Use letters, numbers, "-", "_", ".", "~", or "%" only.`;
      }
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
  const enabledReminderCount = useMemo(
    () => reminders.filter((reminder) => reminder.isEnabled).length,
    [reminders],
  );
  const primaryReminder = useMemo(
    () => reminders.find((reminder) => reminder.isEnabled) || reminders[0] || null,
    [reminders],
  );
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

    if (!selectedConference) {
      setValidationError('Select a conference before saving events.');
      setSaving(false);
      return;
    }

    const validationIssue = validateEvent();
    if (validationIssue) {
      setValidationError(validationIssue);
      setSaving(false);
      return;
    }

    setValidationError(null);

    try {
      const normalizedReminders = remindersEnabled
        ? reminders.map((reminder, index) => ({
            ...reminder,
            eventId: event.id,
            isEnabled: reminder.isEnabled !== false,
            leadMinutes: sanitizeEventNotificationLeadMinutes(reminder.leadMinutes),
            channel: sanitizeNotificationChannel(reminder.channel),
            bodyTemplate: reminder.bodyTemplate.trim(),
            sortOrder: index,
          }))
        : [];
      const activeReminder = normalizedReminders.find((reminder) => reminder.isEnabled);

      const eventToSave: Event = {
        ...event,
        conferenceId: selectedConference.id,
        title: event.title.trim(),
        locationInfo: event.locationInfo,
        description: event.description.trim(),
        notifyBefore: Boolean(activeReminder),
        notificationLeadMinutes: activeReminder
          ? activeReminder.leadMinutes
          : DEFAULT_EVENT_NOTIFICATION_LEAD_MINUTES,
        notificationChannel: activeReminder ? activeReminder.channel : '',
        additionalLinks: event.additionalLinks,
        dateKey: event.dateKey,
        images: event.images,
        tag: event.tag || EventTag.NORMAL,
      };

      if (id) {
        const savedEvent = await eventService.updateEvent(Number(id), { ...eventToSave, id: Number(id) });
        await eventReminderService.replaceEventReminders(
          savedEvent.id,
          normalizedReminders.map((reminder) => ({ ...reminder, eventId: savedEvent.id })),
        );
        setEvent(savedEvent);
        setSuccess('Event updated successfully.');
      } else {
        const createdEvent = await eventService.createEvent(eventToSave);
        const savedReminders = await eventReminderService.replaceEventReminders(
          createdEvent.id,
          normalizedReminders.map((reminder) => ({ ...reminder, eventId: createdEvent.id })),
        );
        setEvent(createdEvent);
        setReminders(
          savedReminders.map((reminder, index) => ({
            ...reminder,
            eventId: createdEvent.id,
            sortOrder: index,
          })),
        );
        setSuccess('Event created successfully.');
        navigate(`/events/${createdEvent.id}/edit`, { replace: true });
      }
    } catch (saveError) {
      setError('Failed to save event. Please try again.');
      console.error('Error saving event:', saveError);
    } finally {
      setSaving(false);
    }
  };

  if (conferenceLoading || loadingEvent) {
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
          <Box sx={{ textAlign: 'left' }}>
            <Typography variant="h5">{id ? 'Edit Event' : 'Create Event'}</Typography>
            <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
              Keep event details accurate for attendees and app users.
            </Typography>
          </Box>
          <Button startIcon={<ArrowBackIcon />} onClick={() => navigate('/events')} variant="outlined">
            Back to List
          </Button>
        </Stack>

        <Stack direction="row" spacing={1} flexWrap="wrap" sx={{ mb: 3 }}>
          <Chip
            size="small"
            color={enabledReminderCount > 0 ? 'primary' : 'default'}
            label={
              enabledReminderCount > 0
                ? `${enabledReminderCount} reminder${enabledReminderCount === 1 ? '' : 's'} enabled`
                : remindersEnabled
                  ? 'Reminder notifications enabled'
                  : 'Reminder disabled'
            }
          />
          {primaryReminder && (
            <Chip
              size="small"
              variant="outlined"
              label={`Primary: ${primaryReminder.leadMinutes} min · ${primaryReminder.channel.trim() || 'GENERAL'}`}
            />
          )}
          <Chip size="small" variant="outlined" label={`${event.additionalLinks.length} link${event.additionalLinks.length === 1 ? '' : 's'}`} />
          <Chip size="small" variant="outlined" label={`${event.images.length} image${event.images.length === 1 ? '' : 's'}`} />
        </Stack>

        {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
        {validationError && <Alert severity="warning" sx={{ mb: 2 }}>{validationError}</Alert>}
        {!selectedConference && (
          <Alert severity="warning" sx={{ mb: 2 }}>
            No conference selected. Choose a conference before editing events.
          </Alert>
        )}
        {selectedConference && (
          <Alert severity="info" sx={{ mb: 2 }}>
            Editing event for selected conference: <strong>{selectedConference.name}</strong>
          </Alert>
        )}
        {!availableDates.length && (
          <Alert severity="info" sx={{ mb: 2 }}>
            No conference dates are available. Set the selected conference start and end dates in Conferences.
          </Alert>
        )}
        {event.dateKey && !hasSelectedDate && (
          <Alert severity="warning" sx={{ mb: 2 }}>
            The event date is outside the selected conference range. Choose a valid date before saving.
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
                <FormControl
                  fullWidth
                  required
                  error={submitAttempted && (!event.dateKey || !hasSelectedDate)}
                >
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
                  {submitAttempted && event.dateKey && !hasSelectedDate && (
                    <FormHelperText>Select a date within the conference range.</FormHelperText>
                  )}
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

              </Stack>
            </Paper>

            <Paper variant="outlined" sx={{ p: 2.5 }}>
              <Typography variant="h6" sx={{ mb: 2 }}>
                Reminder Notifications
              </Typography>

              <Stack spacing={2}>
                <FormControlLabel
                  control={
                    <Switch
                      checked={remindersEnabled}
                      onChange={(e) => {
                        clearFeedback();
                        const nextEnabled = e.target.checked;
                        setRemindersEnabled(nextEnabled);
                        if (nextEnabled && reminders.length === 0) {
                          setReminders([{ ...createDefaultReminder(event.id), sortOrder: 0 }]);
                        }
                        if (!nextEnabled) {
                          setReminders([]);
                        }
                      }}
                    />
                  }
                  label="Enable reminder notifications for this event"
                />

                {remindersEnabled && (
                  <>
                    {!reminders.length ? (
                      <Alert severity="info">Add at least one reminder rule.</Alert>
                    ) : (
                      reminders.map((reminder, index) => {
                        const invalidLead =
                          reminder.leadMinutes < 0 ||
                          reminder.leadMinutes > MAX_EVENT_NOTIFICATION_LEAD_MINUTES;
                        const invalidChannel = !isValidNotificationChannel(reminder.channel.trim());

                        return (
                          <Paper key={`${reminder.id || 'new'}-${index}`} variant="outlined" sx={{ p: 2 }}>
                            <Stack
                              direction={{ xs: 'column', sm: 'row' }}
                              justifyContent="space-between"
                              spacing={1}
                              sx={{ mb: 2 }}
                            >
                              <Typography variant="subtitle1">Reminder {index + 1}</Typography>
                              <Stack direction="row" spacing={1} alignItems="center">
                                <FormControlLabel
                                  control={
                                    <Switch
                                      checked={reminder.isEnabled}
                                      onChange={(e) => updateReminder(index, { isEnabled: e.target.checked })}
                                    />
                                  }
                                  label={reminder.isEnabled ? 'Enabled' : 'Disabled'}
                                />
                                <IconButton
                                  onClick={() => handleRemoveReminder(index)}
                                  aria-label={`Remove reminder ${index + 1}`}
                                >
                                  <DeleteIcon />
                                </IconButton>
                              </Stack>
                            </Stack>

                            <Stack spacing={2}>
                              <TextField
                                type="number"
                                label="Minutes Before Start"
                                value={reminder.leadMinutes}
                                onChange={(e) =>
                                  updateReminder(index, {
                                    leadMinutes: sanitizeEventNotificationLeadMinutes(e.target.value),
                                  })
                                }
                                inputProps={{ min: 0, max: MAX_EVENT_NOTIFICATION_LEAD_MINUTES, step: 1 }}
                                error={submitAttempted && invalidLead}
                                helperText={
                                  submitAttempted && invalidLead
                                    ? `Use 0-${MAX_EVENT_NOTIFICATION_LEAD_MINUTES}.`
                                    : '0 sends at event start.'
                                }
                              />

                              <TextField
                                label="Reminder Channel (optional)"
                                value={reminder.channel}
                                onChange={(e) => updateReminder(index, { channel: e.target.value })}
                                placeholder="GENERAL or APP_ENV_DEBUG"
                                error={submitAttempted && invalidChannel}
                                helperText={
                                  submitAttempted && invalidChannel
                                    ? 'Allowed: letters, numbers, "-", "_", ".", "~", "%".'
                                    : 'If blank, GENERAL is used.'
                                }
                              />

                              <TextField
                                label="Body Template (optional)"
                                value={reminder.bodyTemplate}
                                onChange={(e) => updateReminder(index, { bodyTemplate: e.target.value })}
                                multiline
                                rows={3}
                                helperText={
                                  'Leave blank for default. Placeholders: {{event_name}}, {{start_time}}, {{location}}, {{conference_name}}.'
                                }
                              />
                            </Stack>
                          </Paper>
                        );
                      })
                    )}

                    <Button startIcon={<AddIcon />} onClick={handleAddReminder} variant="outlined">
                      Add Reminder
                    </Button>
                  </>
                )}
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
                  <Button
                    component="label"
                    startIcon={uploadingImage ? <CircularProgress size={16} /> : <UploadFileIcon />}
                    variant="outlined"
                    disabled={uploadingImage || !selectedConference}
                  >
                    {uploadingImage ? 'Uploading...' : 'Upload Image'}
                    <input hidden accept={ACCEPTED_IMAGE_FILE_INPUT} type="file" onChange={handleImageFileSelection} />
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
                      <Button color="inherit" size="small" onClick={() => navigate('/events')}>
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
                  <Button variant="text" onClick={() => navigate('/events')} disabled={saving}>
                    Cancel
                  </Button>
                  <Button
                    type="submit"
                    variant="contained"
                    color="primary"
                    disabled={
                      saving ||
                      uploadingImage ||
                      !selectedConference ||
                      !availableDates.length ||
                      !availableLocations.length
                    }
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
