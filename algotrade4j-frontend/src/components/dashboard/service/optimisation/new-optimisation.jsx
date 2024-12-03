import React, {useEffect, useState} from 'react';
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
import {X} from '@phosphor-icons/react';
import {toast} from 'react-toastify';
import {strategyClient} from '@/lib/api/clients/strategy-client';
import {logger} from '@/lib/default-logger';
import {optimisationClient} from '@/lib/api/clients/optimisation-client';
import Editor from '@monaco-editor/react';

function OptimisationConfigurationDialog({open, onClose}) {
  const [loading, setLoading] = useState(false);
  const [selectedStrategy, setSelectedStrategy] = useState('');
  const [strategies, setStrategies] = useState([]);
  const [instruments, setInstruments] = useState([]);
  const [jsonError, setJsonError] = useState(null);
  const [errMessage, setErrMessage] = useState(null);

  const {colorScheme} = useColorScheme();
  const colors = {
    background: colorScheme === 'dark' ? '#121517' : '#FFFFFF',
    textColor: colorScheme === 'dark' ? '#D9D9D9' : '#121212',
  };

  const defaultConfig = {
    strategyClass: 'DJATRStrategy',
    instrumentData: {
      internalSymbol: 'NAS100USD'
    },
    period: 'M15',
    spread: 10,
    speed: 'INSTANT',
    initialCash: 10000,
    runParams: [],
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
    setErrMessage(null)
    setJsonError(null)

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

      // For a better XP we will hide some settings from the user, as these are defaulted
      const currentConfig = {...config};
      delete currentConfig.speed;

      setConfig({
        ...currentConfig,
        strategyClass: stratClass,
        runParams,
      });
    } catch (error) {
      toast.error(`Error fetching strategy params: ${error.message}`);
    } finally {
      setLoading(false);
    }
  };

  const handleSave = async () => {
    if (jsonError) {
      toast.error('Please fix JSON errors before submitting');
      return;
    }

    setLoading(true);

    const fixedConfig = {...config};
    fixedConfig.speed = "INSTANT";

    logger.debug("Submitting optimisation job with fixed config", fixedConfig);
    try {
      await optimisationClient.queueOptimisation(fixedConfig);
    } catch (error) {
      logger.error('Error submitting job:', error);
      // The api provides validation, so we can use this to validate the form before closing
      if (error.message) {
        setErrMessage(error.message);
      }

      toast.error(`Error submitting job: ${error.message}`);

      // Then just return out
      setLoading(false);
      return;
    }

    setLoading(false)
    toast.success("Optimisation job submitted successfully");
    onClose();
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
      <DialogTitle sx={{m: 0, p: 2, display: 'flex', alignItems: 'center', justifyContent: 'space-between'}}>
        <Typography>Configuration Optimisation Job</Typography>
        <IconButton onClick={onClose} size="small" aria-label="close">
          <X/>
        </IconButton>
      </DialogTitle>

      <DialogContent dividers sx={{p: 0, display: 'flex', flexDirection: 'column', overflow: 'hidden'}}>
        <Box sx={{p: 3, borderBottom: 1, borderColor: 'divider'}}>
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
          <Box sx={{flex: 1, overflow: 'auto', p: 3}}>
            <Alert severity="warning" sx={{mb: 3}}>
              <div className="mb-2">
                Optimisation is an advanced feature of Algotrade4j. It allows you to fine-tune strategies by running 1000s of simulations with all combinations of parameters.
              </div>

              <div className="mt-3 mb-2">
                <strong>Current Configuration Rules:</strong>
              </div>

              <ul className="list-disc pl-6">
                <li>
                  Supported '<code>instrumentData.internalSymbols</code>': {instruments.map((i) => i.internalSymbol).join(', ')}
                </li>
                <li>
                  Supported '<code>period</code>': {['M1', 'M5', 'M15', 'M30', 'H1', 'H4', 'D'].map((i) => i).join(', ')}
                </li>
                <li>
                  Maximum parallel simulations: 1000
                </li>
                <li>
                  Optimisation jobs may take a few minutes to run all simulations
                </li>
              </ul>

              <div className="mt-3 text-sm">
                <strong>Note:</strong> Only advanced users should use this feature. Incorrect configuration may lead to suboptimal results.
              </div>
            </Alert>
            {errMessage ? (
              <Alert severity="error" sx={{mb: 3}}>
                {`Error submitting job: ${errMessage}`}
              </Alert>
            ) : null}
            <Editor
              height="500px"
              defaultLanguage="json"
              theme={colorScheme === 'dark' ? 'vs-dark' : 'light'}
              value={JSON.stringify(config, null, 2)}
              onChange={(value) => {
                try {
                  const parsed = JSON.parse(value);
                  setConfig(parsed);
                  setJsonError(null);
                } catch (err) {
                  setJsonError(err.message);
                }
              }}
              options={{
                minimap: {enabled: false},
                formatOnPaste: true,
                formatOnType: true,
                automaticLayout: true,
              }}
            />
          </Box>
        ) : null}
      </DialogContent>

      <DialogActions sx={{p: 2}}>
        <Typography color="error" sx={{flex: 1, pl: 2}}>
          {jsonError && 'Invalid JSON configuration'}
        </Typography>
        <Button onClick={onClose} disabled={loading}>
          Cancel
        </Button>
        <Button
          variant="contained"
          onClick={handleSave}
          disabled={loading || jsonError || !selectedStrategy}
          startIcon={loading && <CircularProgress size={20}/>}
        >
          Submit Optimisation Job
        </Button>
      </DialogActions>
    </Dialog>
  );
}

export default OptimisationConfigurationDialog;
