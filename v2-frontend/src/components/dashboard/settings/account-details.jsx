'use client';

import * as React from 'react';
import Avatar from '@mui/material/Avatar';
import Button from '@mui/material/Button';
import Card from '@mui/material/Card';
import CardActions from '@mui/material/CardActions';
import CardContent from '@mui/material/CardContent';
import CardHeader from '@mui/material/CardHeader';
import FormControl from '@mui/material/FormControl';
import FormHelperText from '@mui/material/FormHelperText';
import InputLabel from '@mui/material/InputLabel';
import OutlinedInput from '@mui/material/OutlinedInput';
import Stack from '@mui/material/Stack';
import { User as UserIcon } from '@phosphor-icons/react/dist/ssr/User';

import { useUser } from '@/hooks/use-user';

export function AccountDetails() {
  const { user } = useUser();

  return (
    <Card>
      <CardHeader
        avatar={
          <Avatar>
            <UserIcon fontSize="var(--Icon-fontSize)" />
          </Avatar>
        }
        title="Basic details"
      />
      <CardContent>
        <Stack spacing={3}>
          <Stack spacing={2}>
            <FormControl disabled>
              <InputLabel>Full name</InputLabel>
              <OutlinedInput defaultValue={user?.fullName} name={user?.fullName} />
            </FormControl>
            <FormControl disabled>
              <InputLabel>Email address</InputLabel>
              <OutlinedInput name="email" type="email" value={user?.email} />
            </FormControl>
          </Stack>
          <FormHelperText>Please contact us to change your details</FormHelperText>
        </Stack>
      </CardContent>
      <CardActions sx={{ justifyContent: 'flex-end' }}>
        <Button disabled={true} variant="contained">
          Save changes
        </Button>
      </CardActions>
    </Card>
  );
}
