"use client"

import { useState, useEffect } from "react"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Button } from "@/components/ui/button"
import { Tooltip, TooltipTrigger, TooltipContent } from "@/components/ui/tooltip"
import { Badge } from "@/components/ui/badge"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { useToast } from "@/hooks/use-toast"
import { Send, Trash2, Eye, Download, ListChecks, RefreshCw } from "lucide-react"
import { QuestionModal } from "./QuestionModal"
import { getAllSurveyScenarios, simulateSurveyScenario, deleteSurveyScenario } from "@/lib/survey"

interface SurveyScenarioTableProps {
  onViewResult: (scenarioId: string, scenario: ScenarioFromAPI) => void
  onScenarioDeleted?: (deletedScenarioId: string) => void
}

interface ScenarioFromAPI {
  id: string
  minAge: number
  maxAge: number
  percentage: number
  status: "draft" | "sent"
  location?: string
  address?: string
  questionSetId?: string
  questionSet?: {
    id: string
    name: string
    description?: string
  }
  questions?: Array<{
    id: string
    question: string
  }>
  gender?: string | null
  simulatedSurvey?: any
  createdAt: string
  updatedAt: string
}

export function SurveyScenarioTable({ onViewResult, onScenarioDeleted }: SurveyScenarioTableProps) {
  const { toast } = useToast()
  const [modalOpen, setModalOpen] = useState(false)
  const [selectedScenarioId, setSelectedScenarioId] = useState<string>("")
  const [scenarios, setScenarios] = useState<ScenarioFromAPI[]>([])
  const [loading, setLoading] = useState(false)
  const [errorScenarios, setErrorScenarios] = useState<{ [key: string]: string }>({})

  useEffect(() => {
    fetchScenarios()
  }, [])

  const fetchScenarios = async () => {
    setLoading(true)
    try {
      const response = await getAllSurveyScenarios()
      const scenariosData = response?.data || []
      setScenarios(scenariosData)
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to load scenarios",
        variant: "destructive",
      })
    } finally {
      setLoading(false)
    }
  }

  const handleSimulate = async (scenarioId: string) => {
    const scenario = scenarios.find((s) => s.id === scenarioId)

    if (!scenario?.questionSet?.id) {
      toast({
        title: "No Question Set Selected",
        description: "Please select a question set before simulating.",
        variant: "destructive",
      })
      return
    }

    try {
      const response = await simulateSurveyScenario(scenarioId)

      await fetchScenarios()

      const updatedScenarios = await getAllSurveyScenarios()
      const updatedScenario = updatedScenarios?.data?.find((s: ScenarioFromAPI) => s.id === scenarioId)

      if (updatedScenario) {
        onViewResult(scenarioId, updatedScenario)
      }

      toast({
        title: "Survey Distributed",
        description: "Survey has been sent to assigned users. Check results below.",
      })
    } catch (error: any) {
      console.error("Simulate error:", error)

      // Extract error message from different error formats
      let errorMessage = "Failed to simulate scenario"

      if (error?.message) {
        errorMessage = error.message
      } else if (error?.response?.data?.message) {
        errorMessage = error.response.data.message
      }

      // Mark scenario as error and store error message
      setErrorScenarios(prev => ({
        ...prev,
        [scenarioId]: errorMessage
      }))

      toast({
        title: "Error",
        description: errorMessage,
        variant: "destructive",
      })
    }
  }

  const handleDelete = async (scenarioId: string) => {
    try {
      await deleteSurveyScenario(scenarioId)
      await fetchScenarios()

      // Clear error state for this scenario
      setErrorScenarios(prev => {
        const updated = { ...prev }
        delete updated[scenarioId]
        return updated
      })

      if (onScenarioDeleted) {
        onScenarioDeleted(scenarioId)
      }

      toast({
        title: "Scenario Deleted",
        description: "The scenario has been removed.",
      })
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to delete scenario",
        variant: "destructive",
      })
    }
  }

  const handleViewResult = (scenarioId: string) => {
    const scenario = scenarios.find((s) => s.id === scenarioId)
    if (!scenario) {
      toast({
        title: "Error",
        description: "Scenario not found",
        variant: "destructive",
      })
      return
    }

    if (scenario.status !== "sent") {
      toast({
        title: "Not Simulated",
        description: "Please simulate this scenario first",
        variant: "destructive",
      })
      return
    }

    onViewResult(scenarioId, scenario)
  }

  const handleSelectQuestions = (scenarioId: string) => {
    setSelectedScenarioId(scenarioId)
    setModalOpen(true)
  }

  const handleModalOpenChange = (open: boolean) => {
    setModalOpen(open)
    if (!open) {
      // Refresh scenarios when modal closes (after set is attached)
      fetchScenarios()
    }
  }

  const handleExport = () => {
    const dataStr = JSON.stringify(scenarios, null, 2)
    const dataBlob = new Blob([dataStr], { type: "application/json" })
    const url = URL.createObjectURL(dataBlob)
    const link = document.createElement("a")
    link.href = url
    link.download = `survey-scenarios-${new Date().toISOString()}.json`
    link.click()
    URL.revokeObjectURL(url)
  }

  const currentScenario = scenarios.find((s) => s.id === selectedScenarioId)

  return (
    <>
      <Card className="shadow-sm">
        <CardHeader>
          <div className="flex items-center justify-between">
            <CardTitle className="text-xl">Demographic Scenarios</CardTitle>
            <div className="flex gap-2">
              <Button onClick={fetchScenarios} variant="outline" size="sm" disabled={loading}>
                <RefreshCw className={`mr-2 h-4 w-4 ${loading ? "animate-spin" : ""}`} />
                Refresh
              </Button>
              {scenarios.length > 0 && (
                <Button onClick={handleExport} variant="outline" size="sm" className="shadow-sm">
                  <Download className="mr-2 h-4 w-4" />
                  Export JSON
                </Button>
              )}
            </div>
          </div>
        </CardHeader>
        <CardContent>
          {scenarios.length === 0 ? (
            <div className="rounded-lg border-2 border-dashed border-gray-200 bg-gray-50/50 py-12 text-center">
              <p className="text-sm text-muted-foreground">
                No scenarios created yet. Use the form above to create one.
              </p>
            </div>
          ) : (
            <div className="overflow-hidden rounded-lg border">
              <Table>
                <TableHeader>
                  <TableRow className="bg-gray-50/50">
                    <TableHead className="font-semibold">#</TableHead>
                    <TableHead className="font-semibold">Age Range</TableHead>
                    <TableHead className="font-semibold">Gender</TableHead>
                    <TableHead className="font-semibold">Location</TableHead>
                    <TableHead className="font-semibold">%</TableHead>
                    <TableHead className="font-semibold">Questions</TableHead>
                    <TableHead className="font-semibold">Status</TableHead>
                    <TableHead className="text-right font-semibold">Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {scenarios.map((scenario, index) => {
                    const hasError = errorScenarios[scenario.id]
                    return (
                      <TableRow
                        key={scenario.id}
                        className={`transition-colors ${hasError
                          ? "bg-red-50 hover:bg-red-100/80 border-l-4 border-l-red-500"
                          : "hover:bg-gray-50/50"
                          }`}
                      >
                        <TableCell className="font-medium text-muted-foreground">{index + 1}</TableCell>
                        <TableCell className="font-semibold">
                          {scenario.minAge}–{scenario.maxAge}
                        </TableCell>
                        <TableCell className="font-medium">
                          {scenario.gender
                            ? scenario.gender.charAt(0).toUpperCase() + scenario.gender.slice(1)
                            : "All"}
                        </TableCell>
                        <TableCell>
                          {scenario.address || scenario.location ? (
                            <Tooltip>
                              <TooltipTrigger asChild>
                                <div className="flex flex-wrap gap-1 max-w-xs cursor-help">
                                  {(() => {
                                    const locations = (Array.isArray(scenario.address) ? scenario.address : Array.isArray(scenario.location) ? scenario.location : []) as string[];
                                    return (
                                      <>
                                        {locations.slice(0, 2).map((addr: string) => (
                                          <Badge key={addr} variant="outline" className="text-xs font-medium">
                                            {addr.split(",")[0]}
                                          </Badge>
                                        ))}
                                        {locations.length > 2 && (
                                          <Badge variant="secondary" className="text-xs font-medium bg-blue-100 text-blue-700">
                                            +{locations.length - 2} more
                                          </Badge>
                                        )}
                                      </>
                                    );
                                  })()}
                                </div>
                              </TooltipTrigger>
                              <TooltipContent className="max-w-sm bg-slate-900 border border-slate-700 text-white px-3 py-2 rounded-lg shadow-lg">
                                <div className="space-y-2">
                                  <div className="text-xs font-semibold text-slate-300 tracking-wide">Full Locations Selected</div>
                                  <div className="border-t border-slate-600 pt-2 space-y-1">
                                    {(() => {
                                      const locations = Array.isArray(scenario.address) ? scenario.address : Array.isArray(scenario.location) ? scenario.location : [];
                                      return locations.map((addr: string) => (
                                        <div key={addr} className="text-xs text-slate-200 py-1">{addr}</div>
                                      ));
                                    })()}
                                  </div>
                                </div>
                              </TooltipContent>
                            </Tooltip>
                          ) : (
                            <span className="text-muted-foreground">N/A</span>
                          )}
                        </TableCell>
                        <TableCell className="font-semibold text-blue-600">
                          {scenario.percentage}%
                        </TableCell>
                        <TableCell>
                          <Badge variant="outline" className="font-medium max-w-xs truncate">
                            {scenario.questionSet?.name || "No Set"}
                          </Badge>
                        </TableCell>
                        <TableCell>
                          {hasError ? (
                            <div className="flex flex-col gap-1">
                              <Badge variant="destructive" className="font-medium shadow-sm">
                                Error
                              </Badge>
                              <span className="text-xs text-red-700 font-medium">{hasError}</span>
                            </div>
                          ) : (
                            <Badge
                              variant={scenario.status === "sent" ? "default" : "secondary"}
                              className="font-medium shadow-sm"
                            >
                              {scenario.status}
                            </Badge>
                          )}
                        </TableCell>
                        <TableCell className="text-right">
                          <div className="flex justify-end gap-2">
                            {hasError ? (
                              <>
                                <span className="text-xs text-red-600 font-semibold">Delete only</span>
                                <Button
                                  size="sm"
                                  variant="destructive"
                                  onClick={() => handleDelete(scenario.id)}
                                  className="shadow-sm"
                                >
                                  <Trash2 className="h-4 w-4" />
                                </Button>
                              </>
                            ) : (
                              <>
                                {scenario.status === "draft" && (
                                  <>
                                    <Button
                                      size="sm"
                                      variant="outline"
                                      onClick={() => handleSelectQuestions(scenario.id)}
                                      className="shadow-sm"
                                    >
                                      <ListChecks className="mr-2 h-4 w-4" />
                                      Select Set Questions
                                    </Button>
                                    {(!scenario.questionSet?.id) ? (
                                      <Tooltip>
                                        <TooltipTrigger asChild>
                                          <Button size="sm" disabled className="shadow-sm">
                                            <Send className="mr-2 h-4 w-4" />
                                            Simulate
                                          </Button>
                                        </TooltipTrigger>
                                        <TooltipContent>Please select at least one question first</TooltipContent>
                                      </Tooltip>
                                    ) : (
                                      <Button size="sm" onClick={() => handleSimulate(scenario.id)} className="shadow-sm">
                                        <Send className="mr-2 h-4 w-4" />
                                        Simulate
                                      </Button>
                                    )}
                                  </>
                                )}
                                {scenario.status === "sent" && (
                                  <Button
                                    size="sm"
                                    variant="outline"
                                    onClick={() => handleViewResult(scenario.id)}
                                    className="shadow-sm"
                                  >
                                    <Eye className="mr-2 h-4 w-4" />
                                    View Result
                                  </Button>
                                )}
                                <Button
                                  size="sm"
                                  variant="destructive"
                                  onClick={() => handleDelete(scenario.id)}
                                  className="shadow-sm"
                                >
                                  <Trash2 className="h-4 w-4" />
                                </Button>
                              </>
                            )}
                          </div>
                        </TableCell>
                      </TableRow>
                    )
                  })}
                </TableBody>
              </Table>
            </div>
          )}
        </CardContent>
      </Card>

      {currentScenario && (
        <QuestionModal
          open={modalOpen}
          onOpenChange={handleModalOpenChange}
          scenarioId={selectedScenarioId}
          onSuccess={fetchScenarios}
        />
      )}
    </>
  )
}