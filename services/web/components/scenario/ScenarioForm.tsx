"use client"

import { useState } from "react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { useScenarioStore } from "@/store/useScenarioStore"
import { useToast } from "@/hooks/use-toast"
import { TRAITS, BEHAVIORS, DEMOGRAPHICS } from "@/components/tree/TreeToolbox"
import { Plus } from "lucide-react"

export function ScenarioForm() {
  const { generateScenario } = useScenarioStore()
  const { toast } = useToast()
  const [trait, setTrait] = useState("")
  const [behavior, setBehavior] = useState("")
  const [demographic, setDemographic] = useState("")
  const [percentage, setPercentage] = useState("50")

  const handleGenerate = () => {
    if (!trait || !behavior || !demographic) {
      toast({
        title: "Missing Fields",
        description: "Please fill in all fields.",
        variant: "destructive",
      })
      return
    }

    const demo = DEMOGRAPHICS.find((d) => d.id === demographic)
    if (!demo) return

    const percentValue = Number.parseInt(percentage, 10) / 100
    generateScenario(
      demo.ageRange[0],
      demo.ageRange[1],
      demo.location,
      percentValue,
      undefined,
      trait,
      behavior,
    )

    toast({
      title: "Scenario Created",
      description: "Your scenario has been generated successfully.",
    })

    // Reset form
    setTrait("")
    setBehavior("")
    setDemographic("")
    setPercentage("50")
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>Create New Scenario</CardTitle>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="grid gap-4 md:grid-cols-2">
          <div className="space-y-2">
            <Label htmlFor="trait">Trait</Label>
            <Select value={trait} onValueChange={setTrait}>
              <SelectTrigger id="trait">
                <SelectValue placeholder="Select trait" />
              </SelectTrigger>
              <SelectContent>
                {TRAITS.map((t) => (
                  <SelectItem key={t} value={t}>
                    {t}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          <div className="space-y-2">
            <Label htmlFor="behavior">Behavior</Label>
            <Select value={behavior} onValueChange={setBehavior}>
              <SelectTrigger id="behavior">
                <SelectValue placeholder="Select behavior" />
              </SelectTrigger>
              <SelectContent>
                {BEHAVIORS.map((b) => (
                  <SelectItem key={b} value={b}>
                    {b}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          <div className="space-y-2">
            <Label htmlFor="demographic">Demographic</Label>
            <Select value={demographic} onValueChange={setDemographic}>
              <SelectTrigger id="demographic">
                <SelectValue placeholder="Select demographic" />
              </SelectTrigger>
              <SelectContent>
                {DEMOGRAPHICS.map((d) => (
                  <SelectItem key={d.id} value={d.id}>
                    {d.ageRange[0]}–{d.ageRange[1]} | {d.location}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          <div className="space-y-2">
            <Label htmlFor="percentage">Distribution Percentage (%)</Label>
            <Input
              id="percentage"
              type="number"
              min="0"
              max="100"
              value={percentage}
              onChange={(e) => setPercentage(e.target.value)}
            />
          </div>
        </div>

        <Button onClick={handleGenerate} className="w-full">
          <Plus className="mr-2 h-4 w-4" />
          Generate Scenario
        </Button>
      </CardContent>
    </Card>
  )
}
