import React, {useState} from 'react';
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow,} from "@/components/ui/table";
import {Pagination, PaginationContent, PaginationItem, PaginationLink, PaginationNext, PaginationPrevious,} from "@/components/ui/pagination";
import {Card} from "@/components/ui/card";
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from "@/components/ui/select";
import {AlertCircleIcon, AlertTriangleIcon, InfoIcon} from "lucide-react";

const LogsTable = ({logs, rowsPerPage: defaultRowsPerPage = 10}) => {
    const [page, setPage] = useState(0);
    const [rowsPerPage, setRowsPerPage] = useState(defaultRowsPerPage);

    const getLogIcon = (type) => {
        switch (type) {
            case 'INFO':
                return <InfoIcon className="h-4 w-4 text-blue-500"/>;
            case 'WARN':
                return <AlertTriangleIcon className="h-4 w-4 text-yellow-500"/>;
            case 'ERROR':
                return <AlertCircleIcon className="h-4 w-4 text-red-500"/>;
            default:
                return <InfoIcon className="h-4 w-4 text-blue-500"/>;
        }
    };

    const handleChangePage = (newPage) => {
        setPage(newPage);
    };

    const handleChangeRowsPerPage = (value) => {
        setRowsPerPage(parseInt(value, 10));
        setPage(0);
    };

    const totalPages = Math.ceil(logs.length / rowsPerPage);

    return (
        <Card>
            <Table>
                <TableHeader>
                    <TableRow>
                        <TableHead>Time</TableHead>
                        <TableHead>Message</TableHead>
                    </TableRow>
                </TableHeader>
                <TableBody>
                    {logs
                        .slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)
                        .map((log, index) => (
                            <TableRow key={index}>
                                <TableCell className="whitespace-nowrap">
                                    <div className="flex items-center">
                                        {getLogIcon(log.type)}
                                        <span className="ml-2">{log.timestamp}</span>
                                    </div>
                                </TableCell>
                                <TableCell className="max-w-xs truncate">
                                    {log.message}
                                </TableCell>
                            </TableRow>
                        ))}
                </TableBody>
            </Table>
            <div className="flex items-center justify-between px-2">
                <Pagination className="p-2">
                    <PaginationContent>
                        <PaginationItem>
                            <PaginationPrevious onClick={() => handleChangePage(page - 1)} disabled={page === 0}/>
                        </PaginationItem>
                        {[...Array(totalPages)].map((_, i) => (
                            <PaginationItem key={i}>
                                <PaginationLink onClick={() => handleChangePage(i)} isActive={page === i}>
                                    {i + 1}
                                </PaginationLink>
                            </PaginationItem>
                        ))}
                        <PaginationItem>
                            <PaginationNext onClick={() => handleChangePage(page + 1)} disabled={page === totalPages - 1}/>
                        </PaginationItem>
                    </PaginationContent>
                </Pagination>
                <div className="flex items-center space-x-2">
                    <Select value={rowsPerPage.toString()} onValueChange={handleChangeRowsPerPage}>
                        <SelectTrigger className="w-[70px]">
                            <SelectValue placeholder={rowsPerPage}/>
                        </SelectTrigger>
                        <SelectContent>
                            {[5, 10, 25].map((option) => (
                                <SelectItem key={option} value={option.toString()}>
                                    {option}
                                </SelectItem>
                            ))}
                        </SelectContent>
                    </Select>
                </div>
            </div>
        </Card>
    );
};

export default LogsTable;