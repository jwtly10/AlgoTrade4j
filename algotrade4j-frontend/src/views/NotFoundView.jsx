import React from 'react';
import {Box, Button, Typography} from '@mui/material';
import {Link} from 'react-router-dom';

function NotFoundView() {
    return (
        <Box
            display="flex"
            flexDirection="column"
            justifyContent="center"
            alignItems="center"
            minHeight="80vh"
        >
            <Typography variant="h1" color="primary" gutterBottom>
                404
            </Typography>
            <Typography variant="h5" color="textSecondary" gutterBottom>
                Oops! Page not found.
            </Typography>
            <Typography variant="body1" color="textSecondary" paragraph>
                The page you are looking for might have been removed, had its name changed, or is temporarily unavailable.
            </Typography>
            <Button component={Link} to="/" variant="contained" color="primary">
                Go to Homepage
            </Button>
        </Box>
    );
}

export default NotFoundView;