import axios from 'axios';
import log from '../logger.js';
import {getWebSocketUrl, handleError, handleResponse, handleWSMessage} from "@/api/utils.js";

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


// Crude implementation to sign users out of these endpoints fail auth
mainInstance.interceptors.response.use(
    (response) => response,
    async (error) => {
        if (error.response && error.response.status === 403) {
            // Token has expired
            try {
                // Logout properly to clean http only cookie
                await authClient.logout();
            } catch (logoutError) {
                log.error('Error during logout:', logoutError);
            }

            // Redirect to login page, forcing page reload
            window.location.href = '/login';
        }
        return Promise.reject(error);
    }
);

export const systemClient = {
    /**
     * This is a utility method to dump the java heap from the frontend
     * it will only work when running locally hence hardcoding localhost:8080
     **/
    dumpHeap: async () => {
        const url = 'http://localhost:8080/generate-heapdump';
        try {
            const res = await axios.get(url);
            log.debug(res);
        } catch (error) {
            log.debug(error);
        }
    },

    monitor: async () => {
        const url = `${V1}/monitor`;
        try {
            const response = await mainInstance.get(url);
            return handleResponse(response, url);
        } catch (error) {
            return handleError(error, url);
        }
    },
};

export const authClient = {
    login: async (username, password) => {
        const url = `${V1}/auth/signin`;
        try {
            const response = await mainInstance.post(url, {username, password});
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
            const response = await mainInstance.post(url, {newPassword});
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
    }
};

export const apiClient = {
    startStrategy: async (config, strategyId, showChart) => {
        let runAsync = false;
        if (config.speed === 'INSTANT') {
            runAsync = true;
        }
        const url = `${V1}/strategies/start?strategyId=${strategyId}&async=${runAsync}&showChart=${showChart}`;
        try {
            const response = await mainInstance.post(url, config);
            return handleResponse(response, url);
        } catch (error) {
            return handleError(error, url);
        }
    },

    stopStrategy: async (strategyId) => {
        const url = `${V1}/strategies/${strategyId}/stop`;
        try {
            const response = await mainInstance.post(url);
            return handleResponse(response, url);
        } catch (error) {
            return handleError(error, url);
        }
    },

    generateId: async (config) => {
        const url = `${V1}/strategies/generate-id`;
        try {
            const response = await mainInstance.post(url, config);
            return handleResponse(response, url);
        } catch (error) {
            return handleError(error, url);
        }
    },

    getParams: async (strategyId) => {
        const url = `${V1}/strategies/${strategyId}/params`;
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

    connectWebSocket: (strategyId, onMessage) => {
        return new Promise((resolve, reject) => {
            const socket = new WebSocket(`${WS_BASE_URL}/strategy-events`);
            handleWSMessage(socket, onMessage, strategyId, resolve, reject);
        });
    },

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
        const url = `${V1}/optimisation/share/${taskId}/${shareWithUserId}`
        try {
            const response = await mainInstance.post(url);
            return handleResponse(response, url);
        } catch (error) {
            return handleError(error, url);
        }
    },

    deleteTask: async (taskId) => {
        const url = `${V1}/optimisation/tasks/${taskId}`
        try {
            const response = await mainInstance.delete(url);
            return handleResponse(response, url);
        } catch (error) {
            return handleError(error, url);
        }
    },

    getTaskResults: async (taskId) => {
        const url = `${V1}/optimisation/tasks/${taskId}/results`
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