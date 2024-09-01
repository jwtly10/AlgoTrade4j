import axios from 'axios';
import log from '../logger.js';

import pako from 'pako';

// In prod we are deployed on the same host.
// Locally we run apps seperately on same host
const isDev = import.meta.env.MODE === 'development';
const API_BASE_URL = isDev ? 'http://localhost:8080/api/v1' : '/api/v1';
const WS_BASE_URL = isDev
    ? `${'http://localhost:8080'.replace('http', 'ws')}/ws/v1`
    : `${window.location.protocol === 'https:' ? 'wss:' : 'ws:'}//${window.location.host}/ws/v1`;

const axiosInstance = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
    withCredentials: true,
});

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
        const url = '/system/monitor';
        try {
            const response = await axiosInstance.get(url);
            return handleResponse(response, url);
        } catch (error) {
            return handleError(error, url);
        }
    },
    version: async () => {
        const url = '/system/version';
        try {
            const response = await axiosInstance.get(url);
            return handleResponse(response, url);
        } catch (error) {
            return handleError(error, url);
        }
    },
};

export const authClient = {
    login: async (username, password) => {
        const url = '/auth/signin';
        try {
            const response = await axiosInstance.post(url, {username, password});
            return handleResponse(response, url);
        } catch (error) {
            return handleError(error, url);
        }
    },

    signup: async (signupData) => {
        const url = '/auth/signup';
        try {
            const response = await axiosInstance.post(url, signupData);
            return handleResponse(response, url);
        } catch (error) {
            return handleError(error, url);
        }
    },

    verifyToken: async () => {
        const url = '/auth/verify';
        try {
            const response = await axiosInstance.get(url);
            return handleResponse(response, url);
        } catch (error) {
            return handleError(error, url);
        }
    },

    logout: async () => {
        const url = '/auth/logout';
        try {
            const response = await axiosInstance.post(url);
            return handleResponse(response, url);
        } catch (error) {
            return handleError(error, url);
        }
    },
};

export const adminClient = {
    createUser: async (userData) => {
        const url = '/admin/users';
        try {
            const response = await axiosInstance.post(url, userData);
            return handleResponse(response, url);
        } catch (error) {
            return handleError(error, url);
        }
    },

    getUsers: async () => {
        const url = '/admin/users';
        try {
            const response = await axiosInstance.get(url);
            return handleResponse(response, url);
        } catch (error) {
            return handleError(error, url);
        }
    },

    updateUser: async (userId, userData) => {
        const url = `/admin/users/${userId}`;
        try {
            const response = await axiosInstance.put(url, userData);
            return handleResponse(response, url);
        } catch (error) {
            return handleError(error, url);
        }
    },

    changeUserPassword: async (userId, newPassword) => {
        const url = `/admin/users/${userId}/change-password`;
        try {
            const response = await axiosInstance.post(url, {newPassword});
            return handleResponse(response, url);
        } catch (error) {
            return handleError(error, url);
        }
    },

    deleteUser: async (userId) => {
        const url = `/admin/users/${userId}`;
        try {
            const response = await axiosInstance.delete(url);
            return handleResponse(response, url);
        } catch (error) {
            return handleError(error, url);
        }
    },

    getRoles: async () => {
        const url = '/admin/roles';
        try {
            const response = await axiosInstance.get(url);
            return handleResponse(response, url);
        } catch (error) {
            return handleError(error, url);
        }
    },
};

export const apiClient = {
    startStrategy: async (config, strategyId, showChart) => {
        let runAsync = false;
        if (config.speed === 'INSTANT') {
            runAsync = true;
        }
        const url = `/strategies/start?strategyId=${strategyId}&async=${runAsync}&showChart=${showChart}`;
        try {
            const response = await axiosInstance.post(url, config);
            return handleResponse(response, url);
        } catch (error) {
            return handleError(error, url);
        }
    },

    stopStrategy: async (strategyId) => {
        const url = `/strategies/${strategyId}/stop`;
        try {
            const response = await axiosInstance.post(url);
            return handleResponse(response, url);
        } catch (error) {
            return handleError(error, url);
        }
    },

    generateId: async (config) => {
        const url = '/strategies/generate-id';
        try {
            const response = await axiosInstance.post(url, config);
            return handleResponse(response, url);
        } catch (error) {
            return handleError(error, url);
        }
    },

    getParams: async (strategyId) => {
        const url = `/strategies/${strategyId}/params`;
        try {
            const response = await axiosInstance.get(url);
            return handleResponse(response, url);
        } catch (error) {
            return handleError(error, url);
        }
    },

    getStrategies: async () => {
        const url = '/strategies';
        try {
            const response = await axiosInstance.get(url);
            return handleResponse(response, url);
        } catch (error) {
            return handleError(error, url);
        }
    },

    connectWebSocket: (strategyId, onMessage) => {
        return new Promise((resolve, reject) => {
            const socket = new WebSocket(`${WS_BASE_URL}/strategy-events`);
            socket.binaryType = 'arraybuffer';

            socket.onopen = () => {
                socket.send(`STRATEGY:${strategyId}`);
                resolve(socket);
            };

            socket.onmessage = (event) => {
                try {
                    const buffer = new Uint8Array(event.data);
                    const isCompressed = buffer[0] === 1;
                    const messageData = buffer.slice(1);

                    let jsonData;
                    if (isCompressed) {
                        const decompressedData = pako.inflate(messageData, {to: 'string'});
                        jsonData = JSON.parse(decompressedData);
                    } else {
                        jsonData = JSON.parse(new TextDecoder().decode(messageData));
                    }
                    onMessage(jsonData);
                } catch (error) {
                    log.error('Error processing WebSocket message:', error);
                }
            };

            socket.onerror = (error) => {
                log.error('WebSocket error:', error);
                reject(error);
            };

            socket.onclose = () => {
                log.debug('WebSocket disconnected');
            };
        });
    },

    queueOptimisation: async (config) => {
        const url = `/optimisation/queue`;
        try {
            const response = await axiosInstance.post(url, config);
            return handleResponse(response, url);
        } catch (error) {
            return handleError(error, url);
        }
    },

    getOptimisationTasks: async () => {
        const url = `/optimisation/tasks`;
        try {
            const response = await axiosInstance.get(url);
            return handleResponse(response, url);
        } catch (error) {
            return handleError(error, url);
        }
    },

    shareTask: async (taskId, shareWithUserId) => {
        const url = `/optimisation/share/${taskId}/${shareWithUserId}`
        try {
            const response = await axiosInstance.post(url);
            return handleResponse(response, url);
        } catch (error) {
            return handleError(error, url);
        }
    },

    deleteTask: async (taskId) => {
        const url = `/optimisation/tasks/${taskId}`
        try {
            const response = await axiosInstance.delete(url);
            return handleResponse(response, url);
        } catch (error) {
            return handleError(error, url);
        }
    },

    getTaskResults: async (taskId) => {
        const url = `/optimisation/tasks/${taskId}/results`
        try {
            const response = await axiosInstance.get(url);
            return handleResponse(response, url);
        } catch (error) {
            return handleError(error, url);
        }
    },

    getInstruments: async () => {
        const url = `/instruments`;
        try {
            const response = await axiosInstance.get(url);
            return handleResponse(response, url);
        } catch (error) {
            return handleError(error, url);
        }
    },
};

const handleResponse = (response, url) => {
    log.debug(`Request Response (${url}): `, response.data);
    return response.data;
};

const handleError = (error, url) => {
    log.error(`API call failed (${url}): `, error);
    throw error;
};