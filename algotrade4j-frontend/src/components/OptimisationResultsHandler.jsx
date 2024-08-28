import React from 'react';
import {Box, CircularProgress, Typography} from '@mui/material';
import OptimizationResults from './OptimisationResults.jsx';

const OptimisationResultsContainer = ({optimisationResults, isPolling}) => {
    if (isPolling) {
        return (
            <Box display="flex" justifyContent="center" alignItems="center" height="100%">
                <CircularProgress/>
                <Typography variant="h6" sx={{ml: 2}}>Pending optimisation results, please check back later...</Typography>
            </Box>
        );
    }

    if (!optimisationResults) {
        return (
            <Box display="flex" justifyContent="center" alignItems="center" height="100%">
                <Typography variant="h6">No optimisation results available. Start a new optimisation run.</Typography>
            </Box>
        );
    }

    return <OptimizationResults data={optimisationResults}/>;
};

export default OptimisationResultsContainer;