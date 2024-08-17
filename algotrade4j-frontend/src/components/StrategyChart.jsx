import React, {useCallback, useEffect, useRef, useState} from 'react';
import {ColorType, createChart, CrosshairMode, TickMarkType} from 'lightweight-charts';
import {apiClient} from '../api/apiClient.js';
import 'chartjs-adapter-date-fns';
import AnalysisReport from './AnalysisReport.jsx';
import {EquityChart} from './EquityChart.jsx';
import TradesTable from './TradesTable.jsx';
import {Box, Button, Checkbox, Divider, FormControlLabel, Grid, MenuItem, Paper, Select, Tab, TableContainer, Tabs, Typography,} from '@mui/material';
import {TabPanel} from './TabPanel.jsx';
import LogsTable from './LogsTable.jsx';
import ConfigModal from './ConfigModal.jsx';
import {Toast} from "./Toast.jsx";
import {OptimisationPanel} from "./OptimisationPanel.jsx";


const StrategyChart = () => {
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

    // const [rawParams, setRawParams] = useState([]);
    // const [runParams, setRunParams] = useState([])
    const [strategyConfig, setStrategyConfig] = useState({
        strategyClass: '',
        initialCash: '10000',
        symbol: 'NAS100USD',
        spread: "50",
        speed: "FAST",
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
            symbol: storedConfig.symbol || strategyConfig.symbol,
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
                background: {type: ColorType.Solid, color: '#ffffff'},
                textColor: 'black',
            },
            timeScale: {
                timeVisible: true,
                secondsVisible: false,
                tickMarkFormatter: (time, tickMarkType, locale) => {
                    const localdate = new Date(time * 1000);
                    const date = new Date(localdate.getUTCFullYear(), localdate.getUTCMonth(), localdate.getUTCDate(),
                        localdate.getUTCHours(), localdate.getUTCMinutes(), localdate.getUTCSeconds());
                    const month = (date.getMonth() + 1).toString().padStart(2, '0');
                    const day = date.getDate().toString().padStart(2, '0');
                    const hours = date.getHours();
                    const minutes = date.getMinutes().toString().padStart(2, '0');

                    if (tickMarkType === TickMarkType.Year) {
                        return date.getFullYear().toString();
                    } else if (tickMarkType === TickMarkType.Month) {
                        return `${month}-${day}`;
                    } else if (tickMarkType === TickMarkType.DayOfMonth) {
                        return `${month}-${day}`;
                    } else if (tickMarkType === TickMarkType.Time) {
                        if (minutes === '00') {
                            if (hours === 0) {
                                return `${month}-${day}`;
                            } else if (hours % 12 === 0) {
                                return hours === 12 ? '12:00' : '00:00';
                            } else {
                                return `${hours}:00`;
                            }
                        } else {
                            return `${hours}:${minutes}`;
                        }
                    }

                    // Default case
                    return `${month}-${day} ${hours}:${minutes}`;
                },
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

        chart.timeScale().applyOptions({
            rightOffset: 12,
            barSpacing: 8,
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
        console.log('Starting strategy...');

        if (runOptimisation) {
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
            console.log('WebSocket connected');
            await apiClient.startStrategy(hackConfig, generatedIdForClass);
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
                    {time: data.dateTime, value: data.value.value},
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

    const handleChangeStrategy = async (event) => {
        // Get the class of the strategy
        const stratClass = event.target.value;
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
        <Paper elevation={3} className="chart-container" sx={{p: 3, mb: 3}}>
            <Grid container spacing={3}>
                <Grid item xs={12} container alignItems="center" spacing={2}>
                    <Grid item xs={4} lg={8}>
                        <Typography variant="subtitle1">Strategy:</Typography>
                    </Grid>
                    <Grid item xs={8} lg={4}>
                        <Select
                            value={strategyClass}
                            onChange={handleChangeStrategy}
                            // onChange={(e) => setStrategyClass(e.target.value)}
                            fullWidth
                            displayEmpty
                            renderValue={(selected) => {
                                if (!selected) {
                                    return "Select a strategy";
                                }
                                return selected;
                            }}
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
                    </Grid>
                </Grid>
                <Grid item xs={12} container alignItems="center" spacing={2}>
                    <Grid item xs={8}>
                        <Button
                            variant="contained"
                            color={isRunning ? 'error' : 'success'}
                            onClick={isRunning ? stopStrategy : startStrategy}
                            fullWidth
                        >
                            {isRunning ? 'Stop' : 'Start'}
                        </Button>
                    </Grid>
                    <Grid item xs={4} container direction="column" spacing={1}>
                        <Grid item>
                            <Button variant="contained" onClick={handleOpenParams} fullWidth disabled={strategyClass === ""}>
                                Params
                            </Button>
                        </Grid>
                    </Grid>
                    <Grid item>
                        <FormControlLabel
                            control={
                                <Checkbox
                                    size="small"
                                    onChange={(e) => setRunOptimisation(e.target.checked)}
                                />
                            }
                            label="Run optimisation"
                        />
                    </Grid>
                </Grid>
                <Grid item xs={12}>
                    <Divider sx={{my: 2}}/>
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

            <Box sx={{mt: 3, p: 2, bgcolor: 'background.paper', borderRadius: 1}}>
                <Box
                    sx={{width: '100%', height: '400px', overflow: 'hidden'}}
                    ref={chartContainerRef}
                />
            </Box>

            <Box sx={{borderBottom: 1, borderColor: 'divider', mt: 3}}>
                <Tabs value={tabValue} onChange={handleTabChange} aria-label="strategy tabs">
                    <Tab label="Trades"/>
                    <Tab label="Analysis"/>
                    <Tab label="Equity History"/>
                    <Tab label="Logs"/>
                    <Tab label="Optimisation"/>
                </Tabs>
            </Box>

            <TabPanel value={tabValue} index={0}>
                <TradesTable trades={trades}/>
            </TabPanel>
            <TabPanel value={tabValue} index={1}>
                {analysisData !== null ? (
                    <AnalysisReport data={analysisData}/>
                ) : (
                    <TableContainer component={Paper}>
                        <Typography variant="body1" sx={{p: 2, textAlign: 'center'}}>
                            No analysis data available yet.
                        </Typography>
                    </TableContainer>
                )}
            </TabPanel>
            <TabPanel value={tabValue} index={2}>
                {equityHistory.length > 0 ? (
                    <EquityChart equityHistory={equityHistory}/>
                ) : (
                    <TableContainer component={Paper}>
                        <Typography variant="body1" sx={{p: 2, textAlign: 'center'}}>
                            No equity history available yet.
                        </Typography>
                    </TableContainer>
                )}
            </TabPanel>
            <TabPanel value={tabValue} index={3}>
                {logs.length > 0 ? (
                    <LogsTable logs={logs}/>
                ) : (
                    <TableContainer component={Paper}>
                        <Typography variant="body1" sx={{p: 2, textAlign: 'center'}}>
                            No logs available yet.
                        </Typography>
                    </TableContainer>
                )}
            </TabPanel>
            <TabPanel value={tabValue} index={4}>
                <OptimisationPanel setToast={setToast} optimisationId={optimisationId}/>
            </TabPanel>
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
        </Paper>
    );
};

export default StrategyChart;