import React, {useEffect, useState} from 'react';
import {apiClient} from '../api/apiClient.js';
import 'chartjs-adapter-date-fns';
import {Box, Button, FormControl, Grid, InputLabel, MenuItem, Paper, Select, Typography,} from '@mui/material';
import ConfigModal from '../components/modals/ConfigModal.jsx';
import {Toast} from "../components/Toast.jsx";
import log from '../logger.js'
import {useOptimisationResults} from "../hooks/useOptimisationResults.js";
import OptimisationResultsContainer from "../components/OptimisationResultsHandler.jsx";
import PlayArrowIcon from '@mui/icons-material/PlayArrow';
import SettingsIcon from '@mui/icons-material/Settings';
import ClearIcon from '@mui/icons-material/Clear';


const OptimisationView = () => {
    const {optimisationResults, isPolling, startNewOptimisation, stopOptimisation} = useOptimisationResults(apiClient);

    const [isRunning, setIsRunning] = useState(false);
    const [isModalOpen, setIsModalOpen] = useState(false);

    const [strategies, setStrategies] = useState([]);
    const [strategyClass, setStrategyClass] = useState("");

    const [strategyConfig, setStrategyConfig] = useState({
        strategyClass: '',
        initialCash: '10000',
        instrumentData: {},
        spread: "50",
        speed: "NORMAL",
        period: "1D",
        timeframe: {
            from: '',
            to: '',
        },
        runParams: []
    })

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
        const fetchStrategies = async () => {
            try {
                const res = await apiClient.getStrategies()
                setStrategies(res)

                const lastStrat = localStorage.getItem("LAST_STRAT")

                res.forEach(strat => {
                    if (strat === lastStrat) {
                        handleChangeStrategy(lastStrat)
                    }
                })
            } catch (error) {
                log.error('Failed to get strategies:', error);
            }
        }
        fetchStrategies()
    }, []);

    // Here we check local storage and update the config with the values from local storage
    // if there are any, else we use the defaults from the strategy
    const loadConfigFromLocalStorage = (runParams, stratClass) => {
        const storedConfig = JSON.parse(localStorage.getItem(`strategyConfig_${stratClass}`)) || {};

        // Check storedConfig for runParams, if we have them, we need to update the values!!!!! in the run params
        const updatedRunParams = runParams.map(param => {
            const storedParam = storedConfig.runParams?.find(p => p.name === param.name);

            // Only update if theres data to update
            if (storedParam) {
                return {
                    ...param,
                    value: storedParam.value !== undefined ? storedParam.value : param.value,
                    start: storedParam.start !== undefined ? storedParam.start : param.start,
                    stop: storedParam.stop !== undefined ? storedParam.stop : param.stop,
                    step: storedParam.step !== undefined ? storedParam.step : param.step,
                    selected: storedParam.selected !== undefined ? storedParam.selected : param.selected,
                };
            } else {
                return param; // Keep the original parameter if no stored version is found
            }
        });

        // Setting some defaults in case we don't have any values in local storage
        const today = new Date().toISOString().split('T')[0] + 'T00:00:00Z';
        const lastMonth = new Date(Date.now() - (86400000 * 30)).toISOString().split('T')[0] + 'T00:00:00Z';

        const updatedConfig = {
            ...strategyConfig,
            initialCash: storedConfig.initialCash || strategyConfig.initialCash,
            instrumentData: storedConfig.instrumentData || strategyConfig.instrumentData,
            spread: storedConfig.spread || strategyConfig.spread,
            period: storedConfig.period || strategyConfig.period,
            speed: storedConfig.speed || strategyConfig.speed,
            timeframe: {
                from: storedConfig.timeframe?.from || lastMonth,
                to: storedConfig.timeframe?.to || today,
            },
            runParams: updatedRunParams,
            strategyClass: stratClass,
        };

        // Now we can update the state with the updated values
        setStrategyConfig(updatedConfig);
    };

    const startOptimisation = async () => {
        if (strategyClass === "") {
            log.error('Strategy class is required');
            setToast({
                open: true,
                level: 'warning',
                message: 'Please select a strategy',
            });
            return;
        }

        setIsRunning(true)

        const hackConfig = {
            ...strategyConfig,
            strategyClass: strategyClass,
        }

        log.debug('Starting optimisation...');

        const oId = crypto.randomUUID()
        try {
            await apiClient.startOptimisation(hackConfig, oId)
            startNewOptimisation(oId)
            log.debug('Optimisation started');
            setToast({
                open: true,
                level: 'success',
                message: 'Optimisation started',
            })
            setIsRunning(false);
        } catch (error) {
            log.error('Failed to start optimisation:', error);
            setToast({
                open: true,
                level: 'error',
                message: 'Failed to start optimisation: ' + error.response.data.message,
            })
            setIsRunning(false);
        }
    };

    const handleOpenParams = () => {
        setIsModalOpen(true);
    }

    const getParams = async (stratClass) => {
        try {
            return await apiClient.getParams(stratClass);
        } catch (error) {
            log.error('Failed to get strategy params:', error);
            setToast({
                open: true,
                message: 'Failed to get strategy params: ' + error.response.data.message,
            });
        }
    };

    const handleConfigSave = (config) => {
        log.debug('Saving params:', config);
        setIsModalOpen(false);
    };

    const handleChangeStrategy = async (valueOrEvent) => {
        // Get the class of the strategy
        let stratClass;
        // Hack to use this function in other places
        if (typeof valueOrEvent === 'string') {
            // If a string is passed directly
            stratClass = valueOrEvent;
        } else if (valueOrEvent && valueOrEvent.target) {
            // If an event object is passed (from onChange)
            stratClass = valueOrEvent.target.value;
        } else {
            log.error('Invalid input to handleChangeStrategy');
            return;
        }
        localStorage.setItem("LAST_STRAT", stratClass);
        setStrategyClass(stratClass);

        // Update config with class
        setStrategyConfig({
            ...strategyConfig,
            strategyClass: stratClass,
        });

        // Now, we know what params have come from the strategy defaults. So we should set these are the run params for now.
        const params = await getParams(stratClass)
        log.debug("Params", params)

        let runParams = [];
        params.forEach(param => {
            runParams.push({
                name: param.name,
                // Default from server
                value: param.value,
                // Defaults
                defaultValue: param.value,
                description: param.description,
                group: param.group,
                start: "1",
                stop: "1",
                step: "1",
                selected: false,
            })
        })

        log.debug("Run Params", runParams)

        setStrategyConfig({
            ...strategyConfig,
            runParams: runParams,
        })

        // Now we have the defaults, we need to make sure we have the values from local storage, in case we changed this at any point
        loadConfigFromLocalStorage(runParams, stratClass);
    }

    return (
        <Box sx={{
            display: 'flex',
            flexDirection: 'column',
            height: 'calc(100vh - 64px - 30px)', // Subtract navbar height and version banner
            width: '100vw',
            overflow: 'hidden'
        }}>
            {/* Header */}
            <Box sx={{flexShrink: 0, p: 2, bgcolor: 'background.paper', overflow: 'auto'}}>
                <Grid container justifyContent="flex-end" spacing={2}>
                </Grid>
            </Box>

            {/* Main content area */}
            <Box sx={{flexGrow: 1, display: 'flex', overflow: 'hidden'}}>
                {/* Left section (3/4 width) */}
                <Box sx={{flexGrow: 1, height: '100%', overflow: 'hidden'}}>
                    <Paper elevation={3} sx={{height: '100%', display: 'flex', flexDirection: 'column', p: 3}}>
                        {/* Result Section */}
                        <Box sx={{flexShrink: 0, height: '100%', minHeight: '500px', mb: 3, bgcolor: 'background.paper', borderRadius: 1, overflow: 'hidden'}}>
                            <OptimisationResultsContainer
                                optimisationResults={optimisationResults}
                                isPolling={isPolling}
                            />
                        </Box>
                    </Paper>
                </Box>

                {/* Right section (1/4 width) */}
                <Box sx={{width: '25%', minWidth: '300px', p: 3, bgcolor: 'background.paper', boxShadow: 1, overflow: 'auto'}}>
                    <Box sx={{display: 'flex', flexDirection: 'column', height: '100%'}}>
                        <FormControl fullWidth variant="outlined" sx={{mb: 3}}>
                            <InputLabel>Select a strategy</InputLabel>
                            <Select
                                value={strategyClass}
                                onChange={handleChangeStrategy}
                                label="Select a strategy"
                                displayEmpty
                            >
                                {strategies.length > 0 ? (
                                    strategies.map((strategy, index) => (
                                        <MenuItem key={index} value={strategy}>
                                            {strategy}
                                        </MenuItem>
                                    ))
                                ) : (
                                    <MenuItem value="" disabled>
                                        No strategies available
                                    </MenuItem>
                                )}
                            </Select>
                        </FormControl>

                        <Button
                            variant="contained"
                            color="success"
                            onClick={startOptimisation}
                            disabled={strategyClass === "" || isRunning}
                            size="large"
                            sx={{
                                mb: 2,
                            }}
                            startIcon={<PlayArrowIcon/>}
                        >
                            Start Optimisation
                        </Button>

                        <Button
                            variant="contained"
                            onClick={handleOpenParams}
                            disabled={strategyClass === ""}
                            size="large"
                            sx={{
                                mb: 2,
                                backgroundColor: '#9c27b0',
                                '&:hover': {backgroundColor: '#7b1fa2'},
                                '&:disabled': {backgroundColor: 'rgba(156, 39, 176, 0.12)'},
                            }}
                            startIcon={<SettingsIcon/>}
                        >
                            Configure Parameters
                        </Button>

                        <Button
                            variant="contained"
                            onClick={stopOptimisation}
                            size="large"
                            sx={{
                                mb: 2,
                                backgroundColor: '#d32f2f',
                                '&:hover': {backgroundColor: '#c62828'},
                            }}
                            startIcon={<ClearIcon/>}
                        >
                            Clear Cache
                        </Button>

                        <Box sx={{flexGrow: 1}}/> {/* Spacer */}

                        <Typography variant="body2" color="text.secondary" align="center">
                            Select a strategy and configure parameters before starting an optimisation run.
                        </Typography>
                    </Box>
                </Box>
            </Box>

            {/* Modals */}
            <ConfigModal
                open={isModalOpen}
                onClose={() => setIsModalOpen(false)}
                onSave={handleConfigSave}
                strategyConfig={strategyConfig}
                setStrategyConfig={setStrategyConfig}
                strategyClass={strategyClass}
                backtestMode={false}
            />
            <Toast
                open={toast.open}
                message={toast.message}
                severity={toast.level}
                onClose={handleCloseToast}
            />
        </Box>
    );
};

export default OptimisationView;