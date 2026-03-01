import React, { useMemo, useState } from 'react';
import { Link as RouterLink, NavLink, Outlet, useLocation } from 'react-router-dom';
import {
  AppBar,
  Box,
  Breadcrumbs,
  Container,
  Divider,
  Drawer,
  FormControl,
  IconButton,
  Link,
  List,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  MenuItem,
  Select,
  Toolbar,
  Typography,
  useMediaQuery,
  useTheme,
} from '@mui/material';
import MenuIcon from '@mui/icons-material/Menu';
import EventIcon from '@mui/icons-material/Event';
import NotificationsIcon from '@mui/icons-material/Notifications';
import AdminPanelSettingsIcon from '@mui/icons-material/AdminPanelSettings';
import LinkIcon from '@mui/icons-material/Link';
import LogoutIcon from '@mui/icons-material/Logout';
import LocationOnIcon from '@mui/icons-material/LocationOn';
import { useAuth } from '../contexts/AuthContext';
import ApartmentIcon from '@mui/icons-material/Apartment';
import { useConference } from '../contexts/ConferenceContext';

const drawerWidth = 272;

type NavigationItem = {
  text: string;
  path: string;
  icon: React.ReactElement;
  requiresConference?: boolean;
};

const navigationItems: NavigationItem[] = [
  { text: 'Conferences', icon: <ApartmentIcon />, path: '/conferences' },
  { text: 'Authorized Users', icon: <AdminPanelSettingsIcon />, path: '/admin' },
  { text: 'Event Manager', icon: <EventIcon />, path: '/events', requiresConference: true },
  { text: 'Resources', icon: <LinkIcon />, path: '/resources', requiresConference: true },
  { text: 'Locations', icon: <LocationOnIcon />, path: '/locations', requiresConference: true },
  { text: 'Notifications', icon: <NotificationsIcon />, path: '/notifications', requiresConference: true },
];

const sectionTitleByRoot: Record<string, string> = {
  events: 'Event Manager',
  conferences: 'Conferences',
  resources: 'Resources',
  locations: 'Locations',
  notifications: 'Notifications',
  admin: 'Authorized Users',
};

const segmentTitleByKey: Record<string, string> = {
  events: 'Event Manager',
  conferences: 'Conferences',
  resources: 'Resources',
  locations: 'Locations',
  notifications: 'Notifications',
  admin: 'Authorized Users',
  new: 'Create Event',
  edit: 'Edit Event',
};

const toSentenceCase = (value: string): string => {
  return value
    .split('-')
    .filter(Boolean)
    .map((segment) => segment[0].toUpperCase() + segment.slice(1))
    .join(' ');
};

const isPathActive = (pathname: string, path: string): boolean => {
  return pathname === path || pathname.startsWith(`${path}/`);
};

const AdminShell: React.FC = () => {
  const { currentUser, logout } = useAuth();
  const { conferences, selectedConferenceId, setSelectedConferenceId } = useConference();
  const location = useLocation();
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));
  const [mobileOpen, setMobileOpen] = useState(false);

  const title = useMemo(() => {
    const section = location.pathname.split('/').filter(Boolean)[0];
    if (!section) {
      return 'District 37 Admin Portal';
    }
    return sectionTitleByRoot[section] ?? 'District 37 Admin Portal';
  }, [location.pathname]);

  const breadcrumbs = useMemo(() => {
    const segments = location.pathname.split('/').filter(Boolean);
    if (!segments.length) {
      return [];
    }

    const crumbs: Array<{ label: string; to: string }> = [];
    let pathAccumulator = '';
    segments.forEach((segment, index) => {
      if (segment === 'edit' && segments[0] === 'events') {
        pathAccumulator = `${pathAccumulator}/${segment}`;
        crumbs.push({ label: 'Edit Event', to: pathAccumulator });
        return;
      }

      if (segments[0] === 'events' && index === 1 && /^\d+$/.test(segment)) {
        pathAccumulator = `${pathAccumulator}/${segment}`;
        return;
      }

      pathAccumulator = `${pathAccumulator}/${segment}`;
      const label = segmentTitleByKey[segment] ?? toSentenceCase(segment);
      crumbs.push({ label, to: pathAccumulator });
    });

    return crumbs;
  }, [location.pathname]);

  const selectedConferenceName = useMemo(() => {
    if (!selectedConferenceId) {
      return null;
    }
    return conferences.find((conference) => conference.id === selectedConferenceId)?.name || null;
  }, [conferences, selectedConferenceId]);

  const handleDrawerToggle = () => {
    setMobileOpen((value) => !value);
  };

  const handleNavigate = () => {
    setMobileOpen(false);
  };

  const handleLogout = async () => {
    try {
      await logout();
    } finally {
      setMobileOpen(false);
    }
  };

  const drawer = (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      <Toolbar>
        <Typography variant="subtitle1" sx={{ fontWeight: 700 }}>
          District 37 Admin
        </Typography>
      </Toolbar>
      <Divider />
      <List sx={{ py: 1, flex: 1 }}>
        {navigationItems.map((item) => {
          const isDisabled = item.requiresConference === true && !selectedConferenceId;

          return (
            <ListItem key={item.text} disablePadding>
              {isDisabled ? (
                <ListItemButton
                  disabled
                  sx={{
                    mx: 1,
                    borderRadius: 1,
                  }}
                >
                  <ListItemIcon sx={{ minWidth: 40 }}>{item.icon}</ListItemIcon>
                  <ListItemText primary={item.text} />
                </ListItemButton>
              ) : (
                <ListItemButton
                  component={NavLink}
                  to={item.path}
                  onClick={handleNavigate}
                  selected={isPathActive(location.pathname, item.path)}
                  sx={{
                    mx: 1,
                    borderRadius: 1,
                  }}
                >
                  <ListItemIcon sx={{ minWidth: 40 }}>{item.icon}</ListItemIcon>
                  <ListItemText primary={item.text} />
                </ListItemButton>
              )}
            </ListItem>
          );
        })}
        {!selectedConferenceId && (
          <ListItem sx={{ px: 2, pt: 1.5 }}>
            <Typography variant="caption" color="text.secondary" sx={{ textAlign: 'left' }}>
              Select a conference to manage events, resources, locations, and notifications.
            </Typography>
          </ListItem>
        )}
      </List>
      <Divider />
      <List sx={{ py: 1 }}>
        <ListItem disablePadding>
          <ListItemButton onClick={() => void handleLogout()} sx={{ mx: 1, borderRadius: 1 }}>
            <ListItemIcon sx={{ minWidth: 40 }}>
              <LogoutIcon />
            </ListItemIcon>
            <ListItemText primary="Logout" />
          </ListItemButton>
        </ListItem>
      </List>
    </Box>
  );

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh' }}>
      <AppBar
        position="fixed"
        sx={{
          width: { md: `calc(100% - ${drawerWidth}px)` },
          ml: { md: `${drawerWidth}px` },
        }}
      >
        <Toolbar sx={{ gap: 2 }}>
          {isMobile && (
            <IconButton color="inherit" edge="start" onClick={handleDrawerToggle} aria-label="open navigation">
              <MenuIcon />
            </IconButton>
          )}
          <Box sx={{ flex: 1, minWidth: 0 }}>
            <Typography variant="h6" noWrap>
              {title}
            </Typography>
            <Typography variant="body2" sx={{ opacity: 0.85 }} noWrap>
              {selectedConferenceName ? `Selected: ${selectedConferenceName}` : 'District 37 Toastmasters Admin'}
            </Typography>
          </Box>
          <Box
            sx={{
              display: { xs: 'none', sm: 'flex' },
              alignItems: 'center',
              gap: 1,
              minWidth: { sm: 220, md: 280 },
              maxWidth: 380,
              flexShrink: 1,
            }}
          >
            <ApartmentIcon fontSize="small" sx={{ opacity: 0.9 }} />
            <FormControl size="small" sx={{ flex: 1, minWidth: 0 }}>
              <Select
                value={selectedConferenceId?.toString() || ''}
                displayEmpty
                inputProps={{ 'aria-label': 'Selected conference' }}
                renderValue={(value) => {
                  if (!value) {
                    return 'Select conference';
                  }
                  const conferenceId = Number.parseInt(value, 10);
                  return conferences.find((conference) => conference.id === conferenceId)?.name || 'Select conference';
                }}
                onChange={(event) => {
                  const rawValue = event.target.value;
                  const nextValue = rawValue ? Number.parseInt(rawValue, 10) : null;
                  setSelectedConferenceId(nextValue !== null && Number.isFinite(nextValue) ? nextValue : null);
                }}
                sx={{
                  color: 'common.white',
                  borderRadius: 999,
                  bgcolor: 'rgba(255,255,255,0.16)',
                  '.MuiSvgIcon-root': { color: 'rgba(255,255,255,0.95)' },
                  '.MuiSelect-select': {
                    py: 0.85,
                    pr: 4,
                    minWidth: 0,
                    whiteSpace: 'nowrap',
                    overflow: 'hidden',
                    textOverflow: 'ellipsis',
                    fontWeight: 500,
                  },
                  '.MuiOutlinedInput-notchedOutline': { borderColor: 'rgba(255,255,255,0.35)' },
                  '&:hover .MuiOutlinedInput-notchedOutline': { borderColor: 'rgba(255,255,255,0.7)' },
                  '&.Mui-focused .MuiOutlinedInput-notchedOutline': { borderColor: 'common.white' },
                }}
              >
                {conferences.map((conference) => (
                  <MenuItem key={conference.id} value={conference.id.toString()}>
                    {conference.name}
                    {conference.isActive ? ' (Active)' : ''}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          </Box>
          {currentUser?.email && (
            <Typography variant="body2" sx={{ display: { xs: 'none', sm: 'block' }, opacity: 0.92 }} noWrap>
              {currentUser.email}
            </Typography>
          )}
        </Toolbar>
      </AppBar>

      <Box component="nav" sx={{ width: { md: drawerWidth }, flexShrink: { md: 0 } }}>
        <Drawer
          variant="temporary"
          open={mobileOpen}
          onClose={handleDrawerToggle}
          ModalProps={{ keepMounted: true }}
          sx={{
            display: { xs: 'block', md: 'none' },
            '& .MuiDrawer-paper': {
              boxSizing: 'border-box',
              width: drawerWidth,
            },
          }}
        >
          {drawer}
        </Drawer>
        <Drawer
          variant="permanent"
          open
          sx={{
            display: { xs: 'none', md: 'block' },
            '& .MuiDrawer-paper': {
              boxSizing: 'border-box',
              width: drawerWidth,
            },
          }}
        >
          {drawer}
        </Drawer>
      </Box>

      <Box
        component="main"
        sx={{
          flexGrow: 1,
          width: { md: `calc(100% - ${drawerWidth}px)` },
          px: { xs: 2, sm: 3 },
          pb: 4,
        }}
      >
        <Toolbar />
        <Container maxWidth="lg" sx={{ pt: 2 }}>
          {breadcrumbs.length > 1 && (
            <Breadcrumbs sx={{ mb: 2 }}>
              {breadcrumbs.map((crumb, index) => {
                const isLast = index === breadcrumbs.length - 1;
                if (isLast) {
                  return (
                    <Typography key={crumb.to} color="text.primary">
                      {crumb.label}
                    </Typography>
                  );
                }

                return (
                  <Link key={crumb.to} component={RouterLink} to={crumb.to} color="inherit" underline="hover">
                    {crumb.label}
                  </Link>
                );
              })}
            </Breadcrumbs>
          )}
          <Outlet />
        </Container>
      </Box>
    </Box>
  );
};

export default AdminShell;
