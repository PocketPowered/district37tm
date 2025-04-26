import React from 'react';
import { Button, Box } from '@mui/material';

const GoogleSignInButton: React.FC<{ onClick: () => void }> = ({ onClick }) => {
  return (
    <Button
      variant="outlined"
      onClick={onClick}
      sx={{
        backgroundColor: 'white',
        color: 'rgba(0, 0, 0, 0.87)',
        border: '1px solid #dadce0',
        borderRadius: '4px',
        padding: '8px 16px',
        textTransform: 'none',
        fontSize: '14px',
        fontWeight: 500,
        '&:hover': {
          backgroundColor: '#f8f9fa',
          borderColor: '#dadce0',
        },
        '&:active': {
          backgroundColor: '#f1f3f4',
        },
      }}
    >
      <Box
        component="img"
        src="https://www.gstatic.com/firebasejs/ui/2.0.0/images/auth/google.svg"
        alt="Google logo"
        sx={{
          width: 18,
          height: 18,
          mr: 1,
        }}
      />
      Sign in with Google
    </Button>
  );
};

export default GoogleSignInButton; 