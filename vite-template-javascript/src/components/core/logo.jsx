'use client';

import * as React from 'react';
import Box from '@mui/material/Box';
import { useColorScheme } from '@mui/material/styles';

import { NoSsr } from '@/components/core/no-ssr';

const HEIGHT = 120;
const WIDTH = 60;
// const HEIGHT = 60;
// const WIDTH = 60;

export function Logo({ color = 'dark', emblem, height = HEIGHT, width = WIDTH }) {
  let url;

  if (emblem) {
    url = color === 'light' ? '/logo/logo-emblem.png' : '/logo/logo-emblem--dark.png';
  } else {
    url = color === 'light' ? '/logo/logo.png' : '/logo/logo--dark.png';
  }

  return <Box alt="logo" component="img" src={url} width={width} />;
}

export function DynamicLogo({ colorDark = 'light', colorLight = 'dark', height = HEIGHT, width = WIDTH, ...props }) {
  const { colorScheme } = useColorScheme();
  const color = colorScheme === 'dark' ? colorDark : colorLight;

  return (
    <NoSsr fallback={<Box sx={{ height: `${height}px`, width: `${width}px` }} />}>
      <Logo color={color} height={height} width={width} {...props} />
    </NoSsr>
  );
}
