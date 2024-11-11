'use client';

import { internalAuthClient } from '@/lib/api/auth/internal-auth-client';

class AuthClient {
  async signUp(_) {
    return { error: 'Sign up not implemented yet' };
  }

  async signInWithOAuth(_) {
    return { error: 'Social authentication not implemented yet' };
  }

  async signInWithPassword(params) {
    const { username, password } = params;

    try {
      await internalAuthClient.login(username, password);
    } catch (error) {
      return { error: error.message };
    }

    return {};
  }

  async resetPassword(_) {
    return { error: 'Password reset not implemented' };
  }

  async updatePassword(_) {
    return { error: 'Update reset not implemented' };
  }

  async getUser() {
    try {
      const user = await internalAuthClient.verifyToken();
      return {
        data: {
          id: user.id,
          avatar: '',
          email: user.email,
          name: user.firstName,
        },
      };
    } catch (error) {
      // TODO: CHeck what error was

      // Return null if no user
      return { data: null };
    }
  }

  async signOut() {
    try {
      await internalAuthClient.logout();
    } catch (error) {
      return { error: error.message };
    }

    return {};
  }
}

export const authClient = new AuthClient();
