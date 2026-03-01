import React, { useState, useEffect } from 'react';
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
  Chip,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  ListItemSecondaryAction,
  IconButton,
  CircularProgress,
  Alert
} from '@mui/material';
import { formatDateForDisplay, formatCurrency } from '../../utils/helpers';
import StatusBadge from '../common/StatusBadge';
import LocationOnIcon from '@mui/icons-material/LocationOn';
import HomeIcon from '@mui/icons-material/Home';
import AttachMoneyIcon from '@mui/icons-material/AttachMoney';
import CalendarTodayIcon from '@mui/icons-material/CalendarToday';
import PersonIcon from '@mui/icons-material/Person';
import DescriptionIcon from '@mui/icons-material/Description';
import DownloadIcon from '@mui/icons-material/Download';
import FolderIcon from '@mui/icons-material/Folder';
import { appraisalService } from '../../services/appraisalService';

const AppraisalDetailsDialog = ({ open, onClose, appraisal }) => {
  const [documents, setDocuments] = useState([]);
  const [documentsLoading, setDocumentsLoading] = useState(false);
  const [documentsError, setDocumentsError] = useState('');

  // Load documents when dialog opens and appraisal changes
  useEffect(() => {
    if (open && appraisal?.appraisalId) {
      loadDocuments();
    }
    // Clear documents when dialog closes
    if (!open) {
      setDocuments([]);
      setDocumentsError('');
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [open, appraisal?.appraisalId]);

  // Handle null appraisal case after hooks are defined
  if (!appraisal) return null;

  const property = appraisal.property || {};

  const loadDocuments = async () => {
    if (!appraisal.appraisalId) return;
    
    setDocumentsLoading(true);
    setDocumentsError('');
    try {
      const response = await appraisalService.getAppraisalDocuments(appraisal.appraisalId);
      setDocuments(response.data || []);
    } catch (err) {
      setDocumentsError(err.response?.data?.message || 'Failed to load documents');
      setDocuments([]);
    } finally {
      setDocumentsLoading(false);
    }
  };

  const handleDownloadDocument = async (document) => {
    if (!document?.documentId) {
      console.error('Document ID is missing');
      return;
    }

    try {
      console.log('Downloading document:', document.documentId, document.fileName);
      const response = await appraisalService.downloadDocumentById(document.documentId);
      
      if (!response || !response.data) {
        throw new Error('No document data received');
      }
      
      // Create blob and download
      const contentType = response.headers?.['content-type'] || 'application/octet-stream';
      const blob = new Blob([response.data], { type: contentType });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', document.originalFileName || document.fileName || 'document');
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch (err) {
      console.error('Failed to download document:', err);
      const errorMsg = err.response?.status === 404 
        ? 'Document file not found on server' 
        : err.response?.data?.message || err.message || 'Failed to download document';
      // You could set a local error state here if needed
      alert(errorMsg); // Temporary solution for error display
    }
  };

  const getDocumentTypeLabel = (type) => {
    const labels = {
      'APPRAISAL_REPORT': 'Appraisal Report',
      'TITLE_DEED': 'Title Deed',
      'FLOOR_PLAN': 'Floor Plan',
      'PLAT_MAP': 'Plat Map',
      'PROPERTY_PHOTO': 'Property Photo',
      'TAX_RECORD': 'Tax Record',
      'OTHER': 'Other'
    };
    return labels[type] || type;
  };

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
              <Grid item xs={12} sm={6}>
                <Box display="flex" alignItems="center" gap={1}>
                  <PersonIcon fontSize="small" color="action" />
                  <Box>
                    <Typography variant="body2" color="text.secondary">Appraiser</Typography>
                    <Typography variant="body1">{appraisal.appraiserName}</Typography>
                  </Box>
                </Box>
              </Grid>
            )}
            
            {appraisal.lenderName && (
              <Grid item xs={12} sm={6}>
                <Box display="flex" alignItems="center" gap={1}>
                  <PersonIcon fontSize="small" color="action" />
                  <Box>
                    <Typography variant="body2" color="text.secondary">Lender</Typography>
                    <Typography variant="body1">{appraisal.lenderName}</Typography>
                  </Box>
                </Box>
              </Grid>
            )}
            
            {(appraisal.documentCount > 0 || documents.length > 0) && (
              <Grid item xs={12}>
                <Chip 
                  label={`${Math.max(appraisal.documentCount || 0, documents.length)} Document(s) Attached`}
                  color="info"
                  variant="outlined"
                />
              </Grid>
            )}
          </Grid>
        </Box>

        {/* Supporting Documents */}
        <Box mb={3}>
          <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <FolderIcon color="primary" />
            Supporting Documents ({documents.length})
          </Typography>
          <Divider sx={{ mb: 2 }} />
          
          {documentsLoading ? (
            <Box display="flex" justifyContent="center" py={2}>
              <CircularProgress size={24} />
            </Box>
          ) : documentsError ? (
            <Alert severity="error" sx={{ mb: 2 }}>
              {documentsError}
            </Alert>
          ) : documents.length === 0 ? (
            <Typography variant="body2" color="text.secondary" textAlign="center" py={2}>
              No supporting documents found
            </Typography>
          ) : (
            <List dense>
              {documents.map((document, index) => (
                <ListItem key={document.documentId || index} divider>
                  <ListItemIcon>
                    <DescriptionIcon color="primary" />
                  </ListItemIcon>
                  <ListItemText
                    primary={document.originalFileName || document.fileName}
                    secondary={
                      <Box>
                        <Typography variant="body2" color="text.secondary">
                          Type: {getDocumentTypeLabel(document.documentType)}
                        </Typography>
                        {document.uploadedAt && (
                          <Typography variant="body2" color="text.secondary">
                            Uploaded: {formatDateForDisplay(document.uploadedAt)}
                          </Typography>
                        )}
                        {document.fileSize && (
                          <Typography variant="body2" color="text.secondary">
                            Size: {(document.fileSize / 1024 / 1024).toFixed(2)} MB
                          </Typography>
                        )}
                      </Box>
                    }
                  />
                  <ListItemSecondaryAction>
                    <IconButton
                      edge="end"
                      onClick={() => handleDownloadDocument(document)}
                      title="Download document"
                      size="small"
                    >
                      <DownloadIcon />
                    </IconButton>
                  </ListItemSecondaryAction>
                </ListItem>
              ))}
            </List>
          )}
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
