import * as React from 'react';

import { liveClient } from '@/lib/api/auth/live-client';
import { logger } from '@/lib/default-logger';

export const useLive = () => {
  const socketRef = React.useRef(null);

  const [isConnectedToLive, setIsConnectedToLive] = React.useState(false);

  const [trades, setTrades] = React.useState([]);
  const [indicators, setIndicators] = React.useState({});
  const [chartData, setChartData] = React.useState([]);
  const [tradeIdMap, setTradeIdMap] = React.useState(new Map());
  const tradeCounterRef = React.useRef(1);

  const [analysisData, setAnalysisData] = React.useState({});

  // Log state
  const [logs, setLogs] = React.useState([]);

  // Strategy id is the STRING id of the strategy
  const viewStrategy = async (strategyId) => {
    if (socketRef.current) {
      logger.debug('Closing existing WebSocket connection before creating new one');
      try {
        socketRef.current.close(1000, 'New connection requested');
        socketRef.current = null;
      } catch (error) {
        logger.error('Error closing existing WebSocket:', error);
      }
    }
    // Clean previous data
    setChartData([]);
    setTrades([]);
    setTradeIdMap(new Map());
    tradeCounterRef.current = 1;
    setIndicators({});
    setLogs([]);
    setAnalysisData({});

    try {
      logger.debug('Viewing strategy:', strategyId);
      socketRef.current = await liveClient.connectLiveWS(strategyId, handleWebSocketMessage);
      setIsConnectedToLive(true);
      return socketRef.current;
    } catch (error) {
      logger.error('Failed to view strategy:', error);
      setIsConnectedToLive(false);
    }
  };

  const handleWebSocketMessage = (data) => {
    if (data.type === 'BAR' || (data.type === 'TRADE' && (data.action === 'OPEN' || data.action === 'CLOSE'))) {
      updateTradingViewChart(data);
    } else if (data.type === 'INDICATOR') {
      updateIndicator(data);
    } else if (data.type === 'ACCOUNT' || data.type === 'ASYNC_ACCOUNT') {
      logger.debug('Account event');
    } else if (data.type === 'STRATEGY_STOP') {
      logger.debug('Strategy stop event');
    } else if (data.type === 'LIVE_ANALYSIS') {
      logger.debug('Live analysis event', data);
      setAnalysis(data);
    } else if (data.type === 'TRADE' && data.action === 'UPDATE') {
      updateTrades(data);
    } else if (data.type === 'LOG') {
      updateLogs(data);
    } else if (data.type === 'ALL_LOGS') {
      logger.debug('All logs event');
      updateAsyncLogs(data);
    } else if (data.type === 'BAR_SERIES') {
      logger.debug('Bar series event');
      updateTradingViewChart(data);
    } else if (data.type === 'ALL_TRADES') {
      logger.debug('All trades event', data);
      updateTradingViewChart(data);
    } else if (data.type === 'ALL_INDICATORS') {
      logger.debug('All indicator event');
      setAllIndicators(data);
    } else if (data.type === 'ERROR') {
      logger.error('Error event', data);
    } else {
      logger.debug('Some unhandled event data was sent', data);
    }
  };

  const setAnalysis = (data) => {
    setAnalysisData(data);
  };

  // For regenerating the logs
  const updateAsyncLogs = (data) => {
    setLogs(
      data.logs.map((log) => {
        return {
          timestamp: new Date(log.time * 1000)
            .toLocaleString('en-GB', {
              year: 'numeric',
              month: '2-digit',
              day: '2-digit',
              hour: '2-digit',
              minute: '2-digit',
              second: '2-digit',
              hour12: false,
              timeZoneName: 'short',
              timeZone: 'Europe/London',
            })
            .replace(',', ''),
          type: log.logType,
          message: log.message,
        };
      })
    );
  };

  // For appending new logs
  const updateLogs = (data) => {
    setLogs((prevLogs) => {
      return [
        {
          timestamp: new Date(data.time * 1000)
            .toLocaleString('en-GB', {
              year: 'numeric',
              month: '2-digit',
              day: '2-digit',
              hour: '2-digit',
              minute: '2-digit',
              second: '2-digit',
              hour12: false,
              timeZoneName: 'short',
              timeZone: 'Europe/London',
            })
            .replace(',', ''),
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
    logger.debug(data);
    if (data.value !== 0) {
      // Only add non-zero values
      setIndicators((prevIndicators) => ({
        ...prevIndicators,
        [data.indicatorName]: [
          ...(prevIndicators[data.indicatorName] || []),
          { time: data.value.dateTime, value: data.value },
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

  const updateTradingViewChart = React.useCallback(
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
          }
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
              price: data.action === 'CLOSE' ? trade.closePrice.value : trade.entryPrice.value,
              profit: trade.profit,
              action: data.action,
            };
            return updatedTrades;
          }

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
              price: data.action === 'CLOSE' ? trade.closePrice.value : trade.entryPrice.value,
              profit: trade.profit,
              action: data.action,
            },
          ];
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
    isConnectedToLive,
    analysisData,
    trades,
    indicators,
    chartData,
    logs,
    socketRef,
    setIsConnectedToLive,
    viewStrategy,
  };
};
