import React, {useEffect, useState} from 'react';
import {BrowserRouter as Router, Navigate, Route, Routes} from 'react-router-dom';
import {CssBaseline, ThemeProvider} from '@mui/material';
import {createTheme} from '@mui/material/styles';
import Navbar from './components/Navbar';
import StrategyChart from './components/StrategyChart';
import AuthModal from './components/AuthModal';
import {authClient} from './api/apiClient.js';

const defaultTheme = createTheme()

function App() {
    const [user, setUser] = useState(null);
    const [authModalOpen, setAuthModalOpen] = useState(false);

    useEffect(() => {
        const verifyToken = async () => {
            try {
                const userData = await authClient.verifyToken();
                setUser(userData);
            } catch (error) {
                console.error('Token verification failed:', error);
            }
        };

        verifyToken();
    }, []);

    const handleOpenAuthModal = () => setAuthModalOpen(true);
    const handleCloseAuthModal = () => setAuthModalOpen(false);

    return (
        <ThemeProvider theme={defaultTheme}>
            <CssBaseline/>
            <Router>
                <Navbar user={user} setUser={setUser} openAuthModal={handleOpenAuthModal}/>
                <Routes>
                    <Route
                        path="/"
                        element={user ? <StrategyChart/> : <Navigate to="/login" replace/>}
                    />
                    <Route
                        path="/login"
                        element={user ? <Navigate to="/" replace/> : <AuthModal open={true} onClose={() => {
                        }} setUser={setUser}/>}
                    />
                    <Route path="/signup" element={<Navigate to="/login" replace/>}/>
                </Routes>
                {!user && <AuthModal open={authModalOpen} onClose={handleCloseAuthModal} setUser={setUser}/>}
            </Router>
        </ThemeProvider>
    );
}

export default App;