'use client';

import * as React from 'react';
import {
  Avatar,
  Box,
  Card,
  CardContent,
  CardHeader,
  Chip,
  IconButton,
  Popover,
  Stack,
  TablePagination,
  Typography,
  useMediaQuery,
  useTheme,
} from '@mui/material';
import { Activity, Info } from '@phosphor-icons/react';

import { dayjs } from '@/lib/dayjs';

const MetadataPopover = ({ metadata, anchorEl, onClose }) => (
  <Popover
    open={Boolean(anchorEl)}
    anchorEl={anchorEl}
    onClose={onClose}
    anchorOrigin={{
      vertical: 'bottom',
      horizontal: 'left',
    }}
    transformOrigin={{
      vertical: 'top',
      horizontal: 'left',
    }}
  >
    <Box sx={{ p: 2, maxWidth: 300 }}>
      <Typography variant="subtitle2" gutterBottom>
        Action Details
      </Typography>
      <pre
        style={{
          margin: 0,
          whiteSpace: 'pre-wrap',
          wordBreak: 'break-word',
          fontSize: '0.875rem',
        }}
      >
        {metadata === null ? 'No metadata available' : JSON.stringify(metadata, null, 2)}
      </pre>
    </Box>
  </Popover>
);

const ActionCard = ({ action, onMetadataClick }) => {
  const getActionColor = (actionType) => {
    const actionLower = actionType.toLowerCase();
    if (actionLower.includes('login')) return 'info';
    if (actionLower.includes('toggle')) return 'warning';
    if (actionLower.includes('create') || actionLower.includes('add')) return 'success';
    if (actionLower.includes('delete') || actionLower.includes('remove')) return 'error';
    return 'default';
  };

  const formatAction = (actionType) => {
    return actionType
      .split('_')
      .map((word) => word.charAt(0) + word.slice(1).toLowerCase())
      .join(' ');
  };

  return (
    <Card
      variant="outlined"
      sx={{
        '&:hover': { bgcolor: 'action.hover' },
        transition: 'background-color 0.2s ease',
      }}
    >
      <CardContent sx={{ py: 1.5, px: 2, '&:last-child': { pb: 1.5 } }}>
        <Stack direction="row" alignItems="center" justifyContent="space-between" spacing={2}>
          <Stack direction="row" alignItems="center" spacing={2}>
            <Chip
              label={formatAction(action.action)}
              color={getActionColor(action.action)}
              size="small"
              sx={{
                fontWeight: 500,
                '& .MuiChip-label': { px: 1.5 },
              }}
            />
            <Typography variant="body2" color="text.secondary" sx={{ whiteSpace: 'nowrap' }}>
              {dayjs(action.timestamp * 1000).format('MMM D, HH:mm')}
            </Typography>
          </Stack>
          <IconButton
            size="small"
            onClick={(e) => onMetadataClick(e, action.metaData)}
            sx={{
              color: 'action.active',
              '&:hover': { color: 'primary.main' },
            }}
          >
            <Info size={18} />
          </IconButton>
        </Stack>
      </CardContent>
    </Card>
  );
};

const UserActionLogsTable = ({ userActions = [] }) => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('sm'));
  const [page, setPage] = React.useState(0);
  const [rowsPerPage] = React.useState(7);
  const [metadataAnchorEl, setMetadataAnchorEl] = React.useState(null);
  const [selectedMetadata, setSelectedMetadata] = React.useState(null);

  const handleMetadataClick = (event, metadata) => {
    setMetadataAnchorEl(event.currentTarget);
    setSelectedMetadata(metadata);
  };

  const handleMetadataClose = () => {
    setMetadataAnchorEl(null);
    setSelectedMetadata(null);
  };

  const paginatedActions = userActions.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage);

  const stats = React.useMemo(
    () => ({
      last24Hours: userActions.filter((action) => Date.now() / 1000 - action.timestamp < 24 * 60 * 60).length,
    }),
    [userActions]
  );

  return (
    <Card>
      <CardHeader
        avatar={
          <Avatar
            sx={{
              bgcolor: 'primary.main',
              width: 40,
              height: 40,
            }}
          >
            <Activity weight="fill" />
          </Avatar>
        }
        title={
          <Typography variant="h6" component="div">
            User Actions
          </Typography>
        }
        subheader={
          <Typography variant="body2" color="text.secondary">
            {stats.last24Hours} actions in the last 24 hours
          </Typography>
        }
        sx={{ pb: 0 }}
      />
      <CardContent>
        <Stack spacing={1.5}>
          {paginatedActions.map((action) => (
            <ActionCard key={action.id} action={action} onMetadataClick={handleMetadataClick} />
          ))}

          <TablePagination
            component="div"
            count={userActions.length}
            page={page}
            onPageChange={(event, newPage) => setPage(newPage)}
            rowsPerPage={rowsPerPage}
            rowsPerPageOptions={[5]}
            sx={{
              borderTop: `1px solid ${theme.palette.divider}`,
              '& .MuiTablePagination-toolbar': {
                minHeight: 52,
              },
            }}
          />
        </Stack>
      </CardContent>

      <MetadataPopover metadata={selectedMetadata} anchorEl={metadataAnchorEl} onClose={handleMetadataClose} />
    </Card>
  );
};

export default UserActionLogsTable;
