import React, { useCallback, useEffect, useState } from 'react';
import {
  Box,
  Button,
  Container,
  Typography,
  Paper,
  CircularProgress,
  Alert,
  Stack,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
} from '@mui/material';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import AddIcon from '@mui/icons-material/Add';
import { dateService } from '../services/dateService';
import { createUtcDateKeyFromDate, formatDateKey } from '../utils/dateKey';
import { useConference } from '../contexts/ConferenceContext';

const DateManager: React.FC = () => {
  const {
    selectedConference,
    loading: conferenceLoading,
    refreshConferences,
  } = useConference();
  const [dates, setDates] = useState<number[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedDate, setSelectedDate] = useState<Date | null>(null);
  const [openDialog, setOpenDialog] = useState(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [dateToDelete, setDateToDelete] = useState<number | null>(null);

  const fetchDates = useCallback(async () => {
    if (!selectedConference) {
      setDates([]);
      setError(null);
      setLoading(false);
      return;
    }

    try {
      const fetchedDates = await dateService.getDates(selectedConference);
      setDates(fetchedDates);
      setError(null);
    } catch (err) {
      setError('Failed to fetch dates');
      console.error(err);
    } finally {
      setLoading(false);
    }
  }, [selectedConference]);

  useEffect(() => {
    setLoading(true);
    void fetchDates();
  }, [fetchDates]);

  const handleAddDate = async () => {
    if (!selectedDate) return;
    if (!selectedConference) {
      setError('Select a conference before adding dates.');
      return;
    }

    try {
      await dateService.addDate(createUtcDateKeyFromDate(selectedDate), selectedConference);
      await refreshConferences();
      await fetchDates();
      setSelectedDate(null);
      setOpenDialog(false);
    } catch (err) {
      setError('Failed to add date');
      console.error(err);
    }
  };

  const handleRemoveClick = (timestamp: number) => {
    setDateToDelete(timestamp);
    setDeleteDialogOpen(true);
  };

  const handleRemoveConfirm = async () => {
    if (!dateToDelete) return;
    try {
      await dateService.removeDate(dateToDelete, selectedConference);
      await refreshConferences();
      await fetchDates();
      setDeleteDialogOpen(false);
      setDateToDelete(null);
    } catch (err) {
      setError('Failed to remove date');
      console.error(err);
    }
  };

  const handleRemoveCancel = () => {
    setDeleteDialogOpen(false);
    setDateToDelete(null);
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
      <Container maxWidth="md">
        <Paper sx={{ p: 3 }}>
          <Alert severity="warning">
            No conference selected. Choose a conference before managing dates.
          </Alert>
        </Paper>
      </Container>
    );
  }

  return (
    <LocalizationProvider dateAdapter={AdapterDateFns}>
      <Container maxWidth="md">
        <Paper sx={{ p: 3 }}>
          <Stack
            direction={{ xs: 'column', sm: 'row' }}
            justifyContent="flex-end"
            alignItems={{ xs: 'stretch', sm: 'center' }}
            spacing={2}
            sx={{ mb: 3 }}
          >
            <Button
              startIcon={<AddIcon />}
              onClick={() => setOpenDialog(true)}
              variant="contained"
              color="primary"
            >
              Add New Date
            </Button>
          </Stack>

          {error && (
            <Alert severity="error" sx={{ mb: 3 }}>
              {error}
            </Alert>
          )}

          {dates.length === 0 ? (
            <Box sx={{ textAlign: 'center', py: 4 }}>
              <Typography variant="h6" gutterBottom>
                No Dates Found
              </Typography>
              <Typography color="text.secondary" paragraph>
                There are no dates in this selected conference yet. Add one to begin scheduling events.
              </Typography>
            </Box>
          ) : (
            <Stack spacing={2}>
              {dates.map((timestamp) => (
                <Paper key={timestamp} sx={{ p: 2 }}>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <Typography>
                      {formatDateKey(timestamp, {
                        weekday: 'long',
                        month: 'long',
                        day: 'numeric',
                        year: 'numeric',
                      })}
                    </Typography>
                    <Button color="error" onClick={() => handleRemoveClick(timestamp)}>
                      Remove
                    </Button>
                  </Box>
                </Paper>
              ))}
            </Stack>
          )}

          <Dialog open={openDialog} onClose={() => setOpenDialog(false)}>
            <DialogTitle>Select a Date</DialogTitle>
            <DialogContent>
              <Box sx={{ mt: 2 }}>
                <DatePicker
                  label="Date"
                  value={selectedDate}
                  onChange={(newValue) => setSelectedDate(newValue)}
                  sx={{ width: '100%' }}
                />
              </Box>
            </DialogContent>
            <DialogActions>
              <Button onClick={() => setOpenDialog(false)}>Cancel</Button>
              <Button onClick={handleAddDate} disabled={!selectedDate} variant="contained">
                Add Date
              </Button>
            </DialogActions>
          </Dialog>

          <Dialog
            open={deleteDialogOpen}
            onClose={handleRemoveCancel}
            aria-labelledby="remove-dialog-title"
            aria-describedby="remove-dialog-description"
          >
            <DialogTitle id="remove-dialog-title">Remove Date</DialogTitle>
            <DialogContent>
              <DialogContentText id="remove-dialog-description">
                Are you sure you want to remove this date? This will also remove all events associated with this date.
              </DialogContentText>
            </DialogContent>
            <DialogActions>
              <Button onClick={handleRemoveCancel}>Cancel</Button>
              <Button onClick={handleRemoveConfirm} color="error" autoFocus>
                Remove
              </Button>
            </DialogActions>
          </Dialog>
        </Paper>
      </Container>
    </LocalizationProvider>
  );
};

export default DateManager;
