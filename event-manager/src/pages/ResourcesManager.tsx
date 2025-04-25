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
  Link,
  Tabs,
  Tab
} from '@mui/material';
import { resourceService } from '../services/resourceService';
import { BackendExternalLink } from '../types/BackendExternalLink';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

function TabPanel(props: TabPanelProps) {
  const { children, value, index, ...other } = props;

  return (
    <div
      role="tabpanel"
      hidden={value !== index}
      id={`simple-tabpanel-${index}`}
      aria-labelledby={`simple-tab-${index}`}
      {...other}
    >
      {value === index && (
        <Box sx={{ p: 3 }}>
          {children}
        </Box>
      )}
    </div>
  );
}

const ResourcesManager: React.FC = () => {
  const [resources, setResources] = useState<BackendExternalLink[]>([]);
  const [firstTimerResources, setFirstTimerResources] = useState<BackendExternalLink[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [openDialog, setOpenDialog] = useState(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [resourceToDelete, setResourceToDelete] = useState<BackendExternalLink | null>(null);
  const [editingResource, setEditingResource] = useState<BackendExternalLink | null>(null);
  const [formData, setFormData] = useState<BackendExternalLink>({
    id: null,
    displayName: '',
    url: '',
    description: null
  });
  const [urlError, setUrlError] = useState<string | null>(null);
  const [currentTab, setCurrentTab] = useState(0);

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

  const fetchResources = async () => {
    try {
      const [regularResources, firstTimerResources] = await Promise.all([
        resourceService.getAllResources(),
        resourceService.getAllFirstTimerResources()
      ]);
      setResources(regularResources);
      setFirstTimerResources(firstTimerResources);
      setError(null);
    } catch (err) {
      setError('Failed to fetch resources');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchResources();
  }, []);

  const handleAddResource = async () => {
    if (!validateUrl(formData.url)) {
      setUrlError('URL must start with https://');
      return;
    }

    try {
      const formattedUrl = formatUrl(formData.url);
      const newResource = await (currentTab === 0 
        ? resourceService.createResource
        : resourceService.createFirstTimerResource)({
        id: null,
        displayName: formData.displayName,
        url: formattedUrl,
        description: formData.description
      });
      await fetchResources();
      setFormData({ id: null, displayName: '', url: '', description: null });
      setOpenDialog(false);
      setUrlError(null);
    } catch (err) {
      setError('Failed to add resource');
      console.error(err);
    }
  };

  const handleUpdateResource = async () => {
    if (!editingResource?.id) return;
    if (!validateUrl(formData.url)) {
      setUrlError('URL must start with https://');
      return;
    }

    try {
      const formattedUrl = formatUrl(formData.url);
      await (currentTab === 0 
        ? resourceService.updateResource
        : resourceService.updateFirstTimerResource)(editingResource.id, {
        id: editingResource.id,
        displayName: formData.displayName,
        url: formattedUrl,
        description: formData.description
      });
      await fetchResources();
      setFormData({ id: null, displayName: '', url: '', description: null });
      setEditingResource(null);
      setOpenDialog(false);
      setUrlError(null);
    } catch (err) {
      setError('Failed to update resource');
      console.error(err);
    }
  };

  const handleRemoveClick = (resource: BackendExternalLink) => {
    setResourceToDelete(resource);
    setDeleteDialogOpen(true);
  };

  const handleRemoveConfirm = async () => {
    if (!resourceToDelete?.id) return;
    try {
      await (currentTab === 0 
        ? resourceService.deleteResource
        : resourceService.deleteFirstTimerResource)(resourceToDelete.id);
      await fetchResources();
      setDeleteDialogOpen(false);
      setResourceToDelete(null);
    } catch (err) {
      setError('Failed to remove resource');
      console.error(err);
    }
  };

  const handleRemoveCancel = () => {
    setDeleteDialogOpen(false);
    setResourceToDelete(null);
  };

  const handleEditClick = (resource: BackendExternalLink) => {
    setEditingResource(resource);
    setFormData({
      id: resource.id,
      displayName: resource.displayName || '',
      url: resource.url || '',
      description: resource.description || null
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

  const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
    setCurrentTab(newValue);
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', p: 3 }}>
        <CircularProgress />
      </Box>
    );
  }

  const renderResourceList = (resources: BackendExternalLink[]) => (
    <Stack spacing={2}>
      {resources.map((resource) => (
        <Paper key={resource.id} sx={{ p: 2 }}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <Box>
              <Box sx={{ textAlign: 'left', width: '100%' }}>
                <Typography variant="subtitle1" align="left">
                  {resource.displayName}
                </Typography>
                <Link
                  href={resource.url}
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
                    {resource.url}
                  </Typography>
                </Link>
                {resource.description && (
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
                    {resource.description}
                  </Typography>
                )}
              </Box>
            </Box>
            <Box>
              <Button
                startIcon={<EditIcon />}
                onClick={() => handleEditClick(resource)}
                sx={{ mr: 1 }}
              >
                Edit
              </Button>
              <Button
                startIcon={<DeleteIcon />}
                color="error"
                onClick={() => handleRemoveClick(resource)}
              >
                Delete
              </Button>
            </Box>
          </Box>
        </Paper>
      ))}
    </Stack>
  );

  return (
    <Container maxWidth="md">
      <Paper sx={{ p: 3 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
          <Typography variant="h5">
            Resources Manager
          </Typography>
          <Button
            startIcon={<AddIcon />}
            onClick={() => {
              setEditingResource(null);
              setFormData({ id: null, displayName: '', url: '', description: null });
              setUrlError(null);
              setOpenDialog(true);
            }}
            variant="contained"
            color="primary"
          >
            Add New Resource
          </Button>
        </Box>

        {error && (
          <Alert severity="error" sx={{ mb: 3 }}>
            {error}
          </Alert>
        )}

        <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
          <Tabs value={currentTab} onChange={handleTabChange}>
            <Tab label="Regular Resources" />
            <Tab label="First Timer Resources" />
          </Tabs>
        </Box>

        <TabPanel value={currentTab} index={0}>
          {resources.length === 0 ? (
            <Box sx={{ textAlign: 'center', py: 4 }}>
              <Typography variant="h6" gutterBottom>
                No Resources Found
              </Typography>
              <Typography color="text.secondary" paragraph>
                There are no resources yet. Click the button above to add your first resource.
              </Typography>
            </Box>
          ) : (
            renderResourceList(resources)
          )}
        </TabPanel>

        <TabPanel value={currentTab} index={1}>
          {firstTimerResources.length === 0 ? (
            <Box sx={{ textAlign: 'center', py: 4 }}>
              <Typography variant="h6" gutterBottom>
                No First Timer Resources Found
              </Typography>
              <Typography color="text.secondary" paragraph>
                There are no first timer resources yet. Click the button above to add your first resource.
              </Typography>
            </Box>
          ) : (
            renderResourceList(firstTimerResources)
          )}
        </TabPanel>

        <Dialog 
          open={openDialog} 
          onClose={() => setOpenDialog(false)}
          maxWidth="md"
          fullWidth
        >
          <DialogTitle>
            {editingResource ? 'Edit Resource' : 'Add New Resource'}
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
                placeholder="Enter a description for this resource"
              />
            </Box>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setOpenDialog(false)}>Cancel</Button>
            <Button 
              onClick={editingResource ? handleUpdateResource : handleAddResource}
              disabled={!formData.displayName || !formData.url || !!urlError}
              variant="contained"
            >
              {editingResource ? 'Update' : 'Add'}
            </Button>
          </DialogActions>
        </Dialog>

        <Dialog
          open={deleteDialogOpen}
          onClose={handleRemoveCancel}
          aria-labelledby="remove-dialog-title"
          aria-describedby="remove-dialog-description"
        >
          <DialogTitle id="remove-dialog-title">Remove Resource</DialogTitle>
          <DialogContent>
            <DialogContentText id="remove-dialog-description">
              Are you sure you want to remove this resource?
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

export default ResourcesManager; 