import React from 'react';
import { Box, Typography } from '@mui/material';
import AppraisalCard from '../common/AppraisalCard';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import UploadFileIcon from '@mui/icons-material/UploadFile';
import FolderOpenIcon from '@mui/icons-material/FolderOpen';

const AppraiserAppraisalList = ({ appraisals, onAccept, onUpload, onViewDocuments, emptyMessage }) => {
  if (!appraisals || appraisals.length === 0) {
    return (
      <Box display="flex" justifyContent="center" py={4}>
        <Typography variant="body1" color="text.secondary">
          {emptyMessage || 'No appraisals found'}
        </Typography>
      </Box>
    );
  }

  const getActionsForAppraisal = (appraisal) => {
    const actions = [];

    if (onAccept) {
      actions.push({
        label: 'Accept Request',
        onClick: onAccept,
        icon: <CheckCircleIcon />,
        color: 'success'
      });
    }

    if (onUpload) {
      actions.push({
        label: 'Upload Document',
        onClick: onUpload,
        icon: <UploadFileIcon />,
        color: 'primary'
      });
    }

    if (onViewDocuments && appraisal.documentCount > 0) {
      actions.push({
        label: 'View Documents',
        onClick: onViewDocuments,
        icon: <FolderOpenIcon />,
        variant: 'outlined',
        color: 'secondary'
      });
    }

    return actions;
  };

  return (
    <Box>
      {appraisals.map((appraisal) => (
        <AppraisalCard
          key={appraisal.appraisalId}
          appraisal={appraisal}
          actions={getActionsForAppraisal(appraisal)}
        />
      ))}
    </Box>
  );
};

export default AppraiserAppraisalList;
