'use client';

import * as React from 'react';
import Avatar from '@mui/material/Avatar';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import CardHeader from '@mui/material/CardHeader';
import Chip from '@mui/material/Chip';
import Divider from '@mui/material/Divider';
import Grid from '@mui/material/Grid2';
import IconButton from '@mui/material/IconButton';
import Link from '@mui/material/Link';
import Stack from '@mui/material/Stack';
import Typography from '@mui/material/Typography';
import { ArrowLeft as ArrowLeftIcon } from '@phosphor-icons/react/dist/ssr/ArrowLeft';
import { CaretDown as CaretDownIcon } from '@phosphor-icons/react/dist/ssr/CaretDown';
import { CheckCircle as CheckCircleIcon } from '@phosphor-icons/react/dist/ssr/CheckCircle';
import { PencilSimple as PencilSimpleIcon } from '@phosphor-icons/react/dist/ssr/PencilSimple';
import { ShieldWarning as ShieldWarningIcon } from '@phosphor-icons/react/dist/ssr/ShieldWarning';
import { User as UserIcon } from '@phosphor-icons/react/dist/ssr/User';
import { Helmet } from 'react-helmet-async';
import { useParams } from 'react-router-dom';



import { config } from '@/config';
import { paths } from '@/paths';
import { adminClient } from '@/lib/api/auth/admin-client';
import { logger } from '@/lib/default-logger';
import { RouterLink } from '@/components/core/link';
import { PropertyItem } from '@/components/core/property-item';
import { PropertyList } from '@/components/core/property-list';
import LoginLogsTable from '@/components/dashboard/admin/users/login-logs';
import UserActionLogsTable from '@/components/dashboard/admin/users/user-action-logs';





const metadata = { title: `Details | Customers | Dashboard | ${config.site.name}` };

export function Page() {
  const { userId } = useParams();
  const [userDetails, setUserDetails] = React.useState(null);
  const [isEditing, setIsEditing] = React.useState(false);

  React.useEffect(() => {
    async function fetchUserDetails() {
      console.log('This is the userId: ', userId);
      if (!userId) return;

      try {
        const res = await adminClient.getUserDetails(userId);
        setUserDetails(res);
        console.log(res);
      } catch (error) {
        logger.error(error);
      }
    }

    fetchUserDetails();
  }, []);

  if (!userDetails) {
    return null;
  }

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
          <Stack spacing={3}>
            <div>
              <Link
                color="text.primary"
                component={RouterLink}
                href={paths.dashboard.admin.users.list}
                sx={{ alignItems: 'center', display: 'inline-flex', gap: 1 }}
                variant="subtitle2"
              >
                <ArrowLeftIcon fontSize="var(--icon-fontSize-md)" />
                Users
              </Link>
            </div>
            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={3} sx={{ alignItems: 'flex-start' }}>
              <Stack direction="row" spacing={2} sx={{ alignItems: 'center', flex: '1 1 auto' }}>
                <div>
                  <Stack direction="row" spacing={2} sx={{ alignItems: 'center', flexWrap: 'wrap' }}>
                    <Typography variant="h4">{userDetails.user.firstName + ' ' + userDetails.user.lastName}</Typography>
                    <Chip
                      icon={<CheckCircleIcon color="var(--mui-palette-success-main)" weight="fill" />}
                      label="Active"
                      size="small"
                      variant="outlined"
                    />
                  </Stack>
                  <Typography color="text.secondary" variant="body1">
                    {userDetails.user.email}
                  </Typography>
                </div>
              </Stack>
              <div>
                <Button endIcon={<CaretDownIcon />} variant="contained">
                  Action
                </Button>
              </div>
            </Stack>
          </Stack>

          <Stack spacing={4}>
            <Grid container spacing={4}>
              <Grid
                size={{
                  lg: 4,
                  xs: 12,
                }}
              >
                <Stack spacing={4}>
                  <Card>
                    <CardHeader
                      action={
                        <IconButton>
                          <PencilSimpleIcon />
                        </IconButton>
                      }
                      avatar={
                        <Avatar>
                          <UserIcon fontSize="var(--Icon-fontSize)" />
                        </Avatar>
                      }
                      title="Basic details"
                    />
                    <PropertyList
                      divider={<Divider />}
                      orientation="vertical"
                      sx={{ '--PropertyItem-padding': '12px 24px' }}
                    >
                      {[
                        { key: 'User ID', value: userDetails.user.id },
                        { key: 'Name', value: userDetails.user.firstName + ' ' + userDetails.user.lastName },
                        { key: 'Email', value: userDetails.user.email },
                        { key: 'Role', value: <Chip label={userDetails.user.role} size="small" variant="soft" /> },
                      ].map((item) => (
                        <PropertyItem key={item.key} name={item.key} value={item.value} />
                      ))}
                    </PropertyList>
                  </Card>
                  <Card>
                    <CardHeader
                      avatar={
                        <Avatar>
                          <ShieldWarningIcon fontSize="var(--Icon-fontSize)" />
                        </Avatar>
                      }
                      title="Security"
                    />
                    <CardContent>
                      <Stack spacing={1}>
                        <div>
                          <Button color="error" variant="contained">
                            Delete account
                          </Button>
                        </div>
                        <Typography color="text.secondary" variant="body2">
                          A deleted customer cannot be restored. All data will be permanently removed.
                        </Typography>
                      </Stack>
                    </CardContent>
                  </Card>
                </Stack>
              </Grid>
              <Grid
                size={{
                  lg: 8,
                  xs: 12,
                }}
              >
                <UserActionLogsTable userActions={userDetails.actions} />
              </Grid>
            </Grid>

            <LoginLogsTable loginAttempts={userDetails.loginLogs} />
          </Stack>
        </Stack>
      </Box>
    </React.Fragment>
  );
}