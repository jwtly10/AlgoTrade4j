'use client';

import * as React from 'react';
import CssBaseline from '@mui/material/CssBaseline';
import { ThemeProvider } from '@mui/material/styles';

import { useSettings } from '@/hooks/use-settings';
import { createTheme } from '@/styles/theme/create-theme';

import { Rtl } from './rtl';

function CustomThemeProvider({ children }) {
  const { settings } = useSettings();

  const theme = createTheme({
    primaryColor: settings.primaryColor,
    direction: settings.direction,
  });

  return (
    <Rtl direction={settings.direction}>
      <ThemeProvider disableTransitionOnChange theme={theme}>
        <CssBaseline />
        {children}
      </ThemeProvider>
    </Rtl>
  );
}

export { CustomThemeProvider as ThemeProvider };
