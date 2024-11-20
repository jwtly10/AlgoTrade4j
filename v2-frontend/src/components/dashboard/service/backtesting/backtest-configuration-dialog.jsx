import React, { useEffect, useState } from 'react';
import {
  Box,
  Button,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControl,
  Grid,
  IconButton,
  InputLabel,
  List,
  ListItemButton,
  ListItemText,
  MenuItem,
  Paper,
  Select,
  Tab,
  Tabs,
  TextField,
  Tooltip,
  Typography,
} from '@mui/material';
import { strategyClient } from '@/lib/api/clients/strategy-client';
import { logger } from '@/lib/default-logger';
import { Info, X } from '@phosphor-icons/react';
import { toast } from 'react-toastify';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import dayjs from 'dayjs';
import utc from 'dayjs/plugin/utc';

dayjs.extend(utc);

function BacktestConfigurationDialog({ open, onClose, backtestConfig, onSave, onDelete }) {
  const [loading, setLoading] = useState(false);
  const [activeTab, setActiveTab] = useState(0);
  const [activeGroup, setActiveGroup] = useState('');
  const [instruments, setInstruments] = useState([]);

  const [config, setConfig] = useState({});

  // On load clear anything that might be in the dialog
  // and get the base data for this dialog to work
  useEffect(() => {
    logger.debug(`Edit Backtest Configuration : ${backtestConfig}`);
    if (!backtestConfig) {
      toast.error('Error fetching base data: initialConfig is empty');
    }
    setConfig(backtestConfig);
    setActiveTab(0);
    setActiveGroup('');
    fetchBaseData();

    const groups = Object.keys(groupParams(backtestConfig.runParams));
    setActiveGroup(groups[0] || '');

    fetchBaseData();
  }, [open]);

  // Utility to get basic data for setting values in the dialog
  const fetchBaseData = async () => {
    setLoading(true);
    try {
      const [instrumentsResponse] = await Promise.all([strategyClient.getInstruments()]);

      setInstruments(instrumentsResponse);
    } catch (err) {
      toast.error(`Error fetching base data: ${err.message}`);
      logger.error('Error fetching base data:', err);
    }
    setLoading(false);
  };

  const groupParams = (params = config?.runParams || []) => {
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
      toast.error(`Error saving backtest config: ${error.message}`);
      logger.error('Error saving backtest config:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleParamChange = (paramName, value) => {
    logger.debug('Changing param', paramName, value);
    logger.debug(config.runParams.map((param) => (param.name === paramName ? { ...param, value } : param)));
    setConfig((prev) => ({
      ...prev,
      runParams: prev.runParams.map((param) => (param.name === paramName ? { ...param, value } : param)),
    }));
  };

  const handleDateChange = (type, date) => {
    setConfig((prev) => ({
      ...prev,
      timeframe: {
        ...prev.timeframe,
        [type]: date ? `${date.format('YYYY-MM-DD')}T00:00:00Z` : null,
      },
    }));
  };

  const isValid = () => {
    if (config.instrumentData === undefined) {
      // On mount this is empty, so we need to check for it
      return true;
    }

    logger.debug('Validating config:');
    logger.debug(config);

    if (!config?.instrumentData.internalSymbol) return false;
    if (!config?.period) return false;

    // Check if all required parameters have values
    const allParamsSet = config.runParams.every((param) => param.value !== undefined && param.value !== '');

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
          height: 'auto',
          display: 'flex',
          flexDirection: 'column',
        },
      }}
    >
      <DialogTitle sx={{ m: 0, p: 2, display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <Typography>Edit Backtest Configuration</Typography>
        <IconButton onClick={onClose} size="small" aria-label="close">
          <X />
        </IconButton>
      </DialogTitle>

      <DialogContent dividers sx={{ p: 0, display: 'flex', overflow: 'hidden' }}>
        <Box sx={{ display: 'flex', flexDirection: 'column', width: '100%', height: '100%' }}>
          <Box
            sx={{
              p: 3,
              borderBottom: 1,
              borderColor: 'divider',
              pt: backtestConfig ? 2 : 3,
              pb: backtestConfig ? 2 : 3,
            }}
          >
            <Tabs value={activeTab} onChange={(e, v) => setActiveTab(v)}>
              <Tab label="Parameters" />
              <Tab label="Run Configuration" />
            </Tabs>
          </Box>

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
                    {Object.keys(groupParams()).map((group) => (
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
                      {groupParams()[activeGroup]?.map((param) => (
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
                      label="Intial Cash"
                      value={config.initialCash || ''}
                      onChange={(e) => {
                        setConfig((prev) => ({
                          ...prev,
                          initialCash: e.target.value,
                        }));
                      }}
                      placeholder={`Enter initial cash`}
                      type="number"
                    />
                  </Grid>
                  <Grid item xs={12} md={6}>
                    <FormControl fullWidth>
                      <InputLabel>Spread</InputLabel>
                      <Select
                        value={config.spread}
                        onChange={(e) =>
                          setConfig((prev) => ({
                            ...prev,
                            spread: e.target.value,
                          }))
                        }
                        label="Spread"
                      >
                        <MenuItem value="" disabled>
                          <em>Select a spread</em>
                        </MenuItem>
                        {[0, 5, 10, 30, 50, 100].map((spread, index) => (
                          <MenuItem key={index} value={spread}>
                            {spread}
                          </MenuItem>
                        ))}
                      </Select>
                    </FormControl>
                  </Grid>
                  <Grid item xs={12} md={6}>
                    <FormControl fullWidth>
                      <InputLabel>Speed</InputLabel>
                      <Select
                        value={config.speed}
                        onChange={(e) =>
                          setConfig((prev) => ({
                            ...prev,
                            speed: e.target.value,
                          }))
                        }
                        label="Speed"
                        disabled
                      >
                        <MenuItem value="INSTANT" disabled>
                          Instant
                        </MenuItem>
                      </Select>
                    </FormControl>
                  </Grid>
                  <Grid item xs={12} md={6}>
                    <FormControl fullWidth>
                      <InputLabel>Instrument</InputLabel>
                      <Select
                        value={config.instrumentData.internalSymbol}
                        onChange={(e) =>
                          setConfig((prev) => ({
                            ...prev,
                            instrumentData: {
                              ...prev.instrumentData,
                              internalSymbol: e.target.value,
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
                        value={config.period}
                        onChange={(e) =>
                          setConfig((prev) => ({
                            ...prev,
                            period: e.target.value,
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
                  <Grid item xs={12}>
                    <Grid container spacing={3}>
                      <Grid item xs={12} md={6}>
                        <DatePicker
                          label="From Date"
                          value={config.timeframe?.from ? dayjs(config.timeframe.from) : null}
                          onChange={(date) => handleDateChange('from', date)}
                          slotProps={{
                            textField: {
                              fullWidth: true,
                              variant: 'outlined',
                            },
                          }}
                          format="YYYY-MM-DD"
                        />
                      </Grid>
                      <Grid item xs={12} md={6}>
                        <DatePicker
                          label="To Date"
                          value={config.timeframe?.to ? dayjs(config.timeframe.to) : null}
                          onChange={(date) => handleDateChange('to', date)}
                          slotProps={{
                            textField: {
                              fullWidth: true,
                              variant: 'outlined',
                            },
                          }}
                          format="YYYY-MM-DD"
                        />
                      </Grid>
                    </Grid>
                  </Grid>
                </Grid>
              </Box>
            )}
          </Box>
        </Box>
      </DialogContent>

      <DialogActions sx={{ p: 2 }}>
        <Button onClick={onClose} disabled={loading}>
          Cancel
        </Button>
        <Button variant="contained" onClick={handleSave} disabled={loading || !isValid()}>
          {loading ? <CircularProgress size={24} /> : 'Save'}
        </Button>
      </DialogActions>
    </Dialog>
  );
}

export default BacktestConfigurationDialog;
