'use client';

import * as React from 'react';
import Avatar from '@mui/material/Avatar';
import Box from '@mui/material/Box';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import CardHeader from '@mui/material/CardHeader';
import Typography from '@mui/material/Typography';
import { ChartLine } from '@phosphor-icons/react';



import { TradingViewChart } from './tradingview-chart';


export function LiveCandleStickChart({
  liveConfiguration,
  chartData,
  trades,
  indicators,
  isRunningLive,
  liveErrorMessage,
  readyToShowChart,
}) {
  const renderContent = () => {
    // State 1: No data yet
    if (!isRunningLive) {
      return (
        <Box display="flex" justifyContent="center" alignItems="center" minHeight={100}>
          <Typography color="text.secondary">
            We can&apos;t show data while the strategy is inactive. Please activate for live details.
          </Typography>
        </Box>
      );
    }

    if (!liveConfiguration || !chartData || chartData.length === 0 || !readyToShowChart) {
      return (
        <Box display="flex" justifyContent="center" alignItems="center" minHeight={100}>
          <Typography color="text.secondary">Connecting to live service. This may take a few moments ...</Typography>
        </Box>
      );
    }

    // State 2: Error state
    if (liveErrorMessage) {
      return (
        <Box
          display="flex"
          alignItems="center"
          justifyContent="center"
          width="100%"
          height="100%"
          minHeight={400}
          p={3}
        >
          <Box width="100%" maxWidth={600}>
            <Typography variant="h5" component="h2" textAlign="center" fontWeight="600" mb={2} color="text.primary">
              Live Execution Error
            </Typography>

            <Typography textAlign="center" mb={3} color="text.secondary">
              An issue occurred during the live strategy run. Please review the error details below:
            </Typography>

            <Box
              sx={{
                backgroundColor: 'error.lighter',
                border: 1,
                borderColor: 'error.light',
                borderRadius: 1,
                p: 2,
                mb: 2,
              }}
            >
              <Typography fontWeight="medium" color="error.main" mb={1}>
                Error Details:
              </Typography>
              <Typography variant="body2" color="error.main" sx={{ wordBreak: 'break-word' }}>
                {liveErrorMessage}
              </Typography>
            </Box>

            <Typography
              variant="body2"
              textAlign="center"
              color="text.secondary"
              sx={{
                mt: 2,
                fontSize: '0.875rem',
              }}
            >
              If this issue persists or was unexpected, please contact our support team for assistance, quoting the
              above strat ID.
            </Typography>
          </Box>
        </Box>
      );
    }

    // State 3: Show chart with data
    return (
      <TradingViewChart
        // eslint-disable-next-line react/jsx-boolean-value -- annoying error message, this is fine
        showChart={true}
        strategyConfig={liveConfiguration}
        chartData={chartData}
        trades={trades}
        indicators={indicators}
      />
    );
  };

  return (
    <Card>
      <CardHeader
        sx={{
          height: '10px',
        }}
        avatar={
          <Avatar>
            <ChartLine fontSize="var(--Icon-fontSize)" />
          </Avatar>
        }
        title="Live Chart"
      />
      <CardContent>{renderContent()}</CardContent>
    </Card>
  );
}