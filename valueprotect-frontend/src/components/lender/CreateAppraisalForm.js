import React, { useState } from 'react';
import {
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Button,
  Grid,
  MenuItem,
  Alert,
  CircularProgress,
  Box
} from '@mui/material';
import { appraisalService } from '../../services/appraisalService';
import { PROPERTY_TYPES, APPRAISAL_STATUS } from '../../utils/constants';
import { formatDateForAPI } from '../../utils/helpers';

const CreateAppraisalForm = ({ onSuccess, onCancel }) => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [formData, setFormData] = useState({
    // Property fields
    apn: '',
    addressLine1: '',
    city: '',
    stateProvince: '',
    zipPostalCode: '',
    propertyType: PROPERTY_TYPES.SINGLE_FAMILY,
    yearBuilt: '',
    lotSizeSqft: '',
    buildingSizeSqft: '',
    // Appraisal fields
    effectiveDate: formatDateForAPI(new Date()),
    reportDate: formatDateForAPI(new Date()),
    appraisedValue: '',
    purpose: '',
    status: APPRAISAL_STATUS.DRAFT
  });

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
    setError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      // Validate required fields
      if (!formData.apn || !formData.addressLine1 || !formData.city || 
          !formData.stateProvince || !formData.zipPostalCode) {
        setError('Please fill in all required property fields');
        setLoading(false);
        return;
      }

      if (!formData.appraisedValue || !formData.purpose) {
        setError('Please fill in all required appraisal fields');
        setLoading(false);
        return;
      }

      const requestData = {
        property: {
          apn: formData.apn,
          addressLine1: formData.addressLine1,
          city: formData.city,
          stateProvince: formData.stateProvince,
          zipPostalCode: formData.zipPostalCode,
          propertyType: formData.propertyType,
          yearBuilt: formData.yearBuilt ? parseInt(formData.yearBuilt) : null,
          lotSizeSqft: formData.lotSizeSqft ? parseFloat(formData.lotSizeSqft) : null,
          buildingSizeSqft: formData.buildingSizeSqft ? parseFloat(formData.buildingSizeSqft) : null
        },
        effectiveDate: formData.effectiveDate,
        reportDate: formData.reportDate,
        appraisedValue: parseFloat(formData.appraisedValue),
        purpose: formData.purpose,
        status: formData.status
      };

      await appraisalService.createAppraisal(requestData);
      onSuccess();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create appraisal');
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <DialogTitle>Create New Appraisal Request</DialogTitle>
      <DialogContent>
        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}

        <Box component="form" onSubmit={handleSubmit}>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            {/* Property Information */}
            <Grid item xs={12}>
              <TextField
                required
                fullWidth
                label="APN (Assessor Parcel Number)"
                name="apn"
                value={formData.apn}
                onChange={handleChange}
                disabled={loading}
              />
            </Grid>

            <Grid item xs={12}>
              <TextField
                required
                fullWidth
                label="Address"
                name="addressLine1"
                value={formData.addressLine1}
                onChange={handleChange}
                disabled={loading}
              />
            </Grid>

            <Grid item xs={4}>
              <TextField
                required
                fullWidth
                label="City"
                name="city"
                value={formData.city}
                onChange={handleChange}
                disabled={loading}
              />
            </Grid>

            <Grid item xs={4}>
              <TextField
                required
                fullWidth
                label="State"
                name="stateProvince"
                value={formData.stateProvince}
                onChange={handleChange}
                disabled={loading}
              />
            </Grid>

            <Grid item xs={4}>
              <TextField
                required
                fullWidth
                label="Zip Code"
                name="zipPostalCode"
                value={formData.zipPostalCode}
                onChange={handleChange}
                disabled={loading}
              />
            </Grid>

            <Grid item xs={6}>
              <TextField
                select
                fullWidth
                label="Property Type"
                name="propertyType"
                value={formData.propertyType}
                onChange={handleChange}
                disabled={loading}
              >
                {Object.entries(PROPERTY_TYPES).map(([key, value]) => (
                  <MenuItem key={value} value={value}>
                    {key.replace(/_/g, ' ')}
                  </MenuItem>
                ))}
              </TextField>
            </Grid>

            <Grid item xs={6}>
              <TextField
                fullWidth
                label="Year Built"
                name="yearBuilt"
                type="number"
                value={formData.yearBuilt}
                onChange={handleChange}
                disabled={loading}
              />
            </Grid>

            <Grid item xs={6}>
              <TextField
                fullWidth
                label="Lot Size (sqft)"
                name="lotSizeSqft"
                type="number"
                value={formData.lotSizeSqft}
                onChange={handleChange}
                disabled={loading}
              />
            </Grid>

            <Grid item xs={6}>
              <TextField
                fullWidth
                label="Building Size (sqft)"
                name="buildingSizeSqft"
                type="number"
                value={formData.buildingSizeSqft}
                onChange={handleChange}
                disabled={loading}
              />
            </Grid>

            {/* Appraisal Information */}
            <Grid item xs={6}>
              <TextField
                required
                fullWidth
                label="Effective Date"
                name="effectiveDate"
                type="date"
                value={formData.effectiveDate}
                onChange={handleChange}
                disabled={loading}
                InputLabelProps={{ shrink: true }}
              />
            </Grid>

            <Grid item xs={6}>
              <TextField
                required
                fullWidth
                label="Report Date"
                name="reportDate"
                type="date"
                value={formData.reportDate}
                onChange={handleChange}
                disabled={loading}
                InputLabelProps={{ shrink: true }}
              />
            </Grid>

            <Grid item xs={12}>
              <TextField
                required
                fullWidth
                label="Appraised Value"
                name="appraisedValue"
                type="number"
                value={formData.appraisedValue}
                onChange={handleChange}
                disabled={loading}
                InputProps={{
                  startAdornment: '$'
                }}
              />
            </Grid>

            <Grid item xs={12}>
              <TextField
                required
                fullWidth
                label="Purpose"
                name="purpose"
                multiline
                rows={3}
                value={formData.purpose}
                onChange={handleChange}
                disabled={loading}
                placeholder="e.g., Purchase, Refinance, Estate Planning"
              />
            </Grid>
          </Grid>
        </Box>
      </DialogContent>
      <DialogActions>
        <Button onClick={onCancel} disabled={loading}>
          Cancel
        </Button>
        <Button
          onClick={handleSubmit}
          variant="contained"
          disabled={loading}
        >
          {loading ? <CircularProgress size={24} /> : 'Create Appraisal'}
        </Button>
      </DialogActions>
    </>
  );
};

export default CreateAppraisalForm;
