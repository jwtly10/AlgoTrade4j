import React, { useCallback, useEffect, useRef, useState } from 'react';
import { ColorType, createChart, CrosshairMode } from 'lightweight-charts';
import { client } from '../api/client';
import 'chartjs-adapter-date-fns';
import AnalysisReport from './AnalysisReport.jsx';
import { EquityChart } from './EquityChart.jsx';
import TradesTable from './TradesTable.jsx';
import {
    Box,
    Button,
    Divider,
    Grid,
    Paper,
    Tab,
    TableContainer,
    Tabs,
    Typography,
} from '@mui/material';
import { TabPanel } from './TabPanel.jsx';
import LogsTable from './LogsTable.jsx';
import ParamModal from './ParamModal.jsx';

const StrategyChart = () => {
    const socketRef = useRef(null);

    const [isRunning, setIsRunning] = useState(false);
    const [strategyId, setStrategyId] = useState(null);
    const [account, setAccount] = useState({
        initialBalance: 0,
        balance: 0,
        equity: 0,
    });

    // Charting state
    const [trades, setTrades] = useState([]);
    const [indicators, setIndicators] = useState({});
    const [chartData, setChartData] = useState([]);
    const chartContainerRef = useRef();
    const [tradeIdMap, setTradeIdMap] = useState(new Map());
    const tradeCounterRef = useRef(1);

    // Analysis state
    const [analysisData, setAnalysisData] = useState(null);
    const [equityHistory, setEquityHistory] = useState([]);

    // Log state
    const [logs, setLogs] = useState([]);

    // UI State
    const [tabValue, setTabValue] = useState(0);

    // Params state
    const [params, setParams] = useState({});
    const [isModalOpen, setIsModalOpen] = useState(false);

    const handleTabChange = (event, newValue) => {
        setTabValue(newValue);
    };

    useEffect(() => {
        return () => {
            if (socketRef.current) {
                socketRef.current.close();
            }
        };
    }, []);

    useEffect(() => {
        const handleResize = () => {
            chart.applyOptions({
                width: chartContainerRef.current.clientWidth,
                height: chartContainerRef.current.clientHeight,
            });
        };

        const chart = createChart(chartContainerRef.current, {
            width: chartContainerRef.current.clientWidth,
            height: 500,
            layout: {
                background: { type: ColorType.Solid, color: '#ffffff' },
                textColor: 'black',
            },
            watermark: {
                color: 'rgba(0, 0, 0, 0.1)',
                visible: true,
                text: chartData.length > 0 ? chartData[0].symbol : '',
                fontSize: 80,
                horzAlign: 'center',
                vertAlign: 'center',
            },
        });

        chart.applyOptions({
            handleScroll: {
                mouseWheel: true,
                pressedMouseMove: true,
            },
            handleScale: {
                mouseWheel: true,
                pinch: true,
            },
            crosshair: {
                mode: CrosshairMode.Normal,
            },
            tooltip: {
                fontFamily: 'Arial',
                fontSize: 10,
                backgroundColor: 'rgba(255, 255, 255, 0.9)',
                borderColor: '#2962FF',
            },
            legend: {
                visible: true,
                fontSize: 12,
                fontFamily: 'Arial',
                color: '#333',
            },
            rightPriceScale: {
                borderColor: 'rgba(197, 203, 206, 0.8)',
                borderVisible: true,
                scaleMargins: {
                    top: 0.1,
                    bottom: 0.1,
                },
            },
        });

        window.addEventListener('resize', handleResize);
        handleResize();

        const candlestickSeries = chart.addCandlestickSeries({
            upColor: '#26a69a',
            downColor: '#ef5350',
            borderVisible: false,
            wickUpColor: '#26a69a',
            wickDownColor: '#ef5350',
        });

        try {
            candlestickSeries.setData(chartData);
        } catch (e) {
            console.error('Failed to set data:', e);
            console.log(chartData);
        }

        // Add indicator series
        const indicatorSeries = {};
        Object.keys(indicators).forEach((indicatorName) => {
            const indicatorData = indicators[indicatorName];
            if (indicatorData && indicatorData.length > 0) {
                // Filter out zero values and invalid entries
                const validData = indicatorData
                    .filter((item) => !isNaN(item.time) && !isNaN(item.value) && item.value !== 0)
                    .sort((a, b) => a.time - b.time);

                if (validData.length > 0) {
                    indicatorSeries[indicatorName] = chart.addLineSeries({
                        color: getIndicatorColor(indicatorName),
                        lineWidth: 2,
                    });
                    indicatorSeries[indicatorName].setData(validData);
                }
            }
        });

        addTradePriceLines(chart, candlestickSeries, trades);

        const openMarkers = trades.map((trade) => ({
            time: trade.openTime,
            position: trade.position === 'long' ? 'belowBar' : 'aboveBar',
            color: trade.position === 'long' ? '#26a69a' : '#ef5350',
            shape: 'arrowUp',
            text: `#${trade.tradeId} OPEN ${trade.position.toUpperCase()} @ ${trade.entry}`,
        }));

        const closeMarkers = trades
            .filter((trade) => trade.closePrice && trade.closeTime)
            .map((trade) => ({
                time: trade.closeTime,
                position: trade.position === 'long' ? 'aboveBar' : 'belowBar',
                color: trade.position === 'long' ? '#ef5350' : '#26a69a',
                shape: 'arrowDown',
                text: `#${trade.tradeId} CLOSE ${trade.position.toUpperCase()} @ ${trade.closePrice}`,
            }));

        const allMarkers = [...openMarkers, ...closeMarkers].sort((a, b) => a.time - b.time);

        try {
            candlestickSeries.setMarkers(allMarkers);
        } catch (e) {
            console.error('Failed to set trade markers:', e);
            console.log(allMarkers);
        }

        return () => {
            window.removeEventListener('resize', handleResize);
            chart.remove();
        };
    }, [chartData, trades]);

    const getIndicatorColor = (indicatorName) => {
        const colors = ['#2196F3', '#FF9800', '#4CAF50', '#E91E63', '#9C27B0'];
        return colors[indicatorName.length % colors.length];
    };

    function addTradePriceLines(chart, candlestickSeries, trades) {
        trades.forEach((trade) => {
            const color = trade.position === 'long' ? '#26a69a' : '#ef5350';
            const priceLine = {
                price: trade.entry,
                color: color,
                lineWidth: 2,
                lineStyle: 2, // Dashed line
                axisLabelVisible: true,
                title: `#${trade.tradeId}`,
            };
            candlestickSeries.createPriceLine(priceLine);
        });
    }

    const startStrategy = async () => {
        setIsRunning(true);
        // Clean previous data
        setChartData([]);
        setAnalysisData(null);
        setEquityHistory([]);
        setTrades([]);
        setTradeIdMap(new Map());
        tradeCounterRef.current = 1;
        setIndicators({});
        console.log('Starting strategy...');
        try {
            const config = {
                strategyId: 'SimpleSMAStrategy',
                subscriptions: ['BAR', 'TRADE', 'INDICATOR'],
                initialCash: '10000',
                barSeriesSize: 10000,
            };
            setStrategyId('SimpleSMAStrategy');

            socketRef.current = await client.connectWebSocket(
                'SimpleSMAStrategy',
                handleWebSocketMessage
            );
            console.log('WebSocket connected');
            await client.startStrategy(config);
        } catch (error) {
            console.error('Failed to start strategy:', error);
            setIsRunning(false);
        }
    };

    const stopStrategy = async () => {
        try {
            if (strategyId) {
                // await client.stopStrategy(strategyId);
                // We can stop strategy by just closing ws connection
                setIsRunning(false);
                if (socketRef.current) {
                    socketRef.current.close();
                }
            }
        } catch (error) {
            console.error('Failed to stop strategy:', error);
        }
    };

    const handleWebSocketMessage = (data) => {
        if (
            data.type === 'BAR' ||
            (data.type === 'TRADE' && (data.action === 'OPEN' || data.action === 'CLOSE'))
        ) {
            updateTradingViewChart(data);
        } else if (data.type === 'INDICATOR') {
            updateIndicator(data);
        } else if (data.type === 'ACCOUNT') {
            updateAccount(data);
        } else if (data.type === 'STRATEGY_STOP') {
            setIsRunning(false);
        } else if (data.type === 'ANALYSIS') {
            setAnalysis(data);
        } else if (data.type === 'TRADE' && data.action === 'UPDATE') {
            updateTrades(data);
        } else if (data.type === 'LOG') {
            updateLogs(data);
        } else {
            console.log('WHAT OTHER EVENT WAS SENT?' + data);
        }
    };

    const updateLogs = (data) => {
        setLogs((prevLogs) => {
            return [
                ...prevLogs,
                {
                    timestamp: new Date(data.time * 1000).toLocaleString('en-US', {
                        year: 'numeric',
                        month: '2-digit',
                        day: '2-digit',
                        hour: '2-digit',
                        minute: '2-digit',
                        second: '2-digit',
                        fractionalSecondDigits: 3,
                        hour12: false,
                    }),
                    type: data.level,
                    message: data.message,
                },
            ];
        });
    };

    const setAnalysis = (data) => {
        setAnalysisData(data);
        setEquityHistory(data.equityHistory);
    };

    const updateTrades = (data) => {
        // Update the profit value of the trade
        const trade = data.trade;
        setTrades((prevTrades) => {
            const updatedTrades = prevTrades.map((prevTrade) => {
                if (prevTrade.tradeId === trade.id) {
                    return {
                        ...prevTrade,
                        profit: trade.profit.value,
                        closePrice: trade.closePrice.value,
                        closeTime: trade.closeTime,
                    };
                }
                return prevTrade;
            });
            return updatedTrades;
        });
    };

    const updateIndicator = (data) => {
        if (data.value.value !== 0) {
            // Only add non-zero values
            setIndicators((prevIndicators) => ({
                ...prevIndicators,
                [data.indicatorName]: [
                    ...(prevIndicators[data.indicatorName] || []),
                    { time: data.dateTime, value: data.value.value },
                ],
            }));
        }
    };

    const updateAccount = (data) => {
        setAccount({
            initialBalance: data.account.initialBalance.value,
            balance: data.account.balance.value,
            equity: data.account.equity.value,
        });
    };

    const updateTradingViewChart = useCallback(
        (data) => {
            if (data.type === 'BAR') {
                const bar = data.bar;
                setChartData((prevData) => {
                    const lastBar = prevData[prevData.length - 1];
                    if (lastBar && lastBar.time === bar.openTime) {
                        // Update the existing bar
                        const updatedBar = {
                            ...lastBar,
                            high: Math.max(lastBar.high, bar.high.value),
                            low: Math.min(lastBar.low, bar.low.value),
                            close: bar.close.value,
                        };
                        return [...prevData.slice(0, -1), updatedBar];
                    } else {
                        // Add a new bar
                        return [
                            ...prevData,
                            {
                                time: bar.openTime,
                                open: bar.open.value,
                                high: bar.high.value,
                                low: bar.low.value,
                                close: bar.close.value,
                                symbol: bar.symbol,
                            },
                        ];
                    }
                });
            } else if (data.type === 'TRADE') {
                const trade = data.trade;
                setTrades((prevTrades) => {
                    const existingTradeIndex = prevTrades.findIndex((t) => t.tradeId === trade.id);

                    if (existingTradeIndex !== -1) {
                        // If the trade already exists, update it with new data
                        const updatedTrades = [...prevTrades];
                        updatedTrades[existingTradeIndex] = {
                            ...updatedTrades[existingTradeIndex],
                            openTime: trade.openTime,
                            closeTime: trade.closeTime,
                            symbol: trade.symbol,
                            entry: trade.entryPrice.value,
                            stopLoss: trade.stopLoss.value,
                            closePrice: trade.closePrice.value,
                            takeProfit: trade.takeProfit.value,
                            quantity: trade.quantity.value,
                            isLong: trade.long,
                            position: trade.long ? 'long' : 'short',
                            price:
                                data.action === 'CLOSE'
                                    ? trade.closePrice.value
                                    : trade.entryPrice.value,
                            profit: trade.profit.value,
                            action: data.action,
                        };
                        return updatedTrades;
                    } else {
                        // If it's a new trade, add it to the array
                        return [
                            ...prevTrades,
                            {
                                id: tradeIdMap.get(trade.id) || tradeCounterRef.current - 1,
                                tradeId: trade.id,
                                openTime: trade.openTime,
                                closeTime: trade.closeTime,
                                symbol: trade.symbol,
                                entry: trade.entryPrice.value,
                                stopLoss: trade.stopLoss.value,
                                closePrice: trade.closePrice.value,
                                takeProfit: trade.takeProfit.value,
                                quantity: trade.quantity.value,
                                isLong: trade.long,
                                position: trade.long ? 'long' : 'short',
                                price:
                                    data.action === 'CLOSE'
                                        ? trade.closePrice.value
                                        : trade.entryPrice.value,
                                profit: trade.profit.value,
                                action: data.action,
                            },
                        ];
                    }
                });

                // Update the tradeIdMap if it's a new trade
                setTradeIdMap((prevMap) => {
                    if (!prevMap.has(trade.id)) {
                        const newMap = new Map(prevMap);
                        newMap.set(trade.id, tradeCounterRef.current);
                        tradeCounterRef.current += 1;
                        return newMap;
                    }
                    return prevMap;
                });
            }
        },
        [tradeIdMap]
    );

    const getParams = async () => {
        try {
            const params = await client.getParams('test');
            console.log('Strategy Params:', params);
            setParams(params);
            setIsModalOpen(true);
        } catch (error) {
            console.error('Failed to get strategy params:', error);
        }
    };

    const handleParamSave = (params) => {
        console.log('Saving params:', params);
        setIsModalOpen(false);
    };

    return (
        <Paper elevation={3} className="chart-container" sx={{ p: 3, mb: 3 }}>
            <Grid container spacing={3}>
                <Grid item xs={12}>
                    <Typography variant="h4" component="h1" gutterBottom>
                        AlgoTrade4J
                    </Typography>
                </Grid>
                <Grid item xs={12} sm={6}>
                    <Button
                        variant="contained"
                        color={isRunning ? 'error' : 'success'}
                        onClick={isRunning ? stopStrategy : startStrategy}
                        fullWidth
                    >
                        {isRunning ? 'Stop Strategy' : 'Start Strategy'}
                    </Button>
                    <Button variant="contained" onClick={getParams} fullWidth>
                        Get Strategy Params
                    </Button>
                </Grid>
                <Grid item xs={12} sm={6}>
                    <Typography variant="subtitle1">
                        Strategy ID:{' '}
                        <Box component="span" fontWeight="bold">
                            {strategyId}
                        </Box>
                    </Typography>
                </Grid>
                <Grid item xs={12}>
                    <Divider sx={{ my: 2 }} />
                </Grid>
                <Grid item xs={12} sm={6} md={3}>
                    <Typography variant="body2">
                        Initial Balance:{' '}
                        <Box component="span" fontWeight="bold">
                            ${account.initialBalance}
                        </Box>
                    </Typography>
                </Grid>
                <Grid item xs={12} sm={6} md={3}>
                    <Typography variant="body2">
                        Current Balance:{' '}
                        <Box component="span" fontWeight="bold">
                            ${account.balance}
                        </Box>
                    </Typography>
                </Grid>
                <Grid item xs={12} sm={6} md={3}>
                    <Typography variant="body2">
                        Equity:{' '}
                        <Box component="span" fontWeight="bold">
                            ${account.equity}
                        </Box>
                    </Typography>
                </Grid>
                <Grid item xs={12} sm={6} md={3}>
                    <Typography variant="body2">
                        Open Position Value:{' '}
                        <Box component="span" fontWeight="bold">
                            $
                            {Math.round((account.equity - account.balance + Number.EPSILON) * 100) /
                                100}
                        </Box>
                    </Typography>
                </Grid>
            </Grid>

            <Box sx={{ mt: 3, p: 2, bgcolor: 'background.paper', borderRadius: 1 }}>
                <Box
                    sx={{ width: '100%', height: '400px', overflow: 'hidden' }}
                    ref={chartContainerRef}
                />
            </Box>

            <Box sx={{ borderBottom: 1, borderColor: 'divider', mt: 3 }}>
                <Tabs value={tabValue} onChange={handleTabChange} aria-label="strategy tabs">
                    <Tab label="Trades" />
                    <Tab label="Analysis" />
                    <Tab label="Equity History" />
                    <Tab label="Logs" />
                </Tabs>
            </Box>

            <TabPanel value={tabValue} index={0}>
                <TradesTable trades={trades} />
            </TabPanel>
            <TabPanel value={tabValue} index={1}>
                {analysisData !== null ? (
                    <AnalysisReport data={analysisData} />
                ) : (
                    <TableContainer component={Paper}>
                        <Typography variant="body1" sx={{ p: 2, textAlign: 'center' }}>
                            No analysis data available yet.
                        </Typography>
                    </TableContainer>
                )}
            </TabPanel>
            <TabPanel value={tabValue} index={2}>
                {equityHistory.length > 0 ? (
                    <EquityChart equityHistory={equityHistory} />
                ) : (
                    <TableContainer component={Paper}>
                        <Typography variant="body1" sx={{ p: 2, textAlign: 'center' }}>
                            No equity history available yet.
                        </Typography>
                    </TableContainer>
                )}
            </TabPanel>
            <TabPanel value={tabValue} index={3}>
                {logs.length > 0 ? (
                    <LogsTable logs={logs} />
                ) : (
                    <TableContainer component={Paper}>
                        <Typography variant="body1" sx={{ p: 2, textAlign: 'center' }}>
                            No logs available yet.
                        </Typography>
                    </TableContainer>
                )}
            </TabPanel>
            <ParamModal
                open={isModalOpen}
                onClose={() => setIsModalOpen(false)}
                params={params}
                onSave={handleParamSave}
            ></ParamModal>
        </Paper>
    );
};

export default StrategyChart;
