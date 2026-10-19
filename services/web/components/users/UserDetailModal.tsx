"use client"

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Progress } from "@/components/ui/progress"
import { X } from "lucide-react"
import { Button } from "@/components/ui/button"
import {
    BarChart,
    Bar,
    XAxis,
    YAxis,
    CartesianGrid,
    Tooltip,
    ResponsiveContainer,
    RadarChart,
    Radar,
    PolarGrid,
    PolarAngleAxis,
    PolarRadiusAxis,
} from "recharts"

interface User {
    id: string
    username: string
    fullName: string
    email: string
    phoneNumber?: string
    gender?: string
    location: string
    role?: string
    dateOfBirth: string
    createdAt: string
    updatedAt: string
    bigFive?: {
        id: string
        openness: number
        conscientiousness: number
        extraversion: number
        agreeableness: number
        neuroticism: number
        createdAt: string
        updatedAt: string
    } | null
}

interface UserDetailModalProps {
    user: User | null
    onClose: () => void
}

export function UserDetailModal({ user, onClose }: UserDetailModalProps) {
    if (!user) return null

    // Calculate age from dateOfBirth
    const calculateAge = (dateOfBirth: string): number => {
        const today = new Date()
        const birthDate = new Date(dateOfBirth)
        let age = today.getFullYear() - birthDate.getFullYear()
        const monthDiff = today.getMonth() - birthDate.getMonth()
        if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
            age--
        }
        return age
    }

    const userAge = calculateAge(user.dateOfBirth)

    // Convert bigFive values to percentages (multiply by 100 for display)
    const convertToPercentage = (value: number): number => {
        return Math.round(value * 100)
    }

    const oceanData = user.bigFive
        ? [
            {
                name: "Openness",
                value: convertToPercentage(user.bigFive.openness),
                fullMark: 100,
            },
            {
                name: "Conscientiousness",
                value: convertToPercentage(user.bigFive.conscientiousness),
                fullMark: 100,
            },
            {
                name: "Extraversion",
                value: convertToPercentage(user.bigFive.extraversion),
                fullMark: 100,
            },
            {
                name: "Agreeableness",
                value: convertToPercentage(user.bigFive.agreeableness),
                fullMark: 100,
            },
            {
                name: "Neuroticism",
                value: convertToPercentage(user.bigFive.neuroticism),
                fullMark: 100,
            },
        ]
        : []

    const oceanBarData = user.bigFive
        ? [
            {
                name: "Openness",
                score: convertToPercentage(user.bigFive.openness),
                fill: "#3b82f6",
            },
            {
                name: "Conscientiousness",
                score: convertToPercentage(user.bigFive.conscientiousness),
                fill: "#10b981",
            },
            {
                name: "Extraversion",
                score: convertToPercentage(user.bigFive.extraversion),
                fill: "#f59e0b",
            },
            {
                name: "Agreeableness",
                score: convertToPercentage(user.bigFive.agreeableness),
                fill: "#ec4899",
            },
            {
                name: "Neuroticism",
                score: convertToPercentage(user.bigFive.neuroticism),
                fill: "#ef4444",
            },
        ]
        : []

    return (
        <div className="fixed inset-0 z-50 bg-black/50 flex items-center justify-center p-4">
            <div className="bg-white rounded-lg shadow-lg max-w-2xl w-full max-h-[90vh] overflow-y-auto">
                {/* Header */}
                <div className="flex items-center justify-between p-6 border-b sticky top-0 bg-white">
                    <h2 className="text-2xl font-bold">User Profile</h2>
                    <Button
                        variant="ghost"
                        size="icon"
                        onClick={onClose}
                        className="rounded-full"
                    >
                        <X className="h-5 w-5" />
                    </Button>
                </div>

                <div className="p-6 space-y-6">
                    {/* Personal Info */}
                    <div className="grid gap-4 md:grid-cols-2">
                        <div>
                            <div className="space-y-3">
                                <div>
                                    <p className="text-sm text-muted-foreground">Full Name</p>
                                    <p className="text-lg font-semibold">{user.fullName}</p>
                                </div>
                                <div>
                                    <p className="text-sm text-muted-foreground">Username</p>
                                    <p className="text-lg font-semibold">{user.username}</p>
                                </div>
                                <div>
                                    <p className="text-sm text-muted-foreground">Email</p>
                                    <p className="text-lg font-semibold">{user.email}</p>
                                </div>
                            </div>
                        </div>

                        <div>
                            <div className="space-y-3">
                                <div>
                                    <p className="text-sm text-muted-foreground">Age</p>
                                    <p className="text-lg font-semibold">{userAge} years old</p>
                                </div>
                                <div>
                                    <p className="text-sm text-muted-foreground">Gender</p>
                                    <div className="mt-1">
                                        <Badge>{user.gender || "Not specified"}</Badge>
                                    </div>
                                </div>
                                <div>
                                    <p className="text-sm text-muted-foreground">Location</p>
                                    <p className="text-lg font-semibold">{user.location}</p>
                                </div>
                            </div>
                        </div>
                    </div>

                    {/* OCEAN Radar Chart */}
                    {user.bigFive && (
                        <Card>
                            <CardHeader>
                                <CardTitle className="text-base">OCEAN Profile - Radar View</CardTitle>
                            </CardHeader>
                            <CardContent>
                                <ResponsiveContainer width="100%" height={300}>
                                    <RadarChart data={oceanData}>
                                        <PolarGrid />
                                        <PolarAngleAxis dataKey="name" />
                                        <PolarRadiusAxis angle={90} domain={[0, 100]} />
                                        <Radar
                                            name="Score"
                                            dataKey="value"
                                            stroke="#3b82f6"
                                            fill="#3b82f6"
                                            fillOpacity={0.6}
                                        />
                                        <Tooltip />
                                    </RadarChart>
                                </ResponsiveContainer>
                            </CardContent>
                        </Card>
                    )}

                    {/* OCEAN Bar Chart */}
                    {user.bigFive && (
                        <Card>
                            <CardHeader>
                                <CardTitle className="text-base">OCEAN Profile - Detailed Scores</CardTitle>
                            </CardHeader>
                            <CardContent>
                                <ResponsiveContainer width="100%" height={300}>
                                    <BarChart
                                        data={oceanBarData}
                                        layout="vertical"
                                        margin={{ top: 5, right: 30, left: 150, bottom: 5 }}
                                    >
                                        <CartesianGrid strokeDasharray="3 3" />
                                        <XAxis type="number" domain={[0, 100]} />
                                        <YAxis dataKey="name" type="category" />
                                        <Tooltip />
                                        <Bar dataKey="score" fill="#3b82f6" />
                                    </BarChart>
                                </ResponsiveContainer>
                            </CardContent>
                        </Card>
                    )}

                    {/* OCEAN Breakdown */}
                    {user.bigFive && (
                        <Card>
                            <CardHeader>
                                <CardTitle className="text-base">OCEAN Trait Breakdown</CardTitle>
                            </CardHeader>
                            <CardContent className="space-y-4">
                                <div>
                                    <div className="flex justify-between mb-2">
                                        <span className="font-medium">Openness</span>
                                        <span className="text-sm text-muted-foreground">{convertToPercentage(user.bigFive.openness)}/100</span>
                                    </div>
                                    <Progress value={convertToPercentage(user.bigFive.openness)} className="h-2" />
                                    <p className="text-xs text-muted-foreground mt-1">
                                        {convertToPercentage(user.bigFive.openness) > 70
                                            ? "High openness to experience and creativity"
                                            : convertToPercentage(user.bigFive.openness) > 40
                                                ? "Moderate openness to new ideas"
                                                : "Preference for familiar routines"}
                                    </p>
                                </div>

                                <div>
                                    <div className="flex justify-between mb-2">
                                        <span className="font-medium">Conscientiousness</span>
                                        <span className="text-sm text-muted-foreground">{convertToPercentage(user.bigFive.conscientiousness)}/100</span>
                                    </div>
                                    <Progress value={convertToPercentage(user.bigFive.conscientiousness)} className="h-2" />
                                    <p className="text-xs text-muted-foreground mt-1">
                                        {convertToPercentage(user.bigFive.conscientiousness) > 70
                                            ? "Highly organized and disciplined"
                                            : convertToPercentage(user.bigFive.conscientiousness) > 40
                                                ? "Moderately organized"
                                                : "More spontaneous and flexible"}
                                    </p>
                                </div>

                                <div>
                                    <div className="flex justify-between mb-2">
                                        <span className="font-medium">Extraversion</span>
                                        <span className="text-sm text-muted-foreground">{convertToPercentage(user.bigFive.extraversion)}/100</span>
                                    </div>
                                    <Progress value={convertToPercentage(user.bigFive.extraversion)} className="h-2" />
                                    <p className="text-xs text-muted-foreground mt-1">
                                        {convertToPercentage(user.bigFive.extraversion) > 70
                                            ? "Outgoing and highly social"
                                            : convertToPercentage(user.bigFive.extraversion) > 40
                                                ? "Moderately social"
                                                : "More introverted and reserved"}
                                    </p>
                                </div>

                                <div>
                                    <div className="flex justify-between mb-2">
                                        <span className="font-medium">Agreeableness</span>
                                        <span className="text-sm text-muted-foreground">{convertToPercentage(user.bigFive.agreeableness)}/100</span>
                                    </div>
                                    <Progress value={convertToPercentage(user.bigFive.agreeableness)} className="h-2" />
                                    <p className="text-xs text-muted-foreground mt-1">
                                        {convertToPercentage(user.bigFive.agreeableness) > 70
                                            ? "Very cooperative and compassionate"
                                            : convertToPercentage(user.bigFive.agreeableness) > 40
                                                ? "Moderately agreeable"
                                                : "More assertive and competitive"}
                                    </p>
                                </div>

                                <div>
                                    <div className="flex justify-between mb-2">
                                        <span className="font-medium">Neuroticism</span>
                                        <span className="text-sm text-muted-foreground">{convertToPercentage(user.bigFive.neuroticism)}/100</span>
                                    </div>
                                    <Progress value={convertToPercentage(user.bigFive.neuroticism)} className="h-2" />
                                    <p className="text-xs text-muted-foreground mt-1">
                                        {convertToPercentage(user.bigFive.neuroticism) > 70
                                            ? "Prone to anxiety and emotional volatility"
                                            : convertToPercentage(user.bigFive.neuroticism) > 40
                                                ? "Moderately emotionally reactive"
                                                : "More emotionally stable and calm"}
                                    </p>
                                </div>
                            </CardContent>
                        </Card>
                    )}

                    {/* Metadata */}
                    <div className="pt-4 border-t">
                        <div className="grid gap-2 text-sm text-muted-foreground">
                            <div className="flex justify-between">
                                <span>Created:</span>
                                <span>{new Date(user.createdAt).toLocaleDateString()}</span>
                            </div>
                            <div className="flex justify-between">
                                <span>Last Updated:</span>
                                <span>{new Date(user.updatedAt).toLocaleDateString()}</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    )
}
