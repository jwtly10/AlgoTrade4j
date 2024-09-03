import React from 'react';
import {Table, TableBody, TableCell, TableRow,} from "@/components/ui/table";
import {Card, CardContent, CardHeader, CardTitle} from "@/components/ui/card";

const AnalysisReport = ({ data }) => {
    const formatCurrency = (value) => {
        return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(value);
    };

    const formatPercentage = (value) => {
        return `${value}%`;
    };

    return (
        <div className="space-y-6">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <Card>
                    <CardHeader>
                        <CardTitle>General Statistics</CardTitle>
                    </CardHeader>
                    <CardContent>
                        <Table>
                            <TableBody>
                                <TableRow>
                                    <TableCell className="font-medium">Initial Deposit</TableCell>
                                    <TableCell>{formatCurrency(data.stats.initialDeposit)}</TableCell>
                                </TableRow>
                                <TableRow>
                                    <TableCell className="font-medium">Total Net Profit</TableCell>
                                    <TableCell>{formatCurrency(data.stats.totalNetProfit)}</TableCell>
                                </TableRow>
                                <TableRow>
                                    <TableCell className="font-medium">Gross Profit</TableCell>
                                    <TableCell>{formatCurrency(data.stats.grossProfit)}</TableCell>
                                </TableRow>
                                <TableRow>
                                    <TableCell className="font-medium">Gross Loss</TableCell>
                                    <TableCell>{formatCurrency(data.stats.grossLoss)}</TableCell>
                                </TableRow>
                                <TableRow>
                                    <TableCell className="font-medium">Profit Factor</TableCell>
                                    <TableCell>{data.stats.profitFactor.toFixed(2)}</TableCell>
                                </TableRow>
                                <TableRow>
                                    <TableCell className="font-medium">Expected Payoff</TableCell>
                                    <TableCell>{formatCurrency(data.stats.expectedPayoff)}</TableCell>
                                </TableRow>
                                <TableRow>
                                    <TableCell className="font-medium">Max Drawdown</TableCell>
                                    <TableCell>{formatPercentage(data.stats.maxDrawdown.toFixed(2))}</TableCell>
                                </TableRow>
                                <TableRow>
                                    <TableCell className="font-medium">Sharpe Ratio</TableCell>
                                    <TableCell>{data.stats.sharpeRatio.toFixed(2)}</TableCell>
                                </TableRow>
                                <TableRow>
                                    <TableCell className="font-medium">Ticks Modelled</TableCell>
                                    <TableCell>{data.stats.ticksModelled}</TableCell>
                                </TableRow>
                            </TableBody>
                        </Table>
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader>
                        <CardTitle>Trade Statistics</CardTitle>
                    </CardHeader>
                    <CardContent>
                        <Table>
                            <TableBody>
                                <TableRow>
                                    <TableCell className="font-medium">Total Trades</TableCell>
                                    <TableCell>{data.stats.totalTrades}</TableCell>
                                </TableRow>
                                <TableRow>
                                    <TableCell className="font-medium">Long Trades (Win %)</TableCell>
                                    <TableCell>
                                        {data.stats.totalLongTrades} ({formatPercentage(data.stats.longWinPercentage.toFixed(2))})
                                    </TableCell>
                                </TableRow>
                                <TableRow>
                                    <TableCell className="font-medium">Short Trades (Win %)</TableCell>
                                    <TableCell>
                                        {data.stats.totalShortTrades} ({formatPercentage(data.stats.shortWinPercentage.toFixed(2))})
                                    </TableCell>
                                </TableRow>
                                <TableRow>
                                    <TableCell className="font-medium">Largest Profitable Trade</TableCell>
                                    <TableCell>{formatCurrency(data.stats.largestProfitableTrade)}</TableCell>
                                </TableRow>
                                <TableRow>
                                    <TableCell className="font-medium">Largest Losing Trade</TableCell>
                                    <TableCell>{formatCurrency(data.stats.largestLosingTrade)}</TableCell>
                                </TableRow>
                                <TableRow>
                                    <TableCell className="font-medium">Average Profitable Trade</TableCell>
                                    <TableCell>{formatCurrency(data.stats.averageProfitableTradeReturn)}</TableCell>
                                </TableRow>
                                <TableRow>
                                    <TableCell className="font-medium">Average Losing Trade</TableCell>
                                    <TableCell>{formatCurrency(data.stats.averageLosingTradeReturn)}</TableCell>
                                </TableRow>
                            </TableBody>
                        </Table>
                    </CardContent>
                </Card>
            </div>

            <Card>
                <CardHeader>
                    <CardTitle>Consecutive Trade Statistics</CardTitle>
                </CardHeader>
                <CardContent>
                    <Table>
                        <TableBody>
                            <TableRow>
                                <TableCell className="font-medium">Max Consecutive Wins</TableCell>
                                <TableCell>{data.stats.maxConsecutiveWins}</TableCell>
                            </TableRow>
                            <TableRow>
                                <TableCell className="font-medium">Max Consecutive Losses</TableCell>
                                <TableCell>{data.stats.maxConsecutiveLosses}</TableCell>
                            </TableRow>
                            <TableRow>
                                <TableCell className="font-medium">Max Consecutive Profit</TableCell>
                                <TableCell>{formatCurrency(data.stats.maxConsecutiveProfit)}</TableCell>
                            </TableRow>
                            <TableRow>
                                <TableCell className="font-medium">Max Consecutive Loss</TableCell>
                                <TableCell>{formatCurrency(data.stats.maxConsecutiveLoss)}</TableCell>
                            </TableRow>
                            <TableRow>
                                <TableCell className="font-medium">Average Consecutive Wins</TableCell>
                                <TableCell>{data.stats.averageConsecutiveWins.toFixed(2)}</TableCell>
                            </TableRow>
                            <TableRow>
                                <TableCell className="font-medium">Average Consecutive Losses</TableCell>
                                <TableCell>{data.stats.averageConsecutiveLosses.toFixed(2)}</TableCell>
                            </TableRow>
                        </TableBody>
                    </Table>
                </CardContent>
            </Card>
        </div>
    );
};

export default AnalysisReport;