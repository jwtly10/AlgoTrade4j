import { API_CONFIG } from '@/lib/api/config';
import { createApiClient } from '@/lib/api/client-factory';
import { getWebSocketUrl, handleWSMessage } from '@/lib/api/utils.js';

const apiClient = createApiClient(API_CONFIG.LIVE);
const WS_BASE_URL = getWebSocketUrl(API_CONFIG.LIVE.host);

export const liveClient = {
  getLiveStrategies: () =>
    apiClient.get('/live/strategies'),

  getLiveStrategy: (strategyId) =>
    apiClient.get(`/live/strategies/${strategyId}`),

  createLiveStrategy: (strategy) =>
    apiClient.post('/live/strategies', strategy),

  updateLiveStrategy: (strategy) =>
    apiClient.put(`/live/strategies/${strategy.id}`, strategy),

  toggleLiveStrategy: (strategyId) =>
    apiClient.post(`/live/strategies/${strategyId}/toggle`),

  deleteLiveStrategy: (strategyId) =>
    apiClient.delete(`/live/strategies/${strategyId}`),

  closeLiveTrade: (strategyId, tradeId) =>
    apiClient.post(`/live/strategies/${strategyId}/${tradeId}/close`),

  getLogs: (strategyId) =>
    apiClient.get(`/live/strategies/${strategyId}/logs`),

  connectLiveWS: (strategyId, onMessage) => {
    return new Promise((resolve, reject) => {
      const socket = new WebSocket(`${WS_BASE_URL}/live-strategy-events`);
      handleWSMessage(socket, onMessage, strategyId, resolve, reject);
    });
  },
};
