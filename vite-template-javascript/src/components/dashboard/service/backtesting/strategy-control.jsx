'use client';

import * as React from 'react';
import Button from '@mui/material/Button';
import Card from '@mui/material/Card';
import FormControl from '@mui/material/FormControl';
import InputLabel from '@mui/material/InputLabel';
import MenuItem from '@mui/material/MenuItem';
import Select from '@mui/material/Select';
import Stack from '@mui/material/Stack';
import { Gear as GearIcon } from '@phosphor-icons/react/dist/ssr/Gear';
import { Play as PlayIcon } from '@phosphor-icons/react/dist/ssr/Play';



import { logger } from '@/lib/default-logger';

import { BacktestConfigurationDialog } from './configuration-dialog';

export function StrategyControl({
  systemStrategies = [],
  onSystemStrategyChange,
  selectedSystemStrategyClass = '',
  backtestConfiguration,
  setBacktestConfiguration,
}) {
  const [configOpen, setConfigOpen] = React.useState(false);

  const handleStrategyChange = (event) => {
    onSystemStrategyChange(event.target.value);
  };

  const handleConfigSave = (newConfig) => {
    logger.debug(`Saving new config: ${JSON.stringify(newConfig, null, 2)}`);
    setBacktestConfiguration(newConfig);
  };

  return (
    <>
      <Card
        sx={{
          p: 3,
          backgroundColor: 'background.paper',
          boxShadow: (theme) => theme.shadows[1],
        }}
      >
        <Stack
          direction={{ xs: 'column', sm: 'row' }}
          spacing={2}
          sx={{
            alignItems: 'center',
            justifyContent: 'space-between',
          }}
        >
          <FormControl
            sx={{
              minWidth: { xs: '100%', sm: 300 },
            }}
          >
            <InputLabel id="strategy-select-label">Select Strategy</InputLabel>
            <Select
              labelId="strategy-select-label"
              value={selectedSystemStrategyClass}
              onChange={handleStrategyChange}
              label="Select Strategy"
              sx={{
                '& .MuiSelect-select': {
                  display: 'flex',
                  alignItems: 'center',
                },
              }}
            >
              {systemStrategies.map((strategy, index) => (
                <MenuItem key={index} value={strategy}>
                  {strategy}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
          <Stack
            direction="row"
            spacing={2}
            sx={{
              width: { xs: '100%', sm: 'auto' },
            }}
          >
            <Button
              variant="contained"
              startIcon={<PlayIcon weight="fill" />}
              sx={{
                flex: { xs: 1, sm: 'none' },
              }}
              disabled={!selectedSystemStrategyClass}
            >
              Start Strategy
            </Button>
            <Button
              startIcon={<GearIcon weight="fill" />}
              sx={{
                flex: { xs: 1, sm: 'none' },
              }}
              disabled={!selectedSystemStrategyClass}
              onClick={() => setConfigOpen(true)}
            >
              Configure
            </Button>
          </Stack>
        </Stack>
      </Card>

      <BacktestConfigurationDialog
        open={configOpen}
        onClose={() => setConfigOpen(false)}
        configuration={backtestConfiguration}
        onSave={handleConfigSave}
      />
    </>
  );
}