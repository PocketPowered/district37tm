import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import {
  Alert,
  Box,
  Button,
  Container,
  Paper,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import AuthLoadingScreen from './AuthLoadingScreen';
import { supabase } from '../lib/supabase';

interface ProtectedRouteProps {
  children: React.ReactNode;
}

type AccessRequestStatus = 'pending' | 'approved' | 'denied';

interface AccessRequest {
  id: number;
  status: AccessRequestStatus;
  requested_at: string;
  reviewed_at: string | null;
  decision_note: string | null;
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children }) => {
  const { currentUser, loading, isAuthorized } = useAuth();
  const location = useLocation();
  const [requestNote, setRequestNote] = useState('');
  const [requestLoading, setRequestLoading] = useState(false);
  const [requestError, setRequestError] = useState<string | null>(null);
  const [requestSuccess, setRequestSuccess] = useState<string | null>(null);
  const [latestRequest, setLatestRequest] = useState<AccessRequest | null>(null);

  const requestEmail = useMemo(
    () => (currentUser?.email || '').trim().toLowerCase(),
    [currentUser?.email]
  );

  const loadLatestRequest = useCallback(async () => {
    if (!requestEmail) {
      setLatestRequest(null);
      setRequestError(null);
      return;
    }

    setRequestError(null);

    const { data, error } = await supabase
      .from('access_requests')
      .select('id, status, requested_at, reviewed_at, decision_note')
      .eq('email', requestEmail)
      .order('requested_at', { ascending: false })
      .limit(1)
      .maybeSingle();

    if (error) {
      console.error('Error loading latest access request:', error);
      setRequestError('Unable to load your access request status right now.');
      return;
    }

    setLatestRequest((data as AccessRequest | null) || null);
  }, [requestEmail]);

  useEffect(() => {
    if (!currentUser?.email || isAuthorized) {
      return;
    }
    void loadLatestRequest();
  }, [currentUser?.email, isAuthorized, loadLatestRequest]);

  const handleSubmitRequest = async () => {
    if (!requestEmail || latestRequest?.status === 'pending') {
      return;
    }

    setRequestLoading(true);
    setRequestError(null);
    setRequestSuccess(null);

    try {
      const { error } = await supabase.from('access_requests').insert({
        email: requestEmail,
        full_name:
          typeof currentUser?.user_metadata?.full_name === 'string'
            ? currentUser.user_metadata.full_name
            : null,
        request_note: requestNote.trim() || null,
      });

      if (error) {
        throw error;
      }

      setRequestNote('');
      setRequestSuccess('Request submitted. An admin can now approve or deny your access.');
      await loadLatestRequest();
    } catch (error) {
      const errorCode = typeof error === 'object' && error && 'code' in error
        ? String((error as { code?: unknown }).code || '')
        : '';

      if (errorCode === '23505') {
        setRequestError('You already have a pending request.');
      } else {
        setRequestError('Failed to submit request. Please try again.');
      }
      console.error('Error submitting access request:', error);
    } finally {
      setRequestLoading(false);
    }
  };

  const latestRequestStatus = latestRequest?.status;
  const isPending = latestRequestStatus === 'pending';

  const formatTimestamp = (value: string | null): string => {
    if (!value) return 'N/A';
    return new Date(value).toLocaleString();
  };

  if (loading) {
    return <AuthLoadingScreen />;
  }

  if (!currentUser) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (!isAuthorized) {
    return (
      <Container maxWidth="sm">
        <Box
          sx={{
            marginTop: 8,
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
          }}
        >
          <Paper sx={{ p: 3, width: '100%' }}>
            <Stack spacing={2}>
              <Typography variant="h5" component="h1" gutterBottom>
                Access Denied
              </Typography>
              <Typography variant="body1">
                You do not have permission to access this portal yet. Submit an access request for admins to review.
              </Typography>
              <TextField
                fullWidth
                label="Signed-in Email"
                value={requestEmail}
                disabled
              />
              <TextField
                fullWidth
                multiline
                minRows={3}
                label="Optional Note"
                placeholder="Add context for why you need access."
                value={requestNote}
                onChange={(event) => setRequestNote(event.target.value)}
                disabled={requestLoading || isPending}
              />
              {latestRequestStatus && (
                <Alert severity={latestRequestStatus === 'denied' ? 'error' : 'info'}>
                  Latest request status: <strong>{latestRequestStatus}</strong>
                  {' • '}
                  Submitted: {formatTimestamp(latestRequest.requested_at)}
                  {latestRequest.reviewed_at && (
                    <>
                      {' • '}
                      Reviewed: {formatTimestamp(latestRequest.reviewed_at)}
                    </>
                  )}
                  {latestRequest.decision_note && (
                    <>
                      {' • '}
                      Note: {latestRequest.decision_note}
                    </>
                  )}
                </Alert>
              )}
              {requestError && <Alert severity="error">{requestError}</Alert>}
              {requestSuccess && <Alert severity="success">{requestSuccess}</Alert>}
              <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
                <Button
                  variant="contained"
                  onClick={handleSubmitRequest}
                  disabled={!requestEmail || requestLoading || isPending}
                >
                  {requestLoading ? 'Submitting...' : (isPending ? 'Request Pending' : 'Request Access')}
                </Button>
                {latestRequestStatus === 'approved' && (
                  <Button variant="outlined" onClick={() => window.location.reload()}>
                    Refresh Access
                  </Button>
                )}
              </Box>
            </Stack>
          </Paper>
        </Box>
      </Container>
    );
  }

  return <>{children}</>;
};

export default ProtectedRoute;
