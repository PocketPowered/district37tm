import React, { useEffect, useState } from 'react';
import { 
  Box, 
  Button, 
  Container, 
  Typography,
  Paper,
  CircularProgress,
  Alert,
  Stack,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
  TextField,
  IconButton,
  Card,
  CardContent,
  Grid
} from '@mui/material';
import { locationService } from '../services/locationService';
import { Location } from '../types/Location';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import ArrowForwardIcon from '@mui/icons-material/ArrowForward';

const LocationsManager: React.FC = () => {
  const [locations, setLocations] = useState<Location[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [openDialog, setOpenDialog] = useState(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [locationToDelete, setLocationToDelete] = useState<Location | null>(null);
  const [editingLocation, setEditingLocation] = useState<Location | null>(null);
  const [formData, setFormData] = useState<Location>({
    id: '',
    locationName: '',
    locationImages: []
  });
  const [newImageUrl, setNewImageUrl] = useState('');

  const fetchLocations = async () => {
    try {
      const locations = await locationService.getAllLocations();
      setLocations(locations);
      setError(null);
    } catch (err) {
      setError('Failed to fetch locations');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchLocations();
  }, []);

  const handleAddLocation = async () => {
    try {
      const newLocation = await locationService.createLocation(formData);
      await fetchLocations();
      setFormData({ id: '', locationName: '', locationImages: [] });
      setOpenDialog(false);
    } catch (err) {
      setError('Failed to add location');
      console.error(err);
    }
  };

  const handleUpdateLocation = async () => {
    if (!editingLocation?.id) return;
    try {
      await locationService.updateLocation(editingLocation.id, formData);
      await fetchLocations();
      setFormData({ id: '', locationName: '', locationImages: [] });
      setEditingLocation(null);
      setOpenDialog(false);
    } catch (err) {
      setError('Failed to update location');
      console.error(err);
    }
  };

  const handleRemoveClick = (location: Location) => {
    setLocationToDelete(location);
    setDeleteDialogOpen(true);
  };

  const handleRemoveConfirm = async () => {
    if (!locationToDelete?.id) return;
    try {
      await locationService.deleteLocation(locationToDelete.id);
      await fetchLocations();
      setDeleteDialogOpen(false);
      setLocationToDelete(null);
    } catch (err) {
      setError('Failed to remove location');
      console.error(err);
    }
  };

  const handleRemoveCancel = () => {
    setDeleteDialogOpen(false);
    setLocationToDelete(null);
  };

  const handleEditClick = (location: Location) => {
    setEditingLocation(location);
    setFormData(location);
    setOpenDialog(true);
  };

  const handleFormChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleAddImage = () => {
    if (newImageUrl.trim()) {
      setFormData(prev => ({
        ...prev,
        locationImages: [...prev.locationImages, newImageUrl.trim()]
      }));
      setNewImageUrl('');
    }
  };

  const handleRemoveImage = (index: number) => {
    setFormData(prev => ({
      ...prev,
      locationImages: prev.locationImages.filter((_, i) => i !== index)
    }));
  };

  const handleKeyDown = (index: number, e: React.KeyboardEvent) => {
    if (!formData.locationImages) return;
    
    if (e.key === 'ArrowLeft' && index > 0) {
      e.preventDefault();
      const newImages = [...formData.locationImages];
      [newImages[index], newImages[index - 1]] = [newImages[index - 1], newImages[index]];
      setFormData(prev => ({ ...prev, locationImages: newImages }));
    } else if (e.key === 'ArrowRight' && index < formData.locationImages.length - 1) {
      e.preventDefault();
      const newImages = [...formData.locationImages];
      [newImages[index], newImages[index + 1]] = [newImages[index + 1], newImages[index]];
      setFormData(prev => ({ ...prev, locationImages: newImages }));
    }
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', p: 3 }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Container maxWidth="md">
      <Paper sx={{ p: 3 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
          <Typography variant="h5">
            Locations Manager
          </Typography>
          <Button
            startIcon={<AddIcon />}
            onClick={() => {
              setEditingLocation(null);
              setFormData({ id: '', locationName: '', locationImages: [] });
              setOpenDialog(true);
            }}
            variant="contained"
            color="primary"
          >
            Add New Location
          </Button>
        </Box>

        {error && (
          <Alert severity="error" sx={{ mb: 3 }}>
            {error}
          </Alert>
        )}

        {locations.length === 0 ? (
          <Box sx={{ textAlign: 'center', py: 4 }}>
            <Typography variant="h6" gutterBottom>
              No Locations Found
            </Typography>
            <Typography color="text.secondary" paragraph>
              There are no locations yet. Click the button above to add your first location.
            </Typography>
          </Box>
        ) : (
          <Stack spacing={2}>
            {locations.map((location) => (
              <Paper key={location.id} sx={{ p: 2 }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <Box sx={{ flex: 1 }}>
                    <Typography variant="subtitle1" sx={{ textAlign: 'left' }}>
                      {location.locationName}
                    </Typography>
                  </Box>
                  <Box>
                    <Button
                      startIcon={<EditIcon />}
                      onClick={() => handleEditClick(location)}
                      sx={{ mr: 1 }}
                    >
                      Edit
                    </Button>
                    <Button
                      startIcon={<DeleteIcon />}
                      color="error"
                      onClick={() => handleRemoveClick(location)}
                    >
                      Delete
                    </Button>
                  </Box>
                </Box>
              </Paper>
            ))}
          </Stack>
        )}

        <Dialog 
          open={openDialog} 
          onClose={() => setOpenDialog(false)}
          maxWidth="md"
          fullWidth
        >
          <DialogTitle>
            {editingLocation ? 'Edit Location' : 'Add New Location'}
          </DialogTitle>
          <DialogContent>
            <Box sx={{ mt: 2, display: 'flex', flexDirection: 'column', gap: 3 }}>
              <TextField
                label="Location Name"
                name="locationName"
                value={formData.locationName}
                onChange={handleFormChange}
                fullWidth
                required
              />
              <Box>
                <Typography variant="subtitle2" gutterBottom>
                  Location Images
                </Typography>
                <Box sx={{ display: 'flex', gap: 1, mb: 2 }}>
                  <TextField
                    size="small"
                    placeholder="Image URL"
                    value={newImageUrl}
                    onChange={(e) => setNewImageUrl(e.target.value)}
                    sx={{ flex: 1 }}
                  />
                  <Button
                    startIcon={<AddIcon />}
                    onClick={handleAddImage}
                    variant="outlined"
                    disabled={!newImageUrl.trim()}
                  >
                    Add Image
                  </Button>
                </Box>
                <Box
                  sx={{
                    display: 'grid',
                    gridTemplateColumns: 'repeat(auto-fill, minmax(250px, 1fr))',
                    gap: 2,
                    width: '100%',
                    minHeight: '200px'
                  }}
                >
                  {formData.locationImages.map((image, index) => (
                    <Paper
                      key={index}
                      tabIndex={0}
                      onKeyDown={(e) => handleKeyDown(index, e)}
                      sx={{
                        p: 1,
                        display: 'flex',
                        flexDirection: 'column',
                        gap: 1,
                        position: 'relative',
                        transition: 'transform 0.2s ease, box-shadow 0.2s ease',
                        '&:focus': {
                          outline: 'none',
                          transform: 'scale(1.02)',
                          boxShadow: (theme) => theme.shadows[4]
                        }
                      }}
                    >
                      <Box sx={{ 
                        display: 'flex', 
                        gap: 0.5,
                        position: 'absolute',
                        top: 8,
                        left: 8,
                        zIndex: 1
                      }}>
                        <IconButton
                          size="small"
                          disabled={index === 0}
                          onClick={() => handleKeyDown(index, { key: 'ArrowLeft', preventDefault: () => {} } as React.KeyboardEvent)}
                          sx={{ 
                            backgroundColor: 'rgba(255, 255, 255, 0.9)',
                            border: '1px solid rgba(0, 0, 0, 0.2)',
                            '&:hover': {
                              backgroundColor: 'rgba(255, 255, 255, 1)',
                              border: '1px solid rgba(0, 0, 0, 0.4)'
                            },
                            '&.Mui-disabled': {
                              backgroundColor: 'rgba(255, 255, 255, 0.5)',
                              border: '1px solid rgba(0, 0, 0, 0.1)'
                            }
                          }}
                        >
                          <ArrowBackIcon fontSize="small" />
                        </IconButton>
                        <IconButton
                          size="small"
                          disabled={index === formData.locationImages.length - 1}
                          onClick={() => handleKeyDown(index, { key: 'ArrowRight', preventDefault: () => {} } as React.KeyboardEvent)}
                          sx={{ 
                            backgroundColor: 'rgba(255, 255, 255, 0.9)',
                            border: '1px solid rgba(0, 0, 0, 0.2)',
                            '&:hover': {
                              backgroundColor: 'rgba(255, 255, 255, 1)',
                              border: '1px solid rgba(0, 0, 0, 0.4)'
                            },
                            '&.Mui-disabled': {
                              backgroundColor: 'rgba(255, 255, 255, 0.5)',
                              border: '1px solid rgba(0, 0, 0, 0.1)'
                            }
                          }}
                        >
                          <ArrowForwardIcon fontSize="small" />
                        </IconButton>
                      </Box>
                      <Box
                        component="img"
                        src={image}
                        alt={`Upload ${index + 1}`}
                        sx={{
                          width: '100%',
                          height: 150,
                          objectFit: 'cover',
                          borderRadius: 1
                        }}
                        onError={(e) => {
                          const target = e.target as HTMLImageElement;
                          target.src = 'https://via.placeholder.com/150?text=Image+Not+Found';
                        }}
                      />
                      <Typography
                        variant="caption"
                        sx={{
                          wordBreak: 'break-all',
                          fontSize: '0.7rem',
                          color: 'text.secondary',
                          width: '100%',
                          whiteSpace: 'pre-wrap',
                          overflow: 'visible'
                        }}
                      >
                        {image}
                      </Typography>
                      <IconButton
                        size="small"
                        onClick={() => handleRemoveImage(index)}
                        sx={{
                          position: 'absolute',
                          top: 8,
                          right: 8,
                          backgroundColor: 'rgba(255, 255, 255, 0.9)',
                          border: '1px solid rgba(0, 0, 0, 0.2)',
                          '&:hover': {
                            backgroundColor: 'rgba(255, 255, 255, 1)',
                            border: '1px solid rgba(0, 0, 0, 0.4)'
                          }
                        }}
                      >
                        <DeleteIcon fontSize="small" />
                      </IconButton>
                    </Paper>
                  ))}
                </Box>
              </Box>
            </Box>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setOpenDialog(false)}>Cancel</Button>
            <Button 
              onClick={editingLocation ? handleUpdateLocation : handleAddLocation}
              disabled={!formData.locationName}
              variant="contained"
            >
              {editingLocation ? 'Update' : 'Add'}
            </Button>
          </DialogActions>
        </Dialog>

        <Dialog
          open={deleteDialogOpen}
          onClose={handleRemoveCancel}
          aria-labelledby="remove-dialog-title"
          aria-describedby="remove-dialog-description"
        >
          <DialogTitle id="remove-dialog-title">Remove Location</DialogTitle>
          <DialogContent>
            <DialogContentText id="remove-dialog-description">
              Are you sure you want to remove this location?
            </DialogContentText>
          </DialogContent>
          <DialogActions>
            <Button onClick={handleRemoveCancel}>Cancel</Button>
            <Button onClick={handleRemoveConfirm} color="error" autoFocus>
              Remove
            </Button>
          </DialogActions>
        </Dialog>
      </Paper>
    </Container>
  );
};

export default LocationsManager; 