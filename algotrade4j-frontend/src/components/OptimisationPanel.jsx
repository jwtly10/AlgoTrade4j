import React, {useEffect, useState} from 'react';
import {client} from '../api/client';
import {Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, TableSortLabel} from "@mui/material";

const STORAGE_KEY = 'latestOptimisationResults';

export const OptimisationPanel = ({setToast, optimisationId}) => {
    const [optimisationResults, setOptimisationResults] = useState(null);
    const [orderBy, setOrderBy] = useState('totalNetProfit');
    const [order, setOrder] = useState('desc');
    const [openDialog, setOpenDialog] = useState(false);
    const [selectedRun, setSelectedRun] = useState(null);

    useEffect(() => {
        // Load results from local storage when component mounts or optimisationId changes
        const storedData = localStorage.getItem(STORAGE_KEY);
        if (storedData) {
            const {id, results} = JSON.parse(storedData);
            if (id === optimisationId) {
                setOptimisationResults(results);
            } else {
                setOptimisationResults(null);
            }
        } else {
            setOptimisationResults(null);
        }
    }, [optimisationId]);

    const handleRefresh = async () => {
        if (optimisationId !== "") {
            try {
                const results = await client.getOptimisationResults(optimisationId);
                setOptimisationResults(results);
                localStorage.setItem(STORAGE_KEY, JSON.stringify({
                    id: optimisationId,
                    results: results
                }));
            } catch (error) {
                setToast({
                    open: true,
                    message: 'Failed to fetch optimisation results: ' + error.response.data.message,
                    level: 'error'
                })
            }
        }
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

    const sortedResults = optimisationResults ? [...optimisationResults].sort((a, b) => {
        if (b.stats[orderBy].value < a.stats[orderBy].value) {
            return order === 'asc' ? 1 : -1;
        }
        if (b.stats[orderBy].value > a.stats[orderBy].value) {
            return order === 'asc' ? -1 : 1;
        }
        return 0;
    }) : [];

    return (
        <div>
            <Button onClick={handleRefresh} disabled={!optimisationId}>
                Refresh results
            </Button>
            {optimisationResults ? (
                <TableContainer component={Paper}>
                    <Table>
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
                                        active={orderBy === 'shortWinPercentage'}
                                        direction={orderBy === 'shortWinPercentage' ? order : 'asc'}
                                        onClick={() => handleRequestSort('shortWinPercentage')}
                                    >
                                        Win Rate
                                    </TableSortLabel>
                                </TableCell>
                                <TableCell>Parameters</TableCell>
                                <TableCell>Actions</TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {sortedResults.map((run) => (
                                <TableRow key={run.strategyId}>
                                    <TableCell>{run.stats.totalNetProfit.value.toFixed(2)}</TableCell>
                                    <TableCell>{run.stats.maxDrawdown.value.toFixed(2)}</TableCell>
                                    <TableCell>{run.stats.profitFactor.value.toFixed(2)}</TableCell>
                                    <TableCell>{run.stats.sharpeRatio.value.toFixed(2)}</TableCell>
                                    <TableCell>{run.stats.expectedPayoff.value.toFixed(2)}</TableCell>
                                    <TableCell>{run.stats.shortWinPercentage.value.toFixed(2)}%</TableCell>
                                    <TableCell>{JSON.stringify(run.parameters)}</TableCell>
                                    <TableCell>
                                        <Button onClick={() => handleOpenDialog(run)}>View Details</Button>
                                    </TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                </TableContainer>
            ) : (
                <Box sx={{mt: 2, p: 2, textAlign: 'center'}}>
                    {/* TODO: We *should* have a progress tracker for this */}
                    <p>No optimization results available yet. Please start a new run or wait</p>
                </Box>
            )}
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
        </div>
    );
};