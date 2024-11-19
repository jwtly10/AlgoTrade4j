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
import BrokerAccountModal from '@/components/dashboard/service/trading/broker-account-modal';
import StrategyConfigurationDialog from '@/components/dashboard/service/trading/live-strategy-configuration';
import { toast } from 'react-toastify';
import Card from "@mui/material/Card";
import {Buildings} from "@phosphor-icons/react";

const metadata = { title: `Live Strategies | Dashboard | ${config.site.name}` };

export function Page() {
  const intervalRef = React.useRef(null);
  const [liveStrategies, setLiveStrategies] = React.useState([]);
  const [brokerAccounts, setBrokerAccounts] = React.useState([]);
  const [idToggling, setIdToggling] = React.useState(null);

  // Creating or editing strategies
  const [liveStrategyModalOpen, setLiveStrategyModalOpen] = React.useState(false);
  const [selectedStrategy, setSelectedStrategy] = React.useState(null);

  // Creating or editing brokers
  const [brokerModalOpen, setBrokerModalOpen] = React.useState(false);
  const [selectedAccount, setSelectedAccount] = React.useState(null);
  const [isSavingBroker, setIsSavingBroker] = React.useState(false);

  React.useEffect(() => {
    fetchLiveStrategies();
    fetchBrokerAccounts();

    intervalRef.current = setInterval(() => {
      fetchLiveStrategies();
      fetchBrokerAccounts();
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
      toast.error(`Error getting live strategies: ${error.message}`);
      logger.error('Error getting live strategies from db', error);
    }
  };

  const fetchBrokerAccounts = async () => {
    try {
      const res = await brokerClient.getBrokerAccounts();
      setBrokerAccounts(res);
    } catch (error) {
      toast.error(`Error getting broker accounts: ${error.message}`);
      logger.error('Error getting broker accounts', error);
    }
  };

  const handleToggleStrategy = async (strategy) => {
    try {
      setIdToggling(strategy.id);
      await liveClient.toggleLiveStrategy(strategy.id);

      if (strategy.active) {
        toast.success(`Strategy ${strategy.strategyName} stopped successfully`);
      } else {
        toast.success(`Strategy ${strategy.strategyName} started successfully`);
      }

      fetchLiveStrategies();
      setIdToggling(null);
    } catch (error) {
      toast.error(`Error toggling live strategy: ${error.message}`);
      logger.error('Error toggling live strategy', error);
    }
  };

  const handleStrategyEditClick = (strategy) => {
    logger.debug('Editing strategy:', strategy);
    setSelectedStrategy(strategy);
    setLiveStrategyModalOpen(true);
  };

  const handleStrategyCreateClick = () => {
    logger.debug('Creating new strategy');
    setSelectedStrategy(null);
    setLiveStrategyModalOpen(true);
  };

  const handleDeleteStrategy = async (strategyConfig) => {
    const confirm = window.confirm('Are you sure you want to delete this live strategy?');
    if (!confirm) return;

    logger.debug('Deleting strategy:', strategyConfig);

    try {
      await liveClient.deleteLiveStrategy(strategyConfig.id);
      toast.success('Strategy deleted successfully');
      await fetchLiveStrategies();
      setSelectedStrategy(null);
      setLiveStrategyModalOpen(false);
      logger.debug('Strategy deleted successfully');
    } catch (error) {
      toast.error(`Error deleting strategy: ${error.message}`);
      logger.error('Error deleting strategy', error);
    }
  };

  const handleSaveStrategy = async (editConfig) => {
    try {
      if (selectedStrategy) {
        logger.debug('Saving strategy:', editConfig);
        await liveClient.updateLiveStrategy(editConfig);
        toast.success('Strategy updated successfully');
      } else {
        logger.debug('Creating new strategy:', editConfig);
        await liveClient.createLiveStrategy(editConfig);
        toast.success('Strategy created successfully');
      }
      await fetchLiveStrategies();
      setSelectedStrategy(null);
      setLiveStrategyModalOpen(false);
    } catch (error) {
      toast.error(`Error saving strategy: ${error.message}`);
      logger.error('Error saving strategy', error);
    }
  };

  const handleBrokerEditClick = (account) => {
    logger.debug('Editing account:', account);
    setSelectedAccount(account);
    setBrokerModalOpen(true);
  };

  const handleBrokerCreateClick = () => {
    logger.debug('Creating new account');
    setSelectedAccount(null);
    setBrokerModalOpen(true);
  };

  const handleSaveBroker = async (formData) => {
    logger.debug('Saving account:', formData);
    setIsSavingBroker(true);
    try {
      if (selectedAccount) {
        await brokerClient.updateBrokerAccount(selectedAccount.accountId, formData);
        toast.success('Broker Account updated successfully');
      } else {
        await brokerClient.createBrokerAccount(formData);
        toast.success('Broker Account created successfully');
      }
      await fetchBrokerAccounts();
      setBrokerModalOpen(false);
      setSelectedAccount(null);
      logger.debug('Account saved successfully');
    } catch (error) {
      toast.error(`Error saving broker account: ${error.message}`);
      logger.error('Error saving account', error);
    } finally {
      setIsSavingBroker(false);
    }
  };

  const handleDeleteBroker = async (accountId) => {
    const confirm = window.confirm('Are you sure you want to delete this broker account?');
    if (!confirm) return;

    logger.debug('Deleting account:', accountId);

    try {
      await brokerClient.deleteBrokerAccount(accountId);
      toast.success('Account deleted successfully');
      await fetchBrokerAccounts();
      setBrokerModalOpen(false);
      setSelectedAccount(null);
      logger.debug('Broker Account deleted successfully');
    } catch (error) {
      toast.error(`Error deleting account: ${error.message}`);
      logger.error('Error deleting account', error);
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
              <Button startIcon={<PlusIcon />} variant="contained" onClick={handleStrategyCreateClick}>
                New Live Strategy
              </Button>
            </div>
          </Stack>
          <Grid container spacing={2}>
            {liveStrategies.length === 0 ? (
              <Grid item xs={12}>
                <Card
                  sx={{
                    height: '100%',
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: 'center',
                    justifyContent: 'center',
                    padding: 4,
                    textAlign: 'center',
                    minHeight: 200
                  }}
                >
                  <Buildings
                    size={48}
                    weight="light"
                    style={{
                      marginBottom: 16,
                      opacity: 0.6
                    }}
                  />
                  <Typography variant="h6" gutterBottom>
                    No Live Strategies
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    Configure your first Live Strategy to get started
                  </Typography>
                </Card>
              </Grid>
            ) : (
              liveStrategies.map((strategy) => (
                <Grid item xs={12} md={6} lg={6} key={strategy.id}>
                  <StrategyCard
                    strategy={strategy}
                    handleToggle={handleToggleStrategy}
                    toggling={idToggling === strategy.id}
                    onEdit={handleStrategyEditClick}
                  />
                </Grid>
              ))
            )}
          </Grid>
          <Stack direction={{ xs: 'row', sm: 'row' }} spacing={3} sx={{ alignItems: 'flex-start', mt: 6 }}>
            <Box sx={{ flex: '1 1 auto' }}>
              <Typography variant="h4">Broker Accounts</Typography>
            </Box>
            <div>
              <Button startIcon={<PlusIcon />} variant="contained" onClick={handleBrokerCreateClick}>
                New Broker
              </Button>
            </div>
          </Stack>
          <Grid container spacing={2}>
            {brokerAccounts.length === 0 ? (
              <Grid item xs={12}>
                <Card
                  sx={{
                    height: '100%',
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: 'center',
                    justifyContent: 'center',
                    padding: 4,
                    textAlign: 'center',
                    minHeight: 200
                  }}
                >
                  <Buildings
                    size={48}
                    weight="light"
                    style={{
                      marginBottom: 16,
                      opacity: 0.6
                    }}
                  />
                  <Typography variant="h6" gutterBottom>
                    No Broker Accounts
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    Add your first broker account to get started
                  </Typography>
                </Card>
              </Grid>
            ) : (
              brokerAccounts.map((account) => (
                <Grid
                  item
                  xs={12}
                  key={account.id}
                >
                  <BrokerAccountCard account={account} onEdit={handleBrokerEditClick} />
                </Grid>
              ))
            )}
          </Grid>
        </Stack>
        <BrokerAccountModal
          open={brokerModalOpen}
          onClose={() => setBrokerModalOpen(false)}
          account={selectedAccount} // null for create, account object for edit
          onSave={handleSaveBroker}
          onDelete={handleDeleteBroker}
          isSaving={isSavingBroker}
        />
        <StrategyConfigurationDialog
          open={liveStrategyModalOpen}
          onClose={() => setLiveStrategyModalOpen(false)}
          initialConfig={selectedStrategy} // If null will trigger create mode
          onSave={handleSaveStrategy}
          onDelete={handleDeleteStrategy}
        />
      </Box>
    </React.Fragment>
  );
}
