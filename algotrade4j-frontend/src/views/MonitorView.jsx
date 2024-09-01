import React, {useEffect, useState} from 'react';
import {Box, Paper, Typography} from '@mui/material';
import {systemClient} from '../api/apiClient';
import log from '../logger.js'

const MonitorView = () => {
    const [monitorInfo, setMonitorInfo] = useState({});

    useEffect(() => {
        const fetchMonitorInfo = async () => {
            try {
                const response = await systemClient.monitor();
                setMonitorInfo(response);
            } catch (error) {
                log.error('Failed to fetch monitor info:', error);
            }
        };

        fetchMonitorInfo();
    }, []);

    const prettyPrintedJson = JSON.stringify(monitorInfo, null, 2);

    return (
        <Box sx={{padding: 3}}>
            <Typography variant="h4" gutterBottom>System Monitor</Typography>
            <Paper sx={{padding: 2}}>
                {Object.keys(monitorInfo).length > 0 ? (
                    <pre style={{whiteSpace: 'pre-wrap', wordWrap: 'break-word'}}>
                        {prettyPrintedJson}
                    </pre>
                ) : (
                    <Typography>Loading system information...</Typography>
                )}
            </Paper>
        </Box>
    );
};

export default MonitorView;