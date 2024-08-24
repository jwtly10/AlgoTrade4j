import React, {useEffect, useState} from 'react';
import {Box, Button, Checkbox, Dialog, DialogActions, DialogContent, DialogTitle, FormControl, IconButton, InputLabel, MenuItem, Select, Stack, Tab, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Tabs, TextField, Tooltip, Typography,} from '@mui/material';
import InfoIcon from '@mui/icons-material/Info';
import {apiClient} from '../../api/apiClient.js'


const ConfigModal = ({open, onClose, strategyConfig, setStrategyConfig, strategyClass}) => {
    const [activeTab, setActiveTab] = useState(0);
    const [localConfig, setLocalConfig] = useState(strategyConfig);
    const [instruments, setInstruments] = useState([]);

    useEffect(() => {
        if (open) {
            console.log("Config", strategyConfig);
            setLocalConfig(strategyConfig);
        }
    }, [open, strategyClass]);

    useEffect(() => {
        // Get all supported instruments
        const fetchInstruments = async () => {
            try {
                const inst = await apiClient.getInstruments()
                setInstruments(inst)
            } catch (e) {
                console.error("Failed to fetch instruments")
            }
        }

        fetchInstruments()
    }, [])

    const saveToLocalStorage = () => {
        localStorage.setItem(`strategyConfig_${strategyClass}`, JSON.stringify(localConfig));
    };

    const handleTabChange = (event, newValue) => {
        setActiveTab(newValue);
    };

    // const handleInputChange = (index, field, value) => {
    //     setLocalConfig(prev => {
    //         const updatedRunParams = [...prev.runParams];
    //         updatedRunParams[index] = {...updatedRunParams[index], [field]: value};
    //         return {...prev, runParams: updatedRunParams};
    //     });
    // };

    const handleInputChange = (index, field, value) => {
        setLocalConfig(prev => {
            const updatedRunParams = [...prev.runParams];
            updatedRunParams[index] = {...updatedRunParams[index], [field]: value};
            const newConfig = {...prev, runParams: updatedRunParams};
            console.log("Updated localConfig:", newConfig);
            return newConfig;
        });
    };

    const handleConfigChange = (field, value) => {
        setLocalConfig(prev => ({
            ...prev,
            [field]: value,
        }));
    };

    const handleReset = () => {
        setLocalConfig(strategyConfig);
    };

    const handleClose = () => {
        saveToLocalStorage();
        console.log(localConfig)
        setStrategyConfig(localConfig);
        onClose();
    };

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
                                    <TableCell>Stop</TableCell>
                                    <TableCell>Step</TableCell>
                                    <TableCell>Optimize</TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {localConfig.runParams.map((param, index) => (
                                    <TableRow key={param.name}>
                                        <TableCell>
                                            <Stack direction="row" alignItems="center" spacing={1}>
                                                <Typography variant="body1">{param.name}</Typography>
                                                <Tooltip title={param.description || 'No description available'} arrow>
                                                    <IconButton size="small">
                                                        <InfoIcon fontSize="small" color="action"/>
                                                    </IconButton>
                                                </Tooltip>
                                            </Stack>
                                        </TableCell>
                                        <TableCell>
                                            <TextField
                                                size="small"
                                                value={param.value}
                                                onChange={(e) => handleInputChange(index, 'value', e.target.value)}
                                                autoComplete="off"
                                            />
                                            <Typography variant="caption" color="textSecondary">
                                                (Default: {param.defaultValue})
                                            </Typography>
                                        </TableCell>
                                        <TableCell>
                                            <TextField
                                                size="small"
                                                value={param.start || ''}
                                                onChange={(e) => handleInputChange(index, 'start', e.target.value)}
                                                autoComplete="off"
                                            />
                                        </TableCell>
                                        <TableCell>
                                            <TextField
                                                size="small"
                                                value={param.stop || ''}
                                                onChange={(e) => handleInputChange(index, 'stop', e.target.value)}
                                                autoComplete="off"
                                            />
                                        </TableCell>
                                        <TableCell>
                                            <TextField
                                                size="small"
                                                value={param.step || ''}
                                                onChange={(e) => handleInputChange(index, 'step', e.target.value)}
                                                autoComplete="off"
                                            />
                                        </TableCell>
                                        <TableCell>
                                            <Checkbox
                                                checked={param.selected || false}
                                                onChange={(e) => handleInputChange(index, 'selected', e.target.checked)}
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
                        <FormControl fullWidth margin="normal">
                            <InputLabel id="speed-label">Speed</InputLabel>
                            <Select
                                labelId="speed-label"
                                value={localConfig.speed}
                                onChange={(e) => handleConfigChange('speed', e.target.value)}
                                label="speed"
                            >
                                <MenuItem value="SLOW">Slow (Visual)</MenuItem>
                                <MenuItem value="NORMAL">Normal (Visual)</MenuItem>
                                {/*<MenuItem value="FAST">Fast</MenuItem>*/}
                                {/*<MenuItem value="VERY_FAST">Very fast</MenuItem>*/}
                                <MenuItem value="INSTANT">Instant (Async)</MenuItem>
                            </Select>
                        </FormControl>
                        <FormControl fullWidth margin="normal">
                            <InputLabel id="spread-label">Spread</InputLabel>
                            <Select
                                labelId="spread-label"
                                value={localConfig.spread}
                                onChange={(e) => handleConfigChange('spread', e.target.value)}
                                label="Spread"
                            >
                                <MenuItem value="0.1">0.1</MenuItem>
                                <MenuItem value="0.5">0.5</MenuItem>
                                <MenuItem value="10">10</MenuItem>
                                <MenuItem value="30">30</MenuItem>
                                <MenuItem value="50">50</MenuItem>
                                <MenuItem value="100">100</MenuItem>
                            </Select>
                        </FormControl>
                        <FormControl fullWidth margin="normal">
                            <InputLabel id="instrument-label">Instrument</InputLabel>
                            <Select
                                labelId="instrument-label"
                                value={localConfig.instrumentData.internalSymbol || ''}
                                onChange={(e) => {
                                    const selectedInstrument = instruments.find(i => i.internalSymbol === e.target.value)
                                    handleConfigChange('instrumentData', selectedInstrument)
                                }
                                }
                                label="Instrument"
                            >
                                {
                                    instruments.length > 0 ? (
                                        instruments.map((instrument, index) => (
                                            <MenuItem key={index} value={instrument.internalSymbol}>{instrument.internalSymbol}</MenuItem>
                                        ))
                                    ) : (
                                        <MenuItem value="" disabled>No Instruments available</MenuItem>
                                    )
                                }
                            </Select>
                        </FormControl>
                        <FormControl fullWidth margin="normal">
                            <InputLabel id="period-label">Period</InputLabel>
                            <Select
                                labelId="period-label"
                                value={localConfig.period}
                                onChange={(e) => handleConfigChange('period', e.target.value)}
                                label="period"
                            >
                                <MenuItem value="1m">1m</MenuItem>
                                <MenuItem value="5m">5m</MenuItem>
                                <MenuItem value="15m">15m</MenuItem>
                                <MenuItem value="30m">30m</MenuItem>
                                <MenuItem value="1H">1H</MenuItem>
                                <MenuItem value="4H">4H</MenuItem>
                                <MenuItem value="1D">1D</MenuItem>
                            </Select>
                        </FormControl>
                        <TextField
                            label="From"
                            type="date"
                            value={localConfig.timeframe.from.slice(0, 10)}
                            onChange={(e) => handleConfigChange('timeframe', {
                                ...localConfig.timeframe,
                                from: e.target.value + 'T00:00:00Z'
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
                            value={localConfig.timeframe.to.slice(0, 10)}
                            onChange={(e) => handleConfigChange('timeframe', {
                                ...localConfig.timeframe,
                                to: e.target.value + 'T00:00:00Z'
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