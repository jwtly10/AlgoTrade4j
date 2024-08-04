import React, { useCallback, useEffect, useRef, useState } from 'react';
import { ColorType, createChart } from 'lightweight-charts';
import { client } from '../api/client';

const StrategyChart = () => {
    const [strategyId, setStrategyId] = useState(null);
    const [messages, setMessages] = useState([]);
    const [isRunning, setIsRunning] = useState(false);
    const socketRef = useRef(null);
    const [trades, setTrades] = useState([]);
    const [tradeIdMap, setTradeIdMap] = useState(new Map());
    const tradeCounterRef = useRef(1);


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
                background: { type: ColorType.Solid, color: '#ffffff' },
                textColor: 'black',
            },
        });

        window.addEventListener('resize', handleResize);
        handleResize();


        const candlestickSeries = chart.addCandlestickSeries({
            upColor: '#26a69a', downColor: '#ef5350', borderVisible: false,
            wickUpColor: '#26a69a', wickDownColor: '#ef5350',
        });

        candlestickSeries.setData(chartData);

        const markers = trades.map(trade => ({
            time: trade.action === 'OPEN' ? trade.openTime : trade.closeTime,
            position: trade.position === 'long' ? 'belowBar' : 'aboveBar',
            color: trade.position === 'long' ? '#26a69a' : '#ef5350',
            shape: trade.action === 'OPEN' ? 'arrowUp' : 'arrowDown',
            text: `#${trade.id} ${trade.action} ${trade.position.toUpperCase()} @ ${trade.price}`,
        }));


        candlestickSeries.setMarkers(markers);

        return () => {
            window.removeEventListener('resize', handleResize);
            chart.remove();
        };
    }, [chartData, trades]);


    const startStrategy = async () => {
        setMessages([]);
        setIsRunning(true);
        console.log('Starting strategy...');
        try {
            const config = {
                'strategyId': 'SimplePrintStrategy',
                'subscriptions': ['BAR', 'TRADE'],
                'initialCash': '10000',
                'barSeriesSize': 10000,
            };
            setStrategyId('SimplePrintStrategy');

            socketRef.current = await client.connectWebSocket('SimplePrintStrategy', handleWebSocketMessage);
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
        setMessages((prevMessages) => [...prevMessages, data]);
        updateTradingViewChart(data);
    };

    const updateTradingViewChart = useCallback((data) => {
        console.log('Updating TradingView chart with:', data);
        if (data.type === 'BAR') {
            const bar = data.bar;
            setChartData(prevData => [...prevData, {
                time: bar.dateTime / 1000,
                open: bar.open.value,
                high: bar.high.value,
                low: bar.low.value,
                close: bar.close.value,
            }]);
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
                openTime: trade.openTime / 1000,
                closeTime: trade.closeTime / 1000,
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
            <div style={{ width: '100%' }} ref={chartContainerRef} />
        </div>
    );
};

export default StrategyChart;