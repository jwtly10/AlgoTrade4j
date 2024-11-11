'use client';

import * as React from 'react';
import Box from '@mui/material/Box';
import Stack from '@mui/material/Stack';
import Typography from '@mui/material/Typography';

import { paths } from '@/paths';
import { RouterLink } from '@/components/core/link';
import { DynamicLogo } from '@/components/core/logo';
import { Link } from '@mui/material';

export function ResetPasswordForm() {
  return (
    <Stack spacing={4}>
      <div>
        <Box component={RouterLink} href={paths.home} sx={{ display: 'inline-block', fontSize: 0 }}>
          <DynamicLogo colorDark="light" colorLight="dark" height={32} width={122} />
        </Box>
      </div>
      <Typography variant="h5">Reset password</Typography>

      <Typography color="text.secondary" variant="body2">
        Get in contact with us to reset your password.
      </Typography>

      <Link component={RouterLink} href={paths.auth.custom.signIn} sx={{ color: 'inherit', fontSize: '0.875rem' }}>
        <Typography color="text.secondary" variant="body2">
          Go back to login
        </Typography>
      </Link>
    </Stack>
  );
}
