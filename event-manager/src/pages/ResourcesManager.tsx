import React, { useEffect, useMemo, useState } from 'react';
import {
  Alert,
  Autocomplete,
  Box,
  Button,
  CircularProgress,
  Container,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  Link,
  Paper,
  Stack,
  Tab,
  Tabs,
  TextField,
  Typography,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import DeleteIcon from '@mui/icons-material/Delete';
import EditIcon from '@mui/icons-material/Edit';
import { BackendExternalLink } from '../types/BackendExternalLink';
import { resourceService } from '../services/resourceService';

const SYSTEM_RESOURCE_TYPES = ['splash'];
const PRIORITY_RESOURCE_TYPES = ['general', 'first_timer'];

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

interface ResourceFormData {
  id: string | null;
  displayName: string;
  url: string;
  description: string;
  resourceType: string;
}

const initialFormData: ResourceFormData = {
  id: null,
  displayName: '',
  url: '',
  description: '',
  resourceType: 'general',
};

const formatCategoryLabel = (resourceType: string): string => {
  return resourceType
    .split('_')
    .filter(Boolean)
    .map((segment) => segment.charAt(0).toUpperCase() + segment.slice(1))
    .join(' ');
};

const normalizeCategory = (resourceType: string): string => {
  return resourceType
    .trim()
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '_')
    .replace(/^_+|_+$/g, '')
    .replace(/_+/g, '_');
};

const validateCategory = (resourceType: string): boolean => {
  return /^[a-z0-9]+(?:_[a-z0-9]+)*$/.test(resourceType);
};

const sortCategories = (resourceTypes: string[]): string[] => {
  const priorityOrder = new Map(PRIORITY_RESOURCE_TYPES.map((type, index) => [type, index]));

  return [...resourceTypes].sort((left, right) => {
    const leftPriority = priorityOrder.get(left);
    const rightPriority = priorityOrder.get(right);

    if (leftPriority !== undefined && rightPriority !== undefined) {
      return leftPriority - rightPriority;
    }

    if (leftPriority !== undefined) return -1;
    if (rightPriority !== undefined) return 1;

    return formatCategoryLabel(left).localeCompare(formatCategoryLabel(right));
  });
};

function TabPanel(props: TabPanelProps) {
  const { children, value, index, ...other } = props;

  return (
    <div
      role="tabpanel"
      hidden={value !== index}
      id={`resource-tabpanel-${index}`}
      aria-labelledby={`resource-tab-${index}`}
      {...other}
    >
      {value === index && <Box sx={{ p: 3 }}>{children}</Box>}
    </div>
  );
}

const ResourcesManager: React.FC = () => {
  const [categories, setCategories] = useState<string[]>([]);
  const [resourcesByCategory, setResourcesByCategory] = useState<Record<string, BackendExternalLink[]>>({});
  const [selectedCategory, setSelectedCategory] = useState<string>('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [openDialog, setOpenDialog] = useState(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [resourceToDelete, setResourceToDelete] = useState<BackendExternalLink | null>(null);
  const [editingResource, setEditingResource] = useState<BackendExternalLink | null>(null);
  const [formData, setFormData] = useState<ResourceFormData>(initialFormData);
  const [urlError, setUrlError] = useState<string | null>(null);
  const [categoryError, setCategoryError] = useState<string | null>(null);

  const currentTab = useMemo(
    () => categories.findIndex((category) => category === selectedCategory),
    [categories, selectedCategory]
  );

  const validateUrl = (url: string): boolean => {
    return url.startsWith('https://');
  };

  const formatUrl = (url: string): string => {
    if (url.startsWith('https://')) return url;
    if (url.startsWith('http://')) {
      return url.replace('http://', 'https://');
    }
    return `https://${url}`;
  };

  const resetDialog = () => {
    setEditingResource(null);
    setFormData({
      ...initialFormData,
      resourceType: selectedCategory || categories[0] || initialFormData.resourceType,
    });
    setUrlError(null);
    setCategoryError(null);
  };

  const fetchResources = async (preferredCategory?: string) => {
    try {
      const [resources, fetchedCategories] = await Promise.all([
        resourceService.getAllResources(SYSTEM_RESOURCE_TYPES),
        resourceService.getResourceCategories(SYSTEM_RESOURCE_TYPES),
      ]);

      const mergedCategories = sortCategories(
        Array.from(new Set([...PRIORITY_RESOURCE_TYPES, ...fetchedCategories]))
      );

      const nextGroupedResources = mergedCategories.reduce<Record<string, BackendExternalLink[]>>(
        (accumulator, category) => {
          accumulator[category] = [];
          return accumulator;
        },
        {}
      );

      resources.forEach((resource) => {
        const resourceType = resource.resourceType;
        if (!resourceType) return;

        if (!nextGroupedResources[resourceType]) {
          nextGroupedResources[resourceType] = [];
          mergedCategories.push(resourceType);
        }
        nextGroupedResources[resourceType].push(resource);
      });

      const sortedMergedCategories = sortCategories(Array.from(new Set(mergedCategories)));

      setCategories(sortedMergedCategories);
      setResourcesByCategory(nextGroupedResources);
      setSelectedCategory((previousCategory) => {
        if (preferredCategory && sortedMergedCategories.includes(preferredCategory)) {
          return preferredCategory;
        }
        if (previousCategory && sortedMergedCategories.includes(previousCategory)) {
          return previousCategory;
        }
        return sortedMergedCategories[0] || '';
      });
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

  const handleUrlChange = (url: string) => {
    setFormData((prev) => ({
      ...prev,
      url,
    }));

    if (url && !validateUrl(url)) {
      setUrlError('URL must start with https://');
    } else {
      setUrlError(null);
    }
  };

  const handleResourceTypeInput = (resourceType: string) => {
    setFormData((prev) => ({
      ...prev,
      resourceType,
    }));

    if (resourceType.trim().length === 0) {
      setCategoryError('Category is required');
    } else {
      setCategoryError(null);
    }
  };

  const handleAddResource = async () => {
    if (!validateUrl(formData.url)) {
      setUrlError('URL must start with https://');
      return;
    }

    const normalizedResourceType = normalizeCategory(formData.resourceType);
    if (!normalizedResourceType || !validateCategory(normalizedResourceType)) {
      setCategoryError('Category must use letters/numbers and underscores only');
      return;
    }

    try {
      const formattedUrl = formatUrl(formData.url);
      await resourceService.createResource(
        {
          id: null,
          displayName: formData.displayName,
          url: formattedUrl,
          description: formData.description || null,
        },
        normalizedResourceType
      );

      await fetchResources(normalizedResourceType);
      setFormData({
        ...initialFormData,
        resourceType: normalizedResourceType,
      });
      setOpenDialog(false);
      setUrlError(null);
      setCategoryError(null);
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

    const normalizedResourceType = normalizeCategory(formData.resourceType);
    if (!normalizedResourceType || !validateCategory(normalizedResourceType)) {
      setCategoryError('Category must use letters/numbers and underscores only');
      return;
    }

    try {
      const formattedUrl = formatUrl(formData.url);
      await resourceService.updateResource(
        editingResource.id,
        {
          id: editingResource.id,
          displayName: formData.displayName,
          url: formattedUrl,
          description: formData.description || null,
        },
        normalizedResourceType
      );

      await fetchResources(normalizedResourceType);
      setFormData({
        ...initialFormData,
        resourceType: normalizedResourceType,
      });
      setEditingResource(null);
      setOpenDialog(false);
      setUrlError(null);
      setCategoryError(null);
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
      await resourceService.deleteResource(resourceToDelete.id);
      await fetchResources(selectedCategory);
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
      description: resource.description || '',
      resourceType: resource.resourceType || selectedCategory || categories[0] || initialFormData.resourceType,
    });
    setUrlError(null);
    setCategoryError(null);
    setOpenDialog(true);
  };

  const handleFormChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    if (name === 'url') {
      handleUrlChange(value);
      return;
    }

    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleTabChange = (_event: React.SyntheticEvent, newValue: number) => {
    const nextCategory = categories[newValue];
    if (nextCategory) {
      setSelectedCategory(nextCategory);
    }
  };

  const renderResourceList = (resources: BackendExternalLink[], category: string) => {
    if (resources.length === 0) {
      return (
        <Box sx={{ textAlign: 'center', py: 4 }}>
          <Typography variant="h6" gutterBottom>
            No {formatCategoryLabel(category)} Resources Found
          </Typography>
          <Typography color="text.secondary" paragraph>
            There are no resources in this category yet. Click the button above to add one.
          </Typography>
        </Box>
      );
    }

    return (
      <Stack spacing={2}>
        {resources.map((resource) => (
          <Paper key={resource.id} sx={{ p: 2 }}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
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
                      textDecoration: 'underline',
                    },
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
                      opacity: 0.7,
                    }}
                  >
                    {resource.description}
                  </Typography>
                )}
              </Box>
              <Box>
                <Button startIcon={<EditIcon />} onClick={() => handleEditClick(resource)} sx={{ mr: 1 }}>
                  Edit
                </Button>
                <Button startIcon={<DeleteIcon />} color="error" onClick={() => handleRemoveClick(resource)}>
                  Delete
                </Button>
              </Box>
            </Box>
          </Paper>
        ))}
      </Stack>
    );
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
          <Typography variant="h5">Resources Manager</Typography>
          <Button
            startIcon={<AddIcon />}
            onClick={() => {
              resetDialog();
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

        {categories.length === 0 ? (
          <Box sx={{ textAlign: 'center', py: 4 }}>
            <Typography variant="h6" gutterBottom>
              No Resource Categories Found
            </Typography>
            <Typography color="text.secondary" paragraph>
              Add your first resource and category to get started.
            </Typography>
          </Box>
        ) : (
          <>
            <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
              <Tabs
                value={currentTab < 0 ? 0 : currentTab}
                onChange={handleTabChange}
                variant="scrollable"
                scrollButtons="auto"
              >
                {categories.map((category) => (
                  <Tab
                    key={category}
                    id={`resource-tab-${category}`}
                    label={formatCategoryLabel(category)}
                  />
                ))}
              </Tabs>
            </Box>

            {categories.map((category, index) => (
              <TabPanel key={category} value={currentTab < 0 ? 0 : currentTab} index={index}>
                {renderResourceList(resourcesByCategory[category] || [], category)}
              </TabPanel>
            ))}
          </>
        )}

        <Dialog open={openDialog} onClose={() => setOpenDialog(false)} maxWidth="md" fullWidth>
          <DialogTitle>{editingResource ? 'Edit Resource' : 'Add New Resource'}</DialogTitle>
          <DialogContent>
            <Box sx={{ mt: 2, display: 'flex', flexDirection: 'column', gap: 3 }}>
              <Autocomplete
                freeSolo
                options={categories}
                value={formData.resourceType}
                onChange={(_event, value) => {
                  handleResourceTypeInput(typeof value === 'string' ? value : '');
                }}
                onInputChange={(_event, value) => {
                  handleResourceTypeInput(value);
                }}
                renderInput={(params) => (
                  <TextField
                    {...params}
                    label="Category"
                    required
                    error={!!categoryError}
                    helperText={
                      categoryError ||
                      `Use snake_case. Example: ${normalizeCategory(formData.resourceType || 'first timer') || 'first_timer'}`
                    }
                    placeholder="general"
                  />
                )}
              />

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
                helperText={urlError || 'URL must start with https://'}
                placeholder="https://example.com"
              />
              <TextField
                label="Description"
                name="description"
                value={formData.description}
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
              disabled={!formData.displayName || !formData.url || !formData.resourceType || !!urlError || !!categoryError}
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
