import { createTheme } from '@mui/material/styles';

import { colorSchemes } from './color-schemes';
import { components } from './components/components';
import { shadows } from './shadows';
import { typography } from './typography';

function customCreateTheme(config) {
  const theme = createTheme({
    breakpoints: { values: { xs: 0, sm: 600, md: 900, lg: 1200, xl: 1440 } },
    colorSchemes: colorSchemes({ primaryColor: config.primaryColor }),
    components,
    cssVariables: {
      colorSchemeSelector: 'class',
    },
    direction: config.direction,
    shadows,
    shape: { borderRadius: 8 },
    typography,
  });

  return theme;
}

export { customCreateTheme as createTheme };
