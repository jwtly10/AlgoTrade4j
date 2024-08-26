import React, {useEffect, useState} from 'react';
import {Box, Typography} from '@mui/material';
import {systemClient} from '../api/apiClient';

const BANNER_HEIGHT = '30px';

const VersionBanner = () => {
    const [versionInfo, setVersionInfo] = useState(null);

    useEffect(() => {
        const fetchVersion = async () => {
            try {
                const response = await systemClient.version()
                setVersionInfo({
                    ...response,
                    environment: response.environment || process.env.REACT_APP_ENVIRONMENT || 'local'
                });
            } catch (error) {
                console.error('Failed to fetch version info:', error);
                setVersionInfo({
                    version: "Unknown",
                    commit: "Unknown",
                    environment: process.env.REACT_APP_ENVIRONMENT || 'local'
                });
            }
        };

        fetchVersion();
    }, []);

    if (!versionInfo) return null;

    const getBannerColor = () => {
        switch (versionInfo.environment.toLowerCase()) {
            case 'production':
                return '#1976d2';  // Blue for production
            case 'dev':
                return '#9c27b0';  // Purple for local
            default:
                return '#4caf50';  // Green for other environments
        }
    };

    const getBannerContent = () => {
        const capitalizedEnvironment = versionInfo.environment.toUpperCase();
        return `Environment: ${capitalizedEnvironment} | Version: ${versionInfo.version} | Commit: ${versionInfo.commit}`;
    };

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
            }}
        >
            <Typography variant="body2" sx={{fontWeight: 'bold'}}>
                {getBannerContent()}
            </Typography>
        </Box>
    );
};

export default VersionBanner;