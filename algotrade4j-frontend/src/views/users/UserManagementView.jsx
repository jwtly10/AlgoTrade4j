import React, {useEffect, useState} from 'react';
import {adminClient} from '../../api/apiClient.js';
import {useToast} from "../../hooks/use-toast.js";
import {Edit, Lock, Trash, UserPlus} from "lucide-react";
import {Button} from "../../components/ui/button.jsx";
import {Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle} from "../../components/ui/dialog.jsx";
import {Input} from "../../components/ui/input.jsx";
import {Label} from "../../components/ui/label.jsx";
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from "../../components/ui/select.jsx";
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from "../../components/ui/table.jsx";
import {Tooltip, TooltipContent, TooltipProvider, TooltipTrigger} from "../../components/ui/tooltip.jsx";
import MetadataViewer from "@/components/user/MetadataViewer.jsx";
import CreateUserForm from "../../components/user/CreateUserForm.jsx";
import log from '../../logger.js';
import {useIsMobile} from '@/hooks/useIsMobile.js';
import UserCard from '@/components/user/UserCard.jsx';

const UserManagementView = ({loggedInUser}) => {
    const [users, setUsers] = useState([]);
    const [roles, setRoles] = useState([]);
    const [selectedUser, setSelectedUser] = useState(null);
    const [isEditDialogOpen, setIsEditDialogOpen] = useState(false);
    const [isPasswordDialogOpen, setIsPasswordDialogOpen] = useState(false);
    const [isCreateDialogOpen, setIsCreateDialogOpen] = useState(false);
    const [newPassword, setNewPassword] = useState('');

    const [expandedUser, setExpandedUser] = useState(null);
    const [userLogs, setUserLogs] = useState([]);
    const [loginLogs, setLoginLogs] = useState([]);
    const [activeTab, setActiveTab] = useState('actions');
    const [actionFilter, setActionFilter] = useState('ALL');
    const [sortOrder, setSortOrder] = useState('desc');
    const [currentPage, setCurrentPage] = useState(1);
    const [logsPerPage] = useState(10);

    const [uiIsLoadingLogs, setUiIsLoadingLogs] = useState(false);

    const {toast} = useToast();
    const isMobile = useIsMobile();

    useEffect(() => {
        if (loggedInUser && loggedInUser.role !== 'ADMIN') {
            toast({
                title: 'Access Denied',
                description: 'You do not have permission to access this page',
                variant: 'destructive',
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
            toast({
                title: 'Failed to fetch users',
                description: 'Error fetching users: ' + error.message,
                variant: 'destructive',
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
                title: 'Failed to fetch roles',
                description: 'Error fetching roles: ' + error.message,
                variant: 'destructive',
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
            await fetchUsers();
            toast({
                title: 'User Created',
                description: `User ${newUserData.username} created successfully`,
            });
        } catch (error) {
            log.error('Failed to create user:', error);
            toast({
                title: 'Error',
                description: 'Error creating user: ' + error.message,
                variant: 'destructive',
            });
        }
    };

    const handleSaveUser = async () => {
        if (selectedUser.username === loggedInUser.username) {
            if (selectedUser.role !== loggedInUser.role) {
                // They've changed their own role
                toast({
                    title: 'Error',
                    description: "You can't change your own role. Please ask another admin.",
                    variant: 'destructive',
                });
            }
            return;
        }

        try {
            await adminClient.updateUser(selectedUser.id, selectedUser);
            setIsEditDialogOpen(false);
            await fetchUsers();
            toast({
                title: 'User Updated',
                description: `User ${selectedUser.username} updated successfully`,
            });
        } catch (error) {
            log.error('Failed to update user:', error);
            toast({
                title: 'Error',
                description: 'Error updating user: ' + error.message,
                variant: 'destructive',
            });
        }
    };

    const handleChangePassword = async () => {
        try {
            await adminClient.changeUserPassword(selectedUser.id, newPassword);
            setIsPasswordDialogOpen(false);
            setNewPassword('');
            toast({
                title: 'Password Changed',
                description: `Password for user ${selectedUser.username} changed successfully`,
            });
        } catch (error) {
            log.error('Failed to change password:', error);
            toast({
                title: 'Error',
                description: 'Error changing password: ' + error.message,
                variant: 'destructive',
            });
        }
    };

    const handleDeleteUser = async (userId) => {
        if (window.confirm('Are you sure you want to delete this user?')) {
            try {
                await adminClient.deleteUser(userId);
                await fetchUsers();
                toast({
                    title: 'User Deleted',
                    description: 'User deleted successfully',
                });
            } catch (error) {
                log.error('Failed to delete user:', error);
                toast({
                    title: 'Error',
                    description: 'Error deleting user: ' + error.message,
                    variant: 'destructive',
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

    const formatTimestamp = (timestamp) => {
        const date = new Date(timestamp * 1000); // Convert to milliseconds
        return date.toLocaleString('en-GB', {
            year: 'numeric',
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit',
            timeZone: 'Europe/London',
            timeZoneName: 'short',
            hour12: false,
        });
    };

    const handleUserClick = async (userId) => {
        if (expandedUser === userId) {
            // If the clicked user is already expanded, collapse it
            setExpandedUser(null);
        } else {
            // If a different user is clicked, collapse the current one and expand the new one
            setExpandedUser(userId);
            if (!userLogs[userId]) {
                setUiIsLoadingLogs(true);
                try {
                    const logs = await adminClient.getTrackingForUser(userId);
                    setUserLogs((prev) => ({...prev, [userId]: logs}));
                } catch (error) {
                    log.error('Failed to fetch user logs:', error);
                    toast({
                        title: 'Error',
                        description: 'Failed to fetch user logs: ' + error.message,
                        variant: 'destructive',
                    });
                }

                try {
                    const loginLogs = await adminClient.getLoginLogsForUser(userId);
                    setLoginLogs((prev) => ({...prev, [userId]: loginLogs}));
                } catch (error) {
                    log.error('Failed to fetch login logs:', error);
                    toast({
                        title: 'Error',
                        description: 'Failed to fetch login logs: ' + error.message,
                        variant: 'destructive',
                    });
                }
                setUiIsLoadingLogs(false);
            }
        }
    };

    const getFilteredAndSortedLogs = (userId) => {
        const filteredLogs = (userLogs[userId] || [])
            .filter((log) => actionFilter === 'ALL' || log.action === actionFilter)
            .sort((a, b) => {
                const dateA = new Date(a.timestamp);
                const dateB = new Date(b.timestamp);
                return sortOrder === 'desc' ? dateB - dateA : dateA - dateB;
            });

        // Calculate pagination
        const indexOfLastLog = currentPage * logsPerPage;
        const indexOfFirstLog = indexOfLastLog - logsPerPage;
        const currentLogs = filteredLogs.slice(indexOfFirstLog, indexOfLastLog);

        return {
            logs: currentLogs,
            totalPages: Math.ceil(filteredLogs.length / logsPerPage),
        };
    };

    const getFilteredAndSortedLoginLogs = (userId) => {
        const sortedLoginLogs = (loginLogs[userId] || []).sort((a, b) => {
            return sortOrder === 'desc' ? b.id - a.id : a.id - b.id;
        });

        // Calculate pagination
        const indexOfLastLog = currentPage * logsPerPage;
        const indexOfFirstLog = indexOfLastLog - logsPerPage;
        const currentLoginLogs = sortedLoginLogs.slice(indexOfFirstLog, indexOfLastLog);

        return {
            logs: currentLoginLogs,
            totalPages: Math.ceil(sortedLoginLogs.length / logsPerPage),
        };
    };

    const handlePageChange = (pageNumber) => {
        setCurrentPage(pageNumber);
    };

    const toggleSortOrder = () => {
        setSortOrder(sortOrder === 'desc' ? 'asc' : 'desc');
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

            {isMobile || true ? ( // TODO: I really like this UI even on desktop, should delete old code in future
                // Mobile view
                <div className="space-y-4">
                    {users.map((user) => (
                        <UserCard
                            key={user.id}
                            user={user}
                            onEdit={() => handleEditUser(user)}
                            onChangePassword={() => {
                                setSelectedUser(user);
                                setIsPasswordDialogOpen(true);
                            }}
                            onDelete={() => handleDeleteUser(user.id)}
                            onUserClick={() => handleUserClick(user.id)}
                            expandedUser={expandedUser}
                            userLogs={userLogs}
                            loginLogs={loginLogs}
                            activeTab={activeTab}
                            setActiveTab={setActiveTab}
                            uiIsLoadingLogs={uiIsLoadingLogs}
                        />
                    ))}
                </div>
            ) : (
                // Desktop view with the table
                <div className="shadow-md rounded-lg overflow-x-auto">
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
                                <React.Fragment key={user.id}>
                                    <TableRow
                                        className="hover:bg-gray-100 cursor-pointer"
                                        onClick={() => handleUserClick(user.id)}
                                    >
                                        <TableCell className="font-medium">{`${user.firstName} ${user.lastName}`}</TableCell>
                                        <TableCell>{user.username}</TableCell>
                                        <TableCell>{user.email}</TableCell>
                                        <TableCell>{user.role}</TableCell>
                                        <TableCell>{formatDate(user.createdAt)}</TableCell>
                                        <TableCell>{formatDate(user.updatedAt)}</TableCell>
                                        <TableCell className="text-right">
                                            <TooltipProvider>
                                                <Tooltip>
                                                    <TooltipTrigger asChild>
                                                        <Button
                                                            variant="ghost"
                                                            size="sm"
                                                            onClick={(e) => {
                                                                e.stopPropagation();
                                                                handleEditUser(user);
                                                            }}
                                                        >
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
                                                        <Button
                                                            variant="ghost"
                                                            size="sm"
                                                            onClick={(e) => {
                                                                e.stopPropagation();
                                                                setSelectedUser(user);
                                                                setIsPasswordDialogOpen(true);
                                                            }}
                                                        >
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
                                                        <Button
                                                            variant="ghost"
                                                            size="sm"
                                                            onClick={(e) => {
                                                                e.stopPropagation();
                                                                handleDeleteUser(user.id);
                                                            }}
                                                        >
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
                                    {expandedUser === user.id && (
                                        <TableRow>
                                            <TableCell colSpan={7}>
                                                <div className="p-4">
                                                    <div className="flex justify-between items-center mb-4">
                                                        <h3 className="text-lg font-semibold">
                                                            User Logs
                                                        </h3>
                                                        <div className="flex gap-4">
                                                            <Button
                                                                variant={
                                                                    activeTab === 'actions'
                                                                        ? 'outline'
                                                                        : 'solid'
                                                                }
                                                                onClick={() =>
                                                                    setActiveTab('actions')
                                                                }
                                                            >
                                                                Actions
                                                            </Button>
                                                            <Button
                                                                variant={
                                                                    activeTab === 'logins'
                                                                        ? 'outline'
                                                                        : 'solid'
                                                                }
                                                                onClick={() =>
                                                                    setActiveTab('logins')
                                                                }
                                                            >
                                                                Logins
                                                            </Button>
                                                        </div>
                                                    </div>

                                                    {activeTab === 'actions' && (
                                                        <div>
                                                            <Table>
                                                                <TableHeader>
                                                                    <TableRow>
                                                                        <TableHead>
                                                                            Action
                                                                        </TableHead>
                                                                        <TableHead>
                                                                            Timestamp
                                                                        </TableHead>
                                                                        <TableHead>
                                                                            Metadata
                                                                        </TableHead>
                                                                    </TableRow>
                                                                </TableHeader>
                                                                <TableBody>
                                                                    {getFilteredAndSortedLogs(
                                                                        user.id
                                                                    ).logs.length > 0 ? (
                                                                        getFilteredAndSortedLogs(
                                                                            user.id
                                                                        ).logs.map((log) => (
                                                                            <TableRow key={log.id}>
                                                                                <TableCell>
                                                                                    {log.action}
                                                                                </TableCell>
                                                                                <TableCell>
                                                                                    {formatTimestamp(
                                                                                        log.timestamp
                                                                                    )}
                                                                                </TableCell>
                                                                                <TableCell>
                                                                                    <MetadataViewer
                                                                                        metadata={
                                                                                            log.metaData
                                                                                        }
                                                                                        title={`Metadata for ${log.action}`}
                                                                                    />
                                                                                </TableCell>
                                                                            </TableRow>
                                                                        ))
                                                                    ) : (
                                                                        <TableRow>
                                                                            <TableCell
                                                                                colSpan={3}
                                                                                className="text-center"
                                                                            >
                                                                                No logs found
                                                                            </TableCell>
                                                                        </TableRow>
                                                                    )}
                                                                </TableBody>
                                                            </Table>
                                                            <div className="mt-4 flex justify-center">
                                                                <Pagination
                                                                    currentPage={currentPage}
                                                                    totalPages={
                                                                        getFilteredAndSortedLogs(
                                                                            user.id
                                                                        ).totalPages
                                                                    }
                                                                    onPageChange={handlePageChange}
                                                                />
                                                            </div>
                                                        </div>
                                                    )}

                                                    {activeTab === 'logins' && (
                                                        <div>
                                                            <Table>
                                                                <TableHeader>
                                                                    <TableRow>
                                                                        <TableHead>
                                                                            IP Address
                                                                        </TableHead>
                                                                        <TableHead>
                                                                            User Agent
                                                                        </TableHead>
                                                                        <TableHead>
                                                                            Login Time
                                                                        </TableHead>
                                                                    </TableRow>
                                                                </TableHeader>
                                                                <TableBody>
                                                                    {getFilteredAndSortedLoginLogs(
                                                                        user.id
                                                                    ).logs.length > 0 ? (
                                                                        getFilteredAndSortedLoginLogs(
                                                                            user.id
                                                                        ).logs.map((login) => (
                                                                            <TableRow
                                                                                key={login.id}
                                                                            >
                                                                                <TableCell>
                                                                                    {
                                                                                        login.ipAddress
                                                                                    }
                                                                                </TableCell>
                                                                                <TableCell>
                                                                                    {
                                                                                        login.userAgent
                                                                                    }
                                                                                </TableCell>
                                                                                <TableCell>
                                                                                    {formatDate(
                                                                                        login.loginTime
                                                                                    )}
                                                                                </TableCell>
                                                                            </TableRow>
                                                                        ))
                                                                    ) : (
                                                                        <TableRow>
                                                                            <TableCell
                                                                                colSpan={3}
                                                                                className="text-center"
                                                                            >
                                                                                No login logs found
                                                                            </TableCell>
                                                                        </TableRow>
                                                                    )}
                                                                </TableBody>
                                                            </Table>
                                                            <div className="mt-4 flex justify-center">
                                                                <Pagination
                                                                    currentPage={currentPage}
                                                                    totalPages={
                                                                        getFilteredAndSortedLoginLogs(
                                                                            user.id
                                                                        ).totalPages
                                                                    }
                                                                    onPageChange={handlePageChange}
                                                                />
                                                            </div>
                                                        </div>
                                                    )}
                                                </div>
                                            </TableCell>
                                        </TableRow>
                                    )}
                                </React.Fragment>
                            ))}
                        </TableBody>
                    </Table>
                </div>
            )}

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
                                onChange={(e) =>
                                    setSelectedUser({...selectedUser, firstName: e.target.value})
                                }
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
                                onChange={(e) =>
                                    setSelectedUser({...selectedUser, lastName: e.target.value})
                                }
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
                                onChange={(e) =>
                                    setSelectedUser({...selectedUser, username: e.target.value})
                                }
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
                                onChange={(e) =>
                                    setSelectedUser({...selectedUser, email: e.target.value})
                                }
                                className="col-span-3"
                            />
                        </div>
                        <div className="grid grid-cols-4 items-center gap-4">
                            <Label htmlFor="role" className="text-right">
                                Role
                            </Label>
                            <Select
                                value={selectedUser?.role || ''}
                                onValueChange={(value) =>
                                    setSelectedUser({...selectedUser, role: value})
                                }
                            >
                                <SelectTrigger className="col-span-3">
                                    <SelectValue placeholder="Select a role"/>
                                </SelectTrigger>
                                <SelectContent onClick={(e) => e.stopPropagation()}>
                                    {roles.map((role) => (
                                        <SelectItem key={role} value={role}>
                                            {role}
                                        </SelectItem>
                                    ))}
                                </SelectContent>
                            </Select>
                        </div>
                    </div>
                    <DialogFooter className="flex flex-col sm:flex-row sm:space-x-4">
                        <Button variant="outline" onClick={() => setIsEditDialogOpen(false)}>
                            Cancel
                        </Button>
                        <Button onClick={handleSaveUser} className="mt-2 sm:mt-0">
                            Save
                        </Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>

            <Dialog open={isPasswordDialogOpen} onOpenChange={setIsPasswordDialogOpen}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>Change Password</DialogTitle>
                    </DialogHeader>
                    <form
                        onSubmit={(e) => {
                            e.preventDefault();
                            handleChangePassword();
                        }}
                    >
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
                        <DialogFooter className="flex flex-col sm:flex-row sm:space-x-4">
                            <Button
                                type="button"
                                variant="outline"
                                onClick={() => setIsPasswordDialogOpen(false)}
                            >
                                Cancel
                            </Button>
                            <Button type="submit" className="mt-2 sm:mt-0">
                                Save
                            </Button>
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

const Pagination = ({currentPage, totalPages, onPageChange}) => {
    return (
        <div className="flex items-center space-x-2">
            <Button
                variant="outline"
                size="sm"
                onClick={() => onPageChange(currentPage - 1)}
                disabled={currentPage === 1}
            >
                Previous
            </Button>
            <span>{`Page ${currentPage} of ${totalPages}`}</span>
            <Button
                variant="outline"
                size="sm"
                onClick={() => onPageChange(currentPage + 1)}
                disabled={currentPage === totalPages}
            >
                Next
            </Button>
        </div>
    );
};


export default UserManagementView;