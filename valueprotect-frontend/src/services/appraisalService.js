import apiClient from './api';

export const appraisalService = {
  // Create new appraisal request (Lender)
  createAppraisal: (appraisalData) => 
    apiClient.post('/appraisals', appraisalData),

  // Get all appraisals for current user's company
  getAppraisals: () => 
    apiClient.get('/appraisals'),

  // Get appraisals filtered by status
  getAppraisalsByStatus: (status) => 
    apiClient.get('/appraisals', { params: { status } }),

  // Get single appraisal details
  getAppraisalById: (appraisalId) => 
    apiClient.get(`/appraisals/${appraisalId}`),

  // Update appraisal
  updateAppraisal: (appraisalId, appraisalData) => 
    apiClient.put(`/appraisals/${appraisalId}`, appraisalData),

  // Delete appraisal
  deleteAppraisal: (appraisalId) => 
    apiClient.delete(`/appraisals/${appraisalId}`),

  // Get appraisal documents
  getAppraisalDocuments: (appraisalId) => 
    apiClient.get(`/appraisals/${appraisalId}/documents`),

  // Upload appraisal document
  uploadDocument: (appraisalId, file, documentType) => {
    const formData = new FormData();
    formData.append('file', file);
    
    return apiClient.post(
      `/appraisals/${appraisalId}/documents?documentType=${documentType}`, 
      formData,
      {
        headers: { 'Content-Type': 'multipart/form-data' }
      }
    );
  },

  // Download appraisal document
  downloadDocument: (appraisalId, filename) => 
    apiClient.get(`/appraisals/${appraisalId}/documents/download/${filename}`, {
      responseType: 'blob'
    }),

  // Delete document
  deleteDocument: (documentId) => 
    apiClient.delete(`/appraisals/documents/${documentId}`)
};
