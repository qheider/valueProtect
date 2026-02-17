import React from 'react';
import {
  Card,
  CardContent,
  Typography,
  Box,
  Button,
  Chip
} from '@mui/material';
import { formatDateForDisplay, formatCurrency } from '../../utils/helpers';
import StatusBadge from './StatusBadge';
import LocationOnIcon from '@mui/icons-material/LocationOn';
import CalendarTodayIcon from '@mui/icons-material/CalendarToday';
import AttachMoneyIcon from '@mui/icons-material/AttachMoney';

const AppraisalCard = ({ appraisal, actions }) => {
  const property = appraisal.property || {};

  return (
    <Card sx={{ mb: 2, '&:hover': { boxShadow: 3 }, transition: 'box-shadow 0.3s' }}>
      <CardContent>
        <Box display="flex" justifyContent="space-between" alignItems="start" mb={2}>
          <Box>
            <Typography variant="h6" component="div">
              {property.addressLine1 || 'N/A'}
            </Typography>
            <Typography variant="body2" color="text.secondary">
              {property.city}, {property.stateProvince} {property.zipPostalCode}
            </Typography>
          </Box>
          <StatusBadge status={appraisal.status} />
        </Box>

        <Box display="flex" flexDirection="column" gap={1} mb={2}>
          <Box display="flex" alignItems="center" gap={1}>
            <LocationOnIcon fontSize="small" color="action" />
            <Typography variant="body2">
              APN: {property.apn || 'N/A'}
            </Typography>
          </Box>
          
          <Box display="flex" alignItems="center" gap={1}>
            <AttachMoneyIcon fontSize="small" color="action" />
            <Typography variant="body2">
              Value: {formatCurrency(appraisal.appraisedValue)}
            </Typography>
          </Box>

          <Box display="flex" alignItems="center" gap={1}>
            <CalendarTodayIcon fontSize="small" color="action" />
            <Typography variant="body2">
              Report Date: {formatDateForDisplay(appraisal.reportDate)}
            </Typography>
          </Box>

          {appraisal.appraiserName && (
            <Typography variant="body2" color="text.secondary">
              Appraiser: {appraisal.appraiserName}
            </Typography>
          )}

          <Typography variant="body2" color="text.secondary">
            Purpose: {appraisal.purpose || 'N/A'}
          </Typography>

          {appraisal.documentCount > 0 && (
            <Chip 
              label={`${appraisal.documentCount} Document(s)`} 
              size="small" 
              variant="outlined" 
              sx={{ width: 'fit-content' }}
            />
          )}
        </Box>

        {actions && (
          <Box display="flex" gap={1} mt={2}>
            {actions.map((action, index) => (
              <Button
                key={index}
                variant={action.variant || 'contained'}
                color={action.color || 'primary'}
                size="small"
                onClick={() => action.onClick(appraisal)}
                startIcon={action.icon}
              >
                {action.label}
              </Button>
            ))}
          </Box>
        )}
      </CardContent>
    </Card>
  );
};

export default AppraisalCard;
