import React from 'react';
import {Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle} from "../ui/dialog";
import {Button} from "../ui/button";
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from "../ui/table";
import {ScrollArea} from "../ui/scroll-area";

const ConfigDialog = ({open, onOpenChange, selectedTask}) => {
    const {config} = selectedTask || {};

    const formatDuration = (seconds) => {
        const hours = Math.floor(seconds / 3600);
        const minutes = Math.floor((seconds % 3600) / 60);
        const remainingSeconds = seconds % 60;
        const parts = [];
        if (hours > 0) parts.push(`${hours}h`);
        if (minutes > 0) parts.push(`${minutes}m`);
        if (remainingSeconds > 0 || parts.length === 0) parts.push(`${remainingSeconds}s`);
        return parts.join(' ');
    };

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent className="sm:max-w-[800px]">
                <DialogHeader>
                    <DialogTitle>Optimisation Configuration</DialogTitle>
                </DialogHeader>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <div>
                        <h3 className="text-lg font-semibold mb-2">General Settings</h3>
                        <dl className="space-y-1">
                            <div>
                                <dt className="font-medium inline">Strategy Class:</dt>
                                <dd className="inline ml-1">{config?.strategyClass}</dd>
                            </div>
                            <div>
                                <dt className="font-medium inline">Instrument:</dt>
                                <dd className="inline ml-1">{config?.instrument}</dd>
                            </div>
                            <div>
                                <dt className="font-medium inline">Period:</dt>
                                <dd className="inline ml-1">{formatDuration(config?.period)}</dd>
                            </div>
                            <div>
                                <dt className="font-medium inline">Spread:</dt>
                                <dd className="inline ml-1">{config?.spread}</dd>
                            </div>
                            <div>
                                <dt className="font-medium inline">Speed:</dt>
                                <dd className="inline ml-1">{config?.speed}</dd>
                            </div>
                            <div>
                                <dt className="font-medium inline">Initial Cash:</dt>
                                <dd className="inline ml-1">{config?.initialCash}</dd>
                            </div>
                        </dl>
                    </div>
                    <div>
                        <h3 className="text-lg font-semibold mb-2">Timeframe</h3>
                        <dl className="space-y-1">
                            <div>
                                <dt className="font-medium inline">From:</dt>
                                <dd className="inline ml-1">{config?.timeframe?.from}</dd>
                            </div>
                            <div>
                                <dt className="font-medium inline">To:</dt>
                                <dd className="inline ml-1">{config?.timeframe?.to}</dd>
                            </div>
                        </dl>
                    </div>
                </div>
                <h3 className="text-lg font-semibold mt-6 mb-2">Parameter Ranges</h3>
                <ScrollArea className="h-[300px] rounded-md border">
                    <Table>
                        <TableHeader>
                            <TableRow>
                                <TableHead>Name</TableHead>
                                <TableHead className="text-right">Value</TableHead>
                                <TableHead className="text-right">Start</TableHead>
                                <TableHead className="text-right">End</TableHead>
                                <TableHead className="text-right">Step</TableHead>
                                <TableHead className="text-center">Selected</TableHead>
                            </TableRow>
                        </TableHeader>
                        <TableBody>
                            {config?.parameterRanges.map((param) => (
                                <TableRow key={param.name}>
                                    <TableCell className="font-medium">{param.name}</TableCell>
                                    <TableCell className="text-right">{param.value}</TableCell>
                                    <TableCell className="text-right">{param.start}</TableCell>
                                    <TableCell className="text-right">{param.end}</TableCell>
                                    <TableCell className="text-right">{param.step}</TableCell>
                                    <TableCell className="text-center">
                                        {param.selected ? 'Yes' : 'No'}
                                    </TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                </ScrollArea>
                <DialogFooter>
                    <Button onClick={() => onOpenChange(false)}>Close</Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
};

export default ConfigDialog;