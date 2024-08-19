import React from 'react';
import {BrowserRouter as Router, Navigate, Route, Routes} from 'react-router-dom';

import LoginPage from '../pages/LoginPage';
import RegistrationPage from '../pages/RegistrationPage';
import DashboardPage from '../pages/DashboardPage';
import ChatPage from "../pages/ChatPage";

const hasValidToken = () => {
    return localStorage.getItem('token') !== null;
};

// Protected route component
const ProtectedRoute = ({children}) => {
    return hasValidToken() ? children : <Navigate to="/login"/>;
};

const AppRoutes = () => (
    <Router>
        <Routes>
            <Route path="/login" element={<LoginPage/>}/>
            <Route
                path="/dashboard"
                element={
                    <ProtectedRoute>
                        <DashboardPage/>
                    </ProtectedRoute>
                }
            />
            <Route
                path="/chat"
                element={
                    <ProtectedRoute>
                        <ChatPage/>
                    </ProtectedRoute>
                }
            />
            <Route path="/register" element={<RegistrationPage/>}/>
        </Routes>
    </Router>
);
export default AppRoutes;