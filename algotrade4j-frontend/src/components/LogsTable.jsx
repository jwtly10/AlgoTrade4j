import React from 'react';
import {Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow} from '@mui/material';
import {Error, Info, Warning} from '@mui/icons-material';

const LogsTable = ({logs}) => {
    const getLogIcon = (type) => {
        switch (type) {
            case 'info':
                return <Info color="info"/>;
            case 'warning':
                return <Warning color="warning"/>;
            case 'error':
                return <Error color="error"/>;
            default:
                return <Info color="info"/>;
        }
    };

    return (
        <TableContainer component={Paper}>
            <Table>
                <TableHead>
                    <TableRow>
                        <TableCell>Time</TableCell>
                        <TableCell>Message</TableCell>
                    </TableRow>
                </TableHead>
                <TableBody>
                    {logs.map((log, index) => (
                        <TableRow key={index}>
                            <TableCell>
                                <div style={{display: 'flex', alignItems: 'center'}}>
                                    {getLogIcon(log.type)}
                                    <span style={{marginLeft: '8px'}}>{log.timestamp}</span>
                                </div>
                            </TableCell>
                            <TableCell>{log.message}</TableCell>
                        </TableRow>
                    ))}
                </TableBody>
            </Table>
        </TableContainer>
    );
};

export default LogsTable;