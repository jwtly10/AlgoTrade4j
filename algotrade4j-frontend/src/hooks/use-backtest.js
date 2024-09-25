// hooks/useBacktest.js

import {useCallback, useEffect, useRef, useState} from 'react';
import {apiClient} from '../api/apiClient.js';
import {useToast} from '@/hooks/use-toast';
import log from '../logger.js';

export const useBacktest = () => {
    const socketRef = useRef(null);
    const {toast} = useToast();

    const [isStrategyRunning, setIsStrategyRunning] = useState(false);
    const [account, setAccount] = useState({
        initialBalance: 0,
        balance: 0,
        equity: 0,
    });

    // Charting state
    const [trades, setTrades] = useState([]);
    const [indicators, setIndicators] = useState({});
    const [chartData, setChartData] = useState([]);
    const [tradeIdMap, setTradeIdMap] = useState(new Map());
    const tradeCounterRef = useRef(1);

    // Analysis state
    const [analysisData, setAnalysisData] = useState(null);
    const [equityHistory, setEquityHistory] = useState([]);

    // Log state
    const [logs, setLogs] = useState([]);

    const [backtestErrorMsg, setBacktestErrorMsg] = useState('');

    // UI State
    const [tabValue, setTabValue] = useState('trades');
    const [isModalOpen, setIsModalOpen] = useState(false);

    const [strategies, setStrategies] = useState([]);
    const [strategyClass, setStrategyClass] = useState('');

    const [isAsync, setAsync] = useState(false);
    const [progressData, setProgressData] = useState(null);
    const [showChart, setShowChart] = useState(true);
    const [startTime, setStartTime] = useState(null);

    const [strategyConfig, setStrategyConfig] = useState({
        strategyClass: '',
        initialCash: '10000',
        instrumentData: {
            internalSymbol: 'NAS100USD',
            oandaSymbol: 'NAS100_USD',
            decimalPlaces: 1,
            minimumMove: 0.1,
            instrument: 'NAS100USD',
        },
        spread: '10',
        speed: 'INSTANT',
        period: 'M30',
        timeframe: {
            from: '',
            to: '',
        },
        runParams: [],
    });

    useEffect(() => {
        const initialize = async () => {
            // Fetch strategies
            try {
                const res = await apiClient.getStrategies();
                setStrategies(res);

                const lastStrat = localStorage.getItem('LAST_STRAT');

                for (const strat of res) {
                    if (strat === lastStrat) {
                        await handleChangeStrategy(lastStrat);
                        break;
                    }
                }
            } catch (error) {
                log.error('Failed to get strategies:', error);
                return; // Exit if fetching strategies fails
            }

            // Load saved chart data
            const chartData = localStorage.getItem('chartData');
            const analysisData = localStorage.getItem('analysisData');
            const tradeData = localStorage.getItem('tradeData');
            const accountData = localStorage.getItem('accountData');
            const indicatorData = localStorage.getItem('indicatorData');

            if (chartData && analysisData && tradeData && accountData && indicatorData) {
                try {
                    setChartData(JSON.parse(chartData));
                    setTrades(JSON.parse(tradeData));
                    setAccount(JSON.parse(accountData));
                    setIndicators(JSON.parse(indicatorData));
                    setAnalysisData(JSON.parse(analysisData));
                    setEquityHistory(JSON.parse(analysisData).equityHistory);
                } catch (error) {
                    log.error('Failed to parse saved chart data:', error);
                }
            }
        };

        initialize();

        return () => {
            if (socketRef.current) {
                socketRef.current.close();
            }
        };
    }, []);
    ;

    // Here we check local storage and update the config with the values from local storage
    // if there are any, else we use the defaults from the strategy
    const loadConfigFromLocalStorage = (runParams, stratClass) => {
        const storedConfig = JSON.parse(localStorage.getItem(`strategyConfig_${stratClass}`)) || {};

        // Check storedConfig for runParams, if we have them, we need to update the values!!!!! in the run params
        const updatedRunParams = runParams.map((param) => {
            const storedParam = storedConfig.runParams?.find((p) => p.name === param.name);

            // Only update if theres data to update
            if (storedParam) {
                return {
                    ...param,
                    value: storedParam.value !== undefined ? storedParam.value : param.value,
                    start: storedParam.start !== undefined ? storedParam.start : param.start,
                    stop: storedParam.stop !== undefined ? storedParam.stop : param.stop,
                    step: storedParam.step !== undefined ? storedParam.step : param.step,
                    selected:
                        storedParam.selected !== undefined ? storedParam.selected : param.selected,
                };
            } else {
                return param; // Keep the original parameter if no stored version is found
            }
        });

        // Setting some defaults in case we don't have any values in local storage
        const today = new Date().toISOString().split('T')[0] + 'T00:00:00Z';
        const lastMonth =
            new Date(Date.now() - 86400000 * 30).toISOString().split('T')[0] + 'T00:00:00Z';

        const updatedConfig = {
            ...strategyConfig,
            initialCash: storedConfig.initialCash || strategyConfig.initialCash,
            instrumentData: storedConfig.instrumentData || strategyConfig.instrumentData,
            spread: storedConfig.spread || strategyConfig.spread,
            period: storedConfig.period || strategyConfig.period,
            speed: storedConfig.speed || strategyConfig.speed,
            timeframe: {
                from: storedConfig.timeframe?.from || lastMonth,
                to: storedConfig.timeframe?.to || today,
            },
            runParams: updatedRunParams,
            strategyClass: stratClass,
        };

        // Now we can update the state with the updated values
        setStrategyConfig(updatedConfig);
    };

    const startOptimisation = async () => {
        if (strategyClass === '') {
            log.error('Strategy class is required');
            toast({
                title: 'Warning',
                description: 'Please select a strategy before starting optimisation.',
                variant: 'warning',
            });
            return;
        }

        const hackConfig = {
            ...strategyConfig,
            strategyClass: strategyClass,
        };

        try {
            await apiClient.queueOptimisation(hackConfig);
            log.debug('Optimisation queued');
            toast({
                title: 'Success',
                description: 'Optimisation queued successfully. The page will refresh shortly.',
            });
        } catch (error) {
            log.error('Failed to queue optimisation:', error);
            toast({
                title: 'Error',
                description: `Failed to queue optimisation: ${error.message}`,
                variant: 'destructive',
            });
        }
    };

    const cleanChartData = () => {
        // Clean previous data
        setAccount({
            initialBalance: Number(strategyConfig.initialCash ? strategyConfig.initialCash : 0),
            balance: 0,
            equity: 0,
        });
        setChartData([]);
        localStorage.removeItem('chartData');
        localStorage.removeItem('tradeData');
        localStorage.removeItem('accountData');
        localStorage.removeItem('indicatorData');
        localStorage.removeItem('analysisData');
        setAnalysisData(null);
        setEquityHistory([]);
        setBacktestErrorMsg("")
        setTrades([]);
        setTradeIdMap(new Map());
        tradeCounterRef.current = 1;
        setIndicators({});
        setAsync(false);
        setLogs([]);
    }

    const startStrategy = async () => {
        if (strategyClass === '') {
            log.error('Strategy class is required');
            toast({
                title: 'Strategy Required',
                description: 'Please select a strategy',
                variant: 'warning',
            });
            return;
        }

        cleanChartData();

        const hackConfig = {
            ...strategyConfig,
            strategyClass: strategyClass,
        };

        log.debug('Starting strategy...');

        try {
            log.debug('Starting strategy with config:', strategyConfig);

            const generatedIdForClass = await apiClient.generateId(hackConfig);
            setIsStrategyRunning(true);

            socketRef.current = await apiClient.connectWebSocket(
                generatedIdForClass,
                handleWebSocketMessage
            );

            if (hackConfig.speed === 'INSTANT') {
                setAsync(true);
            } else {
                // We only hide chart for instant runs
                setShowChart(true);
            }

            log.debug('WebSocket connected');
            await apiClient.startStrategy(hackConfig, generatedIdForClass, showChart);
            setStartTime(Date.now());
        } catch (error) {
            log.error('Failed to start strategy:', error);
            toast({
                title: 'Strategy Start Failed',
                description: `Failed to start strategy: ${error.message}`,
                variant: 'destructive',
            });
            setIsStrategyRunning(false);
        }
    };

    const stopStrategy = async () => {
        try {
            if (strategyClass !== '') {
                // We can stop strategy by just closing ws connection
                setIsStrategyRunning(false);
                setAccount({
                    initialBalance: 0,
                    balance: 0,
                    equity: 0,
                });
                if (socketRef.current) {
                    socketRef.current.close();
                }
            }
        } catch (error) {
            log.error('Failed to stop strategy:', error);
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
            updateAccount(data);
        } else if (data.type === 'STRATEGY_STOP') {
            log.info('Strategy stop event');
            setIsStrategyRunning(false);
            setAsync(false);
        } else if (data.type === 'ANALYSIS') {
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
            updateAsyncProgress(data);
        } else if (data.type === 'ERROR') {
            setBacktestErrorMsg(data.message);
            setIsStrategyRunning(false);
            toast({
                title: 'Backtest Run Error',
                description: `Strategy failed to run due to : ${data.message}`,
                variant: 'destructive',
            });
        } else {
            log.debug('WHAT OTHER EVENT WAS SENT?' + data);
        }
    };

    const updateAsyncProgress = (data) => {
        setProgressData(data);
    };

    const updateLogs = (data) => {
        setLogs((prevLogs) => {
            return [
                {
                    timestamp: new Date(data.time * 1000).toLocaleString(),
                    type: data.logType,
                    message: data.message,
                },
                ...prevLogs,
            ];
        });
    };

    const setAnalysis = (data) => {
        setAnalysisData(data);
        setEquityHistory(data.equityHistory);
        saveAnalysisDataToLocalStorage(data);
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
            saveIndicatorDataToLocalStorage(newIndicators);
            return newIndicators;
        });
    };

    const updateAccount = (data) => {
        const accountData = {
            initialBalance: data.account.initialBalance,
            balance: data.account.balance.toFixed(2),
            equity: data.account.equity.toFixed(2),
        };
        setAccount(accountData);
        saveAccountDataToLocalStorage(accountData);
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
                if (!showChart) {
                    // If no chart. Dont load the chart
                    return;
                }
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
                    saveChartDataToLocalStorage(newChartData);
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

                    saveTradeDataToLocalStorage(newTrades);
                    return newTrades;
                });

                // Reset the tradeIdMap and tradeCounter
                setTradeIdMap(new Map());
                tradeCounterRef.current = 0;
            }
        },
        [tradeIdMap]
    );

    const handleOpenParams = () => {
        setIsModalOpen(true);
    };

    const getParams = async (stratClass) => {
        try {
            return await apiClient.getParams(stratClass);
        } catch (error) {
            log.error('Failed to get strategy params:', error);
            toast({
                title: 'Params Fetch Failed',
                description: `Failed to get strategy params: ${error.message}`,
                variant: 'destructive',
            });
        }
    };

    const handleConfigSave = (config) => {
        log.debug('Saving params:', config);
        setIsModalOpen(false);
    };

    const handleChangeStrategy = async (valueOrEvent) => {
        setChartData([]);
        setTrades([]);
        setAccount({
            initialBalance: 0,
            balance: 0,
            equity: 0,
        });
        setIndicators({});
        setAnalysisData(null);
        setEquityHistory([]);

        // Get the class of the strategy
        let stratClass;
        // Hack to use this function in other places
        if (typeof valueOrEvent === 'string') {
            // If a string is passed directly
            stratClass = valueOrEvent;
        } else if (valueOrEvent && valueOrEvent.target) {
            // If an event object is passed (from onChange)
            stratClass = valueOrEvent.target.value;
        } else {
            log.error('Invalid input to handleChangeStrategy');
            return;
        }
        localStorage.setItem('LAST_STRAT', stratClass);
        setStrategyClass(stratClass);

        // Update config with class
        setStrategyConfig({
            ...strategyConfig,
            strategyClass: stratClass,
        });

        // Now, we know what params have come from the strategy defaults. So we should set these are the run params for now.
        const params = await getParams(stratClass);
        log.debug('Params', params);

        let runParams = [];
        params.forEach((param) => {
            runParams.push({
                name: param.name,
                // Default from server
                value: param.value,
                // Defaults
                defaultValue: param.value,
                description: param.description,
                group: param.group,
                start: '1',
                stop: '1',
                step: '1',
                selected: false,
            });
        });

        log.debug('Run Params', runParams);

        setStrategyConfig({
            ...strategyConfig,
            runParams: runParams,
        });

        // Now we have the defaults, we need to make sure we have the values from local storage, in case we changed this at any point
        loadConfigFromLocalStorage(runParams, stratClass);
    };

    const saveChartDataToLocalStorage = (data) => {
        try {
            localStorage.setItem('chartData', JSON.stringify(data));
        } catch (error) {
            log.error('Failed to save chart data to localStorage:', error);
        }
    };

    const saveTradeDataToLocalStorage = (data) => {
        try {
            localStorage.setItem('tradeData', JSON.stringify(data));
        } catch (error) {
            log.error('Failed to save trade data to localStorage:', error);
        }
    };

    const saveAccountDataToLocalStorage = (data) => {
        try {
            localStorage.setItem('accountData', JSON.stringify(data));
        } catch (error) {
            log.error('Failed to save account data to localStorage:', error);
        }
    };

    const saveIndicatorDataToLocalStorage = (data) => {
        try {
            localStorage.setItem('indicatorData', JSON.stringify(data));
        } catch (error) {
            log.error('Failed to save indicator data to localStorage:', error);
        }
    };

    const saveAnalysisDataToLocalStorage = (data) => {
        try {
            localStorage.setItem('analysisData', JSON.stringify(data));
        } catch (error) {
            log.error('Failed to save analysis data to localStorage:', error);
        }
    };

    return {
        isStrategyRunning,
        account,
        trades,
        indicators,
        chartData,
        analysisData,
        equityHistory,
        logs,
        tabValue,
        setTabValue,
        isModalOpen,
        setIsModalOpen,
        strategies,
        strategyClass,
        isAsync,
        progressData,
        showChart,
        startTime,
        strategyConfig,
        setStrategyConfig,
        startOptimisation,
        startStrategy,
        backtestErrorMsg,
        stopStrategy,
        handleOpenParams,
        handleConfigSave,
        handleChangeStrategy,
        updateTradingViewChart,
    };
};
