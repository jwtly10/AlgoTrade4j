import * as React from 'react';
import Box from '@mui/material/Box';
import Grid from '@mui/material/Grid2';
import Stack from '@mui/material/Stack';
import Typography from '@mui/material/Typography';
import { Helmet } from 'react-helmet-async';



import { config } from '@/config';
import { dayjs } from '@/lib/dayjs';
import { useStrategyControl } from '@/hooks/services/use-strategy-control';
import { CurrentBalance } from '@/components/dashboard/crypto/current-balance';
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
          <Grid container spacing={4}>
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
              />
            </Grid>
            <Grid
              size={{
                xs: 12,
              }}
            >
              <CurrentBalance
                data={[
                  { name: 'USD', value: 10076.81, color: 'var(--mui-palette-success-main)' },
                  { name: 'BTC', value: 16213.2, color: 'var(--mui-palette-warning-main)' },
                  { name: 'ETH', value: 9626.8, color: 'var(--mui-palette-primary-main)' },
                ]}
              />
            </Grid>
            <Grid
              size={{
                md: 8,
                xs: 12,
              }}
            >
              <TradeList
                trades={[
                  {
                    id: 'TX-003',
                    description: 'Buy BTC',
                    type: 'add',
                    balance: 643,
                    currency: 'BTC',
                    amount: 0.2105,
                    createdAt: dayjs().subtract(2, 'day').subtract(1, 'hour').subtract(32, 'minute').toDate(),
                  },
                  {
                    id: 'TX-002',
                    description: 'Buy BTC',
                    type: 'add',
                    balance: 2344,
                    currency: 'BTC',
                    amount: 0.1337,
                    createdAt: dayjs().subtract(3, 'day').subtract(1, 'hour').subtract(43, 'minute').toDate(),
                  },
                  {
                    id: 'TX-001',
                    description: 'Sell BTC',
                    type: 'sub',
                    balance: 4805,
                    currency: 'BTC',
                    amount: 0.2105,
                    createdAt: dayjs().subtract(6, 'day').subtract(1, 'hour').subtract(32, 'minute').toDate(),
                  },
                ]}
              />
            </Grid>
          </Grid>
        </Stack>
      </Box>
    </React.Fragment>
  );
}