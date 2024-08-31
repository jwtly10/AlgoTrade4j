import React, {useEffect, useState} from 'react';
import {Box, Tooltip, Typography} from '@mui/material';
import {systemClient} from '../api/apiClient';

const BANNER_HEIGHT = '30px';

const VersionBanner = ({ user }) => {
    const isDevelopment = import.meta.env.MODE === 'development';
    const [versionInfo, setVersionInfo] = useState(null);

    useEffect(() => {
        const fetchVersion = async () => {
            try {
                const response = await systemClient.version();
                setVersionInfo({
                    ...response,
                });
            } catch (error) {
                console.error('Failed to fetch version info:', error);
                setVersionInfo({
                    version: 'Unknown',
                    commit: 'Unknown',
                    environment: 'Unknown',
                });
            }
        };

        fetchVersion();
    }, []);

    const handleDumpHeap = async () => {
        try {
            await systemClient.dumpHeap();
            console.log('Heap dump initiated');
        } catch (error) {
            console.error('Failed to dump heap:', error);
        }
    };

    if (!versionInfo) return null;

    const getBannerColor = () => {
        switch (versionInfo.environment.toLowerCase()) {
            case 'production':
                return '#1976d2'; // Blue for production
            case 'dev':
                return '#9c27b0'; // Purple for dev
            default:
                return '#4caf50'; // Green for other environments
        }
    };

    const showDumpHeap = isDevelopment && user?.role === 'ADMIN';

    return (
        <Box
            sx={{
                backgroundColor: getBannerColor(),
                color: '#ffffff',
                padding: '4px',
                textAlign: 'center',
                height: BANNER_HEIGHT,
                lineHeight: BANNER_HEIGHT,
                fontWeight: 'bold',
                boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
                display: 'flex',
                justifyContent: 'center',
                alignItems: 'center',
            }}
        >
            <Typography variant="body2" sx={{ fontWeight: 'bold' }}>
                {versionInfo.environment.toUpperCase()} - v{versionInfo.version}
                {' | '}
                <Tooltip title={`App started at: ${versionInfo.startTime}`} arrow>
                    <span style={{cursor: 'help'}}>
                        Uptime: {calculateUptime(versionInfo.startTime)}
                    </span>
                </Tooltip>
                {showDumpHeap && (
                    <>
                        {' | '}
                        <span
                            onClick={handleDumpHeap}
                            style={{
                                cursor: 'pointer',
                                textDecoration: 'underline',
                            }}
                        >
                            DEBUG: Dump Java Heap
                        </span>
                    </>
                )}
            </Typography>
        </Box>
    );
};

const calculateUptime = (startTime) => {
    if (!startTime || startTime === 'Unknown') return 'Unknown';

    const start = new Date(startTime);
    const now = new Date();
    const diff = now - start;

    const days = Math.floor(diff / (1000 * 60 * 60 * 24));
    const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
    const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));

    return `${days}d ${hours}h ${minutes}m`;
};

export default VersionBanner;