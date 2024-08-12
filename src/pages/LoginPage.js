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
  min-height: 100vh;
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

const LoginPage = () => {
    const navigate = useNavigate();
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');

    const handleLogin = async () => {
        try {
            const response = await login(username, password);
            // and navigate to the dashboard
            if (response.token)
            {
                localStorage.setItem('token', response.token);
                navigate('/dashboard');
            }
        } catch (error) {
            console.error('Login failed:', error);
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
            <RegisterLink onClick={() => navigate('/register')}>
                Don't have an account? Register
            </RegisterLink>
        </LoginContainer>
    );
};

export default LoginPage;