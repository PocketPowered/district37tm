import React, { useCallback, useEffect, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  Chip,
  CircularProgress,
  FormControl,
  FormControlLabel,
  FormHelperText,
  InputLabel,
  MenuItem,
  Paper,
  Select,
  SelectChangeEvent,
  Stack,
  Switch,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TablePagination,
  TableRow,
  TextField,
  Typography,
} from '@mui/material';
import { notificationService, NotificationHistoryItem } from '../services/notificationService';
import {
  NOTIFICATION_TARGET_HELPER_TEXT,
  NOTIFICATION_TARGET_OPTIONS,
  NotificationTarget,
  resolveNotificationTopic,
} from '../constants/notificationTopics';

interface NotificationFormProps {
  onSuccess?: () => void;
}

const HISTORY_ROWS_PER_PAGE_OPTIONS = [10, 25, 50];

const NotificationForm: React.FC<NotificationFormProps> = ({ onSuccess }) => {
  const [title, setTitle] = useState('');
  const [body, setBody] = useState('');
  const [target, setTarget] = useState<NotificationTarget>('GENERAL');
  const [version, setVersion] = useState('');
  const [customTopic, setCustomTopic] = useState('');
  const [scheduleEnabled, setScheduleEnabled] = useState(false);
  const [scheduledAtLocal, setScheduledAtLocal] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const [historyItems, setHistoryItems] = useState<NotificationHistoryItem[]>([]);
  const [historyTotalCount, setHistoryTotalCount] = useState(0);
  const [historyLoading, setHistoryLoading] = useState(false);
  const [historyError, setHistoryError] = useState<string | null>(null);
  const [historyPage, setHistoryPage] = useState(0);
  const [historyRowsPerPage, setHistoryRowsPerPage] = useState(10);

  const resolveTopic = () =>
    resolveNotificationTopic({
      target,
      version,
      customTopic,
    });

  const loadHistory = useCallback(async (page: number, rowsPerPage: number) => {
    setHistoryLoading(true);
    setHistoryError(null);

    try {
      const history = await notificationService.getNotificationHistory(page, rowsPerPage);
      setHistoryItems(history.items);
      setHistoryTotalCount(history.totalCount);
    } catch (loadError) {
      console.error('Error loading notification history:', loadError);
      setHistoryError('Failed to load notification history.');
    } finally {
      setHistoryLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadHistory(historyPage, historyRowsPerPage);
  }, [historyPage, historyRowsPerPage, loadHistory]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setSuccess(null);

    try {
      const topic = resolveTopic();
      if (!topic) {
        throw new Error(
          target === 'APP_VERSION'
            ? 'Enter a version to target (for example: 8.0).'
            : 'Enter a custom topic.',
        );
      }

      let scheduledForIso: string | undefined;
      if (scheduleEnabled) {
        if (!scheduledAtLocal.trim()) {
          throw new Error('Choose when to send this scheduled notification.');
        }

        const parsedScheduledAt = new Date(scheduledAtLocal);
        if (Number.isNaN(parsedScheduledAt.getTime())) {
          throw new Error('Scheduled time is invalid.');
        }

        scheduledForIso = parsedScheduledAt.toISOString();
      }

      const result = await notificationService.sendNotification(title, body, topic, scheduledForIso);

      if (result.scheduled) {
        const scheduledLabel = result.scheduledFor
          ? new Date(result.scheduledFor).toLocaleString()
          : new Date(scheduledForIso || Date.now()).toLocaleString();
        setSuccess(`Notification scheduled for ${scheduledLabel} to topic "${topic}".`);
      } else {
        setSuccess(`Notification sent successfully to topic "${topic}".`);
      }

      setTitle('');
      setBody('');
      setHistoryPage(0);
      await loadHistory(0, historyRowsPerPage);
      if (onSuccess) onSuccess();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to send notification. Please try again.');
      console.error('Error sending notification:', err);
    } finally {
      setLoading(false);
    }
  };

  const formatTimestamp = (value: string | null) => {
    if (!value) return '—';
    return new Date(value).toLocaleString();
  };

  const formatSender = (createdBy: string) => {
    return createdBy === 'system:event-reminder' ? 'Auto reminder' : createdBy;
  };

  const getStatusChipColor = (status: string): 'default' | 'success' | 'warning' => {
    if (status === 'sent') return 'success';
    if (status === 'queued') return 'warning';
    return 'default';
  };

  return (
    <Box sx={{ maxWidth: 1200, mx: 'auto', mt: 4 }}>
      <Paper elevation={3} sx={{ p: 3 }}>
        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}

        {success && (
          <Alert severity="success" sx={{ mb: 2 }}>
            {success}
          </Alert>
        )}

        <form onSubmit={handleSubmit}>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
            <TextField fullWidth label="Title" value={title} onChange={(e) => setTitle(e.target.value)} required />

            <TextField
              fullWidth
              label="Message"
              value={body}
              onChange={(e) => setBody(e.target.value)}
              multiline
              rows={4}
              required
            />

            <FormControl fullWidth>
              <InputLabel id="notification-target-label">Target</InputLabel>
              <Select
                labelId="notification-target-label"
                label="Target"
                value={target}
                onChange={(e: SelectChangeEvent<NotificationTarget>) => setTarget(e.target.value as NotificationTarget)}
              >
                {NOTIFICATION_TARGET_OPTIONS.map((option) => (
                  <MenuItem key={option.value} value={option.value}>
                    {option.label}
                  </MenuItem>
                ))}
              </Select>
              <FormHelperText>{NOTIFICATION_TARGET_HELPER_TEXT}</FormHelperText>
            </FormControl>

            {target === 'APP_VERSION' && (
              <TextField
                fullWidth
                label="App Version"
                value={version}
                onChange={(e) => setVersion(e.target.value)}
                placeholder="8.0"
                required
                helperText={`Will send to topic: ${resolveTopic() || 'APP_VERSION_<version>'}`}
              />
            )}

            {target === 'CUSTOM' && (
              <TextField
                fullWidth
                label="Custom Topic"
                value={customTopic}
                onChange={(e) => setCustomTopic(e.target.value)}
                placeholder="APP_VERSION_8_0"
                required
                helperText="Use this for advanced targeting."
              />
            )}

            <FormControlLabel
              control={
                <Switch
                  checked={scheduleEnabled}
                  onChange={(e) => {
                    setScheduleEnabled(e.target.checked);
                    if (!e.target.checked) {
                      setScheduledAtLocal('');
                    }
                  }}
                />
              }
              label="Schedule"
            />

            {scheduleEnabled && (
              <TextField
                type="datetime-local"
                label="Send At"
                value={scheduledAtLocal}
                onChange={(e) => setScheduledAtLocal(e.target.value)}
                InputLabelProps={{ shrink: true }}
                fullWidth
                required
                helperText="Scheduled notifications are queued and sent automatically when due."
              />
            )}

            <Button type="submit" variant="contained" color="primary" fullWidth disabled={loading}>
              {loading ? 'Saving...' : scheduleEnabled ? 'Schedule Notification' : 'Send Notification'}
            </Button>
          </Box>
        </form>
      </Paper>

      <Paper elevation={3} sx={{ p: 3, mt: 3 }}>
        <Stack direction={{ xs: 'column', sm: 'row' }} justifyContent="space-between" spacing={1} sx={{ mb: 2 }}>
          <Typography variant="h6">Notification History</Typography>
          <Button
            variant="outlined"
            onClick={() => void loadHistory(historyPage, historyRowsPerPage)}
            disabled={historyLoading}
          >
            Refresh
          </Button>
        </Stack>

        {historyError && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {historyError}
          </Alert>
        )}

        {historyLoading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
            <CircularProgress size={28} />
          </Box>
        ) : historyItems.length === 0 ? (
          <Typography variant="body2" color="text.secondary">
            No queued or sent notifications yet.
          </Typography>
        ) : (
          <>
            <TableContainer>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell sx={{ width: 180 }}>Created</TableCell>
                    <TableCell sx={{ width: 180 }}>Scheduled For</TableCell>
                    <TableCell sx={{ width: 180 }}>Sent At</TableCell>
                    <TableCell sx={{ width: 210 }}>Title</TableCell>
                    <TableCell>Message</TableCell>
                    <TableCell sx={{ width: 180 }}>Topic</TableCell>
                    <TableCell sx={{ width: 110 }}>Status</TableCell>
                    <TableCell sx={{ width: 170 }}>Sender</TableCell>
                    <TableCell sx={{ width: 130 }}>Source</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {historyItems.map((item) => (
                    <TableRow key={item.id} hover>
                      <TableCell>{formatTimestamp(item.createdAt)}</TableCell>
                      <TableCell>{formatTimestamp(item.scheduledFor)}</TableCell>
                      <TableCell>{formatTimestamp(item.sentAt)}</TableCell>
                      <TableCell sx={{ fontWeight: 500 }}>{item.title}</TableCell>
                      <TableCell sx={{ whiteSpace: 'normal', wordBreak: 'break-word' }}>{item.body}</TableCell>
                      <TableCell>
                        <Chip size="small" variant="outlined" label={item.topic} />
                      </TableCell>
                      <TableCell>
                        <Chip
                          size="small"
                          color={getStatusChipColor(item.status)}
                          variant={item.status === 'sent' ? 'filled' : 'outlined'}
                          label={item.status}
                        />
                      </TableCell>
                      <TableCell>{formatSender(item.createdBy)}</TableCell>
                      <TableCell>{item.source || 'manual'}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
            <TablePagination
              component="div"
              count={historyTotalCount}
              page={historyPage}
              onPageChange={(_event, newPage) => setHistoryPage(newPage)}
              rowsPerPage={historyRowsPerPage}
              onRowsPerPageChange={(e) => {
                const nextRows = Number.parseInt(e.target.value, 10);
                setHistoryRowsPerPage(Number.isFinite(nextRows) ? nextRows : 10);
                setHistoryPage(0);
              }}
              rowsPerPageOptions={HISTORY_ROWS_PER_PAGE_OPTIONS}
            />
          </>
        )}
      </Paper>
    </Box>
  );
};

export default NotificationForm;
