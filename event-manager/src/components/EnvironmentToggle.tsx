import React, { useState } from 'react';
import { Box, Button, Typography, Paper, Collapse, IconButton } from '@mui/material';
import { apiConfig, toggleEnvironment } from '../config/api';
import SettingsIcon from '@mui/icons-material/Settings';
import CloseIcon from '@mui/icons-material/Close';

const EnvironmentToggle: React.FC = () => {
  const [expanded, setExpanded] = useState(false);

  return (
    <Box
      sx={{
        position: 'fixed',
        bottom: 16,
        right: 16,
        zIndex: 1000,
        display: 'flex',
        flexDirection: 'row-reverse',
        alignItems: 'flex-end',
        gap: 1,
      }}
    >
      <Collapse in={expanded} orientation="horizontal">
        <Paper
          elevation={3}
          sx={{
            padding: 2,
            backgroundColor: apiConfig.isProduction ? '#ffebee' : '#e8f5e9',
            display: 'flex',
            alignItems: 'center',
            gap: 2,
          }}
        >
          <Typography variant="body2">
            Current Environment: {apiConfig.isProduction ? 'Production' : 'Local'}
          </Typography>
          <Button
            variant="contained"
            color={apiConfig.isProduction ? 'error' : 'success'}
            size="small"
            onClick={toggleEnvironment}
          >
            Switch to {apiConfig.isProduction ? 'Local' : 'Production'}
          </Button>
          <IconButton
            size="small"
            onClick={() => setExpanded(false)}
            sx={{ ml: 1 }}
          >
            <CloseIcon fontSize="small" />
          </IconButton>
        </Paper>
      </Collapse>
      <IconButton
        color={apiConfig.isProduction ? 'error' : 'success'}
        onClick={() => setExpanded(true)}
        sx={{
          backgroundColor: apiConfig.isProduction ? '#ffebee' : '#e8f5e9',
          '&:hover': {
            backgroundColor: apiConfig.isProduction ? '#ffcdd2' : '#c8e6c9',
          },
        }}
      >
        <SettingsIcon />
      </IconButton>
    </Box>
  );
};

export default EnvironmentToggle; 