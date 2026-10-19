"use client"

import Link from "next/link"
import { usePathname } from "next/navigation"
import {
  LayoutDashboard,
  TreePine,
  FileText,
  Users,
  MessageSquare,
  Brain,
  CheckCircle,
  Leaf,
  MapPin,
  LogOut,
  User,
} from "lucide-react"

import { cn } from "@/lib/utils"
import { useAuth } from "@/contexts/AuthContext"

import { Button } from "@/components/ui/button"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { Avatar, AvatarFallback } from "@/components/ui/avatar"

const navItems = [
  { title: "Dashboard", href: "/dashboard", icon: LayoutDashboard },
  { title: "Models", href: "/dashboard/tree", icon: TreePine },
  { title: "Questions", href: "/dashboard/questions", icon: MessageSquare },
  { title: "Surveys", href: "/dashboard/survey", icon: FileText },
  { title: "Results", href: "/dashboard/survey-results", icon: Users },
  { title: "Verify", href: "/dashboard/models-verify", icon: CheckCircle },
  { title: "Waste Monitoring", href: "/dashboard/activity-monitoring", icon: MapPin },
  { title: "Users", href: "/dashboard/users-ocean", icon: Brain },
]

export function TopNavbar() {
  const pathname = usePathname()
  const { user, logout } = useAuth()

  return (
    <header className="border-b bg-background">
      <div className="mx-auto flex h-16 max-w-7xl items-center justify-between px-6">

        {/* LEFT */}
        <div className="flex items-center gap-8">

          {/* LOGO */}
          <Link href="/dashboard" className="flex items-center gap-2">
            <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-green-600">
              <Leaf className="h-5 w-5 text-white" />
            </div>
            <span className="text-lg font-semibold">GreenMind</span>
          </Link>

          {/* NAV */}
          <nav className="hidden md:flex items-center gap-2">
            {navItems.map((item) => {
              const isActive = pathname.startsWith(item.href)

              return (
                <Link
                  key={item.href}
                  href={item.href}
                  className={cn(
                    "flex items-center gap-2 rounded-md px-3 py-2 text-sm transition",
                    isActive
                      ? "bg-muted font-medium text-foreground"
                      : "text-muted-foreground hover:bg-muted hover:text-foreground"
                  )}
                >
                  <item.icon className="h-4 w-4" />
                  {item.title}
                </Link>
              )
            })}
          </nav>
        </div>

        {/* RIGHT */}
        <div className="flex items-center gap-4">

          {/* USER MENU */}
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <button className="flex items-center gap-2 rounded-full hover:bg-muted p-1">
                <Avatar className="h-8 w-8">
                  <AvatarFallback>
                    {user?.fullName?.[0] || "U"}
                  </AvatarFallback>
                </Avatar>
              </button>
            </DropdownMenuTrigger>

            <DropdownMenuContent align="end" className="w-48">
              <DropdownMenuLabel>
                {user?.email || "My Account"}
              </DropdownMenuLabel>

              <DropdownMenuSeparator />

              <DropdownMenuItem asChild>
                <Link href="/profile" className="flex items-center gap-2">
                  <User className="h-4 w-4" />
                  Profile
                </Link>
              </DropdownMenuItem>

              <DropdownMenuSeparator />

              <DropdownMenuItem
                onClick={logout}
                className="text-red-500 focus:text-red-500"
              >
                <LogOut className="mr-2 h-4 w-4" />
                Log out
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      </div>
    </header>
  )
}