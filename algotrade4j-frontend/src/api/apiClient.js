import axios from 'axios';

const DEFAULT_API_URL = 'http://localhost:8080';

const API_BASE_URL = `${import.meta.env.VITE_API_URL || DEFAULT_API_URL}/api/v1`;
const WS_BASE_URL = `${(import.meta.env.VITE_API_URL || DEFAULT_API_URL).replace('http', 'ws')}/ws/v1`;

const axiosInstance = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
    withCredentials: true,
});

export const authClient = {
    login: async (username, password) => {
        try {
            const response = await axiosInstance.post('/auth/signin', {username, password});
            return handleResponse(response);
        } catch (error) {
            return handleError(error);
        }
    },

    signup: async (signupData) => {
        try {
            const response = await axiosInstance.post('/auth/signup', signupData);
            return handleResponse(response);
        } catch (error) {
            return handleError(error);
        }
    },

    verifyToken: async () => {
        try {
            const response = await axiosInstance.get('/auth/verify');
            return handleResponse(response);
        } catch (error) {
            return handleError(error);
        }
    },

    logout: async () => {
        try {
            const response = await axiosInstance.post('/auth/logout');
            return handleResponse(response);
        } catch (error) {
            return handleError(error);
        }
    }
};

export const adminClient = {
    createUser: async (userData) => {
        try {
            const response = await axiosInstance.post('/admin/users', userData);
            return handleResponse(response);
        } catch (error) {
            return handleError(error)
        }
    },

    getUsers: async () => {
        try {
            const response = await axiosInstance.get('/admin/users');
            return handleResponse(response);
        } catch (error) {
            return handleError(error);
        }
    },

    updateUser: async (userId, userData) => {
        try {
            const response = await axiosInstance.put(`/admin/users/${userId}`, userData);
            return handleResponse(response);
        } catch (error) {
            return handleError(error);
        }
    },

    changeUserPassword: async (userId, newPassword) => {
        try {
            const response = await axiosInstance.post(`/admin/users/${userId}/change-password`, {newPassword});
            return handleResponse(response);
        } catch (error) {
            return handleError(error);
        }
    },

    deleteUser: async (userId) => {
        try {
            const response = await axiosInstance.delete(`/admin/users/${userId}`);
            return handleResponse(response);
        } catch (error) {
            return handleError(error);
        }
    },
    getRoles: async () => {
        try {
            const response = await axiosInstance.get('/admin/roles');
            return handleResponse(response);
        } catch (error) {
            return handleError(error);
        }
    },
};
export const apiClient = {
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

    startOptimisation: async (config, id) => {
        try {
            const response = await axiosInstance.post('/optimisation/start?optimisationId=' + id, config);
            return handleResponse(response);
        } catch (error) {
            return handleError(error)
        }
    },

    getOptimisationResults: async (id) => {
        try {
            const response = await axiosInstance.get(`/optimisation/${id}/results`);
            return handleResponse(response);
        } catch (error) {
            return handleError(error);
        }
    }
};


const handleResponse = (response) => {
    console.log('Request Response: ', response.data);
    return response.data;
};

const handleError = (error) => {
    console.error('API call failed:', error);
    throw error;
};