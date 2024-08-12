import React from 'react';
import styled from 'styled-components';

const FormContainer = styled.form`
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  max-width: 400px;
  width: 100%;
`;

const FormInput = styled.input`
  padding: 0.5rem;
  border: 1px solid #ccc;
  border-radius: 4px;
`;

const SubmitButton = styled.button`
  padding: 0.5rem 1rem;
  background-color: blue;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
`;

export const RegistrationForm = ({
                                     username,
                                     password,
                                     onUsernameChange,
                                     onPasswordChange,
                                     onSubmit,
                                 }) => {
    return (
        <FormContainer
            onSubmit={(e) => {
                e.preventDefault();
                onSubmit();
            }}
        >
            <FormInput
                type="text"
                placeholder="Username"
                value={username}
                onChange={(e) => onUsernameChange(e.target.value)}
            />
            <FormInput
                type="password"
                placeholder="Password"
                value={password}
                onChange={(e) => onPasswordChange(e.target.value)}
            />
            <SubmitButton type="submit">Register</SubmitButton>
        </FormContainer>
    );
};