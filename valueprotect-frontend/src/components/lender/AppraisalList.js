import React from 'react';
import { Box, Typography } from '@mui/material';
import AppraisalCard from '../common/AppraisalCard';
import VisibilityIcon from '@mui/icons-material/Visibility';
import DownloadIcon from '@mui/icons-material/Download';

const AppraisalList = ({ appraisals, onViewDetails, onDownloadDocuments, emptyMessage }) => {
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
    const actions = [
      {
        label: 'View Details',
        onClick: onViewDetails,
        icon: <VisibilityIcon />,
        variant: 'outlined'
      }
    ];

    if (onDownloadDocuments && appraisal.documentCount > 0) {
      actions.push({
        label: 'Documents',
        onClick: onDownloadDocuments,
        icon: <DownloadIcon />,
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

export default AppraisalList;
