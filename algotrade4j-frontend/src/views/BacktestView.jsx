import React, {useCallback, useEffect, useRef, useState} from 'react';
import {apiClient} from '../api/apiClient.js';
import 'chartjs-adapter-date-fns';
import AnalysisReport from '../components/AnalysisReport.jsx';
import {EquityChart} from '../components/EquityChart.jsx';
import TradesTable from '../components/TradesTable.jsx';
import {Box, Button, FormControl, FormControlLabel, Grid, InputLabel, MenuItem, Paper, Select, Switch, Tab, Tabs, Typography,} from '@mui/material';
import {TabPanel} from '../components/TabPanel.jsx';
import LogsTable from '../components/LogsTable.jsx';
import ConfigModal from '../components/modals/ConfigModal.jsx';
import {Toast} from "../components/Toast.jsx";
import {OptimisationPanel} from "../components/OptimisationPanel.jsx";
import LoadingChart from "../components/LoadingChart.jsx";
import TradingViewChart from "../components/TradingViewChart.jsx";
import EmptyChart from "../components/EmptyChart.jsx";

const BacktestView = () => {
    const socketRef = useRef(null);

    const [isRunning, setIsRunning] = useState(false);
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
    const [isModalOpen, setIsModalOpen] = useState(false);

    const [runOptimisation, setRunOptimisation] = useState(false);
    const [optimisationId, setOptimisationId] = useState("");

    const [strategies, setStrategies] = useState([]);
    const [strategyClass, setStrategyClass] = useState("");

    const [isAsync, setAsync] = useState(false)
    const [progressData, setProgressData] = useState(null);
    const [showChart, setShowChart] = useState(true)


    // const [rawParams, setRawParams] = useState([]);
    // const [runParams, setRunParams] = useState([])
    const [strategyConfig, setStrategyConfig] = useState({
        strategyClass: '',
        initialCash: '10000',
        instrumentData: {},
        spread: "50",
        speed: "NORMAL",
        period: "1D",
        timeframe: {
            from: '',
            to: '',
        },
        runParams: []
    })

    const [toast, setToast] = useState({
        open: false,
        level: 'info',
        message: '',
    });

    const handleCloseToast = (event, reason) => {
        if (reason === 'clickaway') {
            return;
        }
        setToast({...toast, level: "info", open: false});
    };

    const handleTabChange = (event, newValue) => {
        setTabValue(newValue);
    };

    useEffect(() => {
        const fetchStrategies = async () => {
            try {
                const res = await apiClient.getStrategies()
                setStrategies(res)

                const lastStrat = localStorage.getItem("LAST_STRAT")

                res.forEach(strat => {
                    if (strat === lastStrat) {
                        handleChangeStrategy(lastStrat)
                    }
                })


            } catch (error) {
                console.error('Failed to get strategies:', error);
            }
        }

        fetchStrategies()

        return () => {
            if (socketRef.current) {
                socketRef.current.close();
            }
        };
    }, []);

    // Here we check local storage and update the config with the values from local storage
    // if there are any, else we use the defaults from the strategy
    const loadConfigFromLocalStorage = (runParams, stratClass) => {
        const storedConfig = JSON.parse(localStorage.getItem(`strategyConfig_${stratClass}`)) || {};

        // Check storedConfig for runParams, if we have them, we need to update the values!!!!! in the run params
        const updatedRunParams = runParams.map(param => {
            const storedParam = storedConfig.runParams?.find(p => p.name === param.name);

            // Only update if theres data to update
            if (storedParam) {
                return {
                    ...param,
                    value: storedParam.value !== undefined ? storedParam.value : param.value,
                    start: storedParam.start !== undefined ? storedParam.start : param.start,
                    stop: storedParam.stop !== undefined ? storedParam.stop : param.stop,
                    step: storedParam.step !== undefined ? storedParam.step : param.step,
                    selected: storedParam.selected !== undefined ? storedParam.selected : param.selected,
                };
            } else {
                return param; // Keep the original parameter if no stored version is found
            }
        });

        // Setting some defaults in case we don't have any values in local storage
        const today = new Date().toISOString().split('T')[0] + 'T00:00:00Z';
        const lastMonth = new Date(Date.now() - (86400000 * 30)).toISOString().split('T')[0] + 'T00:00:00Z';

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

    const startStrategy = async () => {
        if (strategyClass === "") {
            console.error('Strategy class is required');
            setToast({
                open: true,
                level: 'warning',
                message: 'Please select a strategy',
            });
            return;
        }
        // We need to add the strategy class to strategy config
        // TODO: If we need this in other places we need to refactor
        const hackConfig = {
            ...strategyConfig,
            strategyClass: strategyClass,
        }

        setIsRunning(true);
        // Clean previous data
        setChartData([]);
        setAnalysisData(null);
        setEquityHistory([]);
        setTrades([]);
        setTradeIdMap(new Map());
        tradeCounterRef.current = 1;
        setIndicators({});
        setAsync(false);
        setLogs([])
        console.log('Starting strategy...');

        if (runOptimisation) {
            setToast({
                open: true,
                message: "Optimisation is not supported yet",
                level: "warn"
            })
            return

            const oId = crypto.randomUUID()
            setOptimisationId(oId)
            try {
                await apiClient.startOptimisation(hackConfig, oId)
                console.log('Optimisation started');
                setToast({
                    open: true,
                    level: 'success',
                    message: 'Optimisation started',
                })
                setIsRunning(false);
            } catch (error) {
                console.error('Failed to start optimisation:', error);
                setToast({
                    open: true,
                    level: 'error',
                    message: 'Failed to start optimisation: ' + error.response.data.message,
                })
                setIsRunning(false);
            }
            return
        }

        try {
            console.log('Starting strategy with config:', strategyConfig);

            const generatedIdForClass = await apiClient.generateId(hackConfig)

            socketRef.current = await apiClient.connectWebSocket(
                generatedIdForClass,
                handleWebSocketMessage
            );

            if (hackConfig.speed === "INSTANT") {
                setAsync(true);
            }

            console.log('WebSocket connected');
            await apiClient.startStrategy(hackConfig, generatedIdForClass, showChart);
        } catch (error) {
            console.error('Failed to start strategy:', error);
            setToast({
                open: true,
                level: 'error',
                message: 'Failed to start strategy: ' + error.response.data.message,
            })
            setIsRunning(false);
        }
    };

    const stopStrategy = async () => {
        try {
            if (strategyClass !== "") {
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
        } else if (data.type === 'ACCOUNT' || data.type === 'ASYNC_ACCOUNT') {
            updateAccount(data);
        } else if (data.type === 'STRATEGY_STOP') {
            setIsRunning(false);
            setAsync(false);
        } else if (data.type === 'ANALYSIS') {
            setAnalysis(data);
        } else if (data.type === 'TRADE' && data.action === 'UPDATE') {
            updateTrades(data);
        } else if (data.type === 'LOG') {
            updateLogs(data);
        } else if (data.type === "BAR_SERIES") {
            updateTradingViewChart(data)
        } else if (data.type === "ALL_TRADES") {
            updateTradingViewChart(data)
        } else if (data.type === "ALL_INDICATORS") {
            setAllIndicators(data)
        } else if (data.type === "PROGRESS") {
            updateAsyncProgress(data)
        } else if (data.type === 'ERROR') {
            setToast({
                open: true,
                level: "error",
                message: data.message,
            });
        } else {
            console.log('WHAT OTHER EVENT WAS SENT?' + data);
        }
    };

    const updateAsyncProgress = (data) => {
        console.log(data)
        setProgressData(data);
    }

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
        if (data.value.value.value !== 0) {
            // Only add non-zero values
            setIndicators((prevIndicators) => ({
                ...prevIndicators,
                [data.indicatorName]: [
                    ...(prevIndicators[data.indicatorName] || []),
                    {time: data.value.dateTime, value: data.value.value.value},
                ],
            }));
        }
    };

    const setAllIndicators = (data) => {
        setIndicators(() => {
            const newIndicators = {};
            Object.entries(data.indicators).forEach(([indicatorName, values]) => {
                newIndicators[indicatorName] = values
                    .filter(indicator => indicator.value.value !== 0)
                    .map(indicator => ({
                        time: indicator.dateTime,
                        value: indicator.value.value
                    }));
            });
            return newIndicators;
        });
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
                                instrument: trade.instrument,
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
            } else if (data.type === "BAR_SERIES") {
                if (!showChart) {
                    // If no chart. Dont load the chart
                    return;
                }
                const barSeries = data.barSeries.bars;
                setChartData(() => {
                    // Convert the bar series to the format expected by the chart
                    const newChartData = barSeries.map(bar => ({
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
            } else if (data.type === "ALL_TRADES") {
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
                        quantity: trade.quantity.value,
                        isLong: trade.long,
                        position: trade.long ? 'long' : 'short',
                        price: trade.closePrice ? trade.closePrice.value : trade.entryPrice.value,
                        profit: trade.profit ? trade.profit.value : null,
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

    const handleOpenParams = () => {
        setIsModalOpen(true);
    }

    const getParams = async (stratClass) => {
        try {
            return await apiClient.getParams(stratClass);
        } catch (error) {
            console.error('Failed to get strategy params:', error);
            setToast({
                open: true,
                message: 'Failed to get strategy params: ' + error.response.data.message,
            });
        }
    };

    const handleConfigSave = (config) => {
        console.log('Saving params:', config);
        setIsModalOpen(false);
    };

    const handleChangeStrategy = async (valueOrEvent) => {
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
            console.error('Invalid input to handleChangeStrategy');
            return;
        }
        localStorage.setItem("LAST_STRAT", stratClass);
        setStrategyClass(stratClass);

        // Update config with class
        setStrategyConfig({
            ...strategyConfig,
            strategyClass: stratClass,
        });

        // Now, we know what params have come from the strategy defaults. So we should set these are the run params for now.
        const params = await getParams(stratClass)
        console.log("Params", params)

        let runParams = [];
        params.forEach(param => {
            runParams.push({
                name: param.name,
                // Default from server
                value: param.value,
                // Defaults
                defaultValue: param.value,
                description: param.description,
                group: param.group,
                start: "1",
                stop: "1",
                step: "1",
                selected: false,
            })
        })

        console.log("Run Params", runParams)

        setStrategyConfig({
            ...strategyConfig,
            runParams: runParams,
        })

        // Now we have the defaults, we need to make sure we have the values from local storage, in case we changed this at any point
        loadConfigFromLocalStorage(runParams, stratClass);

        // When we change the strategy, we should get the parameters local storage or use defaults
        // we need start stop step for optimisation only. But this is something that need to be here!
        // for run parameters we only need it for starting strategy
    }

    return (
        <Box sx={{
            display: 'flex',
            flexDirection: 'column',
            height: 'calc(100vh - 64px - 30px)', // Subtract navbar height and version banner
            width: '100vw',
            overflow: 'hidden' // Prevent scrolling on the main container
        }}>
            {/* Header */}
            <Box sx={{flexShrink: 0, p: 2, bgcolor: 'background.paper', overflow: 'auto'}}>
                <Grid container justifyContent="flex-end" spacing={2}>
                    {[
                        {label: 'Initial Balance', value: account.initialBalance},
                        {label: 'Current Balance', value: account.balance},
                        {label: 'Equity', value: account.equity},
                        {
                            label: 'Open Position Value',
                            value: Math.round((account.equity - account.balance + Number.EPSILON) * 100) / 100
                        }
                    ].map((item, index) => (
                        <Grid item key={index}>
                            <Paper elevation={3} sx={{p: 1, minWidth: '200px'}}>
                                <Typography variant="body2" color="text.secondary">
                                    {item.label}
                                </Typography>
                                <Typography variant="h6" fontWeight="bold">
                                    ${item.value.toLocaleString()}
                                </Typography>
                            </Paper>
                        </Grid>
                    ))}
                </Grid>
            </Box>

            {/* Main content area */}
            <Box sx={{flexGrow: 1, display: 'flex', overflow: 'hidden'}}>
                {/* Left section (3/4 width) */}
                <Box sx={{flexGrow: 1, height: '100%', overflow: 'hidden'}}>
                    <Paper elevation={3} sx={{height: '100%', display: 'flex', flexDirection: 'column', p: 3}}>
                        {/* Chart Section */}
                        <Box sx={{flexShrink: 0, height: '40%', minHeight: '500px', mb: 3, bgcolor: 'background.paper', borderRadius: 1, overflow: 'hidden'}}>
                            {isRunning && isAsync ? (
                                <LoadingChart progressData={progressData} startTime={Date.now()}/>
                            ) : chartData && chartData.length > 0 ? (
                                <TradingViewChart showChart={showChart} strategyConfig={strategyConfig} chartData={chartData} trades={trades} indicators={indicators}/>
                            ) : (
                                <EmptyChart trades={trades} showChart={showChart}/>
                            )}
                        </Box>

                        {/* Tabs and Content Section */}
                        <Box sx={{flexGrow: 1, display: 'flex', flexDirection: 'column', overflow: 'hidden'}}>
                            <Tabs
                                value={tabValue}
                                onChange={handleTabChange}
                                aria-label="strategy tabs"
                                sx={{borderBottom: 1, borderColor: 'divider', mb: 2}}
                            >
                                <Tab label="Trades"/>
                                <Tab label="Analysis"/>
                                <Tab label="Equity History"/>
                                <Tab label="Logs"/>
                                {runOptimisation & (
                                    <Tab label="Optimisation"/>
                                )}
                            </Tabs>

                            <Box sx={{flexGrow: 1, overflow: 'auto'}}>
                                <TabPanel value={tabValue} index={0}>
                                    <TradesTable trades={trades}/>
                                </TabPanel>
                                <TabPanel value={tabValue} index={1}>
                                    {analysisData !== null ? (
                                        <AnalysisReport data={analysisData}/>
                                    ) : (
                                        <Typography variant="body1" sx={{p: 2, textAlign: 'center'}}>
                                            No analysis data available yet.
                                        </Typography>
                                    )}
                                </TabPanel>
                                <TabPanel value={tabValue} index={2}>
                                    {equityHistory.length > 0 ? (
                                        <EquityChart equityHistory={equityHistory}/>
                                    ) : (
                                        <Typography variant="body1" sx={{p: 2, textAlign: 'center'}}>
                                            No equity history available yet.
                                        </Typography>
                                    )}
                                </TabPanel>
                                <TabPanel value={tabValue} index={3}>
                                    {logs.length > 0 ? (
                                        <LogsTable logs={logs}/>
                                    ) : (
                                        <Typography variant="body1" sx={{p: 2, textAlign: 'center'}}>
                                            No logs available yet.
                                        </Typography>
                                    )}
                                </TabPanel>
                                {runOptimisation && (
                                    <TabPanel value={tabValue} index={4}>
                                        <OptimisationPanel setToast={setToast} optimisationId={optimisationId}/>
                                    </TabPanel>
                                )}
                            </Box>
                        </Box>
                    </Paper>
                </Box>

                {/* Right section (1/4 width) */}
                <Box sx={{width: '25%', minWidth: '300px', p: 3, bgcolor: 'background.paper', boxShadow: 1, overflow: 'auto'}}>
                    <Box sx={{display: 'flex', flexDirection: 'column', height: '100%'}}>
                        <FormControl fullWidth variant="outlined" sx={{mb: 3}}>
                            <InputLabel>Select a strategy</InputLabel>
                            <Select
                                value={strategyClass}
                                onChange={handleChangeStrategy}
                                label="Select a strategy"
                                displayEmpty
                            >
                                {strategies.length > 0 ? (
                                    strategies.map((strategy, index) => (
                                        <MenuItem key={index} value={strategy}>
                                            {strategy}
                                        </MenuItem>
                                    ))
                                ) : (
                                    <MenuItem value="" disabled>
                                        No strategies available
                                    </MenuItem>
                                )}
                            </Select>
                        </FormControl>

                        <Button
                            variant="contained"
                            color={isRunning ? 'error' : 'success'}
                            onClick={isRunning ? stopStrategy : startStrategy}
                            disabled={strategyClass === ""}
                            size="large"
                            sx={{mb: 2}}
                        >
                            {isRunning ? 'Stop Strategy' : 'Start Strategy'}
                        </Button>

                        <Button
                            variant="outlined"
                            onClick={handleOpenParams}
                            disabled={strategyClass === ""}
                            size="large"
                            sx={{mb: 3}}
                        >
                            Configure Parameters
                        </Button>

                        <FormControlLabel
                            control={
                                <Switch
                                    checked={runOptimisation}
                                    onChange={(e) => setRunOptimisation(e.target.checked)}
                                    color="primary"
                                />
                            }
                            label="Run optimisation?"
                            sx={{mb: 2}}
                        />
                        <FormControlLabel
                            control={
                                <Switch
                                    checked={showChart}
                                    onChange={(e) => setShowChart(e.target.checked)}
                                    color="primary"
                                />
                            }
                            label="Visual Mode?"
                            sx={{mb: 2}}
                        />

                        <Box sx={{flexGrow: 1}}/> {/* Spacer */}

                        <Typography variant="body2" color="text.secondary" align="center">
                            Select a strategy and configure parameters before starting.
                        </Typography>
                    </Box>
                </Box>
            </Box>

            {/* Modals */}
            <ConfigModal
                open={isModalOpen}
                onClose={() => setIsModalOpen(false)}
                onSave={handleConfigSave}
                strategyConfig={strategyConfig}
                setStrategyConfig={setStrategyConfig}
                strategyClass={strategyClass}
            />
            <Toast
                open={toast.open}
                message={toast.message}
                severity={toast.level}
                onClose={handleCloseToast}
            />
        </Box>
    );
};

export default BacktestView;