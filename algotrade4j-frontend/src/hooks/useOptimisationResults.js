import {useCallback, useEffect, useState} from 'react';
import log from '../logger.js';

const OPTIMISATION_ID_KEY = 'OPTIMISATION_ID';
const POLL_INTERVAL = 5000; // 5 seconds

export const useOptimisationResults = (apiClient) => {
    const [optimisationResults, setOptimisationResults] = useState(null);
    const [isPolling, setIsPolling] = useState(false);
    const [optimisationId, setOptimisationId] = useState(
        localStorage.getItem(OPTIMISATION_ID_KEY)
    );

    const pollOptimisationResults = useCallback(async () => {
        if (!optimisationId) return;

        try {
            const results = await apiClient.getOptimisationResults(optimisationId);
            if (Object.keys(results).length > 0) {
                setOptimisationResults(results);
                // We set the results but continue polling
                setIsPolling(false);
            } else {
                // If no results yet, continue polling
                setIsPolling(true);
            }
        } catch (error) {
            if (error.response && error.response.status === 404) {
                // Continue polling on 404
                setIsPolling(true);
            } else {
                console.error('Failed to get optimisation results:', error);
                setIsPolling(false);
            }
        }
    }, [apiClient, optimisationId]);

    useEffect(() => {
        if (optimisationId) {
            setIsPolling(true);
            pollOptimisationResults();
            const interval = setInterval(pollOptimisationResults, POLL_INTERVAL);
            return () => clearInterval(interval);
        } else {
            setIsPolling(false);
        }
    }, [optimisationId, pollOptimisationResults]);

    const startNewOptimisation = (newOptimisationId) => {
        localStorage.setItem(OPTIMISATION_ID_KEY, newOptimisationId);
        setOptimisationId(newOptimisationId);
        setIsPolling(true);
        setOptimisationResults(null);
    };

    const stopOptimisation = () => {
        localStorage.removeItem(OPTIMISATION_ID_KEY);
        setOptimisationId(null);
        setIsPolling(false);
        setOptimisationResults(null);
    };

    return {optimisationResults, isPolling, startNewOptimisation, stopOptimisation};
};
