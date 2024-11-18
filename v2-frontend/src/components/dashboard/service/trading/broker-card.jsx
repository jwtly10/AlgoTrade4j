'use client';

import * as React from 'react';
import Avatar from '@mui/material/Avatar';
import Button from '@mui/material/Button';
import Card from '@mui/material/Card';
import CardActions from '@mui/material/CardActions';
import CardContent from '@mui/material/CardContent';
import CardHeader from '@mui/material/CardHeader';
import Chip from '@mui/material/Chip';
import Divider from '@mui/material/Divider';
import Grid from '@mui/material/Grid';
import Stack from '@mui/material/Stack';
import Typography from '@mui/material/Typography';
import { Buildings, PencilSimple } from '@phosphor-icons/react';

const formatCurrency = (value) => {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(value);
};

export default function BrokerAccountCard({ account, onEdit }) {
  return (
    <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      <CardHeader
        avatar={
          <Avatar sx={{ bgcolor: account.active ? 'success.main' : 'grey.500' }}>
            <Buildings weight="fill" />
          </Avatar>
        }
        title={
          <Typography variant="h6" component="div">
            {account.brokerName.trim() || 'Unnamed Account'}
          </Typography>
        }
        subheader={
          <Typography variant="body2" color="text.secondary">
            {account.brokerType} • {account.accountId}
          </Typography>
        }
      />
      <CardContent sx={{ flex: 1 }}>
        <Stack spacing={2}>
          <Grid container spacing={2}>
            <Grid item xs={6}>
              <Stack spacing={0.5}>
                <Typography color="text.secondary" variant="caption">
                  Initial Balance
                </Typography>
                <Typography variant="h6">{formatCurrency(account.initialBalance)}</Typography>
              </Stack>
            </Grid>
            <Grid item xs={6}>
              <Stack spacing={0.5}>
                <Typography color="text.secondary" variant="caption">
                  Environment
                </Typography>
                <Chip
                  label={account.brokerEnv.toLowerCase()}
                  color={account.brokerEnv === 'DEMO' ? 'info' : 'warning'}
                  size="small"
                  sx={{
                    width: 'fit-content',
                    textTransform: 'capitalize',
                  }}
                />
              </Stack>
            </Grid>
          </Grid>

          {account.mt5Credentials ? (
            <>
              <Divider sx={{ my: 1 }} />
              <Stack spacing={1}>
                <Typography variant="subtitle2" color="text.secondary">
                  MT5 Details
                </Typography>
                <Grid container spacing={2}>
                  <Grid item xs={6}>
                    <Stack spacing={0.5}>
                      <Typography color="text.secondary" variant="caption">
                        Server
                      </Typography>
                      <Typography variant="body2">{account.mt5Credentials.server}</Typography>
                    </Stack>
                  </Grid>
                  <Grid item xs={6}>
                    <Stack spacing={0.5}>
                      <Typography color="text.secondary" variant="caption">
                        Timezone
                      </Typography>
                      <Typography variant="body2">{account.mt5Credentials.timezone}</Typography>
                    </Stack>
                  </Grid>
                </Grid>
              </Stack>
            </>
          ) : null}
        </Stack>
      </CardContent>
      <Divider />
      <CardActions>
        <Button size="small" startIcon={<PencilSimple />} onClick={() => onEdit(account)}>
          Edit
        </Button>
      </CardActions>
    </Card>
  );
}
