import * as React from 'react';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Container from '@mui/material/Container';
import Stack from '@mui/material/Stack';
import Typography from '@mui/material/Typography';
import { Helmet } from 'react-helmet-async';

import { config } from '@/config';
import { paths } from '@/paths';
import { RouterLink } from '@/components/core/link';

const metadata = { title: `Insufficient Permissions | Errors | ${config.site.name}` };

export function Page() {
  return (
    <React.Fragment>
      <Helmet>
        <title>{metadata.title}</title>
      </Helmet>
      <Box
        component="main"
        sx={{
          alignItems: 'center',
          display: 'flex',
          flexDirection: 'column',
          justifyContent: 'center',
          minHeight: '100%',
          py: '64px',
        }}
      >
        <Container maxWidth="lg">
          <Stack spacing={6}>
            <Box sx={{ display: 'flex', justifyContent: 'center' }}>
              <Box
                alt="Insufficient permissions"
                component="img"
                src="/assets/error.svg"
                sx={{ height: 'auto', maxWidth: '100%', width: '200px' }}
              />
            </Box>
            <Stack spacing={1} sx={{ textAlign: 'center' }}>
              <Typography variant="h4">Insufficient Permissions</Typography>
              <Typography color="text.secondary">
                You don&#39;t have the necessary permissions to access this page. Please contact your administrator for
                more information.
              </Typography>
            </Stack>
            <Box sx={{ display: 'flex', justifyContent: 'center' }}>
              <Button component={RouterLink} href={paths.home} variant="contained">
                Back to home
              </Button>
            </Box>
          </Stack>
        </Container>
      </Box>
    </React.Fragment>
  );
}
