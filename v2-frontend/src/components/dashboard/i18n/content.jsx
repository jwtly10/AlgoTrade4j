'use client';

import * as React from 'react';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import { useTranslation } from 'react-i18next';

export function Content() {
  const { t } = useTranslation();

  return (
    <Box sx={{ border: '1px dashed var(--mui-palette-divider)', p: 3 }}>
      <Typography>{t('translations:content')}</Typography>
    </Box>
  );
}
