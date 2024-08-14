import React, {useState} from 'react';
import {Box, Button, TextField} from '@mui/material';
import SendIcon from '@mui/icons-material/Send';

const MessageInput = ({onSendMessage, selectedUser}) => {
    const [message, setMessage] = useState('');

    const handleSubmit = (e) => {
        e.preventDefault();
        if (message.trim() && selectedUser) {
            onSendMessage(message);
            setMessage('');
        }
    };

    return (
        <Box component="form" onSubmit={handleSubmit} sx={{display: 'flex', gap: 1, mt: 2}}>
            <TextField
                fullWidth
                variant="outlined"
                size="small"
                value={message}
                onChange={(e) => setMessage(e.target.value)}
                placeholder={selectedUser ? `Message ${selectedUser}...` : "Select a user to chat"}
                disabled={!selectedUser}
            />
            <Button
                type="submit"
                variant="contained"
                disabled={!selectedUser}
                endIcon={<SendIcon/>}
            >
                Send
            </Button>
        </Box>
    );
};

export default MessageInput;