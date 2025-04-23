import React from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import { AppBar, Toolbar, Typography, Container, Button } from '@mui/material';
import EventList from './components/EventList';
import EventForm from './components/EventForm';
import './App.css';

function App() {
  return (
    <Router>
      <div className="App">
        <AppBar position="static">
          <Toolbar>
            <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
              District 37 Toastmasters Admin Portal
            </Typography>
            <Button color="inherit" component={Link} to="/">
              Events
            </Button>
            <Button color="inherit" component={Link} to="/events/new">
              New Event
            </Button>
          </Toolbar>
        </AppBar>
        <Container sx={{ mt: 4 }}>
          <Routes>
            <Route path="/" element={<EventList />} />
            <Route path="/events/new" element={<EventForm />} />
            <Route path="/events/:id/edit" element={<EventForm />} />
          </Routes>
        </Container>
      </div>
    </Router>
  );
}

export default App;
