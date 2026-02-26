import React, { useState } from 'react';
import {
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Alert,
  CircularProgress,
  Box,
  Typography,
  MenuItem,
  TextField,
  LinearProgress
} from '@mui/material';
import CloudUploadIcon from '@mui/icons-material/CloudUpload';
import { appraisalService } from '../../services/appraisalService';
import { DOCUMENT_TYPES, DOCUMENT_TYPE_LABELS, APPRAISAL_STATUS } from '../../utils/constants';

const UploadDocumentForm = ({ appraisal, onSuccess, onCancel }) => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [selectedFile, setSelectedFile] = useState(null);
  const [documentType, setDocumentType] = useState(DOCUMENT_TYPES.OTHER);
  const [uploadProgress, setUploadProgress] = useState(0);

  const handleFileChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      // Validate file size (max 10MB)
      if (file.size > 10 * 1024 * 1024) {
        setError('File size must be less than 10MB');
        return;
      }

      // Validate file type
      const allowedTypes = [
        'application/pdf',
        'application/msword',
        'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
        'application/vnd.ms-excel',
        'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
        'image/jpeg',
        'image/png',
        'image/gif'
      ];

      if (!allowedTypes.includes(file.type)) {
        setError('Invalid file type. Please upload PDF, Word, Excel, or image files.');
        return;
      }

      setSelectedFile(file);
      setError('');
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!selectedFile) {
      setError('Please select a file to upload');
      return;
    }

    setError('');
    setLoading(true);
    setUploadProgress(0);

    try {
      // Upload the document
      await appraisalService.uploadDocument(
        appraisal.appraisalId,
        selectedFile,
        documentType
      );

      // Update appraisal status to COMPLETED
      await appraisalService.updateAppraisal(appraisal.appraisalId, {
        ...appraisal,
        status: APPRAISAL_STATUS.COMPLETED
      });

      setUploadProgress(100);
      onSuccess();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to upload document');
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <DialogTitle>Upload Appraisal Document</DialogTitle>
      <DialogContent>
        <Box sx={{ mb: 2 }}>
          <Typography variant="body2" color="text.secondary">
            Property: {appraisal.property?.addressLine1}
          </Typography>
          <Typography variant="body2" color="text.secondary">
            APN: {appraisal.property?.apn}
          </Typography>
        </Box>

        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}

        <Box component="form" onSubmit={handleSubmit}>
          <TextField
            select
            fullWidth
            label="Document Type"
            value={documentType}
            onChange={(e) => setDocumentType(e.target.value)}
            disabled={loading}
            sx={{ mb: 2 }}
          >
            {Object.entries(DOCUMENT_TYPES).map(([key, value]) => (
              <MenuItem key={value} value={value}>
                {DOCUMENT_TYPE_LABELS[value]}
              </MenuItem>
            ))}
          </TextField>

          <Box
            sx={{
              border: '2px dashed #ccc',
              borderRadius: 2,
              p: 3,
              textAlign: 'center',
              mb: 2,
              backgroundColor: '#f9f9f9'
            }}
          >
            <input
              accept=".pdf,.doc,.docx,.xls,.xlsx,.jpg,.jpeg,.png,.gif"
              style={{ display: 'none' }}
              id="file-upload"
              type="file"
              onChange={handleFileChange}
              disabled={loading}
            />
            <label htmlFor="file-upload">
              <Button
                variant="outlined"
                component="span"
                startIcon={<CloudUploadIcon />}
                disabled={loading}
              >
                Choose File
              </Button>
            </label>
            
            {selectedFile && (
              <Box sx={{ mt: 2 }}>
                <Typography variant="body2">
                  Selected: {selectedFile.name}
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  Size: {(selectedFile.size / 1024 / 1024).toFixed(2)} MB
                </Typography>
              </Box>
            )}

            {!selectedFile && (
              <Typography variant="caption" display="block" sx={{ mt: 2 }} color="text.secondary">
                Supported formats: PDF, Word, Excel, Images (max 10MB)
              </Typography>
            )}
          </Box>

          {uploadProgress > 0 && uploadProgress < 100 && (
            <Box sx={{ mb: 2 }}>
              <LinearProgress variant="determinate" value={uploadProgress} />
              <Typography variant="caption" color="text.secondary">
                Uploading... {uploadProgress}%
              </Typography>
            </Box>
          )}
        </Box>
      </DialogContent>
      <DialogActions>
        <Button onClick={onCancel} disabled={loading}>
          Cancel
        </Button>
        <Button
          onClick={handleSubmit}
          variant="contained"
          disabled={loading || !selectedFile}
        >
          {loading ? <CircularProgress size={24} /> : 'Upload'}
        </Button>
      </DialogActions>
    </>
  );
};

export default UploadDocumentForm;
