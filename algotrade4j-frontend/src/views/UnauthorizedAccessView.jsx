import React from 'react';
import {Link} from 'react-router-dom';
import {Button} from "@/components/ui/button";

function UnauthorizedAccessView() {
    return (
        <div className="flex flex-col justify-center items-center min-h-[80vh]">
            <h1 className="text-6xl font-bold text-primary mb-2">403</h1>
            <h2 className="text-2xl text-muted-foreground mb-4">Access Denied</h2>
            <p className="text-muted-foreground mb-6 text-center max-w-md">
                Sorry, you don't have permission to access this page. This area is restricted to admin users only.
            </p>
            <Button asChild>
                <Link to="/dashboard">Return to Dashboard</Link>
            </Button>
        </div>
    );
}

export default UnauthorizedAccessView;