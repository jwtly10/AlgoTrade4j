import { API_CONFIG } from '@/lib/api/config';
import { createApiClient } from '@/lib/api/client-factory';

const apiClient = createApiClient(API_CONFIG.MAIN);

export const strategyClient = {
  getDefaultParamsForStrategyClass: (strategyClass) =>
    apiClient.get(`/strategies/${strategyClass}/params`),

  getStrategies: () =>
    apiClient.get('/strategies'),

  getInstruments: () =>
    apiClient.get('/instruments'),
};
