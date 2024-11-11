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
import { logger } from '@/lib/default-logger';
import { StrategyCard } from '@/components/dashboard/service/trading/live-strategy-card';
import { brokerClient } from '@/lib/api/auth/broker-client';
import BrokerAccountCard from '@/components/dashboard/service/trading/broker-card';

const metadata = { title: `Live Strategies | Dashboard | ${config.site.name}` };

export function Page() {
  const intervalRef = React.useRef(null);
  const [liveStrategies, setLiveStrategies] = React.useState([]);
  const [brokerAccounts, setBrokerAccounts] = React.useState([]);
  const [idToggling, setIdToggling] = React.useState(null);

  React.useEffect(() => {
    fetchLiveStrategies();
    fetchBrokerAccounts();

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

  const fetchBrokerAccounts = async () => {
    try {
      const res = await brokerClient.getBrokerAccounts();
      setBrokerAccounts(res);
    } catch (error) {
      logger.error('Error getting broker accounts', error);
    }
  };

  const handleToggle = async (strategy) => {
    try {
      setIdToggling(strategy.id);
      await liveClient.toggleStrategy(strategy.id);
      fetchLiveStrategies();
      setIdToggling(null);
    } catch (error) {
      logger.error('Error toggling live strategy', error);
    }
  };

  const handleEditBrokerAccount = (account) => {
    logger.debug('Editing broker account', account);
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
                <StrategyCard strategy={strategy} handleToggle={handleToggle} toggling={idToggling === strategy.id} />
              </Grid>
            ))}
          </Grid>
          <Stack direction={{ xs: 'row', sm: 'row' }} spacing={3} sx={{ alignItems: 'flex-start', mt: 6 }}>
            <Box sx={{ flex: '1 1 auto' }}>
              <Typography variant="h4">Broker Accounts</Typography>
            </Box>
            <div>
              <Button startIcon={<PlusIcon />} variant="contained">
                New Broker Account
              </Button>
            </div>
          </Stack>
          <Grid container spacing={2}>
            {brokerAccounts.map((account) => (
              <Grid
                size={{
                  xs: 12,
                  sm: 6,
                  md: 4,
                  lg: 3,
                }}
                key={account.id}
              >
                <BrokerAccountCard account={account} onEdit={handleEditBrokerAccount} />
              </Grid>
            ))}
          </Grid>
        </Stack>
      </Box>
    </React.Fragment>
  );
}