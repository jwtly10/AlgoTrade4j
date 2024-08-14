import React from 'react';
import {Alert, Snackbar} from '@mui/material';

export const ErrorToast = ({open, message, onClose}) => {
    return (
        <Snackbar
            open={open}
            autoHideDuration={6000}
            onClose={onClose}
            anchorOrigin={{vertical: 'top', horizontal: 'center'}}
        >
            <Alert onClose={onClose} severity="error" variant="filled" elevation={6}>
                {message}
            </Alert>
        </Snackbar>
    );
};