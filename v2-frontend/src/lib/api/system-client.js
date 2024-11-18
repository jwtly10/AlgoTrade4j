import axios from 'axios';
import { handleError, handleResponse } from './utils.js';

const LIVE_API_HOST = import.meta.env.VITE_LIVE_API_HOST || 'http://localhost:8081';
const MAIN_API_HOST = import.meta.env.VITE_MAIN_API_HOST || 'http://localhost:8080';
const MT5_API_HOST = import.meta.env.VITE_MT5_API_HOST || 'https://mt5.algotrade4j.trade';

export const systemClient = {
  liveHealth: async () => {
    const url = `${LIVE_API_HOST}/health`;
    try {
      const response = await axios.get(url);
      return handleResponse(response, url);
    } catch (error) {
      return handleError(error, url);
    }
  },
  mainHealth: async () => {
    const url = `${MAIN_API_HOST}/health`;
    try {
      const response = await axios.get(url);
      return handleResponse(response, url);
    } catch (error) {
      return handleError(error, url);
    }
  },
  mt5Health: async () => {
    const url = `${MT5_API_HOST}/health`;
    try {
      const response = await axios.get(url);
      return handleResponse(response, url);
    } catch (error) {
      return handleError(error, url);
    }
  },
};
