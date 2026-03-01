import React, { useMemo, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  Chip,
  CircularProgress,
  Container,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Paper,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import EditIcon from '@mui/icons-material/Edit';
import { useConference } from '../contexts/ConferenceContext';
import { conferenceService } from '../services/conferenceService';
import { Conference, ConferenceUpsertInput } from '../types/Conference';

interface ConferenceFormState {
  name: string;
  slug: string;
  scheduleTitle: string;
  startDate: string;
  endDate: string;
}

const createEmptyConferenceForm = (): ConferenceFormState => ({
  name: '',
  slug: '',
  scheduleTitle: '',
  startDate: '',
  endDate: '',
});

const conferenceToForm = (conference: Conference): ConferenceFormState => ({
  name: conference.name,
  slug: conference.slug,
  scheduleTitle: conference.scheduleTitle,
  startDate: conference.startDate || '',
  endDate: conference.endDate || '',
});

const formatDateLabel = (dateIso: string | null): string => {
  if (!dateIso) {
    return 'Not set';
  }
  const parsed = new Date(`${dateIso}T00:00:00.000Z`);
  return parsed.toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    timeZone: 'UTC',
  });
};

const ConferencesManager: React.FC = () => {
  const {
    conferences,
    selectedConferenceId,
    loading,
    error,
    createConference,
    updateConference,
    setActiveConference,
    setSelectedConferenceId,
  } = useConference();

  const [dialogOpen, setDialogOpen] = useState(false);
  const [saving, setSaving] = useState(false);
  const [activatingConferenceId, setActivatingConferenceId] = useState<number | null>(null);
  const [editingConference, setEditingConference] = useState<Conference | null>(null);
  const [formState, setFormState] = useState<ConferenceFormState>(createEmptyConferenceForm());
  const [formError, setFormError] = useState<string | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const [slugManuallyEdited, setSlugManuallyEdited] = useState(false);

  const hasConferences = conferences.length > 0;
  const activeConference = useMemo(
    () => conferences.find((conference) => conference.isActive) || null,
    [conferences]
  );

  const openCreateDialog = () => {
    setEditingConference(null);
    setFormState(createEmptyConferenceForm());
    setFormError(null);
    setSlugManuallyEdited(false);
    setDialogOpen(true);
  };

  const openEditDialog = (conference: Conference) => {
    setEditingConference(conference);
    setFormState(conferenceToForm(conference));
    setFormError(null);
    setSlugManuallyEdited(true);
    setDialogOpen(true);
  };

  const closeDialog = () => {
    if (saving) return;
    setDialogOpen(false);
    setEditingConference(null);
    setFormError(null);
  };

  const handleNameChange = (name: string) => {
    setFormState((prev) => ({
      ...prev,
      name,
      slug: slugManuallyEdited ? prev.slug : conferenceService.sanitizeSlug(name),
    }));
  };

  const handleSubmit = async () => {
    const name = formState.name.trim();
    const slug = conferenceService.sanitizeSlug(formState.slug || formState.name);
    const scheduleTitle = formState.scheduleTitle.trim() || name;
    const startDate = formState.startDate || null;
    const endDate = formState.endDate || null;

    if (!name) {
      setFormError('Conference name is required.');
      return;
    }

    if (!slug) {
      setFormError('Conference slug is required.');
      return;
    }

    if (startDate && endDate && startDate > endDate) {
      setFormError('End date must be on or after start date.');
      return;
    }

    const payload: ConferenceUpsertInput = {
      name,
      slug,
      scheduleTitle,
      startDate,
      endDate,
    };

    try {
      setSaving(true);
      setFormError(null);
      if (editingConference) {
        await updateConference(editingConference.id, payload);
      } else {
        await createConference(payload);
      }
      setDialogOpen(false);
      setEditingConference(null);
      setSlugManuallyEdited(false);
    } catch (submitError) {
      console.error('Error saving conference:', submitError);
      const fallbackMessage = editingConference
        ? 'Failed to update conference.'
        : 'Failed to create conference.';
      setFormError(
        submitError instanceof Error && submitError.message.trim()
          ? submitError.message
          : fallbackMessage
      );
    } finally {
      setSaving(false);
    }
  };

  const handleSetActive = async (conferenceId: number) => {
    try {
      setActionError(null);
      setActivatingConferenceId(conferenceId);
      await setActiveConference(conferenceId);
    } catch (activateError) {
      console.error('Error setting active conference:', activateError);
      setActionError('Failed to set active conference. Please try again.');
    } finally {
      setActivatingConferenceId(null);
    }
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', p: 3 }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Container maxWidth="lg">
      <Paper sx={{ p: 3 }}>
        <Stack
          direction={{ xs: 'column', sm: 'row' }}
          justifyContent="flex-end"
          alignItems={{ xs: 'stretch', sm: 'center' }}
          spacing={2}
          sx={{ mb: 3 }}
        >
          <Button variant="contained" startIcon={<AddIcon />} onClick={openCreateDialog}>
            Add Conference
          </Button>
        </Stack>

        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}
        {actionError && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {actionError}
          </Alert>
        )}
        {activeConference && (
          <Alert severity="success" sx={{ mb: 2 }}>
            Active conference: <strong>{activeConference.name}</strong>
          </Alert>
        )}

        {!hasConferences && (
          <Alert severity="info">No conferences found yet. Create your first conference.</Alert>
        )}

        <Stack spacing={2}>
          {conferences.map((conference) => {
            const isSelected = selectedConferenceId === conference.id;
            const isActivating = activatingConferenceId === conference.id;
            return (
              <Paper key={conference.id} variant="outlined" sx={{ p: 2.5 }}>
                <Stack
                  direction={{ xs: 'column', md: 'row' }}
                  justifyContent="space-between"
                  spacing={2}
                  alignItems={{ xs: 'flex-start', md: 'center' }}
                >
                  <Box sx={{ textAlign: 'left' }}>
                    <Stack direction="row" spacing={1} alignItems="center" flexWrap="wrap" sx={{ mb: 1 }}>
                      <Typography variant="h6">{conference.name}</Typography>
                      {conference.isActive && <Chip size="small" color="success" label="Active" />}
                      {isSelected && <Chip size="small" color="primary" label="Selected" />}
                    </Stack>
                    <Typography variant="body2" color="text.secondary">
                      Slug: {conference.slug}
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      Schedule title: {conference.scheduleTitle}
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      Date range: {formatDateLabel(conference.startDate)} - {formatDateLabel(conference.endDate)}
                    </Typography>
                  </Box>

                  <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1.25}>
                    <Button
                      variant={isSelected ? 'contained' : 'outlined'}
                      onClick={() => setSelectedConferenceId(conference.id)}
                      disabled={isSelected}
                    >
                      {isSelected ? 'Selected' : 'Select'}
                    </Button>
                    <Button
                      variant={conference.isActive ? 'contained' : 'outlined'}
                      color="success"
                      startIcon={<CheckCircleIcon />}
                      disabled={conference.isActive || isActivating}
                      onClick={() => void handleSetActive(conference.id)}
                    >
                      {conference.isActive ? 'Active' : isActivating ? 'Setting...' : 'Set Active'}
                    </Button>
                    <Button startIcon={<EditIcon />} onClick={() => openEditDialog(conference)}>
                      Edit
                    </Button>
                  </Stack>
                </Stack>
              </Paper>
            );
          })}
        </Stack>

      </Paper>

      <Dialog open={dialogOpen} onClose={closeDialog} fullWidth maxWidth="sm">
        <DialogTitle>{editingConference ? 'Edit Conference' : 'Create Conference'}</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            {formError && <Alert severity="error">{formError}</Alert>}
            <TextField
              label="Conference Name"
              value={formState.name}
              onChange={(event) => handleNameChange(event.target.value)}
              required
              fullWidth
            />
            <TextField
              label="Slug"
              value={formState.slug}
              onChange={(event) => {
                setSlugManuallyEdited(true);
                setFormState((prev) => ({
                  ...prev,
                  slug: conferenceService.sanitizeSlug(event.target.value),
                }));
              }}
              helperText="Used as a stable identifier (letters, numbers, hyphens)."
              required
              fullWidth
            />
            <TextField
              label="Schedule Title"
              value={formState.scheduleTitle}
              onChange={(event) =>
                setFormState((prev) => ({
                  ...prev,
                  scheduleTitle: event.target.value,
                }))
              }
              helperText='Shown in app header. Defaults to conference name if left empty.'
              fullWidth
            />
            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
              <TextField
                label="Start Date"
                type="date"
                value={formState.startDate}
                onChange={(event) =>
                  setFormState((prev) => ({
                    ...prev,
                    startDate: event.target.value,
                  }))
                }
                InputLabelProps={{ shrink: true }}
                fullWidth
              />
              <TextField
                label="End Date"
                type="date"
                value={formState.endDate}
                onChange={(event) =>
                  setFormState((prev) => ({
                    ...prev,
                    endDate: event.target.value,
                  }))
                }
                InputLabelProps={{ shrink: true }}
                fullWidth
              />
            </Stack>
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={closeDialog} disabled={saving}>
            Cancel
          </Button>
          <Button onClick={() => void handleSubmit()} variant="contained" disabled={saving}>
            {saving ? 'Saving...' : editingConference ? 'Save Changes' : 'Create Conference'}
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default ConferencesManager;
