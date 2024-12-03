'use client';

import * as React from 'react';
import {
  Avatar,
  Box,
  Card,
  CardContent,
  CardHeader,
  Chip,
  Stack,
  TablePagination,
  Typography,
  useMediaQuery,
  useTheme,
} from '@mui/material';
import { Shield } from '@phosphor-icons/react';

import { dayjs } from '@/lib/dayjs';

const LoginLogsTable = ({ loginAttempts = [] }) => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('sm'));
  const [page, setPage] = React.useState(0);
  const [rowsPerPage] = React.useState(5);

  const handleChangePage = (event, newPage) => {
    setPage(newPage);
  };

  const paginatedAttempts = loginAttempts.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage);

  const getRecentLoginStats = () => {
    const last24Hours = loginAttempts.filter((attempt) => Date.now() / 1000 - attempt.loginTime < 24 * 60 * 60).length;
    const uniqueIPs = new Set(loginAttempts.map((attempt) => attempt.ipAddress)).size;
    return { last24Hours, uniqueIPs };
  };

  const stats = getRecentLoginStats();

  return (
    <Card>
      <CardHeader
        avatar={
          <Avatar sx={{ bgcolor: 'primary.main' }}>
            <Shield weight="fill" />
          </Avatar>
        }
        title="Login Attempts"
        subheader="Recent login activity"
      />
      <CardContent>
        <Stack spacing={3}>
          {/* Stats Card */}
          <Card variant="outlined" sx={{ bgcolor: 'background.default' }}>
            <Stack
              direction={isMobile ? 'column' : 'row'}
              spacing={2}
              sx={{ p: 2 }}
              divider={
                !isMobile ? (
                  <div
                    style={{
                      borderLeft: `1px solid ${theme.palette.divider}`,
                      margin: '0 16px',
                    }}
                  />
                ) : null
              }
            >
              <Box flex={1}>
                <Typography color="text.secondary" variant="overline">
                  Last 24 Hours
                </Typography>
                <Typography variant="h6">{stats.last24Hours} attempts</Typography>
              </Box>
              <Box flex={1}>
                <Typography color="text.secondary" variant="overline">
                  Unique IP Addresses
                </Typography>
                <Typography variant="h6">{stats.uniqueIPs}</Typography>
              </Box>
            </Stack>
          </Card>

          {/* Login Attempts Cards */}
          <Stack spacing={2}>
            {paginatedAttempts.map((attempt) => (
              <Card
                key={attempt.id}
                variant="outlined"
                sx={{
                  '&:hover': {
                    bgcolor: 'action.hover',
                  },
                }}
              >
                <CardContent sx={{ py: 2, '&:last-child': { pb: 2 } }}>
                  <Stack direction="row" alignItems="center" spacing={2}>
                    <Box flex={1}>
                      <Stack direction="row" spacing={1} alignItems="center">
                        <Typography variant="body1" fontWeight={500}>
                          {dayjs(attempt.loginTime * 1000).format('MMM D, YYYY h:mm A')}
                        </Typography>
                      </Stack>
                      <Stack direction="row" spacing={1} alignItems="center" sx={{ mt: 1 }}>
                        <Typography variant="body2" color="text.secondary" sx={{ minWidth: 100 }}>
                          IP Address:
                        </Typography>
                        <Typography variant="body1">{attempt.ipAddress}</Typography>
                      </Stack>
                      <Stack direction="row" spacing={1} alignItems="baseline" sx={{ mt: 0.5 }}>
                        <Typography variant="body2" color="text.secondary" sx={{ minWidth: 100 }}>
                          Device:
                        </Typography>
                        <Typography
                          variant="body2"
                          color="text.primary"
                          sx={{
                            flex: 1,
                            lineHeight: 1.5,
                          }}
                        >
                          {attempt.userAgent}
                        </Typography>
                      </Stack>
                    </Box>
                  </Stack>
                </CardContent>
              </Card>
            ))}
          </Stack>

          <TablePagination
            component="div"
            count={loginAttempts.length}
            page={page}
            onPageChange={handleChangePage}
            rowsPerPage={rowsPerPage}
            rowsPerPageOptions={[5]}
            sx={{
              borderTop: `1px solid ${theme.palette.divider}`,
            }}
          />
        </Stack>
      </CardContent>
    </Card>
  );
};

export default LoginLogsTable;
