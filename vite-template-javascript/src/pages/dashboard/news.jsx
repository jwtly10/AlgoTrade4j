'use client';

import * as React from 'react';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Card from '@mui/material/Card';
import CardHeader from '@mui/material/CardHeader';
import CardContent from '@mui/material/CardContent';
import Collapse from '@mui/material/Collapse';
import Divider from '@mui/material/Divider';
import IconButton from '@mui/material/IconButton';
import Stack from '@mui/material/Stack';
import Typography from '@mui/material/Typography';
import { Folder } from '@phosphor-icons/react/dist/ssr/Folder';
import { CaretDown } from '@phosphor-icons/react/dist/ssr/CaretDown';
import { useToast } from '@/hooks/use-toast';
import { newsClient } from '@/lib/api/overview-client';
import { Helmet } from 'react-helmet-async';
import { FunnelSimple, X } from '@phosphor-icons/react';
import { FormControl, InputLabel, MenuItem, Select } from '@mui/material';

const NewsItem = ({ event }) => {
  const [expanded, setExpanded] = React.useState(false);
  const [isMobile, setIsMobile] = React.useState(false);
  const isPastEvent = new Date(event.date * 1000) < new Date();

  React.useEffect(() => {
    const checkMobile = () => {
      setIsMobile(window.innerWidth < 640);
    };

    checkMobile();
    window.addEventListener('resize', checkMobile);
    return () => window.removeEventListener('resize', checkMobile);
  }, []);

  const eventTime = new Date(event.date * 1000).toLocaleTimeString('en-US', {
    hour: '2-digit',
    minute: '2-digit',
    hour12: true,
    timeZone: 'UTC',
  });

  const impactIcons = {
    High: <Folder size={20} color="red" weight="fill" title="High Impact" />,
    Medium: <Folder size={20} color="orange" weight="fill" title="Medium Impact" />,
    Low: <Folder size={20} color="#f3d20c" weight="fill" title="Low Impact" />,
    Holiday: <Folder size={20} color="gray" weight="fill" title="Holiday" />,
  };

  const countryIcons = {
    USD: '🇺🇸',
    CAD: '🇨🇦',
    JPY: '🇯🇵',
    GBP: '🇬🇧',
    CNY: '🇨🇳',
    NZD: '🇳🇿',
    AUD: '🇦🇺',
    CHF: '🇨🇭',
    EUR: '🇪🇺',
  };

  const ForecastPreviousContent = () => (
    <Box
      sx={{
        display: 'grid',
        gridTemplateColumns: '1fr 1fr',
        gap: 2,
        justifyContent: 'end',
      }}
    >
      <Box>
        <Typography variant="caption" color="text.secondary">
          Forecast
        </Typography>
        <Typography variant="body2">{event.forecast || '-'}</Typography>
      </Box>
      <Box>
        <Typography variant="caption" color="text.secondary">
          Previous
        </Typography>
        <Typography variant="body2">{event.previous || '-'}</Typography>
      </Box>
    </Box>
  );

  return (
    <Box>
      <Box
        onClick={() => isMobile && setExpanded(!expanded)}
        sx={{
          py: 2,
          px: 3,
          display: 'grid',
          gridTemplateColumns: {
            xs: '80px 40px 1fr auto',
            sm: '100px 40px 1fr 200px',
          },
          alignItems: 'center',
          gap: 2,
          cursor: isMobile ? 'pointer' : 'default',
          '&:hover': {
            bgcolor: 'action.hover',
          },
          color: isPastEvent ? 'text.disabled' : 'inherit',
        }}
      >
        <Typography color="text.secondary" variant="body2">
          {eventTime}
        </Typography>

        <Typography
          component="span"
          sx={{
            fontSize: '1.2rem',
            width: '24px',
            display: 'inline-block',
          }}
        >
          {countryIcons[event.country]}
        </Typography>

        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          {impactIcons[event.impact]}
          <Typography variant="body2" sx={{ fontSize: { xs: '0.875rem', sm: '1rem' } }}>
            {event.title}
          </Typography>
        </Box>

        {isMobile ? (
          <IconButton
            size="small"
            onClick={(e) => {
              e.stopPropagation();
              setExpanded(!expanded);
            }}
            sx={{
              transform: expanded ? 'rotate(180deg)' : 'rotate(0deg)',
              transition: 'transform 0.2s',
            }}
          >
            <CaretDown />
          </IconButton>
        ) : (
          <ForecastPreviousContent />
        )}
      </Box>

      {isMobile && (
        <Collapse in={expanded}>
          <Box sx={{ px: 3, pb: 2 }}>
            <ForecastPreviousContent />
          </Box>
        </Collapse>
      )}
    </Box>
  );
};

const DaySection = ({ date, events }) => {
  const formattedDate = new Date(date).toLocaleDateString('en-GB', {
    weekday: 'long',
    day: 'numeric',
    month: 'short',
    year: 'numeric',
  });

  return (
    <Card sx={{ mb: 2 }}>
      <CardHeader
        sx={{
          py: 2,
          bgcolor: 'grey.50',
          '& .MuiCardHeader-content': { overflow: 'hidden' },
        }}
        title={
          <Typography variant="h6" sx={{ fontSize: { xs: '1rem', sm: '1.25rem' } }}>
            {formattedDate} (UTC)
          </Typography>
        }
      />
      <Divider />
      {events.map((event, index) => (
        <React.Fragment key={`${event.date}-${index}`}>
          <NewsItem event={event} />
          {index < events.length - 1 && <Divider />}
        </React.Fragment>
      ))}
    </Card>
  );
};

const impactOptions = [
  { label: 'High', value: 'High' },
  { label: 'Medium', value: 'Medium' },
  { label: 'Low', value: 'Low' },
  { label: 'Holiday', value: 'Holiday' },
];

const countryOptions = [
  { label: 'USD', value: 'USD' },
  { label: 'EUR', value: 'EUR' },
  { label: 'GBP', value: 'GBP' },
  { label: 'JPY', value: 'JPY' },
  { label: 'AUD', value: 'AUD' },
  { label: 'CAD', value: 'CAD' },
  { label: 'CHF', value: 'CHF' },
  { label: 'CNY', value: 'CNY' },
  { label: 'NZD', value: 'NZD' },
];

const FilterSection = ({ countryFilter, impactFilter, setCountryFilter, setImpactFilter }) => {
  return (
    <Card elevation={0} sx={{ bgcolor: 'background.default' }}>
      <CardContent>
        <Stack spacing={3}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <FunnelSimple size={20} />
            <Typography variant="subtitle1" fontWeight={600}>
              Filters
            </Typography>
            {(countryFilter || impactFilter) && (
              <Button
                variant="outlined"
                color="inherit"
                size="small"
                startIcon={<X size={16} />}
                onClick={() => {
                  setCountryFilter('');
                  setImpactFilter('');
                }}
                sx={{
                  ml: 'auto',
                  borderColor: 'divider',
                  '&:hover': {
                    bgcolor: 'action.hover',
                  },
                }}
              >
                Clear All
              </Button>
            )}
          </Box>

          <Box
            sx={{
              display: 'grid',
              gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' },
              gap: 2,
              alignItems: 'start',
            }}
          >
            <FormControl fullWidth>
              <InputLabel>Country</InputLabel>
              <Select value={countryFilter} onChange={(e) => setCountryFilter(e.target.value)} label="Country">
                <MenuItem value="" disabled>
                  <em>Filter on country</em>
                </MenuItem>
                {countryOptions.map((option) => (
                  <MenuItem key={option.value} value={option.value}>
                    {option.label}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>

            <FormControl fullWidth>
              <InputLabel>Impact</InputLabel>
              <Select value={impactFilter} onChange={(e) => setImpactFilter(e.target.value)} label="Impact">
                <MenuItem value="" disabled>
                  <em>Filter on impact</em>
                </MenuItem>
                {impactOptions.map((option) => (
                  <MenuItem key={option.value} value={option.value}>
                    {option.label}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          </Box>
        </Stack>
      </CardContent>
    </Card>
  );
};

export function Page() {
  const { toast } = useToast();
  const [newsData, setNewsData] = React.useState([]);
  const [countryFilter, setCountryFilter] = React.useState('');
  const [impactFilter, setImpactFilter] = React.useState('');

  React.useEffect(() => {
    const fetchNews = async () => {
      try {
        const data = await newsClient.getNews();
        setNewsData(data);
      } catch (error) {
        toast.error(`Failed to fetch news: ${error.message}`);
        console.error('Failed to fetch news:', error);
      }
    };

    fetchNews();
  }, [toast]);

  const groupedEvents = React.useMemo(() => {
    const filtered = newsData.filter((item) => {
      const matchesCountry = !countryFilter || item.country === countryFilter;
      const matchesImpact = !impactFilter || item.impact === impactFilter;
      return matchesCountry && matchesImpact;
    });

    return filtered.reduce((acc, event) => {
      const date = new Date(event.date * 1000).toDateString();
      if (!acc[date]) {
        acc[date] = [];
      }
      acc[date].push(event);
      return acc;
    }, {});
  }, [newsData, countryFilter, impactFilter]);

  return (
    <React.Fragment>
      <Helmet>
        <title>Economic Calendar</title>
      </Helmet>
      <Box
        sx={{
          maxWidth: 'var(--Content-maxWidth)',
          m: 'var(--Content-margin)',
          p: { xs: '12px', sm: 'var(--Content-padding)' },
          width: 'var(--Content-width)',
        }}
      >
        <Stack spacing={4}>
          <Stack direction={{ xs: 'column', sm: 'row' }} spacing={3} sx={{ alignItems: 'flex-start' }}>
            <Box sx={{ flex: '1 1 auto' }}>
              <Typography variant="h4" sx={{ fontSize: { xs: '1.5rem', sm: '2.125rem' } }}>
                Economic Calendar
              </Typography>
            </Box>
          </Stack>

          <FilterSection
            countryFilter={countryFilter}
            impactFilter={impactFilter}
            setCountryFilter={setCountryFilter}
            setImpactFilter={setImpactFilter}
          />

          {Object.entries(groupedEvents).map(([date, events]) => (
            <DaySection key={date} date={date} events={events} />
          ))}
        </Stack>
      </Box>
    </React.Fragment>
  );
}
