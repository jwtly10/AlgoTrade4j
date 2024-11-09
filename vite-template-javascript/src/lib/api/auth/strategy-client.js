import axios from 'axios';

import { handleError, handleResponse } from '../utils';

const MAIN_API_HOST = import.meta.env.VITE_MAIN_API_HOST || 'http://localhost:8080';
const V1 = '/api/v1';

const mainInstance = axios.create({
  baseURL: MAIN_API_HOST,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true,
});

export const strategyClient = {
  getDefaultParamsForStrategyClass: async (strategyClass) => {
    const url = `${V1}/strategies/${strategyClass}/params`;
    try {
      const response = await mainInstance.get(url);
      return handleResponse(response, url);
    } catch (error) {
      return handleError(error, url);
    }
  },

  getStrategies: async () => {
    const url = `${V1}/strategies`;
    try {
      const response = await mainInstance.get(url);
      return handleResponse(response, url);
    } catch (error) {
      return handleError(error, url);
    }
  },

  getInstruments: async () => {
    const url = `${V1}/instruments`;
    try {
      const response = await mainInstance.get(url);
      return handleResponse(response, url);
    } catch (error) {
      return handleError(error, url);
    }
  },
};
