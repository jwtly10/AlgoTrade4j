import React, {useEffect, useMemo, useState} from 'react';
import {apiClient} from '@/api/apiClient.js';
import {Card, CardContent} from "../ui/card";
import {Tabs, TabsContent, TabsList, TabsTrigger} from "../ui/tabs";
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from "../ui/table";
import {Button} from "../ui/button";
import {ArrowUpDown} from "lucide-react";
import PrettyJsonViewer from './PrettyJsonViewer';

const OptimizationResults = ({task}) => {
    const [results, setResults] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [tabValue, setTabValue] = useState("successful");
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
        },
        {id: 'profitFactor', label: 'Profit Factor', minWidth: 110, format: (value) => value.toFixed(2)},
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

    const handleChangePage = (newPage) => {
        setPage(newPage);
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
        return date.toISOString().split('T')[0];
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

        return [...(tabValue === "successful" ? successfulStrategies : failedStrategies)].sort(comparator);
    }, [successfulStrategies, failedStrategies, order, orderBy, tabValue]);

    const renderTaskInfo = () => (
        <Card className="mb-2">
            <CardContent className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-6 p-3">
                <div className="text-center">
                    <h3 className="text-sm font-semibold mb-2">Symbol</h3>
                    <p className="text-md">{task.config.instrument}</p>
                </div>
                <div className="text-center">
                    <h3 className="text-sm font-semibold mb-2">Period</h3>
                    <p className="text-md">{formatDuration(task.config.period)}</p>
                </div>
                <div className="text-center">
                    <h3 className="text-sm font-semibold mb-2">Timeframe</h3>
                    <p className="text-md">{formatDate(task.config.timeframe.from)} to {formatDate(task.config.timeframe.to)}</p>
                </div>
                <div className="text-center">
                    <h3 className="text-sm font-semibold mb-2">Strategy</h3>
                    <p className="text-md">{task.config.strategyClass}</p>
                </div>
            </CardContent>
        </Card>
    );

    function convertPeriodToSelectText(period) {
        const secondsToValue = {
            60: "M1",
            300: "M5",
            900: "M15",
            1800: "M30",
            3600: "H1",
            14400: "H4",
            86400: "D"
        };

        return secondsToValue[period] || "Unknown";
    }

    const renderTable = (strategies, tableColumns) => (
        <div className="rounded-lg border shadow-sm overflow-hidden">
            <Table>
                <TableHeader>
                    <TableRow>
                        {tableColumns.map((column) => (
                            <TableHead key={column.id} className="py-3 px-4">
                                <Button
                                    variant="ghost"
                                    onClick={() => handleRequestSort(column.id)}
                                    className="font-bold text-sm"
                                >
                                    {column.label}
                                    <ArrowUpDown className="ml-2 h-4 w-4"/>
                                </Button>
                            </TableHead>
                        ))}
                    </TableRow>
                </TableHeader>
                <TableBody>
                    {strategies
                        .slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)
                        .map((row, index) => (
                            <TableRow key={row.output.strategyId}>
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
                                        <TableCell key={column.id} className="py-3 px-4">
                                            {column.format && typeof value === 'number'
                                                ? column.format(value, row)
                                                : column.id === 'parameters'
                                                    ? (() => {
                                                        const originalJsonString = Object.values(value).join('');
                                                        const parsedValue = JSON.parse(originalJsonString);
                                                        const combinedData = {
                                                            ...parsedValue,
                                                            timeframe: task.config.timeframe,
                                                            instrument: task.config.instrument,
                                                            strategyClass: task.config.strategyClass,
                                                            period: convertPeriodToSelectText(task.config.period),
                                                            speed: "INSTANT",
                                                            spread: task.config.spread
                                                        };
                                                        return <PrettyJsonViewer jsonData={combinedData}/>;
                                                    })()
                                                    : value}
                                        </TableCell>
                                    );
                                })}
                            </TableRow>
                        ))}
                </TableBody>
            </Table>
            <div className="flex items-center justify-between px-4 py-3 border-t">
                <div className="text-sm text-gray-700">
                    Showing {page * rowsPerPage + 1} to {Math.min((page + 1) * rowsPerPage, strategies.length)} of {strategies.length} results
                </div>
                <div className="flex items-center space-x-2">
                    <Button
                        variant="outline"
                        size="sm"
                        onClick={() => handleChangePage(page - 1)}
                        disabled={page === 0}
                    >
                        Previous
                    </Button>
                    <Button
                        variant="outline"
                        size="sm"
                        onClick={() => handleChangePage(page + 1)}
                        disabled={page >= Math.ceil(strategies.length / rowsPerPage) - 1}
                    >
                        Next
                    </Button>
                </div>
            </div>
        </div>
    );

    if (loading) {
        return (
            <div className="flex items-center justify-center h-full">
                <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-gray-900"></div>
            </div>
        );
    }

    if (error) {
        return <p className="text-red-500">{error}</p>;
    }

    return (
        <div className="flex flex-col h-full space-y-6">
            {renderTaskInfo()}
            <Tabs value={tabValue} onValueChange={setTabValue} className="flex-grow flex flex-col">
                <TabsList className="mb-4">
                    <TabsTrigger value="successful" className="px-6 py-3 text-sm font-medium">
                        Successful Strategies ({successfulStrategies.length})
                    </TabsTrigger>
                    <TabsTrigger value="failed" className="px-6 py-3 text-sm font-medium">
                        Failed Strategies ({failedStrategies.length})
                    </TabsTrigger>
                </TabsList>
                <TabsContent value="successful" className="flex-grow">
                    {renderTable(sortedData, columns)}
                </TabsContent>
                <TabsContent value="failed" className="flex-grow">
                    {renderTable(sortedData, failedColumns)}
                </TabsContent>
            </Tabs>
        </div>
    );
};

export default OptimizationResults;