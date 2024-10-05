import React, {useState} from 'react';
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from "@/components/ui/table";
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from "@/components/ui/select";
import {Button} from "@/components/ui/button";
import {Tabs, TabsContent, TabsList, TabsTrigger} from "@/components/ui/tabs";
import {formatUTCDate} from '@/utils/dateUtils';
import {Tooltip, TooltipContent, TooltipProvider, TooltipTrigger} from "@/components/ui/tooltip.jsx";
import {X} from "lucide-react";
import {liveStrategyClient} from "@/api/liveClient.js";
import log from "@/logger.js";
import {useToast} from "@/hooks/use-toast.js";


function CustomTablePagination({count, page, rowsPerPage, onPageChange, onRowsPerPageChange}) {
    const totalPages = Math.ceil(count / rowsPerPage);

    const handlePageChange = (value) => {
        const newPage = parseInt(value, 10) - 1; // Convert to 0-based index
        onPageChange(null, newPage);
    };

    return (
        <div className="flex items-center justify-between p-4">
            <div className="flex items-center space-x-2">
                <p className="text-sm text-gray-700">
                    Rows per page:
                </p>
                <Select
                    value={rowsPerPage.toString()}
                    onValueChange={(value) => onRowsPerPageChange({target: {value}})}
                >
                    <SelectTrigger className="w-[70px]">
                        <SelectValue/>
                    </SelectTrigger>
                    <SelectContent>
                        {[5, 10, 25, 50].map((option) => (
                            <SelectItem key={option} value={option.toString()}>
                                {option}
                            </SelectItem>
                        ))}
                    </SelectContent>
                </Select>
            </div>
            <div className="flex items-center space-x-2">
                <Button
                    variant="outline"
                    size="sm"
                    onClick={() => onPageChange(null, page - 1)}
                    disabled={page === 0}
                >
                    Previous
                </Button>
                <Button
                    variant="outline"
                    size="sm"
                    onClick={() => onPageChange(null, page + 1)}
                    disabled={page >= totalPages - 1}
                >
                    Next
                </Button>
                <Select
                    value={(page + 1).toString()}
                    onValueChange={handlePageChange}
                >
                    <SelectTrigger className="w-[100px]">
                        <SelectValue placeholder="Go to page"/>
                    </SelectTrigger>
                    <SelectContent>
                        {[...Array(totalPages)].map((_, index) => (
                            <SelectItem key={index} value={(index + 1).toString()}>
                                Page {index + 1}
                            </SelectItem>
                        ))}
                    </SelectContent>
                </Select>
            </div>
        </div>
    );
}

function TradesTable({trades, strategy = null, useLiveSplit = false}) {
    const [openPage, setOpenPage] = useState(0);
    const [closedPage, setClosedPage] = useState(0);
    const [openRowsPerPage, setOpenRowsPerPage] = useState(10);
    const [closedRowsPerPage, setClosedRowsPerPage] = useState(10);
    const [activeTab, setActiveTab] = useState("open");
    const {toast} = useToast();

    const handleChangePage = (tabType) => (event, newPage) => {
        if (tabType === 'open') {
            setOpenPage(newPage);
        } else {
            setClosedPage(newPage);
        }
    };

    const handleChangeRowsPerPage = (tabType) => (event) => {
        const newRowsPerPage = parseInt(event.target.value, 10);
        if (tabType === 'open') {
            setOpenRowsPerPage(newRowsPerPage);
            setOpenPage(0);
        } else {
            setClosedRowsPerPage(newRowsPerPage);
            setClosedPage(0);
        }
    };

    if (trades.length === 0) {
        return (
            <div className="p-4 text-center">
                <p>No trade data available yet.</p>
            </div>
        );
    }

    const handleCloseTrade = async (strategy, tradeId) => {

        try {
            await liveStrategyClient.closeTrade(strategy.id, tradeId);
            toast({
                title: 'Success',
                description: 'Trade closed successfully',
            });
        } catch (error) {
            log.error('Failed to close trade', error);
            toast({
                title: 'Error',
                description: `Failed to close trade: ${error.message}`,
                variant: 'destructive',
            });
        }
    }

    const openTrades = trades.filter(trade => !trade.closeTime && (!trade.closePrice || trade.closePrice === 0));
    const closedTrades = trades.filter(trade => trade.closeTime || (trade.closePrice && trade.closePrice !== 0));

    const renderTable = (tradesToRender, page, rowsPerPage, onPageChange, onRowsPerPageChange, showActions = false) => (
        <div className="rounded-md border">
            <Table>
                <TableHeader>
                    <TableRow>
                        <TableHead>Order #</TableHead>
                        <TableHead>Time</TableHead>
                        <TableHead>Type</TableHead>
                        <TableHead>Closed</TableHead>
                        <TableHead>Quantity</TableHead>
                        <TableHead>Instrument</TableHead>
                        <TableHead>Open</TableHead>
                        <TableHead>Close</TableHead>
                        <TableHead>S/L</TableHead>
                        <TableHead>T/P</TableHead>
                        <TableHead>Profit</TableHead>
                        {showActions && strategy != null && (
                            <TableHead>Actions</TableHead>
                        )}
                    </TableRow>
                </TableHeader>
                <TableBody>
                    {tradesToRender
                        .slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)
                        .map((trade) => (
                            <TableRow key={trade.id}>
                                <TableCell>{trade.tradeId}</TableCell>
                                <TableCell>{formatUTCDate(trade.openTime)}</TableCell>
                                <TableCell>{trade.isLong ? "LONG" : "SHORT"}</TableCell>
                                <TableCell>{trade.closeTime ? formatUTCDate(trade.closeTime) : ""}</TableCell>
                                <TableCell>{trade.quantity}</TableCell>
                                <TableCell>{trade.instrument}</TableCell>
                                <TableCell>{trade.entry}</TableCell>
                                <TableCell>{trade.closePrice !== 0 ? trade.closePrice : ""}</TableCell>
                                <TableCell>{trade.stopLoss !== 0 ? trade.stopLoss : ""}</TableCell>
                                <TableCell>{trade.takeProfit !== 0 ? trade.takeProfit : ""}</TableCell>
                                <TableCell>{trade.profit ? trade.profit : "0.00"}</TableCell>
                                {showActions && strategy != null && (
                                    <TableCell>
                                        <TooltipProvider>
                                            <Tooltip>
                                                <TooltipTrigger asChild>
                                                    <Button variant="ghost" size="sm" onClick={(e) => {
                                                        handleCloseTrade(strategy, trade.id)
                                                    }}>
                                                        <X className="h-4 w-4"/>
                                                    </Button>
                                                </TooltipTrigger>
                                                <TooltipContent>
                                                    <p>Close trade</p>
                                                </TooltipContent>
                                            </Tooltip>
                                        </TooltipProvider>
                                    </TableCell>
                                )}
                            </TableRow>
                        ))}
                </TableBody>
            </Table>
            <CustomTablePagination
                count={tradesToRender.length}
                rowsPerPage={rowsPerPage}
                page={page}
                onPageChange={onPageChange}
                onRowsPerPageChange={onRowsPerPageChange}
            />
        </div>
    );

    if (useLiveSplit) {
        return (
            <Tabs value={activeTab} onValueChange={setActiveTab}>
                <TabsList>
                    <TabsTrigger value="open">Open Trades</TabsTrigger>
                    <TabsTrigger value="closed">Closed Trades</TabsTrigger>
                </TabsList>
                <TabsContent value="open">
                    {renderTable(
                        openTrades,
                        openPage,
                        openRowsPerPage,
                        handleChangePage('open'),
                        handleChangeRowsPerPage('open'),
                        true
                    )}
                </TabsContent>
                <TabsContent value="closed">
                    {renderTable(
                        closedTrades,
                        closedPage,
                        closedRowsPerPage,
                        handleChangePage('closed'),
                        handleChangeRowsPerPage('closed')
                    )}
                </TabsContent>
            </Tabs>
        );
    } else {
        return renderTable(
            trades,
            openPage,
            openRowsPerPage,
            handleChangePage('open'),
            handleChangeRowsPerPage('open')
        );
    }
}

export default TradesTable;