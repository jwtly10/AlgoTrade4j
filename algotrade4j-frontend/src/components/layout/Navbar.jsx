import React, {useState} from 'react';
import {AppBar, Avatar, Box, Button, IconButton, Menu, MenuItem, Toolbar, Typography} from '@mui/material';
import {Link, useLocation} from 'react-router-dom';
import {authClient} from '../../api/apiClient';
import {Toast} from "../Toast.jsx";
import AdminPanelSettingsIcon from '@mui/icons-material/AdminPanelSettings';
import ArrowDropDownIcon from '@mui/icons-material/ArrowDropDown';

function Navbar({user, setUser}) {
    const location = useLocation();
    const [anchorEl, setAnchorEl] = useState(null);
    const [adminAnchorEl, setAdminAnchorEl] = useState(null);
    const [toast, setToast] = useState({
        open: false,
        message: '',
        severity: 'info',
        duration: 6000
    });

    const handleLogout = async () => {
        try {
            await authClient.logout();
            setUser(null);
            handleMenuClose();
        } catch (error) {
            setToast({
                open: true,
                message: error.response.data.message || 'Error logging out',
                severity: 'error',
                duration: 6000
            });
        }
    };

    const handleToastClose = (event, reason) => {
        if (reason === 'clickaway') {
            return;
        }
        setToast(prev => ({...prev, open: false}));
    };

    const handleMenuOpen = (event) => {
        setAnchorEl(event.currentTarget);
    };

    const handleMenuClose = () => {
        setAnchorEl(null);
    };

    const handleAdminMenuOpen = (event) => {
        setAdminAnchorEl(event.currentTarget);
    };

    const handleAdminMenuClose = () => {
        setAdminAnchorEl(null);
    };

    const isActive = (path) => location.pathname === path;

    const buttonStyle = (path) => ({
        backgroundColor: isActive(path) ? 'rgba(255,255,255,0.2)' : 'transparent',
        '&:hover': {backgroundColor: 'rgba(255,255,255,0.1)'},
        textTransform: 'none',
        mx: 1
    });

    return (
        <AppBar position="static">
            <Toolbar>
                <Typography variant="h6" component="div" sx={{flexGrow: 1}}>
                    AlgoTrade4J
                </Typography>
                {user && (
                    <Box sx={{display: 'flex', alignItems: 'center'}}>
                        <Button
                            color="inherit"
                            component={Link}
                            to="/backtest"
                            sx={buttonStyle('/backtest')}
                        >
                            Backtest
                        </Button>
                        <Button
                            color="inherit"
                            component={Link}
                            to="/optimisation"
                            sx={buttonStyle('/optimisation')}
                        >
                            Optimise
                        </Button>
                        {user.role === 'ADMIN' && (
                            <>
                                <IconButton
                                    color="inherit"
                                    onClick={handleAdminMenuOpen}
                                    sx={{ml: 1}}
                                >
                                    <AdminPanelSettingsIcon/>
                                </IconButton>
                                <Menu
                                    anchorEl={adminAnchorEl}
                                    open={Boolean(adminAnchorEl)}
                                    onClose={handleAdminMenuClose}
                                >
                                    <MenuItem
                                        component={Link}
                                        to="/users"
                                        onClick={handleAdminMenuClose}
                                    >
                                        Manage Users
                                    </MenuItem>
                                    <MenuItem
                                        component={Link}
                                        to="/monitor"
                                        onClick={handleAdminMenuClose}
                                    >
                                        Monitor
                                    </MenuItem>
                                </Menu>
                            </>
                        )}
                        <Box
                            onClick={handleMenuOpen}
                            sx={{
                                display: 'flex',
                                alignItems: 'center',
                                cursor: 'pointer',
                                ml: 2,
                                '&:hover': {backgroundColor: 'rgba(255,255,255,0.1)'},
                                borderRadius: 1,
                                padding: '4px 8px'
                            }}
                        >
                            <Avatar sx={{width: 32, height: 32, mr: 1}}>
                                {user.firstName?.charAt(0).toUpperCase()}
                            </Avatar>
                            <Typography variant="body1" sx={{mr: 1}}>
                                {user.firstName}
                            </Typography>
                            <ArrowDropDownIcon/>
                        </Box>
                        <Menu
                            anchorEl={anchorEl}
                            open={Boolean(anchorEl)}
                            onClose={handleMenuClose}
                        >
                            <MenuItem disabled>
                                Logged in as {user.username}
                            </MenuItem>
                            <MenuItem onClick={handleLogout}>Logout</MenuItem>
                        </Menu>
                    </Box>
                )}
                {!user && (
                    <Box>
                        <Button
                            color="inherit"
                            component={Link}
                            to="/login"
                            sx={buttonStyle('/login')}
                        >
                            Login
                        </Button>
                        <Button
                            color="inherit"
                            component={Link}
                            to="/signup"
                            sx={buttonStyle('/signup')}
                        >
                            Sign Up
                        </Button>
                    </Box>
                )}
            </Toolbar>
            <Toast
                {...toast}
                onClose={handleToastClose}
            />
        </AppBar>
    );
}

export default Navbar;