"use client"

import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { useScenarioStore } from "@/store/useScenarioStore"
import { useToast } from "@/hooks/use-toast"
import { Send } from "lucide-react"

export function ScenarioTable() {
  const { scenarios, simulateDistribution } = useScenarioStore()
  const { toast } = useToast()

  const handleSendToApp = (scenarioId: string) => {
    simulateDistribution(scenarioId)
    toast({
      title: "Scenario Distributed",
      description: "Survey has been sent to assigned users.",
    })
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>Generated Scenarios</CardTitle>
      </CardHeader>
      <CardContent>
        {scenarios.length === 0 ? (
          <div className="py-8 text-center text-muted-foreground">
            No scenarios created yet. Use the form above to create one.
          </div>
        ) : (
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Name</TableHead>
                <TableHead>Demographic</TableHead>
                <TableHead>Distribution %</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Users Assigned</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {scenarios.map((scenario) => (
                <TableRow key={scenario.id}>
                  <TableCell className="font-medium">{scenario.name}</TableCell>
                  <TableCell>
                    {scenario.demographic.ageRange[0]}–{scenario.demographic.ageRange[1]} |{" "}
                    {scenario.demographic.location}
                  </TableCell>
                  <TableCell>{(scenario.percentage * 100).toFixed(0)}%</TableCell>
                  <TableCell>
                    <Badge variant={scenario.status === "sent" ? "default" : "secondary"}>{scenario.status}</Badge>
                  </TableCell>
                  <TableCell>{scenario.usersAssigned.length}</TableCell>
                  <TableCell className="text-right">
                    {scenario.status === "draft" && (
                      <Button size="sm" onClick={() => handleSendToApp(scenario.id)}>
                        <Send className="mr-2 h-4 w-4" />
                        Send to App
                      </Button>
                    )}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        )}
      </CardContent>
    </Card>
  )
}
