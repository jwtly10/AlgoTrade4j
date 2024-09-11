import React, {useEffect, useState} from 'react';
import {Button} from "@/components/ui/button";
import {Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle} from "@/components/ui/dialog";
import {Input} from "@/components/ui/input";
import {Label} from "@/components/ui/label";
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from "@/components/ui/select";
import {Tabs, TabsContent, TabsList, TabsTrigger} from "@/components/ui/tabs";
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from "@/components/ui/table";
import {Tooltip, TooltipContent, TooltipProvider, TooltipTrigger} from "@/components/ui/tooltip";
import {ScrollArea} from "@/components/ui/scroll-area";
import {InfoIcon} from 'lucide-react';
import {apiClient} from '@/api/apiClient.js';

const LiveCreateStratModal = ({open, onClose, onSave, strategies}) => {
    const [activeTab, setActiveTab] = useState('parameters');
    const [activeGroup, setActiveGroup] = useState('');
    const [instruments, setInstruments] = useState([]);
    const [selectedStrategy, setSelectedStrategy] = useState('');
    const [config, setConfig] = useState({
        strategyName: '',
        config: {
            period: 'M15',
            instrumentData: {internalSymbol: ''},
            initialCash: 10000,
            strategyClass: '',
            runParams: []
        },
        brokerConfig: {
            broker: 'OANDA',
            account_id: '',
            type: 'DEMO'
        }
    });

    useEffect(() => {
        if (!open) {
            setSelectedStrategy('');
            setConfig({
                strategyName: '',
                config: {
                    period: 'M15',
                    instrumentData: {internalSymbol: ''},
                    initialCash: 10000,
                    strategyClass: '',
                    runParams: []
                },
                brokerConfig: {
                    broker: 'OANDA',
                    account_id: '',
                    type: 'DEMO'
                }
            });
            setActiveTab('parameters');
            setActiveGroup('');
        }
    }, [open]);

    useEffect(() => {
        const fetchInstruments = async () => {
            try {
                const inst = await apiClient.getInstruments();
                setInstruments(inst);
            } catch (e) {
                console.error("Failed to fetch instruments", e);
            }
        };

        fetchInstruments();
    }, []);

    useEffect(() => {
        if (selectedStrategy) {
            // Fetch strategy parameters and update config
            apiClient.getParams(selectedStrategy).then(params => {
                setConfig(prevConfig => ({
                    ...prevConfig,
                    strategyName: selectedStrategy,
                    config: {
                        ...prevConfig.config,
                        strategyClass: selectedStrategy,
                        runParams: params
                    }
                }));
                const groups = Object.keys(groupParams(params));
                setActiveGroup(groups[0] || '');
            });
        }
    }, [selectedStrategy]);

    const groupParams = (params) => {
        return params.reduce((groups, param) => {
            const group = param.group || 'Ungrouped';
            if (!groups[group]) groups[group] = [];
            groups[group].push(param);
            return groups;
        }, {});
    };

    const handleInputChange = (index, field, value) => {
        setConfig(prevConfig => {
            const updatedRunParams = [...prevConfig.config.runParams];
            updatedRunParams[index] = {...updatedRunParams[index], [field]: value};
            return {
                ...prevConfig,
                config: {
                    ...prevConfig.config,
                    runParams: updatedRunParams
                }
            };
        });
    };

    const handleConfigChange = (field, value) => {
        setConfig(prevConfig => ({
            ...prevConfig,
            config: {
                ...prevConfig.config,
                [field]: value,
            }
        }));
    };

    const handleBrokerConfigChange = (field, value) => {
        setConfig(prevConfig => ({
            ...prevConfig,
            brokerConfig: {
                ...prevConfig.brokerConfig,
                [field]: value,
            }
        }));
    };

    const groupedParams = groupParams(config.config.runParams);

    const valid = () => {
        // Check if a strategy is selected
        if (!selectedStrategy) return false;

        // Check if all run configuration fields are set
        if (!config.config.initialCash ||
            !config.config.instrumentData.internalSymbol ||
            !config.config.period) return false;

        // Check if all broker configuration fields are set
        if (!config.brokerConfig.broker ||
            !config.brokerConfig.account_id ||
            !config.brokerConfig.type) return false;

        // Check if all required parameters have values
        const allParamsSet = config.config.runParams.every(param =>
            param.value !== undefined && param.value !== '');

        if (!allParamsSet) return false;

        // If all checks pass, return true
        return true;
    }

    return (
        <Dialog open={open} onOpenChange={onClose}>
            <DialogContent className="w-full max-w-[1100px] max-h-[80vh] overflow-hidden flex flex-col">
                <DialogHeader>
                    <DialogTitle className="flex items-center mb-4">
                        Create New Live Strategy
                    </DialogTitle>
                    <Select value={selectedStrategy} onValueChange={setSelectedStrategy}>
                        <SelectTrigger>
                            <SelectValue placeholder="Select a strategy"/>
                        </SelectTrigger>
                        <SelectContent>
                            {strategies.map((strategy) => (
                                <SelectItem key={strategy} value={strategy}>
                                    {strategy}
                                </SelectItem>
                            ))}
                        </SelectContent>
                    </Select>
                </DialogHeader>
                {selectedStrategy && (
                    <Tabs value={activeTab} onValueChange={setActiveTab} className="flex-grow overflow-hidden">
                        <TabsList className="grid w-full grid-cols-3">
                            <TabsTrigger value="parameters">Parameters</TabsTrigger>
                            <TabsTrigger value="run-config">Run Configuration</TabsTrigger>
                            <TabsTrigger value="broker-config">Broker Configuration</TabsTrigger>
                        </TabsList>
                        <TabsContent value="parameters" className="flex-grow overflow-hidden">
                            <div className="flex h-full">
                                <div className="w-1/3 md:w-1/4 border-r">
                                    <ScrollArea className="h-full">
                                        {Object.keys(groupedParams).map((group) => (
                                            <Button
                                                key={group}
                                                variant={activeGroup === group ? "secondary" : "ghost"}
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
                                                </TableRow>
                                            </TableHeader>
                                            <TableBody>
                                                {groupedParams[activeGroup]?.map((param, index) => (
                                                    <TableRow key={param.name}>
                                                        <TableCell>
                                                            <div className="flex items-center space-x-2">
                                                                <Label>{param.name}</Label>
                                                                <TooltipProvider>
                                                                    <Tooltip>
                                                                        <TooltipTrigger asChild>
                                                                            <Button variant="ghost" size="icon" className="h-8 w-8 p-0">
                                                                                <InfoIcon className="h-4 w-4"/>
                                                                                <span className="sr-only">Info</span>
                                                                            </Button>
                                                                        </TooltipTrigger>
                                                                        <TooltipContent>
                                                                            <p>{param.description || 'No description available'}</p>
                                                                        </TooltipContent>
                                                                    </Tooltip>
                                                                </TooltipProvider>
                                                            </div>
                                                        </TableCell>
                                                        <TableCell>
                                                            <Input
                                                                value={param.value}
                                                                onChange={(e) => handleInputChange(index, 'value', e.target.value)}
                                                                autoComplete="off"
                                                            />
                                                        </TableCell>
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
                                <div className="space-y-2 col-span-2 mb-3">
                                    <Label htmlFor="custom-name">Custom Strategy Name</Label>
                                    <Input
                                        id="custom-name"
                                        value={config.strategyName}
                                        onChange={(e) => setConfig(prev => ({...prev, strategyName: e.target.value}))}
                                        placeholder="Enter a custom name for your strategy"
                                    />
                                </div>

                                <div className="grid grid-cols-2 gap-4">
                                    <div className="space-y-2">
                                        <Label htmlFor="initial-cash">Initial Cash</Label>
                                        <Input
                                            id="initial-cash"
                                            value={config.config.initialCash}
                                            onChange={(e) => handleConfigChange('initialCash', e.target.value)}
                                        />
                                    </div>

                                    <div className="space-y-2">
                                        <Label htmlFor="instrument">Instrument</Label>
                                        <Select
                                            value={config.config.instrumentData.internalSymbol || ''}
                                            onValueChange={(value) => {
                                                const selectedInstrument = instruments.find(i => i.internalSymbol === value)
                                                handleConfigChange('instrumentData', selectedInstrument)
                                            }}
                                        >
                                            <SelectTrigger id="instrument">
                                                <SelectValue placeholder="Select instrument"/>
                                            </SelectTrigger>
                                            <SelectContent>
                                                {instruments.length > 0 ? (
                                                    instruments.map((instrument) => (
                                                        <SelectItem key={instrument.internalSymbol} value={instrument.internalSymbol}>
                                                            {instrument.internalSymbol}
                                                        </SelectItem>
                                                    ))
                                                ) : (
                                                    <SelectItem value="" disabled>No Instruments available</SelectItem>
                                                )}
                                            </SelectContent>
                                        </Select>
                                    </div>

                                    <div className="space-y-2">
                                        <Label htmlFor="period">Period</Label>
                                        <Select
                                            value={config.config.period}
                                            onValueChange={(value) => handleConfigChange('period', value)}
                                        >
                                            <SelectTrigger id="period">
                                                <SelectValue placeholder="Select period"/>
                                            </SelectTrigger>
                                            <SelectContent>
                                                <SelectItem value="M1">M1</SelectItem>
                                                <SelectItem value="M5">M5</SelectItem>
                                                <SelectItem value="M15">M15</SelectItem>
                                                <SelectItem value="M30">M30</SelectItem>
                                                <SelectItem value="H1">H1</SelectItem>
                                                <SelectItem value="H4">H4</SelectItem>
                                                <SelectItem value="D">D</SelectItem>
                                            </SelectContent>
                                        </Select>
                                    </div>
                                </div>
                            </ScrollArea>
                        </TabsContent>
                        <TabsContent value="broker-config" className="flex-grow overflow-hidden">
                            <ScrollArea className="h-full pr-4">
                                <div className="grid grid-cols-2 gap-4">
                                    <div className="space-y-2">
                                        <Label htmlFor="broker">Broker</Label>
                                        <Select
                                            value={config.brokerConfig.broker}
                                            onValueChange={(value) => handleBrokerConfigChange('broker', value)}
                                        >
                                            <SelectTrigger id="broker">
                                                <SelectValue placeholder="Select broker"/>
                                            </SelectTrigger>
                                            <SelectContent>
                                                <SelectItem value="OANDA">OANDA</SelectItem>
                                                {/* Add more brokers as needed */}
                                            </SelectContent>
                                        </Select>
                                    </div>

                                    <div className="space-y-2">
                                        <Label htmlFor="account-id">Account ID</Label>
                                        <Input
                                            id="account-id"
                                            value={config.brokerConfig.account_id}
                                            onChange={(e) => handleBrokerConfigChange('account_id', e.target.value)}
                                        />
                                    </div>

                                    <div className="space-y-2">
                                        <Label htmlFor="account-type">Account Type</Label>
                                        <Select
                                            value={config.brokerConfig.type}
                                            onValueChange={(value) => handleBrokerConfigChange('type', value)}
                                        >
                                            <SelectTrigger id="account-type">
                                                <SelectValue placeholder="Select account type"/>
                                            </SelectTrigger>
                                            <SelectContent>
                                                <SelectItem value="DEMO">Demo</SelectItem>
                                                <SelectItem value="LIVE">Live</SelectItem>
                                            </SelectContent>
                                        </Select>
                                    </div>
                                </div>
                            </ScrollArea>
                        </TabsContent>
                    </Tabs>

                )}
                <DialogFooter className="space-x-2">
                    <Button
                        variant="outline"
                        onClick={onClose}
                    >
                        Cancel
                    </Button>
                    <Button
                        variant="default"
                        onClick={() => onSave(config)}
                        disabled={!valid()}
                    >
                        Create Strategy
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
};

export default LiveCreateStratModal;