"use client"

import { useState, useEffect } from "react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Badge } from "@/components/ui/badge"
import { Progress } from "@/components/ui/progress"
import { getAllSurveyScenarios, getSimulatedScenario } from "@/lib/survey"
import { useToast } from "@/hooks/use-toast"
import { Loader2, CheckCircle2, Clock, Users, Target, TrendingUp } from "lucide-react"

interface SurveyScenario {
  id: string
  minAge: number
  maxAge: number
  percentage: number
  status: "draft" | "sent"
  address?: string[] | string
  createdAt: string
}

interface SimulatedUser {
  userId: string
  fullName: string
  username: string
  age: number
  gender: string
  location: string
  status: "assigned" | "not_assigned"
}

export default function SurveyResultsPage() {
  const { toast } = useToast()
  const [scenarios, setScenarios] = useState<SurveyScenario[]>([])
  const [selectedScenarioId, setSelectedScenarioId] = useState<string>("")
  const [simulatedUsers, setSimulatedUsers] = useState<SimulatedUser[]>([])
  const [loading, setLoading] = useState(true)
  const [scenarioLoading, setScenarioLoading] = useState(false)

  useEffect(() => {
    fetchScenarios()
  }, [])

  const fetchScenarios = async () => {
    setLoading(true)
    try {
      const response = await getAllSurveyScenarios()
      const scenariosData = response?.data || []
      setScenarios(scenariosData)
      if (scenariosData.length > 0) {
        setSelectedScenarioId(scenariosData[0].id)
        fetchScenarioResults(scenariosData[0].id)
      }
    } catch (error) {
      console.error("Error fetching scenarios:", error)
      toast({
        title: "Error",
        description: "Failed to load survey scenarios",
        variant: "destructive",
      })
    } finally {
      setLoading(false)
    }
  }

  const fetchScenarioResults = async (scenarioId: string) => {
    setScenarioLoading(true)
    try {
      const response = await getSimulatedScenario(scenarioId)
      const data = response?.data

      if (data?.eligibleUsers && Array.isArray(data.eligibleUsers)) {
        const users = data.eligibleUsers.map((u: any) => ({
          userId: u.userId || u.id,
          fullName: u.fullName || u.username,
          username: u.username,
          age: u.age,
          gender: u.gender,
          location: u.location || u.address,
          status: u.status || "not_assigned",
        }))
        setSimulatedUsers(users)
      } else {
        setSimulatedUsers([])
      }
    } catch (error) {
      console.error("Error fetching scenario results:", error)
      toast({
        title: "Error",
        description: "Failed to load survey results",
        variant: "destructive",
      })
      setSimulatedUsers([])
    } finally {
      setScenarioLoading(false)
    }
  }

  const handleScenarioChange = (id: string) => {
    setSelectedScenarioId(id)
    fetchScenarioResults(id)
  }

  const assignedUsers = simulatedUsers.filter(u => u.status === "assigned")
  const completionRate = simulatedUsers.length > 0
    ? Math.round((assignedUsers.length / simulatedUsers.length) * 100)
    : 0

  return (
    <div className="min-h-screen bg-gray-50/30 p-8">
      <div className="mx-auto max-w-7xl space-y-8">
        <div className="space-y-2">
          <h1 className="text-3xl font-bold tracking-tight">Survey Results</h1>
          <p className="text-base text-muted-foreground">
            View survey completion status and user responses
          </p>
        </div>

        {/* Scenario Selector */}
        <Card className="shadow-sm border-0">
          <CardHeader className="border-b bg-gradient-to-r from-gray-50 to-gray-100/50">
            <div className="flex items-center justify-between">
              <div>
                <CardTitle className="text-lg">Select Survey Scenario</CardTitle>
                <p className="text-xs text-muted-foreground mt-1">Choose a scenario to view results</p>
              </div>
              {selectedScenarioId && (
                <Select value={selectedScenarioId} onValueChange={handleScenarioChange}>
                  <SelectTrigger className="w-[320px] shadow-sm">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {scenarios.map((scenario) => (
                      <SelectItem key={scenario.id} value={scenario.id}>
                        <span className="flex items-center gap-2">
                          <Target className="h-4 w-4" />
                          Age {scenario.minAge}-{scenario.maxAge} | {scenario.percentage}% |
                          <Badge variant={scenario.status === "sent" ? "default" : "secondary"} className="text-xs">
                            {scenario.status}
                          </Badge>
                        </span>
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              )}
            </div>
          </CardHeader>
        </Card>

        {/* Analytics Cards */}
        {!scenarioLoading && simulatedUsers.length > 0 && (
          <div className="grid gap-6 md:grid-cols-3">
            <Card className="shadow-sm border-0 bg-gradient-to-br from-blue-50 to-blue-100/50">
              <CardHeader className="pb-3">
                <div className="flex items-center justify-between">
                  <CardTitle className="text-sm font-medium text-gray-700">Total Eligible</CardTitle>
                  <Users className="h-5 w-5 text-blue-600" />
                </div>
              </CardHeader>
              <CardContent>
                <div className="text-4xl font-bold text-blue-600">{simulatedUsers.length}</div>
                <p className="text-xs text-gray-600 mt-1">users matching criteria</p>
              </CardContent>
            </Card>

            <Card className="shadow-sm border-0 bg-gradient-to-br from-green-50 to-green-100/50">
              <CardHeader className="pb-3">
                <div className="flex items-center justify-between">
                  <CardTitle className="text-sm font-medium text-gray-700">Assigned</CardTitle>
                  <CheckCircle2 className="h-5 w-5 text-green-600" />
                </div>
              </CardHeader>
              <CardContent>
                <div className="text-4xl font-bold text-green-600">{assignedUsers.length}</div>
                <p className="text-xs text-gray-600 mt-1">completed survey</p>
              </CardContent>
            </Card>

            <Card className="shadow-sm border-0 bg-gradient-to-br from-purple-50 to-purple-100/50">
              <CardHeader className="pb-3">
                <div className="flex items-center justify-between">
                  <CardTitle className="text-sm font-medium text-gray-700">Completion Rate</CardTitle>
                  <TrendingUp className="h-5 w-5 text-purple-600" />
                </div>
              </CardHeader>
              <CardContent>
                <div className="text-4xl font-bold text-purple-600">{completionRate}%</div>
                <Progress value={completionRate} className="mt-3 h-2" />
              </CardContent>
            </Card>
          </div>
        )}

        {/* Users Results Table */}
        <Card className="shadow-sm">
          <CardHeader className="border-b bg-gray-50/50">
            <CardTitle className="text-xl">Survey Participants</CardTitle>
          </CardHeader>
          <CardContent className="pt-6">
            {scenarioLoading ? (
              <div className="flex items-center justify-center py-12">
                <Loader2 className="h-6 w-6 animate-spin text-muted-foreground" />
              </div>
            ) : simulatedUsers.length === 0 ? (
              <div className="rounded-lg border-2 border-dashed border-gray-200 bg-gray-50/50 py-12 text-center">
                <p className="text-sm text-muted-foreground">No survey data available</p>
              </div>
            ) : (
              <div className="overflow-hidden rounded-lg border">
                <Table>
                  <TableHeader>
                    <TableRow className="bg-gray-50/50">
                      <TableHead className="font-semibold">Name</TableHead>
                      <TableHead className="font-semibold">Age</TableHead>
                      <TableHead className="font-semibold">Gender</TableHead>
                      <TableHead className="font-semibold">Location</TableHead>
                      <TableHead className="font-semibold">Status</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {simulatedUsers.map((user) => (
                      <TableRow key={user.userId} className="hover:bg-gray-50/50">
                        <TableCell className="font-medium">{user.fullName}</TableCell>
                        <TableCell>{user.age}</TableCell>
                        <TableCell>
                          <Badge variant="outline" className="font-medium">
                            {user.gender.charAt(0).toUpperCase() + user.gender.slice(1)}
                          </Badge>
                        </TableCell>
                        <TableCell>{user.location}</TableCell>
                        <TableCell>
                          <Badge
                            variant={user.status === "assigned" ? "default" : "secondary"}
                            className="font-medium"
                          >
                            {user.status === "assigned" ? (
                              <CheckCircle2 className="mr-1 h-4 w-4" />
                            ) : (
                              <Clock className="mr-1 h-4 w-4" />
                            )}
                            {user.status === "assigned" ? "Assigned" : "Not Assigned"}
                          </Badge>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
