import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { cn } from "@/lib/utils"
import {
  UserPlus,
  Settings,
  Trash2,
  AlertTriangle,
  FileDown,
  MoreHorizontal,
} from "lucide-react"

const activities = [
  {
    id: 1,
    user: "Sarah Chen",
    action: "Created new project",
    time: "2 minutes ago",
    status: "success",
    icon: UserPlus,
  },
  {
    id: 2,
    user: "Mike Johnson",
    action: "Updated user settings",
    time: "15 minutes ago",
    status: "info",
    icon: Settings,
  },
  {
    id: 3,
    user: "Emily Davis",
    action: "Deleted old records",
    time: "1 hour ago",
    status: "warning",
    icon: Trash2,
  },
  {
    id: 4,
    user: "Tom Wilson",
    action: "Failed login attempt",
    time: "2 hours ago",
    status: "error",
    icon: AlertTriangle,
  },
  {
    id: 5,
    user: "Lisa Anderson",
    action: "Exported data report",
    time: "3 hours ago",
    status: "success",
    icon: FileDown,
  },
]

const statusConfig = {
  success: {
    bg: "bg-emerald-50 dark:bg-emerald-950/40",
    text: "text-emerald-700 dark:text-emerald-400",
    border: "border-emerald-200/50 dark:border-emerald-800/40",
    dot: "bg-emerald-500",
    label: "Success",
  },
  info: {
    bg: "bg-blue-50 dark:bg-blue-950/40",
    text: "text-blue-700 dark:text-blue-400",
    border: "border-blue-200/50 dark:border-blue-800/40",
    dot: "bg-blue-500",
    label: "Info",
  },
  warning: {
    bg: "bg-amber-50 dark:bg-amber-950/40",
    text: "text-amber-700 dark:text-amber-400",
    border: "border-amber-200/50 dark:border-amber-800/40",
    dot: "bg-amber-500",
    label: "Warning",
  },
  error: {
    bg: "bg-rose-50 dark:bg-rose-950/40",
    text: "text-rose-700 dark:text-rose-400",
    border: "border-rose-200/50 dark:border-rose-800/40",
    dot: "bg-rose-500",
    label: "Error",
  },
}

export function RecentActivity() {
  return (
    <Card className="card-float animate-fade-up animate-fade-up-delay-4">
      <CardHeader className="pb-4">
        <div className="flex items-center justify-between">
          <div>
            <CardTitle className="text-lg md:text-xl font-semibold tracking-tight">
              Recent Activity
            </CardTitle>
            <p className="text-sm text-muted-foreground mt-1">
              Latest actions from your team
            </p>
          </div>
          <button className="h-8 w-8 flex items-center justify-center rounded-lg hover:bg-muted/50 transition-colors">
            <MoreHorizontal className="h-4 w-4 text-muted-foreground" />
          </button>
        </div>
      </CardHeader>
      <CardContent className="pt-0">
        <div className="space-y-1">
          {activities.map((activity, index) => {
            const config = statusConfig[activity.status as keyof typeof statusConfig]
            const Icon = activity.icon

            return (
              <div
                key={activity.id}
                className={cn(
                  "flex items-center gap-4 p-3 md:p-4 rounded-xl transition-all duration-300",
                  "hover:bg-muted/30 group",
                  "animate-fade-up",
                  index === 0 && "animate-fade-up-delay-1",
                  index === 1 && "animate-fade-up-delay-2",
                  index === 2 && "animate-fade-up-delay-3",
                  index === 3 && "animate-fade-up-delay-4",
                  index === 4 && "animate-fade-up-delay-5"
                )}
              >
                {/* Icon */}
                <div
                  className={cn(
                    "flex h-10 w-10 shrink-0 items-center justify-center rounded-xl transition-colors",
                    config.bg,
                    `border ${config.border}`
                  )}
                >
                  <Icon className={cn("h-4.5 w-4.5", config.text)} />
                </div>

                {/* Content */}
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium text-foreground truncate">
                    {activity.user}
                  </p>
                  <p className="text-sm text-muted-foreground truncate">
                    {activity.action}
                  </p>
                </div>

                {/* Meta */}
                <div className="flex items-center gap-3 shrink-0">
                  <span className="text-xs text-muted-foreground whitespace-nowrap hidden sm:block">
                    {activity.time}
                  </span>
                  <Badge
                    variant="outline"
                    className={cn(
                      "text-[10px] md:text-xs px-2 py-0.5 font-medium",
                      config.bg,
                      config.text,
                      config.border
                    )}
                  >
                    <span className={cn("h-1.5 w-1.5 rounded-full mr-1.5", config.dot)} />
                    {config.label}
                  </Badge>
                </div>
              </div>
            )
          })}
        </div>
      </CardContent>
    </Card>
  )
}
