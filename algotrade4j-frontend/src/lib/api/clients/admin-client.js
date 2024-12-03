import { API_CONFIG } from '@/lib/api/config';
import { createApiClient } from '@/lib/api/client-factory';

const apiClient = createApiClient(API_CONFIG.MAIN);

export const adminClient = {
  createUser: (userData) =>
    apiClient.post('/admin/users', userData),

  getUsers: () =>
    apiClient.get('/admin/users'),

  getUserDetails: (userId) =>
    apiClient.get(`/admin/user-details/${userId}`),

  updateUser: (userId, userData) =>
    apiClient.put(`/admin/users/${userId}`, userData),

  changeUserPassword: (userId, newPassword) =>
    apiClient.post(`/admin/users/${userId}/change-password`, { newPassword }),

  deleteUser: (userId) =>
    apiClient.delete(`/admin/users/${userId}`),

  getRoles: () =>
    apiClient.get('/admin/roles'),

  getTrackingForUser: (userId) =>
    apiClient.get(`/admin/tracking/${userId}`),

  getLoginLogsForUser: (userId) =>
    apiClient.get(`/admin/login-logs/${userId}`),
};
