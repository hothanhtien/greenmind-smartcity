"use client"

import Link from "next/link"
import { usePathname } from "next/navigation"
import { useEffect, useMemo, useState } from "react"
import {
  LayoutDashboard,
  TreePine,
  FileText,
  Users,
  MessageSquare,
  Brain,
  CheckCircle,
  MapPin,
  Wind,
  BookOpen,
  ChevronDown,
  Flag,
  Leaf,
  CreditCard,
} from "lucide-react"
import { cn } from "@/lib/utils"
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from "@/components/ui/collapsible"

const navItems = [
  { title: "Env. Impact", href: "/dashboard/environmental-impact", icon: Wind },
  { title: "Household", href: "/dashboard/household-management", icon: MapPin },
  { title: "Users", href: "/dashboard/users-ocean", icon: Brain },
  { title: "Waste Report", href: "/dashboard/waste-report", icon: MapPin },
  { title: "Campaigns", href: "/dashboard/campaign-management", icon: Flag },
  { title: "Community", href: "/dashboard/blogs", icon: BookOpen },
  { title: "Payments", href: "/dashboard/payment-analysis", icon: CreditCard },
]

const modelSurveyItems = [
  { title: "Models", href: "/dashboard/tree", icon: TreePine },
  { title: "Questions", href: "/dashboard/questions", icon: MessageSquare },
  { title: "Surveys", href: "/dashboard/survey", icon: FileText },
  { title: "Results", href: "/dashboard/survey-results", icon: Users },
  { title: "Verify", href: "/dashboard/models-verify", icon: CheckCircle },
]

interface SidebarNavProps {
  isMobile?: boolean
}

export function SidebarNav({ isMobile = false }: SidebarNavProps) {
  const pathname = usePathname()

  const isModelSurveyActive = useMemo(
    () => modelSurveyItems.some((item) => pathname.startsWith(item.href)),
    [pathname]
  )

  const [isModelSurveyOpen, setIsModelSurveyOpen] = useState(isModelSurveyActive)

  useEffect(() => {
    if (isModelSurveyActive) setIsModelSurveyOpen(true)
  }, [isModelSurveyActive])

  return (
    <nav className="flex-1 space-y-1 p-2">

      <Collapsible open={isModelSurveyOpen} onOpenChange={setIsModelSurveyOpen}>
        <div className="space-y-1">
          <CollapsibleTrigger asChild>
            <button
              type="button"
              className={cn(
                "flex w-full items-center gap-3 px-3 py-2.5 rounded-xl text-sm transition-all duration-200 min-w-0",
                isModelSurveyActive
                  ? "bg-gradient-to-r from-emerald-500/15 to-transparent text-emerald-700 dark:text-emerald-400 font-medium"
                  : "text-muted-foreground hover:bg-muted/50 hover:text-foreground"
              )}
            >
              <Leaf className={cn("h-5 w-5 shrink-0", isModelSurveyActive ? "text-emerald-600" : "")} />
              <span
                className={cn(
                  "flex-1 truncate text-left",
                  "opacity-0 group-hover:opacity-100",
                  "transition-opacity duration-150",
                  isMobile && "opacity-100"
                )}
              >
                Models / Survey
              </span>
              <ChevronDown
                className={cn(
                  "h-4 w-4 shrink-0 transition-transform duration-200",
                  isModelSurveyOpen ? "rotate-180" : "",
                  "opacity-0 group-hover:opacity-100",
                  isMobile && "opacity-100"
                )}
              />
            </button>
          </CollapsibleTrigger>

          <CollapsibleContent
            className={cn("space-y-1 pl-4", !isMobile && "hidden group-hover:block")}
          >
            <div className="my-2 h-px bg-gradient-to-r from-emerald-500/20 to-transparent" />
            {modelSurveyItems.map((item) => {
              const isActive = pathname.startsWith(item.href)
              return (
                <Link
                  key={item.href}
                  href={item.href}
                  className={cn(
                    "flex items-center gap-3 px-3 py-2 rounded-xl text-sm transition-all duration-200",
                    isActive
                      ? "bg-emerald-500/10 text-emerald-700 dark:text-emerald-400 font-medium"
                      : "text-muted-foreground/80 hover:bg-muted/30 hover:text-foreground"
                  )}
                >
                  <item.icon className="h-4 w-4 shrink-0" />
                  <span
                    className={cn(
                      "truncate",
                      "opacity-0 group-hover:opacity-100",
                      "transition-opacity duration-150",
                      isMobile && "opacity-100"
                    )}
                  >
                    {item.title}
                  </span>
                </Link>
              )
            })}
          </CollapsibleContent>
        </div>
      </Collapsible>

      {/* Divider */}
      <div className="my-3 h-px bg-gradient-to-r from-transparent via-border to-transparent" />

      {/* Other Nav Items */}
      {navItems.map((item) => {
        const isActive = pathname.startsWith(item.href)
        return (
          <Link
            key={item.href}
            href={item.href}
            className={cn(
              "flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm transition-all duration-200 min-w-0",
              isActive
                ? "bg-gradient-to-r from-emerald-500/15 to-transparent text-emerald-700 dark:text-emerald-400 font-medium"
                : "text-muted-foreground hover:bg-muted/50 hover:text-foreground"
            )}
          >
            <item.icon className={cn("h-5 w-5 shrink-0", isActive ? "text-emerald-600" : "")} />
            <span
              className={cn(
                "truncate",
                "opacity-0 group-hover:opacity-100",
                "transition-opacity duration-150",
                isMobile && "opacity-100"
              )}
            >
              {item.title}
            </span>
          </Link>
        )
      })}
    </nav>
  )
}
