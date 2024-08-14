import React, {useEffect, useRef, useState} from 'react';
import {useNavigate} from 'react-router-dom';
import Dashboard from '../components/Dashboard';
import {getMySubscriptions} from '../utils/api'
import {
    AppBar,
    Box,
    Checkbox,
    Grid,
    IconButton,
    List,
    ListItem,
    ListItemButton,
    ListItemIcon,
    ListItemText,
    Paper,
    Toolbar,
    Typography
} from '@mui/material';
import ChatIcon from '@mui/icons-material/Chat';
import LogoutIcon from '@mui/icons-material/Logout';

const DashboardPage = () => {
    const navigate = useNavigate();
    const [availableSymbols, setAvailableSymbols] = useState([]);
    const [subscriptions, setSubscriptions] = useState([]);
    const subscriptionWebSocketRef = useRef(null);
    const realTimeWebSocketRef = useRef(null);


    useEffect(() => {
        const token = localStorage.getItem('token');
        const subscriptionSocket = new WebSocket(`ws://localhost:8081/ws/subscribe?token=${token}`);
        const realTimeSocket = new WebSocket(`ws://localhost:8081/ws/realtime?token=${token}`);

        subscriptionWebSocketRef.current = subscriptionSocket;
        realTimeWebSocketRef.current = realTimeSocket;

        subscriptionSocket.onopen = () => {
            console.log("Connected to the subscription server");

            getMySubscriptions()
                .then(subscriptionsData => {
                    setSubscriptions(subscriptionsData);
                })
                .catch(error => {
                    console.error("Error fetching subscriptions:", error);
                    setSubscriptions([]);  // Set to empty array if there's an error
                });

        };

        subscriptionSocket.onclose = (event) => {
            console.log("Subscription WebSocket connection closed:", event);
        };

        realTimeSocket.onopen = () => {
            console.log("Connected to the real-time data server");
        };

        realTimeSocket.onmessage = (event) => {
            try {
                const message = parseInstrumentData(event.data);
                console.log("Received real-time data from server:", message);
                setAvailableSymbols((prevSymbols) => {
                    const updatedSymbols = [...prevSymbols];
                    const index = updatedSymbols.findIndex((item) => item.symbol === message.symbol);
                    if (index !== -1) {
                        updatedSymbols[index] = message;
                    } else {
                        updatedSymbols.push(message);
                    }
                    return updatedSymbols;
                });
            } catch (error) {
                console.error("Error parsing real-time data:", error);
            }
        };

        realTimeSocket.onerror = (error) => {
            console.error("Real-time WebSocket error:", error);
        };

        realTimeSocket.onclose = (event) => {
            console.log("Real-time WebSocket connection closed:", event);
        };

        // Cleanup function
        return () => {
            if (subscriptionWebSocketRef.current) {
                subscriptionWebSocketRef.current.close();
            }
            if (realTimeWebSocketRef.current) {
                realTimeWebSocketRef.current.close();
            }
        };
    }, []);

    useEffect(() => {
        const symbols = [
            {symbol: 'AAPL'},
            {symbol: 'GOOGL'},
            {symbol: 'MSFT'},
            {symbol: 'AMZN'},
            {symbol: 'FB'},
            {symbol: 'TSLA'},
            {symbol: 'NVDA'},
            {symbol: 'JPM'},
            {symbol: 'V'},
            {symbol: 'JNJ'},
            {symbol: 'WMT'},
            {symbol: 'PG'},
            {symbol: 'DIS'},
            {symbol: 'NFLX'},
            {symbol: 'PYPL'},
        ];
        setAvailableSymbols(symbols);
    }, []);

    const toggleSubscription = (symbol) => {
        if (subscriptions.includes(symbol)) {
            setSubscriptions(subscriptions.filter((s) => s !== symbol));
            subscriptionWebSocketRef.current.send(`UNSUBSCRIBE: ${symbol}`);
        } else {
            setSubscriptions([...subscriptions, symbol]);
            subscriptionWebSocketRef.current.send(`SUBSCRIBE: ${symbol}`);
        }
    };

    const getSubscribedData = () => {
        return availableSymbols;
    };

    const parseInstrumentData = (data) => {
        const regex = /InstrumentData\{symbol='([^']+)', price=(\d+\.\d+), timeStamp=(\d+)\}/;
        const match = data.match(regex);
        if (match) {
            return {
                symbol: match[1],
                price: parseFloat(match[2]),
                timeStamp: parseInt(match[3]),
            };
        } else {
            // Try to parse the data as JSON
            try {
                const jsonData = JSON.parse(data);
                return jsonData;
            } catch (error) {
                console.error('Error parsing real-time data:', error);
                throw error;
            }
        }
    };

    const handleLogout = () => {
        if (subscriptionWebSocketRef.current) {
            subscriptionWebSocketRef.current.close();
        }
        if (realTimeWebSocketRef.current) {
            realTimeWebSocketRef.current.close();
        }
        localStorage.clear();
        navigate('/login');
    };

    const handleChatNavigation = () => {
        navigate('/chat');
    };

    return (
        <Box sx={{flexGrow: 1}}>
            <AppBar position="static">
                <Toolbar>
                    <Typography variant="h6" component="div" sx={{flexGrow: 1}}>
                        Dashboard
                    </Typography>
                    <IconButton color="inherit" onClick={handleChatNavigation}>
                        <ChatIcon/>
                    </IconButton>
                    <IconButton color="inherit" onClick={handleLogout}>
                        <LogoutIcon/>
                    </IconButton>
                </Toolbar>
            </AppBar>
            <Grid container spacing={2} sx={{p: 2}}>
                <Grid item xs={12} md={4}>
                    <Paper elevation={3} sx={{p: 2, maxHeight: '80vh', overflow: 'auto'}}>
                        <Typography variant="h6" gutterBottom>
                            Subscriptions
                        </Typography>
                        <List>
                            {availableSymbols.map((item) => (
                                <ListItem key={item.symbol} disablePadding>
                                    <ListItemButton onClick={() => toggleSubscription(item.symbol)}>
                                        <ListItemIcon>
                                            <Checkbox
                                                edge="start"
                                                checked={subscriptions.includes(item.symbol)}
                                                tabIndex={-1}
                                                disableRipple
                                            />
                                        </ListItemIcon>
                                        <ListItemText primary={item.symbol}/>
                                    </ListItemButton>
                                </ListItem>
                            ))}
                        </List>
                    </Paper>
                </Grid>
                <Grid item xs={12} md={8}>
                    <Paper elevation={3} sx={{p: 2, height: '80vh', overflow: 'auto'}}>
                        <Typography variant="h6" gutterBottom>
                            Subscribed Data
                        </Typography>
                        <Dashboard data={getSubscribedData()} subscriptions={subscriptions}/>
                    </Paper>
                </Grid>
            </Grid>
        </Box>
    );
};

export default DashboardPage;












