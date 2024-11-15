import axios from 'axios';
import { getWebSocketUrl, handleError, handleResponse } from './utils';

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

export const newsClient = {
  getNews: async () => {
    const url = `${V1}/news/forexfactory`;
    try {
      const response = await liveInstance.get(url);
      return handleResponse(response, url);
    } catch (error) {
      return handleError(error, url);
    }
  },
};

export const liveOverviewClient = {
  getRecentActivities: async () => {
    const url = `${V1}/overview/activities`;
    try {
      const response = await liveInstance.get(url);
      return handleResponse(response, url);
    } catch (error) {
      return handleError(error, url);
    }
  },
};
