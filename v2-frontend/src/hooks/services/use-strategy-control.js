import { useEffect, useState } from 'react';



import { strategyClient } from '@/lib/api/auth/strategy-client';
import { logger } from '@/lib/default-logger';





export const useStrategyControl = () => {
  const [selectedSystemStrategyClass, setSelectedSystemStrategyClass] = useState('');
  const [systemStrategies, setSystemStrategies] = useState([]);

  const [backtestConfiguration, setBacktestConfiguration] = useState({
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
    period: 'M15',
    timeframe: {
      from: '',
      to: '',
    },
    runParams: [],
  });

  useEffect(() => {
    const setStrategiesInSystem = async () => {
      const res = await strategyClient.getStrategies();

      const lastUsedSystemStrat = localStorage.getItem('lastUsedSystemStrat');

      for (const strat of res) {
        if (strat === lastUsedSystemStrat) {
          await onSystemStrategyChange(strat);
          break;
        }
      }
      setSystemStrategies(res);
    };
    setStrategiesInSystem();
  }, []);

  const onSystemStrategyChange = async (valueOrEvent) => {
    logger.debug('onSystemStrategyChange', valueOrEvent);
    let stratClass;
    // Hack to use this function in other places
    if (typeof valueOrEvent === 'string') {
      // If a string is passed directly
      stratClass = valueOrEvent;
    } else if (valueOrEvent && valueOrEvent.target) {
      // If an event object is passed (from onChange)
      stratClass = valueOrEvent.target.value;
    } else {
      logger.error('Invalid input to handleChangeStrategy');
    }

    logger.debug('Selected Strategy Class', stratClass);
    setSelectedSystemStrategyClass(stratClass);

    setBacktestConfiguration({
      ...backtestConfiguration,
      strategyClass: stratClass,
    });

    const defaultParamsForClass = await strategyClient.getDefaultParamsForStrategyClass(stratClass);
    logger.debug(`Default params for ${stratClass}`, defaultParamsForClass);

    const runParams = [];
    defaultParamsForClass.forEach((param) => {
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
        type: param.type,
        enumValues: param.enumValues,
      });
    });

    logger.debug(`Parsed Default run params for ${stratClass}`, runParams);

    setBacktestConfiguration({
      ...backtestConfiguration,
      runParams,
    });

    // Now we have the defaults, we should load the config from local storage to overwrite anything that was saved
    loadConfigFromLocalStorage(runParams, stratClass);
  };

  const loadConfigFromLocalStorage = (runParams, stratClass) => {
    const storedConfig = JSON.parse(localStorage.getItem(`strategyConfig_${stratClass}`)) || {};

    // Check local storage for a match in the params and update the values
    const updatedRunParams = runParams.map((param) => {
      const storedParam = storedConfig.runParams?.find((p) => p.name === param.name);

      // Only update if theres data to update
      if (storedParam) {
        logger.debug(`Found Stored param for ${param.name}`, storedParam);
        return {
          ...param,
          value: storedParam.value !== undefined ? storedParam.value : param.value,
          start: storedParam.start !== undefined ? storedParam.start : param.start,
          stop: storedParam.stop !== undefined ? storedParam.stop : param.stop,
          step: storedParam.step !== undefined ? storedParam.step : param.step,
          selected: storedParam.selected !== undefined ? storedParam.selected : param.selected,
          stringList: storedParam.stringList !== undefined ? storedParam.stringList : param.stringList,
        };
      }

      logger.debug(`No Stored param for ${param.name}`);
      return param; // Keep the original parameter if no stored version is found
    });

    // Setting some defaults in case we don't have any values in local storage
    const today = new Date().toISOString().split('T')[0] + 'T00:00:00Z';
    const lastMonth = new Date(Date.now() - 86400000 * 30).toISOString().split('T')[0] + 'T00:00:00Z';

    const updatedConfig = {
      ...backtestConfiguration,
      initialCash: storedConfig.initialCash || backtestConfiguration.initialCash,
      instrumentData: storedConfig.instrumentData || backtestConfiguration.instrumentData,
      spread: storedConfig.spread || backtestConfiguration.spread,
      period: storedConfig.period || backtestConfiguration.period,
      speed: storedConfig.speed || backtestConfiguration.speed,
      timeframe: {
        from: storedConfig.timeframe?.from || lastMonth,
        to: storedConfig.timeframe?.to || today,
      },
      runParams: updatedRunParams,
      strategyClass: stratClass,
    };

    // Now we can update the state with the updated values
    setBacktestConfiguration(updatedConfig);
  };

  return {
    selectedSystemStrategyClass,
    systemStrategies,
    onSystemStrategyChange,
    backtestConfiguration,
    setBacktestConfiguration,
  };
};