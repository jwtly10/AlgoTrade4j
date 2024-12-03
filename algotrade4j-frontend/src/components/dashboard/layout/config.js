import { paths } from '@/paths';

export const layoutConfig = {
  navItems: [
    {
      key: 'dashboards',
      title: 'Dashboards',
      items: [
        { key: 'overview', title: 'Overview', href: paths.dashboard.overview, icon: 'house' },
        { key: 'news', title: 'Economic Calendar', href: paths.dashboard.news, icon: 'newspaper' },
      ],
    },
    {
      key: 'Platforms',
      title: 'Platforms',
      items: [
        { key: 'trading', title: 'Live Trading', href: paths.dashboard.service.trading.list, icon: 'chart-line' },
        {
          key: 'backtesting',
          title: 'Backtesting',
          href: paths.dashboard.service.backtesting,
          icon: 'clock-counter-clockwise',
        },
        { key: 'optimisation', title: 'Optimisation', href: paths.dashboard.service.optimisation, icon: 'repeat' },
      ],
    },
    {
      key: 'admin',
      title: 'Admin',
      items: [{ key: 'user', title: 'Users', href: paths.dashboard.admin.users.list, icon: 'users' }],
    },
  ],
};
