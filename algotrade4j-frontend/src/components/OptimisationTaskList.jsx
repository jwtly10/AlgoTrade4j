import React, {useState} from 'react';
import {Box, Button, Chip, CircularProgress, Dialog, DialogActions, DialogContent, DialogTitle, Divider, IconButton, LinearProgress, Paper, styled, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Tooltip, Typography,} from '@mui/material';
import {Assessment, Delete, Share, TaskAlt, Visibility} from '@mui/icons-material';
import {adminClient, apiClient} from '../api/apiClient';
import {useOptimisationTasks} from '../hooks/useOptimisationTasks.js';
import ConfigDialog from "./optimisation/ConfigDialog.jsx";
import {Toast} from "./Toast.jsx";
import ShareDialog from "./optimisation/ShareDialog.jsx";
import OptimizationResults from "./OptimisationResults.jsx";
import AccessTimeIcon from '@mui/icons-material/AccessTime';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import ErrorIcon from '@mui/icons-material/Error';
import PendingIcon from '@mui/icons-material/Pending';
import log from '../logger.js'


// Styled components for larger dialogs
const LargeDialog = styled(Dialog)(({theme}) => ({
    '& .MuiDialog-paper': {
        width: '90%',
        maxWidth: '90%',
        maxHeight: '90%',
    },
}));

const LargeDialogContent = styled(DialogContent)(({theme}) => ({
    height: '70vh',
    overflowY: 'auto',
}));

const StyledChip = styled(Chip)(({theme, state}) => ({
    fontWeight: 'bold',
    color: theme.palette.getContrastText(
        state === 'COMPLETED' ? theme.palette.success.main :
            state === 'FAILED' ? theme.palette.error.main :
                state === 'RUNNING' ? theme.palette.info.main :
                    theme.palette.grey[500]
    ),
    backgroundColor:
        state === 'COMPLETED' ? theme.palette.success.main :
            state === 'FAILED' ? theme.palette.error.main :
                state === 'RUNNING' ? theme.palette.info.main :
                    theme.palette.grey[500],
}));

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

    const getStateIcon = (state) => {
        switch (state) {
            case 'COMPLETED':
                return <CheckCircleIcon/>;
            case 'FAILED':
                return <ErrorIcon/>;
            case 'RUNNING':
                return <AccessTimeIcon/>;
            default:
                return <PendingIcon/>;
        }
    };

    return (
        <TableRow>
            <TableCell>{task.id}</TableCell>
            <TableCell>{task.config.strategyClass}</TableCell>
            <TableCell>
                <StyledChip
                    icon={getStateIcon(task.state)}
                    label={task.state}
                    state={task.state}
                />
                {task.state === 'RUNNING' && task.progressInfo && (
                    <Box sx={{mt: 1}}>
                        <LinearProgress
                            variant="determinate"
                            value={task.progressInfo.percentage}
                            sx={{mb: 1, height: 8, borderRadius: 5}}
                        />
                        <Box sx={{display: 'flex', justifyContent: 'space-between', alignItems: 'center'}}>
                            <Typography variant="body2" color="text.secondary">
                                {`${task.progressInfo.completedTasks} / ${task.progressInfo.completedTasks + task.progressInfo.remainingTasks} params`}
                            </Typography>
                            <Typography variant="body2" color="text.secondary">
                                {task.progressInfo.completedTasks === 0
                                    ? 'Calculating ETA...'
                                    : `ETA: ${formatTime(task.progressInfo.estimatedTimeMs)}`
                                }
                            </Typography>
                        </Box>
                    </Box>
                )}
                {task.state === 'FAILED' && (
                    <Tooltip title={task.errorMessage || "An error occurred"}>
                        <Typography color="error" variant="body2" sx={{mt: 1}}>
                            Click for error details
                        </Typography>
                    </Tooltip>
                )}
            </TableCell>
            <TableCell>{formatDate(task.createdAt)}</TableCell>
            <TableCell>
                <Tooltip title="Share Task">
                    <IconButton onClick={() => onShare(task)} size="small" sx={{mr: 1}}>
                        <Share/>
                    </IconButton>
                </Tooltip>
                <Tooltip title="View Configuration">
                    <IconButton onClick={() => onViewConfig(task)} size="small" sx={{mr: 1}}>
                        <Visibility/>
                    </IconButton>
                </Tooltip>
                {task.state !== "RUNNING" && (
                    <Tooltip title="Delete Task">
                        <IconButton onClick={() => onDelete(task.id)} size="small" sx={{mr: 1}}>
                            <Delete/>
                        </IconButton>
                    </Tooltip>
                )}
                {task.state === "COMPLETED" && (
                    <Tooltip title="Get Results">
                        <IconButton onClick={() => onGetResults(task)} size="small">
                            <Assessment/>
                        </IconButton>
                    </Tooltip>
                )}
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
    const [toast, setToast] = useState({
        open: false,
        level: 'info',
        message: '',
    });

    const [selectedTaskResults, setSelectedTaskResults] = useState([]);

    const handleCloseToast = (event, reason) => {
        if (reason === 'clickaway') {
            return;
        }
        setToast({...toast, level: "info", open: false});
    };

    const handleForceRefresh = () => {
        fetchOptimisationTasks();
    };

    const handleShare = async (task) => {
        setSelectedTask(task);

        try {
            const res = await adminClient.getUsers()
            setUsers(res)
        } catch (error) {
            setToast({
                open: true,
                level: 'error',
                message: 'Failed to get users: ' + error.response.data.message,
            })
        }
        setShareDialogOpen(true);
    };

    const handleDelete = async (taskId) => {
        if (window.confirm("Are you sure you want to delete this task?")) {
            try {
                await apiClient.deleteTask(taskId);
                setToast({
                    open: true,
                    level: 'success',
                    message: 'Task deleted successfully',
                });
                handleForceRefresh()
            } catch (error) {
                setToast({
                    open: true,
                    level: 'error',
                    message: 'Failed to delete task: ' + error.response?.data?.message || error.message,
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
            setToast({
                    open: true,
                    level: 'success',
                    message: "Share successful"
                }
            )
        } catch (error) {
            log.debug(error)
            setToast({
                open: true,
                level: 'error',
                message: error.response.data.message,
            })
        }
    };

    if (isLoading && optimisationTasks.length === 0) {
        return (
            <Box sx={{display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100%'}}>
                <CircularProgress/>
            </Box>
        );
    }

    if (optimisationTasks.length === 0) {
        return (
            <Paper sx={{width: '100%', height: '100%', display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center', p: 3}}>
                <TaskAlt sx={{fontSize: 60, color: 'primary.main', mb: 2}}/>
                <Typography variant="h5" gutterBottom>
                    No Optimization Tasks
                </Typography>
                <Typography variant="body1" color="text.secondary" align="center">
                    There are currently no optimization tasks to display.
                    New tasks will appear here when they are created.
                </Typography>
            </Paper>
        );
    }

    return (
        <Paper sx={{width: '100%', height: '100%', display: 'flex', flexDirection: 'column', overflow: 'hidden'}}>
            <TableContainer sx={{flexGrow: 1, overflow: 'auto'}}>
                <Table stickyHeader aria-label="Optimization Tasks">
                    <TableHead>
                        <TableRow>
                            <TableCell>ID</TableCell>
                            <TableCell>Strategy</TableCell>
                            <TableCell>State</TableCell>
                            <TableCell>Created</TableCell>
                            <TableCell>Actions</TableCell>
                        </TableRow>
                    </TableHead>
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
            </TableContainer>

            {/* Share Dialog */}
            <ShareDialog
                open={shareDialogOpen}
                onClose={() => setShareDialogOpen(false)}
                users={users}
                handleShareWithUser={handleShareWithUser}
            />

            {/* Config Dialog */}
            <ConfigDialog
                open={configDialogOpen}
                onClose={() => setConfigDialogOpen(false)}
                selectedTask={selectedTask}
            />

            {/* Results Dialog */}
            <LargeDialog open={resultsDialogOpen} onClose={() => setResultsDialogOpen(false)}>
                <DialogTitle>Optimisation Results</DialogTitle>
                <Divider/>
                <LargeDialogContent>
                    <OptimizationResults task={selectedTask}/>
                </LargeDialogContent>
                <DialogActions>
                    <Button onClick={() => setResultsDialogOpen(false)}>Close</Button>
                </DialogActions>
            </LargeDialog>
            <Toast
                open={toast.open}
                message={toast.message}
                severity={toast.level}
                onClose={handleCloseToast}
            />
        </Paper>
    );
};

export default OptimizationTaskList;