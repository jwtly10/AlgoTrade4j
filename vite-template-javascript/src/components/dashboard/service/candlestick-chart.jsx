'use client';

import * as React from 'react';
import { Chip, Paper } from '@mui/material';
import Avatar from '@mui/material/Avatar';
import Box from '@mui/material/Box';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import CardHeader from '@mui/material/CardHeader';
import LinearProgress from '@mui/material/LinearProgress';
import Typography from '@mui/material/Typography';
import { Grid } from '@mui/system';
import { ArrowRight } from '@phosphor-icons/react';
import { Wallet as WalletIcon } from '@phosphor-icons/react/dist/ssr/Wallet';



import { TradingViewChart } from './tradingview-chart';


export function CandlestickChart({
  backtestConfiguration,
  chartData,
  trades,
  indicators,
  isBacktestRunning,
  backtestErrorMsg,
  backtestProgress,
  backtestStartTime,
}) {
  const renderContent = () => {
    // State 1: No data yet
    if (!backtestConfiguration && !isBacktestRunning && !backtestErrorMsg) {
      return (
        <Box display="flex" justifyContent="center" alignItems="center" minHeight={100}>
          <Typography color="text.secondary">Run a strategy to see the backtest results</Typography>
        </Box>
      );
    }

    // State 2: Error state
    if (backtestErrorMsg) {
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
              Backtest Execution Error
            </Typography>

            <Typography textAlign="center" mb={3} color="text.secondary">
              An issue occurred during the backtest run. Please review the error details below:
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
                {backtestErrorMsg}
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

    // State 3: In Progress
    if (isBacktestRunning) {
      return (
        <Box
          sx={{
            p: 4,
            borderRadius: 2,
            bgcolor: 'background.paper',
            boxShadow: 1,
          }}
        >
          {/* Header Section */}
          <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
            <Box>
              <Typography variant="h6" color="primary.main" gutterBottom>
                Backtest Progress
              </Typography>
              <Typography variant="subtitle2" color="text.secondary">
                Strategy: {backtestProgress?.strategyId}
              </Typography>
            </Box>
            <Chip
              label={`${Math.round(backtestProgress?.percentageComplete)}% Complete`}
              color="primary"
              variant="outlined"
            />
          </Box>

          {/* Progress Bar */}
          <Box mb={4}>
            <LinearProgress
              variant="determinate"
              value={backtestProgress?.percentageComplete || 0}
              sx={{
                height: 8,
                borderRadius: 4,
                bgcolor: 'grey.100',
                '& .MuiLinearProgress-bar': {
                  borderRadius: 4,
                },
              }}
            />
          </Box>

          {/* Stats Grid */}
          <Grid container spacing={3} mb={2}>
            <Grid xs={12} sm={6} md={3}>
              <Paper sx={{ p: 2, height: '100%' }}>
                <Typography variant="caption" color="text.secondary">
                  Days Modelled
                </Typography>
                <Typography variant="h6">
                  {backtestProgress?.currentIndex} / {backtestProgress?.totalDays}
                </Typography>
              </Paper>
            </Grid>
            <Grid xs={12} sm={6} md={3}>
              <Paper sx={{ p: 2, height: '100%' }}>
                <Typography variant="caption" color="text.secondary">
                  Ticks Processed
                </Typography>
                <Typography variant="h6">{backtestProgress?.ticksModelled.toLocaleString()}</Typography>
              </Paper>
            </Grid>
            <Grid xs={12} sm={6} md={3}>
              <Paper sx={{ p: 2, height: '100%' }}>
                <Typography variant="caption" color="text.secondary">
                  Instrument
                </Typography>
                <Typography variant="h6">{backtestProgress?.instrument}</Typography>
              </Paper>
            </Grid>
            <Grid xs={12} sm={6} md={3}>
              <Paper sx={{ p: 2, height: '100%' }}>
                <Typography variant="caption" color="text.secondary">
                  Current Day
                </Typography>
                <Typography variant="h6">
                  {new Date(backtestProgress?.currentDay * 1000).toLocaleDateString()}
                </Typography>
              </Paper>
            </Grid>
            <Grid xs={12} sm={6} md={3}>
              <Paper sx={{ p: 2, height: '100%' }}>
                <Typography variant="caption" color="text.secondary">
                  Backtest Start Time
                </Typography>
                <Typography variant="h6">
                  {backtestStartTime ? new Date(backtestStartTime).toLocaleTimeString() : 'N/A'} UTC
                </Typography>
              </Paper>
            </Grid>
          </Grid>

          {/* Date Range */}
          <Paper sx={{ p: 2 }}>
            <Box display="flex" justifyContent="space-between" alignItems="center">
              <Box>
                <Typography variant="caption" color="text.secondary">
                  Start Date
                </Typography>
                <Typography variant="body1">
                  {new Date(backtestProgress?.fromDay * 1000).toLocaleDateString()}
                </Typography>
              </Box>
              <ArrowRight color="action" />
              <Box textAlign="right">
                <Typography variant="caption" color="text.secondary">
                  End Date
                </Typography>
                <Typography variant="body1">{new Date(backtestProgress?.toDay * 1000).toLocaleDateString()}</Typography>
              </Box>
            </Box>
          </Paper>

          {/* Timestamp */}
          <Box mt={2} display="flex" justifyContent="flex-end">
            <Typography variant="caption" color="text.secondary">
              Last Updated: {new Date(backtestProgress?.timestamp).toLocaleTimeString()}
            </Typography>
          </Box>
        </Box>
      );
    }

    // State 4: Show chart with data
    return (
      <TradingViewChart
        // eslint-disable-next-line react/jsx-boolean-value -- annoying error message, this is fine
        showChart={true}
        strategyConfig={backtestConfiguration}
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
            <WalletIcon fontSize="var(--Icon-fontSize)" />
          </Avatar>
        }
        title="Backtest Chart"
      />
      <CardContent>{renderContent()}</CardContent>
    </Card>
  );
}