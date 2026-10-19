"use client"

import { useState, useEffect, useRef } from "react"
import Link from "next/link"
import { useRouter } from "next/navigation"
import { Menu, Leaf, Bell, Search, X, ArrowRight } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Sheet, SheetContent, SheetTrigger } from "@/components/ui/sheet"
import { UserNav } from "@/components/layout/UserNav"
import { SidebarNav } from "@/components/layout/SidebarNav"
import { Input } from "@/components/ui/input"

const navItems = [
  { title: "Env. Impact", href: "/dashboard/environmental-impact" },
  { title: "Household", href: "/dashboard/household-management" },
  { title: "Users", href: "/dashboard/users-ocean" },
  { title: "Waste Report", href: "/dashboard/waste-report" },
  { title: "Campaigns", href: "/dashboard/campaign-management" },
  { title: "Community", href: "/dashboard/blogs" },
  { title: "Models", href: "/dashboard/tree" },
  { title: "Questions", href: "/dashboard/questions" },
  { title: "Surveys", href: "/dashboard/survey" },
  { title: "Results", href: "/dashboard/survey-results" },
  { title: "Verify", href: "/dashboard/models-verify" },
  { title: "Payment Analysis", href: "/dashboard/payment-analysis" },
]

export function DashboardHeader() {
  const router = useRouter()
  const [searchQuery, setSearchQuery] = useState("")
  const [searchResults, setSearchResults] = useState<typeof navItems>([])
  const [showResults, setShowResults] = useState(false)
  const searchRef = useRef<HTMLDivElement>(null)

  // Close dropdown when clicking outside
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (searchRef.current && !searchRef.current.contains(event.target as Node)) {
        setShowResults(false)
      }
    }
    document.addEventListener("mousedown", handleClickOutside)
    return () => document.removeEventListener("mousedown", handleClickOutside)
  }, [])

  const handleSearchChange = (value: string) => {
    setSearchQuery(value)

    if (!value.trim()) {
      setSearchResults([])
      setShowResults(false)
      return
    }

    setShowResults(true)
    const lowerQuery = value.toLowerCase()
    const filtered = navItems.filter((item) =>
      item.title.toLowerCase().includes(lowerQuery)
    )
    setSearchResults(filtered)
  }

  const handleResultClick = (href: string) => {
    router.push(href)
    setSearchQuery("")
    setSearchResults([])
    setShowResults(false)
  }

  const clearSearch = () => {
    setSearchQuery("")
    setSearchResults([])
    setShowResults(false)
  }

  return (
    <header className="sticky top-0 z-40 flex h-16 items-center justify-between bg-background/60 backdrop-blur-xl px-6 md:px-10 lg:px-14">
      <div className="flex items-center gap-6">
        {/* Mobile Navigation */}
        <Sheet>
          <SheetTrigger asChild>
            <Button
              variant="ghost"
              size="icon"
              className="lg:hidden h-9 w-9 hover:bg-muted/50 rounded-lg"
            >
              <Menu className="h-5 w-5" />
              <span className="sr-only">Toggle navigation</span>
            </Button>
          </SheetTrigger>
          <SheetContent side="left" className="flex flex-col p-0 w-[280px]">
            <div className="flex h-16 items-center border-b px-6">
              <Link href="/dashboard" className="flex items-center gap-3">
                <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-gradient-to-br from-emerald-500 to-teal-600 shadow-lg shadow-emerald-500/20">
                  <Leaf className="h-5 w-5 text-white" />
                </div>
                <span className="text-lg font-semibold tracking-tight">GreenMind</span>
              </Link>
            </div>
            <SidebarNav isMobile={true} />
          </SheetContent>
        </Sheet>

        {/* Search Bar */}
        <div className="hidden md:flex items-center w-64 lg:w-80" ref={searchRef}>
          <div className="relative w-full group">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground transition-colors group-hover:text-foreground" />
            <Input
              placeholder="Search pages..."
              value={searchQuery}
              onChange={(e) => handleSearchChange(e.target.value)}
              onFocus={() => searchQuery && setShowResults(true)}
              className="pl-10 pr-10 h-9 bg-muted/40 border-transparent focus:bg-muted/60 focus:border-border/50 rounded-lg text-sm transition-all"
            />
            {searchQuery && (
              <button
                onClick={clearSearch}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
              >
                <X className="h-4 w-4" />
              </button>
            )}
          </div>

          {/* Search Results Dropdown */}
          {showResults && (
            <div className="absolute top-full left-0 mt-2 w-full max-h-72 overflow-y-auto bg-white dark:bg-slate-900 rounded-xl border border-gray-200 dark:border-slate-700 shadow-xl z-50">
              {searchResults.length === 0 ? (
                <div className="p-4 text-center text-sm text-muted-foreground">
                  No pages found
                </div>
              ) : (
                <>
                  <div className="px-3 py-2 text-xs font-medium text-muted-foreground uppercase tracking-wider border-b">
                    {searchResults.length} page{searchResults.length !== 1 ? "s" : ""}
                  </div>
                  {searchResults.map((result) => (
                    <button
                      key={result.href}
                      onClick={() => handleResultClick(result.href)}
                      className="w-full flex items-center justify-between px-3 py-2.5 hover:bg-muted/50 transition-colors text-left"
                    >
                      <span className="text-sm font-medium">{result.title}</span>
                      <ArrowRight className="h-4 w-4 text-muted-foreground" />
                    </button>
                  ))}
                </>
              )}
            </div>
          )}
        </div>
      </div>

      {/* Right side */}
      <div className="flex items-center gap-2">
        {/* Notifications */}
        <Button
          variant="ghost"
          size="icon"
          className="relative h-9 w-9 hover:bg-muted/50 rounded-lg"
        >
          <Bell className="h-5 w-5" />
          <span className="absolute top-1.5 right-1.5 h-2 w-2 rounded-full bg-emerald-500" />
          <span className="sr-only">Notifications</span>
        </Button>

        <UserNav />
      </div>
    </header>
  )
}