import React, {useState} from 'react';
import {Link, useLocation} from 'react-router-dom';
import {authClient} from '@/api/apiClient.js';
import {Avatar, AvatarFallback} from '@/components/ui/avatar';
import {DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuLabel, DropdownMenuSeparator, DropdownMenuTrigger,} from '@/components/ui/dropdown-menu';
import {ChevronDown, Menu, Moon, Settings, Sun, X} from 'lucide-react';
import {useToast} from '@/hooks/use-toast';
import {cn} from '@/lib/utils';
import {Button} from '@/components/ui/button.jsx';
import {useTheme} from '@/components/ThemeProvider';

function Navbar({user, setUser}) {
    const location = useLocation();
    const {toast} = useToast();
    const {theme, toggleTheme} = useTheme();
    const [isMobileMenuOpen, setMobileMenuOpen] = useState(false);

    const handleLogout = async () => {
        try {
            await authClient.logout();
            setUser(null);
            setMobileMenuOpen(false);  // Close menu after logout
        } catch (error) {
            toast({
                title: 'Error',
                description: error.message || 'Error logging out',
                variant: 'destructive',
            });
        }
    };

    const isActive = (path) => {
        if (path === '/') {
            return location.pathname === '/';
        }
        return location.pathname.startsWith(path);
    };

    const navItemStyles = 'px-3 py-2 text-sm font-medium rounded-md transition-colors';
    const navItemActiveStyles = 'bg-primary text-primary-foreground';
    const navItemInactiveStyles = 'text-foreground hover:bg-accent hover:text-accent-foreground';

    const handleMenuClick = () => {
        setMobileMenuOpen(false);
    };

    return (
        <nav className="relative z-50 w-full bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60 border-b border-border shadow-sm p-2 px-4">
            <div className="flex justify-between items-center">
                <Link
                    to="/dashboard"
                    className="text-xl font-bold text-foreground hover:text-primary transition-colors"
                    onClick={handleMenuClick} // Close menu when the logo is clicked
                >
                    AT4J
                </Link>

                {/* Mobile Menu Button */}
                <div className="flex md:hidden">
                    <Button
                        variant="ghost"
                        size="icon"
                        onClick={() => setMobileMenuOpen(!isMobileMenuOpen)}
                        className="h-8 w-8"
                    >
                        {isMobileMenuOpen ? (
                            <X className="h-6 w-6"/>
                        ) : (
                            <Menu className="h-6 w-6"/>
                        )}
                    </Button>
                </div>

                {/* Links for desktop and mobile */}
                <div
                    className={`flex-col md:flex md:flex-row md:items-center ${isMobileMenuOpen ? 'block absolute top-14 left-0 w-full bg-background z-10 p-4 shadow-lg rounded-lg' : 'hidden'} md:block`}
                >
                    {user ? (
                        <div className="flex flex-col md:flex-row items-center space-y-4 md:space-y-0 md:space-x-4">
                            <Link
                                to="/dashboard"
                                className={cn(
                                    navItemStyles,
                                    isActive('/dashboard')
                                        ? navItemActiveStyles
                                        : navItemInactiveStyles
                                )}
                                onClick={handleMenuClick}
                            >
                                Dashboard
                            </Link>
                            <Link
                                to="/news"
                                className={cn(
                                    navItemStyles,
                                    isActive('/news')
                                        ? navItemActiveStyles
                                        : navItemInactiveStyles
                                )}
                                onClick={handleMenuClick}
                            >
                                News
                            </Link>

                            {(user.role === 'ADMIN' || user.role === 'LIVE_VIEWER') && (
                                <Link
                                    to="/live"
                                    className={cn(
                                        navItemStyles,
                                        isActive('/live')
                                            ? navItemActiveStyles
                                            : navItemInactiveStyles
                                    )}
                                    onClick={handleMenuClick}
                                >
                                    Live
                                </Link>
                            )}
                            <Link
                                to="/backtest"
                                className={cn(
                                    navItemStyles,
                                    isActive('/backtest')
                                        ? navItemActiveStyles
                                        : navItemInactiveStyles
                                )}
                                onClick={handleMenuClick}
                            >
                                Backtest
                            </Link>
                            <Link
                                to="/optimisation"
                                className={cn(
                                    navItemStyles,
                                    isActive('/optimisation')
                                        ? navItemActiveStyles
                                        : navItemInactiveStyles
                                )}
                                onClick={handleMenuClick}
                            >
                                Optimise
                            </Link>

                            {user.role === 'ADMIN' && (
                                <DropdownMenu>
                                    <DropdownMenuTrigger asChild>
                                        <button
                                            className={cn(
                                                navItemStyles,
                                                'flex items-center',
                                                isActive('/users') || isActive('/monitor')
                                                    ? navItemActiveStyles
                                                    : navItemInactiveStyles
                                            )}
                                        >
                                            <Settings className="h-4 w-4 mr-1"/>
                                            Admin
                                        </button>
                                    </DropdownMenuTrigger>
                                    <DropdownMenuContent
                                        align="end"
                                        className="shadow-md rounded-lg"
                                    >
                                        <DropdownMenuItem asChild>
                                            <Link to="/users" onClick={handleMenuClick}>
                                                Manage Users
                                            </Link>
                                        </DropdownMenuItem>
                                        <DropdownMenuItem asChild>
                                            <Link to="/monitor" onClick={handleMenuClick}>
                                                Monitor
                                            </Link>
                                        </DropdownMenuItem>
                                    </DropdownMenuContent>
                                </DropdownMenu>
                            )}

                            <DropdownMenu>
                                <DropdownMenuTrigger asChild>
                                    <button
                                        className={cn(
                                            navItemStyles,
                                            'flex items-center space-x-2 focus:outline-none focus:ring-0'
                                        )}
                                    >
                                        <Avatar className="h-6 w-6">
                                            <AvatarFallback>
                                                {user.firstName?.charAt(0).toUpperCase()}
                                            </AvatarFallback>
                                        </Avatar>
                                        <span>{user.firstName}</span>
                                        <ChevronDown className="h-4 w-4"/>
                                    </button>
                                </DropdownMenuTrigger>
                                <DropdownMenuContent
                                    align="end"
                                    className="shadow-md rounded-lg mt-2"
                                >
                                    <DropdownMenuLabel>
                                        Logged in as {user.username}
                                    </DropdownMenuLabel>
                                    <DropdownMenuSeparator/>
                                    <DropdownMenuItem onSelect={handleLogout}>
                                        Logout
                                    </DropdownMenuItem>
                                </DropdownMenuContent>
                            </DropdownMenu>

                            <Button
                                variant="ghost"
                                size="icon"
                                onClick={() => {
                                    toggleTheme();
                                    handleMenuClick();
                                }}
                                className="h-8 w-8"
                            >
                                {theme === 'light' ? (
                                    <Moon className="h-5 w-5"/>
                                ) : (
                                    <Sun className="h-5 w-5"/>
                                )}
                                <span className="sr-only">Toggle theme</span>
                            </Button>
                        </div>
                    ) : (
                        <div className="flex flex-col md:flex-row items-center space-y-2 md:space-y-0 md:space-x-2">
                            <Link
                                to="/login"
                                className={cn(
                                    navItemStyles,
                                    isActive('/login') ? navItemActiveStyles : navItemInactiveStyles
                                )}
                                onClick={handleMenuClick}
                            >
                                Login
                            </Link>
                            <Link
                                to="/signup"
                                className={cn(
                                    navItemStyles,
                                    isActive('/signup')
                                        ? navItemActiveStyles
                                        : navItemInactiveStyles
                                )}
                                onClick={handleMenuClick}
                            >
                                Sign Up
                            </Link>
                        </div>
                    )}
                </div>
            </div>
        </nav>
    );
}

export default Navbar;