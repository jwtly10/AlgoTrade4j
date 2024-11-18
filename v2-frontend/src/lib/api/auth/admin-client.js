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

export const adminClient = {
  createUser: async (userData) => {
    const url = `${V1}/admin/users`;
    try {
      const response = await mainInstance.post(url, userData);
      return handleResponse(response, url);
    } catch (error) {
      return handleError(error, url);
    }
  },

  getUsers: async () => {
    const url = `${V1}/admin/users`;
    try {
      const response = await mainInstance.get(url);
      return handleResponse(response, url);
    } catch (error) {
      return handleError(error, url);
    }
  },

  getUserDetails: async (userId) => {
    const url = `${V1}/admin/user-details/${userId}`;
    try {
      const response = await mainInstance.get(url);
      return handleResponse(response, url);
    } catch (error) {
      return handleError(error, url);
    }
  },

  updateUser: async (userId, userData) => {
    const url = `${V1}/admin/users/${userId}`;
    try {
      const response = await mainInstance.put(url, userData);
      return handleResponse(response, url);
    } catch (error) {
      return handleError(error, url);
    }
  },

  changeUserPassword: async (userId, newPassword) => {
    const url = `${V1}/admin/users/${userId}/change-password`;
    try {
      const response = await mainInstance.post(url, { newPassword });
      return handleResponse(response, url);
    } catch (error) {
      return handleError(error, url);
    }
  },

  deleteUser: async (userId) => {
    const url = `${V1}/admin/users/${userId}`;
    try {
      const response = await mainInstance.delete(url);
      return handleResponse(response, url);
    } catch (error) {
      return handleError(error, url);
    }
  },

  getRoles: async () => {
    const url = `${V1}/admin/roles`;
    try {
      const response = await mainInstance.get(url);
      return handleResponse(response, url);
    } catch (error) {
      return handleError(error, url);
    }
  },

  getTrackingForUser: async (userId) => {
    const url = `${V1}/admin/tracking/${userId}`;
    try {
      const response = await mainInstance.get(url);
      return handleResponse(response, url);
    } catch (error) {
      return handleError(error, url);
    }
  },

  getLoginLogsForUser: async (userId) => {
    const url = `${V1}/admin/login-logs/${userId}`;
    try {
      const response = await mainInstance.get(url);
      return handleResponse(response, url);
    } catch (error) {
      return handleError(error, url);
    }
  },
};
