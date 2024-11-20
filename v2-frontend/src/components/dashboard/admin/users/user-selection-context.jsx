'use client';

import * as React from 'react';

import { useSelection } from '@/hooks/use-selection';

function noop() {
  return undefined;
}

export const UserSelectionContext = React.createContext({
  deselectAll: noop,
  deselectOne: noop,
  selectAll: noop,
  selectOne: noop,
  selected: new Set(),
  selectedAny: false,
  selectedAll: false,
});

export function UserSelectionProvider({ children, users = [] }) {
  const userIds = React.useMemo(() => users.map((user) => user.id), [users]);
  const selection = useSelection(userIds);

  return <UserSelectionContext.Provider value={{ ...selection }}>{children}</UserSelectionContext.Provider>;
}

export function useCustomersSelection() {
  return React.useContext(UserSelectionContext);
}
