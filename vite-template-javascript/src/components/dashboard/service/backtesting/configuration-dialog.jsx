'use client';

import * as React from 'react';
import {
  Box,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControl,
  Grid,
  IconButton,
  InputLabel,
  MenuItem,
  Paper,
  Select,
  Slider,
  Tab,
  Tabs,
  TextField,
  Typography,
} from '@mui/material';
import { DatePicker, LocalizationProvider } from '@mui/x-date-pickers';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import dayjs from 'dayjs';



import { strategyClient } from '@/lib/api/auth/strategy-client';
import { logger } from '@/lib/default-logger';





function TabPanel({ children, value, index, ...other }) {
  return (
    <div
      role="tabpanel"
      hidden={value !== index}
      id={`config-tabpanel-${index}`}
      aria-labelledby={`config-tab-${index}`}
      {...other}
    >
      {value === index && <Box sx={{ p: 2 }}>{children}</Box>}
    </div>
  );
}

function ConfigurationField({ param, value, onChange }) {
  const handleChange = (event) => {
    onChange(param.name, event.target.value);
  };

  const handleSliderChange = (event, newValue) => {
    onChange(param.name, newValue);
  };

  if (param.type === 'enum') {
    return (
      <FormControl fullWidth margin="normal" variant="outlined">
        <InputLabel id={`${param.name}-label`}>{param.description}</InputLabel>
        <Select labelId={`${param.name}-label`} value={value} onChange={handleChange} label={param.description}>
          {param.enumValues.map((enumValue) => (
            <MenuItem key={enumValue} value={enumValue}>
              {enumValue}
            </MenuItem>
          ))}
        </Select>
      </FormControl>
    );
  }

  if (param.selected && param.start && param.stop && param.step) {
    return (
      <Box sx={{ mt: 3, mb: 2 }}>
        <Typography variant="body2" color="text.secondary" gutterBottom>
          {param.description}
        </Typography>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <Slider
            value={parseFloat(value)}
            onChange={handleSliderChange}
            min={parseFloat(param.start)}
            max={parseFloat(param.stop)}
            step={parseFloat(param.step)}
            marks
            valueLabelDisplay="auto"
            sx={{ flexGrow: 1 }}
          />
          <TextField
            value={value}
            onChange={handleChange}
            type="number"
            size="small"
            sx={{ width: '100px' }}
            InputProps={{
              step: param.type === 'double' ? 0.1 : 1,
            }}
          />
        </Box>
      </Box>
    );
  }

  return (
    <TextField
      fullWidth
      margin="normal"
      variant="outlined"
      label={param.description}
      value={value}
      onChange={handleChange}
      type={param.type === 'int' || param.type === 'double' ? 'number' : 'text'}
      InputProps={{
        step: param.type === 'double' ? 0.1 : 1,
      }}
    />
  );
}

function GeneralConfigSection({ config, onChange }) {
  const handleDateChange = (field, date) => {
    onChange('timeframe', {
      ...config.timeframe,
      [field]: date ? date.format('YYYY-MM-DDTHH:mm:ss[Z]') : '',
    });
  };

  return (
    <LocalizationProvider dateAdapter={AdapterDayjs}>
      <Grid container spacing={2}>
        <Grid item xs={12} sm={6}>
          <TextField
            fullWidth
            label="Strategy Class"
            value={config.strategyClass}
            onChange={(e) => onChange('strategyClass', e.target.value)}
            margin="normal"
            variant="outlined"
            disabled
          />
        </Grid>
        <Grid item xs={12} sm={6}>
          <TextField
            fullWidth
            label="Speed"
            value={config.speed}
            onChange={(e) => onChange('speed', e.target.value)}
            disabled
            type="text"
            margin="normal"
            variant="outlined"
          />
        </Grid>
        <Grid item xs={12} sm={6}>
          <TextField
            fullWidth
            label="Initial Cash"
            value={config.initialCash}
            onChange={(e) => onChange('initialCash', e.target.value)}
            type="number"
            margin="normal"
            variant="outlined"
          />
        </Grid>
        <Grid item xs={12} sm={6}>
          <FormControl fullWidth margin="normal">
            <InputLabel>Spread</InputLabel>
            <Select value={config.spread} onChange={(e) => onChange('spread', e.target.value)} label="Spread">
              <MenuItem value="0">0</MenuItem>
              <MenuItem value="5">5</MenuItem>
              <MenuItem value="10">10</MenuItem>
              <MenuItem value="30">30</MenuItem>
              <MenuItem value="50">50</MenuItem>
              <MenuItem value="100">100</MenuItem>
            </Select>
          </FormControl>
        </Grid>
        <Grid item xs={12} sm={6}>
          <FormControl fullWidth margin="normal" variant="outlined">
            <InputLabel>Period</InputLabel>
            <Select value={config.period} onChange={(e) => onChange('period', e.target.value)} label="Period">
              <MenuItem value="M1">M1</MenuItem>
              <MenuItem value="M5">M5</MenuItem>
              <MenuItem value="M15">M15</MenuItem>
              <MenuItem value="M30">M30</MenuItem>
              <MenuItem value="H1">H1</MenuItem>
              <MenuItem value="H4">H4</MenuItem>
              <MenuItem value="D">D</MenuItem>
            </Select>
          </FormControl>
        </Grid>
        <Grid item xs={12} sm={6}>
          <DatePicker
            label="From Date"
            value={config.timeframe?.from ? dayjs(config.timeframe.from) : null}
            onChange={(date) => handleDateChange('from', date)}
            slotProps={{
              textField: {
                fullWidth: true,
                margin: 'normal',
                variant: 'outlined',
              },
            }}
          />
        </Grid>
        <Grid item xs={12} sm={6}>
          <DatePicker
            label="To Date"
            value={config.timeframe?.to ? dayjs(config.timeframe.to) : null}
            onChange={(date) => handleDateChange('to', date)}
            slotProps={{
              textField: {
                fullWidth: true,
                margin: 'normal',
                variant: 'outlined',
              },
            }}
          />
        </Grid>
      </Grid>
    </LocalizationProvider>
  );
}

function InstrumentConfigSection({ instrumentData, onChange, instruments }) {
  return (
    <Grid container spacing={2}>
      <Grid item xs={12}>
        <FormControl fullWidth margin="normal" variant="outlined">
          <InputLabel>Instrument</InputLabel>
          <Select
            value={instrumentData.internalSymbol || ''}
            onChange={(event) => {
              const selectedInstrument = instruments.find((i) => i.internalSymbol === event.target.value);
              logger.debug('Selected Instrument', selectedInstrument);
              onChange('instrumentData', selectedInstrument);
            }}
            label="Instrument"
          >
            {instruments.length > 0 ? (
              instruments.map((instrument, index) => (
                <MenuItem key={index} value={instrument.internalSymbol}>
                  {instrument.internalSymbol}
                </MenuItem>
              ))
            ) : (
              <MenuItem value="" disabled>
                No Instruments available
              </MenuItem>
            )}
          </Select>
        </FormControl>
      </Grid>
    </Grid>
  );
}

export function BacktestConfigurationDialog({ open, onClose, configuration, onSave }) {
  const [currentTab, setCurrentTab] = React.useState(0);
  const [localConfig, setLocalConfig] = React.useState(configuration);
  const [instruments, setInstruments] = React.useState([]);

  React.useEffect(() => {
    setLocalConfig(configuration);
  }, [configuration]);

  React.useEffect(() => {
    // Get all supported instruments
    const fetchInstruments = async () => {
      const inst = await strategyClient.getInstruments();
      setInstruments(inst);
    };

    fetchInstruments();
  }, []);

  const groupedParams = React.useMemo(() => {
    const groups = {
      General: [],
      Instrument: [],
      ...Object.fromEntries(
        Array.from(new Set(configuration?.runParams?.map((param) => param.group) || [])).map((group) => [group, []])
      ),
    };

    configuration?.runParams?.forEach((param) => {
      if (groups[param.group]) {
        groups[param.group].push(param);
      }
    });

    return groups;
  }, [configuration]);

  const handleTabChange = (event, newValue) => {
    setCurrentTab(newValue);
  };

  const handleFieldChange = (name, value) => {
    setLocalConfig((prev) => ({
      ...prev,
      runParams: prev.runParams.map((param) => (param.name === name ? { ...param, value } : param)),
    }));
  };

  const handleGeneralConfigChange = (field, value) => {
    setLocalConfig((prev) => ({
      ...prev,
      [field]: value,
    }));
  };

  const handleInstrumentConfigChange = (field, value) => {
    if (field === 'instrumentData') {
      // When receiving complete instrument data
      setLocalConfig((prev) => ({
        ...prev,
        instrumentData: value,
      }));
    } else {
      // For any other individual field updates (if needed)
      setLocalConfig((prev) => ({
        ...prev,
        instrumentData: {
          ...prev.instrumentData,
          [field]: value,
        },
      }));
    }
  };

  const handleSave = () => {
    saveToLocalStorage();
    onSave(localConfig);
    onClose();
  };

  const saveToLocalStorage = () => {
    localStorage.setItem(`strategyConfig_${localConfig.strategyClass}`, JSON.stringify(localConfig));
  };

  const groups = Object.keys(groupedParams);

  return (
    <Dialog
      open={open}
      onClose={onClose}
      maxWidth="md"
      fullWidth
      PaperProps={{
        sx: {
          borderRadius: 2,
          boxShadow: 24,
        },
      }}
    >
      <DialogTitle
        sx={{
          m: 0,
          p: 2,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          bgcolor: 'primary.main',
          color: 'primary.contrastText',
        }}
      >
        <Typography variant="h6" component="div">
          Strategy Configuration
        </Typography>
        <IconButton aria-label="close" onClick={onClose} sx={{ color: 'inherit' }}>
          {/* <CloseIcon /> */}
        </IconButton>
      </DialogTitle>

      <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
        <Tabs
          value={currentTab}
          onChange={handleTabChange}
          variant="scrollable"
          scrollButtons="auto"
          sx={{ bgcolor: 'background.paper' }}
        >
          {groups.map((group) => (
            <Tab
              key={group}
              label={group}
              sx={{
                textTransform: 'none',
                minWidth: 100,
                fontWeight: 'medium',
              }}
            />
          ))}
        </Tabs>
      </Box>

      <DialogContent dividers sx={{ bgcolor: 'background.default' }}>
        {groups.map((group, index) => (
          <TabPanel key={group} value={currentTab} index={index}>
            <Paper elevation={0} sx={{ p: 2, bgcolor: 'background.paper' }}>
              {group === 'General' ? (
                <GeneralConfigSection config={localConfig} onChange={handleGeneralConfigChange} />
              ) : group === 'Instrument' ? (
                <InstrumentConfigSection
                  instrumentData={localConfig.instrumentData}
                  onChange={handleInstrumentConfigChange}
                  instruments={instruments}
                />
              ) : (
                groupedParams[group].map((param) => {
                  const paramValue = localConfig?.runParams?.find((p) => p.name === param.name)?.value;
                  return (
                    <ConfigurationField
                      key={param.name}
                      param={param}
                      value={paramValue}
                      onChange={handleFieldChange}
                    />
                  );
                })
              )}
            </Paper>
          </TabPanel>
        ))}
      </DialogContent>

      <DialogActions sx={{ p: 2, bgcolor: 'background.paper' }}>
        <Button onClick={onClose} variant="outlined" color="inherit">
          Cancel
        </Button>
        <Button
          variant="contained"
          onClick={handleSave}
          color="primary"
          sx={{
            px: 4,
          }}
        >
          Save Changes
        </Button>
      </DialogActions>
    </Dialog>
  );
}