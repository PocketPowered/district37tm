import React from 'react';
import { HashRouter as Router, Navigate, Outlet, Route, Routes } from 'react-router-dom';
import { Box, CircularProgress, CssBaseline, ThemeProvider } from '@mui/material';
import { lightTheme } from './theme';
import EventList from './components/EventList';
import EventForm from './components/EventForm';
import NotificationForm from './components/NotificationForm';
import { AuthProvider } from './contexts/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import Login from './pages/Login';
import Admin from './pages/Admin';
import ResourcesManager from './pages/ResourcesManager';
import LocationsManager from './pages/LocationsManager';
import ErrorBoundary from './components/ErrorBoundary';
import AdminShell from './components/AdminShell';
import ConferencesManager from './pages/ConferencesManager';
import { ConferenceProvider, useConference } from './contexts/ConferenceContext';

const RequireConferenceSelection: React.FC = () => {
  const { selectedConferenceId, loading } = useConference();

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', p: 3 }}>
        <CircularProgress />
      </Box>
    );
  }

  if (!selectedConferenceId) {
    return <Navigate to="/conferences" replace />;
  }

  return <Outlet />;
};

const AppContent: React.FC = () => {
  return (
    <Routes>
      <Route path="login" element={<Login />} />
      <Route
        element={
          <ProtectedRoute>
            <ConferenceProvider>
              <AdminShell />
            </ConferenceProvider>
          </ProtectedRoute>
        }
      >
        <Route index element={<Navigate to="/conferences" replace />} />
        <Route path="conferences" element={<ConferencesManager />} />
        <Route path="admin" element={<Admin />} />
        <Route element={<RequireConferenceSelection />}>
          <Route path="events">
            <Route index element={<EventList />} />
            <Route path="new" element={<EventForm />} />
            <Route path=":id/edit" element={<EventForm />} />
          </Route>
          <Route path="notifications" element={<NotificationForm />} />
          <Route path="resources" element={<ResourcesManager />} />
          <Route path="locations" element={<LocationsManager />} />
        </Route>
      </Route>
      <Route path="*" element={<Navigate to="/conferences" replace />} />
    </Routes>
  );
};

const App = () => {
  return (
    <ThemeProvider theme={lightTheme}>
      <CssBaseline />
      <Router>
        <ErrorBoundary>
          <AuthProvider>
            <AppContent />
          </AuthProvider>
        </ErrorBoundary>
      </Router>
    </ThemeProvider>
  );
};

export default App;
