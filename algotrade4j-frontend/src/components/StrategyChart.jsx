import React, { useEffect, useRef, useState } from 'react';
import { client } from '../api/client';

const StrategyChart = () => {
    const [strategyId, setStrategyId] = useState(null);
    const [messages, setMessages] = useState([]);
    const socketRef = useRef(null);

    useEffect(() => {
        return () => {
            if (socketRef.current) {
                socketRef.current.close();
            }
        };
    }, []);

    const startStrategy = async () => {
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
            }, 2000);
        } catch (error) {
            console.error('Failed to start strategy:', error);
        }
    };

    const handleWebSocketMessage = (data) => {
        setMessages((prevMessages) => [...prevMessages, data]);
        updateTradingViewChart(data);
    };

    const updateTradingViewChart = (data) => {
        console.log('Updating TradingView chart with:', data);
    };

    return (
        <div>
            <h1>Strategy Chart</h1>
            <button onClick={startStrategy}>Start Strategy</button>
            {strategyId && <p>Strategy ID: {strategyId}</p>}
            <div>
                <h2>Messages:</h2>
                <ul>
                    {messages.map((msg, index) => (
                        <li key={index}>{JSON.stringify(msg)}</li>
                    ))}
                </ul>
            </div>
            {/* TradingView chart component */}
        </div>
    );
};

export default StrategyChart;