'use client';
import * as React from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Button,
  MenuItem,
  Grid,
  Stack,
  Card,
  CardHeader,
  CardContent,
  Typography,
  IconButton,
  Tooltip,
} from '@mui/material';
import { Info, Spinner } from '@phosphor-icons/react';
import { brokerClient } from '@/lib/api/auth/broker-client';
import { logger } from '@/lib/default-logger';
import { toast } from 'react-toastify';

function BrokerAccountModal({ open, onClose, account = null, onSave, onDelete, isSaving = false }) {
  const isCreating = !account;
  const [timezones, setTimezones] = React.useState([]);
  const [brokerTypes, setBrokerTypes] = React.useState([]);
  const [errors, setErrors] = React.useState({});
  const [touched, setTouched] = React.useState({
    mt5Credentials: {
      server: false,
      path: false,
      password: false,
      timezone: false,
    },
  });
  const [formData, setFormData] = React.useState({
    brokerName: '',
    brokerType: '',
    brokerEnv: '',
    accountId: '',
    initialBalance: '',
    active: true,
    mt5Credentials: {
      server: '',
      path: '',
      password: '',
      timezone: '',
    },
  });

  const validateForm = () => {
    const newErrors = {};

    // Basic field validation
    if (!formData.brokerName.trim()) {
      newErrors.brokerName = 'Broker name is required';
    }

    if (!formData.brokerType) {
      newErrors.brokerType = 'Broker type is required';
    }

    if (!formData.brokerEnv) {
      newErrors.brokerEnv = 'Environment is required';
    }

    if (!formData.accountId.trim()) {
      newErrors.accountId = 'Account ID is required';
    }

    if (!formData.initialBalance) {
      newErrors.initialBalance = 'Initial balance is required';
    } else if (isNaN(formData.initialBalance) || Number(formData.initialBalance) <= 0) {
      newErrors.initialBalance = 'Initial balance must be a positive number';
    }

    // MT5 specific validation
    if (formData.brokerType.includes('MT5')) {
      if (!formData.mt5Credentials.server.trim()) {
        newErrors['mt5Credentials.server'] = 'MT5 server is required';
      }
      if (!formData.mt5Credentials.path.trim()) {
        newErrors['mt5Credentials.path'] = 'MT5 path is required';
      }
      if (isCreating && !formData.mt5Credentials.password.trim()) {
        newErrors['mt5Credentials.password'] = 'MT5 password is required';
      }
      if (!formData.mt5Credentials.timezone) {
        newErrors['mt5Credentials.timezone'] = 'Timezone is required';
      }
    }

    return newErrors;
  };

  React.useEffect(() => {
    // Clear on load
    setFormData({
      brokerName: '',
      brokerType: '',
      brokerEnv: '',
      accountId: '',
      initialBalance: '',
      active: true,
      mt5Credentials: {
        server: '',
        path: '',
        password: '',
        timezone: '',
      },
    });
    setErrors({});
    setTouched({});

    if (account) {
      setFormData({
        ...account,
        mt5Credentials: account.mt5Credentials || {
          server: '',
          path: '',
          password: '',
          timezone: '',
        },
      });
    }

    const fetchData = async () => {
      try {
        const [brokers, zones] = await Promise.all([brokerClient.getBrokerEnum(), brokerClient.getTimezones()]);
        setBrokerTypes(brokers);
        setTimezones(zones);
      } catch (error) {
        toast.error(`Error fetching broker types and timezones: ${error.message}`);
        logger.error('Error fetching broker types and timezones', error);
      }
    };

    fetchData();
  }, [account, open]);

  const handleChange = (field) => (event) => {
    setFormData((prev) => ({
      ...prev,
      [field]: event.target.value,
    }));

    // Clear error for this field when user starts typing
    if (errors[field]) {
      setErrors((prev) => ({
        ...prev,
        [field]: undefined,
      }));
    }
  };

  const handleMT5Change = (field) => (event) => {
    setFormData((prev) => ({
      ...prev,
      mt5Credentials: {
        ...prev.mt5Credentials,
        [field]: event.target.value,
      },
    }));

    // Clear error for this MT5 field when user starts typing
    if (errors[`mt5Credentials.${field}`]) {
      setErrors((prev) => ({
        ...prev,
        [`mt5Credentials.${field}`]: undefined,
      }));
    }
  };

  const handleBlur = (field) => () => {
    if (field.includes('mt5Credentials.')) {
      // For MT5 credential fields
      const nestedField = field.split('.')[1];
      setTouched((prev) => ({
        ...prev,
        mt5Credentials: {
          ...(prev.mt5Credentials || {}),
          [nestedField]: true,
        },
      }));
    } else {
      // For regular fields
      setTouched((prev) => ({
        ...prev,
        [field]: true,
      }));
    }

    const validationErrors = validateForm();
    setErrors(validationErrors);
  };

  const getCurrentTimeByZone = (zoneId) => {
    return new Date().toLocaleTimeString('en-US', { timeZone: zoneId, timeStyle: 'short' });
  };

  const handleSubmit = () => {
    const validationErrors = validateForm();
    setErrors(validationErrors);

    if (Object.keys(validationErrors).length > 0) {
      setTouched({
        brokerName: true,
        brokerType: true,
        brokerEnv: true,
        accountId: true,
        initialBalance: true,
        mt5Credentials: {
          server: true,
          path: true,
          password: true,
          timezone: true,
        },
      });
      return;
    }

    const submitData = {
      ...formData,
      mt5Credentials: formData.brokerType.includes('MT5') ? formData.mt5Credentials : null,
    };
    onSave(submitData);
  };

  return (
    <Dialog
      open={open}
      onClose={onClose}
      maxWidth="md"
      fullWidth
      PaperProps={{
        sx: {
          borderRadius: 1,
          p: 2,
        },
      }}
    >
      <DialogTitle variant="h5">{isCreating ? 'Create New Broker Account' : 'Edit Broker Account'}</DialogTitle>
      <DialogContent>
        <Stack spacing={3}>
          <Card>
            <CardHeader title="Broker Information" sx={{ pb: 1 }} titleTypographyProps={{ variant: 'h6' }} />
            <CardContent>
              <Grid container spacing={3}>
                <Grid item xs={12} md={6}>
                  <Stack spacing={0.5}>
                    <Typography color="text.secondary" variant="caption">
                      Broker Name
                    </Typography>
                    <TextField
                      fullWidth
                      size="small"
                      value={formData.brokerName}
                      onChange={handleChange('brokerName')}
                      onBlur={handleBlur('brokerName')}
                      variant="outlined"
                      placeholder="Enter broker name"
                      error={touched.brokerName && Boolean(errors.brokerName)}
                      helperText={touched.brokerName && errors.brokerName}
                    />
                  </Stack>
                </Grid>
                <Grid item xs={12} md={6}>
                  <Stack spacing={0.5}>
                    <Typography color="text.secondary" variant="caption">
                      Broker Type
                    </Typography>
                    <TextField
                      fullWidth
                      select
                      size="small"
                      value={formData.brokerType}
                      onChange={handleChange('brokerType')}
                      onBlur={handleBlur('brokerType')}
                      variant="outlined"
                      disabled={!isCreating}
                      error={touched.brokerType && Boolean(errors.brokerType)}
                      helperText={touched.brokerType && errors.brokerType}
                    >
                      {brokerTypes.map((type) => (
                        <MenuItem key={type} value={type}>
                          {type}
                        </MenuItem>
                      ))}
                    </TextField>
                  </Stack>
                </Grid>
                <Grid item xs={12} md={6}>
                  <Stack spacing={0.5}>
                    <Typography color="text.secondary" variant="caption">
                      Environment
                    </Typography>
                    <TextField
                      fullWidth
                      select
                      size="small"
                      value={formData.brokerEnv}
                      onChange={handleChange('brokerEnv')}
                      onBlur={handleBlur('brokerEnv')}
                      variant="outlined"
                      error={touched.brokerEnv && Boolean(errors.brokerEnv)}
                      helperText={touched.brokerEnv && errors.brokerEnv}
                    >
                      <MenuItem value="LIVE">LIVE</MenuItem>
                      <MenuItem value="DEMO">DEMO</MenuItem>
                    </TextField>
                  </Stack>
                </Grid>
                <Grid item xs={12} md={6}>
                  <Stack spacing={0.5}>
                    <Typography color="text.secondary" variant="caption">
                      Initial Balance
                    </Typography>
                    <TextField
                      fullWidth
                      size="small"
                      type="number"
                      value={formData.initialBalance}
                      onChange={handleChange('initialBalance')}
                      onBlur={handleBlur('initialBalance')}
                      variant="outlined"
                      placeholder="Enter initial balance"
                      error={touched.initialBalance && Boolean(errors.initialBalance)}
                      helperText={touched.initialBalance && errors.initialBalance}
                    />
                  </Stack>
                </Grid>
                <Grid item xs={12} md={6}>
                  <Stack spacing={0.5}>
                    <Typography color="text.secondary" variant="caption">
                      Account ID
                    </Typography>
                    <TextField
                      fullWidth
                      size="small"
                      value={formData.accountId}
                      onChange={handleChange('accountId')}
                      onBlur={handleBlur('accountId')}
                      variant="outlined"
                      disabled={!isCreating}
                      placeholder="Enter account ID"
                      error={touched.accountId && Boolean(errors.accountId)}
                      helperText={touched.accountId && errors.accountId}
                    />
                  </Stack>
                </Grid>
              </Grid>
            </CardContent>
          </Card>

          {formData.brokerType.includes('MT5') && (
            <Card>
              <CardHeader title="MT5 Credentials" sx={{ pb: 1 }} titleTypographyProps={{ variant: 'h6' }} />
              <CardContent>
                <Grid container spacing={3}>
                  {isCreating ? (
                    <Grid item xs={12} md={6}>
                      <Stack spacing={0.5}>
                        <Typography color="text.secondary" variant="caption">
                          MT5 Password
                        </Typography>
                        <TextField
                          fullWidth
                          size="small"
                          type="password"
                          value={formData.mt5Credentials.password}
                          onChange={handleMT5Change('password')}
                          onBlur={handleBlur('mt5Credentials.password')}
                          variant="outlined"
                          placeholder="Enter MT5 password"
                          error={touched?.mt5Credentials?.password && Boolean(errors['mt5Credentials.password'])}
                          helperText={touched?.mt5Credentials?.password && errors['mt5Credentials.password']}
                          InputProps={{
                            endAdornment: (
                              <Tooltip title="Password cannot be changed once set">
                                <IconButton size="small">
                                  <Info size={20} />
                                </IconButton>
                              </Tooltip>
                            ),
                          }}
                        />
                      </Stack>
                    </Grid>
                  ) : null}
                  <Grid item xs={12} md={6}>
                    <Stack spacing={0.5}>
                      <Typography color="text.secondary" variant="caption">
                        MT5 Server
                      </Typography>
                      <TextField
                        fullWidth
                        size="small"
                        value={formData.mt5Credentials.server}
                        onChange={handleMT5Change('server')}
                        onBlur={handleBlur('mt5Credentials.server')}
                        variant="outlined"
                        placeholder="Enter MT5 server"
                        error={touched?.mt5Credentials?.server && Boolean(errors['mt5Credentials.server'])}
                        helperText={touched?.mt5Credentials?.server && errors['mt5Credentials.server']}
                      />
                    </Stack>
                  </Grid>
                  <Grid item xs={12} md={6}>
                    <Stack spacing={0.5}>
                      <Typography color="text.secondary" variant="caption">
                        MT5 Path
                      </Typography>
                      <TextField
                        fullWidth
                        size="small"
                        value={formData.mt5Credentials.path}
                        onChange={handleMT5Change('path')}
                        onBlur={handleBlur('mt5Credentials.path')}
                        variant="outlined"
                        placeholder="Enter MT5 path"
                        error={touched?.mt5Credentials?.path && Boolean(errors['mt5Credentials.path'])}
                        helperText={touched?.mt5Credentials?.path && errors['mt5Credentials.path']}
                      />
                    </Stack>
                  </Grid>
                  <Grid item xs={12} md={6}>
                    <Stack spacing={0.5}>
                      <Typography color="text.secondary" variant="caption">
                        MT5 Timezone
                      </Typography>
                      <TextField
                        fullWidth
                        select
                        size="small"
                        value={formData.mt5Credentials.timezone}
                        onChange={handleMT5Change('timezone')}
                        onBlur={handleBlur('mt5Credentials.timezone')}
                        variant="outlined"
                        placeholder="Select timezone"
                        error={touched?.mt5Credentials?.timezone && Boolean(errors['mt5Credentials.timezone'])}
                        helperText={touched?.mt5Credentials?.timezone && errors['mt5Credentials.timezone']}
                      >
                        {timezones.map((tz) => (
                          <MenuItem key={tz.zoneId} value={tz.name}>
                            {`${tz.name} (${getCurrentTimeByZone(tz.zoneId)})`}
                          </MenuItem>
                        ))}
                      </TextField>
                    </Stack>
                  </Grid>
                </Grid>
              </CardContent>
            </Card>
          )}
        </Stack>
      </DialogContent>
      <DialogActions sx={{ pt: 3 }}>
        <Button onClick={onClose} variant="outlined" disabled={isSaving}>
          Cancel
        </Button>
        <Button
          onClick={handleSubmit}
          variant="contained"
          disabled={isSaving}
          startIcon={isSaving ? <Spinner weight="bold" className="animate-spin" /> : null}
        >
          {isSaving ? 'Saving...' : isCreating ? 'Create Account' : 'Save Changes'}
        </Button>
        {!isCreating && (
          <Button onClick={() => onDelete(formData.accountId)} variant="contained" color="error" disabled={isSaving}>
            Delete Account
          </Button>
        )}
      </DialogActions>
    </Dialog>
  );
}

export default BrokerAccountModal;
