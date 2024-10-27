import React, {useEffect, useState} from 'react';
import {liveNewsClient} from "@/api/liveClient.js";
import {Folder} from 'lucide-react';
import {useToast} from "@/hooks/use-toast.js";
import {Card, CardContent, CardHeader, CardTitle} from "@/components/ui/card.jsx";
import {Button} from "@/components/ui/button.jsx";
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from "@/components/ui/select.jsx";

export const impactIcons = {
    High: <Folder className="text-red-500 fill-red-500 size-5" title="High Impact"/>,
    Medium: <Folder className="text-orange-500 fill-orange-500 size-5" title="Medium Impact"/>,
    Low: <Folder className="text-yellow-500 fill-yellow-500 size-5" title="Low Impact"/>,
    Holiday: <Folder className="text-gray-500 fill-gray-500 size-5" title="Holiday"/>,
};

export const countryIcons = {
    USD: '🇺🇸', // United States Dollar
    CAD: '🇨🇦', // Canadian Dollar
    JPY: '🇯🇵', // Japanese Yen
    GBP: '🇬🇧', // British Pound
    CNY: '🇨🇳', // Chinese Yuan
    NZD: '🇳🇿', // New Zealand Dollar
    AUD: '🇦🇺', // Australian Dollar
    CHF: '🇨🇭', // Swiss Franc
    EUR: '🇪🇺', // Euro
};


const NewsView = () => {
    const [newsData, setNewsData] = useState([]);
    const [filteredData, setFilteredData] = useState(null); // initially null
    const [countryFilter, setCountryFilter] = useState(localStorage.getItem('countryFilter') || '');
    const [impactFilter, setImpactFilter] = useState(localStorage.getItem('impactFilter') || '');
    const {toast} = useToast();

    useEffect(() => {
        async function fetchNews() {
            try {
                const data = await liveNewsClient.getNews();
                setNewsData(data);
                applyFilters(data);
            } catch (error) {
                toast({
                    title: 'Error',
                    description: `Failed to get news: ${error.message}`,
                    variant: 'destructive',
                });
            }
        }

        fetchNews();
    }, [toast]);

    const applyFilters = (data) => {
        let filtered = data;
        if (countryFilter) {
            filtered = filtered.filter(item => item.country === countryFilter);
        }
        if (impactFilter) {
            filtered = filtered.filter(item => item.impact === impactFilter);
        }
        setFilteredData(filtered);
    };

    const getNextEvent = () => {
        if (!filteredData) return null;
        const now = new Date();
        return filteredData.find(item => new Date(item.date * 1000) > now);
    };

    const nextEvent = getNextEvent()

    useEffect(() => {
        if (newsData.length) applyFilters(newsData);
    }, [countryFilter, impactFilter, newsData]);

    useEffect(() => {
        localStorage.setItem('countryFilter', countryFilter);
        localStorage.setItem('impactFilter', impactFilter);
    }, [countryFilter, impactFilter]);

    const groupedData = filteredData?.reduce((acc, item) => {
        const date = new Date(item.date * 1000).toLocaleDateString('en-GB', {
            weekday: 'long', year: 'numeric', month: 'short', day: 'numeric'
        });
        if (!acc[date]) acc[date] = [];
        acc[date].push(item);
        return acc;
    }, {});

    return (
        <div className="container p-6 mx-auto px-4 py-8">
            <div className="mb-8">  {/* removed flex-shrink-0 */}
                <h1 className="text-3xl font-bold">Economic Calendar</h1>
                <p className="text-sm text-muted-foreground">
                    Data by <a href="https://www.forexfactory.com" className="text-primary hover:underline" target="_blank"
                               rel="noopener noreferrer">ForexFactory</a>
                </p>
            </div>

            <Card className="mb-6">
                <CardHeader>
                    <CardTitle className="text-lg font-medium">Filters</CardTitle>
                </CardHeader>
                <CardContent>
                    <div className="flex flex-wrap items-center gap-6">
                        <div className="flex-1 min-w-[230px]">
                            <label className="text-sm font-medium mb-2 block">Country</label>
                            <Select
                                value={countryFilter || null}
                                onValueChange={(value) => setCountryFilter(value || '')}
                            >
                                <SelectTrigger className="w-full">
                                    <SelectValue placeholder="All Countries"/>
                                </SelectTrigger>
                                <SelectContent>
                                    <SelectItem value={null}>All Countries</SelectItem>
                                    {Object.entries(countryIcons).map(([key, emoji]) => (
                                        <SelectItem key={key} value={key}>
                                            <div className="flex items-center space-x-2">
                                                <span>{emoji}</span>
                                                <span>{key}</span>
                                            </div>
                                        </SelectItem>
                                    ))}
                                </SelectContent>
                            </Select>
                        </div>

                        <div className="flex-1 min-w-[240px]">
                            <label className="text-sm font-medium mb-2 block">Impact</label>
                            <Select
                                value={impactFilter || null}
                                onValueChange={(value) => setImpactFilter(value || '')}
                            >
                                <SelectTrigger className="w-full">
                                    <SelectValue placeholder="All Impact Levels"/>
                                </SelectTrigger>
                                <SelectContent>
                                    <SelectItem value={null}>All Impact Levels</SelectItem>
                                    {Object.entries(impactIcons).map(([key, icon]) => (
                                        <SelectItem key={key} value={key}>
                                            <div className="flex items-center space-x-2">
                                                {icon}
                                                <span>{key}</span>
                                            </div>
                                        </SelectItem>
                                    ))}
                                </SelectContent>
                            </Select>
                        </div>

                        <div className="flex flex-col justify-end h-[68px]">
                            <Button
                                variant="outline"
                                onClick={() => {
                                    setCountryFilter('');
                                    setImpactFilter('');
                                    localStorage.removeItem('countryFilter');
                                    localStorage.removeItem('impactFilter');
                                }}
                            >
                                Clear Filters
                            </Button>
                        </div>
                    </div>
                </CardContent>
            </Card>


            {filteredData !== null && (
                <div className="rounded-md border">
                    <div className="h-[calc(100vh-450px)] overflow-auto space-y-6 pr-2">
                        {Object.entries(groupedData).map(([date, items]) => (
                            <Card key={date}>
                                <CardHeader className="bg-muted/50">
                                    <CardTitle className="text-lg font-medium">{date} (UTC)</CardTitle>
                                </CardHeader>
                                <CardContent className="p-0">
                                    <div className="divide-y">
                                        {items.map((item, index) => {
                                            const isPastEvent = new Date(item.date * 1000) < new Date();
                                            const isNextEvent = nextEvent && nextEvent.date === item.date;
                                            const eventTime = new Date(item.date * 1000).toLocaleTimeString('en-US', {
                                                hour: '2-digit',
                                                minute: '2-digit',
                                                hour12: true,
                                                timeZone: 'UTC'
                                            });

                                            return (
                                                <div
                                                    key={index}
                                                    className={`flex items-center p-4 gap-4 ${
                                                        isPastEvent
                                                            ? 'text-muted-foreground bg-muted/50'
                                                            : 'hover:bg-accent/50 transition-colors'
                                                    }`}
                                                >
                                                    <div className="w-20 font-medium">
                                                        {isNextEvent && (
                                                            <div className="text-xs text-primary font-semibold mb-1">NEXT EVENT</div>
                                                        )}
                                                        {eventTime}
                                                    </div>

                                                    <div className="flex items-center gap-2 w-32">
                                                        <span className="text-lg">{countryIcons[item.country] || '🌐'}</span>
                                                        <span>{item.country}</span>
                                                    </div>

                                                    <div className="flex items-center gap-2 w-32">
                                                        {impactIcons[item.impact]}
                                                    </div>

                                                    <div className="flex-1 font-medium">
                                                        {item.title}
                                                    </div>

                                                    <div className="flex gap-6 w-48 text-right">
                                                        <div>
                                                            <div className="text-xs text-muted-foreground">Forecast</div>
                                                            <div>{item.forecast || '-'}</div>
                                                        </div>
                                                        <div>
                                                            <div className="text-xs text-muted-foreground">Previous</div>
                                                            <div>{item.previous || '-'}</div>
                                                        </div>
                                                    </div>
                                                </div>
                                            );
                                        })}
                                    </div>
                                </CardContent>
                            </Card>
                        ))}
                    </div>
                </div>
            )}
        </div>
    );
};

export default NewsView;