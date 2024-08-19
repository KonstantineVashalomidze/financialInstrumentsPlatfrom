import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import styled from 'styled-components';
import { LoginForm } from '../components/LoginForm';
import { login } from '../utils/api';

const LoginContainer = styled.div`
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 90vh;
`;

const LoginTitle = styled.h1`
    margin-bottom: 1.5rem;
`;

const RegisterLink = styled.button`
    margin-top: 1rem;
    background-color: transparent;
    border: none;
    color: blue;
    text-decoration: underline;
    cursor: pointer;
`;

const ErrorMessage = styled.p`
    color: red;
    margin-top: 1rem;
`;

const LoginPage = () => {
    const navigate = useNavigate();
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');

    const handleLogin = async () => {
        try {
            setError(''); // Clear any previous errors
            const response = await login(username, password);
            if (response.token) {
                localStorage.setItem('token', response.token);
                navigate('/dashboard');
            } else {
                // Handle case where login was unsuccessful but didn't throw an error
                setError('Login failed. Please check your credentials.');
            }
        } catch (error) {
            console.error('Login failed:', error);
            if (error.response) {
                // The request was made and the server responded with a status code
                // that falls out of the range of 2xx
                switch (error.response.status) {
                    case 404:
                        setError('Username not found. Please check your username.');
                        break;
                    case 401:
                        setError('Incorrect password. Please try again.');
                        break;
                    default:
                        setError('An error occurred. Please try again later.');
                }
            } else if (error.request) {
                // The request was made but no response was received
                setError('No response from server. Please check your internet connection.');
            } else {
                // Something happened in setting up the request that triggered an Error
                setError('An error occurred. Please try again later.');
            }
        }
    };

    return (
        <LoginContainer>
            <LoginTitle>Login</LoginTitle>
            <LoginForm
                username={username}
                password={password}
                onUsernameChange={setUsername}
                onPasswordChange={setPassword}
                onSubmit={handleLogin}
            />
            {error && <ErrorMessage>{error}</ErrorMessage>}
            <RegisterLink onClick={() => navigate('/register')}>
                Don't have an account? Register
            </RegisterLink>
        </LoginContainer>
    );
};

export default LoginPage;