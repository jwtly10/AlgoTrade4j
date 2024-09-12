import React, {useEffect, useState} from 'react';
import {Dialog, DialogContent, DialogHeader, DialogTitle} from '@/components/ui/dialog';
import {Button} from '@/components/ui/button';
import {Input} from '@/components/ui/input';
import {Label} from '@/components/ui/label';
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue,} from '@/components/ui/select';
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow,} from '@/components/ui/table';
import {ScrollArea} from '@/components/ui/scroll-area';
import {accountClient} from '@/api/liveClient.js';
import {useToast} from '@/hooks/use-toast';

const LiveBrokerModal = ({open, onClose}) => {
    const [accounts, setAccounts] = useState([]);
    const [brokers, setBrokers] = useState([]);
    const [mode, setMode] = useState('list'); // 'list', 'edit', or 'new'
    const [editingAccount, setEditingAccount] = useState(null);
    const [newAccount, setNewAccount] = useState({
        brokerName: '',
        brokerType: '',
        initialBalance: '',
        accountId: '',
    });
    const {toast} = useToast();

    useEffect(() => {
        if (open) {
            resetState();
            fetchAccounts();
            fetchBrokers();
        }
    }, [open]);

    const isFormValid = (account) => {
        return (
            account.brokerName && account.brokerType && account.initialBalance && account.accountId
        );
    };

    const fetchAccounts = async () => {
        try {
            const res = await accountClient.getAccounts();
            setAccounts(res);
        } catch (error) {
            console.error('Failed to fetch accounts', error);
            toast({
                title: 'Error',
                description: `Failed to fetch accounts: ${error.message}`,
                variant: 'destructive',
            });
        }
    };

    const fetchBrokers = async () => {
        try {
            const res = await accountClient.getBrokers();
            setBrokers(res);
        } catch (error) {
            console.error('Failed to fetch brokers', error);
            toast({
                title: 'Error',
                description: `Failed to fetch brokers: ${error.message}`,
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

    const handleSave = async () => {
        const accountToSave = mode === 'edit' ? editingAccount : newAccount;
        if (!isFormValid(accountToSave)) {
            toast({
                title: 'Incomplete Account Details',
                description: 'Please complete in all fields before saving.',
                variant: 'destructive',
            });
            return;
        }

        try {
            var updatedAccount;
            var savedAccount;
            if (mode === 'edit') {
                updatedAccount = await accountClient.updateBrokerAccount(
                    editingAccount.accountId,
                    editingAccount
                );
                const updatedAccounts = accounts.map((account) =>
                    account.id === editingAccount.id ? editingAccount : account
                );
                setAccounts(updatedAccounts);
            } else if (mode === 'new') {
                savedAccount = await accountClient.createBrokerAccount(newAccount);
                setAccounts([...accounts, {...newAccount, id: Date.now()}]);
            }
            setMode('list');
            setEditingAccount(null);
            setNewAccount({brokerName: '', brokerType: '', initialBalance: '', accountId: ''});
            toast({
                title: 'Success',
                description: `Account '${mode === 'edit' ? updatedAccount.accountId : savedAccount.accountId}' updated successfully`,
            });
        } catch (error) {
            console.error('Failed to save account', error);
            toast({
                title: 'Broker Account Error',
                description: `Failed to update Accounts : ${error.message}`,
                variant: 'destructive',
            });
        }
    };

    const handleEdit = (account) => {
        setEditingAccount(account);
        setMode('edit');
    };

    const handleDelete = async (accountId) => {
        try {
            await accountClient.deleteBrokerAccount(accountId);
            setAccounts(accounts.filter((account) => account.accountId !== accountId));
            toast({
                title: 'Success',
                description: `Account '${accountId}' deleted successfully`,
            });
        } catch (error) {
            console.error('Failed to delete account', error);
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
        setMode('list');
        setEditingAccount(null);
        setNewAccount({
            brokerName: '',
            brokerType: '',
            initialBalance: '',
            accountId: '',
        });
    };

    const renderForm = () => (
        <div className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                    <Label htmlFor="broker">Broker</Label>
                    <Select
                        value={mode === 'edit' ? editingAccount?.brokerName : newAccount.brokerName}
                        onValueChange={(value) => handleInputChange('brokerName', value)}
                    >
                        <SelectTrigger id="broker">
                            <SelectValue placeholder="Select broker"/>
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
                    <Label htmlFor="type">Type</Label>
                    <Select
                        value={mode === 'edit' ? editingAccount?.brokerType : newAccount.brokerType}
                        onValueChange={(value) => handleInputChange('brokerType', value)}
                    >
                        <SelectTrigger id="type">
                            <SelectValue placeholder="Select type"/>
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
                        value={
                            mode === 'edit'
                                ? editingAccount?.initialBalance
                                : newAccount.initialBalance
                        }
                        onChange={(e) => handleInputChange('initialBalance', e.target.value)}
                        type="number"
                    />
                </div>
                <div className="space-y-2">
                    <Label htmlFor="accountId">Account ID</Label>
                    <Input
                        id="accountId"
                        value={mode === 'edit' ? editingAccount?.accountId : newAccount.accountId}
                        onChange={(e) => handleInputChange('accountId', e.target.value)}
                        disabled={mode === 'edit'}
                        autoComplete="off"
                    />
                </div>
            </div>
            <div className="flex justify-end space-x-2">
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
                                        <TableHead>Broker</TableHead>
                                        <TableHead>Type</TableHead>
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
                                            <TableCell>{account.initialBalance}</TableCell>
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