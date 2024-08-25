import React, {useEffect, useState} from 'react';
import {Box, Grid, LinearProgress, Paper, Typography} from '@mui/material';
import {formatDistanceToNow} from 'date-fns';

const LoadingChart = ({progressData, startTime}) => {
    const {
        percentageComplete,
        currentIndex,
        totalDays,
        fromDay,
        toDay,
        currentDay,
        instrument,
        ticksModelled,
        strategyId
    } = progressData || {};

    const [timeElapsed, setTimeElapsed] = useState('');

    useEffect(() => {
        const updateTimeElapsed = () => {
            if (startTime) {
                setTimeElapsed(formatDistanceToNow(new Date(startTime), {addSuffix: true}));
            }
        };

        updateTimeElapsed(); // Initial update
        const timer = setInterval(updateTimeElapsed, 1000);

        return () => clearInterval(timer);
    }, [startTime]);

    const formatDate = (timestamp) => {
        if (!timestamp) return '';
        return new Date(timestamp * 1000).toLocaleDateString();
    };

    function formatNumber(number) {
        if (!number) {
            return number
        }
        const decimalPlaces = 0;
        // Convert the number to a string and split it into integer and decimal parts
        let [integerPart, decimalPart] = number.toFixed(decimalPlaces).split('.');

        // Add commas to the integer part
        integerPart = integerPart.replace(/\B(?=(\d{3})+(?!\d))/g, ",");

        // Combine the parts and return
        return decimalPart ? `${integerPart}.${decimalPart}` : integerPart;
    }

    return (
        <Box sx={{width: '100%', height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center'}}>
            <Paper elevation={3} sx={{p: 3, borderRadius: 2}}>
                <Box sx={{maxWidth: 600, mx: 'auto'}}>
                    <Typography variant="h5" gutterBottom align="center">
                        Strategy Running
                    </Typography>
                    <Typography variant="subtitle1" color="text.secondary" align="center" gutterBottom>
                        {strategyId} on {instrument}
                    </Typography>

                    <Box sx={{my: 4}}>
                        <LinearProgress
                            variant="determinate"
                            value={percentageComplete || 0}
                            sx={{height: 10, borderRadius: 5}}
                        />
                        <Typography variant="body2" color="text.secondary" align="right" sx={{mt: 1}}>
                            {percentageComplete?.toFixed(2)}% Complete
                        </Typography>
                    </Box>

                    <Grid container spacing={2} sx={{mt: 2}}>
                        <Grid item xs={6}>
                            <Typography variant="body2">Progress:</Typography>
                        </Grid>
                        <Grid item xs={6}>
                            <Typography variant="body2" align="right">
                                Day {currentIndex} of {totalDays}
                            </Typography>
                        </Grid>
                        <Grid item xs={6}>
                            <Typography variant="body2">Time Elapsed:</Typography>
                        </Grid>
                        <Grid item xs={6}>
                            <Typography variant="body2" align="right">{timeElapsed || 'Calculating...'}</Typography>
                        </Grid>
                        <Grid item xs={6}>
                            <Typography variant="body2">Start Date:</Typography>
                        </Grid>
                        <Grid item xs={6}>
                            <Typography variant="body2" align="right">{formatDate(fromDay)}</Typography>
                        </Grid>
                        <Grid item xs={6}>
                            <Typography variant="body2">End Date:</Typography>
                        </Grid>
                        <Grid item xs={6}>
                            <Typography variant="body2" align="right">{formatDate(toDay)}</Typography>
                        </Grid>
                        <Grid item xs={6}>
                            <Typography variant="body2">Current Date:</Typography>
                        </Grid>
                        <Grid item xs={6}>
                            <Typography variant="body2" align="right">{formatDate(currentDay)}</Typography>
                        </Grid>
                        <Grid item xs={6}>
                            <Typography variant="body2">Ticks Modelled:</Typography>
                        </Grid>
                        <Grid item xs={6}>
                            <Typography variant="body2" align="right">{formatNumber(ticksModelled)}</Typography>
                        </Grid>
                    </Grid>
                </Box>
            </Paper>
        </Box>
    );
};

export default LoadingChart;