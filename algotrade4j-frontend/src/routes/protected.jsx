import * as React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useUser } from '@/hooks/use-user';
import { paths } from '@/paths';

export const ProtectedRoute = ({ children, allowedRoles }) => {
  const { user } = useUser();
  const location = useLocation();

  if (!user) {
    return <Navigate to={paths.auth.custom.signIn} state={{ from: location }} replace />;
  }

  if (!allowedRoles.includes(user.role)) {
    return <Navigate to="/errors/insufficient-permissions" replace />;
  }

  return children;
};
