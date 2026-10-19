"use client"

import { ScenarioForm } from "@/components/scenario/ScenarioForm"
import { ScenarioTable } from "@/components/scenario/ScenarioTable"

export default function ScenarioPage() {
  return (
    <div className="space-y-6 p-6">
      <div>
        <h1 className="text-3xl font-bold">Scenario & Survey Manager</h1>
        <p className="text-muted-foreground">Create and distribute survey scenarios to target demographics</p>
      </div>
      <ScenarioForm />
      <ScenarioTable />
    </div>
  )
}
