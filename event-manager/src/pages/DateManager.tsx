import React, { useEffect, useState } from 'react';
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
  DialogActions
} from '@mui/material';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { dateService } from '../services/dateService';
import { format } from 'date-fns';
import AddIcon from '@mui/icons-material/Add';

const DateManager: React.FC = () => {
  const [dates, setDates] = useState<number[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedDate, setSelectedDate] = useState<Date | null>(null);
  const [openDialog, setOpenDialog] = useState(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [dateToDelete, setDateToDelete] = useState<number | null>(null);

  const fetchDates = async () => {
    try {
      const fetchedDates = await dateService.getDates();
      setDates(fetchedDates);
      setError(null);
    } catch (err) {
      setError('Failed to fetch dates');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDates();
  }, []);

  const handleAddDate = async () => {
    if (!selectedDate) return;
    
    try {
      await dateService.addDate(selectedDate.getTime());
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
      await dateService.removeDate(dateToDelete);
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

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', p: 3 }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <LocalizationProvider dateAdapter={AdapterDateFns}>
      <Container maxWidth="md">
        <Paper sx={{ p: 3 }}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
            <Typography variant="h5">
              Date Manager
            </Typography>
            <Button
              startIcon={<AddIcon />}
              onClick={() => setOpenDialog(true)}
              variant="contained"
              color="primary"
            >
              Add New Date
            </Button>
          </Box>

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
                There are no dates scheduled yet. Click the button above to add your first date.
              </Typography>
            </Box>
          ) : (
            <Stack spacing={2}>
              {dates.map((timestamp) => (
                <Paper key={timestamp} sx={{ p: 2 }}>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <Typography>
                      {format(new Date(timestamp), 'PPP')}
                    </Typography>
                    <Button
                      color="error"
                      onClick={() => handleRemoveClick(timestamp)}
                    >
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
              <Button 
                onClick={handleAddDate} 
                disabled={!selectedDate}
                variant="contained"
              >
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