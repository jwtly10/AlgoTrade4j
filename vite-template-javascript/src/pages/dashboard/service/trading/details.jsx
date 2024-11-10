import * as React from 'react';
import { Button, Box, Typography, CircularProgress, Stack, Link, Grid, Chip } from '@mui/material';
import { ArrowLeft as ArrowLeftIcon } from '@phosphor-icons/react/dist/ssr/ArrowLeft';
import { Play, Stop, ArrowClockwise, Gear } from '@phosphor-icons/react';
import { Helmet } from 'react-helmet-async';
import { useParams } from 'react-router-dom';

import { config } from '@/config';
import { paths } from '@/paths';
import { liveClient } from '@/lib/api/auth/live-client';
import { logger } from '@/lib/default-logger';
import { useLive } from '@/hooks/services/use-live';
import { RouterLink } from '@/components/core/link';
import { AnalysisWidget } from '@/components/dashboard/service/analysis-widget';
import { LiveCandleStickChart } from '@/components/dashboard/service/live-chart';
import { LiveTradeList, TradeList } from '@/components/dashboard/service/trade-list';

const metadata = { title: `Live Strategy Details | Live Trading | Dashboard | ${config.site.name}` };

export function Page() {
  const { strategyId } = useParams();
  const [liveStrategyDetails, setLiveStrategyDetails] = React.useState(null);
  const [readyToShowChart, setReadyToShowChart] = React.useState(false);
  const [isToggling, setIsToggling] = React.useState(false);

  const {
    isConnectedToLive,
    analysisData,
    trades,
    indicators,
    chartData,
    logs,
    socketRef,
    setIsConnectedToLive,
    viewStrategy,
  } = useLive();

  const handleToggle = async (strategy) => {
    if (!strategy || isToggling) return;

    setIsToggling(true);
    try {
      await liveClient.toggleStrategy(strategy.id);

      if (strategy.active) {
        logger.debug('Stopping live strategy:', strategy.strategyName);
        if (socketRef.current) {
          socketRef.current.close(1000, 'Strategy stopped');
          socketRef.current = null;
        }
        setIsConnectedToLive(false);
      } else {
        logger.debug('Starting live strategy:', strategy.strategyName);
        await viewStrategy(strategy.strategyName);
      }

      const updatedStrategy = await liveClient.getLiveStrategy(strategy.id);
      setLiveStrategyDetails(updatedStrategy);
      setReadyToShowChart(updatedStrategy.active);
    } catch (error) {
      logger.error('Error toggling strategy:', error);
    } finally {
      setIsToggling(false);
    }
  };

  const handleRefresh = async () => {
    if (!strategyId || !liveStrategyDetails) return;

    try {
      logger.debug('Refreshing strategy:', liveStrategyDetails.strategyName);
      // Close existing socket connection
      if (socketRef.current) {
        logger.debug('Closing existing socket connection');
        socketRef.current.close(1000, 'Refreshing connection');
        socketRef.current = null;
      }
      setIsConnectedToLive(false);

      // Get updated strategy details
      const updatedStrategy = await liveClient.getLiveStrategy(strategyId);
      setLiveStrategyDetails(updatedStrategy);

      // If strategy is active, reconnect socket
      if (updatedStrategy.active) {
        logger.debug('Reconnecting socket for strategy:', updatedStrategy.strategyName);
        await viewStrategy(updatedStrategy.strategyName);
        setReadyToShowChart(true);
      }

      logger.debug('Strategy refreshed:', updatedStrategy.strategyName);
    } catch (error) {
      logger.error('Error refreshing strategy:', error);
    }
  };

  const handleEditConfig = () => {
    // TODO: Implement editing configuration
    logger.debug('Editing configuration...');
  };

  // Only fetch initial strategy details once on mount
  React.useEffect(() => {
    let isMounted = true;

    const initializeStrategy = async () => {
      logger.debug('Initializing strategy:', strategyId);
      if (!strategyId) return;

      try {
        const strategy = await liveClient.getLiveStrategy(strategyId);
        if (!isMounted) return;

        setLiveStrategyDetails(strategy);
        setReadyToShowChart(strategy.active);

        if (strategy.active) {
          logger.debug('Connecting to live strategy:', strategy.strategyName);
          await viewStrategy(strategy.strategyName);
        }

        logger.debug('Strategy initialized:', strategy.strategyName);
      } catch (error) {
        logger.error('Error initializing strategy:', error);
      }
    };

    initializeStrategy();

    return () => {
      isMounted = false;
      if (socketRef.current) {
        logger.debug('Closing socket connection on unmount');
        socketRef.current.close(1000, 'Component unmounting');
        socketRef.current = null;
      }
      setIsConnectedToLive(false);
      setReadyToShowChart(false);
    };
  }, [strategyId]);

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
        <Stack spacing={3}>
          <Link
            color="text.primary"
            component={RouterLink}
            href={paths.dashboard.service.trading.list}
            sx={{ alignItems: 'center', display: 'inline-flex', gap: 1 }}
            variant="subtitle2"
          >
            <ArrowLeftIcon fontSize="var(--icon-fontSize-md)" />
            Live Strategies
          </Link>

          {liveStrategyDetails ? (
            <Box
              sx={{
                bgcolor: 'background.paper',
                borderRadius: 1,
                p: 3,
                boxShadow: 1,
              }}
            >
              <Stack spacing={2}>
                <Stack
                  direction={{ xs: 'column', sm: 'row' }}
                  spacing={2}
                  sx={{
                    alignItems: { xs: 'flex-start', sm: 'center' },
                    justifyContent: 'space-between',
                  }}
                >
                  <Stack direction="row" spacing={2} alignItems="center">
                    <Typography variant="h4" component="h1">
                      {liveStrategyDetails.strategyName}
                    </Typography>
                    <Chip
                      label={liveStrategyDetails.active ? 'Active' : 'Inactive'}
                      color={liveStrategyDetails.active ? 'success' : 'default'}
                      size="small"
                    />
                  </Stack>
                  <Stack
                    direction="row"
                    spacing={1}
                    sx={{
                      flexWrap: 'wrap',
                      gap: 1,
                    }}
                  >
                    {isToggling ? (
                      <Button disabled variant="contained" startIcon={<CircularProgress size={20} color="inherit" />}>
                        Toggling...
                      </Button>
                    ) : (
                      <Button
                        onClick={() => handleToggle(liveStrategyDetails)}
                        variant="contained"
                        startIcon={liveStrategyDetails.active ? <Stop /> : <Play />}
                        color={liveStrategyDetails.active ? 'error' : 'success'}
                      >
                        {liveStrategyDetails.active ? 'Stop' : 'Start'}
                      </Button>
                    )}
                    {liveStrategyDetails.active ? (
                      <Button variant="outlined" startIcon={<ArrowClockwise />} onClick={handleRefresh}>
                        Refresh Connection
                      </Button>
                    ) : null}
                    <Button variant="outlined" startIcon={<Gear />} onClick={handleEditConfig}>
                      Config
                    </Button>
                  </Stack>
                </Stack>
              </Stack>
            </Box>
          ) : null}

          <Grid container spacing={3}>
            <Grid item xs={12}>
              {liveStrategyDetails ? (
                <LiveCandleStickChart
                  liveConfiguration={liveStrategyDetails.config}
                  chartData={chartData}
                  trades={trades}
                  isRunningLive={isConnectedToLive}
                  indicators={indicators}
                  analysisData={analysisData}
                  logs={logs}
                  readyToShowChart={readyToShowChart}
                />
              ) : null}
            </Grid>

            <Grid item xs={12} md={6}>
              <AnalysisWidget />
            </Grid>
            <Grid item xs={12} md={6}>
              <LiveTradeList trades={trades} />
            </Grid>
          </Grid>
        </Stack>
      </Box>
    </React.Fragment>
  );
}
