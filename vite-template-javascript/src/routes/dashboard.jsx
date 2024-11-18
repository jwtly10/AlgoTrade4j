import * as React from 'react';
import { Outlet } from 'react-router-dom';
import { Layout as DashboardLayout } from '@/components/dashboard/layout/layout';
import { Layout as SettingsLayout } from '@/components/dashboard/settings/layout';

export const route = {
  path: 'dashboard',
  element: (
    <DashboardLayout>
      <Outlet />
    </DashboardLayout>
  ),
  children: [
    {
      index: true,
      lazy: async () => {
        const { Page } = await import('@/pages/dashboard/overview');
        return { Component: Page };
      },
    },
    {
      path: 'admin/users',
      children: [
        {
          index: true,
          lazy: async () => {
            const { Page } = await import('@/pages/dashboard/admin/users/list');
            return { Component: Page };
          },
        },
        {
          path: ':userId',
          lazy: async () => {
            const { Page } = await import('@/pages/dashboard/admin/users/details');
            return { Component: Page };
          },
        },
      ],
    },
    {
      path: 'service',
      children: [
        {
          path: 'backtesting',
          lazy: async () => {
            const { Page } = await import('@/pages/dashboard/service/backtesting/view');
            return { Component: Page };
          },
        },
        {
          path: 'trading',
          children: [
            {
              index: true,
              lazy: async () => {
                const { Page } = await import('@/pages/dashboard/service/trading/list');
                return { Component: Page };
              },
            },
            {
              path: ':strategyId',
              lazy: async () => {
                const { Page } = await import('@/pages/dashboard/service/trading/details');
                return { Component: Page };
              },
            },
          ],
        },
        {
          path: 'optimisation',
          lazy: async () => {
            const { Page } = await import('@/pages/dashboard/service/optimisation/view');
            return { Component: Page };
          },
        },
      ],
    },
    {
      path: 'settings',
      element: (
        <SettingsLayout>
          <Outlet />
        </SettingsLayout>
      ),
      children: [
        {
          path: 'account',
          lazy: async () => {
            const { Page } = await import('@/pages/dashboard/settings/account');
            return { Component: Page };
          },
        },
        {
          path: 'billing',
          lazy: async () => {
            const { Page } = await import('@/pages/dashboard/settings/billing');
            return { Component: Page };
          },
        },
        {
          path: 'notifications',
          lazy: async () => {
            const { Page } = await import('@/pages/dashboard/settings/notifications');
            return { Component: Page };
          },
        },
        {
          path: 'security',
          lazy: async () => {
            const { Page } = await import('@/pages/dashboard/settings/security');
            return { Component: Page };
          },
        },
        {
          path: 'team',
          lazy: async () => {
            const { Page } = await import('@/pages/dashboard/settings/team');
            return { Component: Page };
          },
        },
        {
          path: 'integrations',
          lazy: async () => {
            const { Page } = await import('@/pages/dashboard/settings/integrations');
            return { Component: Page };
          },
        },
      ],
    },

    {
      path: 'news',
      lazy: async () => {
        const { Page } = await import('@/pages/dashboard/news');
        return { Component: Page };
      },
    },
  ],
};
