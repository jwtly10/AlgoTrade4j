import * as React from 'react';
import Avatar from '@mui/material/Avatar';
import Button from '@mui/material/Button';
import Card from '@mui/material/Card';
import CardActions from '@mui/material/CardActions';
import CardHeader from '@mui/material/CardHeader';
import List from '@mui/material/List';
import ListItem from '@mui/material/ListItem';
import ListItemAvatar from '@mui/material/ListItemAvatar';
import ListItemText from '@mui/material/ListItemText';
import Typography from '@mui/material/Typography';
import { ArrowRight as ArrowRightIcon } from '@phosphor-icons/react/dist/ssr/ArrowRight';
import { ArrowsDownUp as ArrowsDownUpIcon } from '@phosphor-icons/react/dist/ssr/ArrowsDownUp';
import { TrendDown as TrendDownIcon } from '@phosphor-icons/react/dist/ssr/TrendDown';
import { TrendUp as TrendUpIcon } from '@phosphor-icons/react/dist/ssr/TrendUp';

import { dayjs } from '@/lib/dayjs';

export function TradeList({ trades }) {
  return (
    <Card>
      <CardHeader
        avatar={
          <Avatar>
            <ArrowsDownUpIcon fontSize="var(--Icon-fontSize)" />
          </Avatar>
        }
        title="Transactions"
      />
      <List disablePadding sx={{ '& .MuiListItem-root': { py: 2 } }}>
        {trades.map((trade) => (
          <ListItem divider key={trade.id}>
            <ListItemAvatar>
              <Avatar
                sx={{
                  bgcolor: trade.type === 'add' ? 'var(--mui-palette-success-50)' : 'var(--mui-palette-error-50)',
                  color: trade.type === 'add' ? 'var(--mui-palette-success-main)' : 'var(--mui-palette-error-main)',
                }}
              >
                {trade.type === 'add' ? (
                  <TrendUpIcon fontSize="var(--Icon-fontSize)" />
                ) : (
                  <TrendDownIcon fontSize="var(--Icon-fontSize)" />
                )}
              </Avatar>
            </ListItemAvatar>
            <ListItemText
              disableTypography
              primary={<Typography variant="subtitle2">{trade.description}</Typography>}
              secondary={
                <Typography color="text.secondary" variant="body2">
                  {dayjs(trade.createdAt).format('MM.DD.YYYY / hh:mm A')}
                </Typography>
              }
            />
            <div>
              <Typography
                color={trade.type === 'add' ? 'var(--mui-palette-success-main)' : 'var(--mui-palette-error-main)'}
                sx={{ textAlign: 'right', whiteSpace: 'nowrap' }}
                variant="subtitle2"
              >
                {trade.type === 'add' ? '+' : '-'} {trade.amount} {trade.currency}
              </Typography>
              <Typography color="text.secondary" sx={{ textAlign: 'right' }} variant="body2">
                {new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(trade.balance)}
              </Typography>
            </div>
          </ListItem>
        ))}
      </List>
      <CardActions>
        <Button color="secondary" endIcon={<ArrowRightIcon />} size="small">
          See all
        </Button>
      </CardActions>
    </Card>
  );
}
