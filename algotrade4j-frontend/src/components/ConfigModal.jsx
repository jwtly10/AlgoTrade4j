import React, {useState, useEffect} from 'react';
import {
    Button,
    Checkbox,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    TextField,
    Typography,
    Tabs,
    Tab, Box,
} from '@mui/material';

const ConfigModal = ({open, onClose, onSave, params, setParams, strategyConfig, setStrategyConfig, strategyClass}) => {
    const [activeTab, setActiveTab] = useState(0);
    const [localParams, setLocalParams] = useState(params);
    const [localConfig, setLocalConfig] = useState(strategyConfig);

    useEffect(() => {
        if (open) {
            loadFromLocalStorage();
        }
    }, [open, strategyClass]);

    const loadFromLocalStorage = () => {
        const storedConfig = JSON.parse(localStorage.getItem(`strategyConfig_${strategyClass}`)) || {};
        const storedParams = JSON.parse(localStorage.getItem(`strategyParams_${strategyClass}`)) || [];

        const updatedConfig = {
            ...strategyConfig,
            initialCash: storedConfig.initialCash || strategyConfig.initialCash,
            symbol: storedConfig.symbol || strategyConfig.symbol,
            timeframe: {
                from: storedConfig.timeframe?.from || strategyConfig.timeframe.from,
                to: storedConfig.timeframe?.to || strategyConfig.timeframe.to,
            },
        };

        const updatedParams = params.map(param => {
            const storedParam = storedParams.find(p => p.name === param.name) || {};
            return {
                ...param,
                value: storedParam.value !== undefined ? storedParam.value : param.value,
                start: storedParam.start || param.start,
                end: storedParam.end || param.end,
                step: storedParam.step || param.step,
                selected: storedParam.selected || param.selected,
            };
        });

        setLocalConfig(updatedConfig);
        setLocalParams(updatedParams);
    };

    const saveToLocalStorage = (updatedConfig, updatedParams) => {
        localStorage.setItem(`strategyConfig_${strategyClass}`, JSON.stringify(localConfig));
        localStorage.setItem(`strategyParams_${strategyClass}`, JSON.stringify(localParams));
    };


    const handleTabChange = (event, newValue) => {
        setActiveTab(newValue);
    };

    const handleInputChange = (index, field, value) => {
        const updatedParams = [...localParams];
        updatedParams[index][field] = value;
        setLocalParams(updatedParams);
    };

    const handleConfigChange = (field, value) => {
        setLocalConfig(prev => ({
            ...prev,
            [field]: value,
        }));
    };

    const handleClose = () => {
        saveToLocalStorage();
        setParams(localParams);
        setStrategyConfig(localConfig);
        onClose();
    };

    const handleReset = () => {
        loadFromLocalStorage();
    }

    return (
        <Dialog open={open} onClose={handleClose} maxWidth="md" fullWidth keepMounted sx={{padding: 3}}>
            <DialogTitle sx={{padding: 3}}>Strategy Configuration</DialogTitle>
            <Box sx={{px: 3}}>
                <Tabs value={activeTab} onChange={handleTabChange}>
                    <Tab label="Parameters"/>
                    <Tab label="Configuration"/>
                </Tabs>
            </Box>
            <DialogContent>
                {activeTab === 0 && (
                    <TableContainer>
                        <Table>
                            <TableHead>
                                <TableRow>
                                    <TableCell>Parameter</TableCell>
                                    <TableCell>Value</TableCell>
                                    <TableCell>Start</TableCell>
                                    <TableCell>End</TableCell>
                                    <TableCell>Step</TableCell>
                                    <TableCell>Optimize</TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {localParams.map((param, index) => (
                                    <TableRow key={param.name}>
                                        <TableCell>
                                            <Typography variant="body1">{param.name}</Typography>
                                            <Typography variant="caption" color="textSecondary">
                                                {param.description}
                                            </Typography>
                                        </TableCell>
                                        <TableCell>
                                            <TextField
                                                size="small"
                                                value={param.value}
                                                onChange={(e) =>
                                                    handleInputChange(index, 'value', e.target.value)
                                                }
                                                autoComplete="off"
                                            />
                                            <Typography variant="caption" color="textSecondary">
                                                (Default: {params[index].value})
                                            </Typography>
                                        </TableCell>
                                        <TableCell>
                                            <TextField
                                                size="small"
                                                value={param.start || ''}
                                                onChange={(e) =>
                                                    handleInputChange(index, 'start', e.target.value)
                                                }
                                                autoComplete="off"
                                            />
                                        </TableCell>
                                        <TableCell>
                                            <TextField
                                                size="small"
                                                value={param.end || ''}
                                                onChange={(e) =>
                                                    handleInputChange(index, 'end', e.target.value)
                                                }
                                                autoComplete="off"
                                            />
                                        </TableCell>
                                        <TableCell>
                                            <TextField
                                                size="small"
                                                value={param.step || ''}
                                                onChange={(e) =>
                                                    handleInputChange(index, 'step', e.target.value)
                                                }
                                                autoComplete="off"
                                            />
                                        </TableCell>
                                        <TableCell>
                                            <Checkbox
                                                checked={param.selected || false}
                                                onChange={(e) =>
                                                    handleInputChange(
                                                        index,
                                                        'selected',
                                                        e.target.checked
                                                    )
                                                }
                                            />
                                        </TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                    </TableContainer>
                )}
                {activeTab === 1 && (
                    <div>
                        <TextField
                            label="Initial Cash"
                            value={localConfig.initialCash}
                            onChange={(e) => handleConfigChange('initialCash', e.target.value)}
                            fullWidth
                            margin="normal"
                        />
                        <TextField
                            label="Symbol"
                            value={localConfig.symbol}
                            onChange={(e) => handleConfigChange('symbol', e.target.value)}
                            fullWidth
                            margin="normal"
                        />
                        <TextField
                            label="From"
                            type="date"
                            value={localConfig.timeframe.from.slice(0, 10)}  // Only take the date part
                            onChange={(e) => handleConfigChange('timeframe', {
                                ...localConfig.timeframe,
                                from: e.target.value + 'T00:00:00Z'  // Append time as 00:00:00Z
                            })}
                            fullWidth
                            margin="normal"
                            InputLabelProps={{
                                shrink: true,
                            }}
                        />
                        <TextField
                            label="To"
                            type="date"
                            value={localConfig.timeframe.to.slice(0, 10)}  // Only take the date part
                            onChange={(e) => handleConfigChange('timeframe', {
                                ...localConfig.timeframe,
                                to: e.target.value + 'T00:00:00Z'  // Append time as 00:00:00Z
                            })}
                            fullWidth
                            margin="normal"
                            InputLabelProps={{
                                shrink: true,
                            }}
                        />
                    </div>
                )}
            </DialogContent>
            <DialogActions sx={{padding: 3}}>
                <Button onClick={handleReset}>Reset</Button>
                <Button onClick={handleClose} variant="contained" color="primary">
                    Close
                </Button>
            </DialogActions>
        </Dialog>
    );
};

export default ConfigModal;