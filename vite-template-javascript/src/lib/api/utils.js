import pako from 'pako';

import { logger } from '@/lib/default-logger';
import { toast } from 'react-toastify';

export const getWebSocketUrl = (url) => {
  const parsedUrl = new URL(url);
  const isLocalDev = parsedUrl.port === '8081';

  if (isLocalDev) {
    return `ws://${parsedUrl.host}/ws/v1`;
  }

  return `${parsedUrl.protocol === 'https:' ? 'wss:' : 'ws:'}//${parsedUrl.host}/ws/v1`;
};

export const handleResponse = (response, url) => {
  logger.debug(`Request Response (${url}): `, response.data);
  return response.data;
};

export const handleError = (error, url) => {
  logger.error(`API call failed (${url}): `, error);

  let errorMessage;

  if (error.response && error.response.data && error.response.data.message) {
    // Case 1: Nice error from the backend
    errorMessage = error.response.data.message;
  } else if (error.message) {
    // Case 2: Error with a message property
    errorMessage = error.message;
  } else {
    // Case 3: Unexpected error format
    errorMessage = 'An unexpected error occurred';
  }

  // Create a custom error object with the extracted message
  const customError = new Error(errorMessage);
  customError.originalError = error;

  throw customError;
};

export const handleWSMessage = (socket, onMessage, strategyId, resolve, reject) => {
  socket.binaryType = 'arraybuffer';

  socket.onopen = () => {
    socket.send(`STRATEGY:${strategyId}`);
    resolve(socket);
  };

  socket.onmessage = (event) => {
    try {
      const buffer = new Uint8Array(event.data);
      const isCompressed = buffer[0] === 1;
      const messageData = buffer.slice(1);

      let jsonData;
      if (isCompressed) {
        const decompressedData = pako.inflate(messageData, { to: 'string' });
        jsonData = JSON.parse(decompressedData);
      } else {
        try {
          jsonData = JSON.parse(new TextDecoder().decode(messageData));
        } catch (error) {
          // If this fails, we should try reading the raw data
          jsonData = JSON.parse(event.data);
        }
      }
      onMessage(jsonData);
    } catch (error) {
      toast.error('Error processing WebSocket message');
      logger.error('Error processing WebSocket message:', error, event.data);
    }
  };

  socket.onerror = (error) => {
    logger.error('WebSocket error:', error);
    reject(error);
  };

  socket.onclose = () => {
    logger.debug('WebSocket disconnected');
  };
};
