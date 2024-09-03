import React, {useEffect, useState} from 'react';
import {systemClient} from '../api/apiClient';
import log from '../logger.js';
import {Card, CardContent, CardHeader, CardTitle} from "@/components/ui/card";
import {Clock, Coffee, MemoryStick, Server} from 'lucide-react';
import {Progress} from "@/components/ui/progress";

const MonitorView = () => {
    const [monitorInfo, setMonitorInfo] = useState(null);

    useEffect(() => {
        const fetchMonitorInfo = async () => {
            try {
                const response = await systemClient.monitor();
                setMonitorInfo(response);
            } catch (error) {
                log.error('Failed to fetch monitor info:', error);
            }
        };

        fetchMonitorInfo();
        const interval = setInterval(fetchMonitorInfo, 5000); // Refresh every 5 seconds

        return () => clearInterval(interval);
    }, []);

    if (!monitorInfo) {
        return <div className="flex justify-center items-center h-screen">Loading system information...</div>;
    }

    return (
        <div className="container mx-auto px-4 py-8">
            <h1 className="text-2xl font-semibold mb-6">System Monitor</h1>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                <MemoryCard jvmMemory={monitorInfo.jvmMemory}/>
                <SystemInfoCard
                    environment={monitorInfo.environment}
                    javaVersion={monitorInfo.javaVersion}
                    version={monitorInfo.version}
                    commit={monitorInfo.commit}
                />
                <UptimeCard
                    uptime={monitorInfo.uptime}
                    startTime={monitorInfo.startTime}
                    timestamp={monitorInfo.timestamp}
                />
                {/* Add placeholder for potential future cards */}
                {Object.keys(monitorInfo).filter(key =>
                    !['jvmMemory', 'environment', 'javaVersion', 'version', 'commit', 'uptime', 'startTime', 'timestamp'].includes(key)
                ).map(key => (
                    <GenericCard key={key} title={key} data={monitorInfo[key]}/>
                ))}
            </div>
        </div>
    );
};

const MemoryCard = ({jvmMemory}) => {
    const heapUsagePercent = jvmMemory?.current
        ? parseFloat((jvmMemory.current.used.split(' ')[0] / jvmMemory.current.total.split(' ')[0]) * 100)
        : 0;

    return (
        <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">JVM Memory</CardTitle>
                <MemoryStick className="h-6 w-6"/>
            </CardHeader>
            <CardContent>
                <div className="text-2xl font-bold">
                    {heapUsagePercent.toFixed(2)}% Heap Used
                </div>
                <Progress value={heapUsagePercent} className="mt-2"/>
                <div className="text-xs text-muted-foreground mt-2">
                    <div>Max: {jvmMemory?.max || 'N/A'}</div>
                    <div>Allocated: {jvmMemory?.usedOfMax || 'N/A'} ({jvmMemory?.percentUsed || 'N/A'})</div>
                    <div>Heap: {jvmMemory?.current?.used || 'N/A'} / {jvmMemory?.current?.total || 'N/A'}</div>
                </div>
            </CardContent>
        </Card>
    );
};

const SystemInfoCard = ({environment, javaVersion, version, commit}) => (
    <Card>
        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">System Information</CardTitle>
            <Server className="h-6 w-6"/>
        </CardHeader>
        <CardContent>
            <div className="text-xs text-muted-foreground">
                <div>Environment: {environment || 'N/A'}</div>
                <div>Java Version: {javaVersion || 'N/A'}</div>
                <div>App Version: {version || 'N/A'}</div>
                <div>Commit: {commit || 'N/A'}</div>
            </div>
        </CardContent>
    </Card>
);

const UptimeCard = ({uptime, startTime, timestamp}) => (
    <Card>
        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Uptime</CardTitle>
            <Clock className="h-6 w-6"/>
        </CardHeader>
        <CardContent>
            <div className="text-2xl font-bold">{uptime || 'N/A'}</div>
            <div className="text-xs text-muted-foreground mt-2">
                <div>Start Time: {startTime || 'N/A'}</div>
                <div>Current Time: {timestamp || 'N/A'}</div>
            </div>
        </CardContent>
    </Card>
);

const GenericCard = ({title, data}) => (
    <Card>
        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">{title}</CardTitle>
            <Coffee className="h-6 w-6"/>
        </CardHeader>
        <CardContent>
            <pre className="text-xs text-muted-foreground whitespace-pre-wrap">
                {JSON.stringify(data, null, 2)}
            </pre>
        </CardContent>
    </Card>
);

export default MonitorView;