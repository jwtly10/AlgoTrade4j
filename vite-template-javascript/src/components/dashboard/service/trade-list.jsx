import * as React from 'react';
import { Avatar, Box, Card, CardHeader, Chip, Collapse, Grid, IconButton, List, ListItem, ListItemAvatar, ListItemText, Pagination, Paper, Table, TableBody, TableCell, TableContainer, TableRow, Typography } from '@mui/material';
import { styled } from '@mui/material/styles';
import { ArrowsDownUp, CaretDown, CaretUp, TrendDown, TrendUp } from '@phosphor-icons/react';
import dayjs from 'dayjs';





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

function TradeListItem({ trade }) {
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
      <ListItem divider>
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
            <Typography variant="subtitle1">
              {trade.isLong ? 'BUY' : 'SELL'} {trade.instrument}
            </Typography>
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
      </ListItem>
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