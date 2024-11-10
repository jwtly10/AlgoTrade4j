import * as React from 'react';
import { Button } from '@mui/material';
import Box from '@mui/material/Box';
import Grid from '@mui/material/Grid2';
import Link from '@mui/material/Link';
import Stack from '@mui/material/Stack';
import { ArrowLeft as ArrowLeftIcon } from '@phosphor-icons/react/dist/ssr/ArrowLeft';
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
import { TradeList } from '@/components/dashboard/service/trade-list';

const metadata = { title: `Live Strategy Details | Live Trading | Dashboard | ${config.site.name}` };

export function Page() {
  const { strategyId } = useParams();
  const [liveStrategyDetails, setLiveStrategyDetails] = React.useState(null);
  const [readyToShowChart, setReadyToShowChart] = React.useState(false);

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

  React.useEffect(() => {
    let isSubscribed = true;
    let currentSocket = null;

    // Add debug log to track mount/unmount cycles
    logger.debug('Effect running, creating new connection...');
    const fetchLiveAndStartViewingStrategy = async () => {
      if (!strategyId) {
        logger.error('No strategy ID provided for useEffect');
      }

      if (socketRef.current) {
        logger.debug('Socket connection already exists, skipping...');
        return;
      }

      if (!isSubscribed) return;

      const res = await liveClient.getLiveStrategy(strategyId);
      if (!isSubscribed) return;

      setLiveStrategyDetails(res);

      logger.debug('Live strategy details', res);

      if (res.active && isSubscribed) {
        currentSocket = await viewStrategy(res.strategyName);
        if (!isSubscribed) {
          if (currentSocket) {
            currentSocket.close(1000, 'Component unmounted during connection');
          }
          return;
        }
        setReadyToShowChart(true);
      }
    };

    fetchLiveAndStartViewingStrategy();

    return () => {
      logger.debug('Cleanup running, isSubscribed:', isSubscribed);
      isSubscribed = false;

      if (currentSocket) {
        try {
          logger.debug('Closing WebSocket connection in cleanup');
          currentSocket.close(1000, 'Component unmounting');
          currentSocket = null;
          socketRef.current = null;
        } catch (error) {
          logger.error('Error closing WebSocket:', error);
        }
      }

      setIsConnectedToLive(false);
      setReadyToShowChart(false);
      setLiveStrategyDetails(null);
    };
  }, []);

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
          <Stack spacing={3}>
            <div>
              <Stack direction={{ xs: 'column', sm: 'row' }} spacing={3} sx={{ alignItems: 'flex-start' }}>
                <Stack direction="row" spacing={2} sx={{ alignItems: 'center', flex: '1 1 auto' }}>
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
                </Stack>
                <div>
                  <Button style={{ marginRight: '10px' }} variant="contained">
                    Refresh
                  </Button>
                  <Button variant="contained">Edit Configuration</Button>
                </div>
              </Stack>
            </div>
            <Stack spacing={4}>
              <Grid container spacing={4} alignItems="start">
                <Grid size={{ xs: 12 }}>
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
                <Grid
                  size={{
                    md: 6,
                    xs: 12,
                  }}
                >
                  <AnalysisWidget />
                </Grid>
                <Grid
                  size={{
                    md: 6,
                    xs: 12,
                  }}
                >
                  <TradeList />
                </Grid>
              </Grid>
            </Stack>
          </Stack>
        </Stack>
      </Box>
    </React.Fragment>
  );
}
