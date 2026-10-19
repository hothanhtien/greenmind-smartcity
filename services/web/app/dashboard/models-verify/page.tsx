"use client"

import {
  Breadcrumb,
  BreadcrumbItem,
  BreadcrumbLink,
  BreadcrumbList,
  BreadcrumbPage,
  BreadcrumbSeparator,
} from "@/components/ui/breadcrumb"
import { ModelsTable } from "@/components/models/ModelsTable"

export default function ModelsVerifyPage() {
  return (
    // Note: Padding is adjusted as the new global header provides top space
    <div className="flex flex-1 flex-col gap-8 p-6 md:p-8"> 
      <header>
        <div>
          <Breadcrumb className="hidden md:flex">
            <BreadcrumbList>
              <BreadcrumbItem>
                <BreadcrumbLink asChild>
                  <a href="/dashboard">Dashboard</a>
                </BreadcrumbLink>
              </BreadcrumbItem>
              <BreadcrumbSeparator />
              <BreadcrumbItem>
                <BreadcrumbPage>Models Verification</BreadcrumbPage>
              </BreadcrumbItem>
            </BreadcrumbList>
          </Breadcrumb>
          <div className="mt-2">
            <h1 className="text-2xl font-bold tracking-tight sm:text-3xl">
              Models Verification
            </h1>
            <p className="mt-1 text-muted-foreground">
              View, create, and verify AI models with their feedback.
            </p>
          </div>
        </div>
      </header>
      <main>
        <ModelsTable />
      </main>
    </div>
  )
}
