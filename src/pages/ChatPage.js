import React, {useEffect, useRef, useState} from 'react';
import {getMessageHistoryBetween, getUsernames} from '../utils/api';
import UserList from '../components/UserList';
import ChatWindow from '../components/ChatWindow';
import MessageInput from '../components/MessageInput';
import {useNavigate} from "react-router-dom";
import {Box, Button, Grid, Paper} from "@mui/material";
import ArrowBackIcon from '@mui/icons-material/ArrowBack';

const ChatPage = () => {
    const [users, setUsers] = useState([]);
    const [selectedUser, setSelectedUser] = useState(null);
    const [messages, setMessages] = useState([]);
    const currentUser = localStorage.getItem('username');
    const token = localStorage.getItem('token');
    const socketRef = useRef(null);
    const navigate = useNavigate();

    console.log('ChatPage rendered. Current user:', currentUser);

    const handleBackToDashboard = () => {
        navigate('/dashboard');
    };

    useEffect(() => {
        console.log('Initial useEffect running');
        const fetchUsers = async () => {
            try {
                const usernames = await getUsernames();
                console.log('Fetched usernames:', usernames);
                setUsers(usernames.filter(username => username !== currentUser));
            } catch (error) {
                console.error('Error fetching users:', error);
            }
        };
        fetchUsers();

        socketRef.current = new WebSocket(`ws://localhost:8081/ws/chat?token=${token}`);

        socketRef.current.onopen = () => {
            console.log('WebSocket connection established');
        };

        socketRef.current.onmessage = (event) => {
            const messageText = event.data;
            console.log('Received message:', messageText);

            let newMessage;

            if (messageText.startsWith('From')) {
                const match = messageText.match(/From (.*?): (.*)/);
                if (match) {
                    const [, sender, content] = match;
                    newMessage = {
                        sender,
                        content,
                        timestamp: new Date().toISOString()
                    };
                }
            } else if (messageText.startsWith('Message sent to')) {
                console.log('Message sent confirmation:', messageText);
                return;
            } else if (messageText.startsWith('User') && messageText.endsWith('is not available')) {
                newMessage = {
                    sender: 'System',
                    content: messageText,
                    timestamp: new Date().toISOString()
                };
            } else if (messageText === 'Connected successfully. You can now send messages.') {
                console.log('Connection established');
                return;
            } else {
                console.warn('Received unexpected message format:', messageText);
                return;
            }

            if (newMessage) {
                setMessages(prevMessages => [...prevMessages, newMessage]);
            }
        };

        socketRef.current.onerror = (error) => {
            console.error('WebSocket error:', error);
        };

        socketRef.current.onclose = () => {
            console.log('WebSocket connection closed');
        };

        return () => {
            if (socketRef.current) {
                socketRef.current.close();
            }
        };
    }, [currentUser, token]);

    useEffect(() => {
        console.log('Selected user changed:', selectedUser);
        const fetchMessages = async () => {
            if (selectedUser) {
                try {
                    const history = await getMessageHistoryBetween(selectedUser);
                    console.log('Fetched message history:', history);
                    if (history && Array.isArray(history)) {
                        const formattedMessages = history.map(msg => {
                            const timestamp = msg && msg._id
                                ? new Date(msg._id.toString().substring(0, 8)).getTime()
                                : new Date().getTime();

                            return {
                                sender: msg.senderUsername === currentUser ? currentUser : selectedUser,
                                content: msg.content || '',
                                timestamp: timestamp
                            };
                        });
                        setMessages(formattedMessages);
                    } else {
                        console.log('No message history or invalid data received');
                        setMessages([]);
                    }
                } catch (error) {
                    console.error('Error fetching messages:', error);
                    setMessages([]);
                }
            } else {
                setMessages([]);
            }
        };
        fetchMessages();
    }, [selectedUser]);

    const handleSelectUser = (user) => {
        setSelectedUser(user);
    };

    const handleSendMessage = (content) => {
        if (socketRef.current && socketRef.current.readyState === WebSocket.OPEN && selectedUser) {
            const message = `${selectedUser}:${content}`;
            socketRef.current.send(message);

            const newMessage = {
                sender: currentUser,
                content: content,
                timestamp: new Date().toISOString()
            };
            setMessages(prevMessages => [...prevMessages, newMessage]);
        } else {
            console.error('WebSocket is not connected or no user selected');
        }
    };

    return (
        <Box sx={{flexGrow: 1, p: 2}}>
            <Grid container spacing={2}>
                <Grid item xs={12} md={4}>
                    <UserList
                        users={users}
                        onSelectUser={handleSelectUser}
                        selectedUser={selectedUser}
                    />
                </Grid>
                <Grid item xs={12} md={8}>
                    <Paper elevation={3} sx={{p: 2}}>
                        <Button
                            variant="outlined"
                            startIcon={<ArrowBackIcon/>}
                            onClick={handleBackToDashboard}
                            sx={{mb: 2}}
                        >
                            Back to Dashboard
                        </Button>
                        <ChatWindow
                            messages={messages}
                            currentUser={currentUser}
                        />
                        <MessageInput
                            onSendMessage={handleSendMessage}
                            selectedUser={selectedUser}
                        />
                    </Paper>
                </Grid>
            </Grid>
        </Box>
    );
};

export default ChatPage;