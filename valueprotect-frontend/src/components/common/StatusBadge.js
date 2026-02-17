import React from 'react';
import { Chip } from '@mui/material';
import { STATUS_LABELS, STATUS_COLORS } from '../../utils/constants';

const StatusBadge = ({ status }) => {
  const label = STATUS_LABELS[status] || status;
  const color = STATUS_COLORS[status] || '#757575';

  return (
    <Chip
      label={label}
      size="small"
      sx={{
        backgroundColor: color,
        color: 'white',
        fontWeight: 'bold'
      }}
    />
  );
};

export default StatusBadge;
