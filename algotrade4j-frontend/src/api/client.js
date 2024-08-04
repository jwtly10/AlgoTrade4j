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
    startStrategy: async (config) => {
        try {
            const response = await axiosInstance.post('/strategies/start', config);
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

    connectWebSocket: (strategyId, onMessage) => {
        const socket = new WebSocket(`${WS_BASE_URL}/strategy-events`);

        socket.onopen = () => {
            console.log('WebSocket connected');
            socket.send(`STRATEGY:${strategyId}`);
        };

        socket.onmessage = (event) => {
            const data = JSON.parse(event.data);
            onMessage(data);
        };

        socket.onerror = (error) => {
            console.error('WebSocket error:', error);
        };

        socket.onclose = () => {
            console.log('WebSocket disconnected');
        };

        return socket;
    },
};