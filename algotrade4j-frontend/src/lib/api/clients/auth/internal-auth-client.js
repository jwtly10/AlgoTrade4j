import { API_CONFIG } from '@/lib/api/config';
import { createApiClient } from '@/lib/api/client-factory';

const apiClient = createApiClient(API_CONFIG.MAIN);

export const internalAuthClient = {
  login: (username, password) =>
    apiClient.post('/auth/signin', { username, password }),

  signup: (signupData) =>
    apiClient.post('/auth/signup', signupData),

  verifyToken: () =>
    apiClient.get('/auth/verify'),

  logout: () =>
    apiClient.post('/auth/logout'),
};
