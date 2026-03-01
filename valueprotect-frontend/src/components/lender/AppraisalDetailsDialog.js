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
  Alert,
  TextField,
  MenuItem,
  LinearProgress
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
import CloudUploadIcon from '@mui/icons-material/CloudUpload';
import AttachFileIcon from '@mui/icons-material/AttachFile';
import DeleteIcon from '@mui/icons-material/Delete';
import { appraisalService } from '../../services/appraisalService';
import { DOCUMENT_TYPES, DOCUMENT_TYPE_LABELS, USER_ROLES } from '../../utils/constants';
import { useAuth } from '../../context/AuthContext';

const AppraisalDetailsDialog = ({ open, onClose, appraisal }) => {
  const { user } = useAuth();
  console.log('🔍 DEBUG - Current user in AppraisalDetailsDialog:', user);
  console.log('🔍 DEBUG - User role:', user?.role);
  const [documents, setDocuments] = useState([]);
  const [documentsLoading, setDocumentsLoading] = useState(false);
  const [documentsError, setDocumentsError] = useState('');
  
  // File upload states
  const [selectedFiles, setSelectedFiles] = useState([]);
  const [uploadLoading, setUploadLoading] = useState(false);
  const [uploadProgress, setUploadProgress] = useState(0);
  const [uploadError, setUploadError] = useState('');

  // Load documents when dialog opens and appraisal changes
  useEffect(() => {
    if (open && appraisal?.appraisalId) {
      loadDocuments();
    }
    // Clear documents when dialog closes
    if (!open) {
      setDocuments([]);
      setDocumentsError('');
      setSelectedFiles([]);
      setUploadError('');
      setUploadProgress(0);
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

  const handleDownloadDocument = async (documentItem) => {
    if (!documentItem?.documentId) {
      console.error('Document ID is missing');
      return;
    }

    try {
      console.log('Downloading document:', documentItem.documentId, documentItem.fileName);
      const response = await appraisalService.downloadDocumentById(documentItem.documentId);
      
      if (!response || !response.data) {
        throw new Error('No document data received');
      }
      
      // Create blob and download
      const contentType = response.headers?.['content-type'] || 'application/octet-stream';
      const blob = new Blob([response.data], { type: contentType });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', documentItem.originalFileName || documentItem.fileName || 'document');
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

  const handleDeleteDocument = async (documentItem) => {
    if (!documentItem?.documentId) {
      console.error('Document ID is missing');
      return;
    }

    // Confirm deletion
    const confirmed = window.confirm(
      `Are you sure you want to delete "${documentItem.fileName || 'this document'}"? This action cannot be undone.`
    );
    
    if (!confirmed) {
      return;
    }

    try {
      console.log('Deleting document:', documentItem.documentId, documentItem.fileName);
      await appraisalService.deleteDocument(documentItem.documentId);
      
      // Reload documents list after successful deletion
      await loadDocuments();
      
      console.log('Document deleted successfully');
    } catch (err) {
      console.error('Failed to delete document:', err);
      const errorMsg = err.response?.status === 404 
        ? 'Document not found or already deleted' 
        : err.response?.data?.message || err.message || 'Failed to delete document';
      alert(errorMsg);
    }
  };

  const handleFileSelect = (e) => {
    const files = Array.from(e.target.files);
    const allowedTypes = [
      'application/pdf',
      'application/msword',
      'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
      'application/vnd.ms-excel',
      'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      'image/jpeg',
      'image/png',
      'image/gif',
      'image/jpg'
    ];
    const maxSizeMB = 10;
    const maxSizeBytes = maxSizeMB * 1024 * 1024;
    
    const validFiles = [];
    const errors = [];
    
    files.forEach(file => {
      // Check file size
      if (file.size > maxSizeBytes) {
        errors.push(`${file.name}: File size (${(file.size / 1024 / 1024).toFixed(2)}MB) exceeds the ${maxSizeMB}MB limit`);
        return;
      }
      
      // Check file type
      if (!allowedTypes.includes(file.type)) {
        errors.push(`${file.name}: File type '${file.type}' is not supported. Please use PDF, Word, Excel, or image files.`);
        return;
      }
      
      validFiles.push(file);
    });
    
    if (errors.length > 0) {
      setUploadError(errors.join('\\n'));
    } else {
      setUploadError(''); // Clear any previous errors
    }
    
    if (validFiles.length > 0) {
      const newFiles = validFiles.map(file => ({
        file,
        documentType: DOCUMENT_TYPES.OTHER,
        id: Math.random().toString(36).substr(2, 9)
      }));
      setSelectedFiles([...selectedFiles, ...newFiles]);
    }
    
    // Clear the input
    e.target.value = '';
  };

  const handleDocumentTypeChange = (fileId, documentType) => {
    setSelectedFiles(prevFiles =>
      prevFiles.map(fileItem =>
        fileItem.id === fileId ? { ...fileItem, documentType } : fileItem
      )
    );
  };

  const handleRemoveFile = (fileId) => {
    setSelectedFiles(prevFiles => prevFiles.filter(fileItem => fileItem.id !== fileId));
  };

  const handleUploadDocuments = async () => {
    if (selectedFiles.length === 0) return;

    setUploadLoading(true);
    setUploadProgress(0);
    setUploadError('');

    try {
      for (let i = 0; i < selectedFiles.length; i++) {
        const fileItem = selectedFiles[i];
        setUploadProgress(((i + 1) / selectedFiles.length) * 100);
        
        await appraisalService.uploadDocument(
          appraisal.appraisalId,
          fileItem.file,
          fileItem.documentType
        );
      }

      // Clear selected files and reload documents
      setSelectedFiles([]);
      await loadDocuments();
      
      setUploadProgress(100);
      
    } catch (err) {
      setUploadError(err.response?.data?.message || 'Failed to upload documents');
    } finally {
      setUploadLoading(false);
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
          <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1, justifyContent: 'space-between' }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <FolderIcon color="primary" />
              Supporting Documents ({documents.length + selectedFiles.length})
            </Box>
            <Button
              variant="outlined"
              component="label"
              startIcon={<AttachFileIcon />}
              size="small"
              disabled={uploadLoading}
            >
              Add Files
              <input
                type="file"
                hidden
                multiple
                accept=".pdf,.jpg,.jpeg,.png,.doc,.docx,.xls,.xlsx"
                onChange={handleFileSelect}
              />
            </Button>
          </Typography>
          <Divider sx={{ mb: 2 }} />

          {/* Upload Error Alert */}
          {uploadError && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {uploadError}
            </Alert>
          )}

          {/* Selected Files for Upload */}
          {selectedFiles.length > 0 && (
            <Box mb={2}>
              <Typography variant="subtitle2" gutterBottom>
                Files to Upload ({selectedFiles.length})
              </Typography>
              {selectedFiles.map((fileItem) => (
                <Box key={fileItem.id} sx={{ border: 1, borderColor: 'divider', borderRadius: 1, p: 2, mb: 1 }}>
                  <Grid container spacing={2} alignItems="center">
                    <Grid item xs={12} sm={4}>
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
                        disabled={uploadLoading}
                      >
                        {Object.entries(DOCUMENT_TYPES).map(([key, value]) => (
                          <MenuItem key={value} value={value}>
                            {DOCUMENT_TYPE_LABELS[value]}
                          </MenuItem>
                        ))}
                      </TextField>
                    </Grid>
                    <Grid item xs={12} sm={2}>
                      <IconButton
                        size="small"
                        onClick={() => handleRemoveFile(fileItem.id)}
                        disabled={uploadLoading}
                      >
                        <DeleteIcon />
                      </IconButton>
                    </Grid>
                  </Grid>
                </Box>
              ))}
              
              {/* Upload Progress */}
              {uploadLoading && (
                <Box sx={{ mb: 2 }}>
                  <LinearProgress 
                    variant="determinate" 
                    value={uploadProgress} 
                    sx={{ mb: 1 }}
                  />
                  <Typography variant="body2" color="textSecondary" textAlign="center">
                    Uploading files... {Math.round(uploadProgress)}%
                  </Typography>
                </Box>
              )}
              
              <Box sx={{ display: 'flex', gap: 1, justifyContent: 'flex-end' }}>
                <Button
                  variant="contained"
                  onClick={handleUploadDocuments}
                  disabled={uploadLoading || selectedFiles.length === 0}
                  startIcon={<CloudUploadIcon />}
                >
                  Upload Documents
                </Button>
              </Box>
            </Box>
          )}

          {/* Existing Documents */}
          {documentsLoading ? (
            <Box display="flex" justifyContent="center" py={2}>
              <CircularProgress size={24} />
            </Box>
          ) : documentsError ? (
            <Alert severity="error" sx={{ mb: 2 }}>
              {documentsError}
            </Alert>
          ) : documents.length === 0 && selectedFiles.length === 0 ? (
            <Typography variant="body2" color="text.secondary" textAlign="center" py={2}>
              No supporting documents found. Use the "Add Files" button to upload documents.
            </Typography>
          ) : documents.length > 0 && (
            <>
              <Typography variant="subtitle2" gutterBottom>
                Uploaded Documents ({documents.length})
              </Typography>
              <List dense>
                {documents.map((documentItem, index) => (
                  <ListItem key={documentItem.documentId || index} divider>
                    <ListItemIcon>
                      <DescriptionIcon color="primary" />
                    </ListItemIcon>
                    <ListItemText
                      primary={documentItem.originalFileName || documentItem.fileName}
                      secondary={
                        <Box>
                          <Typography variant="body2" color="text.secondary">
                            Type: {getDocumentTypeLabel(documentItem.documentType)}
                          </Typography>
                          {documentItem.uploadedAt && (
                            <Typography variant="body2" color="text.secondary">
                              Uploaded: {formatDateForDisplay(documentItem.uploadedAt)}
                            </Typography>
                          )}
                          {documentItem.fileSize && (
                            <Typography variant="body2" color="text.secondary">
                              Size: {(documentItem.fileSize / 1024 / 1024).toFixed(2)} MB
                            </Typography>
                          )}
                        </Box>
                      }
                    />
                    <ListItemSecondaryAction>
                      <Box sx={{ display: 'flex', gap: 0.5 }}>
                        <IconButton
                          onClick={() => handleDownloadDocument(documentItem)}
                          title="Download document"
                          size="small"
                        >
                          <DownloadIcon />
                        </IconButton>
                        {user?.roles?.[0] === 'LENDER' && (
                          <IconButton
                            onClick={() => handleDeleteDocument(documentItem)}
                            title="Delete document"
                            size="small"
                            color="error"
                          >
                            <DeleteIcon />
                          </IconButton>
                        )}
                      </Box>
                    </ListItemSecondaryAction>
                  </ListItem>
                ))}
              </List>
            </>
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
