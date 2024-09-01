import React, {useState} from 'react';
import {Box, Button, TextField, Typography} from '@mui/material';
import {authClient} from '../api/apiClient';
import {Toast} from "../components/Toast.jsx";
import log from '../logger.js'

function SignUpView({setUser, onSuccess}) {
    const [formData, setFormData] = useState({
        username: '',
        email: '',
        password: '',
        firstName: '',
        lastName: ''
    });
    const [toast, setToast] = useState({
        open: false,
        message: '',
        severity: 'info',
        duration: 6000
    });

    const handleChange = (e) => {
        setFormData({...formData, [e.target.name]: e.target.value});
    };

    const handleToastClose = (event, reason) => {
        if (reason === 'clickaway') {
            return;
        }
        setToast(prev => ({...prev, open: false}));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            await authClient.signup(formData);
            const userData = await authClient.login(formData.username, formData.password);
            setUser(userData);
            if (onSuccess) onSuccess();
        } catch (error) {
            log.error('Signup failed:', error);
            setToast({
                open: true,
                message: error.response.data.message || 'Sign up failed',
                severity: 'error',
                duration: 6000
            });
        }
    };

    const isSignUpEnabled = import.meta.env.VITE_ENABLE_SIGNUP === 'true';
    if (!isSignUpEnabled) {
        return (
            <Box sx={{textAlign: 'center', pb: 3}}>
                <Typography variant="h6" gutterBottom>
                    Sign Up Unavailable
                </Typography>
                <Typography variant="body1">
                    We're sorry, but sign up is currently not available.
                    <br/>
                    This is probably a privately hosted instance of AlgoTrade4J. Please contact the owner for more information.
                </Typography>
            </Box>
        );
    }

    return (
        <Box component="form" onSubmit={handleSubmit} sx={{mt: 1}}>
            <TextField
                margin="normal"
                required
                fullWidth
                name="username"
                label="Username"
                value={formData.username}
                onChange={handleChange}
                autoComplete="off"
            />
            <TextField
                margin="normal"
                required
                fullWidth
                name="email"
                label="Email"
                type="email"
                value={formData.email}
                onChange={handleChange}
                autoComplete="off"
            />
            <TextField
                margin="normal"
                required
                fullWidth
                name="password"
                label="Password"
                type="password"
                value={formData.password}
                onChange={handleChange}
                autoComplete="off"
            />
            <TextField
                margin="normal"
                required
                fullWidth
                name="firstName"
                label="First Name"
                value={formData.firstName}
                onChange={handleChange}
                autoComplete="off"
            />
            <TextField
                margin="normal"
                required
                fullWidth
                name="lastName"
                label="Last Name"
                value={formData.lastName}
                onChange={handleChange}
                autoComplete="off"
            />
            <Button type="submit" fullWidth variant="contained" sx={{mt: 3, mb: 2}}>
                Sign Up
            </Button>
            <Toast
                {...toast}
                onClose={handleToastClose}
            />
        </Box>
    );
}

export default SignUpView;