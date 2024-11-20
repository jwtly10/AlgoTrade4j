'use client';

import * as React from 'react';
import Avatar from '@mui/material/Avatar';
import Box from '@mui/material/Box';
import Chip from '@mui/material/Chip';
import Link from '@mui/material/Link';
import Stack from '@mui/material/Stack';
import Typography from '@mui/material/Typography';

import { paths } from '@/paths';
import { dayjs } from '@/lib/dayjs';
import { DataTable } from '@/components/core/data-table';
import { RouterLink } from '@/components/core/link';

import { useCustomersSelection } from './user-selection-context';

const columns = [
  {
    formatter: (row) => (
      <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
        <Avatar src={row.avatar} />{' '}
        <div>
          <Link
            color="inherit"
            component={RouterLink}
            href={paths.dashboard.admin.users.details(row.id)}
            sx={{ whiteSpace: 'nowrap' }}
            variant="subtitle2"
          >
            {row.username}
          </Link>
          <Typography color="text.secondary" variant="body2">
            {row.email}
          </Typography>
        </div>
      </Stack>
    ),
    name: 'Username',
    width: '250px',
  },
  {
    formatter: (row) => {
      const { label, icon } = { label: row.role, icon: null };
      return <Chip icon={icon} label={label} size="small" variant="outlined" />;
    },
    name: 'Role',
    width: '150px',
  },
  {
    formatter(row) {
      return dayjs(row.createdAt * 1000).format('MMM D, YYYY h:mm A');
    },
    name: 'Created at',
    width: '200px',
  },
  {
    formatter(row) {
      return dayjs(row.updatedAt * 1000).format('MMM D, YYYY h:mm A');
    },
    name: 'Last Updated At',
    width: '200px',
  },
];

export function UserTable({ rows }) {
  const { deselectAll, deselectOne, selectAll, selectOne, selected } = useCustomersSelection();

  return (
    <React.Fragment>
      <DataTable
        columns={columns}
        onDeselectAll={deselectAll}
        onDeselectOne={(_, row) => {
          deselectOne(row.id);
        }}
        onSelectAll={selectAll}
        onSelectOne={(_, row) => {
          selectOne(row.id);
        }}
        rows={rows}
        selectable={false}
        selected={selected}
      />
      {!rows.length ? (
        <Box sx={{ p: 3 }}>
          <Typography color="text.secondary" sx={{ textAlign: 'center' }} variant="body2">
            No customers found
          </Typography>
        </Box>
      ) : null}
    </React.Fragment>
  );
}
