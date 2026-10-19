"use client"

import { useState } from "react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Checkbox } from "@/components/ui/checkbox"
import { useToast } from "@/hooks/use-toast"
import { Plus } from "lucide-react"
import { createSurveyScenario } from "@/lib/survey"

interface SurveyFormProps {
  onScenarioCreated?: () => void
}

export function SurveyForm({ onScenarioCreated }: SurveyFormProps) {
  const { toast } = useToast()

  const [minAge, setMinAge] = useState("")
  const [maxAge, setMaxAge] = useState("")
  const [percentage, setPercentage] = useState("")

  const locations = ["Da Nang", "Quang Ngai", "Quang Nam"]
  const [selectedAddresses, setSelectedAddresses] = useState<string[]>([])
  const [selectedGender, setSelectedGender] = useState("")
  const [searchLocation, setSearchLocation] = useState("")
  const [submitting, setSubmitting] = useState(false)

  const handleGenerate = async () => {
    const min = Number.parseInt(minAge, 10);
    const max = Number.parseInt(maxAge, 10);
    const pct = Number.parseInt(percentage, 10);
    if (selectedAddresses.length === 0) {
      toast({
        title: "Missing Location",
        description: "Please select at least one location.",
        variant: "destructive",
      })
      return
    }

    if (Number.isNaN(min) || Number.isNaN(max)) {
      toast({ title: "Invalid Age", description: "Ages must be numbers.", variant: "destructive" })
      return
    }

    if (min < 10 || max > 100) {
      toast({
        title: "Invalid Age Range",
        description: "Age must be between 10 and 100.",
        variant: "destructive",
      })
      return
    }

    if (min >= max) {
      toast({
        title: "Invalid Age Range",
        description: "Minimum age must be less than maximum age.",
        variant: "destructive",
      })
      return
    }

    if (Number.isNaN(pct) || pct < 1 || pct > 100) {
      toast({
        title: "Invalid Percentage",
        description: "Percentage must be between 1 and 100.",
        variant: "destructive",
      })
      return
    }

    try {
      setSubmitting(true)
      const payload = { minAge: min, maxAge: max, address: selectedAddresses, percentage: pct, gender: selectedGender || "all" }

      console.log("📤 Sending payload:", payload)
      const created = await createSurveyScenario(payload);
      console.log("📥 Response:", created)
      toast({ title: "Scenario Created", description: "Scenario created successfully." })
      setMinAge("")
      setMaxAge("")
      setPercentage("")
      setSelectedAddresses([])
      setSelectedGender("")

      if (onScenarioCreated) {
        onScenarioCreated()
      }
    } catch (e) {
      toast({
        title: "Error",
        description: "Failed to create scenario. Please try again.",
        variant: "destructive",
      })
      console.error("Failed to generate scenario:", e)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <Card className="shadow-sm">
      <CardHeader className="space-y-1">
        <CardTitle className="text-xl">Create Demographic Scenario</CardTitle>
      </CardHeader>
      <CardContent className="space-y-6">
        <div className="grid gap-6 md:grid-cols-2">
          <div className="space-y-2">
            <Label htmlFor="minAge" className="text-sm font-medium">Min Age</Label>
            <Input
              id="minAge"
              type="number"
              min="10"
              max="100"
              value={minAge}
              onChange={(e) => setMinAge(e.target.value)}
              placeholder="Enter minimum age"
              className="h-10"
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="maxAge" className="text-sm font-medium">Max Age</Label>
            <Input
              id="maxAge"
              type="number"
              min="10"
              max="100"
              value={maxAge}
              onChange={(e) => setMaxAge(e.target.value)}
              placeholder="Enter maximum age"
              className="h-10"
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="gender" className="text-sm font-medium">Gender</Label>
            <Select
              value={selectedGender}
              onValueChange={setSelectedGender}
            >
              <SelectTrigger className="h-10 w-full">
                <SelectValue placeholder="Select gender" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">All</SelectItem>
                <SelectItem value="male">Male</SelectItem>
                <SelectItem value="female">Female</SelectItem>
              </SelectContent>
            </Select>
          </div>

          <div className="space-y-2">
            <Label htmlFor="percentage" className="text-sm font-medium">Distribution (%)</Label>
            <Input
              id="percentage"
              type="number"
              min="1"
              max="100"
              value={percentage}
              onChange={(e) => setPercentage(e.target.value)}
              placeholder="Enter percentage"
              className="h-10"
            />
          </div>

          <div className="md:col-span-2 space-y-3">
            <Label className="text-sm font-medium">Locations</Label>
            <Input
              type="text"
              placeholder="Search locations..."
              value={searchLocation}
              onChange={(e) => setSearchLocation(e.target.value)}
              className="h-9 text-sm"
            />
            <div className="border border-gray-200 rounded-lg p-4 bg-white space-y-2 max-h-56 overflow-y-auto hover:border-gray-300 transition">
              {locations.length === 0 ? (
                <div className="text-sm text-muted-foreground py-4 text-center">No locations available</div>
              ) : (
                <div className="space-y-2">
                  {locations.filter(loc => loc.toLowerCase().includes(searchLocation.toLowerCase())).length === 0 ? (
                    <div className="text-sm text-muted-foreground py-2 text-center">No matching locations</div>
                  ) : (
                    locations.filter(loc => loc.toLowerCase().includes(searchLocation.toLowerCase())).map((location) => (
                      <div key={location} className="flex items-center gap-2 p-2 rounded-md hover:bg-gray-50 transition">
                        <Checkbox
                          id={`location-${location}`}
                          checked={selectedAddresses.includes(location)}
                          onCheckedChange={(checked) => {
                            if (checked) {
                              setSelectedAddresses([...selectedAddresses, location])
                            } else {
                              setSelectedAddresses(selectedAddresses.filter(a => a !== location))
                            }
                          }}
                        />
                        <Label htmlFor={`location-${location}`} className="text-sm font-normal cursor-pointer flex-1">
                          {location}
                        </Label>
                      </div>
                    ))
                  )}
                </div>
              )}
            </div>
            {selectedAddresses.length > 0 && (
              <div className="bg-blue-50 border border-blue-200 rounded-lg p-3 space-y-2">
                <div className="text-xs font-semibold text-blue-900">Selected Locations ({selectedAddresses.length})</div>
                <div className="flex flex-wrap gap-2">
                  {selectedAddresses.map((addr) => (
                    <div key={addr} className="bg-blue-500 text-white text-xs px-3 py-1 rounded-full flex items-center gap-1">
                      {addr}
                      <button
                        type="button"
                        onClick={() => setSelectedAddresses(selectedAddresses.filter(a => a !== addr))}
                        className="ml-1 hover:opacity-80"
                      >
                        ✕
                      </button>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
        </div>

        <Button onClick={handleGenerate} className="h-11 w-full text-base font-medium shadow-sm">
          <Plus className="mr-2 h-5 w-5" />
          Generate Scenario
        </Button>
      </CardContent>
    </Card>
  )
}