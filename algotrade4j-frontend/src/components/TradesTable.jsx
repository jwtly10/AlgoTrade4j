import React, {useState} from 'react';
import {Box, FormControl, InputLabel, MenuItem, Paper, Select, Table, TableBody, TableCell, TableContainer, TableHead, TablePagination, TableRow, Typography} from '@mui/material';

function CustomTablePagination(props) {
    const totalPages = Math.ceil(props.count / props.rowsPerPage);

    const handlePageChange = (event) => {
        const newPage = parseInt(event.target.value, 10) - 1; // Convert to 0-based index
        props.onPageChange(null, newPage);
    };

    return (
        <Box display="flex" alignItems="center" justifyContent="space-between" p={2}>
            <TablePagination {...props} />
            <FormControl sx={{minWidth: 120}} size="small">
                <InputLabel id="page-select-label">Go to Page</InputLabel>
                <Select
                    labelId="page-select-label"
                    value={props.page + 1} // Convert to 1-based index for display
                    onChange={handlePageChange}
                    label="Go to Page"
                >
                    {[...Array(totalPages)].map((_, index) => (
                        <MenuItem key={index} value={index + 1}>
                            {index + 1}
                        </MenuItem>
                    ))}
                </Select>
            </FormControl>
        </Box>
    );
}

function TradesTable({trades}) {
    const [page, setPage] = useState(0);
    const [rowsPerPage, setRowsPerPage] = useState(50);

    const handleChangePage = (event, newPage) => {
        setPage(newPage);
    };

    const handleChangeRowsPerPage = (event) => {
        setRowsPerPage(parseInt(event.target.value, 10));
        setPage(0);
    };

    if (trades.length === 0) {
        return (
            <TableContainer component={Paper}>
                <Typography variant="body1" sx={{p: 2, textAlign: 'center'}}>
                    No trade data available yet.
                </Typography>
            </TableContainer>
        );
    }

    return (
        <Paper>
            <TableContainer>
                <Table sx={{minWidth: 650}} aria-label="trades table">
                    <TableHead>
                        <TableRow>
                            <TableCell>Order #</TableCell>
                            <TableCell>Time</TableCell>
                            <TableCell>Type</TableCell>
                            <TableCell>Closed</TableCell>
                            <TableCell>Quantity</TableCell>
                            <TableCell>Instrument</TableCell>
                            <TableCell>Open</TableCell>
                            <TableCell>Close</TableCell>
                            <TableCell>S/L</TableCell>
                            <TableCell>T/P</TableCell>
                            <TableCell>Profit</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {trades
                            .slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)
                            .map((trade) => (
                                <TableRow key={trade.id}>
                                    <TableCell>{trade.tradeId}</TableCell>
                                    <TableCell>{new Date(trade.openTime * 1000).toLocaleString()}</TableCell>
                                    <TableCell>{trade.isLong ? "LONG" : "SHORT"}</TableCell>
                                    <TableCell>{trade.closeTime ? new Date(trade.closeTime * 1000).toLocaleString() : "N/A"}</TableCell>
                                    <TableCell>{trade.quantity}</TableCell>
                                    <TableCell>{trade.instrument}</TableCell>
                                    <TableCell>{trade.entry}</TableCell>
                                    <TableCell>{trade.closePrice}</TableCell>
                                    <TableCell>{trade.stopLoss}</TableCell>
                                    <TableCell>{trade.takeProfit}</TableCell>
                                    <TableCell>${trade.profit}</TableCell>
                                </TableRow>
                            ))}
                    </TableBody>
                </Table>
            </TableContainer>
            <CustomTablePagination
                rowsPerPageOptions={[5, 10, 25, 50]}
                component="div"
                count={trades.length}
                rowsPerPage={rowsPerPage}
                page={page}
                onPageChange={handleChangePage}
                onRowsPerPageChange={handleChangeRowsPerPage}
            />
        </Paper>
    );
}

export default TradesTable;