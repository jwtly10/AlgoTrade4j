import React, {useEffect, useState} from 'react';
import {Card, CardContent, CardDescription, CardHeader, CardTitle} from "@/components/ui/card.jsx";
import {Button} from "@/components/ui/button.jsx";
import {BarChart, RefreshCcwDot, Settings, Zap} from 'lucide-react';
import {useNavigate} from 'react-router-dom';

import {useToast} from "@/hooks/use-toast";
import {liveNewsClient, liveOverViewClient} from "@/api/liveClient";

import {countryIcons, impactIcons} from "@/views/main/NewsView.jsx";
import {RecentActivityCard} from "@/home/RecentActivityCard.jsx";
import SystemHealthCard from "@/components/monitor/SystemHealthCard.jsx";

const QuickActionButton = ({icon, label, onClick}) => (
    <Button variant="outline" className="w-full flex items-center justify-start space-x-2" onClick={onClick}>
        {icon}
        <span>{label}</span>
    </Button>
);

const NewsWidget = () => {
    const [todayNews, setTodayNews] = useState([]);
    const {toast} = useToast();

    const navigate = useNavigate();

    useEffect(() => {
        async function fetchTodayNews() {
            try {
                const data = await liveNewsClient.getNews();
                const today = new Date();
                today.setHours(0, 0, 0, 0);

                const todayEvents = data.filter(item => {
                    const eventDate = new Date(item.date * 1000);
                    const eventDateStart = new Date(eventDate);
                    eventDateStart.setHours(0, 0, 0, 0);
                    return eventDateStart.getTime() === today.getTime();
                }).slice(0, 5); // Limit to 5 events for the widget

                setTodayNews(todayEvents);
            } catch (error) {
                toast({
                    title: 'Error',
                    description: `Failed to get news: ${error.message}`,
                    variant: 'destructive',
                });
            }
        }

        fetchTodayNews();
    }, [toast]);

    return (
        <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-lg font-semibold">Today's Economic Events</CardTitle>
                <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => navigate('/news')}
                    className="text-sm text-muted-foreground hover:text-primary"
                >
                    View All
                </Button>
            </CardHeader>
            <CardContent>
                <div className="space-y-2">
                    {todayNews.length === 0 ? (
                        <div className="text-center text-muted-foreground py-4">
                            No economic events scheduled for today
                        </div>
                    ) : (
                        todayNews.map((item, index) => {
                            const eventTime = new Date(item.date * 1000);
                            const isPastEvent = eventTime < new Date();

                            return (
                                <div
                                    key={index}
                                    className={`flex items-center space-x-3 p-2 rounded-lg border ${
                                        isPastEvent ? 'text-muted-foreground bg-muted/50' : 'bg-card hover:bg-accent'
                                    }`}
                                >
                                    <div className="flex-shrink-0">
                                        {countryIcons[item.country] || '🌐'}
                                    </div>
                                    <div className="flex-1 min-w-0">
                                        <p className="text-sm font-medium truncate">
                                            {item.title}
                                        </p>
                                        <div className="flex items-center text-xs text-muted-foreground">
                                            <span>{eventTime.toLocaleTimeString('en-US', {
                                                hour: '2-digit',
                                                minute: '2-digit',
                                                hour12: true
                                            })}</span>
                                            <span className="mx-1">•</span>
                                            <div className="flex items-center">
                                                {impactIcons[item.impact]}
                                                <span className="ml-1">{item.impact}</span>
                                            </div>
                                        </div>
                                    </div>
                                    {(item.forecast || item.previous) && (
                                        <div className="text-xs text-right flex-shrink-0">
                                            {item.forecast && (
                                                <div>F: {item.forecast}</div>
                                            )}
                                            {item.previous && (
                                                <div>P: {item.previous}</div>
                                            )}
                                        </div>
                                    )}
                                </div>
                            );
                        })
                    )}
                </div>
            </CardContent>
        </Card>
    );
};


const HomeView = () => {
    const navigate = useNavigate();
    const {toast} = useToast();
    const [recentActivities, setRecentActivities] = useState([]);

    useEffect(() => {
        async function fetchRecentActivities() {
            try {
                const data = await liveOverViewClient.getRecentActivities();
                setRecentActivities(data);
            } catch (error) {
                toast({
                    title: 'Error',
                    description: `Failed to get recent activities: ${error.message}`,
                    variant: 'destructive',
                });
            }
        }

        fetchRecentActivities();
    }, [])

    return (
        <div className="container mx-auto px-4 py-4 sm:py-8">
            <h1 className="text-3xl font-bold mb-6">Welcome to AlgoTrade4j</h1>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-6">
                <SystemHealthCard/>

                <Card>
                    <CardHeader>
                        <CardTitle>Live Strategies</CardTitle>
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

            <div className="mb-6">
                <NewsWidget/>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
                <RecentActivityCard recentActivities={recentActivities}/>

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
                            label="Manage Live Strategies"
                            onClick={() => navigate('/live')}
                        />
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