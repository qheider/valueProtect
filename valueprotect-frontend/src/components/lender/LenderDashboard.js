import React, { useState, useEffect } from 'react';
import {
  Container,
  Box,
  Tabs,
  Tab,
  Button,
  Dialog,
  CircularProgress,
  Alert,
  Typography
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import Navbar from '../common/Navbar';
import AppraisalList from './AppraisalList';
import CreateAppraisalForm from './CreateAppraisalForm';
import { appraisalService } from '../../services/appraisalService';
import { APPRAISAL_STATUS } from '../../utils/constants';

const LenderDashboard = () => {
  const [activeTab, setActiveTab] = useState(0);
  const [appraisals, setAppraisals] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [openCreateDialog, setOpenCreateDialog] = useState(false);
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

  const handleCreateSuccess = () => {
    setOpenCreateDialog(false);
    setRefreshKey(prev => prev + 1);
  };

  const filterAppraisalsByStatus = (status) => {
    return appraisals.filter(appraisal => appraisal.status === status);
  };

  const handleViewDetails = (appraisal) => {
    // TODO: Navigate to details page or open details modal
    console.log('View details:', appraisal);
  };

  const handleDownloadDocuments = async (appraisal) => {
    try {
      const response = await appraisalService.getAppraisalDocuments(appraisal.appraisalId);
      console.log('Documents:', response.data);
      // TODO: Implement document download functionality
    } catch (err) {
      console.error('Failed to load documents:', err);
    }
  };

  return (
    <>
      <Navbar title="ValueProtect - Lender Dashboard" />
      <Container maxWidth="lg" sx={{ mt: 4 }}>
        <Box sx={{ borderBottom: 1, borderColor: 'divider', mb: 3 }}>
          <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
            <Typography variant="h4">Appraisal Requests</Typography>
            <Button
              variant="contained"
              startIcon={<AddIcon />}
              onClick={() => setOpenCreateDialog(true)}
            >
              Create New Appraisal
            </Button>
          </Box>
          
          <Tabs value={activeTab} onChange={handleTabChange}>
            <Tab label={`Draft (${filterAppraisalsByStatus(APPRAISAL_STATUS.DRAFT).length})`} />
            <Tab label={`In Review (${filterAppraisalsByStatus(APPRAISAL_STATUS.REVIEW).length})`} />
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
              <AppraisalList
                appraisals={filterAppraisalsByStatus(APPRAISAL_STATUS.DRAFT)}
                onViewDetails={handleViewDetails}
                emptyMessage="No draft appraisals"
              />
            )}
            {activeTab === 1 && (
              <AppraisalList
                appraisals={filterAppraisalsByStatus(APPRAISAL_STATUS.REVIEW)}
                onViewDetails={handleViewDetails}
                emptyMessage="No appraisals in review"
              />
            )}
            {activeTab === 2 && (
              <AppraisalList
                appraisals={filterAppraisalsByStatus(APPRAISAL_STATUS.COMPLETED)}
                onViewDetails={handleViewDetails}
                onDownloadDocuments={handleDownloadDocuments}
                emptyMessage="No completed appraisals"
              />
            )}
          </>
        )}

        <Dialog
          open={openCreateDialog}
          onClose={() => setOpenCreateDialog(false)}
          maxWidth="md"
          fullWidth
        >
          <CreateAppraisalForm
            onSuccess={handleCreateSuccess}
            onCancel={() => setOpenCreateDialog(false)}
          />
        </Dialog>
      </Container>
    </>
  );
};

export default LenderDashboard;
