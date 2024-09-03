import React, {useEffect, useState} from 'react';
import {Tooltip, TooltipContent, TooltipProvider, TooltipTrigger} from "@/components/ui/tooltip";
import {systemClient} from '@/api/apiClient.js';
import log from '../../logger.js';

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
                log.error('Failed to fetch version info:', error);
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
            log.debug('Heap dump initiated');
        } catch (error) {
            log.error('Failed to dump heap:', error);
        }
    };

    if (!versionInfo) return null;

    const getBannerColor = () => {
        switch (versionInfo.environment.toLowerCase()) {
            case 'production':
                return 'bg-blue-600'; // Blue for production
            case 'dev':
                return 'bg-purple-600'; // Purple for dev
            default:
                return 'bg-green-600'; // Green for other environments
        }
    };

    const showDumpHeap = isDevelopment && user?.role === 'ADMIN';

    return (
        <div className={`${getBannerColor()} text-white p-1 text-center h-8 leading-8 font-bold shadow-md flex justify-center items-center`}>
            <div className="text-sm font-bold">
                {versionInfo.environment.toUpperCase()} - v{versionInfo.version}
                {' | '}
                <TooltipProvider>
                    <Tooltip>
                        <TooltipTrigger className="cursor-help">
                            Uptime: {calculateUptime(versionInfo.startTime)}
                        </TooltipTrigger>
                        <TooltipContent>
                            <p>App started at: {versionInfo.startTime}</p>
                        </TooltipContent>
                    </Tooltip>
                </TooltipProvider>
                {showDumpHeap && (
                    <>
                        {' | '}
                        <span
                            onClick={handleDumpHeap}
                            className="cursor-pointer underline"
                        >
              DEBUG: Dump Java Heap
            </span>
                    </>
                )}
            </div>
        </div>
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