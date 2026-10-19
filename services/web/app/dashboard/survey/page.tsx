"use client"

import { SurveyForm } from "@/components/survey/SurveyForm"
import { SurveyScenarioTable } from "@/components/survey/SurveyScenarioTable"
import { SurveySimulator } from "@/components/survey/SurveySimulator"
import { useState } from "react"

interface ScenarioData {
  id: string
  minAge: number
  maxAge: number
  percentage: number
  status: "draft" | "sent"
  location?: string
  gender?: string | null
  questions: Array<{
    id: string
    question: string
  }>
  simulatedSurvey?: any
}

export default function SurveyPage() {
  const [selectedScenarioId, setSelectedScenarioId] = useState<string | null>(null)
  const [selectedScenario, setSelectedScenario] = useState<ScenarioData | null>(null)
  const [refreshKey, setRefreshKey] = useState(0)

  const handleViewResult = (scenarioId: string, scenario: ScenarioData) => {
    setSelectedScenarioId(scenarioId)
    setSelectedScenario(scenario)
  }

  const handleScenarioDeleted = (deletedScenarioId: string) => {
    // Nếu scenario đang được xem bị xóa, reset về trạng thái ban đầu
    if (selectedScenarioId === deletedScenarioId) {
      setSelectedScenarioId(null)
      setSelectedScenario(null)
    }
  }

  const handleScenarioCreated = () => {
    setRefreshKey(prev => prev + 1)
  }

  return (
    <div className="min-h-screen bg-gray-50/30 p-8">
      <div className="mx-auto max-w-7xl space-y-8">
        <div className="space-y-2">
          <h1 className="text-3xl font-bold tracking-tight">Survey Manager</h1>
          <p className="text-base text-muted-foreground">
            Create demographic scenarios and simulate survey distribution
          </p>
        </div>

        <SurveyForm onScenarioCreated={handleScenarioCreated} />
        <SurveyScenarioTable
          key={refreshKey}
          onViewResult={handleViewResult}
          onScenarioDeleted={handleScenarioDeleted}
        />
        <SurveySimulator selectedScenarioId={selectedScenarioId} selectedScenario={selectedScenario} />
      </div>
    </div>
  )
}