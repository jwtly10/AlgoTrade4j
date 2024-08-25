import React from 'react';
import {Box, Grid, Paper, Typography} from '@mui/material';
import InsertChartOutlinedIcon from '@mui/icons-material/InsertChartOutlined';

const EmptyChart = () => {
    return (
        <Box sx={{width: '100%', height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center', bgcolor: 'background.default'}}>
            <Paper elevation={3} sx={{p: 3, borderRadius: 2, bgcolor: 'background.paper', width: '100%', maxWidth: 600}}>
                <Box sx={{mx: 'auto'}}>
                    <Typography variant="h5" gutterBottom align="center">
                        No Chart Data Available
                    </Typography>
                    <Typography variant="subtitle1" color="text.secondary" align="center" gutterBottom>
                        Run a strategy to see chart data
                    </Typography>

                    <Box sx={{display: 'flex', justifyContent: 'center', my: 4}}>
                        <InsertChartOutlinedIcon sx={{fontSize: 60, color: 'text.secondary'}}/>
                    </Box>

                    <Grid container spacing={2} sx={{mt: 2}}>
                        <Grid item xs={12}>
                            <Typography variant="body2" align="center">
                                Select a strategy and run it to populate the chart with data.
                            </Typography>
                        </Grid>
                        <Grid item xs={12}>
                            <Typography variant="body2" align="center">
                                You'll be able to view trade information and analysis once data is available.
                            </Typography>
                        </Grid>
                    </Grid>
                </Box>
            </Paper>
        </Box>
    );
};

export default EmptyChart;