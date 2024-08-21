import React from 'react';
import {Box, CircularProgress, Typography} from '@mui/material';

const LoadingChart = () => {
    return (
        <Box
            sx={{
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                justifyContent: 'center',
                height: '400px',
                bgcolor: 'background.paper',
                borderRadius: 1
            }}
        >
            <CircularProgress size={60} thickness={4} sx={{mb: 2}}/>
            <Typography variant="h6">Strategy Running</Typography>
            <Typography variant="body1" color="text.secondary">
                Please wait while the strategy runs ...
            </Typography>
        </Box>
    );
};

export default LoadingChart;