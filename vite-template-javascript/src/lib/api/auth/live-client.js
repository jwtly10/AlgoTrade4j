import axios from 'axios';



import { getWebSocketUrl, handleError, handleResponse, handleWSMessage } from '../utils';


const LIVE_API_HOST = import.meta.env.VITE_LIVE_API_HOST || 'http://localhost:8081';
const WS_BASE_URL = getWebSocketUrl(LIVE_API_HOST);
const V1 = '/api/v1';

const liveInstance = axios.create({
  baseURL: LIVE_API_HOST,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true,
});

export const liveMonitorClient = {
  monitor: async () => {
    const url = `${V1}/monitor`;
    try {
      const response = await liveInstance.get(url);
      return handleResponse(response, url);
    } catch (error) {
      return handleError(error, url);
    }
  },
};

export const liveClient = {
  getLiveStrategies: async () => {
    const url = `${V1}/live/strategies`;
    try {
      const response = await liveInstance.get(url);
      return handleResponse(response, url);
    } catch (error) {
      return handleError(error, url);
    }
  },

  getLiveStrategy: async (strategyId) => {
    const url = `${V1}/live/strategies/${strategyId}`;
    try {
      const response = await liveInstance.get(url);
      return handleResponse(response, url);
    } catch (error) {
      return handleError(error, url);
    }
  },

  createStrategy: async (strategy) => {
    const url = `${V1}/live/strategies`;
    try {
      const response = await liveInstance.post(url, strategy);
      return handleResponse(response, url);
    } catch (error) {
      return handleError(error, url);
    }
  },

  updateStrategy: async (strategy) => {
    const url = `${V1}/live/strategies/${strategy.id}`;
    try {
      const response = await liveInstance.put(url, strategy);
      return handleResponse(response, url);
    } catch (error) {
      return handleError(error, url);
    }
  },

  toggleStrategy: async (strategyId) => {
    const url = `${V1}/live/strategies/${strategyId}/toggle`;
    try {
      const response = await liveInstance.post(url);
      return handleResponse(response, url);
    } catch (error) {
      return handleError(error, url);
    }
  },

  deleteStrategy: async (strategyId) => {
    const url = `${V1}/live/strategies/${strategyId}`;
    try {
      const response = await liveInstance.delete(url);
      return handleResponse(response, url);
    } catch (error) {
      return handleError(error, url);
    }
  },

  closeTrade: async (strategyId, tradeId) => {
    const url = `${V1}/live/strategies/${strategyId}/${tradeId}/close`;
    try {
      const response = await liveInstance.post(url);
      return handleResponse(response, url);
    } catch (error) {
      return handleError(error, url);
    }
  },

  getLogs: async (strategyId) => {
    const url = `${V1}/live/strategies/${strategyId}/logs`;
    try {
      const response = await liveInstance.get(url);
      return handleResponse(response, url);
    } catch (error) {
      return handleError(error, url);
    }
  },

  connectLiveWS: (strategyId, onMessage) => {
    return new Promise((resolve, reject) => {
      const socket = new WebSocket(`${WS_BASE_URL}/live-strategy-events`);
      handleWSMessage(socket, onMessage, strategyId, resolve, reject);
    });
  },
};

export const liveAccountClient = {
  getBrokers: async () => {
    const url = `${V1}/accounts/brokers`;
    try {
      const response = await liveInstance.get(url);
      return handleResponse(response, url);
    } catch (error) {
      return handleError(error, url);
    }
  },
  getTimezones: async () => {
    const url = `${V1}/accounts/timezones`;
    try {
      const response = await liveInstance.get(url);
      return handleResponse(response, url);
    } catch (error) {
      return handleError(error, url);
    }
  },

  getAccounts: async () => {
    const url = `${V1}/accounts`;
    try {
      const response = await liveInstance.get(url);
      return handleResponse(response, url);
    } catch (error) {
      return handleError(error, url);
    }
  },

  createBrokerAccount: async (account) => {
    const url = `${V1}/accounts`;
    try {
      const response = await liveInstance.post(url, account);
      return handleResponse(response, url);
    } catch (error) {
      return handleError(error, url);
    }
  },

  updateBrokerAccount: async (accountId, account) => {
    const url = `${V1}/accounts/${accountId}`;
    try {
      const response = await liveInstance.put(url, account);
      return handleResponse(response, url);
    } catch (error) {
      return handleError(error, url);
    }
  },

  deleteBrokerAccount: async (accountId) => {
    const url = `${V1}/accounts/${accountId}`;
    try {
      const response = await liveInstance.delete(url);
      return handleResponse(response, url);
    } catch (error) {
      return handleError(error, url);
    }
  },
};