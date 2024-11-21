import React, {useEffect, useState} from 'react';
import {Dialog, DialogContent, DialogHeader, DialogTitle} from '@/components/ui/dialog';
import {Button} from '@/components/ui/button';
import {Input} from '@/components/ui/input';
import {Label} from '@/components/ui/label';
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue,} from '@/components/ui/select';
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow,} from '@/components/ui/table';
import {Card, CardContent, CardHeader, CardTitle} from '@/components/ui/card';
import {ScrollArea} from '@/components/ui/scroll-area';
import {liveAccountClient} from '@/api/liveClient.js';
import {useToast} from '@/hooks/use-toast';
import {Tooltip, TooltipContent, TooltipProvider, TooltipTrigger} from "@/components/ui/tooltip.jsx";
import {InfoIcon} from "lucide-react";
import log from "@/logger.js";

const LiveBrokerModal = ({open, onClose}) => {

    const DEFAULT_NEW_ACCOUNT = {
        brokerName: '',
        brokerType: '',
        brokerEnv: '',
        initialBalance: '',
        accountId: '',
        mt5Credentials: {
            password: '',
            server: '',
            path: '',
            timezone: ''
        }
    }

    const [accounts, setAccounts] = useState([]);
    const [brokers, setBrokers] = useState([]);
    const [timezones, setTimezones] = useState([]);
    const [mode, setMode] = useState('list'); // 'list', 'edit', or 'new'
    const [editingAccount, setEditingAccount] = useState(null);
    const [newAccount, setNewAccount] = useState(DEFAULT_NEW_ACCOUNT);
    const {toast} = useToast();

    useEffect(() => {
        if (open) {
            resetState();
            fetchAccounts();
            fetchBrokers();
            fetchTimezones();
        }
    }, [open]);

    const getCurrentTimeByZone = (zoneId) => {
        return new Date().toLocaleTimeString('en-US', {timeZone: zoneId, timeStyle: 'short'});
    };

    const findTimezoneByCode = (code) => {
        if (code?.name) {
            return code
        }

        return timezones.find(tz => tz.name === code);
    };

    const isFormValid = (account) => {

        const base = account.brokerName && account.brokerType && account.initialBalance && account.accountId

        if (account.brokerType.startsWith('MT5')) {
            const mt5 =
                // If the mode is edit, we don't need to check the password, as we don't want to update it
                // If the mode is new, we need to check the password
                (mode !== "edit" ? account.mt5Credentials?.password : true)
                && account.mt5Credentials?.server
                && account.mt5Credentials?.path
                && account.mt5Credentials?.timezone
            return base && mt5
        } else {
            return base
        }
    };

    const fetchAccounts = async () => {
        try {
            const res = await liveAccountClient.getAccounts();
            setAccounts(res);
        } catch (error) {
            log.error('Failed to fetch accounts', error);
            toast({
                title: 'Error',
                description: `Failed to fetch accounts: ${error.message}`,
                variant: 'destructive',
            });
        }
    };

    const fetchBrokers = async () => {
        try {
            const res = await liveAccountClient.getBrokers();
            setBrokers(res);
        } catch (error) {
            log.error('Failed to fetch brokers', error);
            toast({
                title: 'Error',
                description: `Failed to fetch brokers: ${error.message}`,
                variant: 'destructive',
            });
        }
    };

    const fetchTimezones = async () => {
        try {
            const res = await liveAccountClient.getTimezones();
            setTimezones(res);
        } catch (error) {
            log.error('Failed to fetch timezones', error);
            toast({
                title: 'Error',
                description: `Failed to timezones brokers: ${error.message}`,
                variant: 'destructive',
            });
        }
    };

    const handleInputChange = (field, value) => {
        if (mode === 'edit') {
            setEditingAccount({...editingAccount, [field]: value});
        } else if (mode === 'new') {
            setNewAccount({...newAccount, [field]: value});
        }
    };

    const handleMt5InputChange = (field, value) => {
        if (mode === 'edit') {
            setEditingAccount({
                ...editingAccount,
                mt5Credentials: {
                    ...editingAccount.mt5Credentials,
                    [field]: value,
                },
            });
        } else {
            setNewAccount({
                ...newAccount,
                mt5Credentials: {
                    ...newAccount.mt5Credentials,
                    [field]: value,
                },
            });
        }
    };


    const handleSave = async () => {
        let accountToSave = mode === 'edit' ? editingAccount : newAccount;

        log.debug({accountToSave, editingAccount, newAccount})


        const isEmptyMt5Credentials = (credentials) => {
            return !credentials || Object.values(credentials).every((value) => !value);
        };

        // Remove mt5Credentials if brokerType is not 'MT5' or credentials are empty
        if (!accountToSave.brokerType.startsWith('MT5') || isEmptyMt5Credentials(accountToSave.mt5Credentials)) {
            // Create a copy of the account and remove mt5Credentials
            accountToSave = {
                ...accountToSave,
                mt5Credentials: undefined
            };
        } else {
            accountToSave.mt5Credentials.timezone = accountToSave.mt5Credentials.timezone.name
        }

        if (!isFormValid(accountToSave)) {
            toast({
                title: 'Incomplete Account Details',
                description: 'Please complete all fields before saving.',
                variant: 'destructive',
            });
            return;
        }

        try {
            var updatedAccount;
            var savedAccount;
            if (mode === 'edit') {
                updatedAccount = await liveAccountClient.updateBrokerAccount(
                    editingAccount.accountId,
                    accountToSave
                );
                const updatedAccounts = accounts.map((account) =>
                    account.id === editingAccount.id ? editingAccount : account
                );
                setAccounts(updatedAccounts);
                toast({
                    title: 'Success',
                    description: `Account '${updatedAccount.brokerName}' updated successfully`,
                });
            } else if (mode === 'new') {
                savedAccount = await liveAccountClient.createBrokerAccount(accountToSave);
                setAccounts([...accounts, {...accountToSave, id: Date.now()}]);
                toast({
                    title: 'Success',
                    description: `Account '${savedAccount.accountId}' created successfully`,
                });
            }
            setMode('list');
            setEditingAccount(null);
            setNewAccount(DEFAULT_NEW_ACCOUNT);
        } catch (error) {
            log.error('Failed to save account', error);
            toast({
                title: 'Broker Account Error',
                description: `Failed to update Accounts : ${error.message}`,
                variant: 'destructive',
            });
        }
    };

    const handleEdit = (account) => {
        // Convert the data from DB which just passes in the code
        if (account.mt5Credentials && account.mt5Credentials.timezone) {
            account.mt5Credentials.timezone = findTimezoneByCode(account.mt5Credentials.timezone);
        }

        setEditingAccount(account);
        setMode('edit');
    };

    const handleDelete = async (accountId) => {
        try {
            await liveAccountClient.deleteBrokerAccount(accountId);
            setAccounts(accounts.filter((account) => account.accountId !== accountId));
            toast({
                title: 'Success',
                description: `Account '${accountId}' deleted successfully`,
            });
        } catch (error) {
            log.error('Failed to delete account', error);
            toast({
                title: 'Broker Account Error',
                description: `Failed to delete Account : ${error.message}`,
                variant: 'destructive',
            });
        }
    };

    const resetState = () => {
        setAccounts([]);
        setBrokers([]);
        setTimezones([])
        setMode('list');
        setEditingAccount(null);
        setNewAccount(DEFAULT_NEW_ACCOUNT);
    };

    const renderForm = () => (
        <div className="space-y-6">
            {/* Section 1: General Broker Information */}
            <Card>
                <CardHeader>
                    <CardTitle>Broker Information</CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <div className="space-y-2">
                            <Label htmlFor="brokerName">Name</Label>
                            <Input
                                id="brokerName"
                                value={mode === 'edit' ? editingAccount?.brokerName : newAccount.brokerName}
                                onChange={(e) => handleInputChange('brokerName', e.target.value)}
                                type="text"
                                placeholder="Enter Broker Name"
                                autoComplete="off"
                            />
                        </div>

                        <div className="space-y-2">
                            <Label htmlFor="brokerType">Broker Type</Label>
                            <Select
                                value={mode === 'edit' ? editingAccount?.brokerType : newAccount.brokerType}
                                onValueChange={(value) => handleInputChange('brokerType', value)}
                                disabled={mode === 'edit'}
                            >
                                <SelectTrigger id="brokerType">
                                    <SelectValue placeholder="Select broker type"/>
                                </SelectTrigger>
                                <SelectContent>
                                    {brokers.map((broker) => (
                                        <SelectItem key={broker} value={broker}>
                                            {broker}
                                        </SelectItem>
                                    ))}
                                </SelectContent>
                            </Select>
                        </div>

                        <div className="space-y-2">
                            <Label htmlFor="environment">Environment</Label>
                            <Select
                                value={mode === 'edit' ? editingAccount?.brokerEnv : newAccount.brokerEnv}
                                onValueChange={(value) => handleInputChange('brokerEnv', value)}
                            >
                                <SelectTrigger id="environment">
                                    <SelectValue placeholder="Select environment"/>
                                </SelectTrigger>
                                <SelectContent>
                                    <SelectItem value="LIVE">LIVE</SelectItem>
                                    <SelectItem value="DEMO">DEMO</SelectItem>
                                </SelectContent>
                            </Select>
                        </div>

                        <div className="space-y-2">
                            <Label htmlFor="initialBalance">Initial Balance</Label>
                            <Input
                                id="initialBalance"
                                value={mode === 'edit' ? editingAccount?.initialBalance : newAccount.initialBalance}
                                onChange={(e) => handleInputChange('initialBalance', e.target.value)}
                                type="number"
                                placeholder="Enter initial balance"
                            />
                        </div>

                        <div className="space-y-2">
                            <Label htmlFor="accountId">Account ID</Label>
                            <Input
                                id="accountId"
                                value={mode === 'edit' ? editingAccount?.accountId : newAccount.accountId}
                                onChange={(e) => handleInputChange('accountId', e.target.value)}
                                disabled={mode === 'edit'}
                                placeholder="Enter account ID"
                                type="text"
                                autoComplete="off"
                            />
                        </div>
                    </div>
                </CardContent>
            </Card>

            {/* Section 2: MT5 Credentials (conditional) */}
            {/* We dont need to do this anymore!*/}
            {/*{(mode === 'edit' ? editingAccount?.brokerType : newAccount.brokerType).startsWith('MT5') && (*/}
            {/*    <Card>*/}
            {/*        <CardHeader>*/}
            {/*            <CardTitle>MT5 Credentials</CardTitle>*/}
            {/*        </CardHeader>*/}
            {/*        <CardContent className="space-y-4">*/}
            {/*            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">*/}
            {/*                <>*/}
            {/*                    {(mode !== 'edit' && (*/}
            {/*                        <div className="space-y-2">*/}
            {/*                            <div className="flex items-center space-x-2">*/}
            {/*                                <Label htmlFor="mt5Password">MT5 Password</Label>*/}
            {/*                                <TooltipProvider>*/}
            {/*                                    <Tooltip>*/}
            {/*                                        <TooltipTrigger asChild>*/}
            {/*                                            <Button*/}
            {/*                                                variant="ghost"*/}
            {/*                                                size="icon"*/}
            {/*                                            >*/}
            {/*                                                <InfoIcon className="h-3 w-3"/>*/}
            {/*                                                <span className="sr-only">Info</span>*/}
            {/*                                            </Button>*/}
            {/*                                        </TooltipTrigger>*/}
            {/*                                        <TooltipContent side="top" align="start">*/}
            {/*                                            You will not be able to see or change the password once it's set. You will need to delete and re-enter.*/}
            {/*                                        </TooltipContent>*/}
            {/*                                    </Tooltip>*/}
            {/*                                </TooltipProvider>*/}
            {/*                            </div>*/}
            {/*                            <div className="flex items-center space-x-2">*/}
            {/*                                <Input*/}
            {/*                                    id="mt5Password"*/}
            {/*                                    value={mode === 'edit' ? editingAccount?.mt5Credentials?.password : newAccount.mt5Credentials?.password}*/}
            {/*                                    onChange={(e) => handleMt5InputChange('password', e.target.value)}*/}
            {/*                                    type="password"*/}
            {/*                                    placeholder="Enter MT5 password"*/}
            {/*                                    autoComplete="off"*/}
            {/*                                />*/}
            {/*                            </div>*/}
            {/*                        </div>*/}
            {/*                    ))}*/}
            {/*                </>*/}

            {/*                <div className="space-y-2 mt-3">*/}
            {/*                    <Label htmlFor="mt5Server">MT5 Server</Label>*/}
            {/*                    <Input*/}
            {/*                        id="mt5Server"*/}
            {/*                        value={mode === 'edit' ? editingAccount?.mt5Credentials?.server : newAccount.mt5Credentials?.server}*/}
            {/*                        onChange={(e) => handleMt5InputChange('server', e.target.value)}*/}
            {/*                        type="text"*/}
            {/*                        placeholder="Enter MT5 server"*/}
            {/*                        autoComplete="off"*/}
            {/*                    />*/}
            {/*                </div>*/}

            {/*                <div className="space-y-2">*/}
            {/*                    <Label htmlFor="mt5Path">MT5 Path</Label>*/}
            {/*                    <Input*/}
            {/*                        id="mt5Path"*/}
            {/*                        value={mode === 'edit' ? editingAccount?.mt5Credentials?.path : newAccount.mt5Credentials?.path}*/}
            {/*                        onChange={(e) => handleMt5InputChange('path', e.target.value)}*/}
            {/*                        type="text"*/}
            {/*                        placeholder="Enter MT5 path"*/}
            {/*                        autoComplete="off"*/}
            {/*                    />*/}
            {/*                </div>*/}

            {/*                <div className="space-y-2">*/}
            {/*                    <Label htmlFor="mt5Timezone">MT5 Timezone</Label>*/}
            {/*                    <Select*/}
            {/*                        value={mode === 'edit' ? findTimezoneByCode(editingAccount?.mt5Credentials?.timezone) : findTimezoneByCode(newAccount?.mt5Credentials?.timezone)}*/}
            {/*                        onValueChange={(value) => handleMt5InputChange('timezone', value)}*/}
            {/*                    >*/}
            {/*                        <SelectTrigger id="mt5Timezone">*/}
            {/*                            <SelectValue placeholder="Select timezone"/>*/}
            {/*                        </SelectTrigger>*/}
            {/*                        <SelectContent>*/}
            {/*                            {timezones.map((tz, index) => (*/}
            {/*                                <SelectItem key={index} value={tz}>*/}
            {/*                                    {`${tz.name} (${getCurrentTimeByZone(tz.zoneId)})`}*/}
            {/*                                </SelectItem>*/}
            {/*                            ))}*/}
            {/*                        </SelectContent>*/}
            {/*                    </Select>*/}
            {/*                </div>*/}
            {/*            </div>*/}
            {/*        </CardContent>*/}
            {/*    </Card>*/}
            {/*)}*/}

            {/* Action Buttons */}
            <div className="flex justify-end space-x-2 pt-4">
                <Button variant="outline" onClick={() => setMode('list')}>
                    Cancel
                </Button>
                <Button onClick={handleSave}>Save</Button>
            </div>
        </div>
    );

    return (
        <Dialog open={open} onOpenChange={onClose}>
            <DialogContent className="w-full max-w-[800px] max-h-[80vh] overflow-hidden flex flex-col">
                <DialogHeader>
                    <DialogTitle>Manage Broker Accounts</DialogTitle>
                </DialogHeader>
                <ScrollArea className="flex-grow pr-4">
                    {mode === 'list' && (
                        <>
                            <Table>
                                <TableHeader>
                                    <TableRow>
                                        <TableHead>Name</TableHead>
                                        <TableHead>Type</TableHead>
                                        <TableHead>Env</TableHead>
                                        <TableHead>Initial Balance</TableHead>
                                        <TableHead>Account ID</TableHead>
                                        <TableHead>Actions</TableHead>
                                    </TableRow>
                                </TableHeader>
                                <TableBody>
                                    {accounts.map((account) => (
                                        <TableRow key={account.id}>
                                            <TableCell>{account.brokerName}</TableCell>
                                            <TableCell>{account.brokerType}</TableCell>
                                            <TableCell>{account.brokerEnv}</TableCell>
                                            <TableCell>${parseFloat(account.initialBalance) ? parseFloat(account.initialBalance).toLocaleString() : account.initialBalance}</TableCell>
                                            <TableCell>{account.accountId}</TableCell>
                                            <TableCell>
                                                <Button
                                                    variant="outline"
                                                    size="sm"
                                                    className="mr-2"
                                                    onClick={() => handleEdit(account)}
                                                >
                                                    Edit
                                                </Button>
                                                <Button
                                                    variant="destructive"
                                                    size="sm"
                                                    onClick={() => handleDelete(account.accountId)}
                                                >
                                                    Delete
                                                </Button>
                                            </TableCell>
                                        </TableRow>
                                    ))}
                                </TableBody>
                            </Table>
                            <div className="mt-4">
                                <Button onClick={() => setMode('new')}>Add New Account</Button>
                            </div>
                        </>
                    )}
                    {(mode === 'edit' || mode === 'new') && renderForm()}
                </ScrollArea>
            </DialogContent>
        </Dialog>
    );
};

export default LiveBrokerModal;