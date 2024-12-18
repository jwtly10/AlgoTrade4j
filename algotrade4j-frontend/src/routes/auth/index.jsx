import { route as auth0Route } from './auth0';
import { route as cognitoRoute } from './cognito';
import { route as customRoute } from './custom';
import { route as firebaseRoute } from './firebase';
import { route as supabaseRoute } from './supabase';

export const route = {
  path: '',
  children: [auth0Route, cognitoRoute, customRoute, firebaseRoute, supabaseRoute],
};
