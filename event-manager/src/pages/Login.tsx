import React from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { Container, Typography, Box, Paper } from '@mui/material';
import GoogleSignInButton from '../components/GoogleSignInButton';

const Login: React.FC = () => {
  const { signInWithGoogle } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const from = location.state?.from?.pathname || '/';

  const handleGoogleSignIn = async () => {
    try {
      await signInWithGoogle();
      navigate(from, { replace: true });
    } catch (error) {
      console.error('Failed to sign in:', error);
    }
  };

  return (
    <Container maxWidth="sm">
      <Box
        sx={{
          marginTop: 8,
          display: 'flex',
          flexDirection: 'column',
        }}
      >
        <Paper 
          elevation={3} 
          sx={{ 
            p: 4, 
            width: '100%',
            display: 'flex',
            flexDirection: 'column',
            gap: 3
          }}
        >
          <Box sx={{ width: '100%' }}>
            <Typography component="h1" variant="h4" gutterBottom>
              Welcome to District 37 Toastmasters
            </Typography>
            
            <Typography variant="body1" color="text.secondary" paragraph>
              The District 37 Toastmasters Admin Portal is your central hub for managing events, 
              resources, and communications for our Toastmasters community. Here you can:
            </Typography>

            <Box sx={{ width: '100%', mb: 3 }}>
              <Typography variant="body1" component="ul" sx={{ pl: 2 }}>
                <li>Create and manage upcoming events</li>
                <li>Share important resources with members</li>
                <li>Send notifications to the community</li>
                <li>Manage authorized users and locations</li>
              </Typography>
            </Box>

            <Typography variant="body1" color="text.secondary" paragraph>
              Please sign in with your authorized Google account to access the admin portal.
            </Typography>
          </Box>

          <Box sx={{ display: 'flex', justifyContent: 'center' }}>
            <GoogleSignInButton onClick={handleGoogleSignIn} />
          </Box>
        </Paper>
      </Box>
    </Container>
  );
};

export default Login; 