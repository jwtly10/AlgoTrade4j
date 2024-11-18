import axios from 'axios';

import { handleError, handleResponse } from '@/lib/api/utils.js';

const MAIN_API_HOST = import.meta.env.VITE_MAIN_API_HOST || 'http://localhost:8080';
const V1 = '/api/v1';

const mainInstance = axios.create({
  baseURL: MAIN_API_HOST,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true,
});

export const internalAuthClient = {
  login: async (username, password) => {
    const url = `${V1}/auth/signin`;
    try {
      const response = await mainInstance.post(url, { username, password });
      return handleResponse(response, url);
    } catch (error) {
      return handleError(error, url);
    }
  },

  signup: async (signupData) => {
    const url = `${V1}/auth/signup`;
    try {
      const response = await mainInstance.post(url, signupData);
      return handleResponse(response, url);
    } catch (error) {
      return handleError(error, url);
    }
  },

  verifyToken: async () => {
    const url = `${V1}/auth/verify`;
    try {
      const response = await mainInstance.get(url);
      return handleResponse(response, url);
    } catch (error) {
      return handleError(error, url);
    }
  },

  logout: async () => {
    const url = `${V1}/auth/logout`;
    try {
      const response = await mainInstance.post(url);
      return handleResponse(response, url);
    } catch (error) {
      return handleError(error, url);
    }
  },
};
