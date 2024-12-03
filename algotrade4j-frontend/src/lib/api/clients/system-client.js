import { API_CONFIG } from '@/lib/api/config';
import { createApiClient } from '@/lib/api/client-factory';

// We need to override as these health endpoints are not versioned, and do not support http cookies
const liveClient = createApiClient({
    host: API_CONFIG.LIVE.host,
    version: '',
  }, false);
const mainClient = createApiClient({
    host: API_CONFIG.MAIN.host,
    version: '',
  }, false);
const mt5Client = createApiClient(API_CONFIG.MT5, false)

export const systemClient = {
  liveHealth: () =>
    liveClient.get('/health'),

  mainHealth: () =>
    mainClient.get('/health'),

  mt5Health:  () =>
    mt5Client.get('/health')
};
