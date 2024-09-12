import React, {useEffect, useState} from 'react';
import {Button} from '@/components/ui/button';
import {Checkbox} from '@/components/ui/checkbox';
import {Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle,} from '@/components/ui/dialog';
import {Input} from '@/components/ui/input';
import {Label} from '@/components/ui/label';
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue,} from '@/components/ui/select';
import {Tabs, TabsContent, TabsList, TabsTrigger} from '@/components/ui/tabs';
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow,} from '@/components/ui/table';
import {Tooltip, TooltipContent, TooltipProvider, TooltipTrigger} from '@/components/ui/tooltip';
import {Textarea} from '@/components/ui/textarea';
import {CalendarIcon, InfoIcon} from 'lucide-react';
import {apiClient} from '@/api/apiClient.js';
import log from '../../logger.js';
import {useToast} from '@/hooks/use-toast';
import {ScrollArea} from '@/components/ui/scroll-area';
import {Calendar} from '@/components/ui/calendar';
import {Popover, PopoverContent, PopoverTrigger} from '@/components/ui/popover';
import {format, parseISO} from 'date-fns';

const JsonImportDialog = ({open, onClose, onImport}) => {
    const [jsonInput, setJsonInput] = useState('');
    const {toast} = useToast();

    const handleImport = () => {
        try {
            const importedConfig = JSON.parse(jsonInput);
            onImport(importedConfig);
            onClose();
        } catch (error) {
            log.error('Failed to parse imported JSON', error);
            toast({
                title: 'Import Error',
                description: 'Failed to parse the imported JSON. Please check your input.',
                variant: 'destructive',
            });
        }
    };

    return (
        <Dialog open={open} onOpenChange={onClose}>
            <DialogContent className="sm:max-w-[425px]">
                <DialogHeader>
                    <DialogTitle>Import JSON Configuration</DialogTitle>
                </DialogHeader>
                <div className="grid gap-4 py-4">
                    <div className="grid gap-2">
                        <Label htmlFor="json-input">JSON Configuration</Label>
                        <Textarea
                            id="json-input"
                            value={jsonInput}
                            onChange={(e) => setJsonInput(e.target.value)}
                            placeholder="Paste your JSON configuration here"
                            className="min-h-[200px]"
                        />
                    </div>
                </div>
                <DialogFooter>
                    <Button variant="outline" onClick={onClose}>
                        Cancel
                    </Button>
                    <Button onClick={handleImport}>Import</Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
};

const ConfigModal = ({
                         open,
                         onClose,
                         strategyConfig,
                         setStrategyConfig,
                         strategyClass,
                         showOptimiseParams = false,
                     }) => {
    const [activeTab, setActiveTab] = useState('parameters');
    const [activeGroup, setActiveGroup] = useState('');
    const [localConfig, setLocalConfig] = useState(strategyConfig);
    const [instruments, setInstruments] = useState([]);
    const [isJsonImportOpen, setIsJsonImportOpen] = useState(false);
    const {toast} = useToast();

    useEffect(() => {
        if (open) {
            log.debug('Config', strategyConfig);
            setLocalConfig(strategyConfig);
            const groups = Object.keys(groupParams(strategyConfig.runParams));
            setActiveGroup(groups[0] || '');
        }
    }, [open, strategyClass]);

    const groupParams = (params) => {
        return params.reduce((groups, param) => {
            const group = param.group || 'Ungrouped';
            if (!groups[group]) groups[group] = [];
            groups[group].push(param);
            return groups;
        }, {});
    };

    useEffect(() => {
        // Get all supported instruments
        const fetchInstruments = async () => {
            try {
                const inst = await apiClient.getInstruments();
                setInstruments(inst);
            } catch (e) {
                log.error('Failed to fetch instruments');
            }
        };

        fetchInstruments();
    }, []);

    const handleDateChange = (field, date) => {
        setLocalConfig((prevConfig) => ({
            ...prevConfig,
            timeframe: {
                ...prevConfig.timeframe,
                [field]: date ? format(date, "yyyy-MM-dd'T'HH:mm:ss'Z'") : '',
            },
        }));
    };

    const validateJsonImport = (json) => {
        // TODO: We should validate the json we are letting users passed in
        // However, until we refactor some of this frontend. We will not do this.
        // At the moment the json works for data from the DB, if the user changes something in the json
        // it will not be handled.
    };

    const handleJsonImport = (importedConfig) => {
        try {
            validateJsonImport(importedConfig);
        } catch (error) {
            toast({
                title: 'Import Error',
                description: error,
                variant: 'destructive',
            });
        }

        setLocalConfig((prevConfig) => {
            let updatedConfig = {...prevConfig};

            log.debug('imported config: ', importedConfig);
            log.debug('params config: ', updatedConfig.runParams);

            if (importedConfig) {
                updatedConfig.runParams = updatedConfig.runParams.map((param) => {
                    if (importedConfig.hasOwnProperty(param.name)) {
                        log.debug(
                            `Updating ${param.name} from ${param.value} to ${importedConfig[param.name]}`
                        );
                        return {...param, value: importedConfig[param.name]};
                    }
                    return param;
                });
            }

            // Update other fields that are not in runParams
            Object.keys(importedConfig).forEach((key) => {
                if (
                    !updatedConfig.runParams.some((param) => param.name === key) &&
                    updatedConfig.hasOwnProperty(key)
                ) {
                    log.debug(
                        `Updating ${key} from ${updatedConfig[key]} to ${importedConfig[key]}`
                    );
                    updatedConfig[key] = importedConfig[key];
                }
            });

            log.debug('Previous config:', prevConfig);
            log.debug('Imported configuration:', importedConfig);
            log.debug('Updated configuration:', updatedConfig);

            // Force a re-render by creating a new object
            return {...updatedConfig};
        });

        // Set a timeout to log the updated localConfig after the state has been updated
        setTimeout(() => {
            log.debug('LocalConfig after update:', localConfig);
        }, 0);

        toast({
            title: 'Configuration imported',
            description: 'Your configuration has been successfully imported.',
        });
    };

    const saveToLocalStorage = () => {
        localStorage.setItem(`strategyConfig_${strategyClass}`, JSON.stringify(localConfig));
    };

    const handleInputChange = (index, field, value) => {
        setLocalConfig((prev) => {
            const updatedRunParams = [...prev.runParams];
            updatedRunParams[index] = {...updatedRunParams[index], [field]: value};
            const newConfig = {...prev, runParams: updatedRunParams};
            log.debug('Updated localConfig:', newConfig);
            return newConfig;
        });
    };

    const handleConfigChange = (field, value) => {
        setLocalConfig((prev) => ({
            ...prev,
            [field]: value,
        }));
    };

    const handleReset = () => {
        setLocalConfig(strategyConfig);
    };

    const handleClose = () => {
        saveToLocalStorage();
        log.debug(localConfig);
        setStrategyConfig(localConfig);
        onClose();
    };

    const groupedParams = groupParams(localConfig.runParams);

    return (
        <Dialog
            open={open}
            onOpenChange={(openState) => {
                if (!openState) {
                    handleClose();
                }
            }}
        >
            <DialogContent className="w-full max-w-[1100px] max-h-[80vh] overflow-hidden flex flex-col">
                <DialogHeader>
                    <DialogTitle className="flex items-center">
                        {showOptimiseParams
                            ? 'Optimisation Configuration'
                            : 'Strategy Configuration'}
                    </DialogTitle>
                </DialogHeader>
                <Tabs
                    value={activeTab}
                    onValueChange={setActiveTab}
                    className="flex-grow overflow-hidden"
                >
                    <TabsList className="grid w-full grid-cols-2">
                        <TabsTrigger value="parameters">Parameters</TabsTrigger>
                        <TabsTrigger value="run-config">Run Configuration</TabsTrigger>
                    </TabsList>
                    <TabsContent value="parameters" className="flex-grow overflow-hidden">
                        <div className="flex h-full">
                            <div className="w-1/3 md:w-1/4 border-r">
                                <ScrollArea className="h-full">
                                    {Object.keys(groupedParams).map((group) => (
                                        <Button
                                            key={group}
                                            variant={activeGroup === group ? 'secondary' : 'ghost'}
                                            className="w-full justify-start"
                                            onClick={() => setActiveGroup(group)}
                                        >
                                            {group}
                                        </Button>
                                    ))}
                                </ScrollArea>
                            </div>
                            <div className="w-2/3 md:w-3/4 pl-4">
                                <ScrollArea className="h-full pr-4">
                                    <Table>
                                        <TableHeader>
                                            <TableRow>
                                                <TableHead>Parameter</TableHead>
                                                <TableHead>Value</TableHead>
                                                {showOptimiseParams && (
                                                    <>
                                                        <TableHead>Start</TableHead>
                                                        <TableHead>Stop</TableHead>
                                                        <TableHead>Step</TableHead>
                                                        <TableHead>Optimize</TableHead>
                                                    </>
                                                )}
                                            </TableRow>
                                        </TableHeader>
                                        <TableBody>
                                            {groupedParams[activeGroup]?.map((param) => (
                                                <TableRow key={param.name}>
                                                    <TableCell>
                                                        <div className="flex items-center space-x-2">
                                                            <Label>{param.name}</Label>
                                                            <TooltipProvider>
                                                                <Tooltip>
                                                                    <TooltipTrigger asChild>
                                                                        <Button
                                                                            variant="ghost"
                                                                            size="icon"
                                                                            className="h-8 w-8 p-0"
                                                                        >
                                                                            <InfoIcon className="h-4 w-4"/>
                                                                            <span className="sr-only">
                                                                                Info
                                                                            </span>
                                                                        </Button>
                                                                    </TooltipTrigger>
                                                                    <TooltipContent>
                                                                        <p>
                                                                            {param.description ||
                                                                                'No description available'}
                                                                        </p>
                                                                    </TooltipContent>
                                                                </Tooltip>
                                                            </TooltipProvider>
                                                        </div>
                                                    </TableCell>
                                                    <TableCell>
                                                        <Input
                                                            value={param.value}
                                                            onChange={(e) =>
                                                                handleInputChange(
                                                                    localConfig.runParams.indexOf(
                                                                        param
                                                                    ),
                                                                    'value',
                                                                    e.target.value
                                                                )
                                                            }
                                                            autoComplete="off"
                                                        />
                                                    </TableCell>
                                                    {showOptimiseParams && (
                                                        <>
                                                            <TableCell>
                                                                <Input
                                                                    value={param.start || ''}
                                                                    onChange={(e) =>
                                                                        handleInputChange(
                                                                            localConfig.runParams.indexOf(
                                                                                param
                                                                            ),
                                                                            'start',
                                                                            e.target.value
                                                                        )
                                                                    }
                                                                    autoComplete="off"
                                                                />
                                                            </TableCell>
                                                            <TableCell>
                                                                <Input
                                                                    value={param.stop || ''}
                                                                    onChange={(e) =>
                                                                        handleInputChange(
                                                                            localConfig.runParams.indexOf(
                                                                                param
                                                                            ),
                                                                            'stop',
                                                                            e.target.value
                                                                        )
                                                                    }
                                                                    autoComplete="off"
                                                                />
                                                            </TableCell>
                                                            <TableCell>
                                                                <Input
                                                                    value={param.step || ''}
                                                                    onChange={(e) =>
                                                                        handleInputChange(
                                                                            localConfig.runParams.indexOf(
                                                                                param
                                                                            ),
                                                                            'step',
                                                                            e.target.value
                                                                        )
                                                                    }
                                                                    autoComplete="off"
                                                                />
                                                            </TableCell>
                                                            <TableCell>
                                                                <Checkbox
                                                                    checked={
                                                                        param.selected || false
                                                                    }
                                                                    onCheckedChange={(checked) =>
                                                                        handleInputChange(
                                                                            localConfig.runParams.indexOf(
                                                                                param
                                                                            ),
                                                                            'selected',
                                                                            checked
                                                                        )
                                                                    }
                                                                />
                                                            </TableCell>
                                                        </>
                                                    )}
                                                </TableRow>
                                            ))}
                                        </TableBody>
                                    </Table>
                                </ScrollArea>
                            </div>
                        </div>
                    </TabsContent>
                    <TabsContent value="run-config" className="flex-grow overflow-hidden">
                        <ScrollArea className="h-full pr-4">
                            <div className="grid grid-cols-2 gap-4">
                                <div className="space-y-2">
                                    <Label htmlFor="initial-cash">Initial Cash</Label>
                                    <Input
                                        id="initial-cash"
                                        value={localConfig.initialCash}
                                        onChange={(e) =>
                                            handleConfigChange('initialCash', e.target.value)
                                        }
                                        type="number"
                                    />
                                </div>

                                {!showOptimiseParams && (
                                    <div className="space-y-2">
                                        <Label htmlFor="speed">Speed</Label>
                                        <Select
                                            value={localConfig.speed}
                                            onValueChange={(value) =>
                                                handleConfigChange('speed', value)
                                            }
                                        >
                                            <SelectTrigger id="speed">
                                                <SelectValue placeholder="Select speed"/>
                                            </SelectTrigger>
                                            <SelectContent>
                                                <SelectItem value="SLOW">Slow (Visual)</SelectItem>
                                                <SelectItem value="NORMAL">
                                                    Normal (Visual)
                                                </SelectItem>
                                                <SelectItem value="INSTANT">
                                                    Instant (Async)
                                                </SelectItem>
                                            </SelectContent>
                                        </Select>
                                    </div>
                                )}

                                <div className="space-y-2">
                                    <Label htmlFor="spread">Spread</Label>
                                    <Select
                                        value={localConfig.spread}
                                        onValueChange={(value) =>
                                            handleConfigChange('spread', value)
                                        }
                                    >
                                        <SelectTrigger id="spread">
                                            <SelectValue placeholder="Select spread"/>
                                        </SelectTrigger>
                                        <SelectContent>
                                            <SelectItem value="5">5</SelectItem>
                                            <SelectItem value="10">10</SelectItem>
                                            <SelectItem value="30">30</SelectItem>
                                            <SelectItem value="50">50</SelectItem>
                                            <SelectItem value="100">100</SelectItem>
                                        </SelectContent>
                                    </Select>
                                </div>

                                <div className="space-y-2">
                                    <Label htmlFor="instrument">Instrument</Label>
                                    <Select
                                        value={localConfig.instrumentData.internalSymbol || ''}
                                        onValueChange={(value) => {
                                            const selectedInstrument = instruments.find(
                                                (i) => i.internalSymbol === value
                                            );
                                            handleConfigChange(
                                                'instrumentData',
                                                selectedInstrument
                                            );
                                        }}
                                    >
                                        <SelectTrigger id="instrument">
                                            <SelectValue placeholder="Select instrument"/>
                                        </SelectTrigger>
                                        <SelectContent>
                                            {instruments.length > 0 ? (
                                                instruments.map((instrument, index) => (
                                                    <SelectItem
                                                        key={index}
                                                        value={instrument.internalSymbol}
                                                    >
                                                        {instrument.internalSymbol}
                                                    </SelectItem>
                                                ))
                                            ) : (
                                                <SelectItem value="" disabled>
                                                    No Instruments available
                                                </SelectItem>
                                            )}
                                        </SelectContent>
                                    </Select>
                                </div>

                                <div className="space-y-2">
                                    <Label htmlFor="period">Period</Label>
                                    <Select
                                        value={localConfig.period}
                                        onValueChange={(value) =>
                                            handleConfigChange('period', value)
                                        }
                                    >
                                        <SelectTrigger id="period">
                                            <SelectValue placeholder="Select period"/>
                                        </SelectTrigger>
                                        <SelectContent>
                                            <SelectItem value="M1">1m</SelectItem>
                                            <SelectItem value="M5">5m</SelectItem>
                                            <SelectItem value="M15">15m</SelectItem>
                                            <SelectItem value="M30">30m</SelectItem>
                                            <SelectItem value="H1">1H</SelectItem>
                                            <SelectItem value="H4">4H</SelectItem>
                                            <SelectItem value="D">1D</SelectItem>
                                        </SelectContent>
                                    </Select>
                                </div>

                                <div className="space-y-2">
                                    <Label htmlFor="from-date">From</Label>
                                    <Popover>
                                        <PopoverTrigger asChild>
                                            <Button
                                                variant="outline"
                                                className="w-full justify-start text-left font-normal"
                                                id="from-date"
                                            >
                                                <CalendarIcon className="mr-2 h-4 w-4"/>
                                                {localConfig.timeframe.from ? (
                                                    format(
                                                        parseISO(localConfig.timeframe.from),
                                                        'PPP'
                                                    )
                                                ) : (
                                                    <span>Pick a date</span>
                                                )}
                                            </Button>
                                        </PopoverTrigger>
                                        <PopoverContent className="w-auto p-0">
                                            <Calendar
                                                mode="single"
                                                selected={
                                                    localConfig.timeframe.from
                                                        ? parseISO(localConfig.timeframe.from)
                                                        : undefined
                                                }
                                                onSelect={(date) => handleDateChange('from', date)}
                                                initialFocus
                                            />
                                        </PopoverContent>
                                    </Popover>
                                </div>

                                <div className="space-y-2">
                                    <Label htmlFor="to-date">To</Label>
                                    <Popover>
                                        <PopoverTrigger asChild>
                                            <Button
                                                variant="outline"
                                                className="w-full justify-start text-left font-normal"
                                                id="to-date"
                                            >
                                                <CalendarIcon className="mr-2 h-4 w-4"/>
                                                {localConfig.timeframe.to ? (
                                                    format(
                                                        parseISO(localConfig.timeframe.to),
                                                        'PPP'
                                                    )
                                                ) : (
                                                    <span>Pick a date</span>
                                                )}
                                            </Button>
                                        </PopoverTrigger>
                                        <PopoverContent className="w-auto p-0">
                                            <Calendar
                                                mode="single"
                                                selected={
                                                    localConfig.timeframe.to
                                                        ? parseISO(localConfig.timeframe.to)
                                                        : undefined
                                                }
                                                onSelect={(date) => handleDateChange('to', date)}
                                                initialFocus
                                            />
                                        </PopoverContent>
                                    </Popover>
                                </div>
                            </div>
                        </ScrollArea>
                    </TabsContent>
                </Tabs>
                <DialogFooter>
                    {!showOptimiseParams && (
                        <Button variant="outline" onClick={() => setIsJsonImportOpen(true)}>
                            Import JSON
                        </Button>
                    )}
                    <Button variant="outline" onClick={handleReset}>
                        Reset
                    </Button>
                    <Button onClick={handleClose}>Close</Button>
                </DialogFooter>
            </DialogContent>
            <JsonImportDialog
                open={isJsonImportOpen}
                onClose={() => setIsJsonImportOpen(false)}
                onImport={handleJsonImport}
            />
        </Dialog>
    );
};

export default ConfigModal;