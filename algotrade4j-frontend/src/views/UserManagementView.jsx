import React, {useEffect, useState} from 'react';
import {adminClient} from '../api/apiClient';
import {useToast} from "../hooks/use-toast";
import {Edit, Lock, Trash, UserPlus} from "lucide-react";
import {Button} from "../components/ui/button";
import {Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle} from "../components/ui/dialog";
import {Input} from "../components/ui/input";
import {Label} from "../components/ui/label";
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from "../components/ui/select";
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from "../components/ui/table";
import {Tooltip, TooltipContent, TooltipProvider, TooltipTrigger} from "../components/ui/tooltip";
import CreateUserForm from "../components/user/CreateUserForm";
import log from '../logger.js';

const UserManagementView = ({loggedInUser}) => {
    const [users, setUsers] = useState([]);
    const [roles, setRoles] = useState([]);
    const [selectedUser, setSelectedUser] = useState(null);
    const [isEditDialogOpen, setIsEditDialogOpen] = useState(false);
    const [isPasswordDialogOpen, setIsPasswordDialogOpen] = useState(false);
    const [isCreateDialogOpen, setIsCreateDialogOpen] = useState(false);
    const [newPassword, setNewPassword] = useState('');
    const {toast} = useToast();

    useEffect(() => {
        if (loggedInUser && loggedInUser.role !== 'ADMIN') {
            toast({
                title: "Access Denied",
                description: "You do not have permission to access this page",
                variant: "destructive",
            });
            return;
        }
        fetchUsers();
        fetchRoles();
    }, []);

    const fetchUsers = async () => {
        try {
            const fetchedUsers = await adminClient.getUsers();
            setUsers(fetchedUsers);
        } catch (error) {
            log.error('Failed to fetch users:', error);
            setToast({
                open: true,
                severity: 'error'
            });
            toast({
                title: "Failed to fetch users",
                description: error.response.data.message || 'Failed to fetch user: ' + error,
                variant: "destructive",
            });
        }
    };

    const fetchRoles = async () => {
        try {
            const fetchedRoles = await adminClient.getRoles();
            setRoles(fetchedRoles);
        } catch (error) {
            log.error('Failed to fetch roles:', error);
            toast({
                title: "Failed to fetch roles",
                description: error.response.data.message || 'Failed to fetch roles: ' + error,
                variant: "destructive",
            });
        }
    };

    const handleEditUser = (user) => {
        setSelectedUser({...user});
        setIsEditDialogOpen(true);
    };

    const handleCreateUser = async (newUserData) => {
        try {
            await adminClient.createUser(newUserData);
            setIsCreateDialogOpen(false);
            fetchUsers();
            toast({
                title: "User Created",
                description: `User ${newUserData.username} created successfully`,
            });
        } catch (error) {
            log.error('Failed to create user:', error);
            toast({
                title: "Error",
                description: error.response?.data?.message || `Failed to create user: ${error}`,
                variant: "destructive",
            });
        }
    };

    const handleSaveUser = async () => {
        if (selectedUser.username === loggedInUser.username) {
            if (selectedUser.role !== loggedInUser.role) { // They've changed their own role
                toast({
                    title: "Error",
                    description: "You can't change your own role. Please ask another admin.",
                    variant: "destructive",
                });
            }
            return;
        }

        try {
            await adminClient.updateUser(selectedUser.id, selectedUser);
            setIsEditDialogOpen(false);
            fetchUsers();
            toast({
                title: "User Updated",
                description: `User ${selectedUser.username} updated successfully`,
            });
        } catch (error) {
            log.error('Failed to update user:', error);
            toast({
                title: "Error",
                description: error.response?.data?.message || `Failed to update user: ${error}`,
                variant: "destructive",
            });
        }
    };

    const handleChangePassword = async () => {
        try {
            await adminClient.changeUserPassword(selectedUser.id, newPassword);
            setIsPasswordDialogOpen(false);
            setNewPassword('');
            toast({
                title: "Password Changed",
                description: `Password for user ${selectedUser.username} changed successfully`,
            });
        } catch (error) {
            log.error('Failed to change password:', error);
            toast({
                title: "Error",
                description: error.response?.data?.message || `Failed to change password: ${error}`,
                variant: "destructive",
            });
        }
    };

    const handleDeleteUser = async (userId) => {
        if (window.confirm('Are you sure you want to delete this user?')) {
            try {
                await adminClient.deleteUser(userId);
                fetchUsers();
                toast({
                    title: "User Deleted",
                    description: "User deleted successfully",
                });
            } catch (error) {
                log.error('Failed to delete user:', error);
                toast({
                    title: "Error",
                    description: error.response?.data?.message || `Failed to delete user: ${error}`,
                    variant: "destructive",
                });
            }
        }
    };

    const formatDate = (dateArray) => {
        if (!Array.isArray(dateArray) || dateArray.length < 7) {
            return 'Invalid Date';
        }
        const [year, month, day, hour, minute, second, nanosecond] = dateArray;
        const date = new Date(year, month - 1, day, hour, minute, second, nanosecond / 1000000);
        return date.toLocaleString();
    };

    return (
        <div className="container mx-auto px-4 py-8">
            <div className="flex justify-between items-center mb-6">
                <h1 className="text-2xl font-semibold">User Management</h1>
                <Button onClick={() => setIsCreateDialogOpen(true)}>
                    <UserPlus className="mr-2 h-4 w-4"/>
                    Create User
                </Button>
            </div>
            <div className="hadow-md rounded-lg overflow-hidden">
                <div className="overflow-x-auto">
                    <Table>
                        <TableHeader>
                            <TableRow>
                                <TableHead>Name</TableHead>
                                <TableHead>Username</TableHead>
                                <TableHead>Email</TableHead>
                                <TableHead>Role</TableHead>
                                <TableHead>Created</TableHead>
                                <TableHead>Last Updated</TableHead>
                                <TableHead className="text-right">Actions</TableHead>
                            </TableRow>
                        </TableHeader>
                        <TableBody>
                            {users.map((user) => (
                                <TableRow key={user.id}>
                                    <TableCell>{`${user.firstName} ${user.lastName}`}</TableCell>
                                    <TableCell className="font-medium">{user.username}</TableCell>
                                    <TableCell>{user.email}</TableCell>
                                    <TableCell>{user.role}</TableCell>
                                    <TableCell>{formatDate(user.createdAt)}</TableCell>
                                    <TableCell>{formatDate(user.updatedAt)}</TableCell>
                                    <TableCell className="text-right">
                                        <TooltipProvider>
                                            <Tooltip>
                                                <TooltipTrigger asChild>
                                                    <Button variant="ghost" size="sm" onClick={() => handleEditUser(user)}>
                                                        <Edit className="h-4 w-4"/>
                                                    </Button>
                                                </TooltipTrigger>
                                                <TooltipContent>
                                                    <p>Edit User</p>
                                                </TooltipContent>
                                            </Tooltip>
                                        </TooltipProvider>
                                        <TooltipProvider>
                                            <Tooltip>
                                                <TooltipTrigger asChild>
                                                    <Button variant="ghost" size="sm" onClick={() => {
                                                        setSelectedUser(user);
                                                        setIsPasswordDialogOpen(true);
                                                    }}>
                                                        <Lock className="h-4 w-4"/>
                                                    </Button>
                                                </TooltipTrigger>
                                                <TooltipContent>
                                                    <p>Change Password</p>
                                                </TooltipContent>
                                            </Tooltip>
                                        </TooltipProvider>
                                        <TooltipProvider>
                                            <Tooltip>
                                                <TooltipTrigger asChild>
                                                    <Button variant="ghost" size="sm" onClick={() => handleDeleteUser(user.id)}>
                                                        <Trash className="h-4 w-4"/>
                                                    </Button>
                                                </TooltipTrigger>
                                                <TooltipContent>
                                                    <p>Delete User</p>
                                                </TooltipContent>
                                            </Tooltip>
                                        </TooltipProvider>
                                    </TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                </div>
            </div>

            <Dialog open={isEditDialogOpen} onOpenChange={setIsEditDialogOpen}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>Edit User</DialogTitle>
                    </DialogHeader>
                    <div className="grid gap-4 py-4">
                        <div className="grid grid-cols-4 items-center gap-4">
                            <Label htmlFor="firstName" className="text-right">
                                First Name
                            </Label>
                            <Input
                                id="firstName"
                                value={selectedUser?.firstName || ''}
                                onChange={(e) => setSelectedUser({...selectedUser, firstName: e.target.value})}
                                className="col-span-3"
                            />
                        </div>
                        <div className="grid grid-cols-4 items-center gap-4">
                            <Label htmlFor="lastName" className="text-right">
                                Last Name
                            </Label>
                            <Input
                                id="lastName"
                                value={selectedUser?.lastName || ''}
                                onChange={(e) => setSelectedUser({...selectedUser, lastName: e.target.value})}
                                className="col-span-3"
                            />
                        </div>
                        <div className="grid grid-cols-4 items-center gap-4">
                            <Label htmlFor="username" className="text-right">
                                Username
                            </Label>
                            <Input
                                id="username"
                                value={selectedUser?.username || ''}
                                onChange={(e) => setSelectedUser({...selectedUser, username: e.target.value})}
                                className="col-span-3"
                            />
                        </div>
                        <div className="grid grid-cols-4 items-center gap-4">
                            <Label htmlFor="email" className="text-right">
                                Email
                            </Label>
                            <Input
                                id="email"
                                value={selectedUser?.email || ''}
                                onChange={(e) => setSelectedUser({...selectedUser, email: e.target.value})}
                                className="col-span-3"
                            />
                        </div>
                        <div className="grid grid-cols-4 items-center gap-4">
                            <Label htmlFor="role" className="text-right">
                                Role
                            </Label>
                            <Select
                                value={selectedUser?.role || ''}
                                onValueChange={(value) => setSelectedUser({...selectedUser, role: value})}
                            >
                                <SelectTrigger className="col-span-3">
                                    <SelectValue placeholder="Select a role"/>
                                </SelectTrigger>
                                <SelectContent>
                                    {roles.map((role) => (
                                        <SelectItem key={role} value={role}>{role}</SelectItem>
                                    ))}
                                </SelectContent>
                            </Select>
                        </div>
                    </div>
                    <DialogFooter>
                        <Button variant="outline" onClick={() => setIsEditDialogOpen(false)}>
                            Cancel
                        </Button>
                        <Button onClick={handleSaveUser}>Save</Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>

            <Dialog open={isPasswordDialogOpen} onOpenChange={setIsPasswordDialogOpen}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>Change Password</DialogTitle>
                    </DialogHeader>
                    <form onSubmit={(e) => {
                        e.preventDefault();
                        handleChangePassword();
                    }}>
                        <div className="grid gap-4 py-4">
                            <div className="grid grid-cols-4 items-center gap-4">
                                <Label htmlFor="newPassword" className="text-right">
                                    New Password
                                </Label>
                                <Input
                                    id="newPassword"
                                    type="password"
                                    value={newPassword}
                                    onChange={(e) => setNewPassword(e.target.value)}
                                    className="col-span-3"
                                />
                            </div>
                        </div>
                        <DialogFooter>
                            <Button type="button" variant="outline" onClick={() => setIsPasswordDialogOpen(false)}>
                                Cancel
                            </Button>
                            <Button type="submit">Save</Button>
                        </DialogFooter>
                    </form>
                </DialogContent>
            </Dialog>

            <Dialog open={isCreateDialogOpen} onOpenChange={setIsCreateDialogOpen}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>Create New User</DialogTitle>
                    </DialogHeader>
                    <CreateUserForm onSubmit={handleCreateUser} roles={roles}/>
                </DialogContent>
            </Dialog>
        </div>
    );
};

export default UserManagementView;