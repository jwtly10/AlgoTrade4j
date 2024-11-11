import * as React from 'react';
import {
  Avatar,
  Box,
  Card,
  CardHeader,
  Chip,
  Collapse,
  Grid,
  IconButton,
  List,
  ListItem,
  ListItemAvatar,
  ListItemText,
  Pagination,
  Paper,
  Tab,
  Tabs,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableRow,
  Typography,
} from '@mui/material';
import { styled } from '@mui/material/styles';
import { ArrowsDownUp, CaretDown, CaretUp, TrendDown, TrendUp } from '@phosphor-icons/react';
import dayjs from 'dayjs';

// Keep existing styled components...
const StyledCard = styled(Card)(({ theme }) => ({
  overflow: 'visible',
  '& .MuiListItem-root': {
    padding: theme.spacing(2),
    '&:hover': {
      backgroundColor: theme.palette.action.hover,
      cursor: 'pointer',
    },
  },
}));

const TradeChip = styled(Chip)(({ theme, status }) => ({
  borderRadius: theme.shape.borderRadius,
  fontWeight: 600,
  ...(status === 'OPEN' && {
    backgroundColor: theme.palette.info.light,
    color: theme.palette.info.contrastText,
  }),
  ...(status === 'CLOSE' && {
    backgroundColor: theme.palette.grey[500],
    color: theme.palette.common.white,
  }),
}));

const ProfitTypography = styled(Typography)(({ theme, profit }) => ({
  fontWeight: 600,
  color: profit >= 0 ? theme.palette.success.main : theme.palette.error.main,
}));

const LiveTradeListItem = styled(ListItem)(({ theme, islive }) => ({
  ...(islive && {
    borderLeft: `4px solid ${theme.palette.primary.main}`,
    backgroundColor: theme.palette.action.hover,
  }),
}));

function TradeListItem({ trade, islive = false }) {
  const [expanded, setExpanded] = React.useState(false);
  const isProfit = parseFloat(trade.profit) >= 0;

  const formatDate = (timestamp) => {
    return dayjs.unix(timestamp).format('MMM D, YYYY HH:mm:ss');
  };

  const detailRows = [
    { label: 'Trade ID', value: trade.tradeId },
    { label: 'Entry Price', value: trade.entry },
    { label: 'Stop Loss', value: trade.stopLoss },
    { label: 'Take Profit', value: trade.takeProfit },
    { label: 'Close Price', value: trade.closePrice },
    { label: 'Quantity', value: trade.quantity },
    { label: 'Position Type', value: trade.position.toUpperCase() },
  ];

  return (
    <>
      <LiveTradeListItem divider islive={islive}>
        <ListItemAvatar>
          <Avatar
            sx={{
              bgcolor: isProfit ? 'success.light' : 'error.light',
              color: 'common.white',
            }}
          >
            {isProfit ? <TrendUp weight="bold" size={24} /> : <TrendDown weight="bold" size={24} />}
          </Avatar>
        </ListItemAvatar>
        <ListItemText
          primary={
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <Typography variant="subtitle1">
                {trade.isLong ? 'BUY' : 'SELL'} {trade.instrument}
              </Typography>
            </Box>
          }
          secondary={
            <Typography variant="body2" color="text.secondary">
              {formatDate(trade.openTime)}
            </Typography>
          }
        />
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <TradeChip label={trade.action} status={trade.action} size="small" />
          <ProfitTypography variant="subtitle1" profit={parseFloat(trade.profit)}>
            {parseFloat(trade.profit) > 0 ? '+' : ''}
            {trade.profit}
          </ProfitTypography>
          <IconButton size="small" onClick={() => setExpanded(!expanded)} aria-label="show more">
            {expanded ? <CaretUp weight="bold" size={20} /> : <CaretDown weight="bold" size={20} />}
          </IconButton>
        </Box>
      </LiveTradeListItem>
      <Collapse in={expanded} timeout="auto" unmountOnExit>
        <Box sx={{ margin: 2 }}>
          <TableContainer component={Paper} variant="outlined">
            <Table size="small">
              <TableBody>
                {detailRows.map((row) => (
                  <TableRow key={row.label}>
                    <TableCell component="th" scope="row" sx={{ fontWeight: 600 }}>
                      {row.label}
                    </TableCell>
                    <TableCell align="right">{row.value}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </Box>
      </Collapse>
    </>
  );
}

export function LiveTradeList({ trades = [], islive = true, isBacktestRunning = false }) {
  const [activeTab, setActiveTab] = React.useState(0);
  const [page, setPage] = React.useState(1);
  const tradesPerPage = 5;

  const openTrades = React.useMemo(
    () => trades.filter((trade) => !trade.closeTime && (!trade.closePrice || trade.closePrice === 0)),
    [trades]
  );

  const closedTrades = React.useMemo(
    () => trades.filter((trade) => trade.closeTime || (trade.closePrice && trade.closePrice !== 0)),
    [trades]
  );

  const currentTrades = activeTab === 0 ? openTrades : closedTrades;

  const sortedTrades = React.useMemo(() => {
    return [...currentTrades].sort((a, b) => b.openTime - a.openTime);
  }, [currentTrades]);

  const totalPages = Math.ceil(sortedTrades.length / tradesPerPage);
  const displayedTrades = sortedTrades.slice((page - 1) * tradesPerPage, page * tradesPerPage);

  const handlePageChange = (event, value) => {
    setPage(value);
  };

  const handleTabChange = (event, newValue) => {
    setActiveTab(newValue);
    setPage(1); // Reset to first page when switching tabs
  };

  const renderEmptyTradeMessage = () => (
    <Box
      sx={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        py: 4,
        px: 2,
        color: 'text.secondary',
        bgcolor: 'background.paper',
      }}
    >
      <ArrowsDownUp size={32} weight="light" style={{ marginBottom: 8 }} />
      <Typography variant="subtitle1" gutterBottom>
        {activeTab === 0 ? 'No Open Trades' : 'No Closed Trades'}
      </Typography>
      <Typography variant="body2" color="text.secondary" align="center">
        {activeTab === 0
          ? 'Trades will appear here when positions are opened'
          : 'Completed trades will be displayed here'}
      </Typography>
    </Box>
  );

  if (!trades.length) {
    return (
      <StyledCard>
        <CardHeader
          avatar={
            <Avatar>
              <ArrowsDownUp weight="bold" size={24} />
            </Avatar>
          }
          title={islive ? 'Live Trades' : 'Recent Trades'}
          subheader={isBacktestRunning ? 'Backtest run in progress. Waiting for result.' : 'No trades available'}
        />
        <Box sx={{ p: 2 }} />
      </StyledCard>
    );
  }

  return (
    <StyledCard>
      <CardHeader
        avatar={
          <Avatar>
            <ArrowsDownUp weight="bold" size={24} />
          </Avatar>
        }
        title={islive ? 'Live Trades' : 'Recent Trades'}
        subheader={
          currentTrades.length > 0
            ? `Showing ${(page - 1) * tradesPerPage + 1}-${Math.min(
                page * tradesPerPage,
                sortedTrades.length
              )} of ${sortedTrades.length} trades`
            : activeTab === 0
              ? 'No open trades'
              : 'No closed trades'
        }
      />

      {islive ? (
        <Box sx={{ borderBottom: 1, borderColor: 'divider', px: 2 }}>
          <Tabs value={activeTab} onChange={handleTabChange} aria-label="trade status tabs">
            <Tab label={`Open Trades (${openTrades.length})`} id="trades-tab-0" />
            <Tab label={`Closed Trades (${closedTrades.length})`} id="trades-tab-1" />
          </Tabs>
        </Box>
      ) : null}

      {currentTrades.length > 0 ? (
        <>
          <List disablePadding>
            {displayedTrades.map((trade) => (
              <TradeListItem
                key={trade.id}
                trade={trade}
                islive={!trade.closeTime && (!trade.closePrice || trade.closePrice === 0)}
              />
            ))}
          </List>

          {totalPages > 1 && (
            <Box
              sx={{
                display: 'flex',
                justifyContent: 'center',
                padding: 2,
              }}
            >
              <Pagination count={totalPages} page={page} onChange={handlePageChange} color="primary" />
            </Box>
          )}
        </>
      ) : (
        renderEmptyTradeMessage()
      )}
    </StyledCard>
  );
}

export function TradeList({ trades = [], isBacktestRunning }) {
  const [page, setPage] = React.useState(1);
  const tradesPerPage = 5;

  const sortedTrades = React.useMemo(() => {
    return [...trades].sort((a, b) => b.openTime - a.openTime);
  }, [trades]);

  const totalPages = Math.ceil(sortedTrades.length / tradesPerPage);

  const handlePageChange = (event, value) => {
    setPage(value);
  };

  const displayedTrades = sortedTrades.slice((page - 1) * tradesPerPage, page * tradesPerPage);

  if (!trades.length) {
    return (
      <StyledCard>
        <CardHeader
          avatar={
            <Avatar>
              <ArrowsDownUp weight="bold" size={24} />
            </Avatar>
          }
          title="Recent Trades"
          subheader={isBacktestRunning ? 'Backtest run in progress. Waiting for result.' : 'No data available'}
        />
        <Box sx={{ p: 2 }} />
      </StyledCard>
    );
  }

  return (
    <StyledCard>
      <CardHeader
        avatar={
          <Avatar>
            <ArrowsDownUp weight="bold" size={24} />
          </Avatar>
        }
        title="Recent Trades"
        subheader={`Showing ${(page - 1) * tradesPerPage + 1}-${Math.min(
          page * tradesPerPage,
          sortedTrades.length
        )} of ${sortedTrades.length} trades`}
      />
      <List disablePadding>
        {displayedTrades.map((trade) => (
          <TradeListItem key={trade.id} trade={trade} />
        ))}
      </List>
      <Box
        sx={{
          display: 'flex',
          justifyContent: 'center',
          padding: 2,
        }}
      >
        <Pagination count={totalPages} page={page} onChange={handlePageChange} color="primary" />
      </Box>
    </StyledCard>
  );
}
