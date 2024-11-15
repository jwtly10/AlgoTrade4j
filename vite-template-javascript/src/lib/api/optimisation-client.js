import axios from 'axios';
import { handleError, handleResponse } from './utils.js';

const MAIN_API_HOST = import.meta.env.VITE_MAIN_API_HOST || 'http://localhost:8080';
const V1 = '/api/v1';

const mainInstance = axios.create({
  baseURL: MAIN_API_HOST,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true,
});

export const optimisationClient = {
  queueOptimisation: async (config) => {
    const url = `${V1}/optimisation/queue`;
    try {
      const response = await mainInstance.post(url, config);
      return handleResponse(response, url);
    } catch (error) {
      return handleError(error, url);
    }
  },

  getOptimisationTasks: async () => {
    const url = `${V1}/optimisation/tasks`;
    try {
      const response = await mainInstance.get(url);
      return handleResponse(response, url);
    } catch (error) {
      return handleError(error, url);
    }
  },

  shareTask: async (taskId, shareWithUserId) => {
    const url = `${V1}/optimisation/share/${taskId}/${shareWithUserId}`;
    try {
      const response = await mainInstance.post(url);
      return handleResponse(response, url);
    } catch (error) {
      return handleError(error, url);
    }
  },

  deleteTask: async (taskId) => {
    const url = `${V1}/optimisation/tasks/${taskId}`;
    try {
      const response = await mainInstance.delete(url);
      return handleResponse(response, url);
    } catch (error) {
      return handleError(error, url);
    }
  },

  getTaskResults: async (taskId) => {
    const url = `${V1}/optimisation/tasks/${taskId}/results`;
    try {
      const response = await mainInstance.get(url);
      return handleResponse(response, url);
    } catch (error) {
      return handleError(error, url);
    }
  },
};
