import axios from 'axios';
import { handleError, handleResponse } from '../utils';

const LIVE_API_HOST = import.meta.env.VITE_LIVE_API_HOST || 'http://localhost:8081';
const V1 = '/api/v1';

const liveInstance = axios.create({
  baseURL: LIVE_API_HOST,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true,
});

export const brokerClient = {
  getBrokerAccounts: async () => {
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

  getBrokerEnum: async () => {
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
};
