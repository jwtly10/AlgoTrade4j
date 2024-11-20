export const API_CONFIG = {
  MAIN: {
    host: import.meta.env.VITE_MAIN_API_HOST || 'http://localhost:8080',
    version: '/api/v1',
  },
  LIVE: {
    host: import.meta.env.VITE_LIVE_API_HOST || 'http://localhost:8081',
    version: '/api/v1',
  },
  MT5:{
    host: import.meta.env.VITE_MT5_API_HOST || 'https://mt5.algotrade4j.trade',
    version: ''
  }
};
