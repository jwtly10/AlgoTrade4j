import {Card, CardContent, CardHeader, CardTitle} from "@/components/ui/card"
import {History} from "lucide-react"
import {formatDistanceToNow} from "date-fns"

export function RecentActivityCard({recentActivities}) {
    const formatTimestamp = (timestamp) => {
        const date = new Date(timestamp * 1000)
        return formatDistanceToNow(date, {addSuffix: true})
    }

    const scrollContainerHeight = (5 * 48) + (4 * 16)

    return (
        <Card>
            <CardHeader>
                <CardTitle>Recent Activity</CardTitle>
            </CardHeader>
            <CardContent>
                <div
                    className="overflow-y-auto pr-2"
                    style={{maxHeight: `${scrollContainerHeight}px`}}
                >
                    <ul className="space-y-4">
                        {recentActivities.map((activity, index) => (
                            <li key={index} className="flex items-start">
                                <History className="h-4 w-4 mr-2 mt-1 shrink-0"/>
                                <div className="flex flex-col">
                                    <span className="text-sm">{activity.description}</span>
                                    <span className="text-xs text-muted-foreground">
                                        {formatTimestamp(activity.timestamp)}
                                    </span>
                                </div>
                            </li>
                        ))}
                        {recentActivities.length === 0 && (
                            <li className="text-sm text-muted-foreground">
                                No recent activity
                            </li>
                        )}
                    </ul>
                </div>
            </CardContent>
        </Card>
    )
}