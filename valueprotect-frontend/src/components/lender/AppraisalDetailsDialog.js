import React from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Typography,
  Box,
  Grid,
  Divider,
  Chip
} from '@mui/material';
import { formatDateForDisplay, formatCurrency } from '../../utils/helpers';
import StatusBadge from '../common/StatusBadge';
import LocationOnIcon from '@mui/icons-material/LocationOn';
import HomeIcon from '@mui/icons-material/Home';
import AttachMoneyIcon from '@mui/icons-material/AttachMoney';
import CalendarTodayIcon from '@mui/icons-material/CalendarToday';
import PersonIcon from '@mui/icons-material/Person';

const AppraisalDetailsDialog = ({ open, onClose, appraisal }) => {
  if (!appraisal) return null;

  const property = appraisal.property || {};

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>
        <Box display="flex" justifyContent="space-between" alignItems="center">
          <Typography variant="h5">Appraisal Details</Typography>
          <StatusBadge status={appraisal.status} />
        </Box>
      </DialogTitle>
      
      <DialogContent dividers>
        {/* Property Information */}
        <Box mb={3}>
          <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <HomeIcon color="primary" />
            Property Information
          </Typography>
          <Divider sx={{ mb: 2 }} />
          
          <Grid container spacing={2}>
            <Grid item xs={12}>
              <Box display="flex" alignItems="center" gap={1} mb={1}>
                <LocationOnIcon fontSize="small" color="action" />
                <Typography variant="body1" fontWeight="bold">
                  {property.addressLine1 || 'N/A'}
                </Typography>
              </Box>
              {property.addressLine2 && (
                <Typography variant="body2" color="text.secondary" ml={4}>
                  {property.addressLine2}
                </Typography>
              )}
              <Typography variant="body2" color="text.secondary" ml={4}>
                {property.city}, {property.stateProvince} {property.zipPostalCode}
              </Typography>
              <Typography variant="body2" color="text.secondary" ml={4}>
                {property.country}
              </Typography>
            </Grid>
            
            <Grid item xs={12} sm={6}>
              <Typography variant="body2" color="text.secondary">APN</Typography>
              <Typography variant="body1">{property.apn || 'N/A'}</Typography>
            </Grid>
            
            <Grid item xs={12} sm={6}>
              <Typography variant="body2" color="text.secondary">Property Type</Typography>
              <Typography variant="body1">{property.propertyType || 'N/A'}</Typography>
            </Grid>
            
            {property.squareFeet && (
              <Grid item xs={12} sm={6}>
                <Typography variant="body2" color="text.secondary">Square Feet</Typography>
                <Typography variant="body1">{property.squareFeet.toLocaleString()} sq ft</Typography>
              </Grid>
            )}
            
            {property.yearBuilt && (
              <Grid item xs={12} sm={6}>
                <Typography variant="body2" color="text.secondary">Year Built</Typography>
                <Typography variant="body1">{property.yearBuilt}</Typography>
              </Grid>
            )}
            
            {property.lotSize && (
              <Grid item xs={12} sm={6}>
                <Typography variant="body2" color="text.secondary">Lot Size</Typography>
                <Typography variant="body1">{property.lotSize.toLocaleString()} sq ft</Typography>
              </Grid>
            )}
            
            {property.bedrooms && (
              <Grid item xs={12} sm={6}>
                <Typography variant="body2" color="text.secondary">Bedrooms</Typography>
                <Typography variant="body1">{property.bedrooms}</Typography>
              </Grid>
            )}
            
            {property.bathrooms && (
              <Grid item xs={12} sm={6}>
                <Typography variant="body2" color="text.secondary">Bathrooms</Typography>
                <Typography variant="body1">{property.bathrooms}</Typography>
              </Grid>
            )}
          </Grid>
        </Box>

        {/* Appraisal Information */}
        <Box mb={3}>
          <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <AttachMoneyIcon color="primary" />
            Appraisal Information
          </Typography>
          <Divider sx={{ mb: 2 }} />
          
          <Grid container spacing={2}>
            <Grid item xs={12} sm={6}>
              <Typography variant="body2" color="text.secondary">Appraisal ID</Typography>
              <Typography variant="body1" fontFamily="monospace">{appraisal.appraisalId}</Typography>
            </Grid>
            
            <Grid item xs={12} sm={6}>
              <Typography variant="body2" color="text.secondary">Status</Typography>
              <Typography variant="body1">{appraisal.status}</Typography>
            </Grid>
            
            <Grid item xs={12} sm={6}>
              <Box display="flex" alignItems="center" gap={1}>
                <AttachMoneyIcon fontSize="small" color="action" />
                <Box>
                  <Typography variant="body2" color="text.secondary">Appraised Value</Typography>
                  <Typography variant="h6" color="primary">
                    {formatCurrency(appraisal.appraisedValue)}
                  </Typography>
                </Box>
              </Box>
            </Grid>
            
            <Grid item xs={12} sm={6}>
              <Box display="flex" alignItems="center" gap={1}>
                <CalendarTodayIcon fontSize="small" color="action" />
                <Box>
                  <Typography variant="body2" color="text.secondary">Report Date</Typography>
                  <Typography variant="body1">
                    {formatDateForDisplay(appraisal.reportDate)}
                  </Typography>
                </Box>
              </Box>
            </Grid>
            
            <Grid item xs={12} sm={6}>
              <Typography variant="body2" color="text.secondary">Purpose</Typography>
              <Typography variant="body1">{appraisal.purpose || 'N/A'}</Typography>
            </Grid>
            
            {appraisal.loanAmount && (
              <Grid item xs={12} sm={6}>
                <Typography variant="body2" color="text.secondary">Loan Amount</Typography>
                <Typography variant="body1">{formatCurrency(appraisal.loanAmount)}</Typography>
              </Grid>
            )}
            
            {appraisal.appraiserName && (
              <Grid item xs={12}>
                <Box display="flex" alignItems="center" gap={1}>
                  <PersonIcon fontSize="small" color="action" />
                  <Box>
                    <Typography variant="body2" color="text.secondary">Appraiser</Typography>
                    <Typography variant="body1">{appraisal.appraiserName}</Typography>
                  </Box>
                </Box>
              </Grid>
            )}
            
            {appraisal.documentCount > 0 && (
              <Grid item xs={12}>
                <Chip 
                  label={`${appraisal.documentCount} Document(s) Attached`}
                  color="info"
                  variant="outlined"
                />
              </Grid>
            )}
          </Grid>
        </Box>

        {/* Additional Details */}
        {(appraisal.notes || appraisal.comments) && (
          <Box>
            <Typography variant="h6" gutterBottom>
              Additional Details
            </Typography>
            <Divider sx={{ mb: 2 }} />
            <Typography variant="body2" color="text.secondary">
              {appraisal.notes || appraisal.comments}
            </Typography>
          </Box>
        )}
      </DialogContent>
      
      <DialogActions>
        <Button onClick={onClose} variant="contained">
          Close
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default AppraisalDetailsDialog;
