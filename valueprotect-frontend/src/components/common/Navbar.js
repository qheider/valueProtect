import React, { useEffect, useMemo, useState } from 'react';
import {
  AppBar,
  Toolbar,
  Typography,
  Button,
  Box,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  CircularProgress
} from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import LogoutIcon from '@mui/icons-material/Logout';
import { employeeService } from '../../services/employeeService';

const Navbar = ({ title }) => {
  const navigate = useNavigate();
  const { logout, user } = useAuth();
  const [employee, setEmployee] = useState(null);
  const [profileOpen, setProfileOpen] = useState(false);
  const [loadingProfile, setLoadingProfile] = useState(false);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  useEffect(() => {
    const loadCurrentEmployee = async () => {
      if (!user) {
        return;
      }

      setLoadingProfile(true);
      try {
        const response = await employeeService.getCurrentEmployee();
        setEmployee(response.data);
      } catch (error) {
        setEmployee(null);
      } finally {
        setLoadingProfile(false);
      }
    };

    loadCurrentEmployee();
  }, [user]);

  const displayName = useMemo(() => {
    const firstName = employee?.firstName?.trim();
    const lastName = employee?.lastName?.trim();
    const fullName = [firstName, lastName].filter(Boolean).join(' ');

    return fullName || user?.username || 'Profile';
  }, [employee, user]);

  const getRolesDisplay = (roles) => {
    if (!roles || roles.length === 0) {
      return 'N/A';
    }

    return roles.join(', ');
  };

  const getPrimaryRoleDisplay = (roles) => {
    if (!roles || roles.length === 0) {
      return '';
    }

    const normalizedRole = roles[0].replace('ROLE_', '').toLowerCase();
    return normalizedRole.charAt(0).toUpperCase() + normalizedRole.slice(1);
  };

  const navbarProfileText = getPrimaryRoleDisplay(user?.roles)
    ? `${displayName} (${getPrimaryRoleDisplay(user?.roles)})`
    : displayName;

  return (
    <>
      <AppBar position="static">
        <Toolbar>
        <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
          {title || 'ValueProtect'}
        </Typography>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <Button color="inherit" onClick={() => setProfileOpen(true)} sx={{ textTransform: 'none' }}>
            {navbarProfileText}
          </Button>
          <Button 
            color="inherit" 
            onClick={handleLogout}
            startIcon={<LogoutIcon />}
          >
            Logout
          </Button>
        </Box>
        </Toolbar>
      </AppBar>

      <Dialog open={profileOpen} onClose={() => setProfileOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Employee Profile</DialogTitle>
        <DialogContent dividers>
          {loadingProfile ? (
            <Box display="flex" justifyContent="center" py={2}>
              <CircularProgress size={24} />
            </Box>
          ) : (
            <Box display="flex" flexDirection="column" gap={1}>
              <Typography variant="body2"><strong>Name:</strong> {displayName}</Typography>
              <Typography variant="body2"><strong>Username:</strong> {employee?.userName || user?.username || 'N/A'}</Typography>
              <Typography variant="body2"><strong>Email:</strong> {employee?.email || user?.email || 'N/A'}</Typography>
              <Typography variant="body2"><strong>Employee ID:</strong> {employee?.id ?? user?.employeeId ?? 'N/A'}</Typography>
              <Typography variant="body2"><strong>Employee Number:</strong> {employee?.employeeNumber || 'N/A'}</Typography>
              <Typography variant="body2"><strong>Company:</strong> {employee?.companyName || 'N/A'}</Typography>
              <Typography variant="body2"><strong>City:</strong> {employee?.contactDetailsCity || 'N/A'}</Typography>
              <Typography variant="body2"><strong>Phone:</strong> {employee?.contactDetailsPhone || 'N/A'}</Typography>
              <Typography variant="body2"><strong>Role(s):</strong> {getRolesDisplay(user?.roles)}</Typography>
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setProfileOpen(false)} variant="contained">Close</Button>
        </DialogActions>
      </Dialog>
    </>
  );
};

export default Navbar;
