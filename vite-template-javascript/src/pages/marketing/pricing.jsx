import * as React from 'react';
import Divider from '@mui/material/Divider';
import { Helmet } from 'react-helmet-async';

import { config } from '@/config';
import { Faqs } from '@/components/marketing/pricing/faqs';
import { PlansTable } from '@/components/marketing/pricing/plans-table';

const metadata = { title: `Pricing | ${config.site.name}` };

export function Page() {
  return (
    <React.Fragment>
      <Helmet>
        <title>{metadata.title}</title>
      </Helmet>
      <div>
        <PlansTable />
        <Divider />
        <Faqs />
      </div>
    </React.Fragment>
  );
}
