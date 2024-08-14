import React, {useEffect, useRef} from 'react';
import {Box, Paper, Typography} from '@mui/material';

const ChatWindow = ({messages, currentUser}) => {
    const messagesEndRef = useRef(null);

    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({behavior: "smooth"});
    };

    useEffect(scrollToBottom, [messages]);

    return (
        <Box sx={{height: 400, overflowY: 'auto', p: 2, bgcolor: 'grey.100'}}>
            {messages.map((message, index) => (
                <Box
                    key={index}
                    sx={{
                        display: 'flex',
                        justifyContent: message.sender === currentUser ? 'flex-end' : 'flex-start',
                        mb: 2
                    }}
                >
                    <Paper
                        elevation={2}
                        sx={{
                            p: 1,
                            maxWidth: '70%',
                            bgcolor: message.sender === currentUser ? 'primary.light' : 'background.paper'
                        }}
                    >
                        <Typography variant="subtitle2" fontWeight="bold">
                            {message.sender}
                        </Typography>
                        <Typography variant="body2">{message.content}</Typography>
                    </Paper>
                </Box>
            ))}
            <div ref={messagesEndRef}/>
        </Box>
    );
};

export default ChatWindow;