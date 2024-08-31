import React, {useEffect, useMemo, useState} from 'react';
import {Backdrop, Box, Chip, CircularProgress, Grid, Paper, Tab, Table, TableBody, TableCell, TableContainer, TableHead, TablePagination, TableRow, TableSortLabel, Tabs, Tooltip, Typography,} from '@mui/material';
import {styled} from '@mui/material/styles';
import {apiClient} from '../api/apiClient.js'

const StyledTableContainer = styled(TableContainer)(({theme}) => ({
    flexGrow: 1,
    overflow: 'auto',
}));

const OptimizationResults = ({task}) => {
    const [results, setResults] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [tabValue, setTabValue] = useState(0);
    const [page, setPage] = useState(0);
    const [rowsPerPage, setRowsPerPage] = useState(9);
    const [orderBy, setOrderBy] = useState('totalNetProfit');
    const [order, setOrder] = useState('desc');

    useEffect(() => {
        const fetchResults = async () => {
            setLoading(true);
            try {
                const res = await apiClient.getTaskResults(task.id)
                setResults(res);
                setError(null);
            } catch (err) {
                setError('Failed to fetch results. Please try again later.');
                setResults([]);
            } finally {
                setLoading(false);
            }
        };

        fetchResults();
    }, [task]);

    const columns = [
        {id: 'strategyId', label: 'Strategy ID', minWidth: 100},
        {
            id: 'totalNetProfit',
            label: 'Total Net Profit',
            minWidth: 150,
            format: (value, row) => {
                const percentage = (value / row.output.stats?.initialDeposit) * 100;
                return `${value.toFixed(2)} (${percentage.toFixed(2)}%)`;
            }
        }, {id: 'profitFactor', label: 'Profit Factor', minWidth: 110, format: (value) => value.toFixed(2)},
        {id: 'totalTrades', label: 'Total Trades', minWidth: 100},
        {id: 'winPercentage', label: 'Win %', minWidth: 90, format: (value) => `${(value * 100).toFixed(2)}%`},
        {id: 'maxDrawdown', label: 'Max Drawdown', minWidth: 120, format: (value) => `${value.toFixed(2)}%`},
        {id: 'sharpeRatio', label: 'Sharpe Ratio', minWidth: 110, format: (value) => value.toFixed(2)},
        {id: 'parameters', label: 'Parameters', minWidth: 200},
    ];

    const failedColumns = [
        {id: 'strategyId', label: 'Strategy ID', minWidth: 100},
        {id: 'reason', label: 'Failure Reason', minWidth: 200},
        {id: 'parameters', label: 'Parameters', minWidth: 200},
    ];

    const successfulStrategies = results.filter(result => !result.output.failed);
    const failedStrategies = results.filter(result => result.output.failed);

    const handleChangePage = (event, newPage) => {
        setPage(newPage);
    };

    const handleChangeRowsPerPage = (event) => {
        setRowsPerPage(parseInt(event.target.value, 10));
        setPage(0);
    };

    const handleRequestSort = (property) => {
        const isAsc = orderBy === property && order === 'asc';
        setOrder(isAsc ? 'desc' : 'asc');
        setOrderBy(property);
    };

    const formatDate = (isoString) => {
        const date = new Date(isoString);

        if (isNaN(date.getTime())) {
            return 'Invalid Date';
        }

        const year = date.getUTCFullYear();
        const month = String(date.getUTCMonth() + 1).padStart(2, '0');
        const day = String(date.getUTCDate()).padStart(2, '0');

        return `${year}-${month}-${day}`;
    };

    const formatDuration = (seconds) => {
        const hours = Math.floor(seconds / 3600);
        const minutes = Math.floor((seconds % 3600) / 60);
        const remainingSeconds = seconds % 60;

        const parts = [];
        if (hours > 0) parts.push(`${hours}h`);
        if (minutes > 0) parts.push(`${minutes}m`);
        if (remainingSeconds > 0 || parts.length === 0) parts.push(`${remainingSeconds}s`);

        return parts.join(' ');
    };

    const sortedData = useMemo(() => {
        const comparator = (a, b) => {
            let aValue = a.output.stats?.[orderBy] ?? a.output[orderBy];
            let bValue = b.output.stats?.[orderBy] ?? b.output[orderBy];

            if (orderBy === 'winPercentage') {
                aValue = (a.output.stats?.totalLongWinningTrades + a.output.stats?.totalShortWinningTrades) / a.output.stats?.totalTrades;
                bValue = (b.output.stats?.totalLongWinningTrades + b.output.stats?.totalShortWinningTrades) / b.output.stats?.totalTrades;
            }

            if (aValue < bValue) return order === 'asc' ? -1 : 1;
            if (aValue > bValue) return order === 'asc' ? 1 : -1;
            return 0;
        };

        return [...(tabValue === 0 ? successfulStrategies : failedStrategies)].sort(comparator);
    }, [successfulStrategies, failedStrategies, order, orderBy, tabValue]);

    const renderTaskInfo = () => (
        <Paper elevation={1} sx={{p: 2, mb: 2}}>
            <Grid container spacing={2}>
                <Grid item xs={12} sm={6} md={3}>
                    <Typography variant="subtitle2">Symbol:</Typography>
                    <Typography>{task.config.instrument}</Typography>
                </Grid>
                <Grid item xs={12} sm={6} md={3}>
                    <Typography variant="subtitle2">Period:</Typography>
                    <Typography>{formatDuration(task.config.period)}</Typography>
                </Grid>
                <Grid item xs={12} sm={6} md={3}>
                    <Typography variant="subtitle2">Timeframe:</Typography>
                    <Typography>
                        {formatDate(task.config.timeframe.from)} to {formatDate(task.config.timeframe.to)}
                    </Typography>
                </Grid>
                <Grid item xs={12} sm={6} md={3}>
                    <Typography variant="subtitle2">Strategy:</Typography>
                    <Typography>{task.config.strategyClass}</Typography>
                </Grid>
            </Grid>
        </Paper>
    );


    const renderTable = (strategies, tableColumns) => (
        <Paper sx={{display: 'flex', flexDirection: 'column', height: '100%'}}>
            <StyledTableContainer>
                <Table stickyHeader aria-label="sticky table">
                    <TableHead>
                        <TableRow>
                            {tableColumns.map((column) => (
                                <TableCell
                                    key={column.id}
                                    style={{minWidth: column.minWidth}}
                                    sortDirection={orderBy === column.id ? order : false}
                                >
                                    <TableSortLabel
                                        active={orderBy === column.id}
                                        direction={orderBy === column.id ? order : 'asc'}
                                        onClick={() => handleRequestSort(column.id)}
                                    >
                                        {column.label}
                                    </TableSortLabel>
                                </TableCell>
                            ))}
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {strategies
                            .slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)
                            .map((row) => (
                                <TableRow hover role="checkbox" tabIndex={-1} key={row.output.strategyId}>
                                    {tableColumns.map((column) => {
                                        const value = column.id === 'strategyId'
                                            ? row.output.strategyId
                                            : column.id === 'parameters'
                                                ? JSON.stringify(row.parameters)
                                                : column.id === 'winPercentage'
                                                    ? (row.output.stats?.totalLongWinningTrades + row.output.stats?.totalShortWinningTrades) / row.output.stats?.totalTrades
                                                    : column.id === 'reason'
                                                        ? row.output.reason
                                                        : row.output.stats?.[column.id];
                                        return (
                                            <TableCell key={column.id}>
                                                {column.format && typeof value === 'number'
                                                    ? column.format(value, row)  // Pass both value and row
                                                    : column.id === 'parameters'
                                                        ? (
                                                            <Tooltip title={value} placement="top">
                                                                <Chip label="View Parameters" variant="outlined"/>
                                                            </Tooltip>
                                                        )
                                                        : value}
                                            </TableCell>
                                        );
                                    })}
                                </TableRow>
                            ))}
                    </TableBody>
                </Table>
            </StyledTableContainer>
            <TablePagination
                rowsPerPageOptions={[10]}
                component="div"
                count={strategies.length}
                rowsPerPage={rowsPerPage}
                page={page}
                onPageChange={handleChangePage}
                onRowsPerPageChange={handleChangeRowsPerPage}
            />
        </Paper>
    );

    if (loading) {
        return (
            <Backdrop open={true} style={{zIndex: 9999, color: '#fff'}}>
                <CircularProgress color="inherit"/>
            </Backdrop>
        );
    }

    if (error) {
        return <Typography color="error">{error}</Typography>;
    }

    return (
        <Box sx={{display: 'flex', flexDirection: 'column', height: '100%'}}>
            {renderTaskInfo()}
            <Tabs value={tabValue} onChange={(_, newValue) => setTabValue(newValue)}>
                <Tab label={`Successful Strategies (${successfulStrategies.length})`}/>
                <Tab label={`Failed Strategies (${failedStrategies.length})`}/>
            </Tabs>
            {renderTable(sortedData, tabValue === 0 ? columns : failedColumns)}
        </Box>
    );
};

export default OptimizationResults;