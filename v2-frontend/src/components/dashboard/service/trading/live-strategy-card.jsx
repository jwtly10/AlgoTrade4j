'use client';

import * as React from 'react';
import Avatar from '@mui/material/Avatar';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Card from '@mui/material/Card';
import CardActions from '@mui/material/CardActions';
import CardContent from '@mui/material/CardContent';
import CardHeader from '@mui/material/CardHeader';
import Chip from '@mui/material/Chip';
import Divider from '@mui/material/Divider';
import Grid from '@mui/material/Grid';
import Link from '@mui/material/Link';
import Stack from '@mui/material/Stack';
import Typography from '@mui/material/Typography';
import { Eye, Gear, Play, Spinner, Stop } from '@phosphor-icons/react';
import { Rocket as RocketIcon } from '@phosphor-icons/react/dist/ssr/Rocket';

import { paths } from '@/paths';
import { RouterLink } from '@/components/core/link';

const formatCurrency = (value) => {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(value);
};

const formatPercentage = (value) => {
  return new Intl.NumberFormat('en-US', {
    style: 'percent',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(value / 100);
};

export function StrategyCard({ strategy, handleToggle, toggling, onEdit }) {
  const { strategyName, stats, config, active, brokerAccount } = strategy;

  const isProfit = stats ? stats.profit >= 0 : 0;
  const profitColor = isProfit ? 'success.main' : 'error.main';

  return (
    <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      <CardHeader
        avatar={
          <Avatar sx={{ bgcolor: active ? 'success.main' : 'grey.500' }}>
            <RocketIcon weight="fill" />
          </Avatar>
        }
        action={
          <Chip
            label={active ? 'Active' : 'Inactive'}
            color={active ? 'success' : 'default'}
            size="small"
            sx={{ mt: 1 }}
          />
        }
        title={
          <Typography variant="h6" component="div">
            {strategyName}
          </Typography>
        }
        subheader={
          <Typography variant="body2" color="text.secondary">
            {config.instrumentData.internalSymbol} • {config.period}
          </Typography>
        }
      />
      {strategy.stats ? (
        <CardContent sx={{ flex: 1 }}>
          <Stack spacing={2}>
            {/* General stats */}
            <Grid container spacing={2}>
              <Grid item xs={6}>
                <Stack spacing={0.5}>
                  <Typography color="text.secondary" variant="caption">
                    Balance
                  </Typography>
                  <Typography variant="h6">{formatCurrency(stats.accountBalance)}</Typography>
                </Stack>
              </Grid>
              <Grid item xs={6}>
                <Stack spacing={0.5}>
                  <Typography color="text.secondary" variant="caption">
                    Total Profit
                  </Typography>
                  <Typography variant="h6" color={profitColor}>
                    {formatCurrency(stats.profit)}
                  </Typography>
                </Stack>
              </Grid>
              <Grid item xs={4}>
                <Stack spacing={0.5}>
                  <Typography color="text.secondary" variant="caption">
                    Win Rate
                  </Typography>
                  <Typography variant="body1">{formatPercentage(stats.winRate)}</Typography>
                </Stack>
              </Grid>
              <Grid item xs={4}>
                <Stack spacing={0.5}>
                  <Typography color="text.secondary" variant="caption">
                    Profit Factor
                  </Typography>
                  <Typography variant="body1">{stats.profitFactor.toFixed(2)}</Typography>
                </Stack>
              </Grid>
              <Grid item xs={4}>
                <Stack spacing={0.5}>
                  <Typography color="text.secondary" variant="caption">
                    Total Trades
                  </Typography>
                  <Typography variant="body1">{stats.totalTrades}</Typography>
                </Stack>
              </Grid>
            </Grid>
            {/* Broker data */}
            <Box>
              <Divider sx={{ my: 2 }} />
              <Stack spacing={2}>
                <Typography variant="subtitle2" color="text.secondary">
                  Broker Details
                </Typography>
                <Grid container spacing={2}>
                  <Grid item xs={4}>
                    <Stack spacing={0.5}>
                      <Typography color="text.secondary" variant="caption">
                        Broker
                      </Typography>
                      <Typography variant="body2">
                        {brokerAccount.brokerName.trim() || brokerAccount.brokerType}
                      </Typography>
                    </Stack>
                  </Grid>
                  <Grid item xs={4}>
                    <Stack spacing={0.5}>
                      <Typography color="text.secondary" variant="caption">
                        Type
                      </Typography>
                      <Typography variant="body2">{brokerAccount.brokerType}</Typography>
                    </Stack>
                  </Grid>
                  <Grid item xs={4}>
                    <Stack spacing={0.5}>
                      <Typography color="text.secondary" variant="caption">
                        Environment
                      </Typography>
                      <Chip
                        style={{ textTransform: 'capitalize' }}
                        label={brokerAccount.brokerEnv.toLowerCase()}
                        size="small"
                        color={brokerAccount.brokerEnv === 'DEMO' ? 'info' : 'warning'}
                        sx={{ width: 'fit-content' }}
                      />
                    </Stack>
                  </Grid>
                </Grid>
              </Stack>
            </Box>
          </Stack>
        </CardContent>
      ) : (
        <CardContent sx={{ flex: 1 }}>
          <Stack spacing={2}>
            <Typography color="text.secondary">
              This strategy has not been run yet. Please start the strategy to view stats.
            </Typography>
          </Stack>
        </CardContent>
      )}

      <Divider />
      <CardActions>
        <>
          {toggling ? (
            <Button onClick={() => handleToggle(strategy)} disabled size="small" startIcon={<Spinner />} color="info">
              Toggling
            </Button>
          ) : (
            <Button
              onClick={() => handleToggle(strategy)}
              size="small"
              startIcon={active ? <Stop /> : <Play />}
              color={active ? 'error' : 'success'}
              disabled={toggling}
            >
              {active ? 'Stop' : 'Start'}
            </Button>
          )}
        </>
        <Link
          color="inherit"
          component={RouterLink}
          href={toggling ? '' : paths.dashboard.service.trading.details(strategy.id)}
          sx={{ whiteSpace: 'nowrap' }}
          variant="subtitle2"
          disabled={toggling}
        >
          <Button size="small" startIcon={<Eye />} color="primary" disabled={toggling}>
            View
          </Button>
        </Link>
        <Button
          size="small"
          startIcon={<Gear />}
          color="primary"
          onClick={() => onEdit(strategy)}
          disabled={toggling}
        >
          Configure
        </Button>
      </CardActions>
    </Card>
  );
}
