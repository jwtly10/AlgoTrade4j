import React, {useEffect, useState} from 'react';
import {Button} from '@/components/ui/button';
import {Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle,} from '@/components/ui/dialog';
import {Input} from '@/components/ui/input';
import {Label} from '@/components/ui/label';
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue,} from '@/components/ui/select';
import {Tabs, TabsContent, TabsList, TabsTrigger} from '@/components/ui/tabs';
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow,} from '@/components/ui/table';
import {Tooltip, TooltipContent, TooltipProvider, TooltipTrigger} from '@/components/ui/tooltip';
import {ScrollArea} from '@/components/ui/scroll-area';
import {InfoIcon} from 'lucide-react';
import {accountClient, strategyClient} from '@/api/liveClient.js';
import {apiClient} from '@/api/apiClient.js';
import {toast} from '@/hooks/use-toast';

const LiveCreateStratModal = ({open, onClose, strategies}) => {
    const [activeTab, setActiveTab] = useState('parameters');
    const [activeGroup, setActiveGroup] = useState('');
    const [instruments, setInstruments] = useState([]);
    const [selectedStrategy, setSelectedStrategy] = useState('');
    const [accounts, setAccounts] = useState([]);
    const [config, setConfig] = useState({
        strategyName: '',
        brokerAccount: {
            brokerName: '',
            brokerType: '',
            accountId: '',
            initialBalance: '',
        },
        config: {
            strategyClass: '',
            initialCash: '',
            instrumentData: {internalSymbol: ''},
            period: '',
            runParams: [],
        },
    });

    const handleSave = async () => {
        try {
            // Prepare the LiveStrategy object
            const liveStrategy = {
                strategyName: config.strategyName,
                brokerAccount: config.brokerAccount,
                config: config.config,
            };

            await strategyClient.createStrategy(liveStrategy);

            toast({
                title: 'Strategy Created',
                description: 'Your live strategy has been successfully created.',
            });
            onClose();
        } catch (error) {
            toast({
                title: 'Error',
                description: `Error initialising a new Live Strategy: ${error.message}`,
                variant: 'destructive',
            });
        }
    };

    useEffect(() => {
        if (open) {
            fetchInstruments();
            fetchAccounts();
        } else {
            setSelectedStrategy('');
            setConfig({
                strategyName: '',
                brokerAccount: {
                    brokerName: '',
                    brokerType: '',
                    accountId: '',
                    initialBalance: '',
                },
                config: {
                    strategyClass: '',
                    initialCash: '',
                    instrumentData: {internalSymbol: ''},
                    period: '',
                    runParams: [],
                },
            });
            setActiveTab('parameters');
            setActiveGroup('');
        }
    }, [open]);

    useEffect(() => {
        if (selectedStrategy) {
            // Fetch strategy parameters and update config
            apiClient.getParams(selectedStrategy).then((params) => {
                setConfig((prevConfig) => ({
                    ...prevConfig,
                    config: {
                        ...prevConfig.config,
                        strategyClass: selectedStrategy,
                        runParams: params,
                    },
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

    const fetchInstruments = async () => {
        try {
            const inst = await apiClient.getInstruments();
            setInstruments(inst);
        } catch (error) {
            toast({
                title: 'Error',
                description: `Failed to fetch instruments: ${error.message}`,
                variant: 'destructive',
            });
        }
    };

    const fetchAccounts = async () => {
        try {
            const accounts = await accountClient.getAccounts();
            setAccounts(accounts);
        } catch (error) {
            toast({
                title: 'Error',
                description: `Failed to fetch accounts: ${error.message}`,
                variant: 'destructive',
            });
        }
    };

    const handleInputChange = (paramName, value) => {
        setConfig((prevConfig) => {
            const updatedRunParams = prevConfig.config.runParams.map(param =>
                param.name === paramName ? {...param, value} : param
            );
            return {
                ...prevConfig,
                config: {
                    ...prevConfig.config,
                    runParams: updatedRunParams,
                },
            };
        });
    };

    const handleConfigChange = (field, value) => {
        setConfig((prevConfig) => ({
            ...prevConfig,
            config: {
                ...prevConfig.config,
                [field]: value,
            },
        }));
    };

    const handleBrokerConfigChange = (field, value) => {
        setConfig((prevConfig) => ({
            ...prevConfig,
            config: {
                ...prevConfig.config,
                initialCash: value.initialBalance,
            },
            brokerAccount: value,
        }));
    };

    const groupedParams = groupParams(config.config.runParams);

    const valid = () => {
        // Check if a strategy is selected
        if (!selectedStrategy) return false;

        // Check base config values are set
        if (!config.strategyName) return false;

        // Check broker account
        if (
            !config.brokerAccount.brokerName ||
            !config.brokerAccount.accountId ||
            !config.brokerAccount.brokerType ||
            !config.brokerAccount.initialBalance
        )
            return false;

        // Check if all strategy configuration fields are set
        if (
            !config.config.initialCash ||
            !config.config.instrumentData.internalSymbol ||
            !config.config.period
        )
            return false;

        // Check if all required parameters have values
        const allParamsSet = config.config.runParams.every(
            (param) => param.value !== undefined && param.value !== ''
        );

        if (!allParamsSet) return false;

        return true;
    };

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
                    <Tabs
                        value={activeTab}
                        onValueChange={setActiveTab}
                        className="flex-grow overflow-hidden"
                    >
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
                                                variant={
                                                    activeGroup === group ? 'secondary' : 'ghost'
                                                }
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
                                                                        param.name,
                                                                        e.target.value
                                                                    )
                                                                }
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
                                        onChange={(e) =>
                                            setConfig((prev) => ({
                                                ...prev,
                                                strategyName: e.target.value,
                                            }))
                                        }
                                        placeholder="Enter a custom name for your strategy"
                                    />
                                </div>

                                <div className="grid grid-cols-2 gap-4">
                                    <div className="space-y-2">
                                        <Label htmlFor="instrument">Instrument</Label>
                                        <Select
                                            value={
                                                config.config.instrumentData.internalSymbol || ''
                                            }
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
                                                    instruments.map((instrument) => (
                                                        <SelectItem
                                                            key={instrument.internalSymbol}
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
                                            value={config.config.period}
                                            onValueChange={(value) =>
                                                handleConfigChange('period', value)
                                            }
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
                                        <Label htmlFor="account-id">Account ID</Label>
                                        <Select
                                            value={config.brokerAccount.accountId}
                                            onValueChange={(value) => {
                                                const selectedAccount = accounts.find(
                                                    (account) => account.accountId === value
                                                );
                                                handleBrokerConfigChange(
                                                    'accountId',
                                                    selectedAccount
                                                );
                                            }}
                                        >
                                            <SelectTrigger id="account">
                                                <SelectValue placeholder="Select account"/>
                                            </SelectTrigger>
                                            <SelectContent>
                                                {accounts.map((account) => (
                                                    <SelectItem
                                                        key={account.id}
                                                        value={account.accountId}
                                                    >
                                                        {`${account.brokerName} - ${account.brokerType} - ${account.initialBalance} - ${account.accountId}`}
                                                    </SelectItem>
                                                ))}
                                            </SelectContent>
                                        </Select>
                                    </div>

                                </div>
                            </ScrollArea>
                        </TabsContent>
                    </Tabs>
                )}
                <DialogFooter className="space-x-2">
                    <Button variant="outline" onClick={onClose}>
                        Cancel
                    </Button>
                    <Button
                        variant="default"
                        onClick={() => handleSave(config)}
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