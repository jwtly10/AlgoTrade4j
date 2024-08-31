import React, {useState} from 'react';
import {AppBar, Box, Button, Toolbar, Typography} from '@mui/material';
import {Link, useLocation} from 'react-router-dom';
import {authClient} from '../../api/apiClient';
import {Toast} from "../Toast.jsx";

function Navbar({user, setUser}) {
    const location = useLocation();
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

    const isActive = (path) => location.pathname === path;

    return (
        <AppBar position="static">
            <Toolbar>
                <Typography variant="h6" component="div" sx={{mr: 2}}>
                    AlgoTrade4J
                </Typography>
                <Box sx={{display: 'flex', alignItems: 'center', flexGrow: 1}}>
                    {user && (
                        <>
                            <Button
                                color="inherit"
                                component={Link}
                                to="/backtest"
                                sx={{
                                    backgroundColor: isActive('/backtest') ? 'rgba(255,255,255,0.2)' : 'transparent',
                                    '&:hover': {backgroundColor: 'rgba(255,255,255,0.1)'},
                                    textTransform: 'none'
                                }}
                            >
                                Backtest
                            </Button>
                            <Button
                                color="inherit"
                                component={Link}
                                to="/optimisation"
                                sx={{
                                    backgroundColor: isActive('/optimisation') ? 'rgba(255,255,255,0.2)' : 'transparent',
                                    '&:hover': {backgroundColor: 'rgba(255,255,255,0.1)'},
                                    textTransform: 'none',
                                    ml: 1
                                }}
                            >
                                Optimise
                            </Button>
                            {user.role === 'ADMIN' && (
                                <>
                                <Button
                                    color="inherit"
                                    component={Link}
                                    to="/users"
                                    sx={{
                                        backgroundColor: isActive('/users') ? 'rgba(255,255,255,0.2)' : 'transparent',
                                        '&:hover': {backgroundColor: 'rgba(255,255,255,0.1)'},
                                        textTransform: 'none',
                                        ml: 1
                                    }}
                                >
                                    Manage Users
                                </Button>
                                    <Button
                                        color="inherit"
                                        component={Link}
                                        to="/monitor"
                                        sx={{
                                            backgroundColor: isActive('/monitor') ? 'rgba(255,255,255,0.2)' : 'transparent',
                                            '&:hover': {backgroundColor: 'rgba(255,255,255,0.1)'},
                                            textTransform: 'none',
                                            ml: 1
                                        }}
                                    >
                                        Monitor
                                    </Button>
                                </>
                            )}
                        </>
                    )}
                </Box>
                <Box sx={{display: 'flex', alignItems: 'center'}}>
                    {user ? (
                        <>
                            <Typography variant="body2" sx={{mr: 1}}>
                                Logged in as {user.username}
                            </Typography>
                            <Button
                                color="inherit"
                                onClick={handleLogout}
                                sx={{textTransform: 'none'}}
                            >
                                (Logout)
                            </Button>
                        </>
                    ) : (
                        <>
                            <Button
                                color="inherit"
                                component={Link}
                                to="/login"
                                sx={{
                                    backgroundColor: isActive('/login') ? 'rgba(255,255,255,0.2)' : 'transparent',
                                    '&:hover': {backgroundColor: 'rgba(255,255,255,0.1)'},
                                    textTransform: 'none'
                                }}
                            >
                                Login
                            </Button>
                            <Button
                                color="inherit"
                                component={Link}
                                to="/signup"
                                sx={{
                                    backgroundColor: isActive('/signup') ? 'rgba(255,255,255,0.2)' : 'transparent',
                                    '&:hover': {backgroundColor: 'rgba(255,255,255,0.1)'},
                                    textTransform: 'none'
                                }}
                            >
                                Sign Up
                            </Button>
                        </>
                    )}
                </Box>
            </Toolbar>
            <Toast
                {...toast}
                onClose={handleToastClose}
            />
        </AppBar>
    );
}

export default Navbar;