import React, {useEffect, useState} from 'react';
import {Card, CardContent, CardHeader, CardTitle} from "@/components/ui/card.jsx";
import {AlertTriangle, CheckCircle2, RefreshCw, XCircle} from 'lucide-react';
import {Button} from "@/components/ui/button.jsx";
import {useToast} from "@/hooks/use-toast";
import {systemClient} from "@/api/systemClient.js";

const StatusIndicator = ({status, label}) => {
    const getStatusIcon = () => {
        switch (status) {
            case 'UP':
                return <CheckCircle2 className="w-5 h-5 text-green-500"/>;
            case 'DOWN':
                return <XCircle className="w-5 h-5 text-red-500"/>;
            case 'UNKNOWN':
                return <AlertTriangle className="w-5 h-5 text-gray-400"/>;
            default:
                return <AlertTriangle className="w-5 h-5 text-gray-400"/>;
        }
    };

    const getStatusDisplay = (status) => {
        switch (status) {
            case 'UP':
                return 'Operational';
            case 'DOWN':
                return 'Down';
            case 'UNKNOWN':
                return 'Unknown';
            default:
                return 'Unknown';
        }
    };

    return (
        <div className="flex items-center justify-between p-3 bg-card hover:bg-accent rounded-lg border">
            <div className="flex items-center space-x-3">
                {getStatusIcon()}
                <span className="font-medium">{label}</span>
            </div>
            <div className={`text-sm ${
                status === 'UP' ? 'text-green-500' :
                    status === 'DOWN' ? 'text-red-500' :
                        'text-gray-400'
            }`}>
                {getStatusDisplay(status)}
            </div>
        </div>
    );
};

const SystemHealthCard = () => {
    const [health, setHealth] = useState({
        api: 'UNKNOWN',
        live: 'UNKNOWN',
        mt5: 'UNKNOWN'
    });
    const [isLoading, setIsLoading] = useState(false);
    const {toast} = useToast();

    const fetchHealthStatus = async () => {
        setIsLoading(true);

        const fetchEndpoint = async (endpoint, serviceName) => {
            try {
                const response = await endpoint();
                return response.status || 'DOWN';
            } catch (error) {
                console.error(`Error fetching ${serviceName} health:`, error);
                toast({
                    title: `${serviceName} Health Check Failed`,
                    description: error.message,
                    variant: 'destructive',
                });
                return 'DOWN';
            }
        };

        const apiStatus = await fetchEndpoint(systemClient.mainHealth, 'Backtest API Service');
        const liveStatus = await fetchEndpoint(systemClient.liveHealth, 'Live Trading Service');
        const mt5Status = await fetchEndpoint(systemClient.mt5Health, 'MT5 Gateway Adapter');

        setHealth({
            api: apiStatus,
            live: liveStatus,
            mt5: mt5Status
        });

        setIsLoading(false);
    };

    useEffect(() => {
        fetchHealthStatus();
        const interval = setInterval(fetchHealthStatus, 60000);
        return () => clearInterval(interval);
    }, []);

    const allHealthy = Object.values(health).every(status => status === 'UP');

    return (
        <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-xl font-bold">System Health</CardTitle>
                <Button
                    variant="ghost"
                    size="sm"
                    onClick={fetchHealthStatus}
                    disabled={isLoading}
                >
                    <RefreshCw className={`h-4 w-4 ${isLoading ? 'animate-spin' : ''}`}/>
                </Button>
            </CardHeader>
            <CardContent>
                <div className="space-y-3">
                    <div className={`p-3 mb-4 rounded-lg ${
                        allHealthy ? 'bg-green-100 dark:bg-green-900/20' : 'bg-yellow-100 dark:bg-yellow-900/20'
                    }`}>
                        <p className="text-sm font-medium">
                            {allHealthy
                                ? '✨ All systems are operational'
                                : '⚠️ Some systems require attention'}
                        </p>
                    </div>

                    <StatusIndicator status={health.api} label="Backtest API Service"/>
                    <StatusIndicator status={health.live} label="Live Trading Service"/>
                    <StatusIndicator status={health.mt5} label="MT5 Gateway Adapter"/>
                </div>
            </CardContent>
        </Card>
    );
};

export default SystemHealthCard;