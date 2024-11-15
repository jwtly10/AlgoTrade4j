'use client';

import * as React from 'react';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Grid from '@mui/material/Grid2';
import Stack from '@mui/material/Stack';
import Typography from '@mui/material/Typography';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import IconButton from '@mui/material/IconButton';
import LinearProgress from '@mui/material/LinearProgress';
import { Plus as PlusIcon } from '@phosphor-icons/react/dist/ssr/Plus';
import { Eye, Faders, ShareNetwork, TrashSimple } from '@phosphor-icons/react';
import { Helmet } from 'react-helmet-async';
import Chip from '@mui/material/Chip';

import { config } from '@/config';
import { useOptimisation } from '@/hooks/services/use-optimisation';
import { dayjs } from '@/lib/dayjs';
import Alert from '@mui/material/Alert';
import { AlertTitle } from '@mui/lab';
import OptimisationConfigurationDialog from '@/components/dashboard/service/optimisation/new-optimisation-modal';

const metadata = { title: `Optimization Tasks | Dashboard | ${config.site.name}` };

const getStatusColor = (status) => {
  switch (status) {
    case 'PENDING':
      return 'default';
    case 'RUNNING':
      return 'primary';
    case 'COMPLETED':
      return 'success';
    case 'FAILED':
      return 'error';
    default:
      return 'default';
  }
};

const ITEMS_PER_PAGE = 5;

export function Page() {
  const { optimisationTasks, isLoading, fetchOptimisationTasks } = useOptimisation();
  const [visibleItems, setVisibleItems] = React.useState(ITEMS_PER_PAGE);

  const [showNewJobDialog, setShowNewJobDialog] = React.useState(false);

  const handleShowMore = () => {
    setVisibleItems((prev) => prev + ITEMS_PER_PAGE);
  };

  const visibleTasks = optimisationTasks.slice(0, visibleItems);
  const hasMoreItems = visibleItems < optimisationTasks.length;

  return (
    <React.Fragment>
      <Helmet>
        <title>{metadata.title}</title>
      </Helmet>
      <Box
        sx={{
          maxWidth: 'var(--Content-maxWidth)',
          m: 'var(--Content-margin)',
          p: 'var(--Content-padding)',
          width: 'var(--Content-width)',
        }}
      >
        <Stack spacing={4}>
          <Stack direction={{ xs: 'column', sm: 'row' }} spacing={3} sx={{ alignItems: 'flex-start' }}>
            <Box sx={{ flex: '1 1 auto' }}>
              <Typography variant="h4">Optimization Jobs</Typography>
            </Box>
            <div>
              <Button
                startIcon={<PlusIcon />}
                variant="contained"
                color="primary"
                onClick={() => setShowNewJobDialog(true)}
              >
                New Optimization Job
              </Button>
            </div>
          </Stack>
          <Grid container spacing={4}>
            <Grid size={12}>
              <Box sx={{ display: 'grid', gap: 2, gridTemplateColumns: { xs: '1fr', md: '1fr 1fr 1fr 1fr' } }}>
                <Card>
                  <CardContent>
                    <Typography color="textSecondary" gutterBottom>
                      Total Jobs
                    </Typography>
                    <Typography variant="h4">{optimisationTasks ? optimisationTasks.length : 0}</Typography>
                  </CardContent>
                </Card>
                <Card>
                  <CardContent>
                    <Typography color="textSecondary" gutterBottom>
                      Running Jobs
                    </Typography>
                    <Typography variant="h4">
                      {optimisationTasks ? optimisationTasks.filter((task) => task.state == 'RUNNING').length : 0}
                    </Typography>
                  </CardContent>
                </Card>
                <Card>
                  <CardContent>
                    <Typography color="textSecondary" gutterBottom>
                      Failed Jobs Today
                    </Typography>
                    <Typography variant="h4">
                      {optimisationTasks
                        ? optimisationTasks.filter((task) => {
                            return task.state == 'FAILED' && dayjs(task.updatedAt * 1000).isSame(dayjs(), 'day');
                          }).length
                        : 0}
                    </Typography>
                  </CardContent>
                </Card>
                <Card>
                  <CardContent>
                    <Typography color="textSecondary" gutterBottom>
                      Completed Jobs Today
                    </Typography>
                    <Typography variant="h4">
                      {optimisationTasks
                        ? optimisationTasks.filter((task) => {
                            return task.state == 'COMPLETED' && dayjs(task.updatedAt * 1000).isSame(dayjs(), 'day');
                          }).length
                        : 0}
                    </Typography>
                  </CardContent>
                </Card>
              </Box>
            </Grid>
            <Grid size={12}>
              <Box sx={{ display: 'grid', gap: 3, gridTemplateColumns: { xs: '1fr', md: '1fr' } }}>
                {visibleTasks.map((task) => (
                  <Card key={task.id}>
                    <CardContent>
                      <Stack spacing={2}>
                        <Stack direction="row" justifyContent="space-between" alignItems="center">
                          <Typography variant="h6">{task.config.strategyClass}</Typography>
                          <Chip label={task.state} color={getStatusColor(task.state)} size="small" />
                        </Stack>

                        <Stack spacing={1}>
                          <Typography variant="body2" color="textSecondary">
                            Task ID: {task.id}
                          </Typography>
                          {task.progressInfo && task.progressInfo !== 'null' && (
                            <>
                              <Box sx={{ width: '100%' }}>
                                <LinearProgress
                                  variant="determinate"
                                  value={task.progressInfo.percentage}
                                  sx={{ height: 8, borderRadius: 4 }}
                                />
                              </Box>
                              <Stack direction="row" justifyContent="space-between">
                                <Stack direction="row" spacing={2}>
                                  <Typography variant="body2" color="textSecondary">
                                    Completed: {task.progressInfo.completedTasks}
                                  </Typography>
                                  <Typography variant="body2" color="textSecondary">
                                    Remaining: {task.progressInfo.remainingTasks}
                                  </Typography>
                                </Stack>
                                <Typography variant="body2">
                                  {task.progressInfo.percentage.toFixed(1)}%
                                  {task.progressInfo.estimatedTimeMs > 0 && (
                                    <Typography component="span" variant="body2" color="textSecondary" sx={{ ml: 1 }}>
                                      ({Math.ceil(task.progressInfo.estimatedTimeMs / 1000 / 60)} min remaining)
                                    </Typography>
                                  )}
                                </Typography>
                              </Stack>
                            </>
                          )}
                          {task.state === 'FAILED' && (
                            <Alert
                              severity="error"
                              variant="outlined"
                              sx={{
                                '& .MuiAlert-message': {
                                  width: '100%',
                                },
                              }}
                            >
                              <AlertTitle>Task Failed</AlertTitle>
                              {task.errorMessage}
                            </Alert>
                          )}
                        </Stack>

                        <Stack direction="row" spacing={2} alignItems="center">
                          <Box flex={1}>
                            <Typography variant="body2" color="textSecondary">
                              Started:{' '}
                              {task.createdAt ? dayjs(task.createdAt * 1000).format('MMM D, YYYY h:mm A') : '-'}
                            </Typography>
                            <Typography variant="body2" color="textSecondary">
                              Last Updated:{' '}
                              {task.updatedAt ? dayjs(task.updatedAt * 1000).format('MMM D, YYYY h:mm A') : '-'}
                            </Typography>
                          </Box>
                          <Stack direction="row" spacing={1}>
                            <IconButton size="small" title="Share">
                              <ShareNetwork />
                            </IconButton>
                            <IconButton size="small" title="View Results">
                              <Eye />
                            </IconButton>
                            <IconButton size="small" title="Configuration">
                              <Faders />
                            </IconButton>
                            <IconButton size="small" title="Delete">
                              <TrashSimple />
                            </IconButton>
                          </Stack>
                        </Stack>
                      </Stack>
                    </CardContent>
                  </Card>
                ))}
                {hasMoreItems && (
                  <Box sx={{ display: 'flex', justifyContent: 'center', mt: 3 }}>
                    <Button variant="outlined" onClick={handleShowMore} sx={{ minWidth: 200 }}>
                      Show More
                    </Button>
                  </Box>
                )}
              </Box>
            </Grid>
          </Grid>
        </Stack>

        {showNewJobDialog && (
          <OptimisationConfigurationDialog
            open={showNewJobDialog}
            onClose={() => setShowNewJobDialog(false)}
            onSubmit={(config) => console.log(`Submitted config ${config}`)}
          />
        )}
      </Box>
    </React.Fragment>
  );
}
