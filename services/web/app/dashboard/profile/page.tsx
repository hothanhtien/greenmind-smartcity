"use client"

import { useState, useEffect } from "react"
import { useAuth } from "@/contexts/AuthContext"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Skeleton } from "@/components/ui/skeleton"
import { useToast } from "@/components/ui/use-toast"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"
import { User, Mail, Calendar, MapPin, Heart, Save, Shield } from "lucide-react"

const API_URL = process.env.NEXT_PUBLIC_API_URL || "https://green-api.khoav4.com"

export default function ProfilePage() {
  const { user, login } = useAuth()
  const { toast } = useToast()

  const [formData, setFormData] = useState({
    fullName: "",
    email: "",
    age: "",
    gender: "",
    location: "",
  })
  const [isSaving, setIsSaving] = useState(false)
  const [pageLoading, setPageLoading] = useState(true)

  useEffect(() => {
    // Initialize form with user context data immediately
    if (user) {
      setFormData((prev) => ({
        ...prev,
        fullName: user.fullName || prev.fullName,
        email: user.email || prev.email,
      }))
    }
  }, [user])

  useEffect(() => {
    const fetchUserProfile = async () => {
      const token = localStorage.getItem("access_token")
      try {
        const response = await fetch(`${API_URL}/users/profile`, {
          headers: { Authorization: `Bearer ${token}` },
        })
        if (!response.ok) throw new Error("Failed to fetch user profile.")

        const profileData = await response.json()
        setFormData({
          fullName: profileData.data.fullName || user?.fullName || "",
          email: profileData.data.email || user?.email || "",
          age: profileData.data.age?.toString() || "",
          gender: profileData.data.gender || "",
          location: profileData.data.location || "",
        })
      } catch (error) {
        // Silently fail - user context data is already set
        if (user) {
          setFormData((prev) => ({
            ...prev,
            fullName: user.fullName || "",
            email: user.email || "",
          }))
        }
      } finally {
        setPageLoading(false)
      }
    }

    if (user) {
      fetchUserProfile()
    } else {
      setPageLoading(false)
    }
  }, [user])

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { id, value } = e.target
    setFormData((prev) => ({ ...prev, [id]: value }))
  }

  const handleSelectChange = (id: string, value: string) => {
    setFormData((prev) => ({ ...prev, [id]: value }))
  }

  const handleSave = async () => {
    if (!user) return

    setIsSaving(true)
    const token = localStorage.getItem("access_token")
    const refreshToken = localStorage.getItem("refresh_token")

    try {
      const response = await fetch(`${API_URL}/users/profile`, {
        method: "PATCH",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          fullName: formData.fullName,
          email: formData.email,
          age: Number(formData.age) || null,
          gender: formData.gender,
          location: formData.location,
        }),
      })

      const result = await response.json()
      if (!response.ok) throw new Error(result.message || "Failed to update profile.")

      const updatedUser = result.data
      if (token && refreshToken) {
        login(updatedUser, token, refreshToken)
      }

      toast({
        title: "Success!",
        description: "Your profile has been updated successfully.",
      })
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : "An unknown error occurred."
      toast({
        variant: "destructive",
        title: "Update Failed",
        description: errorMessage,
      })
    } finally {
      setIsSaving(false)
    }
  }

  if (pageLoading) {
    return <ProfileSkeleton />
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 via-emerald-50/30 to-cyan-50/20 p-4 md:p-6 lg:p-8">
      <div className="mx-auto max-w-3xl space-y-6">
        {/* Header */}
        <div className="text-center space-y-2">
          <h1 className="text-3xl font-bold bg-gradient-to-r from-emerald-600 to-teal-600 bg-clip-text text-transparent">
            My Profile
          </h1>
          <p className="text-muted-foreground">
            Manage your personal information and preferences
          </p>
        </div>

        {/* Avatar Section */}
        <div className="flex flex-col items-center">
          <div className="relative">
            <div className="h-24 w-24 rounded-full bg-gradient-to-br from-emerald-500 via-teal-500 to-cyan-500 flex items-center justify-center shadow-xl shadow-emerald-500/20">
              <span className="text-3xl font-bold text-white">
                {(formData.fullName || user?.fullName || "U").charAt(0).toUpperCase()}
              </span>
            </div>
            <div className="absolute -bottom-1 -right-1 h-8 w-8 rounded-full bg-white shadow-lg flex items-center justify-center border-2 border-emerald-500">
              <Shield className="h-4 w-4 text-emerald-500" />
            </div>
          </div>
          <h2 className="mt-4 text-xl font-semibold text-foreground">
            {formData.fullName || user?.fullName || "User"}
          </h2>
          <p className="text-sm text-muted-foreground">{formData.email || user?.email}</p>
        </div>

        {/* Form Card */}
        <div className="rounded-2xl bg-white/80 dark:bg-slate-900/80 backdrop-blur-xl border border-white/20 dark:border-slate-700/50 shadow-xl">
          <div className="p-6 md:p-8">
            <div className="flex items-center gap-3 mb-6 pb-4 border-b">
              <div className="p-2 rounded-lg bg-emerald-500/10">
                <User className="h-5 w-5 text-emerald-600" />
              </div>
              <div>
                <h3 className="font-semibold text-foreground">Personal Information</h3>
                <p className="text-xs text-muted-foreground">Update your account details</p>
              </div>
            </div>

            <div className="grid gap-5 md:grid-cols-2">
              {/* Full Name */}
              <div className="space-y-2 md:col-span-2">
                <Label className="text-sm font-medium flex items-center gap-2">
                  <User className="h-4 w-4 text-muted-foreground" />
                  Full Name
                </Label>
                <Input
                  id="fullName"
                  value={formData.fullName}
                  onChange={handleInputChange}
                  className="h-11"
                />
              </div>

              {/* Email */}
              <div className="space-y-2 md:col-span-2">
                <Label className="text-sm font-medium flex items-center gap-2">
                  <Mail className="h-4 w-4 text-muted-foreground" />
                  Email Address
                </Label>
                <Input
                  id="email"
                  type="email"
                  value={formData.email}
                  onChange={handleInputChange}
                  className="h-11"
                />
              </div>

              {/* Age */}
              <div className="space-y-2">
                <Label className="text-sm font-medium flex items-center gap-2">
                  <Calendar className="h-4 w-4 text-muted-foreground" />
                  Age
                </Label>
                <Input
                  id="age"
                  type="number"
                  value={formData.age}
                  onChange={handleInputChange}
                  placeholder="Enter your age"
                  className="h-11"
                />
              </div>

              {/* Gender */}
              <div className="space-y-2">
                <Label className="text-sm font-medium flex items-center gap-2">
                  <Heart className="h-4 w-4 text-muted-foreground" />
                  Gender
                </Label>
                <Select
                  value={formData.gender}
                  onValueChange={(value) => handleSelectChange("gender", value)}
                >
                  <SelectTrigger className="h-11">
                    <SelectValue placeholder="Select gender" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="Male">Male</SelectItem>
                    <SelectItem value="Female">Female</SelectItem>
                    <SelectItem value="Other">Other</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              {/* Location */}
              <div className="space-y-2 md:col-span-2">
                <Label className="text-sm font-medium flex items-center gap-2">
                  <MapPin className="h-4 w-4 text-muted-foreground" />
                  Location
                </Label>
                <Input
                  id="location"
                  value={formData.location}
                  onChange={handleInputChange}
                  placeholder="e.g., Da Nang, Quang Nam"
                  className="h-11"
                />
              </div>
            </div>

            {/* Save Button */}
            <div className="mt-8 flex justify-end">
              <Button
                onClick={handleSave}
                disabled={isSaving}
                className="h-11 px-8 bg-gradient-to-r from-emerald-500 to-teal-500 hover:from-emerald-600 hover:to-teal-600 shadow-lg shadow-emerald-500/20"
              >
                <Save className="w-4 h-4 mr-2" />
                {isSaving ? "Saving..." : "Save Changes"}
              </Button>
            </div>
          </div>
        </div>

        {/* Info Card */}
        <div className="rounded-xl bg-blue-50/50 dark:bg-blue-950/20 border border-blue-100 dark:border-blue-900/30 p-4">
          <div className="flex gap-3">
            <Shield className="h-5 w-5 text-blue-500 shrink-0 mt-0.5" />
            <div>
              <h4 className="text-sm font-medium text-blue-700 dark:text-blue-400">
                Your data is protected
              </h4>
              <p className="text-xs text-blue-600/70 dark:text-blue-500/70 mt-1">
                Your personal information is encrypted and secure. Only you can view and update your profile.
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

function ProfileSkeleton() {
  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 via-emerald-50/30 to-cyan-50/20 p-4 md:p-6 lg:p-8">
      <div className="mx-auto max-w-3xl space-y-6">
        <div className="text-center space-y-2">
          <Skeleton className="h-9 w-48 mx-auto" />
          <Skeleton className="h-5 w-72 mx-auto" />
        </div>

        <div className="flex flex-col items-center">
          <Skeleton className="h-24 w-24 rounded-full" />
          <Skeleton className="mt-4 h-6 w-32" />
          <Skeleton className="mt-2 h-4 w-48" />
        </div>

        <div className="rounded-2xl bg-white/80 dark:bg-slate-900/80 backdrop-blur-xl border border-white/20 dark:border-slate-700/50 shadow-xl p-6 md:p-8">
          <Skeleton className="h-6 w-48 mb-6" />
          <div className="grid gap-5 md:grid-cols-2">
            <div className="space-y-2 md:col-span-2">
              <Skeleton className="h-4 w-24" />
              <Skeleton className="h-11 w-full" />
            </div>
            <div className="space-y-2 md:col-span-2">
              <Skeleton className="h-4 w-24" />
              <Skeleton className="h-11 w-full" />
            </div>
            <div className="space-y-2">
              <Skeleton className="h-4 w-16" />
              <Skeleton className="h-11 w-full" />
            </div>
            <div className="space-y-2">
              <Skeleton className="h-4 w-16" />
              <Skeleton className="h-11 w-full" />
            </div>
          </div>
          <Skeleton className="h-11 w-40 mt-8 ml-auto" />
        </div>
      </div>
    </div>
  )
}