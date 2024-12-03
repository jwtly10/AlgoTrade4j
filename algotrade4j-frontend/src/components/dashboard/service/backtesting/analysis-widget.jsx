import * as React from 'react';
import {
  Avatar,
  Box,
  Button,
  Card,
  CardActions,
  CardHeader,
  Chip,
  Collapse,
  Divider,
  Grid,
  Paper,
  Typography,
} from '@mui/material';
import { styled } from '@mui/material/styles';
import {
  CaretDown,
  CaretUp,
  ChartLine,
  ChartLineUp,
  Clock,
  TrendDown,
  TrendUp,
  Trophy,
  Wallet,
  Warning,
} from '@phosphor-icons/react';

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

export function AnalysisWidget({ data, accountData, isBacktestRunning }) {
  const [expanded, setExpanded] = React.useState(false);

  const formatCurrency = (value) => {
    const num = Number(value);
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: Number.isInteger(num) ? 0 : 2,
    }).format(num);
  };

  if (!data || !accountData) {
    return (
      <StyledCard>
        <CardHeader
          avatar={
            <Avatar sx={{ bgcolor: 'primary.main' }}>
              <ChartLine weight="bold" size={24} />
            </Avatar>
          }
          title="Strategy Analysis"
          subheader={isBacktestRunning ? 'Backtest run in progress. Waiting for result.' : 'No data available'}
        />
        <Box sx={{ p: 2 }} />
      </StyledCard>
    );
  }

  const profitColor = data.stats.totalNetProfit > 0 ? 'success' : 'error';
  const accountProfitPercent = ((accountData.balance - accountData.initialBalance) / accountData.initialBalance) * 100;

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
            {data.strategyId.split('-')[0]}
            <HighlightChip label={data.instrument} size="small" />
          </Box>
        }
      />

      <Box sx={{ p: 2 }}>
        {/* Account Overview */}
        <Grid container spacing={2} sx={{ mb: 2 }}>
          <Grid item xs={12} md={4}>
            <MetricBox>
              <MetricItem
                label="Account Balance"
                value={formatCurrency(accountData.balance)}
                color="primary"
                icon={Wallet}
              />
              <Typography variant="caption" color="text.secondary">
                Initial: {formatCurrency(accountData.initialBalance)}
              </Typography>
            </MetricBox>
          </Grid>
          <Grid item xs={12} md={4}>
            <MetricBox>
              <MetricItem
                label="Total Return"
                value={accountProfitPercent}
                suffix="%"
                color={accountProfitPercent > 0 ? 'success' : 'error'}
                icon={ChartLineUp}
              />
            </MetricBox>
          </Grid>
          <Grid item xs={12} md={4}>
            <MetricBox>
              <MetricItem label="Max Drawdown" value={data.stats.maxDrawdown} suffix="%" color="error" icon={Warning} />
            </MetricBox>
          </Grid>
        </Grid>

        {/* Trading Statistics */}
        <Grid container spacing={2}>
          <Grid item xs={12}>
            <MetricBox>
              <Typography variant="subtitle1" sx={{ mb: 2, fontWeight: 600 }}>
                Performance Metrics
              </Typography>
              <Grid container spacing={3}>
                <Grid item xs={6} md={3}>
                  <MetricItem
                    label="Net Profit"
                    value={formatCurrency(data.stats.totalNetProfit)}
                    color={profitColor}
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

          <Grid item xs={12} md={6}>
            <MetricBox>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
                <TrendUp size={20} />
                <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>
                  Long Positions
                </Typography>
              </Box>
              <Grid container spacing={2}>
                <Grid item xs={6}>
                  <MetricItem label="Win Rate" value={data.stats.longWinPercentage} suffix="%" />
                </Grid>
                <Grid item xs={6}>
                  <MetricItem label="Total Trades" value={data.stats.totalLongTrades} />
                </Grid>
              </Grid>
            </MetricBox>
          </Grid>

          <Grid item xs={12} md={6}>
            <MetricBox>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
                <TrendDown size={20} />
                <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>
                  Short Positions
                </Typography>
              </Box>
              <Grid container spacing={2}>
                <Grid item xs={6}>
                  <MetricItem label="Win Rate" value={data.stats.shortWinPercentage} suffix="%" />
                </Grid>
                <Grid item xs={6}>
                  <MetricItem label="Total Trades" value={data.stats.totalShortTrades} />
                </Grid>
              </Grid>
            </MetricBox>
          </Grid>
        </Grid>

        <Collapse in={expanded}>
          <Box sx={{ mt: 2 }}>
            <MetricBox>
              <Typography variant="subtitle1" sx={{ mb: 2, fontWeight: 600 }}>
                Detailed Statistics
              </Typography>
              <Grid container spacing={3}>
                <Grid item xs={6} md={3}>
                  <MetricItem
                    label="Largest Profit"
                    value={data.stats.largestProfitableTrade}
                    prefix="$"
                    color="success"
                  />
                </Grid>
                <Grid item xs={6} md={3}>
                  <MetricItem label="Largest Loss" value={data.stats.largestLosingTrade} prefix="$" color="error" />
                </Grid>
                <Grid item xs={6} md={3}>
                  <MetricItem label="Avg Win" value={data.stats.averageProfitableTradeReturn} prefix="$" />
                </Grid>
                <Grid item xs={6} md={3}>
                  <MetricItem label="Avg Loss" value={data.stats.averageLosingTradeReturn} prefix="$" />
                </Grid>
                <Grid item xs={6} md={3}>
                  <MetricItem label="Consecutive Wins" value={data.stats.maxConsecutiveWins} />
                </Grid>
                <Grid item xs={6} md={3}>
                  <MetricItem label="Consecutive Losses" value={data.stats.maxConsecutiveLosses} />
                </Grid>
                <Grid item xs={6} md={3}>
                  <MetricItem label="Expected Payoff" value={data.stats.expectedPayoff} prefix="$" />
                </Grid>
                <Grid item xs={6} md={3}>
                  <MetricItem label="Ticks Modelled" value={data.stats.ticksModelled} />
                </Grid>
              </Grid>
            </MetricBox>
          </Box>
        </Collapse>
      </Box>

      <CardActions>
        <Button
          onClick={() => setExpanded(!expanded)}
          endIcon={expanded ? <CaretUp /> : <CaretDown />}
          size="small"
          color="secondary"
          sx={{ ml: 'auto' }}
        >
          {expanded ? 'Show Less' : 'Show More'}
        </Button>
      </CardActions>
    </StyledCard>
  );
}
