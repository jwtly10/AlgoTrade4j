import React, {useEffect, useState} from 'react';
import {liveNewsClient} from "@/api/liveClient.js";
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow,} from "@/components/ui/table";
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue,} from "@/components/ui/select";
import {Folder, Info} from 'lucide-react';
import {useToast} from "@/hooks/use-toast.js";
import {Button} from "@/components/ui/button.jsx";

const impactIcons = {
    High: <Folder className="text-red-500 fill-red-500 size-5" title="High Impact"/>,
    Medium: <Folder className="text-orange-500 fill-orange-500 size-5" title="Medium Impact"/>,
    Low: <Folder className="text-yellow-500 fill-yellow-500 size-5" title="Low Impact"/>,
    Holiday: <Folder className="text-gray-500 fill-gray-500 size-5" title="Holiday"/>,
};

const countryIcons = {
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
        <div className="container mx-auto p-6">
            <h1 className="text-2xl font-semibold">Forex News Schedule</h1>

            <div className="flex flex-col gap-4 mb-6 p-6 rounded-lg shadow-md">
                <div className="flex flex-wrap items-center gap-4">
                    <div className="flex items-center space-x-2 w-60">
                        <label className="text-sm text-gray-600">Country:</label>
                        <Select
                            value={countryFilter || null}
                            onValueChange={(value) => setCountryFilter(value || '')}
                            className="w-full"
                        >
                            <SelectTrigger className="border border-gray-300 text-sm shadow-sm">
                                <SelectValue placeholder="Select Country"/>
                            </SelectTrigger>
                            <SelectContent>
                                <SelectItem value={null}>All Countries</SelectItem>
                                {Object.entries(countryIcons).map(([key, emoji]) => (
                                    <SelectItem key={key} value={key}>
                                        {emoji} {key}
                                    </SelectItem>
                                ))}
                            </SelectContent>
                        </Select>
                    </div>

                    <div className="flex items-center space-x-2 w-60">
                        <label className="text-sm text-gray-600">Impact:</label>
                        <Select
                            value={impactFilter || null}
                            onValueChange={(value) => setImpactFilter(value || '')}
                            className="w-full"
                        >
                            <SelectTrigger className="border border-gray-300 text-sm shadow-sm">
                                <SelectValue placeholder="Select Impact Level"/>
                            </SelectTrigger>
                            <SelectContent>
                                <SelectItem value={null}>All Impact Levels</SelectItem>
                                {Object.entries(impactIcons).map(([key, icon]) => (
                                    <SelectItem key={key} value={key}>
                                        <div className="flex items-center space-x-2">
                                            <span>{icon}</span>
                                            <span>{key}</span>
                                        </div>
                                    </SelectItem>
                                ))}
                            </SelectContent>
                        </Select>
                    </div>

                    <div className="ml-auto flex flex-col">
                        <Button
                            variant="default"
                            size="sm"
                            onClick={() => {
                                setCountryFilter('');
                                setImpactFilter('');
                                localStorage.removeItem('countryFilter');
                                localStorage.removeItem('impactFilter');
                            }}
                            className="ml-auto"
                        >
                            Clear Filters
                        </Button>
                        <p className="text-sm text-gray-600 mt-4">
                            Data provided by <a href="https://www.forexfactory.com" className="text-blue-500 underline" target="_blank" rel="noopener noreferrer">ForexFactory.com</a>
                        </p>
                    </div>
                </div>
            </div>

            {filteredData !== null && (
                <div className="rounded-md border overflow-auto h-[calc(100vh-400px)]">
                    <Table className="w-full">
                        <TableHeader>
                            <TableRow>
                                <TableHead>Time</TableHead>
                                <TableHead>Country</TableHead>
                                <TableHead>Impact</TableHead>
                                <TableHead>Title</TableHead>
                                <TableHead>Forecast</TableHead>
                                <TableHead>Previous</TableHead>
                            </TableRow>
                        </TableHeader>
                        <TableBody>
                            {Object.entries(groupedData).map(([date, items]) => (
                                <React.Fragment key={date}>
                                    <TableRow className="bg-gray-200">
                                        <TableCell colSpan={6} className="font-semibold">{date + " (UTC)"}</TableCell>
                                    </TableRow>
                                    {items.map((item, index) => (
                                        <TableRow key={index} className="hover:bg-gray-100">
                                            <TableCell>
                                                {new Date(item.date * 1000).toLocaleString('en-GB', {
                                                    weekday: 'short',
                                                    day: 'numeric',
                                                    month: 'short',
                                                    year: 'numeric',
                                                    hour: 'numeric',
                                                    minute: '2-digit',
                                                    hour12: true,
                                                    timeZone: 'UTC',
                                                }).replace('GMT', 'UTC')}
                                            </TableCell>
                                            <TableCell>
                                                <div className="flex items-center space-x-2">
                                                    <span>{countryIcons[item.country] || '🌐'}</span>
                                                    <span>{item.country}</span>
                                                </div>
                                            </TableCell>
                                            <TableCell>
                                                <div className="flex items-center space-x-2">
                                                    {impactIcons[item.impact] || <Info className="text-gray-500 size-5" title={item.impact}/>}
                                                    <span>{item.impact}</span>
                                                </div>
                                            </TableCell>
                                            <TableCell>{item.title}</TableCell>
                                            <TableCell>{item.forecast || '-'}</TableCell>
                                            <TableCell>{item.previous || '-'}</TableCell>
                                        </TableRow>
                                    ))}
                                </React.Fragment>
                            ))}
                        </TableBody>
                    </Table>
                </div>
            )}
        </div>
    );
};

export default NewsView;