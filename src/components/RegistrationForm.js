// RegistrationForm.js
import React from 'react';
import {Box, Button, TextField} from '@mui/material';

export const RegistrationForm = ({
                                     username,
                                     password,
                                     onUsernameChange,
                                     onPasswordChange,
                                     onSubmit,
                                 }) => {
    return (
        <Box
            component="form"
            onSubmit={(e) => {
                e.preventDefault();
                onSubmit();
            }}
            sx={{
                display: 'flex',
                flexDirection: 'column',
                gap: 2,
                maxWidth: 400,
                width: '100%',
            }}
        >
            <TextField
                type="text"
                label="Username"
                variant="outlined"
                value={username}
                onChange={(e) => onUsernameChange(e.target.value)}
                fullWidth
            />
            <TextField
                type="password"
                label="Password"
                variant="outlined"
                value={password}
                onChange={(e) => onPasswordChange(e.target.value)}
                fullWidth
            />
            <Button
                type="submit"
                variant="contained"
                color="primary"
                fullWidth
            >
                Register
            </Button>
        </Box>
    );
};