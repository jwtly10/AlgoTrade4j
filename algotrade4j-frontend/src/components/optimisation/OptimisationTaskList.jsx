import React, {useState} from 'react';
import {useOptimisationTasks} from '@/hooks/useOptimisationTasks.js';
import {adminClient, apiClient} from '@/api/apiClient.js';
import {useToast} from "@/hooks/use-toast.js";
import {Button} from "../ui/button";
import {Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle} from "../ui/dialog";
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from "../ui/table";
import {Tooltip, TooltipContent, TooltipProvider, TooltipTrigger} from "../ui/tooltip";
import {Card, CardContent, CardHeader, CardTitle} from "../ui/card";
import {Badge} from "../ui/badge";
import {Progress} from "../ui/progress";
import {AlertCircle, BarChart2, CheckCircle, Clock, Eye, HelpCircle, Share, Trash} from "lucide-react";
import ConfigDialog from "./ConfigDialog";
import ShareDialog from "./ShareDialog";
import OptimizationResults from "./OptimisationResults.jsx";
import log from '../../logger.js';

const OptimizationTaskRow = ({task, onShare, onViewConfig, onGetResults, onDelete}) => {
    const formatDate = (dateArray) => {
        if (!Array.isArray(dateArray) || dateArray.length < 7) {
            return 'Invalid Date';
        }
        const [year, month, day, hour, minute, second, nanosecond] = dateArray;
        const date = new Date(year, month - 1, day, hour, minute, second, nanosecond / 1000000);
        return date.toLocaleString();
    };

    const formatTime = (ms) => {
        if (ms < 0) return 'Calculating...';
        const seconds = Math.floor(ms / 1000);
        const minutes = Math.floor(seconds / 60);
        const remainingSeconds = seconds % 60;
        if (minutes === 0) {
            return `${remainingSeconds} sec${remainingSeconds !== 1 ? 's' : ''}`;
        } else if (remainingSeconds === 0) {
            return `${minutes} min${minutes !== 1 ? 's' : ''}`;
        } else {
            return `${minutes} min${minutes !== 1 ? 's' : ''} ${remainingSeconds} sec${remainingSeconds !== 1 ? 's' : ''}`;
        }
    };

    const getStateColor = (state) => {
        switch (state) {
            case 'COMPLETED':
                return 'success';
            case 'FAILED':
                return 'destructive';
            case 'RUNNING':
                return 'blue';
            default:
                return 'secondary';
        }
    };

    const getStateIcon = (state) => {
        switch (state) {
            case 'COMPLETED':
                return <CheckCircle className="h-4 w-4 text-green-500"/>;
            case 'FAILED':
                return <AlertCircle className="h-4 w-4 text-red-500"/>;
            case 'RUNNING':
                return <Clock className="h-4 w-4 text-blue-500"/>;
            default:
                return <HelpCircle className="h-4 w-4 text-gray-500"/>;
        }
    };

    return (
        <TableRow>
            <TableCell>{task.id}</TableCell>
            <TableCell>{task.config.strategyClass}</TableCell>
            <TableCell>
                <Badge variant={getStateColor(task.state)}>
                    {getStateIcon(task.state)}
                    <span className="ml-2">{task.state}</span>
                </Badge>
                {task.state === 'RUNNING' && task.progressInfo && (
                    <div className="mt-2">
                        <Progress value={task.progressInfo.percentage} className="h-2 bg-blue-100"/>
                        <div className="flex justify-between text-xs text-blue-600 mt-1">
                            <span>{`${task.progressInfo.completedTasks} / ${task.progressInfo.completedTasks + task.progressInfo.remainingTasks} params`}</span>
                            <span>
                                {task.progressInfo.completedTasks === 0
                                    ? 'Calculating ETA...'
                                    : `ETA: ${formatTime(task.progressInfo.estimatedTimeMs)}`
                                }
                            </span>
                        </div>
                    </div>
                )}
                {task.state === 'FAILED' && (
                    <TooltipProvider>
                        <Tooltip>
                            <TooltipTrigger asChild>
                                <p className="text-sm text-red-500 mt-1 cursor-help">Click for error details</p>
                            </TooltipTrigger>
                            <TooltipContent>
                                <p className="text-red-600">{task.errorMessage || "An error occurred"}</p>
                            </TooltipContent>
                        </Tooltip>
                    </TooltipProvider>
                )}
            </TableCell>
            <TableCell>{formatDate(task.createdAt)}</TableCell>
            <TableCell>
                <div className="flex space-x-2">
                    <TooltipProvider>
                        <Tooltip>
                            <TooltipTrigger asChild>
                                <Button variant="ghost" size="icon" onClick={() => onShare(task)}>
                                    <Share className="h-4 w-4"/>
                                </Button>
                            </TooltipTrigger>
                            <TooltipContent>
                                <p>Share Task</p>
                            </TooltipContent>
                        </Tooltip>
                    </TooltipProvider>
                    <TooltipProvider>
                        <Tooltip>
                            <TooltipTrigger asChild>
                                <Button variant="ghost" size="icon" onClick={() => onViewConfig(task)}>
                                    <Eye className="h-4 w-4"/>
                                </Button>
                            </TooltipTrigger>
                            <TooltipContent>
                                <p>View Configuration</p>
                            </TooltipContent>
                        </Tooltip>
                    </TooltipProvider>
                    {task.state !== "RUNNING" && (
                        <TooltipProvider>
                            <Tooltip>
                                <TooltipTrigger asChild>
                                    <Button variant="ghost" size="icon" onClick={() => onDelete(task.id)}>
                                        <Trash className="h-4 w-4"/>
                                    </Button>
                                </TooltipTrigger>
                                <TooltipContent>
                                    <p>Delete Task</p>
                                </TooltipContent>
                            </Tooltip>
                        </TooltipProvider>
                    )}
                    {task.state === "COMPLETED" && (
                        <TooltipProvider>
                            <Tooltip>
                                <TooltipTrigger asChild>
                                    <Button variant="ghost" size="icon" onClick={() => onGetResults(task)}>
                                        <BarChart2 className="h-4 w-4"/>
                                    </Button>
                                </TooltipTrigger>
                                <TooltipContent>
                                    <p>Get Results</p>
                                </TooltipContent>
                            </Tooltip>
                        </TooltipProvider>
                    )}
                </div>
            </TableCell>
        </TableRow>
    );
};

const OptimizationTaskList = () => {
    const {optimisationTasks, isLoading, fetchOptimisationTasks} = useOptimisationTasks(apiClient);
    const [shareDialogOpen, setShareDialogOpen] = useState(false);
    const [configDialogOpen, setConfigDialogOpen] = useState(false);
    const [resultsDialogOpen, setResultsDialogOpen] = useState(false);
    const [selectedTask, setSelectedTask] = useState(null);
    const [users, setUsers] = useState([]);
    const {toast} = useToast();

    const handleForceRefresh = () => {
        fetchOptimisationTasks();
    };

    const handleShare = async (task) => {
        setSelectedTask(task);
        try {
            const res = await adminClient.getUsers()
            setUsers(res)
        } catch (error) {
            toast({
                title: "Error",
                description: `Failed to get users: ${error.message}`,
                variant: "destructive",
            });
        }
        setShareDialogOpen(true);
    };

    const handleDelete = async (taskId) => {
        if (window.confirm("Are you sure you want to delete this task?")) {
            try {
                await apiClient.deleteTask(taskId);
                toast({
                    title: "Success",
                    description: "Task deleted successfully",
                });
                handleForceRefresh()
            } catch (error) {
                toast({
                    title: "Error",
                    description: `Error deleting run: ${error.message}`,
                    variant: "destructive",
                });
            }
        }
    };

    const handleViewConfig = (task) => {
        setSelectedTask(task);
        setConfigDialogOpen(true);
    };

    const handleGetResults = (task) => {
        setSelectedTask(task);
        setResultsDialogOpen(true);
    };

    const handleShareWithUser = async (userId) => {
        log.debug(`Sharing task ${selectedTask.id} with user ${userId}`);
        setShareDialogOpen(false);
        try {
            await apiClient.shareTask(selectedTask.id, userId)
            toast({
                title: "Success",
                description: "Share successful",
            });
        } catch (error) {
            log.debug(error)
            toast({
                title: "Error",
                description: "Error sharing with user: " + error.message,
                variant: "destructive",
            });
        }
    };

    if (isLoading && optimisationTasks.length === 0) {
        return (
            <div className="flex justify-center items-center h-full">
                <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-gray-900"></div>
            </div>
        );
    }

    if (optimisationTasks.length === 0) {
        return (
            <Card className="w-full h-full flex flex-col justify-center items-center p-6">
                <CardHeader>
                    <CardTitle className="text-2xl font-bold">No Optimization Tasks</CardTitle>
                </CardHeader>
                <CardContent>
                    <p className="text-center text-muted-foreground">
                        There are currently no optimization tasks to display.
                        New tasks will appear here when they are created.
                    </p>
                </CardContent>
            </Card>
        );
    }

    return (
        <Card className="w-full flex flex-col overflow-hidden">
            <div className="overflow-auto flex-grow">
                <Table>
                    <TableHeader>
                        <TableRow>
                            <TableHead>ID</TableHead>
                            <TableHead>Strategy</TableHead>
                            <TableHead>State</TableHead>
                            <TableHead>Created</TableHead>
                            <TableHead>Actions</TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {optimisationTasks.map((task) => (
                            <OptimizationTaskRow
                                key={task.id}
                                task={task}
                                onShare={handleShare}
                                onViewConfig={handleViewConfig}
                                onGetResults={handleGetResults}
                                onDelete={handleDelete}
                            />
                        ))}
                    </TableBody>
                </Table>
            </div>

            <ShareDialog
                open={shareDialogOpen}
                onOpenChange={setShareDialogOpen}
                users={users}
                handleShareWithUser={handleShareWithUser}
            />

            <ConfigDialog
                open={configDialogOpen}
                onOpenChange={setConfigDialogOpen}
                selectedTask={selectedTask}
            />

            <Dialog open={resultsDialogOpen} onOpenChange={setResultsDialogOpen}>
                <DialogContent className="max-w-[70vw] max-h-[90vh] flex flex-col">
                    <DialogHeader>
                        <DialogTitle>Optimisation Results</DialogTitle>
                    </DialogHeader>
                    <div className="flex-grow overflow-auto">
                        <OptimizationResults task={selectedTask}/>
                    </div>
                    <DialogFooter>
                        <Button onClick={() => setResultsDialogOpen(false)}>Close</Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>
        </Card>
    );
};

export default OptimizationTaskList;