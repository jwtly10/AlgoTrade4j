import * as React from 'react';
import Box from '@mui/material/Box';
import Grid from '@mui/material/Grid2';
import Stack from '@mui/material/Stack';
import Typography from '@mui/material/Typography';
import { Helmet } from 'react-helmet-async';



import { config } from '@/config';
import { dayjs } from '@/lib/dayjs';
import { useBacktest } from '@/hooks/services/use-backtest';
import { useStrategyControl } from '@/hooks/services/use-strategy-control';
import { AnalysisWidget } from '@/components/dashboard/service/analysis-widget';
import { CandlestickChart } from '@/components/dashboard/service/backtest-chart';
import { StrategyControl } from '@/components/dashboard/service/backtesting/strategy-control';
import { TradeList } from '@/components/dashboard/service/trade-list';

const metadata = { title: `Crypto | Dashboard | ${config.site.name}` };

export function Page() {
  const {
    selectedSystemStrategyClass,
    systemStrategies,
    onSystemStrategyChange,
    backtestConfiguration,
    setBacktestConfiguration,
  } = useStrategyControl();

  const {
    isBacktestRunning,
    account,
    trades,
    indicators,
    chartData,
    analysisData,
    equityHistory,
    logs,
    backtestErrorMsg,
    backtestProgress,
    cachedBacktestConfiguration,
    backtestStartTime,
    startBacktest,
    stopBacktest,
  } = useBacktest();

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
          <Stack direction={{ xs: 'column', sm: 'row' }} spacing={3} sx={{ alignItems: 'flex-start' }}>
            <Box sx={{ flex: '1 1 auto' }}>
              <Typography variant="h4">Backtest</Typography>
            </Box>
          </Stack>
          <Grid container spacing={4} alignItems="start">
            <Grid
              size={{
                xs: 12,
              }}
            >
              <StrategyControl
                systemStrategies={systemStrategies}
                selectedSystemStrategyClass={selectedSystemStrategyClass}
                onSystemStrategyChange={onSystemStrategyChange}
                backtestConfiguration={backtestConfiguration}
                setBacktestConfiguration={setBacktestConfiguration}
                startBacktest={startBacktest}
                stopBacktest={stopBacktest}
                isBacktestRunning={isBacktestRunning}
              />
            </Grid>
            <Grid size={{ xs: 12 }}>
              <CandlestickChart
                backtestConfiguration={cachedBacktestConfiguration}
                chartData={chartData}
                trades={trades}
                indicators={indicators}
                isBacktestRunning={isBacktestRunning}
                backtestErrorMsg={backtestErrorMsg}
                backtestProgress={backtestProgress}
                backtestStartTime={backtestStartTime}
              />
            </Grid>
            <Grid
              size={{
                md: 6,
                xs: 12,
              }}
            >
              <AnalysisWidget data={analysisData} accountData={account} isBacktestRunning={isBacktestRunning} />
            </Grid>
            <Grid
              size={{
                md: 6,
                xs: 12,
              }}
            >
              <TradeList trades={trades} isBacktestRunning={isBacktestRunning} />
            </Grid>
          </Grid>
        </Stack>
      </Box>
    </React.Fragment>
  );
}