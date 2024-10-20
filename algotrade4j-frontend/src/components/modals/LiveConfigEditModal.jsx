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
import {InfoIcon, Loader2} from 'lucide-react';
import {apiClient} from '@/api/apiClient.js';
import {liveStrategyClient} from "@/api/liveClient.js";
import {toast} from "@/hooks/use-toast.js";

const LiveConfigEditModal = ({open, onClose, strategyConfig, clearChart, isStrategyInUse}) => {
    const [activeTab, setActiveTab] = useState('parameters');
    const [activeGroup, setActiveGroup] = useState('');
    const [instruments, setInstruments] = useState([]);
    const [localConfig, setLocalConfig] = useState(strategyConfig);

    const [uiIsSaving, setUiIsSaving] = useState(false);

    useEffect(() => {
        if (open && strategyConfig) {
            setLocalConfig(strategyConfig);
            const groups = Object.keys(groupParams(strategyConfig.config.runParams));
            setActiveGroup(groups[0] || '');
        }
    }, [open, strategyConfig]);

    useEffect(() => {
        const fetchInstruments = async () => {
            try {
                const inst = await apiClient.getInstruments();
                setInstruments(inst);
            } catch (e) {
                console.error('Failed to fetch instruments', e);
            }
        };

        fetchInstruments();
    }, []);

    const groupParams = (params) => {
        return params.reduce((groups, param) => {
            const group = param.group || 'Ungrouped';
            if (!groups[group]) groups[group] = [];
            groups[group].push(param);
            return groups;
        }, {});
    };

    const handleInputChange = (paramName, value) => {
        setLocalConfig((prevConfig) => {
            const updatedRunParams = prevConfig.config.runParams.map(param =>
                param.name === paramName ? {...param, value} : param
            );
            const newConfig = {
                ...prevConfig,
                config: {
                    ...prevConfig.config,
                    runParams: updatedRunParams,
                },
            };
            return newConfig;
        });
    };

    const handleStrategyNameChange = (value) => {
        setLocalConfig((prevConfig) => {
            const newConfig = {
                ...prevConfig,
                strategyName: value,
            };
            return newConfig;
        });
    }

    const handleTelegramChatIDChange = (value) => {
        setLocalConfig((prevConfig) => {
            const newConfig = {
                ...prevConfig,
                telegramChatId: value,
            };
            return newConfig;
        });
    }

    const handleConfigChange = (field, value) => {
        setLocalConfig((prevConfig) => {
            const newConfig = {
                ...prevConfig,
                config: {
                    ...prevConfig.config,
                    [field]: value,
                },
            };
            return newConfig;
        });
    };

    const handleConfigSave = async (config) => {
        setUiIsSaving(true);
        try {
            await liveStrategyClient.updateStrategy(config);

            if (isStrategyInUse) {
                clearChart();
            }

            toast({
                title: 'Strategy updated',
                description: `Live Strategy '${config.strategyName}' has been updated successfully`,
            });

            setUiIsSaving(false);
            onClose();
        } catch (error) {
            toast({
                title: 'Error',
                description: `Error updating live strategy '${config.strategyName}': ${error.message}`,
                variant: 'destructive',
            });
            setUiIsSaving(false);
        }
        setUiIsSaving(false);
    }

    const handleDelete = async (localConfig) => {
        if (window.confirm('Are you sure you want to delete this live strategy?')) {
            try {

                await liveStrategyClient.deleteStrategy(localConfig.id);
                toast({
                    title: 'Strategy deleted',
                    description: `Live Strategy '${localConfig.strategyName}' has been deleted successfully`,
                });
                onClose();
            } catch (error) {
                toast({
                    title: 'Error',
                    description: `Error deleting live strategy '${localConfig.strategyName}': ${error.message}`,
                    variant: 'destructive',
                });
            }
        }
    }

    const groupedParams = groupParams(localConfig.config.runParams);

    return (
        <Dialog open={open} onOpenChange={onClose}>
            <DialogContent className="w-full max-w-[1100px] max-h-[80vh] overflow-hidden flex flex-col">
                <DialogHeader>
                    <DialogTitle className="flex items-center">Strategy Configuration</DialogTitle>
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
                                                        {param.type === 'enum' ? (
                                                            <Select
                                                                value={param.value}
                                                                onValueChange={(value) =>
                                                                    handleInputChange(param.name, value)
                                                                }
                                                            >
                                                                <SelectTrigger className="w-full">
                                                                    <SelectValue placeholder="Select a value"/>
                                                                </SelectTrigger>
                                                                <SelectContent>
                                                                    {param.enumValues.map((enumValue) => (
                                                                        <SelectItem key={enumValue} value={enumValue}>
                                                                            {enumValue}
                                                                        </SelectItem>
                                                                    ))}
                                                                </SelectContent>
                                                            </Select>
                                                        ) : (
                                                            <Input
                                                                value={param.value}
                                                                onChange={(e) =>
                                                                    handleInputChange(
                                                                        param.name,
                                                                        e.target.value
                                                                    )
                                                                }
                                                                type={param.type === 'int' || param.type === 'double' ? 'number' : 'text'}
                                                                autoComplete="off"
                                                            />
                                                        )}
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
                            <div className="grid grid-cols-2 gap-4">
                                <div className="space-y-2 mb-2">
                                    <Label htmlFor="custom-name">Custom Strategy Name</Label>
                                    <Input
                                        id="custom-name"
                                        value={localConfig.strategyName}
                                        onChange={(e) =>
                                            handleStrategyNameChange(e.target.value)
                                        }
                                        placeholder="Enter a custom name for your strategy"
                                        autoComplete="off"
                                    />
                                </div>

                                <div className="space-y-2 mb-2">
                                    <Label htmlFor="telegram-chat-id">Telegram Chat Id</Label>
                                    <Input
                                        id="telegram-chat-id"
                                        value={localConfig.telegramChatId}
                                        onChange={(e) =>
                                            handleTelegramChatIDChange(e.target.value)
                                        }
                                        placeholder="Enter a Telegram Chat ID for live notifications"
                                        autoComplete="off"
                                    />
                                </div>
                            </div>
                            <div className="grid grid-cols-2 gap-4">
                                <div className="space-y-2">
                                    <Label htmlFor="instrument">Instrument</Label>
                                    <Select
                                        value={
                                            localConfig.config.instrumentData.internalSymbol || ''
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
                                        value={localConfig.config.period}
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
                </Tabs>
                <DialogFooter className="space-x-2">
                    <Button variant="destructive" onClick={() => handleDelete(localConfig)} disabled={uiIsSaving}>
                        Delete
                    </Button>
                    <Button variant="outline" onClick={onClose} disabled={uiIsSaving}>
                        Cancel
                    </Button>
                    <Button
                        variant="default"
                        onClick={() => handleConfigSave(localConfig)}
                        disabled={uiIsSaving}
                    >
                        {uiIsSaving ? (
                            <>
                                <Loader2 className="mr-2 h-4 w-4 animate-spin"/>
                                Saving...
                            </>
                        ) : (
                            'Save changes'
                        )}
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
};

export default LiveConfigEditModal;