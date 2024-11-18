export const paths = {
  home: '/dashboard',
  auth: {
    custom: {
      signIn: '/sign-in',
      signUp: '/sign-up',
      resetPassword: '/reset-password',
    },
  },
  dashboard: {
    overview: '/dashboard',
    admin: {
      users: {
        list: '/dashboard/admin/users',
        create: '/dashboard/admin/users/create',
        details: (userId) => `/dashboard/admin/users/${userId}`,
      },
    },
    service: {
      backtesting: '/dashboard/service/backtesting',
      trading: {
        list: '/dashboard/service/trading',
        details: (strategyId) => `/dashboard/service/trading/${strategyId}`,
      },
      optimisation: '/dashboard/service/optimisation',
    },
    settings: {
      account: '/dashboard/settings/account',
    },
    news: '/dashboard/news',
  },
  pdf: { invoice: (invoiceId) => `/pdf/invoices/${invoiceId}` },
  notAuthorized: '/errors/not-authorized',
  notFound: '/errors/not-found',
  internalServerError: '/errors/internal-server-error',
};
