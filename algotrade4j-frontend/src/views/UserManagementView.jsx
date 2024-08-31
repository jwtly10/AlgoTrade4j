import React, {useEffect, useState} from 'react';
import {adminClient} from '../api/apiClient';
import {Toast} from "../components/Toast.jsx";
import EditIcon from '@mui/icons-material/Edit';
import LockIcon from '@mui/icons-material/Lock';
import DeleteIcon from '@mui/icons-material/Delete';
import {Box, Button, ButtonGroup, Dialog, DialogActions, DialogContent, DialogTitle, FormControl, IconButton, InputLabel, MenuItem, Paper, Select, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, TextField, Tooltip, Typography} from "@mui/material";
import CreateUserForm from "../components/CreateUserForm.jsx";


const UserManagementView = ({loggedInUser}) => {
    const [users, setUsers] = useState([]);
    const [roles, setRoles] = useState([]);
    const [selectedUser, setSelectedUser] = useState(null);
    const [isEditDialogOpen, setIsEditDialogOpen] = useState(false);
    const [isPasswordDialogOpen, setIsPasswordDialogOpen] = useState(false);
    const [isCreateDialogOpen, setIsCreateDialogOpen] = useState(false);
    const [newPassword, setNewPassword] = useState('');
    const [toast, setToast] = useState({
        open: false,
        message: '',
        severity: 'info',
        duration: 6000
    });

    useEffect(() => {
        if (loggedInUser && loggedInUser.role !== 'ADMIN') {
            setToast({
                open: true,
                message: 'You do not have permission to access this page',
                severity: 'error'
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
            console.error('Failed to fetch users:', error);
            setToast({
                open: true,
                message: error.response.data.message || 'Failed to fetch user: ' + error,
                severity: 'error'
            });
        }
    };

    const fetchRoles = async () => {
        try {
            const fetchedRoles = await adminClient.getRoles();
            setRoles(fetchedRoles);
        } catch (error) {
            console.error('Failed to fetch roles:', error);
            setToast({
                open: true,
                message: error.response.data.message || 'Failed to fetch roles: ' + error,
                severity: 'error'
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
            setToast({
                open: true,
                message: `User ${newUserData.username} created successfully`,
                severity: 'success',
            });
        } catch (error) {
            console.error('Failed to create user:', error);
            setToast({
                open: true,
                message: error.response.data.message || 'Failed to create user: ' + error,
                severity: 'error',
            });
        }
    };

    const handleSaveUser = async () => {
        if (selectedUser.username === loggedInUser.username) {
            if (selectedUser.role !== loggedInUser.role) { // They've changed their own role
                setToast({
                    open: true,
                    message: "You can't change your own role. Please ask another admin.",
                    severity: 'error'
                })
            }
            return;
        }

        try {
            await adminClient.updateUser(selectedUser.id, selectedUser);
            setIsEditDialogOpen(false);
            fetchUsers();
            setToast({
                open: true,
                message: `User ${selectedUser.username} updated successfully`,
                severity: 'success',
            })
        } catch (error) {
            console.error('Failed to update user:', error);
            setToast({
                open: true,
                message: error.response.data.message || 'Failed to update user: ' + error,
                severity: 'error',
            });
        }
    };

    const handleChangePassword = async () => {
        try {
            await adminClient.changeUserPassword(selectedUser.id, newPassword);
            setIsPasswordDialogOpen(false);
            setNewPassword('');
            setToast({
                open: true,
                message: `Password for user ${selectedUser.username} changed successfully`,
                severity: 'success',
            })
        } catch (error) {
            console.error('Failed to change password:', error);
            setToast({
                open: true,
                message: error.response.data.message || 'Failed to change password: ' + error,
                severity: 'error',
            });
        }
    };

    const handleDeleteUser = async (userId) => {
        if (window.confirm('Are you sure you want to delete this user?')) {
            try {
                await adminClient.deleteUser(userId);
                fetchUsers();
                setToast({
                    open: true,
                    message: 'User deleted successfully',
                    severity: 'success',
                })
            } catch (error) {
                console.error('Failed to delete user:', error);
                setToast({
                    open: true,
                    message: error.response.data.message || 'Failed to delete user: ' + error,
                    severity: 'error',
                    duration: 6000
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

    const handleToastClose = (event, reason) => {
        if (reason === 'clickaway') {
            return;
        }
        setToast(prev => ({...prev, open: false}));
    };

    return (
        <Box sx={{padding: 3}}>
            <Typography variant="h4" gutterBottom>User Management</Typography>
            <Box sx={{display: 'flex', justifyContent: 'flex-end', mb: 2}}>
                <Button
                    variant="contained"
                    color="primary"
                    onClick={() => setIsCreateDialogOpen(true)}
                >
                    Create User
                </Button>
            </Box>
            <Paper sx={{width: '100%', overflow: 'hidden'}}>
                <TableContainer sx={{maxHeight: 'calc(100vh - 200px)'}}>
                    <Table stickyHeader>
                        <TableHead>
                            <TableRow>
                                <TableCell>First Name</TableCell>
                                <TableCell>Last Name</TableCell>
                                <TableCell sx={{fontWeight: 'bold'}}>Username</TableCell>
                                <TableCell>Email</TableCell>
                                <TableCell>Role</TableCell>
                                <TableCell>Created</TableCell>
                                <TableCell>Last Updated</TableCell>
                                <TableCell align="center">Actions</TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {users.map((user) => (
                                <TableRow key={user.id} hover>
                                    <TableCell>{user.firstName}</TableCell>
                                    <TableCell>{user.lastName}</TableCell>
                                    <TableCell sx={{fontWeight: 'bold'}}>{user.username}</TableCell>
                                    <TableCell>{user.email}</TableCell>
                                    <TableCell>{user.role}</TableCell>
                                    <TableCell>{formatDate(user.createdAt)}</TableCell>
                                    <TableCell>{formatDate(user.updatedAt)}</TableCell>
                                    <TableCell align="center">
                                        <ButtonGroup size="small" aria-label="user actions">
                                            <Tooltip title="Edit User">
                                                <IconButton onClick={() => handleEditUser(user)}>
                                                    <EditIcon/>
                                                </IconButton>
                                            </Tooltip>
                                            <Tooltip title="Change Password">
                                                <IconButton onClick={() => {
                                                    setSelectedUser(user);
                                                    setIsPasswordDialogOpen(true);
                                                }}>
                                                    <LockIcon/>
                                                </IconButton>
                                            </Tooltip>
                                            <Tooltip title="Delete User">
                                                <IconButton onClick={() => handleDeleteUser(user.id)}>
                                                    <DeleteIcon/>
                                                </IconButton>
                                            </Tooltip>
                                        </ButtonGroup>
                                    </TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                </TableContainer>
            </Paper>

            <Dialog open={isEditDialogOpen} onClose={() => setIsEditDialogOpen(false)} disableEnforceFocus disableRestoreFocus aria-hidden={false}>
                <DialogTitle>Edit User</DialogTitle>
                <DialogContent>
                    <TextField
                        label="First Name"
                        value={selectedUser?.firstName || ''}
                        onChange={(e) => setSelectedUser({...selectedUser, firstName: e.target.value})}
                        fullWidth
                        margin="normal"
                        autoComplete="off"
                    />
                    <TextField
                        label="Last Name"
                        value={selectedUser?.lastName || ''}
                        onChange={(e) => setSelectedUser({...selectedUser, lastName: e.target.value})}
                        fullWidth
                        margin="normal"
                        autoComplete="off"
                    />
                    <TextField
                        label="Username"
                        value={selectedUser?.username || ''}
                        onChange={(e) => setSelectedUser({...selectedUser, username: e.target.value})}
                        fullWidth
                        margin="normal"
                        autoComplete="off"
                    />
                    <TextField
                        label="Email"
                        value={selectedUser?.email || ''}
                        onChange={(e) => setSelectedUser({...selectedUser, email: e.target.value})}
                        fullWidth
                        margin="normal"
                        autoComplete="off"
                    />
                    <FormControl fullWidth margin="normal">
                        <InputLabel id="role-select-label">Role</InputLabel>
                        <Select
                            labelId="role-select-label"
                            value={selectedUser?.role || ''}
                            onChange={(e) => setSelectedUser({...selectedUser, role: e.target.value})}
                            label="Role"
                        >
                            {roles.map((role) => (
                                <MenuItem key={role} value={role}>{role}</MenuItem>
                            ))}
                        </Select>
                    </FormControl>
                </DialogContent>
                <DialogActions sx={{p: 3}}>
                    <Button onClick={() => setIsEditDialogOpen(false)} variant="outlined">Cancel</Button>
                    <Button onClick={handleSaveUser} variant="contained">Save</Button>
                </DialogActions>
            </Dialog>

            <Dialog
                open={isPasswordDialogOpen}
                onClose={() => setIsPasswordDialogOpen(false)}
                maxWidth="sm"
                fullWidth
                disableEnforceFocus
                disableRestoreFocus
                aria-hidden={false}
            >
                <form onSubmit={(e) => {
                    e.preventDefault();
                    handleChangePassword();

                }}>
                    <DialogTitle sx={{fontSize: '1.5rem', pt: 3, px: 4}}>Change Password</DialogTitle>
                    <DialogContent sx={{p: 4}}>
                        <input
                            type="text"
                            autoComplete="username"
                            style={{display: 'none'}}
                            aria-hidden="true"
                        />
                        <TextField
                            label="New Password"
                            type="password"
                            value={newPassword}
                            onChange={(e) => setNewPassword(e.target.value)}
                            fullWidth
                            margin="normal"
                            variant="outlined"
                            autoComplete="new-password"
                        />
                    </DialogContent>
                    <DialogActions sx={{p: 4}}>
                        <Button
                            onClick={() => setIsPasswordDialogOpen(false)}
                            variant="outlined"
                            sx={{mr: 1}}
                            type="button"
                        >
                            Cancel
                        </Button>
                        <Button
                            type="submit"
                            variant="contained"
                        >
                            Save
                        </Button>
                    </DialogActions>
                </form>
            </Dialog>
            <Dialog open={isCreateDialogOpen} onClose={() => setIsCreateDialogOpen(false)} disableEnforceFocus disableRestoreFocus aria-hidden={false}>
                <DialogTitle>Create New User</DialogTitle>
                <DialogContent>
                    <CreateUserForm onSubmit={handleCreateUser} roles={roles}/>
                </DialogContent>
            </Dialog>
            <Toast
                {...toast}
                onClose={handleToastClose}
            />
        </Box>
    );
};

export default UserManagementView;