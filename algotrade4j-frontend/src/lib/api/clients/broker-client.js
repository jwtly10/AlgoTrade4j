import { API_CONFIG } from '@/lib/api/config';
import { createApiClient } from '@/lib/api/client-factory';

const apiClient = createApiClient(API_CONFIG.LIVE);

export const brokerClient = {
  getBrokerAccounts: () =>
    apiClient.get('/accounts'),

  createBrokerAccount: (account) =>
    apiClient.post('/accounts', account),

  updateBrokerAccount: (accountId, account) =>
    apiClient.put(`/accounts/${accountId}`, account),

  deleteBrokerAccount: (accountId) =>
    apiClient.delete(`/accounts/${accountId}`),

  getBrokerEnum: () =>
    apiClient.get('/accounts/brokers'),

  getTimezones: () =>
    apiClient.get('/accounts/timezones'),
};
