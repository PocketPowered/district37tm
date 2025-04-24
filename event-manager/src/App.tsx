import React, { useState } from 'react';
import { HashRouter as Router, Routes, Route, Link } from 'react-router-dom';
import { 
  ThemeProvider, 
  CssBaseline, 
  AppBar, 
  Toolbar, 
  Typography, 
  Container, 
  Button, 
  Box,
  IconButton,
  Drawer,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  useTheme,
  useMediaQuery
} from '@mui/material';
import { lightTheme, darkTheme } from './theme';
import EventList from './components/EventList';
import EventForm from './components/EventForm';
import NotificationForm from './components/NotificationForm';
import DateManager from './pages/DateManager';
import ReferencesManager from './pages/ReferencesManager';
import EnvironmentToggle from './components/EnvironmentToggle';
import { AuthProvider, useAuth } from './contexts/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import Login from './pages/Login';
import Admin from './pages/Admin';
import MenuIcon from '@mui/icons-material/Menu';
import EventIcon from '@mui/icons-material/Event';
import CalendarMonthIcon from '@mui/icons-material/CalendarMonth';
import NotificationsIcon from '@mui/icons-material/Notifications';
import AdminPanelSettingsIcon from '@mui/icons-material/AdminPanelSettings';
import LinkIcon from '@mui/icons-material/Link';
import LogoutIcon from '@mui/icons-material/Logout';
import './App.css';

const AppContent: React.FC = () => {
  const { currentUser, logout, isAuthorized } = useAuth();
  const isDarkMode = false;
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('sm'));
  const [mobileOpen, setMobileOpen] = useState(false);

  const handleDrawerToggle = () => {
    setMobileOpen(!mobileOpen);
  };

  const handleNavigation = (path: string) => {
    setMobileOpen(false);
  };

  const navigationItems = isAuthorized ? [
    { text: 'Event Manager', icon: <EventIcon />, path: '/' },
    { text: 'Date Manager', icon: <CalendarMonthIcon />, path: '/dates' },
    { text: 'References', icon: <LinkIcon />, path: '/references' },
    { text: 'Notifications', icon: <NotificationsIcon />, path: '/notifications' },
    { text: 'Authorized Users', icon: <AdminPanelSettingsIcon />, path: '/admin' },
  ] : [];

  const drawer = (
    <List>
      {navigationItems.map((item) => (
        <ListItem 
          key={item.text} 
          component={Link} 
          to={item.path}
          onClick={() => handleNavigation(item.path)}
        >
          <ListItemIcon sx={{ minWidth: 40 }}>
            {item.icon}
          </ListItemIcon>
          <ListItemText primary={item.text} />
        </ListItem>
      ))}
      {currentUser && (
        <ListItem onClick={logout}>
          <ListItemIcon sx={{ minWidth: 40 }}>
            <LogoutIcon />
          </ListItemIcon>
          <ListItemText primary="Logout" />
        </ListItem>
      )}
    </List>
  );

  return (
    <ThemeProvider theme={isDarkMode ? darkTheme : lightTheme}>
      <CssBaseline />
      <div className="App">
        <AppBar position="static">
          <Toolbar sx={{ justifyContent: 'space-between' }}>
            <Box sx={{ display: 'flex', alignItems: 'center' }}>
              {currentUser && isAuthorized && isMobile && (
                <IconButton
                  color="inherit"
                  aria-label="open drawer"
                  edge="start"
                  onClick={handleDrawerToggle}
                  sx={{ mr: 2 }}
                >
                  <MenuIcon />
                </IconButton>
              )}
              <Typography 
                variant="h6" 
                component="div" 
                sx={{ 
                  fontSize: { xs: '1rem', sm: '1.25rem' }
                }}
              >
                District 37 Toastmasters Admin
              </Typography>
            </Box>
            {currentUser && (
              <Box sx={{ display: { xs: 'none', sm: 'flex' }, gap: 2, ml: 'auto' }}>
                {isAuthorized && navigationItems.map((item) => (
                  <Button
                    key={item.text}
                    color="inherit"
                    component={Link}
                    to={item.path}
                  >
                    {item.text}
                  </Button>
                ))}
                <Button color="inherit" onClick={logout}>
                  Logout
                </Button>
              </Box>
            )}
          </Toolbar>
        </AppBar>

        <Drawer
          variant="temporary"
          anchor="left"
          open={mobileOpen}
          onClose={handleDrawerToggle}
          ModalProps={{
            keepMounted: true, // Better open performance on mobile.
          }}
          sx={{
            display: { xs: 'block', sm: 'none' },
            '& .MuiDrawer-paper': { 
              boxSizing: 'border-box', 
              width: 240,
              backgroundColor: theme.palette.background.paper
            },
          }}
        >
          {drawer}
        </Drawer>

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
              path="/references"
              element={
                <ProtectedRoute>
                  <ReferencesManager />
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

const App = () => {
  return (
    <Router>
      <AuthProvider>
        <AppContent />
      </AuthProvider>
    </Router>
  );
};

export default App;
