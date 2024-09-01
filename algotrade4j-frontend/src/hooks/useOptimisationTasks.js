import {useCallback, useEffect, useState} from 'react';
import log from '../logger.js'

const POLL_INTERVAL = 5000; // 5 seconds

export const useOptimisationTasks = (apiClient) => {
    const [optimisationTasks, setOptimisationTasks] = useState([]);
    const [isLoading, setIsLoading] = useState(true);

    const fetchOptimisationTasks = useCallback(async () => {
        try {
            setIsLoading(true);
            const tasks = await apiClient.getOptimisationTasks();
            setOptimisationTasks(tasks);
        } catch (error) {
            log.error('Failed to fetch optimisation tasks:', error);
        } finally {
            setIsLoading(false);
        }
    }, [apiClient]);

    useEffect(() => {
        fetchOptimisationTasks();
        const interval = setInterval(fetchOptimisationTasks, POLL_INTERVAL);
        return () => clearInterval(interval);
    }, [fetchOptimisationTasks]);

    return {optimisationTasks, isLoading, fetchOptimisationTasks};
};