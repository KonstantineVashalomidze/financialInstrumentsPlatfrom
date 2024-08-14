import React, {useState} from 'react';
import {useNavigate} from 'react-router-dom';
import styled from 'styled-components';
import {RegistrationForm} from '../components/RegistrationForm';
import {register} from '../utils/api';


const RegistrationContainer = styled.div`
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 90vh

`;

const RegistrationTitle = styled.h1`
    margin-bottom: 1.5rem;
`;

const LoginLink = styled.button`
    margin-top: 1rem;
    background-color: transparent;
    border: none;
    color: blue;
    text-decoration: underline;
    cursor: pointer;
`;

const RegistrationPage = () => {
    const navigate = useNavigate();
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');

    const handleRegistration = async () => {
        try {
            const response = await register(username, password);
            if (response) {
                navigate('/login');
            }
        } catch (error) {
            console.error('Registration failed:', error);
        }
    };

    return (
        <RegistrationContainer>
            <RegistrationTitle>Register</RegistrationTitle>
            <RegistrationForm
                username={username}
                password={password}
                onUsernameChange={setUsername}
                onPasswordChange={setPassword}
                onSubmit={handleRegistration}
            />
            <LoginLink onClick={() => navigate('/login')}>
                Already have an account? Login
            </LoginLink>
        </RegistrationContainer>
    );
};

export default RegistrationPage;