import React, { useState, useEffect } from 'react';
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
} from '@mui/material';

const ParamModal = ({ open, onClose, params, onSave }) => {
    const [optimizationParams, setOptimizationParams] = useState([]);

    useEffect(() => {
        // Convert the params object to an array of objects
        const paramsArray = Object.entries(params).map(([key, value]) => ({
            parameter: key,
            value,
            start: '',
            end: '',
            step: '',
            selected: false,
        }));
        setOptimizationParams(paramsArray);
    }, [params]);

    const handleInputChange = (index, field, value) => {
        const updatedParams = [...optimizationParams];
        updatedParams[index][field] = value;
        setOptimizationParams(updatedParams);
    };

    const handleSave = () => {
        onSave(optimizationParams);
        onClose();
    };

    return (
        <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
            <DialogTitle>Strategy Parameters</DialogTitle>
            <DialogContent>
                <TableContainer>
                    <Table>
                        <TableHead>
                            <TableRow>
                                <TableCell>Parameter</TableCell>
                                <TableCell>Current Value</TableCell>
                                <TableCell>Start</TableCell>
                                <TableCell>End</TableCell>
                                <TableCell>Step</TableCell>
                                <TableCell>Optimize</TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {optimizationParams.map((param, index) => (
                                <TableRow key={param.parameter}>
                                    <TableCell>{param.parameter}</TableCell>
                                    <TableCell>{param.value}</TableCell>
                                    <TableCell>
                                        <TextField
                                            size="small"
                                            value={param.start}
                                            onChange={(e) =>
                                                handleInputChange(index, 'start', e.target.value)
                                            }
                                            autoComplete="off"
                                        />
                                    </TableCell>
                                    <TableCell>
                                        <TextField
                                            size="small"
                                            value={param.end}
                                            onChange={(e) =>
                                                handleInputChange(index, 'end', e.target.value)
                                            }
                                            autoComplete="off"
                                        />
                                    </TableCell>
                                    <TableCell>
                                        <TextField
                                            size="small"
                                            value={param.step}
                                            onChange={(e) =>
                                                handleInputChange(index, 'step', e.target.value)
                                            }
                                            autoComplete="off"
                                        />
                                    </TableCell>
                                    <TableCell>
                                        <Checkbox
                                            checked={param.selected}
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
            </DialogContent>
            <DialogActions>
                <Button onClick={onClose}>Cancel</Button>
                <Button onClick={handleSave} variant="contained" color="primary">
                    Save
                </Button>
            </DialogActions>
        </Dialog>
    );
};

export default ParamModal;
