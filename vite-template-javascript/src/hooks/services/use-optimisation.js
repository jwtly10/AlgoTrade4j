import * as React from 'react';
import { toast } from 'react-toastify';
import { optimisationClient } from '@/lib/api/optimisation-client';
import { logger } from '@/lib/default-logger';

const POLL_INTERVAL = 5000; // 5 seconds

export const useOptimisation = () => {
  const [optimisationTasks, setOptimisationTasks] = React.useState([]);
  const [isLoading, setIsLoading] = React.useState(true);

  const fetchOptimisationTasks = React.useCallback(async () => {
    try {
      setIsLoading(true);
      const tasks = await optimisationClient.getOptimisationTasks();
      setOptimisationTasks(tasks);
    } catch (error) {
      logger.error('Failed to fetch optimisation tasks:', error);
      toast.error(`Failed to fetch optimisation tasks: ${error.message}`);
    } finally {
      setIsLoading(false);
    }
  }, []);

  React.useEffect(() => {
    fetchOptimisationTasks();
    const interval = setInterval(fetchOptimisationTasks, POLL_INTERVAL);
    return () => clearInterval(interval);
  }, [fetchOptimisationTasks]);

  return { optimisationTasks, isLoading, fetchOptimisationTasks };
};
