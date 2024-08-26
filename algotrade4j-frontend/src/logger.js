import log from 'loglevel';

const isDev = import.meta.env.VITE_NODE_ENV === 'dev';
const isDebugEnabled = import.meta.env.VITE_ENABLE_DEBUG_LOGS === 'true';

log.setLevel(isDev || isDebugEnabled ? 'debug' : 'info');

export default log;