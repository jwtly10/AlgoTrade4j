import * as React from 'react';
import Box from '@mui/material/Box';
import Stack from '@mui/material/Stack';
import Typography from '@mui/material/Typography';
import { Helmet } from 'react-helmet-async';

import { config } from '@/config';
import { Content } from '@/components/dashboard/i18n/content';

const metadata = { title: `i18n | Dashboard | ${config.site.name}` };

export function Page() {
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
          <div>
            <Typography variant="h4">Translations</Typography>
          </div>
          <Typography sx={{ fontStyle: 'italic' }}>
            Use the buttons in the header to change the language and see how the translations are updated.
          </Typography>
          <Stack spacing={3}>
            <Content />
          </Stack>
        </Stack>
      </Box>
    </React.Fragment>
  );
}
