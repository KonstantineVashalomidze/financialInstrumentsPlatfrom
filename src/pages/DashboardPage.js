import React, {useState, useEffect, useRef} from 'react';
import { useNavigate } from 'react-router-dom';
import styled from 'styled-components';
import Dashboard from '../components/Dashboard';
import { getMySubscriptions } from '../utils/api'


const DashboardContainer = styled.div`
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  justify-content: space-between;
  min-height: 100vh;
  padding: 2rem;
`;

const DashboardTitle = styled.h1`
  margin-bottom: 1.5rem;
`;

const LogoutButton = styled.button`
  margin-top: 1rem;
  padding: 0.5rem 1rem;
  background-color: blue;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
`;

const SubscriptionList = styled.div`
  flex: 1;
  margin-right: 2rem;
`;

const SubscriptionDisplay = styled.div`
  flex: 2;
`;

const SubscriptionItem = styled.div`
  display: flex;
  align-items: center;
  margin-bottom: 0.5rem;
  cursor: pointer;

  input[type='checkbox'] {
    margin-right: 0.5rem;
  }
`;

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

            subscriptions.forEach((symbol) => {
                subscriptionSocket.send(`SUBSCRIBE: ${symbol}`);
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
            { symbol: 'AAPL' },
            { symbol: 'GOOGL' },
            { symbol: 'MSFT' },
            { symbol: 'AMZN' },
            { symbol: 'FB' },
            { symbol: 'TSLA' },
            { symbol: 'NVDA' },
            { symbol: 'JPM' },
            { symbol: 'V' },
            { symbol: 'JNJ' },
            { symbol: 'WMT' },
            { symbol: 'PG' },
            { symbol: 'DIS' },
            { symbol: 'NFLX' },
            { symbol: 'PYPL' },
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
        localStorage.removeItem('token');
        navigate('/login');
    };

    return (
        <DashboardContainer>
            <SubscriptionList>
                <DashboardTitle>Subscriptions</DashboardTitle>
                {availableSymbols.map((item) => (
                    <SubscriptionItem key={item.symbol} onClick={() => toggleSubscription(item.symbol)}>
                        <input
                            type="checkbox"
                            checked={subscriptions.includes(item.symbol)}
                            readOnly
                        />
                        <span>{item.symbol}</span>
                    </SubscriptionItem>
                ))}
            </SubscriptionList>
            <SubscriptionDisplay>
                <DashboardTitle>Subscribed Data</DashboardTitle>
                <Dashboard data={getSubscribedData()} subscriptions={subscriptions} />
            </SubscriptionDisplay>
            <LogoutButton onClick={handleLogout}>Logout</LogoutButton>
        </DashboardContainer>
    );
};

export default DashboardPage;












