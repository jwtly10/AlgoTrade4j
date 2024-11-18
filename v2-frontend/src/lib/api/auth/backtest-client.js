import axios from 'axios';

import { logger } from '@/lib/default-logger';

import { getWebSocketUrl, handleError, handleResponse, handleWSMessage } from '../utils';

const MAIN_API_HOST = import.meta.env.VITE_MAIN_API_HOST || 'http://localhost:8080';
const WS_BASE_URL = getWebSocketUrl(MAIN_API_HOST);
const V1 = '/api/v1';

const mainInstance = axios.create({
  baseURL: MAIN_API_HOST,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true,
});

export const backtestClient = {
  startBacktest: async (config, strategyId) => {
    // Async flag is for the backtest to run in the background, and only emit results at the end
    const runAsync = true;
    // Show chart is a flagfor a headless backtest, where no chart data gets sent to the client
    const showChart = true;

    if (config.speed !== 'INSTANT') {
      // For the AT4J Platform, we always run async
      config.speed = 'INSTANT';
    }

    const url = `${V1}/strategies/start?strategyId=${strategyId}&async=${runAsync}&showChart=${showChart}`;
    try {
      const response = await mainInstance.post(url, config);
      return handleResponse(response, url);
    } catch (error) {
      return handleError(error, url);
    }
  },

  stopBacktest: async (strategyId) => {
    const url = `${V1}/strategies/${strategyId}/stop`;
    try {
      const response = await mainInstance.post(url);
      return handleResponse(response, url);
    } catch (error) {
      return handleError(error, url);
    }
  },

  generateBacktestId: async (config) => {
    logger.debug('generateBacktestId', config);
    const url = `${V1}/strategies/generate-id`;
    try {
      const response = await mainInstance.post(url, config);
      return handleResponse(response, url);
    } catch (error) {
      return handleError(error, url);
    }
  },

  connectBacktestWS: (strategyId, onMessage) => {
    return new Promise((resolve, reject) => {
      const socket = new WebSocket(`${WS_BASE_URL}/strategy-events`);
      handleWSMessage(socket, onMessage, strategyId, resolve, reject);
    });
  },
};
