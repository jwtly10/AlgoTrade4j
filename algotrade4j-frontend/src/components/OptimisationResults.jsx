import React, {useState} from 'react';
import {Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, Grid, Paper, Tab, Table, TableBody, TableCell, TableContainer, TableHead, TablePagination, TableRow, TableSortLabel, Tabs, Typography,} from '@mui/material';
import TruncatedParams from './TruncatedParams.jsx'

const OptimizationResults = ({data}) => {
    const [page, setPage] = useState(0);
    const [rowsPerPage, setRowsPerPage] = useState(10);
    const [tabValue, setTabValue] = useState(0);
    const [orderBy, setOrderBy] = useState('totalNetProfit');
    const [order, setOrder] = useState('desc');
    const [openDialog, setOpenDialog] = useState(false);
    const [selectedRun, setSelectedRun] = useState(null);

    const handleChangePage = (event, newPage) => {
        setPage(newPage);
    };

    const handleChangeRowsPerPage = (event) => {
        setRowsPerPage(parseInt(event.target.value, 10));
        setPage(0);
    };

    const handleChangeTab = (event, newValue) => {
        setTabValue(newValue);
        setPage(0);
    };

    const handleRequestSort = (property) => {
        const isAsc = orderBy === property && order === 'asc';
        setOrder(isAsc ? 'desc' : 'asc');
        setOrderBy(property);
    };

    const handleOpenDialog = (run) => {
        setSelectedRun(run);
        setOpenDialog(true);
    };

    const handleCloseDialog = () => {
        setOpenDialog(false);
    };

    const calculateWinPercentage = (strategy) => {
        const totalWinningTrades = strategy.stats.totalLongWinningTrades + strategy.stats.totalShortWinningTrades;
        const totalTrades = strategy.stats.totalTrades;
        return totalTrades > 0 ? (totalWinningTrades / totalTrades) * 100 : 0;
    };

    const sortedStrategies = (strategies) => {
        if (tabValue === 1) return strategies; // Don't sort failed strategies
        return [...strategies].sort((a, b) => {
            if (orderBy === 'winPercentage') {
                const aValue = calculateWinPercentage(a);
                const bValue = calculateWinPercentage(b);
                if (bValue < aValue) {
                    return order === 'asc' ? 1 : -1;
                }
                if (bValue > aValue) {
                    return order === 'asc' ? -1 : 1;
                }
                return 0;
            } else {
                if (b.stats[orderBy]?.value < a.stats[orderBy]?.value) {
                    return order === 'asc' ? 1 : -1;
                }
                if (b.stats[orderBy]?.value > a.stats[orderBy]?.value) {
                    return order === 'asc' ? -1 : 1;
                }
                return 0;
            }
        });
    };


    const renderSuccessfulStrategiesTable = (strategies) => {
        const sortedData = sortedStrategies(strategies);

        return (
            <Box sx={{height: 'calc(100vh - 300px)', display: 'flex', flexDirection: 'column'}}>
            <TableContainer component={Paper} sx={{mt: 2, height: '100%', overflow: 'auto'}}>
                <Typography variant="h6" sx={{p: 2}}>Successful Strategies</Typography>
                <Table size="small">
                    <TableHead>
                        <TableRow>
                            <TableCell>
                                <TableSortLabel
                                    active={orderBy === 'totalNetProfit'}
                                    direction={orderBy === 'totalNetProfit' ? order : 'asc'}
                                    onClick={() => handleRequestSort('totalNetProfit')}
                                >
                                    Profit
                                </TableSortLabel>
                            </TableCell>
                            <TableCell>
                                <TableSortLabel
                                    active={orderBy === 'maxDrawdown'}
                                    direction={orderBy === 'maxDrawdown' ? order : 'asc'}
                                    onClick={() => handleRequestSort('maxDrawdown')}
                                >
                                    Max Drawdown
                                </TableSortLabel>
                            </TableCell>
                            <TableCell>
                                <TableSortLabel
                                    active={orderBy === 'profitFactor'}
                                    direction={orderBy === 'profitFactor' ? order : 'asc'}
                                    onClick={() => handleRequestSort('profitFactor')}
                                >
                                    Profit Factor
                                </TableSortLabel>
                            </TableCell>
                            <TableCell>
                                <TableSortLabel
                                    active={orderBy === 'sharpeRatio'}
                                    direction={orderBy === 'sharpeRatio' ? order : 'asc'}
                                    onClick={() => handleRequestSort('sharpeRatio')}
                                >
                                    Sharpe Ratio
                                </TableSortLabel>
                            </TableCell>
                            <TableCell>
                                <TableSortLabel
                                    active={orderBy === 'expectedPayoff'}
                                    direction={orderBy === 'expectedPayoff' ? order : 'asc'}
                                    onClick={() => handleRequestSort('expectedPayoff')}
                                >
                                    Expected Payoff
                                </TableSortLabel>
                            </TableCell>
                            <TableCell>
                                <TableSortLabel
                                    active={orderBy === 'winPercentage'}
                                    direction={orderBy === 'winPercentage' ? order : 'asc'}
                                    onClick={() => handleRequestSort('winPercentage')}
                                >
                                    Win Rate
                                </TableSortLabel>
                            </TableCell>
                            <TableCell>Parameters</TableCell>
                            <TableCell>Actions</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {sortedData.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage).map((strategy) => (
                            <TableRow key={strategy.strategyId}>
                                <TableCell>{strategy.stats?.totalNetProfit?.value.toFixed(2) || 'N/A'}</TableCell>
                                <TableCell>{strategy.stats?.maxDrawdown?.value.toFixed(2) || 'N/A'}%</TableCell>
                                <TableCell>{strategy.stats?.profitFactor?.value.toFixed(2) || 'N/A'}</TableCell>
                                <TableCell>{strategy.stats?.sharpeRatio?.value.toFixed(4) || 'N/A'}</TableCell>
                                <TableCell>{strategy.stats?.expectedPayoff?.value.toFixed(2) || 'N/A'}</TableCell>
                                <TableCell>
                                    {((strategy.stats?.totalLongWinningTrades + strategy.stats?.totalShortWinningTrades) /
                                        strategy.stats?.totalTrades * 100).toFixed(2) || 'N/A'}%
                                </TableCell>
                                <TableCell><TruncatedParams params={strategy.parameters}/></TableCell>
                                <TableCell>
                                    <Button onClick={() => handleOpenDialog(strategy)}>View Details</Button>
                                </TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
                <TablePagination
                    rowsPerPageOptions={[5, 10, 25]}
                    component="div"
                    count={strategies.length}
                    rowsPerPage={rowsPerPage}
                    page={page}
                    onPageChange={handleChangePage}
                    onRowsPerPageChange={handleChangeRowsPerPage}
                />
            </TableContainer>
            </Box>
        );
    };

    const renderFailedStrategiesTable = (strategies) => {
        return (
            <Box sx={{height: 'calc(100vh - 300px)', display: 'flex', flexDirection: 'column'}}>
            <TableContainer component={Paper} sx={{mt: 2, height: '100%', overflow: 'auto'}}>
                <Typography variant="h6" sx={{p: 2}}>Failed Strategies</Typography>
                <Table size="small">
                    <TableHead>
                        <TableRow>
                            <TableCell>Strategy ID</TableCell>
                            <TableCell>Failure Reason</TableCell>
                            <TableCell>Parameters</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {strategies.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage).map((strategy) => (
                            <TableRow key={strategy.strategyId}>
                                <TableCell>{strategy.strategyId}</TableCell>
                                <TableCell>{strategy.failureReason}</TableCell>
                                <TableCell><TruncatedParams params={strategy.parameters}/></TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
                <TablePagination
                    rowsPerPageOptions={[5, 10, 25]}
                    component="div"
                    count={strategies.length}
                    rowsPerPage={rowsPerPage}
                    page={page}
                    onPageChange={handleChangePage}
                    onRowsPerPageChange={handleChangeRowsPerPage}
                />
            </TableContainer>
            </Box>
        );
    };
    const camelCaseToWords = (s) => {
        const result = s.replace(/([A-Z])/g, ' $1');
        return result.charAt(0).toUpperCase() + result.slice(1);
    };

    const formatValue = (value) => {
        if (typeof value === 'number') {
            return value.toFixed(2);
        }
        return value;
    };

    return (
        <Box sx={{width: '100%', bgcolor: 'background.paper', p: 3}}>
            <Paper elevation={3} sx={{borderRadius: 2, overflow: 'hidden'}}>
            <Tabs value={tabValue} onChange={handleChangeTab} centered>
                <Tab label="Successful Strategy Runs"/>
                <Tab label="Failed Strategy Runs"/>
            </Tabs>
                {tabValue === 0 && renderSuccessfulStrategiesTable(data.successfulStrategies)}
                {tabValue === 1 && renderFailedStrategiesTable(data.failedStrategies)}
            </Paper>

            <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="md" fullWidth>
                <DialogTitle sx={{borderBottom: 1, borderColor: 'divider', pb: 2}}>
                    Detailed Results
                </DialogTitle>
                <DialogContent sx={{p: 3}}>
                    {selectedRun && (
                        <>
                            <Grid container spacing={3} sx={{p: 3}}>
                                {Object.entries(selectedRun.stats).map(([key, value]) => (
                                    <Grid item xs={12} sm={6} md={4} key={key}>
                                        <Paper elevation={2} sx={{p: 2, height: '100%'}}>
                                            <Typography
                                                variant="overline"
                                                sx={{
                                                    mb: 1,
                                                    lineHeight: 1.2,
                                                    height: '2.4em',
                                                    overflow: 'hidden',
                                                    textOverflow: 'ellipsis',
                                                    display: '-webkit-box',
                                                    WebkitLineClamp: 2,
                                                    WebkitBoxOrient: 'vertical',
                                                }}
                                            >
                                                {camelCaseToWords(key)}
                                            </Typography>
                                            <Typography variant="h6">
                                                {formatValue(typeof value === 'object' ? value.value : value)}
                                            </Typography>
                                        </Paper>
                                    </Grid>
                                ))}
                            </Grid>
                        </>
                    )}
                </DialogContent>
                <DialogActions sx={{borderTop: 1, borderColor: 'divider', pt: 2}}>
                    <Button onClick={handleCloseDialog} variant="outlined">Close</Button>
                </DialogActions>
            </Dialog>
        </Box>
    );
};

export default OptimizationResults;