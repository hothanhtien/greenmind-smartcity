"use client"

import Link from "next/link"
import { Leaf } from "lucide-react"
import { SidebarNav } from "./SidebarNav"

export function Sidebar() {
  return (
    <aside
      className={`
        hidden h-full shrink-0 flex-col border-r lg:flex
        w-14 hover:w-56
        transition-[width] duration-300 ease-out
        overflow-hidden
        group
        bg-gradient-to-b from-sidebar to-background
      `}
    >
      {/* Logo */}
      <div className="flex h-16 items-center border-b border-border/50 px-3 overflow-hidden">
        <Link href="/dashboard" className="flex items-center gap-3 min-w-0">
          <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-xl bg-gradient-to-br from-emerald-500 to-teal-600 shadow-lg shadow-emerald-500/20">
            <Leaf className="h-5 w-5 text-white" />
          </div>
          <span
            className="text-lg font-semibold tracking-tight whitespace-nowrap overflow-hidden
              opacity-0 group-hover:opacity-100
              transition-all duration-200 delay-100
              bg-gradient-to-r from-foreground to-foreground/70 bg-clip-text"
          >
            GreenMind
          </span>
        </Link>
      </div>

      <SidebarNav />
    </aside>
  )
}
