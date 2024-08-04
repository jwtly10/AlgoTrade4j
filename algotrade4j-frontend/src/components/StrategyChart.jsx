import React, { useEffect, useRef, useState } from 'react';
import { ColorType, createChart } from 'lightweight-charts';
import { client } from '../api/client';

const StrategyChart = () => {
    const [strategyId, setStrategyId] = useState(null);
    const [messages, setMessages] = useState([]);
    const [isRunning, setIsRunning] = useState(false);
    const socketRef = useRef(null);
    const [trades, setTrades] = useState([]);
    const [tradeCounter, setTradeCounter] = useState(1);


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
        const chart = createChart(chartContainerRef.current, {
            width: 600,
            height: 300,
            layout: {
                background: { type: ColorType.Solid, color: '#ffffff' },
                textColor: 'black',
            },
        });

        const candlestickSeries = chart.addCandlestickSeries({
            upColor: '#26a69a', downColor: '#ef5350', borderVisible: false,
            wickUpColor: '#26a69a', wickDownColor: '#ef5350',
        });

        candlestickSeries.setData(chartData);

        // Add markers for all trades
        const markers = trades.map(trade => ({
            time: trade.time,
            position: trade.position === 'long' ? 'belowBar' : 'aboveBar',
            color: trade.position === 'long' ? '#26a69a' : '#ef5350',
            shape: trade.action === 'OPEN' ? 'arrowUp' : 'arrowDown',
            text: `#${trade.id} ${trade.action} ${trade.position.toUpperCase()} @ ${trade.price}`,
        }));

        candlestickSeries.setMarkers(markers);

        return () => {
            chart.remove();
        };
    }, [chartData, trades]);


    const startStrategy = async () => {
        setMessages([]);
        try {
            const config = {
                'strategyId': 'SimplePrintStrategy',
                'subscriptions': ['BAR', 'TRADE'],
                'initialCash': '10000',
                'barSeriesSize': 10000,
            };
            setStrategyId('SimplePrintStrategy');

            socketRef.current = client.connectWebSocket('SimplePrintStrategy', handleWebSocketMessage);
            console.log('Waiting for 2 seconds before starting strategy...');
            setTimeout(async () => {
                await client.startStrategy(config);
                setIsRunning(true);
            }, 2000);
        } catch (error) {
            console.error('Failed to start strategy:', error);
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

    const updateTradingViewChart = (data) => {
        console.log('Updating TradingView chart with:', data);
        if (data.type === 'BAR') {
            const bar = data.bar;
            setChartData(prevData => [...prevData, {
                time: bar.dateTime / 1000, // Convert to seconds
                open: bar.open.value,
                high: bar.high.value,
                low: bar.low.value,
                close: bar.close.value,
            }]);
        } else if (data.type === 'TRADE') {
            const trade = data.trade;
            setTrades(prevTrades => [...prevTrades, {
                id: tradeCounter,
                time: trade.openTime / 1000, // Convert to seconds
                position: trade.long ? 'long' : 'short',
                price: trade.entryPrice.value,
                action: data.action,
            }]);
            setTradeCounter(prevCounter => prevCounter + 1);
        }
    };

    return (
        <div>
            <h1>Strategy Chart</h1>
            {!isRunning ? (
                <button onClick={startStrategy}>Start Strategy</button>
            ) : (
                <button onClick={stopStrategy}>Stop Strategy</button>
            )}
            {strategyId && <p>Strategy ID: {strategyId}</p>}
            <div style={{ width: '100%' }} ref={chartContainerRef} />
            {/*<div>*/}
            {/*    <h2>Messages:</h2>*/}
            {/*    <ul>*/}
            {/*        {messages.map((msg, index) => (*/}
            {/*            <li key={index}>{JSON.stringify(msg)}</li>*/}
            {/*        ))}*/}
            {/*    </ul>*/}
            {/*</div>*/}
        </div>
    );
};

export default StrategyChart;