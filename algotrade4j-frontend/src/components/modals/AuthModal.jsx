import React, {useState} from 'react';
import {Box, Dialog, DialogContent, DialogTitle, Tab, Tabs} from '@mui/material';
import LoginView from '../../views/LoginView';
import SignUpView from '../../views/SignUpView';

function AuthModal({open, onClose, setUser}) {
    const [tabValue, setTabValue] = useState(0);

    const handleTabChange = (event, newValue) => {
        setTabValue(newValue);
    };

    return (
        <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth disableEnforceFocus disableRestoreFocus aria-hidden={false}>
            <DialogTitle>
                <Tabs value={tabValue} onChange={handleTabChange} centered>
                    <Tab label="Login"/>
                    <Tab label="Sign Up"/>
                </Tabs>
            </DialogTitle>
            <DialogContent>
                <Box sx={{p: 2}}>
                    {tabValue === 0 ? (
                        <LoginView setUser={setUser} onSuccess={onClose}/>
                    ) : (
                        <SignUpView setUser={setUser} onSuccess={onClose}/>
                    )}
                </Box>
            </DialogContent>
        </Dialog>
    );
}

export default AuthModal;