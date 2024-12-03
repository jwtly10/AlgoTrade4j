import * as React from 'react';
import { Avatar, Box, Card, CardHeader, Chip, Grid, Paper, Typography } from '@mui/material';
import { styled } from '@mui/material/styles';
import { ChartLine, ChartLineUp, Wallet, Trophy } from '@phosphor-icons/react';

const StyledCard = styled(Card)(({ theme }) => ({
  height: '100%',
  background: theme.palette.background.paper,
  boxShadow: theme.shadows[3],
  '& .MuiCardHeader-root': {
    paddingBottom: theme.spacing(1),
  },
}));

const MetricBox = styled(Paper)(({ theme }) => ({
  padding: theme.spacing(2.5),
  height: '100%',
  display: 'flex',
  flexDirection: 'column',
  gap: theme.spacing(1),
  background: theme.palette.background.default,
  borderRadius: theme.shape.borderRadius,
}));

const StatValue = styled(Typography)(({ theme, color }) => ({
  fontSize: '1.5rem',
  fontWeight: 700,
  color: color ? theme.palette[color].main : 'inherit',
  lineHeight: 1.2,
}));

const StatLabel = styled(Typography)(({ theme }) => ({
  color: theme.palette.text.secondary,
  fontSize: '0.875rem',
  fontWeight: 500,
}));

const HighlightChip = styled(Chip)(({ theme, color = 'primary' }) => ({
  backgroundColor: theme.palette[color].main,
  color: theme.palette[color].contrastText,
  fontWeight: 600,
}));

function MetricItem({ label, value, color, prefix, suffix, icon: Icon }) {
  return (
    <Box>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 0.5 }}>
        {Icon && <Icon size={16} />}
        <StatLabel>{label}</StatLabel>
      </Box>
      <StatValue color={color}>
        {prefix}
        {typeof value === 'number' ? (Number.isInteger(value) ? value.toString() : value.toFixed(2)) : value}
        {suffix}
      </StatValue>
    </Box>
  );
}

export function LiveTradingAnalysis({ data }) {
  const formatCurrency = (value) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 2,
    }).format(value);
  };

  if (!data?.stats) {
    return (
      <StyledCard>
        <CardHeader
          avatar={
            <Avatar sx={{ bgcolor: 'primary.main' }}>
              <ChartLine weight="bold" size={24} />
            </Avatar>
          }
          title="Live Trading Analysis"
          subheader="No data available"
        />
        <Box sx={{ p: 2 }} />
      </StyledCard>
    );
  }

  const winRateColor = data.stats.winRate > 50 ? 'success' : 'error';

  return (
    <StyledCard>
      <CardHeader
        avatar={
          <Avatar sx={{ bgcolor: 'primary.main' }}>
            <ChartLine weight="bold" size={24} />
          </Avatar>
        }
        title={
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            {data.strategyId}
            <HighlightChip label={data.instrument} size="small" />
          </Box>
        }
        subheader={`Last Updated: ${new Date(data.timestamp).toLocaleString()}`}
      />

      <Box sx={{ p: 2 }}>
        {/* Account Overview */}
        <Grid container spacing={2} sx={{ mb: 2 }}>
          <Grid item xs={12} md={6}>
            <MetricBox>
              <MetricItem
                label="Account Balance"
                value={formatCurrency(data.stats.balance)}
                color="primary"
                icon={Wallet}
              />
              <Typography variant="caption" color="text.secondary">
                Equity: {formatCurrency(data.stats.equity)}
              </Typography>
            </MetricBox>
          </Grid>
          <Grid item xs={12} md={6}>
            <MetricBox>
              <MetricItem
                label="Open Trade P/L"
                value={formatCurrency(data.stats.openTradeProfit)}
                color={data.stats.openTradeProfit >= 0 ? 'success' : 'error'}
                icon={ChartLineUp}
              />
            </MetricBox>
          </Grid>
        </Grid>

        {/* Trading Statistics */}
        <Grid container spacing={2}>
          <Grid item xs={12}>
            <MetricBox>
              <Typography variant="subtitle1" sx={{ mb: 2, fontWeight: 600 }}>
                Live Performance Metrics
              </Typography>
              <Grid container spacing={3}>
                <Grid item xs={6} md={3}>
                  <MetricItem
                    label="Win Rate"
                    value={data.stats.winRate}
                    suffix="%"
                    color={winRateColor}
                    icon={Trophy}
                  />
                </Grid>
                <Grid item xs={6} md={3}>
                  <MetricItem label="Profit Factor" value={data.stats.profitFactor} />
                </Grid>
                <Grid item xs={6} md={3}>
                  <MetricItem label="Total Trades" value={data.stats.totalTrades} />
                </Grid>
                <Grid item xs={6} md={3}>
                  <MetricItem label="Sharpe Ratio" value={data.stats.sharpeRatio} />
                </Grid>
              </Grid>
            </MetricBox>
          </Grid>
        </Grid>
      </Box>
    </StyledCard>
  );
}
