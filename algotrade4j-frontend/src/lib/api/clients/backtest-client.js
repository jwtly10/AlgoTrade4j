import { API_CONFIG } from '@/lib/api/config';
import { createApiClient } from '@/lib/api/client-factory';

const apiClient = createApiClient(API_CONFIG.MAIN);
import { getWebSocketUrl, handleWSMessage } from '@/lib/api/utils.js';
const WS_BASE_URL = getWebSocketUrl(API_CONFIG.MAIN.host);

export const backtestClient = {
  startBacktest: (config, strategyId) =>
    // Async flag is for the backtest to run in the background, and only emit results at the end
    // Show chart is a flag for a headless backtest, where no chart data gets sent to the client
  {
    if (config.speed !== 'INSTANT') {
      // For the AT4J Platform, we always run async
      config.speed = 'INSTANT';
    }

    return apiClient.post(`/strategies/start?strategyId=${strategyId}&async=true&showChart=true`, config)
  },

  stopBacktest: (strategyId) =>
    apiClient.post(`/strategies/${strategyId}/stop`),

  generateBacktestId: (config) =>
    apiClient.post('/strategies/generate-id', config),

  connectBacktestWS: (strategyId, onMessage) => {
    return new Promise((resolve, reject) => {
      const socket = new WebSocket(`${WS_BASE_URL}/strategy-events`);
      handleWSMessage(socket, onMessage, strategyId, resolve, reject);
    });
  },
}
