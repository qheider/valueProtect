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
  Box,
  Typography,
  IconButton,
  Paper,
  Chip,
  LinearProgress
} from '@mui/material';
import { Delete as DeleteIcon, AttachFile as AttachFileIcon } from '@mui/icons-material';
import { appraisalService } from '../../services/appraisalService';
import { PROPERTY_TYPES, APPRAISAL_STATUS, DOCUMENT_TYPES, DOCUMENT_TYPE_LABELS } from '../../utils/constants';
import { formatDateForAPI } from '../../utils/helpers';

const CreateAppraisalForm = ({ onSuccess, onCancel }) => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [uploadProgress, setUploadProgress] = useState(null);
  const [selectedFiles, setSelectedFiles] = useState([]);
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
    livingAreaSqft: '',
    // Property features
    bedroomCount: '',
    bathroomCount: '',
    garageSpaces: '',
    basementType: '',
    hvacType: '',
    exteriorMaterial: '',
    conditionRating: '',
    qualityRating: '',
    // Appraisal fields
    effectiveDate: formatDateForAPI(new Date()),
    reportDate: formatDateForAPI(new Date()),
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

  const handleFileSelect = (e) => {
    const files = Array.from(e.target.files);
    const newFiles = files.map(file => ({
      file,
      documentType: DOCUMENT_TYPES.OTHER,
      id: Math.random().toString(36).substr(2, 9)
    }));
    setSelectedFiles([...selectedFiles, ...newFiles]);
    e.target.value = ''; // Reset input
  };

  const handleDocumentTypeChange = (fileId, documentType) => {
    setSelectedFiles(selectedFiles.map(f => 
      f.id === fileId ? { ...f, documentType } : f
    ));
  };

  const handleRemoveFile = (fileId) => {
    setSelectedFiles(selectedFiles.filter(f => f.id !== fileId));
  };

  const uploadFiles = async (appraisalId) => {
    if (selectedFiles.length === 0) return;

    setUploadProgress({ current: 0, total: selectedFiles.length });
    
    for (let i = 0; i < selectedFiles.length; i++) {
      const { file, documentType } = selectedFiles[i];
      try {
        await appraisalService.uploadDocument(appraisalId, file, documentType);
        setUploadProgress({ current: i + 1, total: selectedFiles.length });
      } catch (err) {
        console.error('Failed to upload file:', file.name, err);
        // Continue with other files even if one fails
      }
    }
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

      if (!formData.purpose) {
        setError('Please fill in the purpose field');
        setLoading(false);
        return;
      }

      const requestData = {
        property: {
          newProperty: {
            apn: formData.apn,
            addressLine1: formData.addressLine1,
            city: formData.city,
            stateProvince: formData.stateProvince,
            zipPostalCode: formData.zipPostalCode,
            propertyType: formData.propertyType,
            yearBuilt: formData.yearBuilt ? parseInt(formData.yearBuilt) : null,
            lotSizeSqft: formData.lotSizeSqft ? parseFloat(formData.lotSizeSqft) : null,
            livingAreaSqft: formData.livingAreaSqft ? parseFloat(formData.livingAreaSqft) : null,
            features: {
              bedroomCount: formData.bedroomCount ? parseInt(formData.bedroomCount) : null,
              bathroomCount: formData.bathroomCount ? parseFloat(formData.bathroomCount) : null,
              garageSpaces: formData.garageSpaces ? parseInt(formData.garageSpaces) : null,
              basementType: formData.basementType || null,
              hvacType: formData.hvacType || null,
              exteriorMaterial: formData.exteriorMaterial || null,
              conditionRating: formData.conditionRating || null,
              qualityRating: formData.qualityRating || null
            }
          }
        },
        effectiveDate: formData.effectiveDate,
        reportDate: formData.reportDate,
        purpose: formData.purpose,
        status: formData.status
      };

      // Create appraisal
      const response = await appraisalService.createAppraisal(requestData);
      const appraisalId = response.data.appraisalId;

      // Upload documents if any selected
      await uploadFiles(appraisalId);

      onSuccess();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create appraisal');
    } finally {
      setLoading(false);
      setUploadProgress(null);
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
                label="Living Area (sqft)"
                name="livingAreaSqft"
                type="number"
                value={formData.livingAreaSqft}
                onChange={handleChange}
                disabled={loading}
              />
            </Grid>

            {/* Property Features */}
            <Grid item xs={12}>
              <Typography variant="subtitle2" sx={{ mt: 2, mb: 1, fontWeight: 600 }}>
                Property Features (Optional)
              </Typography>
            </Grid>

            <Grid item xs={6}>
              <TextField
                fullWidth
                label="Bedrooms"
                name="bedroomCount"
                type="number"
                value={formData.bedroomCount}
                onChange={handleChange}
                disabled={loading}
              />
            </Grid>

            <Grid item xs={6}>
              <TextField
                fullWidth
                label="Bathrooms"
                name="bathroomCount"
                type="number"
                inputProps={{ step: 0.5 }}
                value={formData.bathroomCount}
                onChange={handleChange}
                disabled={loading}
              />
            </Grid>

            <Grid item xs={6}>
              <TextField
                fullWidth
                label="Garage Spaces"
                name="garageSpaces"
                type="number"
                value={formData.garageSpaces}
                onChange={handleChange}
                disabled={loading}
              />
            </Grid>

            <Grid item xs={6}>
              <TextField
                fullWidth
                label="Basement Type"
                name="basementType"
                placeholder="e.g., Full, Partial, None"
                value={formData.basementType}
                onChange={handleChange}
                disabled={loading}
              />
            </Grid>

            <Grid item xs={6}>
              <TextField
                fullWidth
                label="HVAC Type"
                name="hvacType"
                placeholder="e.g., Central Air, Heat Pump"
                value={formData.hvacType}
                onChange={handleChange}
                disabled={loading}
              />
            </Grid>

            <Grid item xs={6}>
              <TextField
                fullWidth
                label="Exterior Material"
                name="exteriorMaterial"
                placeholder="e.g., Brick, Vinyl, Wood"
                value={formData.exteriorMaterial}
                onChange={handleChange}
                disabled={loading}
              />
            </Grid>

            <Grid item xs={6}>
              <TextField
                select
                fullWidth
                label="Condition Rating"
                name="conditionRating"
                value={formData.conditionRating}
                onChange={handleChange}
                disabled={loading}
              >
                <MenuItem value="">None</MenuItem>
                <MenuItem value="C1">C1 - Excellent</MenuItem>
                <MenuItem value="C2">C2 - Very Good</MenuItem>
                <MenuItem value="C3">C3 - Good</MenuItem>
                <MenuItem value="C4">C4 - Average</MenuItem>
                <MenuItem value="C5">C5 - Fair</MenuItem>
                <MenuItem value="C6">C6 - Poor</MenuItem>
              </TextField>
            </Grid>

            <Grid item xs={6}>
              <TextField
                select
                fullWidth
                label="Quality Rating"
                name="qualityRating"
                value={formData.qualityRating}
                onChange={handleChange}
                disabled={loading}
              >
                <MenuItem value="">None</MenuItem>
                <MenuItem value="Q1">Q1 - Excellent</MenuItem>
                <MenuItem value="Q2">Q2 - Very Good</MenuItem>
                <MenuItem value="Q3">Q3 - Good</MenuItem>
                <MenuItem value="Q4">Q4 - Average</MenuItem>
                <MenuItem value="Q5">Q5 - Fair</MenuItem>
                <MenuItem value="Q6">Q6 - Poor</MenuItem>
              </TextField>
            </Grid>

            {/* Appraisal Information */}
            <Grid item xs={12}>
              <Typography variant="subtitle2" sx={{ mt: 2, mb: 1, fontWeight: 600 }}>
                Appraisal Details
              </Typography>
            </Grid>
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

            {/* File Upload Section */}
            <Grid item xs={12}>
              <Typography variant="subtitle1" gutterBottom sx={{ mt: 2 }}>
                Supporting Documents (Optional)
              </Typography>
              
              <Button
                variant="outlined"
                component="label"
                startIcon={<AttachFileIcon />}
                disabled={loading}
                fullWidth
              >
                Attach Files
                <input
                  type="file"
                  hidden
                  multiple
                  accept=".pdf,.jpg,.jpeg,.png,.doc,.docx,.xls,.xlsx"
                  onChange={handleFileSelect}
                />
              </Button>

              {selectedFiles.length > 0 && (
                <Box sx={{ mt: 2 }}>
                  {selectedFiles.map((fileItem) => (
                    <Paper key={fileItem.id} sx={{ p: 2, mb: 1 }}>
                      <Grid container spacing={2} alignItems="center">
                        <Grid item xs={12} sm={5}>
                          <Typography variant="body2" noWrap>
                            {fileItem.file.name}
                          </Typography>
                          <Typography variant="caption" color="textSecondary">
                            {(fileItem.file.size / 1024).toFixed(2)} KB
                          </Typography>
                        </Grid>
                        <Grid item xs={12} sm={6}>
                          <TextField
                            select
                            fullWidth
                            size="small"
                            label="Document Type"
                            value={fileItem.documentType}
                            onChange={(e) => handleDocumentTypeChange(fileItem.id, e.target.value)}
                            disabled={loading}
                          >
                            {Object.entries(DOCUMENT_TYPES).map(([key, value]) => (
                              <MenuItem key={value} value={value}>
                                {DOCUMENT_TYPE_LABELS[value]}
                              </MenuItem>
                            ))}
                          </TextField>
                        </Grid>
                        <Grid item xs={12} sm={1}>
                          <IconButton
                            size="small"
                            onClick={() => handleRemoveFile(fileItem.id)}
                            disabled={loading}
                            color="error"
                          >
                            <DeleteIcon />
                          </IconButton>
                        </Grid>
                      </Grid>
                    </Paper>
                  ))}
                </Box>
              )}
            </Grid>

            {uploadProgress && (
              <Grid item xs={12}>
                <Box sx={{ width: '100%' }}>
                  <Typography variant="body2" color="textSecondary" gutterBottom>
                    Uploading documents: {uploadProgress.current} of {uploadProgress.total}
                  </Typography>
                  <LinearProgress 
                    variant="determinate" 
                    value={(uploadProgress.current / uploadProgress.total) * 100} 
                  />
                </Box>
              </Grid>
            )}
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
