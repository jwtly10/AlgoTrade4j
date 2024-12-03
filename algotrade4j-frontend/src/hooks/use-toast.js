// src/hooks/useToast.js
import { logger } from '@/lib/default-logger';
import { toast } from 'react-toastify';

export const useToast = () => {
  return {
    success: (message) => toast.success(message),
    error: (message, err) => {
      logger.error(err);
      toast.error(message);
    },
    warning: (message) => toast.warning(message),
    info: (message) => toast.info(message),
  };
};
