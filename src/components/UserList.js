import React from 'react';
import {Box, List, ListItem, ListItemButton, ListItemText, Paper, Typography} from '@mui/material';

const UserList = ({users, onSelectUser, selectedUser}) => {
    return (
        <Paper elevation={3} sx={{
            padding: 2,
            maxWidth: 360,
            margin: 'auto',
            maxHeight: '80vh',
            display: 'flex',
            flexDirection: 'column'
        }}>
            <Typography variant="h6" gutterBottom>
                Users
            </Typography>
            <Box sx={{overflow: 'auto', flexGrow: 1}}>
                <List>
                    {users.map((user) => (
                        <ListItem key={user} disablePadding>
                            <ListItemButton
                                onClick={() => onSelectUser(user)}
                                selected={user === selectedUser}
                            >
                                <ListItemText primary={user}/>
                            </ListItemButton>
                        </ListItem>
                    ))}
                </List>
            </Box>
        </Paper>
    );
};

export default UserList;