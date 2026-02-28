import React from 'react';
import {
  Box,
  Button,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  IconButton,
  List,
  ListItem,
  ListItemText,
  Typography
} from '@mui/material';
import DownloadIcon from '@mui/icons-material/Download';
import OpenInNewIcon from '@mui/icons-material/OpenInNew';

const DocumentListDialog = ({
  open,
  onClose,
  title = 'Documents',
  documents = [],
  loading = false,
  onOpenDocument,
  onDownloadDocument
}) => {
  return (
    <Dialog
      open={open}
      onClose={onClose}
      maxWidth="sm"
      fullWidth
    >
      <DialogTitle>{title}</DialogTitle>
      <DialogContent dividers>
        {loading ? (
          <Box display="flex" justifyContent="center" py={2}>
            <CircularProgress size={24} />
          </Box>
        ) : documents.length === 0 ? (
          <Typography variant="body2" color="text.secondary">
            No documents found for this appraisal.
          </Typography>
        ) : (
          <List>
            {documents.map((documentItem) => (
              <ListItem
                key={documentItem.documentId}
                secondaryAction={
                  <Box display="flex" gap={1}>
                    <IconButton
                      edge="end"
                      aria-label="open"
                      onClick={() => onOpenDocument?.(documentItem)}
                    >
                      <OpenInNewIcon fontSize="small" />
                    </IconButton>
                    <IconButton
                      edge="end"
                      aria-label="download"
                      onClick={() => onDownloadDocument?.(documentItem)}
                    >
                      <DownloadIcon fontSize="small" />
                    </IconButton>
                  </Box>
                }
              >
                <ListItemText
                  primary={documentItem.fileName || 'Unnamed document'}
                  secondary={documentItem.documentType || 'Document'}
                />
              </ListItem>
            ))}
          </List>
        )}
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Close</Button>
      </DialogActions>
    </Dialog>
  );
};

export default DocumentListDialog;
