import React from 'react';
import {Link, useLocation} from 'react-router-dom';
import {authClient} from '@/api/apiClient.js';
import {Avatar, AvatarFallback} from "@/components/ui/avatar";
import {DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuLabel, DropdownMenuSeparator, DropdownMenuTrigger,} from "@/components/ui/dropdown-menu";
import {ChevronDown, Moon, Settings, Sun} from "lucide-react";
import {useToast} from "@/hooks/use-toast";
import {cn} from "@/lib/utils";
import {Button} from "@/components/ui/button.jsx";
import {useTheme} from '@/components/ThemeProvider';

function Navbar({user, setUser}) {
    const location = useLocation();
    const {toast} = useToast();
    const {theme, toggleTheme} = useTheme();

    const handleLogout = async () => {
        try {
            await authClient.logout();
            setUser(null);
        } catch (error) {
            toast({
                title: "Error",
                description: error.message || 'Error logging out',
                variant: "destructive",
            });
        }
    };

    const isActive = (path) => {
        if (path === '/') {
            return location.pathname === '/';
        }
        return location.pathname.startsWith(path);
    };

    const navItemStyles = "px-3 py-2 text-sm font-medium rounded-md transition-colors";
    const navItemActiveStyles = "bg-primary text-primary-foreground";
    const navItemInactiveStyles = "text-foreground hover:bg-accent hover:text-accent-foreground";

    return (
        <nav className="w-full bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60 border-b border-border shadow-sm p-2 px-4">
            <div className="flex justify-between items-center">
                <Link to="/" className="text-xl font-bold text-foreground hover:text-primary transition-colors">
                    AT4J
                </Link>

                {user ? (
                    <div className="flex items-center space-x-1">
                        <Link
                            to="/live"
                            className={cn(
                                navItemStyles,
                                isActive('/live') ? navItemActiveStyles : navItemInactiveStyles
                            )}
                        >
                            Live
                        </Link>
                        <Link
                            to="/backtest"
                            className={cn(
                                navItemStyles,
                                isActive('/backtest') ? navItemActiveStyles : navItemInactiveStyles
                            )}
                        >
                            Backtest
                        </Link>
                        <Link
                            to="/optimisation"
                            className={cn(
                                navItemStyles,
                                isActive('/optimisation') ? navItemActiveStyles : navItemInactiveStyles
                            )}
                        >
                            Optimise
                        </Link>

                        {user.role === 'ADMIN' && (
                            <DropdownMenu>
                                <DropdownMenuTrigger asChild>
                                    <button className={cn(navItemStyles, "flex items-center", isActive('/users') || isActive('/monitor') ? navItemActiveStyles : navItemInactiveStyles)}>
                                        <Settings className="h-4 w-4 mr-1"/>
                                        Admin
                                    </button>
                                </DropdownMenuTrigger>
                                <DropdownMenuContent align="end">
                                    <DropdownMenuItem asChild>
                                        <Link to="/users">Manage Users</Link>
                                    </DropdownMenuItem>
                                    <DropdownMenuItem asChild>
                                        <Link to="/monitor">Monitor</Link>
                                    </DropdownMenuItem>
                                </DropdownMenuContent>
                            </DropdownMenu>
                        )}

                        <DropdownMenu>
                            <DropdownMenuTrigger asChild>
                                <button className={cn(navItemStyles, "flex items-center space-x-1")}>
                                    <Avatar className="h-6 w-6">
                                        <AvatarFallback>{user.firstName?.charAt(0).toUpperCase()}</AvatarFallback>
                                    </Avatar>
                                    <span>{user.firstName}</span>
                                    <ChevronDown className="h-3 w-3"/>
                                </button>
                            </DropdownMenuTrigger>
                            <DropdownMenuContent align="end">
                                <DropdownMenuLabel>Logged in as {user.username}</DropdownMenuLabel>
                                <DropdownMenuSeparator/>
                                <DropdownMenuItem onSelect={handleLogout}>Logout</DropdownMenuItem>
                            </DropdownMenuContent>
                        </DropdownMenu>
                        <Button variant="ghost" size="icon" onClick={toggleTheme} className="h-8 w-8">
                            {theme === 'light' ? (
                                <Moon className="h-4 w-4 rotate-0 scale-100 transition-all dark:-rotate-90 dark:scale-0"/>
                            ) : (
                                <Sun className="h-4 w-4 rotate-90 scale-0 transition-all dark:rotate-0 dark:scale-100"/>
                            )}
                            <span className="sr-only">Toggle theme</span>
                        </Button>
                    </div>
                ) : (
                    <div className="flex items-center space-x-1">
                        <Link
                            to="/login"
                            className={cn(
                                navItemStyles,
                                isActive('/login') ? navItemActiveStyles : navItemInactiveStyles
                            )}
                        >
                            Login
                        </Link>
                        <Link
                            to="/signup"
                            className={cn(
                                navItemStyles,
                                isActive('/signup') ? navItemActiveStyles : navItemInactiveStyles
                            )}
                        >
                            Sign Up
                        </Link>
                    </div>
                )}
            </div>
        </nav>
    );
}

export default Navbar;