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
import AppraiserAppraisalList from './AppraiserAppraisalList';
import UploadDocumentForm from './UploadDocumentForm';
import { appraisalService } from '../../services/appraisalService';
import { APPRAISAL_STATUS } from '../../utils/constants';

const AppraiserDashboard = () => {
  const [activeTab, setActiveTab] = useState(0);
  const [appraisals, setAppraisals] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [selectedAppraisal, setSelectedAppraisal] = useState(null);
  const [openUploadDialog, setOpenUploadDialog] = useState(false);
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
        ...appraisal,
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

  const handleUploadSuccess = () => {
    setOpenUploadDialog(false);
    setSelectedAppraisal(null);
    setRefreshKey(prev => prev + 1);
  };

  const handleViewDocuments = async (appraisal) => {
    try {
      const response = await appraisalService.getAppraisalDocuments(appraisal.appraisalId);
      console.log('Documents:', response.data);
      // TODO: Display documents in a modal or navigate to documents page
    } catch (err) {
      console.error('Failed to load documents:', err);
    }
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
                emptyMessage="No new appraisal requests"
              />
            )}
            {activeTab === 1 && (
              <AppraiserAppraisalList
                appraisals={filterAppraisalsByStatus(APPRAISAL_STATUS.REVIEW)}
                onUpload={handleUploadDocument}
                emptyMessage="No appraisals in progress"
              />
            )}
            {activeTab === 2 && (
              <AppraiserAppraisalList
                appraisals={filterAppraisalsByStatus(APPRAISAL_STATUS.COMPLETED)}
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
      </Container>
    </>
  );
};

export default AppraiserDashboard;
