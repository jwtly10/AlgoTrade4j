import React, {useCallback, useEffect, useRef, useState} from 'react';
import {ColorType, createChart} from 'lightweight-charts';
import {client} from '../api/client';

const StrategyChart = () => {
    const [strategyId, setStrategyId] = useState(null);
    const [messages, setMessages] = useState([]);
    const [isRunning, setIsRunning] = useState(false);
    const socketRef = useRef(null);
    const [trades, setTrades] = useState([]);
    const [tradeIdMap, setTradeIdMap] = useState(new Map());
    const tradeCounterRef = useRef(1);
    const [indicators, setIndicators] = useState({});


    const [chartData, setChartData] = useState([]);
    const chartContainerRef = useRef();

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
            text: `#${trade.id} ${trade.action} ${trade.position.toUpperCase()} @ ${trade.price}`,
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

    const convertToUnixTimestamp = (isoString) => {
        return Math.floor(new Date(isoString).getTime() / 1000);
    };

    const startStrategy = async () => {
        setMessages([]);
        setIsRunning(true);
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
        setMessages((prevMessages) => [...prevMessages, data]);
        if (data.type === 'BAR' || data.type === 'TRADE') {
            updateTradingViewChart(data);
        } else if (data.type === 'INDICATOR') {
            updateIndicator(data);
        }
    };

    const updateIndicator = (data) => {
        if (data.value.value !== 0) {  // Only add non-zero values
            setIndicators(prevIndicators => ({
                ...prevIndicators,
                [data.indicatorName]: [
                    ...(prevIndicators[data.indicatorName] || []),
                    {time: convertToUnixTimestamp(data.dateTime), value: data.value.value},
                ],
            }));
        }
    };

    const updateTradingViewChart = useCallback((data) => {
        if (data.type === 'BAR') {
            const bar = data.bar;
            setChartData(prevData => {
                const lastBar = prevData[prevData.length - 1];
                if (lastBar && lastBar.time === convertToUnixTimestamp(bar.openTime)) {
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
                        time: convertToUnixTimestamp(bar.openTime),
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

            setTrades(prevTrades => [...prevTrades, {
                id: tradeIdMap.get(trade.id) || tradeCounterRef.current - 1,
                tradeId: trade.id,
                openTime: convertToUnixTimestamp(trade.openTime),
                closeTime: convertToUnixTimestamp(trade.closeTime),
                position: trade.long ? 'long' : 'short',
                price: data.action === 'CLOSE' ? trade.closePrice.value : trade.entryPrice.value,
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
            <div style={{width: '100%'}} ref={chartContainerRef}/>
        </div>
    );
};

export default StrategyChart;