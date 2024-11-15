import * as React from 'react';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Grid from '@mui/material/Grid2';
import Stack from '@mui/material/Stack';
import Typography from '@mui/material/Typography';
import { ArrowRight as ArrowRightIcon } from '@phosphor-icons/react/dist/ssr/ArrowRight';
import { Briefcase as BriefcaseIcon } from '@phosphor-icons/react/dist/ssr/Briefcase';
import { FileCode as FileCodeIcon } from '@phosphor-icons/react/dist/ssr/FileCode';
import { Info as InfoIcon } from '@phosphor-icons/react/dist/ssr/Info';
import { ListChecks as ListChecksIcon } from '@phosphor-icons/react/dist/ssr/ListChecks';
import { Plus as PlusIcon } from '@phosphor-icons/react/dist/ssr/Plus';
import { Users as UsersIcon } from '@phosphor-icons/react/dist/ssr/Users';
import { Warning as WarningIcon } from '@phosphor-icons/react/dist/ssr/Warning';
import { Helmet } from 'react-helmet-async';

import { config } from '@/config';
import { HelperWidget } from '@/components/dashboard/overview/helper-widget';
import { Summary } from '@/components/dashboard/overview/summary';
import ServiceHealth from '@/components/dashboard/overview/service-health';
import { liveOverviewClient } from '@/lib/api/overview-client';
import { RouterLink } from '@/components/core/link';

import { toast } from 'react-toastify';
import { RecentActivityCard } from '@/components/dashboard/overview/recent-activity';
import { NewsWidget } from '@/components/dashboard/overview/news-widget';
import { paths } from '@/paths';

const metadata = { title: `Overview | Dashboard | ${config.site.name}` };

export function Page() {
  const [recentActivities, setRecentActivities] = React.useState([]);

  React.useEffect(() => {
    async function fetchRecentActivities() {
      try {
        const data = await liveOverviewClient.getRecentActivities();
        setRecentActivities(data);
      } catch (error) {
        toast(`Failed to fetch recent activities: ${error.message}`);
      }
    }

    fetchRecentActivities();
  }, []);

  return (
    <React.Fragment>
      <Helmet>
        <title>{metadata.title}</title>
      </Helmet>
      <Box
        sx={{
          maxWidth: 'var(--Content-maxWidth)',
          m: 'var(--Content-margin)',
          p: 'var(--Content-padding)',
          width: 'var(--Content-width)',
        }}
      >
        <Stack spacing={4}>
          <Stack direction={{ xs: 'column', sm: 'row' }} spacing={3} sx={{ alignItems: 'flex-start' }}>
            <Box sx={{ flex: '1 1 auto' }}>
              <Typography variant="h4">Overview</Typography>
            </Box>
            <div>
              <Button startIcon={<PlusIcon />} variant="contained">
                Dashboard
              </Button>
            </div>
          </Stack>
          <Grid container spacing={4}>
            <Grid size={{ md: 4, xs: 12 }}>
              <ServiceHealth />
            </Grid>
            <Grid size={{ md: 4, xs: 12 }}>
              <NewsWidget />
            </Grid>
            <Grid size={{ md: 4, xs: 12 }}>
              <RecentActivityCard recentActivities={recentActivities} />
            </Grid>
            <Grid size={{ md: 4, xs: 12 }}>
              <Summary amount={5} diff={15} icon={ListChecksIcon} title="Live Strategies" trend="up" />
            </Grid>
            <Grid size={{ md: 4, xs: 12 }}>
              <Summary amount={240} diff={5} icon={UsersIcon} title="Todays Performance" trend="down" />
            </Grid>
            <Grid size={{ md: 4, xs: 12 }}>
              <Summary amount={21} diff={12} icon={WarningIcon} title="Open issues" trend="up" />
            </Grid>

            <Grid size={{ md: 4, xs: 12 }}>
              <HelperWidget
                action={
                  <Button
                    component={RouterLink}
                    href={paths.dashboard.service.backtesting}
                    color="secondary"
                    endIcon={<ArrowRightIcon />}
                    size="small"
                  >
                    Backtest Strategies
                  </Button>
                }
                description="Run backtests on your strategies to see how they would have performed in the past."
                icon={BriefcaseIcon}
                label="Backtest"
                title="Backtest your strategies"
              />
            </Grid>
            <Grid size={{ md: 4, xs: 12 }}>
              <HelperWidget
                action={
                  <Button
                    component={RouterLink}
                    href={paths.dashboard.service.optimisation}
                    color="secondary"
                    endIcon={<ArrowRightIcon />}
                    size="small"
                  >
                    Optimise Strategies
                  </Button>
                }
                description="Run simulations with various parameters to see how your strategies would perform."
                icon={InfoIcon}
                label="Optimisation"
                title="Optimise your strategies"
              />
            </Grid>
            <Grid size={{ md: 4, xs: 12 }}>
              <HelperWidget
                action={
                  <Button
                    component={RouterLink}
                    href={paths.dashboard.service.trading.list}
                    color="secondary"
                    endIcon={<ArrowRightIcon />}
                    size="small"
                  >
                    Live Trade
                  </Button>
                }
                description="Trade live with your backtested strategies to grow real accounts."
                icon={FileCodeIcon}
                label="Live Trade"
                title="Trade live with your strategies"
              />
            </Grid>
          </Grid>
        </Stack>
      </Box>
    </React.Fragment>
  );
}
