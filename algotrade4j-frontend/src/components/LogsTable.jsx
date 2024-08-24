import React, {useState} from 'react';
import {Paper, Table, TableBody, TableCell, TableContainer, TableHead, TablePagination, TableRow} from '@mui/material';
import {Error, Info, Warning} from '@mui/icons-material';

const LogsTable = ({logs, rowsPerPage: defaultRowsPerPage = 10}) => {
    const [page, setPage] = useState(0);
    const [rowsPerPage, setRowsPerPage] = useState(defaultRowsPerPage);

    const getLogIcon = (type) => {
        switch (type) {
            case 'INFO':
                return <Info color="info"/>;
            case 'WARNING':
                return <Warning color="warning"/>;
            case 'ERROR':
                return <Error color="error"/>;
            default:
                return <Info color="info"/>;
        }
    };

    const handleChangePage = (event, newPage) => {
        setPage(newPage);
    };

    const handleChangeRowsPerPage = (event) => {
        setRowsPerPage(parseInt(event.target.value, 10));
        setPage(0);
    };

    return (
        <Paper>
            <TableContainer>
                <Table size="small">
                    <TableHead>
                        <TableRow>
                            <TableCell>Time</TableCell>
                            <TableCell>Message</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {logs
                            .slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)
                            .map((log, index) => (
                                <TableRow key={index}>
                                    <TableCell style={{whiteSpace: 'nowrap'}}>
                                        <div style={{display: 'flex', alignItems: 'center'}}>
                                            {getLogIcon(log.type)}
                                            <span style={{marginLeft: '8px'}}>{log.timestamp}</span>
                                        </div>
                                    </TableCell>
                                    <TableCell style={{maxWidth: '300px', overflow: 'hidden', textOverflow: 'ellipsis'}}>
                                        {log.message}
                                    </TableCell>
                                </TableRow>
                            ))}
                    </TableBody>
                </Table>
            </TableContainer>
            <TablePagination
                rowsPerPageOptions={[5, 10, 25]}
                component="div"
                count={logs.length}
                rowsPerPage={rowsPerPage}
                page={page}
                onPageChange={handleChangePage}
                onRowsPerPageChange={handleChangeRowsPerPage}
            />
        </Paper>
    );
};

export default LogsTable;