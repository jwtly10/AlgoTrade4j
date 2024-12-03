import * as React from 'react';

import {backtestClient} from '@/lib/api/clients/backtest-client';
import {logger} from '@/lib/default-logger';
import {toast} from 'react-toastify';

export const useBacktest = () => {
  const socketRef = React.useRef(null);
  const [isBacktestRunning, setIsBacktestRunning] = React.useState(false);

  // Charting state
  const [trades, setTrades] = React.useState([]);
  const [indicators, setIndicators] = React.useState({});
  const [chartData, setChartData] = React.useState([]);
  const [tradeIdMap, setTradeIdMap] = React.useState(new Map());
  const tradeCounterRef = React.useRef(1);

  // Analysis state
  const [account, setAccount] = React.useState(null);
  const [analysisData, setAnalysisData] = React.useState(null);
  const [equityHistory, setEquityHistory] = React.useState([]);

  // Log state
  const [logs, setLogs] = React.useState([]);
  const [backtestErrorMsg, setBacktestErrorMsg] = React.useState('');

  const [backtestProgress, setBacktestProgress] = React.useState(null);
  const [backtestStartTime, setBacktestStartTime] = React.useState(null);

  const [lastRunBacktestConfig, setLastRunBacktestConfig] = React.useState(null);

  React.useEffect(() => {
    const loadSavedBacktestRunData = () => {
      const localChartData = localStorage.getItem('chartData');
      const localAnalysisData = localStorage.getItem('analysisData');
      const localTradeData = localStorage.getItem('tradeData');
      const localAccountData = localStorage.getItem('accountData');
      const localIndicatorData = localStorage.getItem('indicatorData');
      const localStratConfig = localStorage.getItem('lastConfig');

      if (
        localChartData &&
        localAnalysisData &&
        localTradeData &&
        localAccountData &&
        localIndicatorData &&
        localStratConfig
      ) {
        try {
          setChartData(JSON.parse(localChartData));
          setTrades(JSON.parse(localTradeData));
          setAccount(JSON.parse(localAccountData));
          setIndicators(JSON.parse(localIndicatorData));
          setAnalysisData(JSON.parse(localAnalysisData));
          setEquityHistory(JSON.parse(localAnalysisData).equityHistory);
          setLastRunBacktestConfig(JSON.parse(localStratConfig));
          logger.debug('Loaded saved chart data');
        } catch (error) {
          toast.error(`Failed to load chart data from localstorage: ${error}`);
          logger.error('Failed to load chart data from localstorage ', error);
        }
      }
    };

    loadSavedBacktestRunData();

    return () => {
      if (socketRef.current) {
        socketRef.current.close();
      }
    };
  }, []);

  const startBacktest = async (backtestConfiguration) => {
    if (backtestConfiguration.strategyClass === '') {
      logger.error('Strategy class is required');
      return;
    }

    setIsBacktestRunning(true);
    cleanChartData();

    try {
      logger.debug('Starting strategy with config:', backtestConfiguration);
      const generatedIdForClass = await backtestClient.generateBacktestId(backtestConfiguration);
      setLastRunBacktestConfig(backtestConfiguration);
      logger.debug('Generated ID for class:', generatedIdForClass);

      socketRef.current = await backtestClient.connectBacktestWS(generatedIdForClass, handleBacktestWebSocketMessage);

      logger.debug('WebSocket connected');
      await backtestClient.startBacktest(backtestConfiguration, generatedIdForClass);
      localStorage.setItem('lastUsedSystemStrat', backtestConfiguration.strategyClass);
      localStorage.setItem('lastConfig', JSON.stringify(backtestConfiguration));
      setBacktestStartTime(Date.now());
    } catch (error) {
      toast.error(`Failed to start strategy: ${error.message}`);
      logger.error('Failed to start strategy:', error);
      setBacktestErrorMsg(`Failed to start strategy: ${error.message}`);
      setIsBacktestRunning(false);
    }
  };

  const stopBacktest = async () => {
    try {
      if (isBacktestRunning) {
        logger.debug('Stopping strategy');
        // We can stop strategy by just closing ws connection
        setIsBacktestRunning(false);
        // f (!backtestConfiguration && !isBacktestRunning && !backtestErrorMsg) {
        await cleanChartData();

        if (socketRef.current) {
          socketRef.current.close();
        }
      }
    } catch (error) {
      toast.error(`Failed to stop strategy: ${error.message}`);
      logger.error('Failed to stop strategy:', error);
    }
  };

  const handleBacktestWebSocketMessage = (data) => {
    if (data.type === 'BAR' || (data.type === 'TRADE' && (data.action === 'OPEN' || data.action === 'CLOSE'))) {
      updateTradingViewChart(data);
      // } else if (data.type === 'INDICATOR') {
      //   updateIndicator(data);
    } else if (data.type === 'ASYNC_ACCOUNT') {
      // } else if (data.type === 'ACCOUNT' || data.type === 'ASYNC_ACCOUNT') {
      updateAccount(data);
    } else if (data.type === 'STRATEGY_STOP') {
      setIsBacktestRunning(false);
    } else if (data.type === 'ANALYSIS') {
      setAnalysis(data);
      // } else if (data.type === 'TRADE' && data.action === 'UPDATE') {
      //   updateTrades(data);
    } else if (data.type === 'LOG') {
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
    } else if (data.type === 'BAR_SERIES') {
      updateTradingViewChart(data);
    } else if (data.type === 'ALL_TRADES') {
      updateTradingViewChart(data);
    } else if (data.type === 'ALL_INDICATORS') {
      setAllIndicators(data);
    } else if (data.type === 'PROGRESS') {
      logger.debug('Progress:', data);
      setBacktestProgress(data);
    } else if (data.type === 'ERROR') {
      logger.error('Backtest Error:', data);
      setBacktestErrorMsg(data.message);
      setIsBacktestRunning(false);
    } else {
      logger.debug('Some unhandled event was sent: ', data);
    }
  };

  const cleanChartData = () => {
    // Clean previous data
    setAccount(null);
    setChartData([]);
    localStorage.removeItem('chartData');
    localStorage.removeItem('tradeData');
    localStorage.removeItem('accountData');
    localStorage.removeItem('indicatorData');
    localStorage.removeItem('analysisData');
    localStorage.removeItem('lastConfig');
    setAnalysisData(null);
    setEquityHistory([]);
    setBacktestErrorMsg('');
    setTrades([]);
    setTradeIdMap(new Map());
    setLastRunBacktestConfig(null);
    tradeCounterRef.current = 1;
    setIndicators({});
    setLogs([]);
  };

  const updateTradingViewChart = React.useCallback(
    (data) => {
      if (data.type === 'BAR_SERIES') {
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

  const setAnalysis = (data) => {
    logger.debug('Analysis data:', data);
    setAnalysisData(data);
    setEquityHistory(data.equityHistory);
    saveAnalysisDataToLocalStorage(data);
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
    logger.debug('Account data:', data);
    const accountData = {
      initialBalance: data.account.initialBalance,
      balance: data.account.balance.toFixed(2),
      equity: data.account.equity.toFixed(2),
    };
    setAccount(accountData);
    saveAccountDataToLocalStorage(accountData);
  };

  const saveChartDataToLocalStorage = (data) => {
    try {
      localStorage.setItem('chartData', JSON.stringify(data));
    } catch (error) {
      toast.error(`Failed to save chart data to localstorage: ${error}`);
      logger.error('Failed to save chart data to localStorage:', error);
    }
  };

  const saveTradeDataToLocalStorage = (data) => {
    try {
      localStorage.setItem('tradeData', JSON.stringify(data));
    } catch (error) {
      toast.error(`Failed to save trade data to localstorage: ${error}`);
      logger.error('Failed to save trade data to localStorage:', error);
    }
  };

  const saveAccountDataToLocalStorage = (data) => {
    try {
      localStorage.setItem('accountData', JSON.stringify(data));
    } catch (error) {
      toast.error(`Failed to save account data to localstorage: ${error}`);
      logger.error('Failed to save account data to localStorage:', error);
    }
  };

  const saveIndicatorDataToLocalStorage = (data) => {
    try {
      localStorage.setItem('indicatorData', JSON.stringify(data));
    } catch (error) {
      toast.error(`Failed to save indicator data to localstorage: ${error}`);
      logger.error('Failed to save indicator data to localStorage:', error);
    }
  };

  const saveAnalysisDataToLocalStorage = (data) => {
    try {
      localStorage.setItem('analysisData', JSON.stringify(data));
    } catch (error) {
      toast.error(`Failed to save analysis data to localstorage: ${error}`);
      logger.error('Failed to save analysis data to localStorage:', error);
    }
  };

  return {
    isBacktestRunning,
    account,
    trades,
    indicators,
    chartData,
    analysisData,
    equityHistory,
    logs,
    backtestErrorMsg,
    backtestProgress,
    lastRunBacktestConfig,
    backtestStartTime,
    startBacktest,
    stopBacktest,
  };
};
