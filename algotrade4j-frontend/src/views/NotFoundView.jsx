import React from 'react';
import {Link} from 'react-router-dom';
import {Button} from "@/components/ui/button";

function NotFoundView() {
    return (
        <div className="flex flex-col justify-center items-center min-h-[80vh]">
            <h1 className="text-6xl font-bold text-primary mb-2">404</h1>
            <h2 className="text-2xl text-muted-foreground mb-4">Oops! Page not found.</h2>
            <p className="text-muted-foreground mb-6 text-center max-w-md">
                The page you are looking for might have been removed, had its name changed, or is temporarily unavailable.
            </p>
            <Button asChild>
                <Link to="/dashboard">Go to Dashboard</Link>
            </Button>
        </div>
    );
}

export default NotFoundView;