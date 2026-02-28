import React, { useState, useEffect } from 'react';
import {
  Container,
  Box,
  Tabs,
  Tab,
  Dialog,
  CircularProgress,
  Alert,
  Typography
} from '@mui/material';
import Navbar from '../common/Navbar';
import DocumentListDialog from '../common/DocumentListDialog';
import AppraiserAppraisalList from './AppraiserAppraisalList';
import UploadDocumentForm from './UploadDocumentForm';
import AppraisalDetailsDialog from '../lender/AppraisalDetailsDialog';
import { appraisalService } from '../../services/appraisalService';
import { APPRAISAL_STATUS } from '../../utils/constants';

const AppraiserDashboard = () => {
  const [activeTab, setActiveTab] = useState(0);
  const [appraisals, setAppraisals] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [selectedAppraisal, setSelectedAppraisal] = useState(null);
  const [selectedDetailsAppraisal, setSelectedDetailsAppraisal] = useState(null);
  const [selectedDocumentsAppraisal, setSelectedDocumentsAppraisal] = useState(null);
  const [documents, setDocuments] = useState([]);
  const [documentsLoading, setDocumentsLoading] = useState(false);
  const [openUploadDialog, setOpenUploadDialog] = useState(false);
  const [openDetailsDialog, setOpenDetailsDialog] = useState(false);
  const [openDocumentsDialog, setOpenDocumentsDialog] = useState(false);
  const [refreshKey, setRefreshKey] = useState(0);

  useEffect(() => {
    fetchAppraisals();
  }, [refreshKey]);

  const fetchAppraisals = async () => {
    setLoading(true);
    setError('');
    try {
      const response = await appraisalService.getAppraisals();
      setAppraisals(response.data || []);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load appraisals');
    } finally {
      setLoading(false);
    }
  };

  const handleTabChange = (event, newValue) => {
    setActiveTab(newValue);
  };

  const handleAcceptRequest = async (appraisal) => {
    try {
      // Update appraisal status to REVIEW (in-progress)
      await appraisalService.updateAppraisal(appraisal.appraisalId, {
        status: APPRAISAL_STATUS.REVIEW
      });
      setRefreshKey(prev => prev + 1);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to accept appraisal');
    }
  };

  const handleUploadDocument = (appraisal) => {
    setSelectedAppraisal(appraisal);
    setOpenUploadDialog(true);
  };

  const handleViewDetails = (appraisal) => {
    setSelectedDetailsAppraisal(appraisal);
    setOpenDetailsDialog(true);
  };

  const handleUploadSuccess = () => {
    setOpenUploadDialog(false);
    setSelectedAppraisal(null);
    setRefreshKey(prev => prev + 1);
  };

  const handleViewDocuments = async (appraisal) => {
    setSelectedDocumentsAppraisal(appraisal);
    setOpenDocumentsDialog(true);
    setDocumentsLoading(true);
    try {
      const response = await appraisalService.getAppraisalDocuments(appraisal.appraisalId);
      setDocuments(response.data || []);
    } catch (err) {
      setDocuments([]);
      setError(err.response?.data?.message || 'Failed to load documents');
    } finally {
      setDocumentsLoading(false);
    }
  };

  const getMimeTypeFromFileName = (fileName) => {
    const extension = (fileName || '').split('.').pop()?.toLowerCase();
    const mimeMap = {
      pdf: 'application/pdf',
      jpg: 'image/jpeg',
      jpeg: 'image/jpeg',
      png: 'image/png',
      gif: 'image/gif',
      webp: 'image/webp',
      doc: 'application/msword',
      docx: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
      xls: 'application/vnd.ms-excel',
      xlsx: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    };

    return mimeMap[extension] || 'application/octet-stream';
  };

  const buildDocumentBlob = (response, fileName) => {
    const serverType = response?.headers?.['content-type'];
    const fallbackType = getMimeTypeFromFileName(fileName);
    const resolvedType = serverType && serverType !== 'application/octet-stream'
      ? serverType
      : fallbackType;

    if (response?.data instanceof Blob) {
      return response.data.type ? response.data : new Blob([response.data], { type: resolvedType });
    }

    return new Blob([response?.data], { type: resolvedType });
  };

  const handleDownloadDocument = async (docItem) => {
    if (!docItem?.documentId) {
      return;
    }

    try {
      const response = await appraisalService.downloadDocumentById(docItem.documentId);
      const fileBlob = buildDocumentBlob(response, docItem.fileName);
      const blobUrl = window.URL.createObjectURL(fileBlob);
      const anchor = document.createElement('a');
      anchor.href = blobUrl;
      anchor.setAttribute('download', docItem.fileName || 'document');
      document.body.appendChild(anchor);
      anchor.click();
      anchor.remove();
      window.URL.revokeObjectURL(blobUrl);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to download document');
    }
  };

  const handleOpenDocument = async (docItem) => {
    if (!docItem?.documentId) {
      return;
    }

    try {
      const response = await appraisalService.downloadDocumentById(docItem.documentId);
      const fileBlob = buildDocumentBlob(response, docItem.fileName);
      const blobUrl = window.URL.createObjectURL(fileBlob);
      window.open(blobUrl, '_blank', 'noopener,noreferrer');
      setTimeout(() => window.URL.revokeObjectURL(blobUrl), 1000);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to open document');
    }
  };

  const closeDocumentsDialog = () => {
    setOpenDocumentsDialog(false);
    setSelectedDocumentsAppraisal(null);
    setDocuments([]);
  };

  const filterAppraisalsByStatus = (status) => {
    return appraisals.filter(appraisal => appraisal.status === status);
  };

  return (
    <>
      <Navbar title="ValueProtect - Appraiser Dashboard" />
      <Container maxWidth="lg" sx={{ mt: 4 }}>
        <Box sx={{ borderBottom: 1, borderColor: 'divider', mb: 3 }}>
          <Typography variant="h4" mb={2}>Appraisal Requests</Typography>
          
          <Tabs value={activeTab} onChange={handleTabChange}>
            <Tab label={`New (${filterAppraisalsByStatus(APPRAISAL_STATUS.DRAFT).length})`} />
            <Tab label={`In Progress (${filterAppraisalsByStatus(APPRAISAL_STATUS.REVIEW).length})`} />
            <Tab label={`Completed (${filterAppraisalsByStatus(APPRAISAL_STATUS.COMPLETED).length})`} />
          </Tabs>
        </Box>

        {error && (
          <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError('')}>
            {error}
          </Alert>
        )}

        {loading ? (
          <Box display="flex" justifyContent="center" py={4}>
            <CircularProgress />
          </Box>
        ) : (
          <>
            {activeTab === 0 && (
              <AppraiserAppraisalList
                appraisals={filterAppraisalsByStatus(APPRAISAL_STATUS.DRAFT)}
                onAccept={handleAcceptRequest}
                onViewDetails={handleViewDetails}
                emptyMessage="No new appraisal requests"
              />
            )}
            {activeTab === 1 && (
              <AppraiserAppraisalList
                appraisals={filterAppraisalsByStatus(APPRAISAL_STATUS.REVIEW)}
                onViewDetails={handleViewDetails}
                onUpload={handleUploadDocument}
                emptyMessage="No appraisals in progress"
              />
            )}
            {activeTab === 2 && (
              <AppraiserAppraisalList
                appraisals={filterAppraisalsByStatus(APPRAISAL_STATUS.COMPLETED)}
                onViewDetails={handleViewDetails}
                onViewDocuments={handleViewDocuments}
                emptyMessage="No completed appraisals"
              />
            )}
          </>
        )}

        <Dialog
          open={openUploadDialog}
          onClose={() => setOpenUploadDialog(false)}
          maxWidth="sm"
          fullWidth
        >
          {selectedAppraisal && (
            <UploadDocumentForm
              appraisal={selectedAppraisal}
              onSuccess={handleUploadSuccess}
              onCancel={() => setOpenUploadDialog(false)}
            />
          )}
        </Dialog>

        <AppraisalDetailsDialog
          open={openDetailsDialog}
          onClose={() => setOpenDetailsDialog(false)}
          appraisal={selectedDetailsAppraisal}
        />

        <DocumentListDialog
          open={openDocumentsDialog}
          onClose={closeDocumentsDialog}
          documents={documents}
          loading={documentsLoading}
          onOpenDocument={handleOpenDocument}
          onDownloadDocument={handleDownloadDocument}
        />
      </Container>
    </>
  );
};

export default AppraiserDashboard;
