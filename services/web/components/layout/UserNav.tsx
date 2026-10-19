"use client"

import Link from "next/link"
import { LogOut, User, Settings, ChevronDown } from "lucide-react"
import { useAuth } from "@/contexts/AuthContext"
import { Avatar, AvatarFallback } from "@/components/ui/avatar"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { Button } from "@/components/ui/button"
import { cn } from "@/lib/utils"

export function UserNav() {
  const { user, logout } = useAuth()

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button
          variant="ghost"
          className="relative h-auto p-1.5 md:p-2 hover:bg-muted/50 transition-colors rounded-xl"
        >
          <div className="flex items-center gap-2 md:gap-3">
            <Avatar className="h-8 w-8 md:h-9 md:w-9 ring-2 ring-primary/10 ring-offset-2 ring-offset-background transition-all hover:ring-primary/20">
              <AvatarFallback className="bg-gradient-to-br from-primary/20 to-emerald-500/20 text-primary font-medium">
                {user?.fullName?.[0] || "U"}
              </AvatarFallback>
            </Avatar>
            <div className="hidden lg:flex flex-col items-start">
              <p className="text-sm font-medium leading-none">
                {user?.fullName || "My Account"}
              </p>
              <p className="text-xs leading-none text-muted-foreground">
                {user?.email || ""}
              </p>
            </div>
            <ChevronDown className="hidden md:block h-3.5 w-3.5 text-muted-foreground" />
          </div>
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent
        align="end"
        className="w-64 p-2 bg-card/95 backdrop-blur-xl border-border shadow-xl shadow-black/5 rounded-xl"
      >
        {/* User info header */}
        <div className="flex items-center gap-3 p-3 mb-2 rounded-lg bg-muted/30">
          <Avatar className="h-10 w-10 ring-2 ring-primary/10">
            <AvatarFallback className="bg-gradient-to-br from-primary/20 to-emerald-500/20 text-primary font-medium">
              {user?.fullName?.[0] || "U"}
            </AvatarFallback>
          </Avatar>
          <div className="flex flex-col min-w-0">
            <p className="text-sm font-semibold truncate">
              {user?.fullName || "My Account"}
            </p>
            <p className="text-xs text-muted-foreground truncate">
              {user?.email || ""}
            </p>
          </div>
        </div>

        <DropdownMenuSeparator className="my-2" />

        <DropdownMenuItem
          asChild
          className="cursor-pointer rounded-lg focus:bg-muted/50"
        >
          <Link href="/dashboard/profile">
            <User className="mr-2 h-4 w-4 text-primary" />
            <span>Profile</span>
          </Link>
        </DropdownMenuItem>

        <DropdownMenuItem
          asChild
          className="cursor-pointer rounded-lg focus:bg-muted/50"
        >
          <Link href="/dashboard/profile">
            <Settings className="mr-2 h-4 w-4 text-muted-foreground" />
            <span>Settings</span>
          </Link>
        </DropdownMenuItem>

        <DropdownMenuSeparator className="my-2" />

        <DropdownMenuItem
          onClick={logout}
          className="cursor-pointer rounded-lg text-destructive/80 hover:text-destructive hover:bg-destructive/5 focus:bg-destructive/5"
        >
          <LogOut className="mr-2 h-4 w-4" />
          <span>Log out</span>
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  )
}
