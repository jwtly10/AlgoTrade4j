'use client';
import * as React from 'react';
import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Grid,
  IconButton,
  MenuItem,
  TextField,
  Tooltip,
  Typography,
} from '@mui/material';
import { Info, X } from '@phosphor-icons/react';
import { brokerClient } from '@/lib/api/clients/broker-client';
import { logger } from '@/lib/default-logger';
import { toast } from 'react-toastify';
import FormControl from '@mui/material/FormControl';
import InputLabel from '@mui/material/InputLabel';
import Select from '@mui/material/Select';
import Box from '@mui/material/Box';
import FormHelperText from '@mui/material/FormHelperText';
import CircularProgress from '@mui/material/CircularProgress';

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
      server: 'deprecated',
      path: 'deprecated',
      password: 'deprecated',
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
    // No longer need to do this
    // if (formData.brokerType.includes('MT5')) {
    //   if (!formData.mt5Credentials.server.trim()) {
    //     newErrors['mt5Credentials.server'] = 'MT5 server is required';
    //   }
    //   if (!formData.mt5Credentials.path.trim()) {
    //     newErrors['mt5Credentials.path'] = 'MT5 path is required';
    //   }
    //   if (isCreating && !formData.mt5Credentials.password.trim()) {
    //     newErrors['mt5Credentials.password'] = 'MT5 password is required';
    //   }
    //   if (!formData.mt5Credentials.timezone) {
    //     newErrors['mt5Credentials.timezone'] = 'Timezone is required';
    //   }
    // }

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
        server: 'deprecated',
        path: 'deprecated',
        password: 'deprecated',
        timezone: '',
      },
    });
    setErrors({});
    setTouched({});

    if (account) {
      setFormData({
        ...account,
        mt5Credentials: account.mt5Credentials || {
          server: 'deprecated',
          path: 'deprecated',
          password: 'deprecated',
          timezone: 'deprecated',
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
          server: false,
          path: false,
          password: false,
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
      maxWidth="lg"
      fullWidth
      PaperProps={{
        sx: {
          height: 'auto',
          display: 'flex',
          flexDirection: 'column',
        },
      }}
    >
      <DialogTitle sx={{ m: 0, p: 2, display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <Typography>{isCreating ? 'Create New Broker Account' : 'Edit Broker Account'}</Typography>
        <IconButton onClick={onClose} size="small" aria-label="close">
          <X />
        </IconButton>
      </DialogTitle>

      <DialogContent dividers sx={{ p: 0, display: 'flex', overflow: 'hidden' }}>
        <Box sx={{ display: 'flex', flexDirection: 'column', width: '100%', height: '100%', p: 3 }}>
          <Grid container spacing={3}>
            <Grid item xs={12} md={6}>
              <FormControl fullWidth>
                <InputLabel>Broker Name</InputLabel>
                <TextField
                  fullWidth
                  value={formData.brokerName}
                  onChange={handleChange('brokerName')}
                  onBlur={handleBlur('brokerName')}
                  placeholder="Enter broker name"
                  error={touched.brokerName && Boolean(errors.brokerName)}
                  helperText={touched.brokerName && errors.brokerName}
                />
              </FormControl>
            </Grid>

            <Grid item xs={12} md={6}>
              <FormControl fullWidth>
                <InputLabel>Broker Type</InputLabel>
                <Select
                  value={formData.brokerType}
                  onChange={handleChange('brokerType')}
                  onBlur={handleBlur('brokerType')}
                  disabled={!isCreating}
                  error={touched.brokerType && Boolean(errors.brokerType)}
                  label="Broker Type"
                >
                  <MenuItem value="" disabled>
                    <em>Select a Broker</em>
                  </MenuItem>
                  {brokerTypes.map((type) => (
                    <MenuItem key={type} value={type}>
                      {type}
                    </MenuItem>
                  ))}
                </Select>
                {touched.brokerType && errors.brokerType && <FormHelperText error>{errors.brokerType}</FormHelperText>}
              </FormControl>
            </Grid>

            <Grid item xs={12} md={6}>
              <FormControl fullWidth>
                <InputLabel>Environment</InputLabel>
                <Select
                  value={formData.brokerEnv}
                  onChange={handleChange('brokerEnv')}
                  onBlur={handleBlur('brokerEnv')}
                  error={touched.brokerEnv && Boolean(errors.brokerEnv)}
                  label="Environment"
                >
                  <MenuItem value="" disabled>
                    <em>Select an Environment</em>
                  </MenuItem>
                  <MenuItem value="LIVE">LIVE</MenuItem>
                  <MenuItem value="DEMO">DEMO</MenuItem>
                </Select>
                {touched.brokerEnv && errors.brokerEnv && <FormHelperText error>{errors.brokerEnv}</FormHelperText>}
              </FormControl>
            </Grid>

            <Grid item xs={12} md={6}>
              <FormControl fullWidth>
                <InputLabel>Initial Balance</InputLabel>
                <TextField
                  type="number"
                  value={formData.initialBalance}
                  onChange={handleChange('initialBalance')}
                  onBlur={handleBlur('initialBalance')}
                  placeholder="Enter initial balance"
                  error={touched.initialBalance && Boolean(errors.initialBalance)}
                  helperText={touched.initialBalance && errors.initialBalance}
                />
              </FormControl>
            </Grid>

            <Grid item xs={12} md={6}>
              <FormControl fullWidth>
                <InputLabel>Account ID</InputLabel>
                <TextField
                  value={formData.accountId}
                  onChange={handleChange('accountId')}
                  onBlur={handleBlur('accountId')}
                  disabled={!isCreating}
                  placeholder="Enter account ID"
                  error={touched.accountId && Boolean(errors.accountId)}
                  helperText={touched.accountId && errors.accountId}
                />
              </FormControl>
            </Grid>

            {/* MT5 Credentials Section */}
            {formData.brokerType.includes('MT5') && (
              <>
                <Grid item xs={12}>
                  <Typography variant="h6" sx={{ mb: 2, mt: 2 }}>
                    MT5 Timezone data
                  </Typography>
                </Grid>

                {/*{isCreating && (*/}
                {/*  <Grid item xs={12} md={6}>*/}
                {/*    <FormControl fullWidth>*/}
                {/*      <InputLabel>MT5 Password</InputLabel>*/}
                {/*      <TextField*/}
                {/*        type="password"*/}
                {/*        value={formData.mt5Credentials.password}*/}
                {/*        onChange={handleMT5Change('password')}*/}
                {/*        onBlur={handleBlur('mt5Credentials.password')}*/}
                {/*        placeholder="Enter MT5 password"*/}
                {/*        error={touched?.mt5Credentials?.password && Boolean(errors['mt5Credentials.password'])}*/}
                {/*        helperText={touched?.mt5Credentials?.password && errors['mt5Credentials.password']}*/}
                {/*        InputProps={{*/}
                {/*          endAdornment: (*/}
                {/*            <Tooltip title="Password cannot be changed once set">*/}
                {/*              <IconButton size="small">*/}
                {/*                <Info size={20} />*/}
                {/*              </IconButton>*/}
                {/*            </Tooltip>*/}
                {/*          ),*/}
                {/*        }}*/}
                {/*      />*/}
                {/*    </FormControl>*/}
                {/*  </Grid>*/}
                {/*)}*/}

                {/*<Grid item xs={12} md={6}>*/}
                {/*  <FormControl fullWidth>*/}
                {/*    <InputLabel>MT5 Server</InputLabel>*/}
                {/*    <TextField*/}
                {/*      value={formData.mt5Credentials.server}*/}
                {/*      onChange={handleMT5Change('server')}*/}
                {/*      onBlur={handleBlur('mt5Credentials.server')}*/}
                {/*      placeholder="Enter MT5 server"*/}
                {/*      error={touched?.mt5Credentials?.server && Boolean(errors['mt5Credentials.server'])}*/}
                {/*      helperText={touched?.mt5Credentials?.server && errors['mt5Credentials.server']}*/}
                {/*    />*/}
                {/*  </FormControl>*/}
                {/*</Grid>*/}

                {/*<Grid item xs={12} md={6}>*/}
                {/*  <FormControl fullWidth>*/}
                {/*    <InputLabel>MT5 Path</InputLabel>*/}
                {/*    <TextField*/}
                {/*      value={formData.mt5Credentials.path}*/}
                {/*      onChange={handleMT5Change('path')}*/}
                {/*      onBlur={handleBlur('mt5Credentials.path')}*/}
                {/*      placeholder="Enter MT5 path"*/}
                {/*      error={touched?.mt5Credentials?.path && Boolean(errors['mt5Credentials.path'])}*/}
                {/*      helperText={touched?.mt5Credentials?.path && errors['mt5Credentials.path']}*/}
                {/*    />*/}
                {/*  </FormControl>*/}
                {/*</Grid>*/}

                <Grid item xs={12} md={6}>
                  <FormControl fullWidth>
                    <InputLabel>MT5 Timezone</InputLabel>
                    <Select
                      value={formData.mt5Credentials.timezone}
                      onChange={handleMT5Change('timezone')}
                      onBlur={handleBlur('mt5Credentials.timezone')}
                      placeholder="Select timezone"
                      error={touched?.mt5Credentials?.timezone && Boolean(errors['mt5Credentials.timezone'])}
                      label="MT5 Timezone"
                    >
                      {timezones.map((tz) => (
                        <MenuItem key={tz.zoneId} value={tz.name}>
                          {`${tz.name} (${getCurrentTimeByZone(tz.zoneId)})`}
                        </MenuItem>
                      ))}
                    </Select>
                    {touched?.mt5Credentials?.timezone && errors['mt5Credentials.timezone'] && (
                      <FormHelperText error>{errors['mt5Credentials.timezone']}</FormHelperText>
                    )}
                  </FormControl>
                </Grid>
              </>
            )}
          </Grid>
        </Box>
      </DialogContent>

      <DialogActions sx={{ p: 2 }}>
        {!isCreating && (
          <Button onClick={() => onDelete(formData.accountId)} disabled={isSaving} color="error">
            Delete
          </Button>
        )}
        <Button onClick={onClose} disabled={isSaving}>
          Cancel
        </Button>
        <Button
          variant="contained"
          onClick={handleSubmit}
          disabled={isSaving}
          startIcon={isSaving ? <CircularProgress size={24} /> : null}
        >
          {isSaving ? 'Saving...' : isCreating ? 'Create Account' : 'Update Account'}
        </Button>
      </DialogActions>
    </Dialog>
  );
}

export default BrokerAccountModal;
