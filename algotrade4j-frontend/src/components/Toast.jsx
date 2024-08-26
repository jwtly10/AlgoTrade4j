import React from 'react';
import {Alert, Snackbar} from '@mui/material';

/**
 * Toast component for displaying messages with different severity levels.
 *
 * @param {Object} props
 * @param {boolean} props.open - Controls whether the toast is visible.
 * @param {string} props.message - The message to display in the toast.
 * @param {Function} props.onClose - Function to call when the toast is closed.
 * @param {('success'|'info'|'warning'|'error')} [props.severity='info'] - The severity level of the toast.
 *   - 'success': Indicates a successful or positive action (green).
 *   - 'info': Provides general information (blue).
 *   - 'warning': Indicates a warning that might need attention (amber).
 *   - 'error': Indicates an error or critical issue (red).
 * @param {number} [props.duration=6000] - Duration in milliseconds to show the toast.
 */
export const Toast = ({open, message, onClose, severity = 'info', duration = 6000}) => {
    return (
        <Snackbar
            open={open}
            autoHideDuration={duration}
            onClose={onClose}
            anchorOrigin={{vertical: 'top', horizontal: 'center'}}
            style={{top: '64px'}} // Adjust this value as needed
        >
            <Alert onClose={onClose} severity={severity} variant="filled" elevation={6}>
                {message}
            </Alert>
        </Snackbar>
    );
};