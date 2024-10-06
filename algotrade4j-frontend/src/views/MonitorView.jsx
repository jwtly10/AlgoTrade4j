import React, {useEffect, useState} from 'react';
import {systemClient as MainSystem} from '../api/apiClient';
import {liveMonitorClient as LiveSystem} from '../api/liveClient.js';
import log from '../logger.js';
import {Card, CardContent, CardHeader, CardTitle} from "@/components/ui/card";
import {Activity, Clock, Coffee, MemoryStick, RefreshCw, Server} from 'lucide-react';
import {Progress} from "@/components/ui/progress";
import {Button} from "@/components/ui/button";

const MonitorView = () => {
    const [mainMonitorInfo, setMainMonitorInfo] = useState(null);
    const [liveMonitorInfo, setLiveMonitorInfo] = useState(null);
    const [isLoading, setIsLoading] = useState(true);

    const fetchMonitorInfo = async () => {
        setIsLoading(true);
        try {
            const [mainRes, liveRes] = await Promise.all([
                MainSystem.monitor(),
                LiveSystem.monitor()
            ]);
            setMainMonitorInfo(mainRes);
            setLiveMonitorInfo(liveRes);
        } catch (error) {
            log.error('Failed to fetch monitor info:', error);
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        fetchMonitorInfo();
        const interval = setInterval(fetchMonitorInfo, 30000); // Refresh every 30 seconds
        return () => clearInterval(interval);
    }, []);

    if (isLoading && !mainMonitorInfo && !liveMonitorInfo) {
        return <LoadingScreen/>;
    }

    return (
        <div className="container mx-auto px-4 py-8">
            <header className="flex justify-between items-center mb-8">
                <h1 className="text-3xl font-bold">System Monitor</h1>
                <Button onClick={fetchMonitorInfo} className="flex items-center gap-2">
                    <RefreshCw className="h-4 w-4"/>
                    Refresh
                </Button>
            </header>
            <div className="space-y-12">
                <SystemMonitor title="Backtest API" monitorInfo={mainMonitorInfo}/>
                <SystemMonitor title="Live API" monitorInfo={liveMonitorInfo}/>
            </div>
        </div>
    );
};

const SystemMonitor = ({title, monitorInfo}) => {
    if (!monitorInfo) return null;

    return (
        <section className="rounded-lg p-6">
            <h2 className="text-2xl font-semibold mb-6 flex items-center gap-2">
                <Activity className="h-6 w-6"/>
                {title} <span className="text-sm font-normal">(v{monitorInfo.version})</span>
            </h2>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                <MemoryCard jvmMemory={monitorInfo.jvmMemory}/>
                <SystemInfoCard
                    environment={monitorInfo.environment}
                    version={monitorInfo.version}
                />
                <UptimeCard
                    startTime={monitorInfo.startTime}
                    uptime={monitorInfo.uptime}
                    timestamp={monitorInfo.timestamp}
                />
                {Object.keys(monitorInfo).filter(key =>
                    !['jvmMemory', 'environment', 'appVersion', 'version', 'commit', 'timestamp', 'startTime', 'uptime'].includes(key)
                ).map(key => (
                    <GenericCard key={key} title={key} data={monitorInfo[key]}/>
                ))}
            </div>
        </section>
    );
};

const MemoryCard = ({jvmMemory}) => {
    const heapUsagePercent = jvmMemory?.current
        ? parseFloat((jvmMemory.current.used.split(' ')[0] / jvmMemory.current.total.split(' ')[0]) * 100)
        : 0;

    return (
        <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-lg font-medium">JVM Memory</CardTitle>
                <MemoryStick className="h-6 w-6"/>
            </CardHeader>
            <CardContent>
                <div className="text-3xl font-bold mb-2">
                    {heapUsagePercent.toFixed(2)}% Heap Used
                </div>
                <Progress value={heapUsagePercent} className="h-2 mb-4"/>
                <div className="text-sm space-y-1">
                    <div>Max: {jvmMemory?.max || 'N/A'}</div>
                    <div>Allocated: {jvmMemory?.usedOfMax || 'N/A'} ({jvmMemory?.percentUsed || 'N/A'})</div>
                    <div>Heap: {jvmMemory?.current?.used || 'N/A'} / {jvmMemory?.current?.total || 'N/A'}</div>
                </div>
            </CardContent>
        </Card>
    );
};

const SystemInfoCard = ({environment, version, timestamp}) => (
    <Card>
        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-lg font-medium">System Information</CardTitle>
            <Server className="h-6 w-6"/>
        </CardHeader>
        <CardContent>
            <div className="space-y-2 text-sm">
                <div><strong>Environment:</strong> {environment || 'N/A'}</div>
                <div><strong>App Version:</strong> {version || 'N/A'}</div>
            </div>
        </CardContent>
    </Card>
);

const GenericCard = ({title, data}) => (
    <Card>
        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-lg font-medium">{title}</CardTitle>
            <Coffee className="h-6 w-6"/>
        </CardHeader>
        <CardContent>
            <pre className="text-xs whitespace-pre-wrap overflow-auto max-h-40">
                {JSON.stringify(data, null, 2)}
            </pre>
        </CardContent>
    </Card>
);

const UptimeCard = ({startTime, uptime, timestamp}) => (
    <Card>
        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-lg font-medium">Uptime</CardTitle>
            <Clock className="h-6 w-6"/>
        </CardHeader>
        <CardContent>
            <div className="space-y-2 text-sm">
                <div><strong>Start Time:</strong> {startTime || 'N/A'}</div>
                <div><strong>Uptime:</strong> {uptime || 'N/A'}</div>
                <div><strong>Current Time:</strong> {timestamp || 'N/A'}</div>
            </div>
        </CardContent>
    </Card>
);

const LoadingScreen = () => (
    <div className="flex flex-col justify-center items-center h-screen">
        <div className="animate-spin rounded-full h-32 w-32 border-t-2 border-b-2 border-current"></div>
        <p className="mt-4 text-xl font-semibold">Loading system information...</p>
    </div>
);

export default MonitorView;