'use client';

import * as React from 'react';
import {
  Box,
  Button,
  Card,
  CardContent,
  CardHeader,
  IconButton,
  List,
  ListItem,
  ListItemButton,
  ListItemText,
  Typography,
} from '@mui/material';
import { CaretDown, DotsThree, Folder } from '@phosphor-icons/react';
import { useToast } from '@/hooks/use-toast';
import { newsClient } from '@/lib/api/overview-client';

export function NewsWidget() {
  const [newsData, setNewsData] = React.useState([]);
  const [expanded, setExpanded] = React.useState(false);
  const { toast } = useToast();

  React.useEffect(() => {
    const fetchNews = async () => {
      try {
        const data = await newsClient.getNews();
        const now = new Date();
        const upcoming = data.filter((event) => new Date(event.date * 1000) > now);
        setNewsData(upcoming.slice(0, 8));
      } catch (error) {
        toast.error(`Failed to fetch news: ${error.message}`);
      }
    };
    fetchNews();
  }, [toast]);

  const NewsItem = ({ event }) => {
    const eventTime = new Date(event.date * 1000).toLocaleTimeString('en-US', {
      hour: '2-digit',
      minute: '2-digit',
      hour12: true,
      timeZone: 'UTC',
    });

    const impactColors = {
      High: '#ef4444',
      Medium: '#f97316',
      Low: '#eab308',
      Holiday: '#6b7280',
    };

    const countryIcons = {
      USD: '🇺🇸',
      EUR: '🇪🇺',
      GBP: '🇬🇧',
      JPY: '🇯🇵',
      AUD: '🇦🇺',
      CAD: '🇨🇦',
      CHF: '🇨🇭',
      CNY: '🇨🇳',
      NZD: '🇳🇿',
    };

    return (
      <ListItem disablePadding>
        <ListItemButton sx={{ px: 2, py: 1 }}>
          <Box sx={{ mr: 1, display: 'flex', alignItems: 'center' }}>
            <Folder size={16} weight="fill" color={impactColors[event.impact]} />
          </Box>
          <Typography sx={{ mr: 1, fontSize: '1rem' }}>{countryIcons[event.country]}</Typography>
          <ListItemText
            primary={
              <Typography variant="body2" noWrap>
                {event.title}
              </Typography>
            }
            secondary={eventTime}
          />
        </ListItemButton>
      </ListItem>
    );
  };

  const visibleNews = expanded ? newsData : newsData.slice(0, 4);

  return (
    <Card>
      <CardHeader
        title="Upcoming events"
        action={
          <IconButton>
            <DotsThree weight="bold" />
          </IconButton>
        }
      />
      <CardContent sx={{ p: 0 }}>
        {newsData.length > 0 ? (
          <>
            <List disablePadding>
              {visibleNews.map((event, index) => (
                <NewsItem key={`${event.date}-${index}`} event={event} />
              ))}
            </List>
            {newsData.length > 4 && (
              <Box sx={{ p: 1, textAlign: 'center' }}>
                <Button
                  size="small"
                  endIcon={
                    <CaretDown
                      style={{
                        transform: expanded ? 'rotate(180deg)' : 'none',
                        transition: 'transform 0.2s',
                      }}
                    />
                  }
                  onClick={() => setExpanded(!expanded)}
                >
                  {expanded ? 'Show Less' : `Show ${newsData.length - 4} More`}
                </Button>
              </Box>
            )}
          </>
        ) : (
          <Box sx={{ py: 6, textAlign: 'center' }}>
            <Folder size={32} weight="light" style={{ marginBottom: '8px', opacity: 0.5 }} />
            <Typography color="text.secondary">No upcoming events</Typography>
          </Box>
        )}
      </CardContent>
    </Card>
  );
}
