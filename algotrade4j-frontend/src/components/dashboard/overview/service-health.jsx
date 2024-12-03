import React, { useEffect, useState } from 'react';
import { Alert, Box, Card, CardContent, CardHeader, IconButton, List, ListItem, Typography } from '@mui/material';
import { ArrowsClockwise, CheckCircle, Warning, XCircle } from '@phosphor-icons/react';
import { systemClient } from '@/lib/api/clients/system-client';

const StatusIndicator = ({ status, label }) => {
  const getStatusIcon = () => {
    switch (status) {
      case 'UP':
        return <CheckCircle size={24} weight="fill" color="var(--mui-palette-success-main)" />;
      case 'DOWN':
        return <XCircle size={24} weight="fill" color="var(--mui-palette-error-main)" />;
      default:
        return <Warning size={24} weight="fill" color="var(--mui-palette-grey-400)" />;
    }
  };

  const getStatusDisplay = (status) =>
    ({
      UP: 'Operational',
      DOWN: 'Down',
      UNKNOWN: 'Unknown',
    })[status] || 'Unknown';

  const getStatusColor = (status) =>
    ({
      UP: 'success.main',
      DOWN: 'error.main',
      UNKNOWN: 'grey.400',
    })[status] || 'grey.400';

  return (
    <ListItem
      sx={{
        bgcolor: 'background.paper',
        border: 1,
        borderColor: 'divider',
        borderRadius: 1,
        mb: 1,
        p: 2,
        '&:hover': {
          bgcolor: 'action.hover',
        },
      }}
    >
      <Box sx={{ display: 'flex', alignItems: 'center', width: '100%', justifyContent: 'space-between' }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          {getStatusIcon()}
          <Typography variant="body1" fontWeight="medium">
            {label}
          </Typography>
        </Box>
        <Typography variant="body2" sx={{ color: getStatusColor(status) }}>
          {getStatusDisplay(status)}
        </Typography>
      </Box>
    </ListItem>
  );
};

const ServiceHealth = () => {
  const [health, setHealth] = useState({
    api: 'UNKNOWN',
    live: 'UNKNOWN',
    mt5: 'UNKNOWN',
  });
  const [isLoading, setIsLoading] = useState(false);

  const fetchHealthStatus = async () => {
    setIsLoading(true);
    try {
      const [apiStatus, liveStatus, mt5Status] = await Promise.all([
        systemClient
          .mainHealth()
          .then((res) => res.status)
          .catch(() => 'DOWN'),
        systemClient
          .liveHealth()
          .then((res) => res.status)
          .catch(() => 'DOWN'),
        systemClient
          .mt5Health()
          .then((res) => res.status)
          .catch(() => 'DOWN'),
      ]);
      setHealth({ api: apiStatus, live: liveStatus, mt5: mt5Status });
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchHealthStatus();
    const interval = setInterval(fetchHealthStatus, 60000);
    return () => clearInterval(interval);
  }, []);

  const allHealthy = Object.values(health).every((status) => status === 'UP');

  return (
    <Card>
      <CardHeader
        title="System Health"
        action={
          <IconButton onClick={fetchHealthStatus} disabled={isLoading}>
            <ArrowsClockwise
              size={20}
              weight="bold"
              style={{ animation: isLoading ? 'spin 1s linear infinite' : undefined }}
            />
          </IconButton>
        }
      />
      <CardContent>
        <Alert severity={allHealthy ? 'success' : 'warning'} sx={{ mb: 3 }}>
          {allHealthy ? 'All systems are operational' : 'Some systems require attention'}
        </Alert>

        <List disablePadding>
          <StatusIndicator status={health.api} label="Backtest API Service" />
          <StatusIndicator status={health.live} label="Live Trading Service" />
          <StatusIndicator status={health.mt5} label="MT5 Adapter Service" />
        </List>
      </CardContent>
    </Card>
  );
};

export default ServiceHealth;
