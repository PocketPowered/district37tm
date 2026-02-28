import React, { useEffect, useState } from 'react';
import { Box, CircularProgress, Container, Typography } from '@mui/material';
import { keyframes } from '@emotion/react';

const pulse = keyframes`
  0%, 80%, 100% {
    transform: scale(0.6);
    opacity: 0.35;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
`;

const AuthLoadingScreen: React.FC = () => {
  const [showLongWaitHint, setShowLongWaitHint] = useState(false);

  useEffect(() => {
    const timeoutId = window.setTimeout(() => setShowLongWaitHint(true), 7000);
    return () => window.clearTimeout(timeoutId);
  }, []);

  return (
    <Container maxWidth="sm">
      <Box
        sx={{
          minHeight: '60vh',
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          gap: 2,
          textAlign: 'center',
        }}
      >
        <CircularProgress size={40} />

        <Typography variant="h6" component="h1">
          Checking your admin session
        </Typography>

        <Box sx={{ display: 'flex', gap: 0.75, alignItems: 'center' }}>
          {[0, 1, 2].map((index) => (
            <Box
              key={index}
              sx={{
                width: 8,
                height: 8,
                borderRadius: '50%',
                backgroundColor: 'primary.main',
                animation: `${pulse} 1.2s ease-in-out infinite`,
                animationDelay: `${index * 0.15}s`,
              }}
            />
          ))}
        </Box>

        <Typography variant="body2" color="text.secondary">
          Finalizing authentication and permissions...
        </Typography>

        {showLongWaitHint && (
          <Typography variant="body2" color="text.secondary">
            Still working. This can take a few seconds on slower networks.
          </Typography>
        )}
      </Box>
    </Container>
  );
};

export default AuthLoadingScreen;
