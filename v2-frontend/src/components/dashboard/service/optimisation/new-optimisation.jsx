import React, { useEffect, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControl,
  IconButton,
  InputLabel,
  MenuItem,
  Select,
  Typography,
  useColorScheme,
} from '@mui/material';
import { X } from '@phosphor-icons/react';
import { toast } from 'react-toastify';
import JSONInput from 'react-json-editor-ajrm';
import locale from 'react-json-editor-ajrm/locale/en';
import { strategyClient } from '@/lib/api/auth/strategy-client';
import { logger } from '@/lib/default-logger';
import { optimisationClient } from '@/lib/api/optimisation-client';

function OptimisationConfigurationDialog({ open, onClose, onSubmit }) {
  const [loading, setLoading] = useState(false);
  const [selectedStrategy, setSelectedStrategy] = useState('');
  const [strategies, setStrategies] = useState([]);
  const [instruments, setInstruments] = useState([]);
  const [jsonError, setJsonError] = useState(null);
  const [errMessage, setErrMessage] = useState(null);

  const { colorScheme } = useColorScheme();
  const colors = {
    background: colorScheme === 'dark' ? '#121517' : '#FFFFFF',
    textColor: colorScheme === 'dark' ? '#D9D9D9' : '#121212',
  };

  const defaultConfig = {
    strategyClass: 'DJATRStrategy',
    instrument: 'NAS100USD',
    period: 'M15',
    spread: 10,
    speed: 'INSTANT',
    initialCash: 10000,
    parameterRanges: [
      {
        value: '300',
        name: 'stopLossTicks',
        start: '1',
        end: '1',
        step: '1',
        selected: false,
        stringList: null,
      },
    ],
    timeframe: {
      from: '2024-10-04T00:00:00Z',
      to: '2024-10-20T00:00:00Z',
    },
  };
  const [config, setConfig] = useState(defaultConfig);

  useEffect(() => {
    if (open) {
      setSelectedStrategy('');
      fetchBaseData();
    }
  }, [open]);

  const fetchBaseData = async () => {
    setLoading(true);
    try {
      const [strategiesResponse, instrumentsResponse] = await Promise.all([
        strategyClient.getStrategies(),
        strategyClient.getInstruments(),
      ]);
      setStrategies(strategiesResponse);
      setInstruments(instrumentsResponse);
    } catch (err) {
      toast.error(`Error fetching base data: ${err.message}`);
      logger.error('Error fetching base data:', err);
    }
    setLoading(false);
  };

  const handleStrategyChange = async (e) => {
    const stratClass = e.target.value;
    setSelectedStrategy(stratClass);
    setLoading(true);

    try {
      const defaultParams = await strategyClient.getDefaultParamsForStrategyClass(stratClass);

      // Fill default run params with defaults
      const runParams = defaultParams.map((param) => ({
        name: param.name,
        description: param.description,
        value: param.value,
        defaultValue: param.value,
        group: param.group,
        start: '1',
        end: '1',
        step: '1',
        selected: false,
        stringList: '',
      }));

      setConfig((prev) => ({
        ...prev,
        strategyClass: stratClass,
        runParams,
      }));
    } catch (error) {
      toast.error(`Error fetching strategy params: ${error.message}`);
    } finally {
      setLoading(false);
    }
  };

  const handleJsonChange = ({ jsObject, error }) => {
    if (error) {
      setJsonError(error);
    } else {
      setJsonError(null);
      setConfig(jsObject);
    }
  };

  const handleSave = async () => {
    if (jsonError) {
      toast.error('Please fix JSON errors before submitting');
      return;
    }

    setLoading(true);

    try {
      const res = optimisationClient.queueOptimisation(config);
    } catch (error) {
      // The api provides validation, so we can use this to validate the form before closing
      if (error.message) {
        setErrMessage(error.message);
      } else {
        toast('Error submitting job', error);
      }

      // Then just return out
      setLoading(false);
      return;
    }

    try {
      await onSubmit(config);
      onClose();
      setLoading(false);
    } catch (error) {
      toast.error(`Error saving strategy: ${error.message}`);
      logger.error('Error saving strategy:', error);
      setLoading(false);
    }
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
        <Typography>Configuration Optimisation Job</Typography>
        <IconButton onClick={onClose} size="small" aria-label="close">
          <X />
        </IconButton>
      </DialogTitle>

      <DialogContent dividers sx={{ p: 0, display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
        <Box sx={{ p: 3, borderBottom: 1, borderColor: 'divider' }}>
          <FormControl fullWidth>
            <InputLabel>Strategy Class</InputLabel>
            <Select value={selectedStrategy} onChange={handleStrategyChange} label="Strategy Class">
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
        </Box>

        {selectedStrategy ? (
          <Box sx={{ flex: 1, overflow: 'auto', p: 3 }}>
            {errMessage ? (
              <Alert severity="error" sx={{ mb: 3 }}>
                {`Error submitting job: ${errMessage}`}
              </Alert>
            ) : null}
            <JSONInput
              id="json-editor"
              locale={locale}
              placeholder={config}
              height="100%"
              width="100%"
              onChange={handleJsonChange}
              style={{
                body: {
                  fontSize: '14px',
                  //   backgroundColor: colors.background,
                  borderRadius: '10px',
                  padding: '5px',
                },
                contentBox: {
                  borderRadius: '10px',
                },
              }}
              theme={colorScheme === 'dark' ? '' : 'light_mitsuketa_tribute'} // Use default in dark mode
            />
          </Box>
        ) : null}
      </DialogContent>

      <DialogActions sx={{ p: 2 }}>
        <Typography color="error" sx={{ flex: 1, pl: 2 }}>
          {jsonError && 'Invalid JSON configuration'}
        </Typography>
        <Button onClick={onClose} disabled={loading}>
          Cancel
        </Button>
        <Button
          variant="contained"
          onClick={handleSave}
          disabled={loading || jsonError || !selectedStrategy}
          startIcon={loading && <CircularProgress size={20} />}
        >
          Submit Optimisation Job
        </Button>
      </DialogActions>
    </Dialog>
  );
}

export default OptimisationConfigurationDialog;
