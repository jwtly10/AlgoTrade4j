import React from 'react';
import {Card, CardContent, CardDescription, CardHeader, CardTitle} from "@/components/ui/card";
import {Button} from "@/components/ui/button";
import {Activity, BarChart, History, RefreshCcwDot, Settings, Zap} from 'lucide-react';
import {useNavigate} from 'react-router-dom';

const QuickActionButton = ({icon, label, onClick}) => (
    <Button variant="outline" className="w-full flex items-center justify-start space-x-2" onClick={onClick}>
        {icon}
        <span>{label}</span>
    </Button>
);


const HomeView = () => {
    const navigate = useNavigate();

    return (
        <div className="container mx-auto p-6">
            <h1 className="text-3xl font-bold mb-6">Welcome to AlgoTrade4j</h1>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-6">
                <Card>
                    <CardHeader>
                        <CardTitle>System Status</CardTitle>
                    </CardHeader>
                    <CardContent>
                        <div className="flex items-center text-green-500">
                            <Activity className="mr-2"/>
                            <span>All systems operational</span>
                        </div>
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader>
                        <CardTitle>Active Strategies</CardTitle>
                    </CardHeader>
                    <CardContent>
                        <div className="text-3xl font-bold">5</div>
                        <CardDescription>2 running, 3 paused</CardDescription>
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader>
                        <CardTitle>Today's Performance</CardTitle>
                    </CardHeader>
                    <CardContent>
                        <div className="text-3xl font-bold text-green-500">+2.34%</div>
                        <CardDescription>Across all strategies</CardDescription>
                    </CardContent>
                </Card>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
                <Card>
                    <CardHeader>
                        <CardTitle>Quick Actions</CardTitle>
                    </CardHeader>
                    <CardContent className="space-y-2">
                        <QuickActionButton
                            icon={<BarChart className="h-4 w-4"/>}
                            label="View Performance"
                            onClick={() => navigate('/performance')}
                        />
                        <QuickActionButton
                            icon={<Zap className="h-4 w-4"/>}
                            label="Start New Backtest"
                            onClick={() => navigate('/backtest')}
                        />
                        <QuickActionButton
                            icon={<RefreshCcwDot className="h-4 w-4"/>}
                            label="Start New Optimisation"
                            onClick={() => navigate('/optimisation')}
                        />
                        <QuickActionButton
                            icon={<Settings className="h-4 w-4"/>}
                            label="Manage Strategies"
                            onClick={() => navigate('/strategies')}
                        />
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader>
                        <CardTitle>Recent Activity</CardTitle>
                    </CardHeader>
                    <CardContent>
                        <ul className="space-y-2">
                            <li className="flex items-center">
                                <History className="h-4 w-4 mr-2"/>
                                <span>SMACrossover strategy started</span>
                            </li>
                            <li className="flex items-center">
                                <History className="h-4 w-4 mr-2"/>
                                <span>Backtest completed for RSI strategy</span>
                            </li>
                            <li className="flex items-center">
                                <History className="h-4 w-4 mr-2"/>
                                <span>New market data synced for EURUSD</span>
                            </li>
                        </ul>
                    </CardContent>
                </Card>
            </div>

            <Card>
                <CardHeader>
                    <CardTitle>System Resources</CardTitle>
                </CardHeader>
                <CardContent>
                    <QuickActionButton
                        icon={<BarChart className="h-4 w-4"/>}
                        label="Server Monitor"
                        onClick={() => navigate('/monitor')}
                    />
                </CardContent>
            </Card>
        </div>
    );
};

export default HomeView;