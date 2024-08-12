const API_BASE_URL = 'http://localhost:8081/api/auth';

export const login = async (username, password) => {
    const response = await fetch(`${API_BASE_URL}/login`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ username, password }),
    });

    if (!response.ok) {
        throw new Error('Login failed');
    }

    const data = await response.json();
    return data;
};

export const register = async (username, password) => {
    const response = await fetch(`${API_BASE_URL}/register`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ username, password }),
    });

    if (!response.ok) {
        throw new Error('Registration failed');
    }

    return true;
};


export const getMessageHistoryBetween = async (who) => {
    const response = await fetch(`http://localhost:8081/api/history?recipient=${who}`, {
        headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`,
        },
    });

    if (!response.ok) {
        throw new Error('Error fetching data');
    }

    return await response.json();
}


export const getMySubscriptions = async () => {
    const response = await fetch(`http://localhost:8081/api/subscriptions`, {
        headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`,
        },
    });

    if (!response.ok) {
        throw new Error('Error fetching data');
    }

    return await response.json();
}


