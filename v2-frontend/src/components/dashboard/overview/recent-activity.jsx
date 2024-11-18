import React from 'react';
import {
  Button,
  Card,
  CardActions,
  CardHeader,
  Divider,
  List,
  ListItem,
  ListItemAvatar,
  ListItemButton,
  ListItemText,
  Typography,
} from '@mui/material';
import { ArrowRight, ClockCounterClockwise } from '@phosphor-icons/react';
import { dayjs } from '@/lib/dayjs';

export function RecentActivityCard({ recentActivities }) {
  return (
    <Card>
      <CardHeader title="Recent Activity" />
      <List
        disablePadding
        sx={{
          p: 1,
          maxHeight: 280,
          overflow: 'auto',
          '& .MuiListItemButton-root': { borderRadius: 1 },
        }}
      >
        {recentActivities.map((activity, index) => (
          <ListItem disablePadding key={index}>
            <ListItemButton>
              <ListItemAvatar>
                <ClockCounterClockwise weight="bold" />
              </ListItemAvatar>
              <ListItemText
                sx={{ pr: 2 }}
                primary={
                  <Typography variant="body2" sx={{ wordBreak: 'break-word' }}>
                    {activity.description}
                  </Typography>
                }
              />
              <Typography
                color="text.secondary"
                sx={{
                  whiteSpace: 'nowrap',
                  flexShrink: 0,
                }}
                variant="caption"
              >
                {dayjs(activity.timestamp * 1000).fromNow()}
              </Typography>
            </ListItemButton>
          </ListItem>
        ))}
        {recentActivities.length === 0 && (
          <ListItem>
            <ListItemText secondary="No recent activity" />
          </ListItem>
        )}
      </List>
      <Divider />
      <CardActions>
        <Button color="secondary" endIcon={<ArrowRight weight="bold" />} size="small">
          View all activity
        </Button>
      </CardActions>
    </Card>
  );
}
