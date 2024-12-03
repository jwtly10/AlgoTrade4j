import { API_CONFIG } from '@/lib/api/config';
import { createApiClient } from '@/lib/api/client-factory';

const apiClient = createApiClient(API_CONFIG.LIVE);

export const newsClient = {
  getNews: () =>
    apiClient.get('/news/forexfactory')
}

export const liveOverviewClient = {
  getRecentActivities: () =>
    apiClient.get('/overview/activities')
};
