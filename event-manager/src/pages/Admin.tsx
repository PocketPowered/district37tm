import React, { useCallback, useEffect, useState } from 'react';
import { 
  Alert,
  Box, 
  Typography, 
  Container, 
  TextField, 
  Button, 
  List, 
  ListItem, 
  ListItemText, 
  IconButton,
  Paper
} from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import CheckIcon from '@mui/icons-material/Check';
import CloseIcon from '@mui/icons-material/Close';
import { supabase } from '../lib/supabase';

interface AuthorizedUser {
  id: string;
  email: string;
}

interface PendingAccessRequest {
  id: number;
  email: string;
  full_name: string | null;
  request_note: string | null;
  requested_at: string;
}

const Admin: React.FC = () => {
  const [email, setEmail] = useState('');
  const [authorizedUsers, setAuthorizedUsers] = useState<AuthorizedUser[]>([]);
  const [loading, setLoading] = useState(false);
  const [requestActionId, setRequestActionId] = useState<number | null>(null);
  const [pendingRequests, setPendingRequests] = useState<PendingAccessRequest[]>([]);
  const [error, setError] = useState<string | null>(null);

  const fetchAuthorizedUsers = useCallback(async () => {
    const { data, error } = await supabase
      .from('authorized_users')
      .select('email')
      .order('email', { ascending: true });
    if (error) throw error;

    const users = (data || []).map((row) => ({
      id: row.email,
      email: row.email,
    }));
    setAuthorizedUsers(users);
  }, []);

  const fetchPendingRequests = useCallback(async () => {
    const { data, error } = await supabase
      .from('access_requests')
      .select('id, email, full_name, request_note, requested_at')
      .eq('status', 'pending')
      .order('requested_at', { ascending: true });

    if (error) throw error;
    setPendingRequests((data as PendingAccessRequest[]) || []);
  }, []);

  const refreshPendingRequestsSafely = useCallback(async () => {
    try {
      await fetchPendingRequests();
    } catch (error) {
      console.warn('Pending requests are unavailable:', error);
      setPendingRequests([]);
    }
  }, [fetchPendingRequests]);

  useEffect(() => {
    const initialize = async () => {
      try {
        setError(null);
        await fetchAuthorizedUsers();
      } catch (error) {
        console.error('Error loading admin data:', error);
        setError('Failed to load authorized users. Please refresh and try again.');
      }

      await refreshPendingRequestsSafely();
    };

    void initialize();
  }, [fetchAuthorizedUsers, refreshPendingRequestsSafely]);

  const handleAddUser = async () => {
    const normalizedEmail = email.trim().toLowerCase();
    if (!normalizedEmail) return;

    try {
      setLoading(true);
      setError(null);
      const { error } = await supabase.from('authorized_users').upsert({
        email: normalizedEmail,
      });
      if (error) throw error;
      setEmail('');
      await fetchAuthorizedUsers();
      await refreshPendingRequestsSafely();
    } catch (error) {
      console.error('Error adding authorized user:', error);
      setError('Failed to add authorized user.');
    } finally {
      setLoading(false);
    }
  };

  const handleRemoveUser = async (email: string) => {
    try {
      setLoading(true);
      setError(null);
      const { error } = await supabase.from('authorized_users').delete().eq('email', email.toLowerCase());
      if (error) throw error;
      await fetchAuthorizedUsers();
    } catch (error) {
      console.error('Error removing authorized user:', error);
      setError('Failed to remove authorized user.');
    } finally {
      setLoading(false);
    }
  };

  const handleReviewRequest = async (requestId: number, approve: boolean) => {
    try {
      setRequestActionId(requestId);
      setError(null);

      const { error } = await supabase.rpc('review_access_request', {
        p_request_id: requestId,
        p_approve: approve,
        p_decision_note: null,
      });
      if (error) throw error;

      await fetchAuthorizedUsers();
      await refreshPendingRequestsSafely();
    } catch (error) {
      console.error('Error reviewing access request:', error);
      setError(`Failed to ${approve ? 'approve' : 'deny'} request.`);
    } finally {
      setRequestActionId(null);
    }
  };

  return (
    <Container maxWidth="md">
      <Box sx={{ mt: 4 }}>
        {error && (
          <Alert severity="error" sx={{ mb: 3 }}>
            {error}
          </Alert>
        )}
        
        <Paper sx={{ p: 3, mb: 3 }}>
          <Typography variant="h6" gutterBottom>
            Add New User
          </Typography>
          <Box sx={{ display: 'flex', gap: 2, mb: 3 }}>
            <TextField
              fullWidth
              label="Email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              disabled={loading}
            />
            <Button
              variant="contained"
              onClick={handleAddUser}
              disabled={loading || !email.trim()}
            >
              Add User
            </Button>
          </Box>
        </Paper>

        {pendingRequests.length > 0 && (
          <Paper sx={{ p: 3, mb: 3 }}>
            <Typography variant="h6" gutterBottom>
              Pending Requests
            </Typography>
            <List>
              {pendingRequests.map((request) => (
                <ListItem
                  key={request.id}
                  alignItems="flex-start"
                  secondaryAction={
                    <Box sx={{ display: 'flex', gap: 1 }}>
                      <IconButton
                        edge="end"
                        aria-label="approve request"
                        color="success"
                        onClick={() => handleReviewRequest(request.id, true)}
                        disabled={loading || requestActionId !== null}
                      >
                        <CheckIcon />
                      </IconButton>
                      <IconButton
                        edge="end"
                        aria-label="deny request"
                        color="error"
                        onClick={() => handleReviewRequest(request.id, false)}
                        disabled={loading || requestActionId !== null}
                      >
                        <CloseIcon />
                      </IconButton>
                    </Box>
                  }
                >
                  <ListItemText
                    primary={request.full_name ? `${request.full_name} (${request.email})` : request.email}
                    secondary={
                      <>
                        <Typography component="span" variant="body2" sx={{ display: 'block' }}>
                          Requested: {new Date(request.requested_at).toLocaleString()}
                        </Typography>
                        {request.request_note && (
                          <Typography component="span" variant="body2" color="text.secondary" sx={{ display: 'block' }}>
                            Note: {request.request_note}
                          </Typography>
                        )}
                      </>
                    }
                  />
                </ListItem>
              ))}
            </List>
          </Paper>
        )}

        <Paper sx={{ p: 3 }}>
          <Typography variant="h6" gutterBottom>
            Authorized Users
          </Typography>
          <List>
            {authorizedUsers.map((user) => (
              <ListItem
                key={user.id}
                secondaryAction={
                  <IconButton
                    edge="end"
                    aria-label="delete"
                    onClick={() => handleRemoveUser(user.email)}
                    disabled={loading}
                  >
                    <DeleteIcon />
                  </IconButton>
                }
              >
                <ListItemText primary={user.email} />
              </ListItem>
            ))}
          </List>
        </Paper>
      </Box>
    </Container>
  );
};

export default Admin; 
