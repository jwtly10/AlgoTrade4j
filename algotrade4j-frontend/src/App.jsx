import React, {useEffect, useState} from 'react';
import {BrowserRouter as Router, Navigate, Route, Routes} from 'react-router-dom';
import Navbar from './components/layout/Navbar';
import BacktestView from './views/main/BacktestView.jsx';
import AuthModal from './components/modals/AuthModal';
import {authClient} from './api/apiClient.js';
import UserManagementView from './views/UserManagementView';
import NotFoundView from "./views/NotFoundView.jsx";
import VersionBanner from "./components/layout/VersionBanner.jsx";
import HomeView from "./views/main/HomeView.jsx";
import OptimisationView from "./views/main/OptimisationView.jsx";
import log from './logger.js';
import {ThemeProvider} from "./components/ThemeProvider";
import {Toaster} from "./components/ui/toaster";
import UnauthorizedAccessView from "@/views/UnauthorizedAccessView.jsx";
import MonitorView from "@/views/MonitorView.jsx";
import {useToast} from "@/hooks/use-toast.js";
import LiveStrategyView from "@/views/main/LiveStrategyView.jsx";

function App() {
    const [loading, setLoading] = useState(true);
    const [user, setUser] = useState(null);
    const [authModalOpen, setAuthModalOpen] = useState(false);
    const {toast} = useToast();

    useEffect(() => {
        const verifyToken = async () => {
            setLoading(true)
            try {
                const userData = await authClient.verifyToken();
                setUser(userData);
            } catch (error) {
                log.error('Token verification failed:', error);
                toast({
                    title: 'Session Expired',
                    description: "Your session has expired. Please login again.",
                    status: 'destructive'
                })
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
            <div className="flex justify-center items-center min-h-screen">
                <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-gray-900"></div>
            </div>
        );
    }

    return (
        <ThemeProvider defaultTheme="dark" storageKey="vite-ui-theme">
            <Router>
                <div className="min-h-screen bg-background text-foreground">
                    {user && (<VersionBanner user={user}/>)}
                    {user && (<Navbar user={user} setUser={setUser} openAuthModal={handleOpenAuthModal}/>)}
                    <Routes>
                        <Route
                            path="/"
                            element={user ? <HomeView/> : <Navigate to="/login" replace/>}
                        />

                        <Route
                            path="/backtest"
                            element={user ? <BacktestView/> : <Navigate to="/login" replace/>}
                        />

                        <Route
                            path="/login"
                            element={user ? <Navigate to="/" replace/> : <AuthModal open={true} onClose={() => {
                            }} setUser={setUser}/>}
                        />

                        <Route
                            path="/optimisation"
                            element={user ? <OptimisationView/> : <Navigate to="/login" replace/>}
                        />

                        <Route path="/signup" element={<Navigate to="/login" replace/>}/>

                        {/* Admin routes */}
                        <Route
                            path="/users"
                            element={user ? (
                                user.role === 'ADMIN' ? <UserManagementView loggedInUser={user}/> : <UnauthorizedAccessView/>
                            ) : <Navigate to="/login" replace/>}
                        />
                        <Route
                            path="/monitor"
                            element={user ? (
                                user.role === 'ADMIN' ? <MonitorView/> : <UnauthorizedAccessView/>
                            ) : <Navigate to="/login" replace/>}
                        />

                        <Route
                            path="/live"
                            element={user ? (
                                user.role === 'ADMIN' ? <LiveStrategyView/> : <UnauthorizedAccessView/>
                            ) : <Navigate to="/login" replace/>}
                        />

                        <Route
                            path="*"
                            element={user ? <NotFoundView/> : <Navigate to="/login" replace/>}
                        />
                    </Routes>
                    {!user && <AuthModal open={authModalOpen} onClose={handleCloseAuthModal} setUser={setUser}/>}
                </div>
            </Router>
            <Toaster/>
        </ThemeProvider>
    );
}

export default App;