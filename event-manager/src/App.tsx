import React from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import { ThemeProvider, CssBaseline, AppBar, Toolbar, Typography, Container, Button, Box } from '@mui/material';
import { lightTheme, darkTheme } from './theme';
import EventList from './components/EventList';
import EventForm from './components/EventForm';
import NotificationForm from './components/NotificationForm';
import './App.css';

const App: React.FC = () => {
  // You can add a theme toggle here if needed
  const isDarkMode = false; // Set this based on user preference or system settings

  return (
    <ThemeProvider theme={isDarkMode ? darkTheme : lightTheme}>
      <CssBaseline />
      <Router>
        <div className="App">
          <AppBar position="static">
            <Toolbar>
              <Typography variant="h6" component="div">
                District 37 Toastmasters Admin
              </Typography>
              <Box sx={{ ml: 'auto', display: 'flex', gap: 2 }}>
                <Button color="inherit" component={Link} to="/">
                  Event Manager
                </Button>
                <Button color="inherit" component={Link} to="/notifications">
                  Notifications
                </Button>
              </Box>
            </Toolbar>
          </AppBar>
          <Container maxWidth="lg" sx={{ mt: 4 }}>
            <Routes>
              <Route path="/" element={<EventList />} />
              <Route path="/events/new" element={<EventForm />} />
              <Route path="/events/:id/edit" element={<EventForm />} />
              <Route path="/notifications" element={<NotificationForm />} />
            </Routes>
          </Container>
        </div>
      </Router>
    </ThemeProvider>
  );
};

export default App;
