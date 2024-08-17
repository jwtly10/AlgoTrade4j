import React, {useEffect, useState} from 'react';
import {BrowserRouter as Router, Navigate, Route, Routes} from 'react-router-dom';
import {Box, CircularProgress, CssBaseline, ThemeProvider} from '@mui/material';
import {createTheme} from '@mui/material/styles';
import Navbar from './components/Navbar';
import BacktestView from './views/BacktestView';
import AuthModal from './components/modals/AuthModal';
import {authClient} from './api/apiClient.js';
import UserManagementView from './views/UserManagementView';
import NotFoundView from "./views/NotFoundView.jsx";

const defaultTheme = createTheme()

function App() {
    const [loading, setLoading] = useState(true);
    const [user, setUser] = useState(null);
    const [authModalOpen, setAuthModalOpen] = useState(false);

    useEffect(() => {
        const verifyToken = async () => {
            setLoading(true)
            try {
                const userData = await authClient.verifyToken();
                setUser(userData);
            } catch (error) {
                console.error('Token verification failed:', error);
            } finally {
                setLoading(false);
            }
        };

        verifyToken();
    }, []);

    const handleOpenAuthModal = () => setAuthModalOpen(true);
    const handleCloseAuthModal = () => setAuthModalOpen(false);

    if (loading) {
        return (
            <Box display="flex" justifyContent="center" alignItems="center" minHeight="100vh">
                <CircularProgress/>
            </Box>
        );
    }

    return (
        <ThemeProvider theme={defaultTheme}>
            <CssBaseline/>
            <Router>
                <Navbar user={user} setUser={setUser} openAuthModal={handleOpenAuthModal}/>
                <Routes>
                    <Route
                        path="/"
                        element={user ? <Navigate to="/backtest" replace/> : <Navigate to="/login" replace/>}
                    />
                    <Route
                        path="/backtest"
                        element={user ? <BacktestView/> : <Navigate to="/login" replace/>}
                    />
                    <Route
                        path="/login"
                        element={user ? <Navigate to="/backtest" replace/> : <AuthModal open={true} onClose={() => {
                        }} setUser={setUser}/>}
                    />
                    <Route path="/signup" element={<Navigate to="/login" replace/>}/>
                    {user && user.role === 'ADMIN' && (
                        <Route path="/users" element={<UserManagementView user={user}/>}/>
                    )}
                    <Route
                        path="*"
                        element={user ? <NotFoundView/> : <Navigate to="/login" replace/>}
                    />
                </Routes>
                {!user && <AuthModal open={authModalOpen} onClose={handleCloseAuthModal} setUser={setUser}/>}
            </Router>
        </ThemeProvider>
    );
}

export default App;