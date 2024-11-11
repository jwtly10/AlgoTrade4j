'use client';

import * as React from 'react';

import { authClient } from '@/lib/auth/custom/client';
import { logger } from '@/lib/default-logger';
import { toast } from 'react-toastify';

export const UserContext = React.createContext(undefined);

export function UserProvider({ children }) {
  const [state, setState] = React.useState({
    user: null,
    error: null,
    isLoading: true,
  });

  const checkSession = React.useCallback(async () => {
    try {
      const { data, error } = await authClient.getUser();

      if (error) {
        logger.error(error);
        // Session is invalid
        toast.error('Session is invalid. Please sign in again.');
        setState((prev) => ({ ...prev, user: null, error: 'Something went wrong', isLoading: false }));
        return;
      }

      setState((prev) => ({ ...prev, user: data ?? null, error: null, isLoading: false }));
    } catch (err) {
      logger.error(err);
      setState((prev) => ({ ...prev, user: null, error: 'Something went wrong', isLoading: false }));
    }
  }, []);

  React.useEffect(() => {
    checkSession().catch((err) => {
      logger.error(err);
      // noop
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps -- Expected
  }, []);

  return <UserContext.Provider value={{ ...state, checkSession }}>{children}</UserContext.Provider>;
}

export const UserConsumer = UserContext.Consumer;
