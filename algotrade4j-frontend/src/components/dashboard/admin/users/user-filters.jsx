'use client';

import * as React from 'react';
import Button from '@mui/material/Button';
import FormControl from '@mui/material/FormControl';
import OutlinedInput from '@mui/material/OutlinedInput';
import Select from '@mui/material/Select';
import Stack from '@mui/material/Stack';
import Typography from '@mui/material/Typography';
import { useNavigate } from 'react-router-dom';

import { paths } from '@/paths';
import { FilterButton, FilterPopover, useFilterContext } from '@/components/core/filter-button';
import { Option } from '@/components/core/option';

import { useCustomersSelection } from './user-selection-context';

export function UserFilters({ filters = {}, sortDir = 'desc' }) {
  const { username, email, firstName, lastName, role } = filters;

  const navigate = useNavigate();

  const selection = useCustomersSelection();

  const updateSearchParams = React.useCallback(
    (newFilters, newSortDir) => {
      const searchParams = new URLSearchParams();

      if (newSortDir === 'asc') {
        searchParams.set('sortDir', newSortDir);
      }

      if (newFilters.role) {
        searchParams.set('role', newFilters.role);
      }

      if (newFilters.email) {
        searchParams.set('email', newFilters.email);
      }

      if (newFilters.username) {
        searchParams.set('username', newFilters.username);
      }

      if (newFilters.firstName) {
        searchParams.set('firstName', newFilters.firstName);
      }

      if (newFilters.lastName) {
        searchParams.set('lastName', newFilters.lastName);
      }

      navigate(`${paths.dashboard.admin.users}?${searchParams.toString()}`);
    },
    [navigate]
  );

  const handleClearFilters = React.useCallback(() => {
    updateSearchParams({}, sortDir);
  }, [updateSearchParams, sortDir]);

  const handleEmailChange = React.useCallback(
    (value) => {
      updateSearchParams({ ...filters, email: value }, sortDir);
    },
    [updateSearchParams, filters, sortDir]
  );

  const handleUsernameChange = React.useCallback(
    (value) => {
      updateSearchParams({ ...filters, username: value }, sortDir);
    },
    [updateSearchParams, filters, sortDir]
  );

  const handleFirstNameChange = React.useCallback(
    (value) => {
      updateSearchParams({ ...filters, firstName: value }, sortDir);
    },
    [updateSearchParams, filters, sortDir]
  );

  const handleLastNameChange = React.useCallback(
    (value) => {
      updateSearchParams({ ...filters, lastName: value }, sortDir);
    },
    [updateSearchParams, filters, sortDir]
  );

  const handleRoleChange = React.useCallback(
    (value) => {
      updateSearchParams({ ...filters, role: value }, sortDir);
    },
    [updateSearchParams, filters, sortDir]
  );

  const handleSortChange = React.useCallback(
    (event) => {
      updateSearchParams(filters, event.target.value);
    },
    [updateSearchParams, filters]
  );

  const hasFilters = username || email || firstName || lastName || role;

  return (
    <div>
      <Stack direction="row" spacing={2} sx={{ alignItems: 'center', flexWrap: 'wrap', px: 3, py: 2 }}>
        <Stack direction="row" spacing={2} sx={{ alignItems: 'center', flex: '1 1 auto', flexWrap: 'wrap' }}>
          <FilterButton
            displayValue={email}
            label="Email"
            onFilterApply={(value) => {
              handleEmailChange(value);
            }}
            onFilterDelete={() => {
              handleEmailChange();
            }}
            popover={<EmailFilterPopover />}
            value={email}
          />
          <FilterButton
            displayValue={username}
            label="Username"
            onFilterApply={(value) => {
              handleUsernameChange(value);
            }}
            onFilterDelete={() => {
              handleUsernameChange();
            }}
            popover={<UsernameFilterPopover />}
            value={username}
          />
          {hasFilters ? <Button onClick={handleClearFilters}>Clear filters</Button> : null}
        </Stack>
        {selection.selectedAny ? (
          <Stack direction="row" spacing={2} sx={{ alignItems: 'center' }}>
            <Typography color="text.secondary" variant="body2">
              {selection.selected.size} selected
            </Typography>
            <Button color="error" variant="contained">
              Delete
            </Button>
          </Stack>
        ) : null}
        <Select name="sort" onChange={handleSortChange} sx={{ maxWidth: '100%', width: '120px' }} value={sortDir}>
          <Option value="desc">Newest</Option>
          <Option value="asc">Oldest</Option>
        </Select>
      </Stack>
    </div>
  );
}

function EmailFilterPopover() {
  const { anchorEl, onApply, onClose, open, value: initialValue } = useFilterContext();
  const [value, setValue] = React.useState('');

  React.useEffect(() => {
    setValue(initialValue ?? '');
  }, [initialValue]);

  return (
    <FilterPopover anchorEl={anchorEl} onClose={onClose} open={open} title="Filter by email">
      <FormControl>
        <OutlinedInput
          onChange={(event) => {
            setValue(event.target.value);
          }}
          onKeyUp={(event) => {
            if (event.key === 'Enter') {
              onApply(value);
            }
          }}
          value={value}
        />
      </FormControl>
      <Button
        onClick={() => {
          onApply(value);
        }}
        variant="contained"
      >
        Apply
      </Button>
    </FilterPopover>
  );
}

function UsernameFilterPopover() {
  const { anchorEl, onApply, onClose, open, value: initialValue } = useFilterContext();
  const [value, setValue] = React.useState('');

  React.useEffect(() => {
    setValue(initialValue ?? '');
  }, [initialValue]);

  return (
    <FilterPopover anchorEl={anchorEl} onClose={onClose} open={open} title="Filter by username">
      <FormControl>
        <OutlinedInput
          onChange={(event) => {
            setValue(event.target.value);
          }}
          onKeyUp={(event) => {
            if (event.key === 'Enter') {
              onApply(value);
            }
          }}
          value={value}
        />
      </FormControl>
      <Button
        onClick={() => {
          onApply(value);
        }}
        variant="contained"
      >
        Apply
      </Button>
    </FilterPopover>
  );
}
