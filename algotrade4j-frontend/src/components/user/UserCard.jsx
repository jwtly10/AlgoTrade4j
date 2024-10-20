import React, { useState } from 'react';
import {
    Accordion,
    AccordionItem,
    AccordionTrigger,
    AccordionContent,
} from '@/components/ui/accordion';
import { Button } from '@/components/ui/button';
import { Edit, Lock, Trash } from 'lucide-react';
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from '@/components/ui/tooltip';
import MetadataViewer from './MetadataViewer';
import { Skeleton } from '@/components/ui/skeleton';

const UserCard = ({
    user,
    onEdit,
    onChangePassword,
    onDelete,
    onUserClick,
    expandedUser,
    userLogs,
    loginLogs,
    activeTab,
    setActiveTab,
    uiIsLoadingLogs,
}) => {
    const formatTimestamp = (timestamp) => {
        const date = new Date(timestamp * 1000); // Convert to milliseconds
        return date.toLocaleString('en-GB', {
            year: 'numeric',
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit',
            timeZone: 'Europe/London',
            timeZoneName: 'short',
            hour12: false,
        });
    };

    const formatDate = (dateArray) => {
        if (!Array.isArray(dateArray) || dateArray.length < 7) {
            return 'Invalid Date';
        }
        const [year, month, day, hour, minute, second, nanosecond] = dateArray;
        const date = new Date(year, month - 1, day, hour, minute, second, nanosecond / 1000000);
        return date.toLocaleString();
    };

    // State for pagination
    const [currentActionPage, setCurrentActionPage] = useState(1);
    const [currentLoginPage, setCurrentLoginPage] = useState(1);
    const logsPerPage = 5;

    // Sort and paginate user logs (Actions)
    const sortedUserLogs = userLogs[user.id]?.sort((a, b) => b.id - a.id);
    const paginatedUserLogs = sortedUserLogs?.slice(
        (currentActionPage - 1) * logsPerPage,
        currentActionPage * logsPerPage
    );

    // Sort and paginate login logs
    const sortedLoginLogs = loginLogs[user.id]?.sort((a, b) => b.id - a.id);
    const paginatedLoginLogs = sortedLoginLogs?.slice(
        (currentLoginPage - 1) * logsPerPage,
        currentLoginPage * logsPerPage
    );

    const handleNextActionPage = () => {
        setCurrentActionPage((prevPage) => prevPage + 1);
    };

    const handlePreviousActionPage = () => {
        setCurrentActionPage((prevPage) => Math.max(prevPage - 1, 1));
    };

    const handleNextLoginPage = () => {
        setCurrentLoginPage((prevPage) => prevPage + 1);
    };

    const handlePreviousLoginPage = () => {
        setCurrentLoginPage((prevPage) => Math.max(prevPage - 1, 1));
    };

    return (
        <div className="shadow rounded-lg p-4">
            <div className="flex justify-between items-center">
                <div>
                    <p className="text-lg font-semibold">{`${user.firstName} ${user.lastName}`}</p>
                    <p className="text-sm text-gray-500">{user.email}</p>
                    <div className="text-xs text-gray-500 mt-1">
                        <p>Created: {formatDate(user.createdAt)}</p>
                        <p>Updated: {formatDate(user.updatedAt)}</p>
                    </div>
                </div>
                <div className="flex space-x-2">
                    <TooltipProvider>
                        <Tooltip>
                            <TooltipTrigger asChild>
                                <Button variant="ghost" size="icon" onClick={onEdit}>
                                    <Edit className="h-5 w-5" />
                                </Button>
                            </TooltipTrigger>
                            <TooltipContent>
                                <p>Edit User</p>
                            </TooltipContent>
                        </Tooltip>
                    </TooltipProvider>
                    <TooltipProvider>
                        <Tooltip>
                            <TooltipTrigger asChild>
                                <Button variant="ghost" size="icon" onClick={onChangePassword}>
                                    <Lock className="h-5 w-5" />
                                </Button>
                            </TooltipTrigger>
                            <TooltipContent>
                                <p>Change Password</p>
                            </TooltipContent>
                        </Tooltip>
                    </TooltipProvider>
                    <TooltipProvider>
                        <Tooltip>
                            <TooltipTrigger asChild>
                                <Button variant="ghost" size="icon" onClick={onDelete}>
                                    <Trash className="h-5 w-5" />
                                </Button>
                            </TooltipTrigger>
                            <TooltipContent>
                                <p>Delete User</p>
                            </TooltipContent>
                        </Tooltip>
                    </TooltipProvider>
                </div>
            </div>
            <div className="mt-2">
                <Accordion type="single" collapsible>
                    <AccordionItem value="details">
                        <AccordionTrigger onClick={onUserClick}>
                            {expandedUser === user.id ? 'Hide Details' : 'View Details'}
                        </AccordionTrigger>
                        {expandedUser === user.id && (
                            <AccordionContent>
                                <div className="mt-2">
                                    <div className="flex justify-around mb-4">
                                        <Button
                                            variant={
                                                activeTab === 'actions' ? 'default' : 'outline'
                                            }
                                            onClick={() => setActiveTab('actions')}
                                        >
                                            Actions
                                        </Button>
                                        <Button
                                            variant={activeTab === 'logins' ? 'default' : 'outline'}
                                            onClick={() => setActiveTab('logins')}
                                        >
                                            Logins
                                        </Button>
                                    </div>

                                    {uiIsLoadingLogs ? (
                                        <div>
                                            <Skeleton className="h-6 w-full mb-2" />
                                            <Skeleton className="h-6 w-full mb-2" />
                                            <Skeleton className="h-6 w-full mb-2" />
                                        </div>
                                    ) : (
                                        <>
                                            {activeTab === 'actions' && (
                                                <div>
                                                    {paginatedUserLogs?.length > 0 ? (
                                                        paginatedUserLogs.map((log) => (
                                                            <div
                                                                key={log.id}
                                                                className="border-b py-2"
                                                            >
                                                                <p className="text-sm font-semibold">
                                                                    {log.action}
                                                                </p>
                                                                <p className="text-xs text-gray-500">
                                                                    {formatTimestamp(log.timestamp)}
                                                                </p>
                                                                {log.metaData ? (
                                                                    <div className="mt-2">
                                                                        <MetadataViewer
                                                                            metadata={log.metaData}
                                                                            title={`Metadata for ${log.action}`}
                                                                        />
                                                                    </div>
                                                                ) : (
                                                                    <div className="mt-2">
                                                                        <Button
                                                                            variant="outline"
                                                                            size="sm"
                                                                            disabled
                                                                        >
                                                                            No Metadata
                                                                        </Button>
                                                                    </div>
                                                                )}
                                                            </div>
                                                        ))
                                                    ) : (
                                                        <p>No actions found</p>
                                                    )}

                                                    {/* Pagination Controls for Actions */}
                                                    <div className="flex justify-between mt-4">
                                                        <Button
                                                            variant="outline"
                                                            onClick={handlePreviousActionPage}
                                                            disabled={currentActionPage === 1}
                                                        >
                                                            Previous
                                                        </Button>
                                                        <Button
                                                            variant="outline"
                                                            onClick={handleNextActionPage}
                                                            disabled={
                                                                sortedUserLogs?.length <=
                                                                currentActionPage * logsPerPage
                                                            }
                                                        >
                                                            Next
                                                        </Button>
                                                    </div>
                                                </div>
                                            )}

                                            {activeTab === 'logins' && (
                                                <div>
                                                    {paginatedLoginLogs?.length > 0 ? (
                                                        paginatedLoginLogs.map((login) => (
                                                            <div
                                                                key={login.id}
                                                                className="border-b py-2"
                                                            >
                                                                <p className="text-sm font-semibold">
                                                                    {login.ipAddress}
                                                                </p>
                                                                <p className="text-xs text-gray-500">
                                                                    {formatDate(login.loginTime)}
                                                                </p>
                                                            </div>
                                                        ))
                                                    ) : (
                                                        <p>No login logs found</p>
                                                    )}

                                                    {/* Pagination Controls for Logins */}
                                                    <div className="flex justify-between mt-4">
                                                        <Button
                                                            variant="outline"
                                                            onClick={handlePreviousLoginPage}
                                                            disabled={currentLoginPage === 1}
                                                        >
                                                            Previous
                                                        </Button>
                                                        <Button
                                                            variant="outline"
                                                            onClick={handleNextLoginPage}
                                                            disabled={
                                                                sortedLoginLogs?.length <=
                                                                currentLoginPage * logsPerPage
                                                            }
                                                        >
                                                            Next
                                                        </Button>
                                                    </div>
                                                </div>
                                            )}
                                        </>
                                    )}
                                </div>
                            </AccordionContent>
                        )}
                    </AccordionItem>
                </Accordion>
            </div>
        </div>
    );
};

export default UserCard;
