"use client"

import { useEffect, useState } from "react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Badge } from "@/components/ui/badge"
import { CheckCircle2, Loader2, FileSearch, AlertCircle } from "lucide-react"
import { getSimulatedScenario } from "@/lib/survey"
import { useToast } from "@/hooks/use-toast"

interface SurveySimulatorProps {
  selectedScenarioId: string | null
  selectedScenario: {
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
  } | null
}

interface SimulatedUser {
  userId: string
  user: string
  gender?: string
  age: number
  location: string
  assignmentStatus: "assigned" | "not_assigned"
  surveyStatus: string
}

interface SimulatedData {
  scenarioId: string
  status: string
  totalEligible: number
  targetCount: number
  assignedCount: number
  notAssignedCount: number
  users: {
    assigned: SimulatedUser[]
    notAssigned: SimulatedUser[]
  }
}

export function SurveySimulator({ selectedScenarioId, selectedScenario }: SurveySimulatorProps) {
  const [simulatedData, setSimulatedData] = useState<SimulatedData | null>(null)
  const [loading, setLoading] = useState(false)
  const { toast } = useToast()

  useEffect(() => {
    if (selectedScenarioId) {
      fetchSimulatedData(selectedScenarioId)
    } else {
      setSimulatedData(null)
    }
  }, [selectedScenarioId])

  const fetchSimulatedData = async (scenarioId: string) => {
    setLoading(true)
    try {
      const response = await getSimulatedScenario(scenarioId)
      const data = response?.data

      if (!data) {
        setSimulatedData(null)
        return
      }

      // Normalize different possible response shapes from backend
      if (data.users && (data.users.assigned || data.users.notAssigned)) {
        // Already in expected shape
        setSimulatedData({
          scenarioId: data.scenarioId || data.id || "",
          status: data.status || data.simulation?.status || "",
          totalEligible: data.totalEligible ?? (data.users.assigned?.length ?? 0) + (data.users.notAssigned?.length ?? 0),
          targetCount: data.targetCount ?? data.targetCount ?? 0,
          assignedCount: data.assignedCount ?? (data.users.assigned?.length ?? 0),
          notAssignedCount: data.notAssignedCount ?? (data.users.notAssigned?.length ?? 0),
          users: {
            assigned: data.users.assigned ?? [],
            notAssigned: data.users.notAssigned ?? [],
          },
        })
      } else if (Array.isArray(data.eligibleUsers)) {
        // Newer shape: eligibleUsers with status field
        const assigned = data.eligibleUsers.filter((u: any) => u.status === 'assigned').map((u: any) => ({
          userId: u.userId || u.id,
          user: u.fullName || u.username || u.user || "",
          gender: u.gender,
          age: u.age,
          location: u.location || u.address || "",
          assignmentStatus: u.status,
          surveyStatus: u.surveyStatus || "",
        }))

        const notAssigned = data.eligibleUsers.filter((u: any) => u.status !== 'assigned').map((u: any) => ({
          userId: u.userId || u.id,
          user: u.fullName || u.username || u.user || "",
          gender: u.gender,
          age: u.age,
          location: u.location || u.address || "",
          assignmentStatus: u.status,
          surveyStatus: u.surveyStatus || "",
        }))

        setSimulatedData({
          scenarioId: data.scenarioId || "",
          status: data.simulation?.status || data.status || "",
          totalEligible: data.totalEligible ?? data.eligibleUsers.length,
          targetCount: data.targetCount ?? data.targetCount ?? 0,
          assignedCount: data.assigned ?? assigned.length,
          notAssignedCount: data.unassigned ?? notAssigned.length,
          users: { assigned, notAssigned },
        })
      } else {
        // Fallback: try to use data as-is
        setSimulatedData({
          scenarioId: data.scenarioId || data.id || "",
          status: data.status || data.simulation?.status || "",
          totalEligible: data.totalEligible ?? 0,
          targetCount: data.targetCount ?? 0,
          assignedCount: data.assigned ?? 0,
          notAssignedCount: data.unassigned ?? 0,
          users: { assigned: [], notAssigned: [] },
        })
      }
    } catch (error: any) {
      console.error("Error fetching simulated data:", error)
      const errorMessage = error?.message || "Failed to load simulation results"

      // Check if scenario hasn't been simulated yet
      if (errorMessage.includes("not found") || errorMessage.includes("No simulation")) {
        toast({
          title: "Not Simulated Yet",
          description: "This scenario hasn't been simulated. Please click 'Simulate' first.",
          variant: "destructive",
        })
      } else {
        toast({
          title: "Error",
          description: errorMessage,
          variant: "destructive",
        })
      }
      setSimulatedData(null)
    } finally {
      setLoading(false)
    }
  }

  if (!selectedScenarioId) {
    return (
      <Card className="shadow-sm transition-all duration-300 hover:shadow-md">
        <CardHeader>
          <CardTitle className="text-xl">Assignment Overview</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="rounded-lg border-2 border-dashed border-gray-200 bg-gradient-to-br from-gray-50 to-gray-100/50 py-16 text-center transition-all duration-300">
            <div className="flex flex-col items-center gap-3">
              <FileSearch className="h-12 w-12 text-gray-400 animate-pulse" />
              <p className="text-sm font-medium text-gray-600">
                No scenario selected
              </p>
              <p className="text-xs text-muted-foreground max-w-md">
                Click "View Result" on a simulated scenario to see assignment details
              </p>
            </div>
          </div>
        </CardContent>
      </Card>
    )
  }

  if (loading) {
    return (
      <Card className="shadow-sm transition-all duration-300">
        <CardHeader>
          <CardTitle className="text-xl">Assignment Overview</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="flex flex-col items-center justify-center gap-4 py-16">
            <Loader2 className="h-10 w-10 animate-spin text-primary" />
            <div className="text-center">
              <p className="text-sm font-medium text-gray-600">Loading simulation results...</p>
              <p className="text-xs text-muted-foreground mt-1">Please wait</p>
            </div>
          </div>
        </CardContent>
      </Card>
    )
  }

  if (!simulatedData) {
    return (
      <Card className="shadow-sm transition-all duration-300 hover:shadow-md">
        <CardHeader>
          <CardTitle className="text-xl">Assignment Overview</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="rounded-lg border-2 border-dashed border-amber-200 bg-gradient-to-br from-amber-50 to-orange-50/30 py-16 text-center transition-all duration-300">
            <div className="flex flex-col items-center gap-3">
              <AlertCircle className="h-12 w-12 text-amber-500 animate-bounce" />
              <p className="text-sm font-medium text-amber-700">
                Not simulated yet
              </p>
              <p className="text-xs text-amber-600/80 max-w-md">
                Click "Simulate" button first, then "View Result" to see assignment details
              </p>
            </div>
          </div>
        </CardContent>
      </Card>
    )
  }

  const allUsers = [...simulatedData.users.assigned, ...simulatedData.users.notAssigned]
  const assignmentRate = simulatedData.totalEligible > 0
    ? (simulatedData.assignedCount / simulatedData.totalEligible) * 100
    : 0

  return (
    <div className="space-y-6">
      <Card className="shadow-sm">
        <CardHeader className="space-y-3">
          <CardTitle className="text-xl">Assignment Overview</CardTitle>
          <div className="rounded-lg bg-blue-50 px-4 py-3 text-sm">
            <p className="font-medium text-blue-900">
              Scenario ID: <span className="font-bold">{simulatedData.scenarioId}</span> |
              Age Range: <span className="font-bold">{selectedScenario ? `${selectedScenario.minAge} - ${selectedScenario.maxAge}` : "N/A"}</span> |
              Location: <span className="font-bold">{selectedScenario?.location || "N/A"}</span> |
              Percentage: <span className="font-bold">{selectedScenario ? `${selectedScenario.percentage}%` : "N/A"}</span> |
              Status: <span className="font-bold">{simulatedData.status}</span> |
              Eligible Users: <span className="font-bold">{simulatedData.totalEligible}</span> |
              Assigned: <span className="font-bold">{simulatedData.assignedCount}</span> ({assignmentRate.toFixed(0)}%)
            </p>
          </div>
        </CardHeader>
        <CardContent>
          {simulatedData.totalEligible === 0 ? (
            <div className="rounded-lg border-2 border-dashed border-gray-200 bg-gray-50/50 py-12 text-center">
              <p className="text-sm text-muted-foreground">No eligible users found for this demographic criteria.</p>
            </div>
          ) : (
            <div className="grid gap-6 md:grid-cols-2">
              <div className="stat-card stat-card-primary">
                <div className="text-center">
                  <div className="stat-number text-7xl font-bold">{simulatedData.assignedCount}</div>
                  <p className="mt-3 text-base font-medium text-gray-600">Users Assigned</p>
                </div>
              </div>

              <div className="stat-card stat-card-secondary">
                <div className="text-center">
                  <div className="stat-number text-7xl font-bold">{simulatedData.notAssignedCount}</div>
                  <p className="mt-3 text-base font-medium text-gray-600">Not Assigned</p>
                </div>
              </div>
            </div>
          )}
        </CardContent>
      </Card>

      {allUsers.length > 0 && (
        <Card className="shadow-sm">
          <CardHeader>
            <CardTitle className="text-xl">User Assignment Details</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="overflow-hidden rounded-lg border">
              <Table>
                <TableHeader>
                  <TableRow className="bg-gray-50/50">
                    <TableHead className="font-semibold">User</TableHead>
                    <TableHead className="font-semibold">Gender</TableHead>
                    <TableHead className="font-semibold">Age</TableHead>
                    <TableHead className="font-semibold">Location</TableHead>
                    <TableHead className="font-semibold">Status</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {allUsers.map((user) => {
                    const isAssigned = user.assignmentStatus === "assigned"
                    const genderLabel = user.gender
                      ? `${user.gender.charAt(0).toUpperCase()}${user.gender.slice(1)}`
                      : "N/A"
                    return (
                      <TableRow key={user.userId} className="transition-colors hover:bg-gray-50/50">
                        <TableCell className="font-semibold">{user.user}</TableCell>
                        <TableCell className="font-medium">{genderLabel}</TableCell>
                        <TableCell className="font-medium text-muted-foreground">{user.age}</TableCell>
                        <TableCell className="font-medium">{user.location}</TableCell>
                        <TableCell>
                          <Badge variant={isAssigned ? "default" : "secondary"} className="font-medium shadow-sm">
                            {isAssigned ? "Assigned" : "Not Assigned"}
                          </Badge>
                        </TableCell>
                      </TableRow>
                    )
                  })}
                </TableBody>
              </Table>
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  )
}