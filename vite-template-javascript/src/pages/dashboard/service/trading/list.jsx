import * as React from 'react';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Grid from '@mui/material/Grid2';
import Stack from '@mui/material/Stack';
import Typography from '@mui/material/Typography';
import { Plus as PlusIcon } from '@phosphor-icons/react/dist/ssr/Plus';
import { Helmet } from 'react-helmet-async';



import { config } from '@/config';
import { liveClient } from '@/lib/api/auth/live-client';
import { dayjs } from '@/lib/dayjs';
import { logger } from '@/lib/default-logger';
import { AccountUpgrade } from '@/components/dashboard/crypto/account-upgrade';
import { CreditCard } from '@/components/dashboard/crypto/credit-card';
import { CurrencyConverter } from '@/components/dashboard/crypto/currency-converter';
import { CurrentBalance } from '@/components/dashboard/crypto/current-balance';
import { DigitalWallet } from '@/components/dashboard/crypto/digital-wallet';
import { Transactions } from '@/components/dashboard/crypto/transactions';
import { StrategyCard } from '@/components/dashboard/service/trading/live-strategy-card';

const metadata = { title: `Live Strategies | Dashboard | ${config.site.name}` };

export function Page() {
  const intervalRef = React.useRef(null);
  const [liveStrategies, setLiveStrategies] = React.useState([]);
  React.useEffect(() => {
    fetchLiveStrategies();

    intervalRef.current = setInterval(() => {
      fetchLiveStrategies();
    }, 5000);

    return () => {
      if (intervalRef.current) {
        clearInterval(intervalRef.current);
      }
    };
  }, []);

  const fetchLiveStrategies = async () => {
    try {
      const res = await liveClient.getLiveStrategies();
      setLiveStrategies(res);
    } catch (error) {
      logger.error('Error getting live strategies from db', error);
    }
  };

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
          <Stack direction={{ xs: 'row', sm: 'row' }} spacing={3} sx={{ alignItems: 'flex-start' }}>
            <Box sx={{ flex: '1 1 auto' }}>
              <Typography variant="h4">Live Strategies</Typography>
            </Box>
            <div>
              <Button startIcon={<PlusIcon />} variant="contained">
                New Live Strategy
              </Button>
            </div>
          </Stack>
          <Grid container spacing={4} alignItems="start">
            {liveStrategies.map((strategy) => (
              <Grid size={{ xs: 12, md: 6, lg: 6 }} key={strategy.id}>
                <StrategyCard strategy={strategy} />
              </Grid>
            ))}
          </Grid>
        </Stack>
      </Box>
    </React.Fragment>
  );
}