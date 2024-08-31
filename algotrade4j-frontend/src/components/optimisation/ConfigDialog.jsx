import React from 'react';
import {Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, Divider, Grid, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Typography,} from '@mui/material';

const ConfigDialog = ({open, onClose, selectedTask}) => {
    const {config} = selectedTask || {};

    return (
        <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
            <DialogTitle>
                <Box display="flex" alignItems="center">
                    <Typography variant="h6">Optimisation Configuration</Typography>
                </Box>
            </DialogTitle>
            <Divider/>
            <DialogContent>
                <Grid container spacing={3}>
                    <Grid item xs={12} md={6}>
                        <Typography variant="h6" gutterBottom>
                            General Settings
                        </Typography>
                        <Typography variant="body1">
                            <strong>Strategy Class:</strong> {config?.strategyClass}
                        </Typography>
                        <Typography variant="body1">
                            <strong>Instrument:</strong> {config?.instrument}
                        </Typography>
                        <Typography variant="body1">
                            <strong>Period:</strong> {config?.period}
                        </Typography>
                        <Typography variant="body1">
                            <strong>Spread:</strong> {config?.spread}
                        </Typography>
                        <Typography variant="body1">
                            <strong>Speed:</strong> {config?.speed}
                        </Typography>
                        <Typography variant="body1">
                            <strong>Initial Cash:</strong> {config?.initialCash}
                        </Typography>
                    </Grid>
                    <Grid item xs={12} md={6}>
                        <Typography variant="h6" gutterBottom>
                            Timeframe
                        </Typography>
                        <Typography variant="body1">
                            <strong>From:</strong> {config?.timeframe?.from}
                        </Typography>
                        <Typography variant="body1">
                            <strong>To:</strong> {config?.timeframe?.to}
                        </Typography>
                    </Grid>
                </Grid>
                <Typography variant="h6" gutterBottom style={{marginTop: '1rem'}}>
                    Parameter Ranges
                </Typography>
                <TableContainer component={Paper}>
                    <Table size="small">
                        <TableHead>
                            <TableRow>
                                <TableCell>Name</TableCell>
                                <TableCell align="right">Value</TableCell>
                                <TableCell align="right">Start</TableCell>
                                <TableCell align="right">End</TableCell>
                                <TableCell align="right">Step</TableCell>
                                <TableCell align="center">Selected</TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {config?.parameterRanges.map((param) => (
                                <TableRow key={param.name}>
                                    <TableCell component="th" scope="row">
                                        {param.name}
                                    </TableCell>
                                    <TableCell align="right">{param.value}</TableCell>
                                    <TableCell align="right">{param.start}</TableCell>
                                    <TableCell align="right">{param.end}</TableCell>
                                    <TableCell align="right">{param.step}</TableCell>
                                    <TableCell align="center">
                                        {param.selected ? 'Yes' : 'No'}
                                    </TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                </TableContainer>
            </DialogContent>
            <DialogActions>
                <Button onClick={onClose}>Close</Button>
            </DialogActions>
        </Dialog>
    );
};

export default ConfigDialog;