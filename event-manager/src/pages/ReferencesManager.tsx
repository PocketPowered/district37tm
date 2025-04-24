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
  FormHelperText,
  Link
} from '@mui/material';
import { referenceService } from '../services/referenceService';
import { BackendExternalLink } from '../types/BackendExternalLink';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';

const ReferencesManager: React.FC = () => {
  const [references, setReferences] = useState<BackendExternalLink[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [openDialog, setOpenDialog] = useState(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [referenceToDelete, setReferenceToDelete] = useState<BackendExternalLink | null>(null);
  const [editingReference, setEditingReference] = useState<BackendExternalLink | null>(null);
  const [formData, setFormData] = useState<BackendExternalLink>({
    id: null,
    displayName: '',
    url: '',
    description: null
  });
  const [urlError, setUrlError] = useState<string | null>(null);

  const validateUrl = (url: string | undefined): boolean => {
    if (!url) return false;
    return url.startsWith('https://');
  };

  const formatUrl = (url: string | undefined): string => {
    if (!url) return '';
    if (url.startsWith('https://')) return url;
    if (url.startsWith('http://')) {
      return url.replace('http://', 'https://');
    }
    return `https://${url}`;
  };

  const handleUrlChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const url = e.target.value;
    setFormData(prev => ({
      ...prev,
      url
    }));
    
    if (url && !validateUrl(url)) {
      setUrlError('URL must start with https://');
    } else {
      setUrlError(null);
    }
  };

  const fetchReferences = async () => {
    try {
      const fetchedReferences = await referenceService.getAllReferences();
      setReferences(fetchedReferences);
      setError(null);
    } catch (err) {
      setError('Failed to fetch references');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchReferences();
  }, []);

  const handleAddReference = async () => {
    if (!validateUrl(formData.url)) {
      setUrlError('URL must start with https://');
      return;
    }

    try {
      const formattedUrl = formatUrl(formData.url);
      const newReference = await referenceService.createReference({
        id: null,
        displayName: formData.displayName,
        url: formattedUrl,
        description: formData.description
      });
      await fetchReferences();
      setFormData({ id: null, displayName: '', url: '', description: null });
      setOpenDialog(false);
      setUrlError(null);
    } catch (err) {
      setError('Failed to add reference');
      console.error(err);
    }
  };

  const handleUpdateReference = async () => {
    if (!editingReference?.id) return;
    if (!validateUrl(formData.url)) {
      setUrlError('URL must start with https://');
      return;
    }

    try {
      const formattedUrl = formatUrl(formData.url);
      await referenceService.updateReference(editingReference.id, {
        id: editingReference.id,
        displayName: formData.displayName,
        url: formattedUrl,
        description: formData.description
      });
      await fetchReferences();
      setFormData({ id: null, displayName: '', url: '', description: null });
      setEditingReference(null);
      setOpenDialog(false);
      setUrlError(null);
    } catch (err) {
      setError('Failed to update reference');
      console.error(err);
    }
  };

  const handleRemoveClick = (reference: BackendExternalLink) => {
    setReferenceToDelete(reference);
    setDeleteDialogOpen(true);
  };

  const handleRemoveConfirm = async () => {
    if (!referenceToDelete?.id) return;
    try {
      await referenceService.deleteReference(referenceToDelete.id);
      await fetchReferences();
      setDeleteDialogOpen(false);
      setReferenceToDelete(null);
    } catch (err) {
      setError('Failed to remove reference');
      console.error(err);
    }
  };

  const handleRemoveCancel = () => {
    setDeleteDialogOpen(false);
    setReferenceToDelete(null);
  };

  const handleEditClick = (reference: BackendExternalLink) => {
    setEditingReference(reference);
    setFormData({
      id: reference.id,
      displayName: reference.displayName || '',
      url: reference.url || '',
      description: reference.description || null
    });
    setUrlError(null);
    setOpenDialog(true);
  };

  const handleFormChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    if (name === 'url') {
      handleUrlChange(e);
    } else {
      setFormData(prev => ({
        ...prev,
        [name]: value
      }));
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
            References Manager
          </Typography>
          <Button
            startIcon={<AddIcon />}
            onClick={() => {
              setEditingReference(null);
              setFormData({ id: null, displayName: '', url: '', description: null });
              setUrlError(null);
              setOpenDialog(true);
            }}
            variant="contained"
            color="primary"
          >
            Add New Reference
          </Button>
        </Box>

        {error && (
          <Alert severity="error" sx={{ mb: 3 }}>
            {error}
          </Alert>
        )}

        {references.length === 0 ? (
          <Box sx={{ textAlign: 'center', py: 4 }}>
            <Typography variant="h6" gutterBottom>
              No References Found
            </Typography>
            <Typography color="text.secondary" paragraph>
              There are no references yet. Click the button above to add your first reference.
            </Typography>
          </Box>
        ) : (
          <Stack spacing={2}>
            {references.map((reference) => (
              <Paper key={reference.id} sx={{ p: 2 }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <Box>
                    <Box sx={{ textAlign: 'left', width: '100%' }}>
                      <Typography variant="subtitle1" align="left">
                        {reference.displayName}
                      </Typography>
                      <Link
                        href={reference.url}
                        target="_blank"
                        rel="noopener noreferrer"
                        sx={{ 
                          color: 'text.secondary',
                          textDecoration: 'none',
                          '&:hover': {
                            textDecoration: 'underline'
                          }
                        }}
                      >
                        <Typography variant="body2" align="left">
                          {reference.url}
                        </Typography>
                      </Link>
                      {reference.description && (
                        <Typography 
                          variant="body2" 
                          color="text.secondary"
                          align="left"
                          sx={{ 
                            mt: 1,
                            fontStyle: 'italic',
                            opacity: 0.7
                          }}
                        >
                          {reference.description}
                        </Typography>
                      )}
                    </Box>
                  </Box>
                  <Box>
                    <Button
                      startIcon={<EditIcon />}
                      onClick={() => handleEditClick(reference)}
                      sx={{ mr: 1 }}
                    >
                      Edit
                    </Button>
                    <Button
                      startIcon={<DeleteIcon />}
                      color="error"
                      onClick={() => handleRemoveClick(reference)}
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
            {editingReference ? 'Edit Reference' : 'Add New Reference'}
          </DialogTitle>
          <DialogContent>
            <Box sx={{ mt: 2, display: 'flex', flexDirection: 'column', gap: 3 }}>
              <TextField
                label="Display Name"
                name="displayName"
                value={formData.displayName}
                onChange={handleFormChange}
                fullWidth
                required
              />
              <TextField
                label="URL"
                name="url"
                value={formData.url}
                onChange={handleFormChange}
                fullWidth
                required
                error={!!urlError}
                helperText={urlError || "URL must start with https://"}
                placeholder="https://example.com"
              />
              <TextField
                label="Description"
                name="description"
                value={formData.description || ''}
                onChange={handleFormChange}
                fullWidth
                multiline
                rows={3}
                placeholder="Enter a description for this reference"
              />
            </Box>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setOpenDialog(false)}>Cancel</Button>
            <Button 
              onClick={editingReference ? handleUpdateReference : handleAddReference}
              disabled={!formData.displayName || !formData.url || !!urlError}
              variant="contained"
            >
              {editingReference ? 'Update' : 'Add'}
            </Button>
          </DialogActions>
        </Dialog>

        <Dialog
          open={deleteDialogOpen}
          onClose={handleRemoveCancel}
          aria-labelledby="remove-dialog-title"
          aria-describedby="remove-dialog-description"
        >
          <DialogTitle id="remove-dialog-title">Remove Reference</DialogTitle>
          <DialogContent>
            <DialogContentText id="remove-dialog-description">
              Are you sure you want to remove this reference?
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

export default ReferencesManager; 