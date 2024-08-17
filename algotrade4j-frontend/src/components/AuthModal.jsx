import React, {useState} from 'react';
import {Box, Dialog, DialogContent, DialogTitle, Tab, Tabs} from '@mui/material';
import Login from './Login';
import SignUp from './SignUp';

function AuthModal({open, onClose, setUser}) {
    const [tabValue, setTabValue] = useState(0);

    const handleTabChange = (event, newValue) => {
        setTabValue(newValue);
    };

    return (
        <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
            <DialogTitle>
                <Tabs value={tabValue} onChange={handleTabChange} centered>
                    <Tab label="Login"/>
                    <Tab label="Sign Up"/>
                </Tabs>
            </DialogTitle>
            <DialogContent>
                <Box sx={{p: 2}}>
                    {tabValue === 0 ? (
                        <Login setUser={setUser} onSuccess={onClose}/>
                    ) : (
                        <SignUp setUser={setUser} onSuccess={onClose}/>
                    )}
                </Box>
            </DialogContent>
        </Dialog>
    );
}

export default AuthModal;