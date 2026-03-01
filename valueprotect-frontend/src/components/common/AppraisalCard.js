import React, { useState } from 'react';
import {
  Card,
  CardContent,
  Typography,
  Box,
  Button,
  Chip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  CircularProgress
} from '@mui/material';
import { formatDateForDisplay, formatCurrency } from '../../utils/helpers';
import StatusBadge from './StatusBadge';
import LocationOnIcon from '@mui/icons-material/LocationOn';
import CalendarTodayIcon from '@mui/icons-material/CalendarToday';
import AttachMoneyIcon from '@mui/icons-material/AttachMoney';
import { employeeService } from '../../services/employeeService';

const AppraisalCard = ({ appraisal, actions }) => {
  const property = appraisal.property || {};
  const [selectedProfile, setSelectedProfile] = useState(null);
  const [loadingProfile, setLoadingProfile] = useState(false);

  const getRolesDisplay = (roles) => {
    if (!roles || roles.length === 0) {
      return 'N/A';
    }

    return roles
      .map((role) => role.replace('ROLE_', ''))
      .join(', ');
  };

  const openProfileDialog = async (role, fallbackName, employeeId, fallbackCompanyName) => {
    setSelectedProfile({
      role,
      name: fallbackName,
      employeeId,
      companyName: fallbackCompanyName,
      roles: role ? [role.toUpperCase()] : []
    });

    if (!employeeId) {
      return;
    }

    setLoadingProfile(true);
    try {
      const response = await employeeService.getEmployeeById(employeeId);
      const employee = response.data;
      const fullName = [employee?.firstName, employee?.lastName].filter(Boolean).join(' ');

      setSelectedProfile({
        role,
        name: fullName || fallbackName,
        username: employee?.userName,
        email: employee?.email,
        employeeId: employee?.id ?? employeeId,
        employeeNumber: employee?.employeeNumber,
        companyName: employee?.companyName || fallbackCompanyName,
        city: employee?.contactDetailsCity,
        phone: employee?.contactDetailsPhone,
        roles: employee?.roles || (role ? [role.toUpperCase()] : [])
      });
    } catch (error) {
      setSelectedProfile((prev) => ({
        ...prev,
        roles: prev?.roles || (role ? [role.toUpperCase()] : [])
      }));
    } finally {
      setLoadingProfile(false);
    }
  };

  const closeProfileDialog = () => {
    setSelectedProfile(null);
  };

  return (
    <Card sx={{ mb: 2, '&:hover': { boxShadow: 3 }, transition: 'box-shadow 0.3s' }}>
      <CardContent>
        <Box display="flex" justifyContent="space-between" alignItems="start" mb={2}>
          <Box>
            <Typography variant="h6" component="div">
              {property.addressLine1 || 'N/A'}
            </Typography>
            <Typography variant="body2" color="text.secondary">
              {property.city}, {property.stateProvince} {property.zipPostalCode}
            </Typography>
          </Box>
          <StatusBadge status={appraisal.status} />
        </Box>

        <Box display="flex" flexDirection="column" gap={1} mb={2}>
          <Box display="flex" alignItems="center" gap={1}>
            <LocationOnIcon fontSize="small" color="action" />
            <Typography variant="body2">
              APN: {property.apn || 'N/A'}
            </Typography>
          </Box>
          
          <Box display="flex" alignItems="center" gap={1}>
            <AttachMoneyIcon fontSize="small" color="action" />
            <Typography variant="body2">
              Value: {formatCurrency(appraisal.appraisedValue)}
            </Typography>
          </Box>

          <Box display="flex" alignItems="center" gap={1}>
            <CalendarTodayIcon fontSize="small" color="action" />
            <Typography variant="body2">
              Report Date: {formatDateForDisplay(appraisal.reportDate)}
            </Typography>
          </Box>

          {appraisal.appraiserName && (
            <Typography variant="body2" color="text.secondary">
              Appraiser:{' '}
              <Button
                size="small"
                variant="text"
                onClick={() => openProfileDialog('Appraiser', appraisal.appraiserName, appraisal.appraiserId, appraisal.appraiserCompanyName)}
                sx={{
                  minWidth: 0,
                  p: 0,
                  textTransform: 'none',
                  textDecoration: 'underline',
                  verticalAlign: 'baseline'
                }}
              >
                {appraisal.appraiserName}
              </Button>
            </Typography>
          )}

          {appraisal.lenderName && (
            <Typography variant="body2" color="text.secondary">
              Lender:{' '}
              <Button
                size="small"
                variant="text"
                onClick={() => openProfileDialog('Lender', appraisal.lenderName, appraisal.lenderId, appraisal.lenderCompanyName)}
                sx={{
                  minWidth: 0,
                  p: 0,
                  textTransform: 'none',
                  textDecoration: 'underline',
                  verticalAlign: 'baseline'
                }}
              >
                {appraisal.lenderName}
              </Button>
            </Typography>
          )}

          <Typography variant="body2" color="text.secondary">
            Purpose: {appraisal.purpose || 'N/A'}
          </Typography>

          {appraisal.documentCount > 0 && (
            <Chip 
              label={`${appraisal.documentCount} Document(s)`} 
              size="small" 
              variant="outlined" 
              sx={{ width: 'fit-content' }}
            />
          )}
        </Box>

        {actions && (
          <Box display="flex" gap={1} mt={2}>
            {actions.map((action, index) => (
              <Button
                key={index}
                variant={action.variant || 'contained'}
                color={action.color || 'primary'}
                size="small"
                onClick={() => action.onClick(appraisal)}
                startIcon={action.icon}
              >
                {action.label}
              </Button>
            ))}
          </Box>
        )}

        <Dialog open={Boolean(selectedProfile)} onClose={closeProfileDialog} maxWidth="sm" fullWidth>
          <DialogTitle>Employee Profile</DialogTitle>
          <DialogContent dividers>
            {loadingProfile ? (
              <Box display="flex" justifyContent="center" py={2}>
                <CircularProgress size={24} />
              </Box>
            ) : (
              <Box display="flex" flexDirection="column" gap={1}>
                <Typography variant="body2"><strong>Name:</strong> {selectedProfile?.name || 'N/A'}</Typography>
                <Typography variant="body2"><strong>Username:</strong> {selectedProfile?.username || 'N/A'}</Typography>
                <Typography variant="body2"><strong>Email:</strong> {selectedProfile?.email || 'N/A'}</Typography>
                <Typography variant="body2"><strong>Employee ID:</strong> {selectedProfile?.employeeId || 'N/A'}</Typography>
                <Typography variant="body2"><strong>Employee Number:</strong> {selectedProfile?.employeeNumber || 'N/A'}</Typography>
                <Typography variant="body2"><strong>Company:</strong> {selectedProfile?.companyName || 'N/A'}</Typography>
                <Typography variant="body2"><strong>City:</strong> {selectedProfile?.city || 'N/A'}</Typography>
                <Typography variant="body2"><strong>Phone:</strong> {selectedProfile?.phone || 'N/A'}</Typography>
                <Typography variant="body2"><strong>Role(s):</strong> {getRolesDisplay(selectedProfile?.roles)}</Typography>
              </Box>
            )}
          </DialogContent>
          <DialogActions>
            <Button onClick={closeProfileDialog} variant="contained">Close</Button>
          </DialogActions>
        </Dialog>
      </CardContent>
    </Card>
  );
};

export default AppraisalCard;
