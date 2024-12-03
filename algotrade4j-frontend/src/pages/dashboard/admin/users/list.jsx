'use client';

import * as React from 'react';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Card from '@mui/material/Card';
import Divider from '@mui/material/Divider';
import Stack from '@mui/material/Stack';
import Typography from '@mui/material/Typography';
import { Plus as PlusIcon } from '@phosphor-icons/react/dist/ssr/Plus';
import { Helmet } from 'react-helmet-async';
import { useSearchParams } from 'react-router-dom';



import { config } from '@/config';
import { paths } from '@/paths';
import { adminClient } from '@/lib/api/clients/admin-client';
import { UserFilters } from '@/components/dashboard/admin/users/user-filters';
import { UserPagination } from '@/components/dashboard/admin/users/user-pagination';
import { UserSelectionProvider } from '@/components/dashboard/admin/users/user-selection-context';
import { UserTable } from '@/components/dashboard/admin/users/user-table';





const metadata = { title: `List | User | Dashboard | ${config.site.name}` };

export function Page() {
  const [users, setUsers] = React.useState([]);
  React.useEffect(() => {
    async function fetchUsers() {
      const users = await adminClient.getUsers();
      setUsers(users);
    }

    fetchUsers();
  }, []);

  const { username, email, firstName, lastName, role, sortDir } = useExtractSearchParams();

  const sortedUsers = applySort(users, sortDir);
  const filteredUsers = applyFilters(sortedUsers, { username, email, firstName, lastName, role });

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
              <Typography variant="h4">Users</Typography>
            </Box>
            <Box sx={{ display: 'flex', justifyContent: 'flex-end' }}>
              <Button href={paths.dashboard.admin.users.create} startIcon={<PlusIcon />} variant="contained">
                Add
              </Button>
            </Box>
          </Stack>
          <UserSelectionProvider users={filteredUsers}>
            <Card>
              <UserFilters filters={{ username, email, firstName, lastName, role }} sortDir={sortDir} />
              <Divider />
              <Box sx={{ overflowX: 'auto' }}>
                <UserTable rows={filteredUsers} />
              </Box>
              <Divider />
              <UserPagination count={filteredUsers.length} page={0} />
            </Card>
          </UserSelectionProvider>
        </Stack>
      </Box>
    </React.Fragment>
  );
}

function useExtractSearchParams() {
  const [searchParams] = useSearchParams();

  return {
    username: searchParams.get('username') || undefined,
    email: searchParams.get('email') || undefined,
    firstName: searchParams.get('firstName') || undefined,
    lastName: searchParams.get('lastName') || undefined,
    role: searchParams.get('role') || undefined,
    sortDir: searchParams.get('sortDir') || 'asc',
  };
}

function applySort(row, sortDir) {
  return row.sort((a, b) => {
    if (sortDir === 'asc') {
      return a.createdAt - b.createdAt;
    }

    return b.createdAt - a.createdAt;
  });
}

function applyFilters(row, { username, email, firstName, lastName, role }) {
  return row.filter((item) => {
    if (email) {
      if (!item.email?.toLowerCase().includes(email.toLowerCase())) {
        return false;
      }
    }

    if (username) {
      if (!item.username?.toLowerCase().includes(username.toLowerCase())) {
        return false;
      }
    }

    if (firstName) {
      if (!item.firstName?.toLowerCase().includes(firstName.toLowerCase())) {
        return false;
      }
    }

    if (lastName) {
      if (!item.lastName?.toLowerCase().includes(lastName.toLowerCase())) {
        return false;
      }
    }

    if (role) {
      if (item.role !== role) {
        return false;
      }
    }

    return true;
  });
}
