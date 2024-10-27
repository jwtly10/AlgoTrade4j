import React, {useEffect, useState} from 'react';
import {liveNewsClient} from "@/api/liveClient.js";
import {ChevronDown, Folder} from 'lucide-react';
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

const NewsItem = ({item, isNextEvent, isPastEvent}) => {
    const [isExpanded, setIsExpanded] = useState(false);
    const eventTime = new Date(item.date * 1000).toLocaleTimeString('en-US', {
        hour: '2-digit',
        minute: '2-digit',
        hour12: true,
        timeZone: 'UTC'
    });

    return (
        <div
            onClick={() => setIsExpanded(!isExpanded)}
            className={`p-2 sm:p-4 cursor-pointer ${
                isPastEvent
                    ? 'text-muted-foreground bg-muted/50'
                    : 'hover:bg-accent/50 transition-colors'
            }`}
        >
            <div className="flex flex-col gap-2 sm:gap-4">
                <div className="grid grid-cols-[75px_70px_30px_1fr] lg:grid-cols-[80px_100px_40px_1fr_200px] items-start gap-2 sm:gap-4">
                    <div className="font-medium text-sm sm:text-base">
                        {isNextEvent && (
                            <div className="text-xs text-primary font-semibold mb-1">NEXT</div>
                        )}
                        {eventTime}
                    </div>

                    <div className="flex items-center gap-1 sm:gap-2 text-sm sm:text-base">
                        <span className="text-base sm:text-lg">{countryIcons[item.country] || '🌐'}</span>
                        <span>{item.country}</span>
                    </div>

                    <div className="flex items-center">
                        {impactIcons[item.impact]}
                    </div>

                    <div className="font-medium text-sm sm:text-base break-words">
                        {item.title}
                    </div>

                    {/* Desktop view for forecast/previous */}
                    <div className="hidden lg:flex justify-end gap-6">
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

                {/* Mobile view for forecast/previous - shown when expanded */}
                {isExpanded && (
                    <div className="lg:hidden flex justify-end gap-4 pt-2 border-t text-sm">
                        <div>
                            <div className="text-xs text-muted-foreground">Forecast</div>
                            <div>{item.forecast || '-'}</div>
                        </div>
                        <div>
                            <div className="text-xs text-muted-foreground">Previous</div>
                            <div>{item.previous || '-'}</div>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};
;


const NewsView = () => {
    const [newsData, setNewsData] = useState([]);
    const [filteredData, setFilteredData] = useState(null); // initially null
    const [countryFilter, setCountryFilter] = useState(localStorage.getItem('countryFilter') || '');
    const [impactFilter, setImpactFilter] = useState(localStorage.getItem('impactFilter') || '');
    const [isFilterExpanded, setIsFilterExpanded] = useState(false);

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
        <div className="container mx-auto px-4 py-4 sm:py-8">
            <div className="mb-4 sm:mb-8">
                <h1 className="text-2xl sm:text-3xl font-bold">Economic Calendar</h1>
                <p className="text-sm text-muted-foreground">
                    Data by <a href="https://www.forexfactory.com" className="text-primary hover:underline" target="_blank"
                               rel="noopener noreferrer">ForexFactory</a>
                </p>
            </div>

            <Card className="mb-2 sm:mb-6">
                <CardHeader
                    className="p-4 sm:p-6 cursor-pointer sm:cursor-default"
                    onClick={() => setIsFilterExpanded(!isFilterExpanded)}
                >
                    <div className="flex justify-between items-center">
                        <CardTitle className="text-base sm:text-lg font-medium">Filters</CardTitle>
                        <Button variant="ghost" size="icon" className="sm:hidden">
                            <ChevronDown className={`h-4 w-4 transition-transform ${isFilterExpanded ? 'rotate-180' : ''}`}/>
                        </Button>
                    </div>
                </CardHeader>
                <div className={`${!isFilterExpanded ? 'hidden sm:block' : ''}`}>
                    <CardContent className="p-4 sm:p-6">
                        <div className="flex flex-col sm:flex-row gap-4 sm:gap-6">
                            <div className="flex-1">
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

                            <div className="flex-1">
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
                                </Select></div>

                            <div className="flex sm:flex-col justify-end h-[68px]">
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
                </div>

            </Card>

            {filteredData !== null && (
                <div className="">
                    <div className="overflow-auto space-y-4 sm:space-y-6 pr-2">
                        {Object.entries(groupedData).map(([date, items]) => (
                            <Card key={date}>
                                <CardHeader className="bg-muted/50 p-4">
                                    <CardTitle className="text-base sm:text-lg font-medium">{date} (UTC)</CardTitle>
                                </CardHeader>
                                <CardContent className="p-0">
                                    <div className="divide-y">
                                        {items.map((item, index) => {
                                            const isPastEvent = new Date(item.date * 1000) < new Date();
                                            const isNextEvent = nextEvent && nextEvent.date === item.date;
                                            return (
                                                <NewsItem
                                                    key={index}
                                                    item={item}
                                                    isNextEvent={isNextEvent}
                                                    isPastEvent={isPastEvent}
                                                />
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