// hooks/useBacktest.js

import {useCallback, useEffect, useRef, useState} from 'react';
import {liveWSClient} from '@/api/liveClient.js';
import {apiClient} from '@/api/apiClient.js';
import {useToast} from '@/hooks/use-toast';
import log from '../logger.js';

export const useLive = () => {
    const socketRef = useRef(null);
    const {toast} = useToast();

    // Charting state
    const [trades, setTrades] = useState([]);
    const [indicators, setIndicators] = useState({});
    const [chartData, setChartData] = useState([]);
    const [tradeIdMap, setTradeIdMap] = useState(new Map());
    const tradeCounterRef = useRef(1);

    const [analysisData, setAnalysisData] = useState({});

    // Log state
    const [logs, setLogs] = useState([]);

    // UI State
    const [tabValue, setTabValue] = useState('trades');
    const [strategies, setStrategies] = useState([]);

    const [isConnected, setIsConnected] = useState(false);

    useEffect(() => {
        const fetchStrategies = async () => {
            try {
                const res = await apiClient.getStrategies();
                setStrategies(res);
            } catch (error) {
                log.error('Failed to get strategies:', error);
            }
        };

        fetchStrategies();

        return () => {
            if (socketRef.current) {
                socketRef.current.close();
            }
        };
    }, []);

    const resetChart = () => {
        setChartData([]);
        setTrades([]);
        setTradeIdMap(new Map());
        tradeCounterRef.current = 1;
        setIndicators({});
        setLogs([]);
        setAnalysisData({})
    }

    const viewStrategy = async (strategyId) => {
        // Clean previous data
        setChartData([]);
        setTrades([]);
        setTradeIdMap(new Map());
        tradeCounterRef.current = 1;
        setIndicators({});
        setLogs([]);
        setAnalysisData({})
        if (socketRef.current) {
            socketRef.current.close();
        }

        try {
            log.debug('Viewing strategy:', strategyId);
            socketRef.current = await liveWSClient.connectWebSocket(strategyId, handleWebSocketMessage);
            setIsConnected(true);
        } catch (error) {
            log.error('Failed to view strategy:', error);
            toast({
                title: 'Strategy View Failed',
                description: `Failed to view strategy: ${error.message}`,
                variant: 'destructive',
            });
            setIsConnected(false);
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
        } else if (data.type === 'ACCOUNT' || data.type === 'ASYNC_ACCOUNT') {
        } else if (data.type === 'STRATEGY_STOP') {
            log.info('Strategy stop event');
        } else if (data.type === 'LIVE_ANALYSIS') {
            setAnalysis(data);
        } else if (data.type === 'TRADE' && data.action === 'UPDATE') {
            updateTrades(data);
        } else if (data.type === 'LOG') {
            updateLogs(data);
        } else if (data.type === 'BAR_SERIES') {
            updateTradingViewChart(data);
        } else if (data.type === 'ALL_TRADES') {
            log.info('All trades event');
            updateTradingViewChart(data);
        } else if (data.type === 'ALL_INDICATORS') {
            log.info('All indicator event');
            setAllIndicators(data);
        } else if (data.type === 'PROGRESS') {
        } else if (data.type === 'ERROR') {
            toast({
                title: 'Error',
                description: data.message,
                variant: 'destructive',
            });
        } else {
            log.debug('WHAT OTHER EVENT WAS SENT?' + data);
        }
    };

    const setAnalysis = (data) => {
        setAnalysisData(data);
    };

    const updateLogs = (data) => {
        setLogs((prevLogs) => {
            return [
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
                    type: data.logType,
                    message: data.message,
                },
                ...prevLogs,
            ];
        });
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
        log.debug(data);
        if (data.value !== 0) {
            // Only add non-zero values
            setIndicators((prevIndicators) => ({
                ...prevIndicators,
                [data.indicatorName]: [
                    ...(prevIndicators[data.indicatorName] || []),
                    {time: data.value.dateTime, value: data.value},
                ],
            }));
        }
    };

    const setAllIndicators = (data) => {
        setIndicators(() => {
            const newIndicators = {};
            Object.entries(data.indicators).forEach(([indicatorName, values]) => {
                newIndicators[indicatorName] = values
                    .filter((indicator) => indicator.value !== 0)
                    .map((indicator) => ({
                        time: indicator.dateTime,
                        value: indicator.value,
                    }));
            });
            return newIndicators;
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
                                instrument: bar.instrument,
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
                            instrument: trade.instrument,
                            entry: trade.entryPrice.value,
                            stopLoss: trade.stopLoss.value,
                            closePrice: trade.closePrice.value,
                            takeProfit: trade.takeProfit.value,
                            quantity: trade.quantity,
                            isLong: trade.long,
                            position: trade.long ? 'long' : 'short',
                            price:
                                data.action === 'CLOSE'
                                    ? trade.closePrice.value
                                    : trade.entryPrice.value,
                            profit: trade.profit,
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
                                instrument: trade.instrument,
                                entry: trade.entryPrice.value,
                                stopLoss: trade.stopLoss.value,
                                closePrice: trade.closePrice.value,
                                takeProfit: trade.takeProfit.value,
                                quantity: trade.quantity,
                                isLong: trade.long,
                                position: trade.long ? 'long' : 'short',
                                price:
                                    data.action === 'CLOSE'
                                        ? trade.closePrice.value
                                        : trade.entryPrice.value,
                                profit: trade.profit,
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
            } else if (data.type === 'BAR_SERIES') {
                const barSeries = data.barSeries.bars;
                setChartData(() => {
                    // Convert the bar series to the format expected by the chart
                    const newChartData = barSeries.map((bar) => ({
                        time: bar.openTime,
                        open: bar.open.value,
                        high: bar.high.value,
                        low: bar.low.value,
                        close: bar.close.value,
                        instrument: bar.instrument,
                    }));

                    // Sort the data by time to ensure correct order
                    newChartData.sort((a, b) => new Date(a.time) - new Date(b.time));
                    return newChartData;
                });
            } else if (data.type === 'ALL_TRADES') {
                const tradesObj = data.trades;

                setTrades(() => {
                    const newTrades = Object.entries(tradesObj).map(([id, trade]) => ({
                        id: parseInt(id), // Assuming the object key is a string, we parse it to an integer
                        tradeId: trade.id,
                        openTime: trade.openTime,
                        closeTime: trade.closeTime,
                        instrument: trade.instrument,
                        entry: trade.entryPrice.value,
                        stopLoss: trade.stopLoss.value,
                        closePrice: trade.closePrice ? trade.closePrice.value : null,
                        takeProfit: trade.takeProfit.value,
                        quantity: trade.quantity,
                        isLong: trade.long,
                        position: trade.long ? 'long' : 'short',
                        price: trade.closePrice ? trade.closePrice.value : trade.entryPrice.value,
                        profit: trade.profit ? trade.profit.toFixed(2) : null,
                        action: trade.closeTime ? 'CLOSE' : 'OPEN',
                    }));

                    // Sort trades by openTime
                    newTrades.sort((a, b) => new Date(a.openTime) - new Date(b.openTime));
                    return newTrades;
                });

                // Reset the tradeIdMap and tradeCounter
                setTradeIdMap(new Map());
                tradeCounterRef.current = 0;
            }
        },
        [tradeIdMap]
    );
    return {
        resetChart,
        isConnected,
        analysisData,
        trades,
        indicators,
        chartData,
        logs,
        tabValue,
        setTabValue,
        strategies,
        viewStrategy,
    };
};