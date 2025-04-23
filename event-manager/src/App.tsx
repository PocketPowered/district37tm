import React from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import { ThemeProvider, CssBaseline, AppBar, Toolbar, Typography, Container, Button, Box } from '@mui/material';
import { lightTheme, darkTheme } from './theme';
import EventList from './components/EventList';
import EventForm from './components/EventForm';
import NotificationForm from './components/NotificationForm';
import DateManager from './pages/DateManager';
import EnvironmentToggle from './components/EnvironmentToggle';
import { AuthProvider, useAuth } from './contexts/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import Login from './pages/Login';
import Admin from './pages/Admin';
import './App.css';

const AppContent: React.FC = () => {
  const { currentUser, logout, isAuthorized } = useAuth();
  const isDarkMode = false;

  return (
    <ThemeProvider theme={isDarkMode ? darkTheme : lightTheme}>
      <CssBaseline />
      <div className="App">
        <AppBar position="static">
          <Toolbar>
            <Typography variant="h6" component="div">
              District 37 Toastmasters Admin
            </Typography>
            <Box sx={{ ml: 'auto', display: 'flex', gap: 2 }}>
              {currentUser && (
                <>
                  {isAuthorized && (
                    <>
                      <Button color="inherit" component={Link} to="/">
                        Event Manager
                      </Button>
                      <Button color="inherit" component={Link} to="/dates">
                        Date Manager
                      </Button>
                      <Button color="inherit" component={Link} to="/notifications">
                        Notifications
                      </Button>
                      <Button color="inherit" component={Link} to="/admin">
                        Authorized Users
                      </Button>
                    </>
                  )}
                  <Button color="inherit" onClick={logout}>
                    Logout
                  </Button>
                </>
              )}
            </Box>
          </Toolbar>
        </AppBar>
        <Container maxWidth="lg" sx={{ mt: 4 }}>
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route
              path="/"
              element={
                <ProtectedRoute>
                  <EventList />
                </ProtectedRoute>
              }
            />
            <Route
              path="/events/new"
              element={
                <ProtectedRoute>
                  <EventForm />
                </ProtectedRoute>
              }
            />
            <Route
              path="/events/:id/edit"
              element={
                <ProtectedRoute>
                  <EventForm />
                </ProtectedRoute>
              }
            />
            <Route
              path="/notifications"
              element={
                <ProtectedRoute>
                  <NotificationForm />
                </ProtectedRoute>
              }
            />
            <Route
              path="/dates"
              element={
                <ProtectedRoute>
                  <DateManager />
                </ProtectedRoute>
              }
            />
            <Route
              path="/admin"
              element={
                <ProtectedRoute>
                  <Admin />
                </ProtectedRoute>
              }
            />
          </Routes>
        </Container>
        <EnvironmentToggle />
      </div>
    </ThemeProvider>
  );
};

const App: React.FC = () => {
  return (
    <Router>
      <AuthProvider>
        <AppContent />
      </AuthProvider>
    </Router>
  );
};

export default App;
