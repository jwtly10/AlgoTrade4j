import React, {useCallback, useEffect, useRef, useState} from 'react';
import {ColorType, createChart} from 'lightweight-charts';
import {client} from '../api/client';
import 'chartjs-adapter-date-fns';
import AnalysisReport from "./AnalysisReport.jsx";
import {EquityChart} from "./EquityChart.jsx";


const StrategyChart = () => {
    const socketRef = useRef(null);

    const [isRunning, setIsRunning] = useState(false);
    const [strategyId, setStrategyId] = useState(null);
    const [account, setAccount] = useState({
        initialBalance: 0,
        balance: 0,
        equity: 0,
    })

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
            width: 600,
            height: 500,
            layout: {
                background: {type: ColorType.Solid, color: '#ffffff'},
                textColor: 'black',
            },
        });

        window.addEventListener('resize', handleResize);
        handleResize();

        const candlestickSeries = chart.addCandlestickSeries({
            upColor: '#26a69a', downColor: '#ef5350', borderVisible: false,
            wickUpColor: '#26a69a', wickDownColor: '#ef5350',
        });

        try {
            candlestickSeries.setData(chartData);
        } catch (e) {
            console.error('Failed to set data:', e);
            console.log(chartData)
        }

        // Add indicator series
        const indicatorSeries = {};
        Object.keys(indicators).forEach(indicatorName => {
            const indicatorData = indicators[indicatorName];
            if (indicatorData && indicatorData.length > 0) {
                // Filter out zero values and invalid entries
                const validData = indicatorData
                    .filter(item => !isNaN(item.time) && !isNaN(item.value) && item.value !== 0)
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

        const markers = trades.map(trade => ({
            time: trade.action === 'OPEN' ? trade.openTime : trade.closeTime,
            position: trade.position === 'long' ? 'belowBar' : 'aboveBar',
            color: trade.position === 'long' ? '#26a69a' : '#ef5350',
            shape: trade.action === 'OPEN' ? 'arrowUp' : 'arrowDown',
            text: `#${trade.tradeId} ${trade.action} ${trade.position.toUpperCase()} @ ${trade.price}`,
        }));


        markers.sort((a, b) => a.time - b.time);
        try {
            candlestickSeries.setMarkers(markers);
        } catch (e) {
            console.error('Failed to set trade markers:', e);
            console.log(markers)
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

    const startStrategy = async () => {
        setIsRunning(true);
        // Clean previous data
        setChartData([]);
        setAnalysisData(null);
        setEquityHistory([])
        setTrades([]);
        setTradeIdMap(new Map());
        tradeCounterRef.current = 1;
        setIndicators({});
        console.log('Starting strategy...');
        try {
            const config = {
                'strategyId': 'SimpleSMAStrategy',
                'subscriptions': ['BAR', 'TRADE', 'INDICATOR'],
                'initialCash': '10000',
                'barSeriesSize': 10000,
            };
            setStrategyId('SimpleSMAStrategy');

            socketRef.current = await client.connectWebSocket('SimpleSMAStrategy', handleWebSocketMessage);
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
                await client.stopStrategy(strategyId);
                setIsRunning(false);
                if (socketRef.current) {
                    socketRef.current.close();
                }
                // setMessages([]);
            }
        } catch (error) {
            console.error('Failed to stop strategy:', error);
        }
    };

    const handleWebSocketMessage = (data) => {
        console.log('New data from websocket:', data);

        if (data.type === 'BAR' || data.type === 'TRADE' && (data.action === "OPEN" || data.action === "CLOSE")) {
            updateTradingViewChart(data);
        } else if (data.type === 'INDICATOR') {
            updateIndicator(data);
        } else if (data.type === 'ACCOUNT') {
            updateAccount(data)
        } else if (data.type === 'STRATEGY_STOP') {
            setIsRunning(false);
        } else if (data.type === 'ANALYSIS') {
            setAnalysis(data)
        } else if (data.type === 'TRADE' && data.action === "UPDATE") {
            console.log("Trade update event")
            updateTrades(data);
        } else {
            console.log("WHAT OTHER EVENT WAS SENT?" + data)
        }
    };

    const setAnalysis = (data) => {
        console.log(data)
        setAnalysisData(data)
        setEquityHistory(data.equityHistory);
    };

    const updateTrades = (data) => {
        // Update the profit value of the trade
        const trade = data.trade;
        setTrades(prevTrades => {
            const updatedTrades = prevTrades.map(prevTrade => {
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
    }

    const updateIndicator = (data) => {
        if (data.value.value !== 0) {  // Only add non-zero values
            setIndicators(prevIndicators => ({
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
    }

    const updateTradingViewChart = useCallback((data) => {
        if (data.type === 'BAR') {
            const bar = data.bar;
            setChartData(prevData => {
                const lastBar = prevData[prevData.length - 1];
                if (lastBar && lastBar.time === bar.openTime) {
                    console.log("New tick");
                    // Update the existing bar
                    const updatedBar = {
                        ...lastBar,
                        high: Math.max(lastBar.high, bar.high.value),
                        low: Math.min(lastBar.low, bar.low.value),
                        close: bar.close.value
                    };
                    return [...prevData.slice(0, -1), updatedBar];
                } else {
                    // Add a new bar
                    return [...prevData, {
                        time: bar.openTime,
                        open: bar.open.value,
                        high: bar.high.value,
                        low: bar.low.value,
                        close: bar.close.value,
                    }];
                }
            });
        } else if (data.type === 'TRADE') {
            const trade = data.trade;
            setTradeIdMap(prevMap => {
                const newMap = new Map(prevMap);
                if (!newMap.has(trade.id)) {
                    newMap.set(trade.id, tradeCounterRef.current);
                    tradeCounterRef.current += 1;
                }
                return newMap;
            });

            console.log(trade)
            setTrades(prevTrades => [...prevTrades, {
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
                price: data.action === 'CLOSE' ? trade.closePrice.value : trade.entryPrice.value,
                profit: trade.profit.value,
                action: data.action,
            }]);
        }
    }, [tradeIdMap]);

    return (
        <div className="chart-container">
            <h1>Strategy Chart</h1>
            {!isRunning ? (
                <button onClick={startStrategy}>Start Strategy</button>
            ) : (
                <button onClick={stopStrategy}>Stop Strategy</button>
            )}
            <p>Strategy ID: {strategyId}</p>
            <p>Initial Balance: ${account.initialBalance} Current Balance: ${account.balance} Equity:
                ${account.equity} Open Position Value: ${
                    Math.round(((account.equity - account.balance) + Number.EPSILON) * 100) / 100
                } </p>
            <div style={{width: '100%'}} ref={chartContainerRef}/>
            <table>
                <thead>
                <tr>
                    <th>Order</th>
                    <th>Time</th>
                    <th>Type</th>
                    <th>State</th>
                    <th>Size</th>
                    <th>Symbol</th>
                    <th>Price</th>
                    <th>S/L</th>
                    <th>T/P</th>
                    <th>Profit</th>
                </tr>
                </thead>
                <tbody>
                {trades.map(trade => (
                    <tr key={trade.id}>
                        <td>{trade.tradeId}</td>
                        <td>{new Date(trade.openTime * 1000).toLocaleString()}</td>
                        <td>{trade.isLong ? "LONG" : "SHORT"}</td>
                        <td>{trade.closePrice === 0 ? "OPEN" : "CLOSED"}</td>
                        <td>{trade.quantity}</td>
                        <td>{trade.symbol}</td>
                        <td>{trade.entry}</td>
                        <td>{trade.stopLoss}</td>
                        <td>{trade.takeProfit}</td>
                        <td>{trade.profit}</td>
                    </tr>
                ))}
                </tbody>
            </table>

            {analysisData !== null && (
                <div>
                    <AnalysisReport data={analysisData}/>
                    {
                        equityHistory.length > 0 && (
                            <EquityChart equityHistory={equityHistory}/>
                        )
                    }
                </div>
            )}
        </div>
    );
};

export default StrategyChart;