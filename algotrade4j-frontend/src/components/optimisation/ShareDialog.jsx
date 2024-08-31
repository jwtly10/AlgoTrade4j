import React from 'react';
import {Avatar, Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, Divider, List, ListItem, ListItemAvatar, ListItemText, TextField, Typography} from '@mui/material';

const ShareDialog = ({open, onClose, users, handleShareWithUser}) => {
    const [searchTerm, setSearchTerm] = React.useState('');

    const filteredUsers = users.filter(user =>
        user.firstName.toLowerCase().includes(searchTerm.toLowerCase()) ||
        user.lastName.toLowerCase().includes(searchTerm.toLowerCase()) ||
        user.email.toLowerCase().includes(searchTerm.toLowerCase())
    );

    return (
        <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
            <DialogTitle>
                <Box display="flex" alignItems="center">
                    <Typography variant="h6">Share Optimisation Run</Typography>
                </Box>
            </DialogTitle>
            <Divider/>
            <DialogContent>
                <TextField
                    autoFocus
                    margin="dense"
                    id="search"
                    label="Search users"
                    type="text"
                    fullWidth
                    variant="outlined"
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    style={{marginBottom: '20px'}}
                />
                <List>
                    {filteredUsers.map((user) => (
                        <ListItem
                            button
                            key={user.id}
                            onClick={() => handleShareWithUser(user.id)}
                            style={{borderRadius: '8px', marginBottom: '8px'}}
                        >
                            <ListItemAvatar>
                                <Avatar>
                                    {user.username.charAt(0).toUpperCase()}
                                </Avatar>
                            </ListItemAvatar>
                            <ListItemText
                                primary={`${user.firstName} ${user.lastName}`}
                                secondary={
                                    <React.Fragment>
                                        <Typography component="span" variant="body2" color="text.primary">
                                            {user.email}
                                        </Typography>
                                        {` — ${user.role}`}
                                    </React.Fragment>
                                }
                            />
                        </ListItem>
                    ))}
                </List>
            </DialogContent>
            <Divider/>
            <DialogActions>
                <Button onClick={onClose} color="primary">
                    Cancel
                </Button>
            </DialogActions>
        </Dialog>
    );
};

export default ShareDialog;