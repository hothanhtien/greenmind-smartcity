import type React from "react"
import { Sidebar } from "@/components/layout/Sidebar"
import ProtectedRoute from "@/components/ProtectedRoute"
import { DashboardHeader } from "@/components/dashboard-header"

export default function DashboardLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <ProtectedRoute>
      <div className="flex h-screen bg-background overflow-hidden">
        {/* Sidebar - Fixed position with backdrop */}
        <div className="relative">
          <Sidebar />
        </div>

        {/* Main content area */}
        <div className="flex flex-1 flex-col min-w-0 overflow-hidden">
          <DashboardHeader />
          <main className="flex-1 overflow-y-auto scroll-smooth">
            {children}
          </main>
        </div>
      </div>
    </ProtectedRoute>
  )
}
