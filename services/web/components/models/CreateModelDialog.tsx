"use client"

import { useState, useEffect } from "react"
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from "@/components/ui/dialog"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Checkbox } from "@/components/ui/checkbox"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover"
import { Badge } from "@/components/ui/badge"
import { Plus, X, Pencil, Check } from "lucide-react"
import { OCEAN_DATA, type OceanKey } from "@/lib/ocean-data"

interface CreateModelDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  onModelCreated: () => void
}

const API_URL = process.env.NEXT_PUBLIC_API_URL || "https://green-api.khoav4.com"

export function CreateModelDialog({
  open,
  onOpenChange,
  onModelCreated,
}: CreateModelDialogProps) {
  const [createLoading, setCreateLoading] = useState(false)
  const [formData, setFormData] = useState({
    ocean: "",
    behavior: "",
    ageRange: "18-30",
    genders: [] as string[],
    locations: [] as string[],
    urban: false,
    setting: "",
    event: "",
  })
  const [newLocation, setNewLocation] = useState("")
  const [isEditingBehavior, setIsEditingBehavior] = useState(false)
  const [editBehaviorValue, setEditBehaviorValue] = useState("")
  const [behaviorPopoverOpen, setBehaviorPopoverOpen] = useState(false)

  const ageRangeOptions = ["0-17", "18-30", "30-50", "50-65", "65+"]
  const genderOptions = [
    { value: "male", label: "Male" },
    { value: "female", label: "Female" },
    { value: "other", label: "Other" },
  ]
  const oceanOptions = ["O", "C", "E", "A", "N"]

  const getAvailableBehaviors = () => {
    if (!formData.ocean || !OCEAN_DATA[formData.ocean as OceanKey]) {
      return []
    }
    return OCEAN_DATA[formData.ocean as OceanKey].behaviors
  }

  useEffect(() => {
    if (open) {
      resetForm()
    }
  }, [open])

  useEffect(() => {
    setFormData(prev => ({ ...prev, behavior: "" }))
    setIsEditingBehavior(false)
    setEditBehaviorValue("")
  }, [formData.ocean])

  const handleCreateModel = async () => {
    try {
      setCreateLoading(true)
      const token = localStorage.getItem("access_token")
      if (!token) return

      const payload = {
        ocean: formData.ocean,
        behavior: formData.behavior,
        context: {
          population: {
            age_range: formData.ageRange,
            gender: formData.genders,
            locations: formData.locations,
            urban: formData.urban,
          },
          setting: formData.setting,
          event: formData.event,
        },
      }

      const response = await fetch(`${API_URL}/models/create`, {
        method: "POST",
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify(payload),
      })

      const result = await response.json()

      if (result.success) {
        onModelCreated()
      } else {
        console.error("Error creating model:", result)
      }
    } catch (error) {
      console.error("Error creating model:", error)
    } finally {
      setCreateLoading(false)
    }
  }

  const resetForm = () => {
    setFormData({
      ocean: "",
      behavior: "",
      ageRange: "18-30",
      genders: [],
      locations: [],
      urban: false,
      setting: "",
      event: "",
    })
    setNewLocation("")
    setIsEditingBehavior(false)
    setEditBehaviorValue("")
  }

  const handleAddLocation = () => {
    if (newLocation.trim() && !formData.locations.includes(newLocation.trim())) {
      setFormData({
        ...formData,
        locations: [...formData.locations, newLocation.trim()],
      })
      setNewLocation("")
    }
  }

  const handleRemoveLocation = (location: string) => {
    setFormData({
      ...formData,
      locations: formData.locations.filter(l => l !== location),
    })
  }

  const handleGenderToggle = (gender: string) => {
    setFormData(prev => ({
      ...prev,
      genders: prev.genders.includes(gender)
        ? prev.genders.filter(g => g !== gender)
        : [...prev.genders, gender],
    }))
  }
  
  const handleSelectBehavior = (behavior: string) => {
    setFormData({ ...formData, behavior })
    setEditBehaviorValue(behavior)
    setBehaviorPopoverOpen(false)
  }

  const handleStartEditBehavior = (e: React.MouseEvent) => {
    e.stopPropagation()
    setIsEditingBehavior(true)
    setEditBehaviorValue(formData.behavior)
  }

  const handleConfirmEditBehavior = () => {
    if (editBehaviorValue.trim()) {
      setFormData({ ...formData, behavior: editBehaviorValue.trim() })
    }
    setIsEditingBehavior(false)
    setBehaviorPopoverOpen(false)
  }

  const handleCancelEditBehavior = () => {
    setIsEditingBehavior(false)
    setEditBehaviorValue(formData.behavior)
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Create New Model</DialogTitle>
        </DialogHeader>

        <div className="space-y-6 py-4">
          <div className="space-y-2">
            <Label htmlFor="ocean">OCEAN Trait</Label>
            <Select
              value={formData.ocean}
              onValueChange={value => setFormData({ ...formData, ocean: value })}
            >
              <SelectTrigger>
                <SelectValue placeholder="Select OCEAN trait" />
              </SelectTrigger>
              <SelectContent>
                {oceanOptions.map(option => (
                  <SelectItem key={option} value={option}>
                    {option} -
                    {option === "O"
                      ? " Openness"
                      : option === "C"
                      ? " Conscientiousness"
                      : option === "E"
                      ? " Extraversion"
                      : option === "A"
                      ? " Agreeableness"
                      : " Neuroticism"}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          <div className="space-y-2">
            <Label>Behavior</Label>
            <Popover
              open={behaviorPopoverOpen}
              onOpenChange={setBehaviorPopoverOpen}
            >
              <PopoverTrigger asChild>
                <Button
                  variant="outline"
                  role="combobox"
                  className="w-full justify-between h-auto min-h-10 py-2"
                  disabled={!formData.ocean}
                >
                  <span className="text-left truncate flex-1">
                    {formData.behavior ||
                      (formData.ocean
                        ? "Select behavior..."
                        : "Select OCEAN first")}
                  </span>
                </Button>
              </PopoverTrigger>
              <PopoverContent className="w-[500px] p-0" align="start">
                {isEditingBehavior ? (
                  <div className="p-3 space-y-3">
                    <Label className="text-sm font-medium">Edit Behavior</Label>
                    <Input
                      value={editBehaviorValue}
                      onChange={e => setEditBehaviorValue(e.target.value)}
                      placeholder="Enter custom behavior..."
                      autoFocus
                      onKeyDown={e => {
                        if (e.key === "Enter") handleConfirmEditBehavior()
                        else if (e.key === "Escape") handleCancelEditBehavior()
                      }}
                    />
                    <div className="flex gap-2 justify-end">
                      <Button
                        size="sm"
                        variant="outline"
                        onClick={handleCancelEditBehavior}
                      >
                        Cancel
                      </Button>
                      <Button size="sm" onClick={handleConfirmEditBehavior}>
                        <Check className="h-4 w-4 mr-1" />
                        Confirm
                      </Button>
                    </div>
                  </div>
                ) : (
                  <div className="py-1">
                    {getAvailableBehaviors().map((behavior, index) => (
                      <div
                        key={index}
                        className="flex items-center justify-between px-3 py-2 hover:bg-muted cursor-pointer group"
                      >
                        <span
                          className="flex-1 text-sm"
                          onClick={() => handleSelectBehavior(behavior)}
                        >
                          {behavior}
                        </span>
                        <Button
                          size="sm"
                          variant="ghost"
                          className="h-7 w-7 p-0 opacity-0 group-hover:opacity-100 transition-opacity"
                          onClick={handleStartEditBehavior}
                        >
                          <Pencil className="h-3.5 w-3.5" />
                        </Button>
                      </div>
                    ))}
                    {getAvailableBehaviors().length === 0 && (
                      <div className="px-3 py-2 text-sm text-muted-foreground">
                        No behaviors available
                      </div>
                    )}
                    <div className="border-t mt-1 pt-1">
                      <div
                        className="flex items-center gap-2 px-3 py-2 hover:bg-muted cursor-pointer text-primary"
                        onClick={() => {
                          setEditBehaviorValue("")
                          setIsEditingBehavior(true)
                        }}
                      >
                        <Plus className="h-4 w-4" />
                        <span className="text-sm">Add custom behavior</span>
                      </div>
                    </div>
                  </div>
                )}
              </PopoverContent>
            </Popover>
          </div>

          <div className="space-y-4 p-4 border rounded-lg">
            <h3 className="font-semibold">Context</h3>
            <div className="space-y-4 p-3 border rounded-lg bg-muted/30">
              <h4 className="text-sm font-medium">Population</h4>
              <div className="space-y-2">
                <Label>Age Range</Label>
                <Select
                  value={formData.ageRange}
                  onValueChange={value =>
                    setFormData({ ...formData, ageRange: value })
                  }
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Select age range" />
                  </SelectTrigger>
                  <SelectContent>
                    {ageRangeOptions.map(option => (
                      <SelectItem key={option} value={option}>
                        {option}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              <div className="space-y-2">
                <Label>Gender</Label>
                <div className="flex flex-wrap gap-2">
                  {genderOptions.map(option => (
                    <div
                      key={option.value}
                      className={`flex items-center gap-2 px-3 py-2 border rounded-md cursor-pointer transition-colors ${
                        formData.genders.includes(option.value)
                          ? "bg-primary text-primary-foreground border-primary"
                          : "hover:bg-muted"
                      }`}
                      onClick={() => handleGenderToggle(option.value)}
                    >
                      <Checkbox
                        checked={formData.genders.includes(option.value)}
                        onCheckedChange={() => handleGenderToggle(option.value)}
                      />
                      <span className="text-sm">{option.label}</span>
                    </div>
                  ))}
                </div>
              </div>

              <div className="space-y-2">
                <Label>Locations</Label>
                <div className="flex gap-2">
                  <Input
                    placeholder="Enter location"
                    value={newLocation}
                    onChange={e => setNewLocation(e.target.value)}
                    onKeyDown={e => {
                      if (e.key === "Enter") {
                        e.preventDefault()
                        handleAddLocation()
                      }
                    }}
                  />
                  <Button
                    type="button"
                    variant="secondary"
                    onClick={handleAddLocation}
                  >
                    <Plus className="h-4 w-4" />
                  </Button>
                </div>
                {formData.locations.length > 0 && (
                  <div className="flex flex-wrap gap-2 mt-2">
                    {formData.locations.map(location => (
                      <Badge
                        key={location}
                        variant="secondary"
                        className="flex items-center gap-1"
                      >
                        {location}
                        <X
                          className="h-3 w-3 cursor-pointer hover:text-destructive"
                          onClick={() => handleRemoveLocation(location)}
                        />
                      </Badge>
                    ))}
                  </div>
                )}
              </div>

              <div className="flex items-center space-x-2">
                <Checkbox
                  id="urban"
                  checked={formData.urban}
                  onCheckedChange={checked =>
                    setFormData({ ...formData, urban: checked as boolean })
                  }
                />
                <Label htmlFor="urban" className="cursor-pointer">
                  Urban Area
                </Label>
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="setting">Setting</Label>
              <Input
                id="setting"
                placeholder="e.g., water cleaning"
                value={formData.setting}
                onChange={e =>
                  setFormData({ ...formData, setting: e.target.value })
                }
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="event">Event</Label>
              <Input
                id="event"
                placeholder="e.g., Green Sunday in rural area"
                value={formData.event}
                onChange={e =>
                  setFormData({ ...formData, event: e.target.value })
                }
              />
            </div>
          </div>
        </div>

        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>
            Cancel
          </Button>
          <Button
            onClick={handleCreateModel}
            disabled={createLoading || !formData.ocean || !formData.behavior}
          >
            {createLoading ? "Creating..." : "Create Model"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}

