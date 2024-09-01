import React, {useState} from 'react';
import {Box, Button, IconButton, InputAdornment, TextField} from '@mui/material';
import {Visibility, VisibilityOff} from '@mui/icons-material';
import {authClient} from '../api/apiClient';
import {Toast} from '../components/Toast';
import log from '../logger.js'

function LoginView({setUser, onSuccess}) {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [showPassword, setShowPassword] = useState(false);
    const [toast, setToast] = useState({
        open: false,
        message: '',
        severity: 'info',
        duration: 6000
    });

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            const userData = await authClient.login(username, password);
            setUser(userData);
            if (onSuccess) onSuccess();
        } catch (error) {
            log.error('Login failed:', error);
            setToast({
                open: true,
                message: error.response.data.message || 'Invalid username or password' + error,
                severity: 'error',
                duration: 6000
            });
        }
    };

    const handleTogglePasswordVisibility = () => {
        setShowPassword(!showPassword);
    };

    const handleToastClose = (event, reason) => {
        if (reason === 'clickaway') {
            return;
        }
        setToast(prev => ({...prev, open: false}));
    };

    return (
        <Box component="form" onSubmit={handleSubmit} sx={{mt: 1}}>
            <TextField
                margin="normal"
                required
                fullWidth
                label="Username"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
            />
            <TextField
                margin="normal"
                required
                fullWidth
                label="Password"
                type={showPassword ? "text" : "password"}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                InputProps={{
                    endAdornment: (
                        <InputAdornment position="end">
                            <IconButton
                                aria-label="toggle password visibility"
                                onClick={handleTogglePasswordVisibility}
                                edge="end"
                            >
                                {showPassword ? <VisibilityOff/> : <Visibility/>}
                            </IconButton>
                        </InputAdornment>
                    )
                }}
            />
            <Button type="submit" fullWidth variant="contained" sx={{mt: 3, mb: 2}}>
                Login
            </Button>
            <Toast
                {...toast}
                onClose={handleToastClose}
            />
        </Box>
    );
}

export default LoginView;