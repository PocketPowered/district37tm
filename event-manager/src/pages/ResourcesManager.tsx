import React, { useCallback, useEffect, useMemo, useState } from 'react';
import {
  Alert,
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
  MenuItem,
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
import { resourceService, normalizeResourceCategoryKey } from '../services/resourceService';
import { useConference } from '../contexts/ConferenceContext';

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
  resourceType: '',
};

const formatCategoryLabel = (resourceType: string): string => {
  return resourceType
    .split('_')
    .filter(Boolean)
    .map((segment) => segment.charAt(0).toUpperCase() + segment.slice(1))
    .join(' ');
};

const getErrorMessage = (error: unknown, fallback: string): string => {
  if (error && typeof error === 'object' && 'message' in error) {
    const message = (error as { message?: unknown }).message;
    if (typeof message === 'string' && message.trim().length > 0) {
      return message;
    }
  }
  return fallback;
};

const sortCategoryKeys = (keys: string[], labels: Record<string, string>): string[] => {
  const priorityOrder = new Map(PRIORITY_RESOURCE_TYPES.map((type, index) => [type, index]));

  return [...keys].sort((left, right) => {
    const leftPriority = priorityOrder.get(left);
    const rightPriority = priorityOrder.get(right);

    if (leftPriority !== undefined && rightPriority !== undefined) {
      return leftPriority - rightPriority;
    }

    if (leftPriority !== undefined) return -1;
    if (rightPriority !== undefined) return 1;

    const leftLabel = labels[left] || formatCategoryLabel(left);
    const rightLabel = labels[right] || formatCategoryLabel(right);
    return leftLabel.localeCompare(rightLabel);
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
  const { selectedConference, loading: conferenceLoading } = useConference();
  const [categories, setCategories] = useState<string[]>([]);
  const [categoryLabels, setCategoryLabels] = useState<Record<string, string>>({});
  const [resourcesByCategory, setResourcesByCategory] = useState<Record<string, BackendExternalLink[]>>({});
  const [selectedCategory, setSelectedCategory] = useState<string>('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [resourceDialogOpen, setResourceDialogOpen] = useState(false);
  const [categoryDialogOpen, setCategoryDialogOpen] = useState(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [deleteCategoryDialogOpen, setDeleteCategoryDialogOpen] = useState(false);

  const [resourceToDelete, setResourceToDelete] = useState<BackendExternalLink | null>(null);
  const [categoryToDelete, setCategoryToDelete] = useState<string | null>(null);
  const [editingResource, setEditingResource] = useState<BackendExternalLink | null>(null);

  const [formData, setFormData] = useState<ResourceFormData>(initialFormData);
  const [urlError, setUrlError] = useState<string | null>(null);
  const [categoryError, setCategoryError] = useState<string | null>(null);

  const [newCategoryName, setNewCategoryName] = useState('');
  const [newCategoryError, setNewCategoryError] = useState<string | null>(null);
  const [deletingCategory, setDeletingCategory] = useState(false);

  const currentTab = useMemo(
    () => categories.findIndex((category) => category === selectedCategory),
    [categories, selectedCategory]
  );

  const categoryOptions = useMemo(() => {
    const options = new Set<string>(categories);
    if (formData.resourceType) {
      options.add(formData.resourceType);
    }
    return Array.from(options);
  }, [categories, formData.resourceType]);

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

  const getCategoryLabel = (key: string): string => {
    return categoryLabels[key] || formatCategoryLabel(key);
  };

  const fetchResources = useCallback(async (preferredCategory?: string) => {
    if (!selectedConference) {
      setCategories([]);
      setCategoryLabels({});
      setResourcesByCategory({});
      setSelectedCategory('');
      setError(null);
      setLoading(false);
      return;
    }

    try {
      const [resources, fetchedCategories] = await Promise.all([
        resourceService.getAllResources(selectedConference.id, SYSTEM_RESOURCE_TYPES),
        resourceService.getResourceCategories(selectedConference.id, SYSTEM_RESOURCE_TYPES),
      ]);

      const nextCategoryLabels: Record<string, string> = {};
      const mergedCategoryKeys: string[] = [];

      fetchedCategories.forEach((category) => {
        nextCategoryLabels[category.key] = category.displayName;
        mergedCategoryKeys.push(category.key);
      });

      const nextGroupedResources: Record<string, BackendExternalLink[]> = {};
      mergedCategoryKeys.forEach((key) => {
        nextGroupedResources[key] = [];
      });

      resources.forEach((resource) => {
        const resourceType = resource.resourceType;
        if (!resourceType) return;

        if (!nextGroupedResources[resourceType]) {
          nextGroupedResources[resourceType] = [];
          mergedCategoryKeys.push(resourceType);
        }

        if (!nextCategoryLabels[resourceType]) {
          nextCategoryLabels[resourceType] = formatCategoryLabel(resourceType);
        }

        nextGroupedResources[resourceType].push(resource);
      });

      const sortedCategories = sortCategoryKeys(Array.from(new Set(mergedCategoryKeys)), nextCategoryLabels);

      setCategories(sortedCategories);
      setCategoryLabels(nextCategoryLabels);
      setResourcesByCategory(nextGroupedResources);
      setSelectedCategory((previousCategory) => {
        if (preferredCategory && sortedCategories.includes(preferredCategory)) {
          return preferredCategory;
        }

        if (previousCategory && sortedCategories.includes(previousCategory)) {
          return previousCategory;
        }

        return sortedCategories[0] || '';
      });
      setError(null);
    } catch (err) {
      setError('Failed to fetch resources');
      console.error(err);
    } finally {
      setLoading(false);
    }
  }, [selectedConference]);

  useEffect(() => {
    setLoading(true);
    void fetchResources();
  }, [fetchResources]);

  const openAddResourceDialogForCategory = (categoryKey: string) => {
    setEditingResource(null);
    setFormData({
      ...initialFormData,
      resourceType: categoryKey,
    });
    setUrlError(null);
    setCategoryError(null);
    setResourceDialogOpen(true);
  };

  const openNewCategoryDialog = () => {
    setNewCategoryName('');
    setNewCategoryError(null);
    setCategoryDialogOpen(true);
  };

  const handleCreateCategory = async () => {
    if (!selectedConference) return;

    const trimmedName = newCategoryName.trim();
    const normalizedKey = normalizeResourceCategoryKey(trimmedName);

    if (!trimmedName || !normalizedKey) {
      setNewCategoryError('Category name must include letters or numbers');
      return;
    }

    try {
      const createdCategory = await resourceService.createResourceCategory(trimmedName, selectedConference.id);
      await fetchResources(createdCategory.key);
      setCategoryDialogOpen(false);
      setNewCategoryName('');
      setNewCategoryError(null);
    } catch (err) {
      setNewCategoryError(getErrorMessage(err, 'Failed to create category'));
      console.error(err);
    }
  };

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

  const handleAddResource = async () => {
    if (!selectedConference) return;

    if (!formData.resourceType) {
      setCategoryError('Category is required');
      return;
    }

    if (!validateUrl(formData.url)) {
      setUrlError('URL must start with https://');
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
        formData.resourceType,
        selectedConference.id
      );

      await fetchResources(formData.resourceType);
      setFormData({
        ...initialFormData,
        resourceType: formData.resourceType,
      });
      setResourceDialogOpen(false);
      setUrlError(null);
      setCategoryError(null);
    } catch (err) {
      setError('Failed to add resource');
      console.error(err);
    }
  };

  const handleUpdateResource = async () => {
    if (!editingResource?.id) return;
    if (!selectedConference) return;

    if (!formData.resourceType) {
      setCategoryError('Category is required');
      return;
    }

    if (!validateUrl(formData.url)) {
      setUrlError('URL must start with https://');
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
        formData.resourceType,
        selectedConference.id
      );

      await fetchResources(formData.resourceType);
      setFormData({
        ...initialFormData,
        resourceType: formData.resourceType,
      });
      setEditingResource(null);
      setResourceDialogOpen(false);
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

  const handleDeleteCategoryClick = (category: string) => {
    setCategoryToDelete(category);
    setDeleteCategoryDialogOpen(true);
  };

  const handleRemoveConfirm = async () => {
    if (!resourceToDelete?.id) return;
    if (!selectedConference) return;

    try {
      await resourceService.deleteResource(resourceToDelete.id, selectedConference.id);
      await fetchResources(selectedCategory);
      setDeleteDialogOpen(false);
      setResourceToDelete(null);
    } catch (err) {
      setError('Failed to remove resource');
      console.error(err);
    }
  };

  const handleDeleteCategoryConfirm = async () => {
    if (!categoryToDelete) return;
    if (!selectedConference) return;

    try {
      setDeletingCategory(true);
      await resourceService.deleteResourceCategory(categoryToDelete, selectedConference.id);
      await fetchResources();
      setDeleteCategoryDialogOpen(false);
      setCategoryToDelete(null);
    } catch (err) {
      setError(getErrorMessage(err, 'Failed to delete category'));
      console.error(err);
    } finally {
      setDeletingCategory(false);
    }
  };

  const handleEditClick = (resource: BackendExternalLink) => {
    const existingType = resource.resourceType || selectedCategory || categories[0] || '';

    setEditingResource(resource);
    setFormData({
      id: resource.id,
      displayName: resource.displayName || '',
      url: resource.url || '',
      description: resource.description || '',
      resourceType: existingType,
    });
    setUrlError(null);
    setCategoryError(null);
    setResourceDialogOpen(true);
  };

  const handleFormChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = event.target;

    if (name === 'url') {
      handleUrlChange(value);
      return;
    }

    if (name === 'resourceType') {
      setCategoryError(value ? null : 'Category is required');
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
            No {getCategoryLabel(category)} Resources Found
          </Typography>
          <Typography color="text.secondary" paragraph>
            There are no resources in this category yet. Click Add Resource above to add one.
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

  if (conferenceLoading || loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', p: 3 }}>
        <CircularProgress />
      </Box>
    );
  }

  if (!selectedConference) {
    return (
      <Container maxWidth="md">
        <Paper sx={{ p: 3 }}>
          <Alert severity="warning">No conference selected. Choose a conference before managing resources.</Alert>
        </Paper>
      </Container>
    );
  }

  const normalizedCategoryPreview = normalizeResourceCategoryKey(newCategoryName);
  const deleteCategoryResourceCount = categoryToDelete ? (resourcesByCategory[categoryToDelete]?.length ?? 0) : 0;
  const deleteCategoryLabel = categoryToDelete ? getCategoryLabel(categoryToDelete) : '';

  return (
    <Container maxWidth="md">
      <Paper sx={{ p: 3 }}>
        <Box sx={{ display: 'flex', justifyContent: 'flex-end', alignItems: 'center', mb: 3 }}>
          <Button startIcon={<AddIcon />} onClick={openNewCategoryDialog} variant="contained" color="primary">
            New Category
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
              Create a category first, then add resources to it.
            </Typography>
          </Box>
        ) : (
          <>
            <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
              <Tabs value={currentTab < 0 ? 0 : currentTab} onChange={handleTabChange} variant="scrollable" scrollButtons="auto">
                {categories.map((category) => (
                  <Tab key={category} id={`resource-tab-${category}`} label={getCategoryLabel(category)} />
                ))}
              </Tabs>
            </Box>

            {categories.map((category, index) => (
              <TabPanel key={category} value={currentTab < 0 ? 0 : currentTab} index={index}>
                <Box sx={{ display: 'flex', justifyContent: 'flex-end', alignItems: 'center', gap: 1, mb: 2 }}>
                  <Button
                    startIcon={<DeleteIcon />}
                    variant="outlined"
                    color="error"
                    onClick={() => handleDeleteCategoryClick(category)}
                  >
                    Delete Category
                  </Button>
                  <Button startIcon={<AddIcon />} variant="outlined" onClick={() => openAddResourceDialogForCategory(category)}>
                    Add Resource
                  </Button>
                </Box>
                {renderResourceList(resourcesByCategory[category] || [], category)}
              </TabPanel>
            ))}
          </>
        )}

        <Dialog open={categoryDialogOpen} onClose={() => setCategoryDialogOpen(false)} maxWidth="sm" fullWidth>
          <DialogTitle>Create Category</DialogTitle>
          <DialogContent>
            <Box sx={{ mt: 2, display: 'flex', flexDirection: 'column', gap: 2 }}>
              <TextField
                label="Category Name"
                value={newCategoryName}
                onChange={(event) => {
                  setNewCategoryName(event.target.value);
                  if (event.target.value.trim()) {
                    setNewCategoryError(null);
                  }
                }}
                required
                error={!!newCategoryError}
                helperText={newCategoryError || `Stable key: ${normalizedCategoryPreview || '(enter a category name)'}`}
                placeholder="First Timer Resources"
                fullWidth
              />
            </Box>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setCategoryDialogOpen(false)}>Cancel</Button>
            <Button onClick={handleCreateCategory} variant="contained" disabled={!newCategoryName.trim()}>
              Create
            </Button>
          </DialogActions>
        </Dialog>

        <Dialog open={resourceDialogOpen} onClose={() => setResourceDialogOpen(false)} maxWidth="md" fullWidth>
          <DialogTitle>{editingResource ? 'Edit Resource' : 'Add New Resource'}</DialogTitle>
          <DialogContent>
            <Box sx={{ mt: 2, display: 'flex', flexDirection: 'column', gap: 3 }}>
              <TextField
                select
                label="Category"
                name="resourceType"
                value={formData.resourceType}
                onChange={handleFormChange}
                required
                error={!!categoryError}
                helperText={categoryError || 'Select the category for this resource'}
                fullWidth
              >
                {categoryOptions.map((category) => (
                  <MenuItem key={category} value={category}>
                    {getCategoryLabel(category)}
                  </MenuItem>
                ))}
              </TextField>

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
            <Button onClick={() => setResourceDialogOpen(false)}>Cancel</Button>
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
          open={deleteCategoryDialogOpen}
          onClose={() => {
            if (deletingCategory) return;
            setDeleteCategoryDialogOpen(false);
            setCategoryToDelete(null);
          }}
          aria-labelledby="delete-category-dialog-title"
          aria-describedby="delete-category-dialog-description"
        >
          <DialogTitle id="delete-category-dialog-title">Delete Category</DialogTitle>
          <DialogContent>
            <DialogContentText id="delete-category-dialog-description">
              {categoryToDelete
                ? `Delete "${deleteCategoryLabel}" category and its ${deleteCategoryResourceCount} resource${
                    deleteCategoryResourceCount === 1 ? '' : 's'
                  }? This action cannot be undone.`
                : 'Delete this category? This action cannot be undone.'}
            </DialogContentText>
          </DialogContent>
          <DialogActions>
            <Button
              onClick={() => {
                setDeleteCategoryDialogOpen(false);
                setCategoryToDelete(null);
              }}
              disabled={deletingCategory}
            >
              Cancel
            </Button>
            <Button onClick={handleDeleteCategoryConfirm} color="error" autoFocus disabled={deletingCategory}>
              {deletingCategory ? 'Deleting...' : 'Delete Category'}
            </Button>
          </DialogActions>
        </Dialog>

        <Dialog
          open={deleteDialogOpen}
          onClose={() => setDeleteDialogOpen(false)}
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
            <Button onClick={() => setDeleteDialogOpen(false)}>Cancel</Button>
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
