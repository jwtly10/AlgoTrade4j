import React from 'react';
import {
    Grid,
    Paper,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    Typography,
} from '@mui/material';

const AnalysisReport = ({ data }) => {
    const formatCurrency = (value) => {
        return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(value);
    };

    const formatPercentage = (value) => {
        return `${value}%`;
    };

    return (
        <div>
            <Grid container spacing={3}>
                <Grid item xs={12} md={6}>
                    <TableContainer component={Paper}>
                        <Table>
                            <TableHead>
                                <TableRow>
                                    <TableCell colSpan={2}>
                                        <Typography variant="h6">General Statistics</Typography>
                                    </TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                <TableRow>
                                    <TableCell>Initial Deposit</TableCell>
                                    <TableCell>
                                        {formatCurrency(data.stats.initialDeposit)}
                                    </TableCell>
                                </TableRow>
                                <TableRow>
                                    <TableCell>Total Net Profit</TableCell>
                                    <TableCell>
                                        {formatCurrency(data.stats.totalNetProfit)}
                                    </TableCell>
                                </TableRow>
                                <TableRow>
                                    <TableCell>Gross Profit</TableCell>
                                    <TableCell>
                                        {formatCurrency(data.stats.grossProfit)}
                                    </TableCell>
                                </TableRow>
                                <TableRow>
                                    <TableCell>Gross Loss</TableCell>
                                    <TableCell>
                                        {formatCurrency(data.stats.grossLoss)}
                                    </TableCell>
                                </TableRow>
                                <TableRow>
                                    <TableCell>Profit Factor</TableCell>
                                    <TableCell>
                                        {data.stats.profitFactor.toFixed(2)}
                                    </TableCell>
                                </TableRow>
                                <TableRow>
                                    <TableCell>Expected Payoff</TableCell>
                                    <TableCell>
                                        {formatCurrency(data.stats.expectedPayoff)}
                                    </TableCell>
                                </TableRow>
                                <TableRow>
                                    <TableCell>Max Drawdown</TableCell>
                                    <TableCell>
                                        {formatPercentage(data.stats.maxDrawdown.toFixed(2))}
                                    </TableCell>
                                </TableRow>
                                <TableRow>
                                    <TableCell>Sharpe Ratio</TableCell>
                                    <TableCell>{data.stats.sharpeRatio.toFixed(2)}</TableCell>
                                </TableRow>
                                <TableRow>
                                    <TableCell>Ticks Modelled</TableCell>
                                    <TableCell>{data.stats.ticksModelled}</TableCell>
                                </TableRow>
                            </TableBody>
                        </Table>
                    </TableContainer>
                </Grid>

                <Grid item xs={12} md={6}>
                    <TableContainer component={Paper}>
                        <Table>
                            <TableHead>
                                <TableRow>
                                    <TableCell colSpan={2}>
                                        <Typography variant="h6">Trade Statistics</Typography>
                                    </TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                <TableRow>
                                    <TableCell>Total Trades</TableCell>
                                    <TableCell>{data.stats.totalTrades}</TableCell>
                                </TableRow>
                                <TableRow>
                                    <TableCell>Long Trades (Win %)</TableCell>
                                    <TableCell>
                                        {data.stats.totalLongTrades} (
                                        {formatPercentage(data.stats.longWinPercentage.toFixed(2))})
                                    </TableCell>
                                </TableRow>
                                <TableRow>
                                    <TableCell>Short Trades (Win %)</TableCell>
                                    <TableCell>
                                        {data.stats.totalShortTrades} (
                                        {formatPercentage(data.stats.shortWinPercentage.toFixed(2))})
                                    </TableCell>
                                </TableRow>
                                <TableRow>
                                    <TableCell>Largest Profitable Trade</TableCell>
                                    <TableCell>
                                        {formatCurrency(data.stats.largestProfitableTrade)}
                                    </TableCell>
                                </TableRow>
                                <TableRow>
                                    <TableCell>Largest Losing Trade</TableCell>
                                    <TableCell>
                                        {formatCurrency(data.stats.largestLosingTrade)}
                                    </TableCell>
                                </TableRow>
                                <TableRow>
                                    <TableCell>Average Profitable Trade</TableCell>
                                    <TableCell>
                                        {formatCurrency(
                                            data.stats.averageProfitableTradeReturn
                                        )}
                                    </TableCell>
                                </TableRow>
                                <TableRow>
                                    <TableCell>Average Losing Trade</TableCell>
                                    <TableCell>
                                        {formatCurrency(data.stats.averageLosingTradeReturn)}
                                    </TableCell>
                                </TableRow>
                            </TableBody>
                        </Table>
                    </TableContainer>
                </Grid>

                <Grid item xs={12}>
                    <TableContainer component={Paper}>
                        <Table>
                            <TableHead>
                                <TableRow>
                                    <TableCell colSpan={2}>
                                        <Typography variant="h6">
                                            Consecutive Trade Statistics
                                        </Typography>
                                    </TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                <TableRow>
                                    <TableCell>Max Consecutive Wins</TableCell>
                                    <TableCell>{data.stats.maxConsecutiveWins}</TableCell>
                                </TableRow>
                                <TableRow>
                                    <TableCell>Max Consecutive Losses</TableCell>
                                    <TableCell>{data.stats.maxConsecutiveLosses}</TableCell>
                                </TableRow>
                                <TableRow>
                                    <TableCell>Max Consecutive Profit</TableCell>
                                    <TableCell>
                                        {formatCurrency(data.stats.maxConsecutiveProfit)}
                                    </TableCell>
                                </TableRow>
                                <TableRow>
                                    <TableCell>Max Consecutive Loss</TableCell>
                                    <TableCell>
                                        {formatCurrency(data.stats.maxConsecutiveLoss)}
                                    </TableCell>
                                </TableRow>
                                <TableRow>
                                    <TableCell>Average Consecutive Wins</TableCell>
                                    <TableCell>
                                        {data.stats.averageConsecutiveWins.toFixed(2)}
                                    </TableCell>
                                </TableRow>
                                <TableRow>
                                    <TableCell>Average Consecutive Losses</TableCell>
                                    <TableCell>
                                        {data.stats.averageConsecutiveLosses.toFixed(2)}
                                    </TableCell>
                                </TableRow>
                            </TableBody>
                        </Table>
                    </TableContainer>
                </Grid>
            </Grid>
        </div>
    );
};

export default AnalysisReport;
