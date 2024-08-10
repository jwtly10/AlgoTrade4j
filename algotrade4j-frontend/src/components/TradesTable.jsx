import React from 'react';
import {Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Typography} from '@mui/material';

function TradesTable({trades}) {
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
        <TableContainer component={Paper}>
            <Table sx={{minWidth: 650}} aria-label="trades table">
                <TableHead>
                    <TableRow>
                        <TableCell>Order</TableCell>
                        <TableCell>Time</TableCell>
                        <TableCell>Type</TableCell>
                        <TableCell>State</TableCell>
                        <TableCell>Size</TableCell>
                        <TableCell>Symbol</TableCell>
                        <TableCell>Open Price</TableCell>
                        <TableCell>Close Price</TableCell>
                        <TableCell>S/L</TableCell>
                        <TableCell>T/P</TableCell>
                        <TableCell>Profit</TableCell>
                    </TableRow>
                </TableHead>
                <TableBody>
                    {trades.map((trade) => (
                        <TableRow key={trade.id}>
                            <TableCell>{trade.tradeId}</TableCell>
                            <TableCell>{new Date(trade.openTime * 1000).toLocaleString()}</TableCell>
                            <TableCell>{trade.isLong ? "LONG" : "SHORT"}</TableCell>
                            <TableCell>{trade.closePrice === 0 ? "OPEN" : "CLOSED"}</TableCell>
                            <TableCell>{trade.quantity}</TableCell>
                            <TableCell>{trade.symbol}</TableCell>
                            <TableCell>{trade.entry}</TableCell>
                            <TableCell>{trade.closePrice}</TableCell>
                            <TableCell>{trade.stopLoss}</TableCell>
                            <TableCell>{trade.takeProfit}</TableCell>
                            <TableCell>{trade.profit}</TableCell>
                        </TableRow>
                    ))}
                </TableBody>
            </Table>
        </TableContainer>
    );
}

export default TradesTable;