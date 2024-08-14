import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api/v1';
const WS_BASE_URL = 'ws://localhost:8080/ws/v1';

const axiosInstance = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

const handleResponse = (response) => {
    console.log('Request Response: ', response.data);
    return response.data;
};

const handleError = (error) => {
    console.error('API call failed:', error);
    throw error;
};

export const client = {
    startStrategy: async (config, strategyId) => {
        try {
            const response = await axiosInstance.post('/strategies/start?strategyId=' + strategyId, config);
            return handleResponse(response);
        } catch (error) {
            return handleError(error);
        }
    },

    stopStrategy: async (strategyId) => {
        try {
            const response = await axiosInstance.post(`/strategies/${strategyId}/stop`);
            return handleResponse(response);
        } catch (error) {
            return handleError(error);
        }
    },

    generateId: async (config) => {
        try {
            const response = await axiosInstance.post('/strategies/generate-id', config);
            return handleResponse(response);
        } catch (error) {
            return handleError(error);
        }
    },

    getParams: async (strategyId) => {
        try {
            const response = await axiosInstance.get(`/strategies/${strategyId}/params`);
            return handleResponse(response);
        } catch (error) {
            return handleError(error);
        }
    },

    getStrategies: async () => {
        try {
            const response = await axiosInstance.get('/strategies');
            return handleResponse(response);
        } catch (error) {
            return handleError(error);
        }
    },

    connectWebSocket: (strategyId, onMessage) => {
        return new Promise((resolve, reject) => {
            const socket = new WebSocket(`${WS_BASE_URL}/strategy-events`);

            socket.onopen = () => {
                socket.send(`STRATEGY:${strategyId}`);
                resolve(socket);
            };

            socket.onmessage = (event) => {
                const data = JSON.parse(event.data);
                onMessage(data);
            };

            socket.onerror = (error) => {
                console.error('WebSocket error:', error);
                reject(error);
            };

            socket.onclose = () => {
                console.log('WebSocket disconnected');
            };
        });
    },
};