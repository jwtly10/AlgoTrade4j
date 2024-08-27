import React, {useState} from 'react';
import {Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, Paper, Tab, Table, TableBody, TableCell, TableContainer, TableHead, TablePagination, TableRow, TableSortLabel, Tabs, Typography,} from '@mui/material';

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

    const sortedStrategies = (strategies) => {
        if (tabValue === 1) return strategies; // Don't sort failed strategies
        return [...strategies].sort((a, b) => {
            if (b.stats[orderBy]?.value < a.stats[orderBy]?.value) {
                return order === 'asc' ? 1 : -1;
            }
            if (b.stats[orderBy]?.value > a.stats[orderBy]?.value) {
                return order === 'asc' ? -1 : 1;
            }
            return 0;
        });
    };

    const renderSuccessfulStrategiesTable = (strategies) => {
        const sortedData = sortedStrategies(strategies);

        return (
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
                                    active={orderBy === 'longWinPercentage'}
                                    direction={orderBy === 'longWinPercentage' ? order : 'asc'}
                                    onClick={() => handleRequestSort('longWinPercentage')}
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
                                <TableCell>{strategy.stats?.longWinPercentage?.value.toFixed(2) || 'N/A'}%</TableCell>
                                <TableCell>{JSON.stringify(strategy.parameters)}</TableCell>
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
        );
    };

    const renderFailedStrategiesTable = (strategies) => {
        return (
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
                                <TableCell>{JSON.stringify(strategy.parameters)}</TableCell>
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
        );
    };

    return (
        <Box sx={{width: '100%', bgcolor: 'background.paper'}}>
            <Tabs value={tabValue} onChange={handleChangeTab} centered>
                <Tab label="Successful Strategy Runs"/>
                <Tab label="Failed Strategy Runs"/>
            </Tabs>
            {tabValue === 0 && renderSuccessfulStrategiesTable(data.successfulStrategies)}
            {tabValue === 1 && renderFailedStrategiesTable(data.failedStrategies)}

            <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="md" fullWidth sx={{p: 3}}>
                <DialogTitle sx={{p: 3}}>Detailed Results</DialogTitle>
                <DialogContent sx={{p: 3}}>
                    {selectedRun && (
                        <Table>
                            <TableBody>
                                {Object.entries(selectedRun.stats).map(([key, value]) => (
                                    <TableRow key={key}>
                                        <TableCell>{key}</TableCell>
                                        <TableCell>{typeof value === 'object' ? value.value : value}</TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                    )}
                </DialogContent>
                <DialogActions sx={{p: 3}}>
                    <Button onClick={handleCloseDialog}>Close</Button>
                </DialogActions>
            </Dialog>
        </Box>
    );
};

export default OptimizationResults;