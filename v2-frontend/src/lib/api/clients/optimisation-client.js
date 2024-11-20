import { API_CONFIG } from '@/lib/api/config';
import { createApiClient } from '@/lib/api/client-factory';

const apiClient = createApiClient(API_CONFIG.MAIN);

export const optimisationClient = {
  queueOptimisation: (config) =>
    apiClient.post('/optimisation/queue', config),

  getOptimisationTasks: () =>
    apiClient.get('/optimisation/tasks'),

  shareTask: (taskId, shareWithUserId) =>
    apiClient.post(`/optimisation/share/${taskId}/${shareWithUserId}`),

  deleteTask: (taskId) =>
    apiClient.delete(`/optimisation/tasks/${taskId}`),

  getTaskResults: (taskId) =>
    apiClient.get(`/optimisation/tasks/${taskId}/results`),
};
