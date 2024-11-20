import React, { useState, useEffect } from 'react';
import {
  Box,
  TextField,
  Button,
  Typography,
  Tabs,
  Tab,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  Grid,
  List,
  ListItemButton,
  ListItemText,
  IconButton,
  Tooltip,
  Paper,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  CircularProgress,
} from '@mui/material';
import { strategyClient } from '@/lib/api/clients/strategy-client';
import { brokerClient } from '@/lib/api/clients/broker-client';
import { logger } from '@/lib/default-logger';
import { Info, X } from '@phosphor-icons/react';
import { toast } from 'react-toastify';

function StrategyConfigurationDialog({ open, onClose, initialConfig = null, onSave, onDelete }) {
  const [loading, setLoading] = useState(false);
  const [selectedStrategy, setSelectedStrategy] = useState('');
  const [strategies, setStrategies] = useState([]);
  const [activeTab, setActiveTab] = useState();
  const [activeGroup, setActiveGroup] = useState('');
  const [instruments, setInstruments] = useState([]);

  const defaultConfig = {
    strategyName: '',
    telegramChatId: '',
    brokerAccount: {
      brokerName: '',
      brokerType: '',
      brokeEnv: '',
      accountId: '',
      initialBalance: '',
    },
    config: {
      strategyClass: '',
      initialCash: '',
      instrumentData: { internalSymbol: '' },
      period: '',
      runParams: [{}],
    },
  };

  const [accounts, setAccounts] = useState([]);
  const [config, setConfig] = useState(defaultConfig);

  // On load clear anything that might be in the dialog
  // and get the base data for this dialog to work
  useEffect(() => {
    setSelectedStrategy('');
    setActiveTab(0);
    setActiveGroup('');
    logger.debug('Is there an initial config?', initialConfig);
    if (!initialConfig) {
      logger.debug('On load, setting default config');
      setConfig(defaultConfig);
    }

    fetchBaseData();
  }, [open]);

  // If initial config present, this is editing a strategy so we can preset things.
  useEffect(() => {
    if (initialConfig) {
      setConfig(initialConfig);
      setSelectedStrategy(initialConfig.config.strategyClass);

      logger.debug('Editing with existing config');

      // Set the first group active
      const groups = Object.keys(groupParams(initialConfig.config.runParams));
      setActiveGroup(groups[0] || '');
    }
  }, [initialConfig]);

  // Utility to get basic data for setting values in the dialog
  const fetchBaseData = async () => {
    setLoading(true);
    try {
      const [strategiesResponse, instrumentsResponse, accountsResponse] = await Promise.all([
        strategyClient.getStrategies(),
        strategyClient.getInstruments(),
        brokerClient.getBrokerAccounts(),
      ]);

      setStrategies(strategiesResponse);
      setInstruments(instrumentsResponse);
      setAccounts(accountsResponse);
    } catch (err) {
      toast.error(`Error fetching base data: ${err.message}`);
      logger.error('Error fetching base data:', err);
    }
    setLoading(false);
  };

  const getDefaultStrategyRunParams = async (strategyClass) => {
    setLoading(true);
    try {
      const params = await strategyClient.getDefaultParamsForStrategyClass(strategyClass);
      return params;
    } catch (error) {
      toast.error(`Error fetching strategy params: ${error.message}`);
      logger.error('Error fetching strategy params:', error);
    } finally {
      setLoading(false);
    }
  };

  const groupParams = (params) => {
    const grouped = params.reduce((groups, param) => {
      const group = param.group || 'General';
      if (!groups[group]) groups[group] = [];
      groups[group].push(param);
      return groups;
    }, {});

    // Only keep General group if it has parameters
    if (grouped['General']?.length === 0) {
      delete grouped['General'];
    }

    return grouped;
  };

  const handleSave = async () => {
    setLoading(true);
    try {
      await onSave(config);
    } catch (error) {
      toast.error(`Error saving strategy: ${error.message}`);
      logger.error('Error saving strategy:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleParamChange = (paramName, value) => {
    setConfig((prev) => ({
      ...prev,
      config: {
        ...prev.config,
        runParams: prev.config.runParams.map((param) => (param.name === paramName ? { ...param, value } : param)),
      },
    }));
  };

  // If we change the strategy, we have to default to the new strategy's parameters
  const handleStrategyChange = async (e) => {
    const stratClass = e.target.value;
    setSelectedStrategy(stratClass);

    if (stratClass) {
      const defaultParams = await getDefaultStrategyRunParams(stratClass);
      logger.debug('defaultParams', defaultParams);
      logger.debug('current config', config);

      // Note we only set the parameter config on the strategy change
      // Since we have no way to transfer data between strategies, we have to accept them as new almost
      setConfig((prev) => ({
        ...prev,
        config: {
          ...prev.config,
          strategyClass: stratClass,
          runParams: defaultParams,
        },
      }));

      // Set the first group active
      const groups = Object.keys(groupParams(defaultParams));
      setActiveGroup(groups[0] || '');
    } else {
      logger.debug('Why is strategyClass empty?');
    }
  };

  const groupedParams = groupParams(config.config.runParams);

  const isValid = () => {
    if (!selectedStrategy) return false;
    if (!config.strategyName) return false;
    if (!config.config?.instrumentData.internalSymbol) return false;
    if (!config.config?.period) return false;
    if (!config.brokerAccount?.accountId) return false;

    // Check if all required parameters have values
    const allParamsSet = config.config.runParams.every((param) => param.value !== undefined && param.value !== '');

    return allParamsSet;
  };

  return (
    <Dialog
      open={open}
      onClose={onClose}
      maxWidth="lg"
      fullWidth
      PaperProps={{
        sx: {
          height: selectedStrategy ? '70vh' : 'auto',
          display: 'flex',
          flexDirection: 'column',
        },
      }}
    >
      <DialogTitle sx={{ m: 0, p: 2, display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <Typography>{initialConfig ? 'Edit Strategy Configuration' : 'Create New Strategy'}</Typography>
        <IconButton onClick={onClose} size="small" aria-label="close">
          <X />
        </IconButton>
      </DialogTitle>

      <DialogContent dividers sx={{ p: 0, display: 'flex', overflow: 'hidden' }}>
        <Box sx={{ display: 'flex', flexDirection: 'column', width: '100%', height: '100%' }}>
          <Box
            sx={{
              p: 3,
              borderBottom: selectedStrategy ? 1 : 0,
              borderColor: 'divider',
              pt: initialConfig ? 2 : 3,
              pb: initialConfig ? 2 : 3,
            }}
          >
            {!initialConfig ? (
              <FormControl fullWidth sx={{ mb: selectedStrategy ? 3 : 0 }}>
                <InputLabel>Strategy Class</InputLabel>
                <Select value={selectedStrategy} onChange={handleStrategyChange} label="Strategy CLass">
                  <MenuItem value="" disabled>
                    <em>Select a strategy class to configure</em>
                  </MenuItem>
                  {strategies.map((strategy) => (
                    <MenuItem key={strategy} value={strategy}>
                      {strategy}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            ) : null}
            {selectedStrategy ? (
              <Tabs value={activeTab} onChange={(e, v) => setActiveTab(v)}>
                <Tab label="Parameters" />
                <Tab label="Run Configuration" />
                <Tab label="Broker Configuration" />
              </Tabs>
            ) : null}
          </Box>

          {selectedStrategy ? (
            <Box sx={{ flex: 1, display: 'flex', overflow: 'hidden' }}>
              {/* Parameters Tab */}
              {activeTab === 0 && (
                <Box sx={{ display: 'flex', width: '100%', height: '100%' }}>
                  {/* Sidebar */}
                  <Paper
                    elevation={0}
                    sx={{
                      width: 150,
                      borderRight: 1,
                      borderColor: 'divider',
                      overflow: 'auto',
                    }}
                  >
                    <List>
                      {Object.keys(groupedParams).map((group) => (
                        <ListItemButton
                          key={group}
                          selected={activeGroup === group}
                          onClick={() => setActiveGroup(group)}
                        >
                          <ListItemText primary={group} />
                        </ListItemButton>
                      ))}
                    </List>
                  </Paper>

                  {/* Parameters Content */}
                  <Box sx={{ flex: 1, p: 3, overflow: 'auto' }}>
                    {activeGroup ? (
                      <Grid container spacing={3}>
                        {groupedParams[activeGroup]?.map((param) => (
                          <Grid item xs={12} md={6} key={param.name}>
                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                              {param.type === 'enum' ? (
                                <FormControl fullWidth>
                                  <InputLabel>{param.name}</InputLabel>
                                  <Select
                                    value={param.value || ''}
                                    onChange={(e) => handleParamChange(param.name, e.target.value)}
                                    label={param.name}
                                  >
                                    {param.enumValues?.map((value) => (
                                      <MenuItem key={value} value={value}>
                                        {value}
                                      </MenuItem>
                                    ))}
                                  </Select>
                                </FormControl>
                              ) : (
                                <TextField
                                  fullWidth
                                  label={param.name}
                                  value={param.value || ''}
                                  onChange={(e) => handleParamChange(param.name, e.target.value)}
                                  placeholder={`Enter ${param.type} value`}
                                  type={param.type === 'int' || param.type === 'double' ? 'number' : 'text'}
                                />
                              )}
                              <Tooltip title={param.description || 'No description available'}>
                                <IconButton size="small">
                                  <Info />
                                </IconButton>
                              </Tooltip>
                            </Box>
                          </Grid>
                        ))}
                      </Grid>
                    ) : null}
                  </Box>
                </Box>
              )}

              {/* Run Configuration Tab */}
              {activeTab === 1 && (
                <Box sx={{ p: 3, width: '100%' }}>
                  <Grid container spacing={3}>
                    <Grid item xs={12} md={6}>
                      <TextField
                        fullWidth
                        label="Strategy Name"
                        value={config.strategyName}
                        placeholder="Enter a name for this strategy"
                        onChange={(e) =>
                          setConfig((prev) => ({
                            ...prev,
                            strategyName: e.target.value,
                          }))
                        }
                      />
                    </Grid>
                    <Grid item xs={12} md={6}>
                      <TextField
                        fullWidth
                        label="Telegram Chat ID"
                        value={config.telegramChatId}
                        placeholder="Enter the Telegram Chat ID to send notifications to"
                        onChange={(e) =>
                          setConfig((prev) => ({
                            ...prev,
                            telegramChatId: e.target.value,
                          }))
                        }
                      />
                    </Grid>
                    <Grid item xs={12} md={6}>
                      <FormControl fullWidth>
                        <InputLabel>Instrument</InputLabel>
                        <Select
                          value={config.config.instrumentData.internalSymbol}
                          onChange={(e) =>
                            setConfig((prev) => ({
                              ...prev,
                              config: {
                                ...prev.config,
                                instrumentData: {
                                  ...prev.config.instrumentData,
                                  internalSymbol: e.target.value,
                                },
                              },
                            }))
                          }
                          label="Instrument"
                        >
                          <MenuItem value="" disabled>
                            <em>Select an instrument</em>
                          </MenuItem>
                          {instruments.map((instrument) => (
                            <MenuItem key={instrument.internalSymbol} value={instrument.internalSymbol}>
                              {instrument.internalSymbol}
                            </MenuItem>
                          ))}
                        </Select>
                      </FormControl>
                    </Grid>
                    <Grid item xs={12} md={6}>
                      <FormControl fullWidth>
                        <InputLabel>Period</InputLabel>
                        <Select
                          value={config.config.period}
                          onChange={(e) =>
                            setConfig((prev) => ({
                              ...prev,
                              config: {
                                ...prev.config,
                                period: e.target.value,
                              },
                            }))
                          }
                          label="Period"
                        >
                          <MenuItem value="" disabled>
                            <em>Select a period</em>
                          </MenuItem>
                          {['M1', 'M5', 'M15', 'M30', 'H1', 'H4', 'D'].map((period) => (
                            <MenuItem key={period} value={period}>
                              {period}
                            </MenuItem>
                          ))}
                        </Select>
                      </FormControl>
                    </Grid>
                  </Grid>
                </Box>
              )}

              {/* Broker Configuration Tab */}
              {activeTab === 2 && (
                <Box sx={{ p: 3, width: '100%' }}>
                  <Grid container spacing={3}>
                    <Grid item xs={12}>
                      <FormControl fullWidth>
                        <InputLabel>Broker Account</InputLabel>
                        <Select
                          value={config.brokerAccount.accountId}
                          onChange={(e) => {
                            const selectedAccount = accounts.find((account) => account.accountId === e.target.value);
                            setConfig((prev) => ({
                              ...prev,
                              brokerAccount: selectedAccount,
                              config: {
                                ...prev.config,
                                initialCash: selectedAccount.initialBalance,
                              },
                            }));
                          }}
                          label="Broker Account"
                        >
                          <MenuItem value="" disabled>
                            <em>Select a broker account</em>
                          </MenuItem>
                          {accounts.map((account) => (
                            <MenuItem key={account.accountId} value={account.accountId}>
                              {`${account.brokerName} - ${account.brokerType} - ${account.brokerEnv} - $${
                                parseFloat(account.initialBalance)
                                  ? parseFloat(account.initialBalance).toLocaleString()
                                  : account.initialBalance
                              }`}
                            </MenuItem>
                          ))}
                        </Select>
                      </FormControl>
                    </Grid>
                  </Grid>
                </Box>
              )}
            </Box>
          ) : null}
        </Box>
      </DialogContent>

      <DialogActions sx={{ p: 2 }}>
        {initialConfig && (
          <Button onClick={() => onDelete(config)} disabled={loading} color="error">
            Delete
          </Button>
        )}
        <Button onClick={onClose} disabled={loading}>
          Cancel
        </Button>
        <Button variant="contained" onClick={handleSave} disabled={loading || !isValid()}>
          {loading ? <CircularProgress size={24} /> : initialConfig ? 'Update Strategy' : 'Create Strategy'}
        </Button>
      </DialogActions>
    </Dialog>
  );
}

export default StrategyConfigurationDialog;
