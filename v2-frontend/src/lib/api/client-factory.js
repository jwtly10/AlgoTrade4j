import axios from 'axios';
import { handleError, handleResponse } from '@/lib/api/utils.js';

export const createApiClient = (config, withCredentials=true) => {
  const instance = axios.create({
    baseURL: config.host,
    headers: {
      'Content-Type': 'application/json',
    },
    withCredentials,
  });

  return {
    get: async (endpoint) => {
      const url = `${config.version}${endpoint}`;
      try {
        const response = await instance.get(url);
        return handleResponse(response, url);
      } catch (error) {
        return handleError(error, url);
      }
    },

    post: async (endpoint, data) => {
      const url = `${config.version}${endpoint}`;
      try {
        const response = await instance.post(url, data);
        return handleResponse(response, url);
      } catch (error) {
        return handleError(error, url);
      }
    },

    put: async (endpoint, data) => {
      const url = `${config.version}${endpoint}`;
      try {
        const response = await instance.put(url, data);
        return handleResponse(response, url);
      } catch (error) {
        return handleError(error, url);
      }
    },

    delete: async (endpoint) => {
      const url = `${config.version}${endpoint}`;
      try {
        const response = await instance.delete(url);
        return handleResponse(response, url);
      } catch (error) {
        return handleError(error, url);
      }
    },
  };
};
