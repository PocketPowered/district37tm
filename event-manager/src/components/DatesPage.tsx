import React, { useEffect, useState } from 'react';
import {
  Box,
  Button,
  Typography,
  Paper,
  CircularProgress,
  Alert,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
} from '@mui/material';
import { dateService } from '../services/dateService';
import AddIcon from '@mui/icons-material/Add';
import DeleteIcon from '@mui/icons-material/Delete';
import { DateTimePicker } from '@mui/x-date-pickers/DateTimePicker';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';

const DatesPage: React.FC = () => {
  const [dates, setDates] = useState<number[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [newDate, setNewDate] = useState<Date | null>(null);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [dateToDelete, setDateToDelete] = useState<number | null>(null);

  useEffect(() => {
    loadDates();
  }, []);

  const loadDates = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await dateService.getAvailableDates();
      setDates(data);
    } catch (error) {
      setError('Failed to load dates. Please try again.');
      console.error('Error loading dates:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleAddDate = async () => {
    if (!newDate) return;
    try {
      setLoading(true);
      setError(null);
      const timestamp = newDate.getTime();
      await dateService.addDate(timestamp);
      setNewDate(null);
      await loadDates();
    } catch (error) {
      setError('Failed to add date. Please try again.');
      console.error('Error adding date:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteClick = (timestamp: number) => {
    console.log('Delete clicked for timestamp:', timestamp); // Debug log
    setDateToDelete(timestamp);
    setDeleteDialogOpen(true);
  };

  const handleDeleteConfirm = async () => {
    if (!dateToDelete) return;
    try {
      setLoading(true);
      setError(null);
      await dateService.removeDate(dateToDelete);
      setDeleteDialogOpen(false);
      setDateToDelete(null);
      await loadDates();
    } catch (error) {
      setError('Failed to delete date. Please try again.');
      console.error('Error deleting date:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteCancel = () => {
    setDeleteDialogOpen(false);
    setDateToDelete(null);
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', p: 3 }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <LocalizationProvider dateAdapter={AdapterDateFns}>
      <Box sx={{ p: 2 }}>
        <Typography variant="h4" gutterBottom>
          Date Manager
        </Typography>
        {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
        <Paper sx={{ p: 2, mb: 2 }}>
          <Box sx={{ display: 'flex', gap: 2, alignItems: 'center' }}>
            <DateTimePicker
              label="New Date"
              value={newDate}
              onChange={setNewDate}
              sx={{ flex: 1 }}
            />
            <Button
              variant="contained"
              startIcon={<AddIcon />}
              onClick={handleAddDate}
              disabled={!newDate}
            >
              Add Date
            </Button>
          </Box>
        </Paper>
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
          {dates.map((timestamp) => (
            <Paper key={timestamp} sx={{ p: 2, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <Typography>
                {new Date(timestamp).toLocaleDateString('en-US', {
                  month: 'long',
                  day: 'numeric',
                  year: 'numeric'
                })}
              </Typography>
              <Button
                variant="outlined"
                color="error"
                startIcon={<DeleteIcon />}
                onClick={() => handleDeleteClick(timestamp)}
              >
                Remove
              </Button>
            </Paper>
          ))}
        </Box>

        <Dialog
          open={deleteDialogOpen}
          onClose={handleDeleteCancel}
          aria-labelledby="delete-dialog-title"
          aria-describedby="delete-dialog-description"
        >
          <DialogTitle id="delete-dialog-title">Remove Date</DialogTitle>
          <DialogContent>
            <DialogContentText id="delete-dialog-description">
              Are you sure you want to remove this date? This will also remove all events associated with this date.
            </DialogContentText>
          </DialogContent>
          <DialogActions>
            <Button onClick={handleDeleteCancel}>Cancel</Button>
            <Button onClick={handleDeleteConfirm} color="error" autoFocus>
              Remove
            </Button>
          </DialogActions>
        </Dialog>
      </Box>
    </LocalizationProvider>
  );
};

export default DatesPage; 