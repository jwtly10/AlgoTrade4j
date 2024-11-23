import React, {useState, useEffect} from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  IconButton,
  Typography,
  Box,
  Button,
  CircularProgress,
  Card,
  CardContent,
  Chip,
  Stack,
  Grid,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
} from '@mui/material';
import {
  X,
  Code,
  CaretUp,
  CaretDown,
  CheckCircle,
  XCircle,
  Plus,
  ArrowLeft,
  Gear,
} from '@phosphor-icons/react';
import {optimisationClient} from '@/lib/api/clients/optimisation-client';
import {toast} from 'react-toastify';

const METRICS = [
  {
    key: 'totalNetProfit',
    label: 'Net Profit',
    format: (val) => `$${val.toFixed(2)}`,
    type: 'currency'
  },
  {
    key: 'profitFactor',
    label: 'Profit Factor',
    format: (val) => val.toFixed(2)
  },
  {
    key: 'winRate',
    label: 'Win Rate',
    format: (val) => `${val.toFixed(1)}%`,
    calculate: (stats) => (
      ((stats.totalLongWinningTrades + stats.totalShortWinningTrades) / stats.totalTrades) * 100
    )
  },
  {
    key: 'maxDrawdown',
    label: 'Max Drawdown',
    format: (val) => `${val.toFixed(2)}%`
  },
  {
    key: 'sharpeRatio',
    label: 'Sharpe Ratio',
    format: (val) => val.toFixed(3)
  },
  {
    key: 'totalTrades',
    label: 'Total Trades',
    format: (val) => val
  },
];

const formatPeriod = (period) => {
  if (period < 60) return `${period}SEC`;
  if (period < 3600) return `${period / 60}MIN`;
  if (period < 86400) return `${period / 3600}H`;
  return `${period / 86400}D`;
};

function OptimisationResults({task, open, onClose}) {
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(false);
  const [selectedResult, setSelectedResult] = useState(null);
  const [view, setView] = useState('grid');
  const [sortField, setSortField] = useState('totalNetProfit');
  const [sortDirection, setSortDirection] = useState('desc');
  const [displayCount, setDisplayCount] = useState(10);
  const [showOnlyFailed, setShowOnlyFailed] = useState(false);

  useEffect(() => {
    if (open && task) {
      const fetchResults = async () => {
        setLoading(true);
        try {
          const data = await optimisationClient.getTaskResults(task.id);
          setResults(Array.isArray(data) ? data : []);
        } catch (error) {
          toast.error(`Error fetching results: ${error.message}`);
        } finally {
          setLoading(false);
        }
      };
      fetchResults();
    }
  }, [open, task]);

  const sortResults = (resultsList) => {
    const filteredList = showOnlyFailed ?
      resultsList.filter(r => r.failed || !r.output?.stats) :
      resultsList;

    return [...filteredList].sort((a, b) => {
      let aVal = a.output?.stats?.[sortField] ?? -Infinity;
      let bVal = b.output?.stats?.[sortField] ?? -Infinity;

      if (sortField === 'winRate') {
        aVal = a.output?.stats ?
          ((a.output.stats.totalLongWinningTrades + a.output.stats.totalShortWinningTrades) /
            a.output.stats.totalTrades * 100) : -Infinity;
        bVal = b.output?.stats ?
          ((b.output.stats.totalLongWinningTrades + b.output.stats.totalShortWinningTrades) /
            b.output.stats.totalTrades * 100) : -Infinity;
      }

      return sortDirection === 'desc' ? bVal - aVal : aVal - bVal;
    });
  };

  const TaskSummary = ({task}) => (
    <Card variant="outlined" sx={{mb: 3}}>
      <CardContent>
        <Grid container spacing={3}>
          <Grid container item xs={12} spacing={4}>
            <Grid item xs={6} md={2}>
              <Typography variant="subtitle2" color="textSecondary">Strategy</Typography>
              <Typography variant="body1">{task.config.strategyClass}</Typography>
            </Grid>

            <Grid item xs={6} md={2}>
              <Typography variant="subtitle2" color="textSecondary">Instrument</Typography>
              <Typography variant="body1">{task.config.instrument}</Typography>
            </Grid>

            <Grid item xs={6} md={2}>
              <Typography variant="subtitle2" color="textSecondary">Timeframe</Typography>
              <Typography variant="body1">{formatPeriod(task.config.period)}</Typography>
            </Grid>

            <Grid item xs={6} md={2}>
              <Typography variant="subtitle2" color="textSecondary">Initial Capital</Typography>
              <Typography variant="body1">${task.config.initialCash.toLocaleString()}</Typography>
            </Grid>

            <Grid item xs={12} md={4}>
              <Typography variant="subtitle2" color="textSecondary">Backtest Period</Typography>
              <Typography variant="body1">
                {new Date(task.config.timeframe.from).toLocaleDateString()} → {new Date(task.config.timeframe.to).toLocaleDateString()}
              </Typography>
            </Grid>
          </Grid>
        </Grid>
      </CardContent>
    </Card>
  );


  const ResultCard = ({result}) => {
    const stats = result.output?.stats;
    const failed = result.failed || !stats;

    return (
      <Card variant="outlined">
        <CardContent>
          <Grid container spacing={2} alignItems="center">
            {/* Status and ID */}
            <Grid item xs={2}>
              <Stack direction="row" spacing={1} alignItems="center">
                {failed ? (
                  <XCircle size={20} color="#dc2626" weight="fill"/>
                ) : (
                  <CheckCircle size={20} color="#16a34a" weight="fill"/>
                )}
                <Typography variant="subtitle2">
                  Run #{result.id}
                </Typography>
              </Stack>
            </Grid>

            {/* Metrics */}
            {failed ? (
              <Grid item xs={8}>
                <Typography color="error">
                  {`Optimisation failed: '${result.output.reason ? result.output.reason : 'Unknown error'}'`}
                </Typography>
              </Grid>
            ) : (
              <Grid item xs={8}>
                <Grid
                  container
                  spacing={3}
                  sx={{
                    width: '100%',
                    justifyContent: 'space-between'
                  }}
                >
                  {METRICS.map(metric => {
                    const value = metric.calculate ?
                      metric.calculate(stats) :
                      stats[metric.key];

                    return (
                      <Grid
                        item
                        key={metric.key}
                        xs={4}
                        md={12 / METRICS.length}  // This ensures even distribution
                        sx={{
                          display: 'flex',
                          flexDirection: 'column',
                          alignItems: 'flex-start'
                        }}
                      >
                        <Typography variant="caption" color="textSecondary">
                          {metric.label}
                        </Typography>
                        <Typography
                          variant="body2"
                          sx={{
                            color: metric.type === 'currency' ?
                              (value < 0 ? 'error.main' : 'success.main') :
                              'text.primary'
                          }}
                        >
                          {metric.format(value)}
                        </Typography>
                      </Grid>
                    );
                  })}
                </Grid>
              </Grid>
            )}

            {/* Actions */}
            <Grid item xs={2}>
              <Stack direction="row" spacing={1} justifyContent="flex-end">
                <IconButton
                  size="small"
                  onClick={() => {
                    setSelectedResult(result);
                    setView('parameters');
                  }}
                >
                  <Gear/>
                </IconButton>
                <IconButton
                  size="small"
                  onClick={() => {
                    setSelectedResult(result);
                    setView('raw');
                  }}
                >
                  <Code/>
                </IconButton>
              </Stack>
            </Grid>
          </Grid>
        </CardContent>
      </Card>
    );
  };

  const ParametersView = ({result}) => (
    <TableContainer component={Paper} sx={{mt: 2}}>
      <Table size="small">
        <TableHead>
          <TableRow>
            <TableCell>Parameter</TableCell>
            <TableCell>Value</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {Object.entries(result.parameters).map(([key, value]) => (
            <TableRow key={key}>
              <TableCell>{key}</TableCell>
              <TableCell>{value}</TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  );

  return (
    <Dialog
      open={open}
      onClose={onClose}
      maxWidth="xl"
      fullWidth
      PaperProps={{
        sx: {
          height: '60vh',
        }
      }}
    >
      <DialogTitle sx={{m: 0, p: 2, display: 'flex', alignItems: 'center', justifyContent: 'space-between'}}>
        {view !== 'grid' && (
          <IconButton
            onClick={() => {
              setView('grid');
              setSelectedResult(null);
            }}
            sx={{mr: 1}}
          >
            <ArrowLeft/>
          </IconButton>
        )}
        <Typography>
          {view === 'grid' ? `Optimization Results: Task ${task.id}` :
            view === 'raw' ? `Raw Data: Run #${selectedResult?.id}` :
              `Parameters: Run #${selectedResult?.id}`}
        </Typography>
        <IconButton onClick={onClose} size="small" aria-label="close">
          <X/>
        </IconButton>
      </DialogTitle>

      <DialogContent dividers>
        {loading ? (
          <Box display="flex" justifyContent="center" alignItems="center" minHeight={400}>
            <CircularProgress/>
          </Box>
        ) : view === 'raw' && selectedResult ? (
          <Paper sx={{p: 2, mt: 2}}>
            <pre style={{margin: 0}}>{JSON.stringify(selectedResult, null, 2)}</pre>
          </Paper>
        ) : view === 'parameters' && selectedResult ? (
          <ParametersView result={selectedResult}/>
        ) : results.length > 0 ? (
          <>
            <Box sx={{overflowX: 'auto'}}>
              <TaskSummary task={task}/>
              <Stack
                direction="row"
                spacing={1}
                sx={{
                  mb: 2,
                  flexWrap: 'wrap',
                  gap: 1
                }}
              >
                <Chip
                  label="Failed Only"
                  onClick={() => setShowOnlyFailed(!showOnlyFailed)}
                  icon={<XCircle size={16}/>}
                  color={showOnlyFailed ? "error" : "default"}
                  variant={showOnlyFailed ? "filled" : "outlined"}
                />
                {METRICS.map(metric => (
                  <Chip
                    key={metric.key}
                    label={metric.label}
                    onClick={() => {
                      if (sortField === metric.key) {
                        setSortDirection(sortDirection === 'asc' ? 'desc' : 'asc');
                      } else {
                        setSortField(metric.key);
                        setSortDirection('desc');
                      }
                    }}
                    icon={sortField === metric.key ?
                      (sortDirection === 'asc' ? <CaretUp/> : <CaretDown/>) :
                      undefined}
                    color={sortField === metric.key ? "primary" : "default"}
                    variant={sortField === metric.key ? "filled" : "outlined"}
                  />
                ))}
              </Stack>
            </Box>

            {/* Get filtered results first */}
            {(() => {
              const filteredResults = sortResults(results);

              if (showOnlyFailed && filteredResults.length === 0) {
                return (
                  <Box display="flex" justifyContent="center" alignItems="center" minHeight={400}>
                    <Typography color="error">No failed results found</Typography>
                  </Box>
                );
              }

              return (
                <>
                  <Stack spacing={1}>
                    {filteredResults
                      .slice(0, displayCount)
                      .map((result) => (
                        <ResultCard key={result.id} result={result}/>
                      ))}
                  </Stack>

                  {filteredResults.length > displayCount && (
                    <Box sx={{textAlign: 'center', mt: 2}}>
                      <Button
                        onClick={() => setDisplayCount(prev => prev + 10)}
                        startIcon={<Plus/>}
                        variant="outlined"
                      >
                        Show More Results
                      </Button>
                    </Box>
                  )}
                </>
              );
            })()}
          </>
        ) : (
          <Box display="flex" justifyContent="center" alignItems="center" minHeight={400}>
            <Typography color="error">No results found</Typography>
          </Box>
        )}
      </DialogContent>
    </Dialog>
  );
}

export default OptimisationResults;
