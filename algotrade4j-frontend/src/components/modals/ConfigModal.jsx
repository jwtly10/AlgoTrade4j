import React, {useEffect, useState} from 'react';
import {Box, Button, Checkbox, Dialog, DialogActions, DialogContent, DialogTitle, FormControl, IconButton, InputLabel, MenuItem, Select, Stack, Tab, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Tabs, TextField, Tooltip, Typography,} from '@mui/material';
import InfoIcon from '@mui/icons-material/Info';
import {apiClient} from '../../api/apiClient.js'
import log from '../../logger.js'
import {Toast} from "../Toast.jsx";

const JsonImportDialog = ({open, onClose, onImport}) => {
    const [jsonInput, setJsonInput] = useState('');

    const handleImport = () => {
        try {
            const importedConfig = JSON.parse(jsonInput);
            onImport(importedConfig);
            onClose();
        } catch (error) {
            log.error("Failed to parse imported JSON", error);
            // You might want to show an error message to the user here
        }
    };

    return (
        <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
            <DialogTitle>Import JSON Configuration</DialogTitle>
            <DialogContent>
                <TextField
                    multiline
                    rows={10}
                    fullWidth
                    variant="outlined"
                    value={jsonInput}
                    onChange={(e) => setJsonInput(e.target.value)}
                    placeholder="Paste your JSON configuration here"
                />
            </DialogContent>
            <DialogActions>
                <Button onClick={onClose}>Cancel</Button>
                <Button onClick={handleImport} variant="contained" color="primary">
                    Import
                </Button>
            </DialogActions>
        </Dialog>
    );
};


const ConfigModal = ({open, onClose, strategyConfig, setStrategyConfig, strategyClass, showOptimiseParams = false}) => {
    const [activeTab, setActiveTab] = useState(0);
    const [localConfig, setLocalConfig] = useState(strategyConfig);
    const [instruments, setInstruments] = useState([]);
    const [isJsonImportOpen, setIsJsonImportOpen] = useState(false);

    const [toast, setToast] = useState({
        open: false,
        level: 'info',
        message: '',
    });
    const handleCloseToast = (event, reason) => {
        if (reason === 'clickaway') {
            return;
        }
        setToast({...toast, level: "info", open: false});
    };

    useEffect(() => {
        if (open) {
            log.debug("Config", strategyConfig);
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
                log.error("Failed to fetch instruments")
            }
        }

        fetchInstruments()
    }, [])

    const handleJsonImport = (importedConfig) => {
        setLocalConfig(prevConfig => {
            let updatedConfig = {...prevConfig};

            log.debug("imported config: ", importedConfig);
            log.debug("params config: ", updatedConfig.runParams);

            if (importedConfig) {
                updatedConfig.runParams = updatedConfig.runParams.map(param => {
                    if (importedConfig.hasOwnProperty(param.name)) {
                        log.debug(`Updating ${param.name} from ${param.value} to ${importedConfig[param.name]}`);
                        return {...param, value: importedConfig[param.name]};
                    }
                    return param;
                });
            }

            // Update other fields that are not in runParams
            Object.keys(importedConfig).forEach(key => {
                if (!updatedConfig.runParams.some(param => param.name === key) && updatedConfig.hasOwnProperty(key)) {
                    log.debug(`Updating ${key} from ${updatedConfig[key]} to ${importedConfig[key]}`);
                    updatedConfig[key] = importedConfig[key];
                }
            });

            log.debug("Previous config:", prevConfig);
            log.debug("Imported configuration:", importedConfig);
            log.debug("Updated configuration:", updatedConfig);

            // Force a re-render by creating a new object
            return {...updatedConfig};
        });

        // Set a timeout to log the updated localConfig after the state has been updated
        setTimeout(() => {
            log.debug("LocalConfig after update:", localConfig);
        }, 0);

        setToast({
            open: true,
            message: "Configuration imported",
            severity: "success",
        });
    };

    const saveToLocalStorage = () => {
        localStorage.setItem(`strategyConfig_${strategyClass}`, JSON.stringify(localConfig));
    };

    const handleTabChange = (event, newValue) => {
        setActiveTab(newValue);
    };

    const handleInputChange = (index, field, value) => {
        setLocalConfig(prev => {
            const updatedRunParams = [...prev.runParams];
            updatedRunParams[index] = {...updatedRunParams[index], [field]: value};
            const newConfig = {...prev, runParams: updatedRunParams};
            log.debug("Updated localConfig:", newConfig);
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
        log.debug(localConfig)
        setStrategyConfig(localConfig);
        onClose();
    };

    return (
        <Dialog open={open} onClose={handleClose} maxWidth="md" fullWidth keepMounted sx={{padding: 3}} disableEnforceFocus disableRestoreFocus aria-hidden={false}>
            <DialogTitle sx={{padding: 3, display: 'flex', alignItems: 'center'}}>
                {showOptimiseParams ? "Optimisation Configuration" : "Strategy Configuration"}
                {showOptimiseParams && (
                    <Tooltip
                        title="Here you can set a range of values you would like to optimise for. Along with parameters used for the test"
                        placement="right"
                        componentsProps={{
                            tooltip: {
                                sx: {
                                    fontSize: '1rem',
                                    padding: '8px 12px'
                                }
                            }
                        }}
                    >
                        <IconButton sx={{marginLeft: 1, padding: 0}}>
                            <InfoIcon fontSize="small"/>
                        </IconButton>
                    </Tooltip>
                )}
            </DialogTitle>
            <Box sx={{px: 3}}>
                <Tabs value={activeTab} onChange={handleTabChange}>
                    <Tab label="Parameters"/>
                    <Tab label="Run Configuration"/>
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
                                    {showOptimiseParams && (
                                        <>
                                            <TableCell>Start</TableCell>
                                            <TableCell>Stop</TableCell>
                                            <TableCell>Step</TableCell>
                                            <TableCell>Optimize</TableCell>
                                        </>
                                    )}
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {Object.entries(
                                    localConfig.runParams.reduce((groups, param) => {
                                        const group = param.group || 'Ungrouped';
                                        if (!groups[group]) groups[group] = [];
                                        groups[group].push(param);
                                        return groups;
                                    }, {})
                                ).map(([groupName, params], groupIndex) => (
                                    <React.Fragment key={groupName}>
                                        <TableRow>
                                            <TableCell
                                                colSpan={6}
                                                style={{
                                                    backgroundColor: 'rgba(255, 255, 255, 0.08)',
                                                    padding: '12px 16px',
                                                }}
                                            >
                                                <Typography
                                                    variant="subtitle1"
                                                    style={{
                                                        color: '#fff',
                                                        fontWeight: 'bold',
                                                        textTransform: 'uppercase'
                                                    }}
                                                >
                                                    {groupName}
                                                </Typography>
                                            </TableCell>
                                        </TableRow>
                                        {params.map((param, index) => (
                                            <TableRow key={param.name}>
                                                <TableCell>
                                                    <Stack direction="row" alignItems="center" spacing={1}>
                                                        <Typography variant="body1">{param.name}</Typography>
                                                        <Tooltip
                                                            title={param.description || 'No description available'}
                                                            arrow
                                                            placement="top"
                                                            componentsProps={{
                                                                tooltip: {
                                                                    sx: {
                                                                        fontSize: '1rem',
                                                                        padding: '8px 12px',
                                                                        maxWidth: '300px',
                                                                        lineHeight: 1.5,
                                                                    }
                                                                }
                                                            }}
                                                        >
                                                            <IconButton size="small">
                                                                <InfoIcon fontSize="small" color="action"/>
                                                            </IconButton>
                                                        </Tooltip>
                                                    </Stack>
                                                </TableCell>
                                                <TableCell>
                                                    <Stack spacing={1}>
                                                        <TextField
                                                            size="small"
                                                            value={param.value}
                                                            onChange={(e) => handleInputChange(localConfig.runParams.indexOf(param), 'value', e.target.value)}
                                                            autoComplete="off"
                                                        />
                                                        <Typography variant="caption" color="textSecondary">
                                                            (Default: {param.defaultValue})
                                                        </Typography>
                                                    </Stack>
                                                </TableCell>
                                                {showOptimiseParams && (
                                                    <>
                                                        <TableCell>
                                                            <TextField
                                                                size="small"
                                                                value={param.start || ''}
                                                                onChange={(e) => handleInputChange(localConfig.runParams.indexOf(param), 'start', e.target.value)}
                                                                autoComplete="off"
                                                            />
                                                        </TableCell>
                                                        <TableCell>
                                                            <TextField
                                                                size="small"
                                                                value={param.stop || ''}
                                                                onChange={(e) => handleInputChange(localConfig.runParams.indexOf(param), 'stop', e.target.value)}
                                                                autoComplete="off"
                                                            />
                                                        </TableCell>
                                                        <TableCell>
                                                            <TextField
                                                                size="small"
                                                                value={param.step || ''}
                                                                onChange={(e) => handleInputChange(localConfig.runParams.indexOf(param), 'step', e.target.value)}
                                                                autoComplete="off"
                                                            />
                                                        </TableCell>
                                                        <TableCell>
                                                            <Checkbox
                                                                checked={param.selected || false}
                                                                onChange={(e) => handleInputChange(localConfig.runParams.indexOf(param), 'selected', e.target.checked)}
                                                            />
                                                        </TableCell>

                                                    </>
                                                )}
                                            </TableRow>
                                        ))}
                                    </React.Fragment>
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
                        {!showOptimiseParams && (
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
                        )}
                        <FormControl fullWidth margin="normal">
                            <InputLabel id="spread-label">Spread</InputLabel>
                            <Select
                                labelId="spread-label"
                                value={localConfig.spread}
                                onChange={(e) => handleConfigChange('spread', e.target.value)}
                                label="Spread"
                            >
                                <MenuItem value="5">5</MenuItem>
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
                                <MenuItem value="M1">1m</MenuItem>
                                <MenuItem value="M5">5m</MenuItem>
                                <MenuItem value="M15">15m</MenuItem>
                                <MenuItem value="M30">30m</MenuItem>
                                <MenuItem value="H1">1H</MenuItem>
                                <MenuItem value="H4">4H</MenuItem>
                                <MenuItem value="D">1D</MenuItem>
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
                {!showOptimiseParams && (
                    <Button onClick={() => setIsJsonImportOpen(true)}>Import JSON</Button>
                )}
                <Button onClick={handleReset}>Reset</Button>
                <Button onClick={handleClose} variant="contained" color="primary">
                    Close
                </Button>
            </DialogActions>
            <JsonImportDialog
                open={isJsonImportOpen}
                onClose={() => setIsJsonImportOpen(false)}
                onImport={handleJsonImport}
            />
            <Toast
                open={toast.open}
                message={toast.message}
                severity={toast.level}
                onClose={handleCloseToast}
            />
        </Dialog>
    );
};

export default ConfigModal;