"use client"

import { useState, useEffect } from "react"
import { useParams, useRouter } from "next/navigation"
import {
  ScatterChart,
  Scatter,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ReferenceLine,
  ResponsiveContainer,
  Cell,
  LabelList,
} from "recharts"
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card"
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
} from "@/components/ui/dialog"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Skeleton } from "@/components/ui/skeleton"
import { ArrowLeft, CheckCircle, AlertTriangle, XCircle, Eye } from "lucide-react"

// Interfaces based on API response
interface Segment {
  id: string
  name: string
  location: string
  ageRange: string
  gender: string
}

interface Feedback {
  id: string
  model_id: string
  segment_id: string
  user_id: string
  trait_checked: string
  expected: number | string
  actual: number | string
  deviation: number | string
  engagement: number | string
  match: boolean
  level: string
  feedback: string[]
  mechanismFeedbacks: any[]
  segment: Segment
  created_at?: string
}

interface Model {
  id: string
  ocean: string
  behavior: string
  age: string
  location: string
  gender: string
  keywords: string
}

interface ApiResponse {
  feedbacks: Feedback[]
  model: Model
}

interface ChartDataPoint {
  id: string
  segmentId: string
  subContext: string
  age: string
  location: string
  gender: string
  engagement: number
  engagementDisplay: number
  engagementOriginal: number
  level: string
  recommendation: string
  feedback: string[]
  feedbackCount: number
}

// Group feedbacks by segment
interface GroupedFeedback {
  segmentId: string
  segment: Segment
  feedbacks: Feedback[]
  latestFeedback: Feedback
  totalCount: number
}

const API_URL = process.env.NEXT_PUBLIC_API_URL || "https://green-api.khoav4.com"

// Helper function to safely format numbers
const formatNumber = (value: any, decimals: number = 2): string => {
  if (value === null || value === undefined || value === "") return "-"
  const num = typeof value === "number" ? value : parseFloat(String(value))
  if (isNaN(num)) return "-"
  return num.toFixed(decimals)
}

// Score is 0-10 scale
const getScoreColor = (score: number): string => {
  if (score >= 7) return "#22c55e" // Green
  if (score >= 5) return "#eab308" // Yellow
  return "#ef4444" // Red
}

const getRecommendation = (score: number): string => {
  if (score >= 7) return "Keep Current Strategy"
  if (score >= 5) return "Optimize with ML Enhancement"
  return "Critical - Switch to Digital/Social Media Strategy"
}

const getRecommendationStatus = (score: number) => {
  if (score >= 7) {
    return {
      icon: <CheckCircle className="h-4 w-4 text-green-500" />,
      text: "Keep Strategy",
      variant: "default" as const,
      className: "bg-green-500",
    }
  }
  if (score >= 5) {
    return {
      icon: <AlertTriangle className="h-4 w-4 text-yellow-500" />,
      text: "Optimize",
      variant: "outline" as const,
      className: "border-yellow-500 text-yellow-500",
    }
  }
  return {
    icon: <XCircle className="h-4 w-4 text-red-500" />,
    text: "Change Strategy",
    variant: "destructive" as const,
    className: "",
  }
}

const CustomTooltip = ({ active, payload }: any) => {
  if (active && payload && payload.length) {
    const data = payload[0].payload as ChartDataPoint
    const score = data.engagementOriginal

    return (
      <div className="bg-popover border rounded-lg p-3 shadow-lg max-w-xs">
        <p className="font-semibold text-sm">{data.subContext}</p>
        <div className="mt-2 space-y-1 text-sm">
          <p>
            <span className="text-muted-foreground">Score: </span>
            <span
              className="font-medium"
              style={{ color: getScoreColor(score) }}
            >
              {score.toFixed(1)}
            </span>
          </p>
          <p>
            <span className="text-muted-foreground">Age: </span>
            {data.age}
          </p>
          <p>
            <span className="text-muted-foreground">Location: </span>
            {data.location}
          </p>
          <p>
            <span className="text-muted-foreground">Total Feedbacks: </span>
            <Badge variant="secondary" className="ml-1">{data.feedbackCount}</Badge>
          </p>
        </div>
        <div className="mt-2 pt-2 border-t">
          <p className="text-xs font-medium" style={{ color: getScoreColor(score) }}>
            💡 {getRecommendation(score)}
          </p>
        </div>
        {data.feedback && data.feedback.length > 0 && (
          <div className="mt-2 pt-2 border-t">
            <p className="text-xs text-muted-foreground">Latest Suggestions:</p>
            <ul className="list-disc list-inside text-xs mt-1">
              {data.feedback.slice(0, 2).map((item, idx) => (
                <li key={idx} className="truncate">{item}</li>
              ))}
            </ul>
          </div>
        )}
      </div>
    )
  }
  return null
}

export default function ModelVerifyDetailPage() {
  const params = useParams()
  const router = useRouter()
  const modelId = params.id as string

  const [model, setModel] = useState<Model | null>(null)
  const [feedbacks, setFeedbacks] = useState<Feedback[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  // Modal state - now holds multiple feedbacks for a segment
  const [selectedSegmentFeedbacks, setSelectedSegmentFeedbacks] = useState<Feedback[] | null>(null)
  const [isModalOpen, setIsModalOpen] = useState(false)

  useEffect(() => {
    fetchData()
  }, [modelId])

  const fetchData = async () => {
    try {
      setLoading(true)
      setError(null)
      const token = localStorage.getItem("access_token")

      if (!token) {
        setError("No access token found. Please login again.")
        setLoading(false)
        return
      }

      const response = await fetch(`${API_URL}/models/${modelId}/feedbacks`, {
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
      })

      if (!response.ok) {
        throw new Error(`Failed to fetch data: ${response.status}`)
      }

      const data: ApiResponse = await response.json()

      setModel(data.model)
      setFeedbacks(data.feedbacks || [])
    } catch (err) {
      console.error("Error fetching data:", err)
      setError(err instanceof Error ? err.message : "Failed to fetch data")
    } finally {
      setLoading(false)
    }
  }

  // Group feedbacks by segment_id
  const groupedFeedbacks: GroupedFeedback[] = Object.values(
    feedbacks.reduce((acc, feedback) => {
      const segmentId = feedback.segment_id
      if (!acc[segmentId]) {
        acc[segmentId] = {
          segmentId,
          segment: feedback.segment,
          feedbacks: [],
          latestFeedback: feedback,
          totalCount: 0,
        }
      }
      acc[segmentId].feedbacks.push(feedback)
      acc[segmentId].totalCount++

      // Update latest feedback based on created_at - use the newest one
      const currentLatest = acc[segmentId].latestFeedback
      if (feedback.created_at && currentLatest.created_at) {
        // Both have created_at - compare dates
        if (new Date(feedback.created_at) > new Date(currentLatest.created_at)) {
          acc[segmentId].latestFeedback = feedback
        }
      } else if (feedback.created_at && !currentLatest.created_at) {
        // New feedback has date, current doesn't - use the one with date
        acc[segmentId].latestFeedback = feedback
      }
      // If new feedback doesn't have created_at but current does, keep current
      // If neither has created_at, keep the first one (don't overwrite)

      return acc
    }, {} as Record<string, GroupedFeedback>)
  )

  // Helper function to extract age from segment name
  // Format: "Location_Age_Gender" (e.g., "Da Nang, Vietnam_12_male" or "Hue_30_female")
  const extractAgeFromSegmentName = (segmentName: string): string => {
    if (!segmentName) return "-"
    const parts = segmentName.split("_")
    if (parts.length >= 2) {
      const agePart = parts[parts.length - 2] // Age is second to last
      return agePart || "-"
    }
    return "-"
  }

  const handleViewSegmentFeedbacks = (segmentFeedbacks: Feedback[]) => {
    setSelectedSegmentFeedbacks(segmentFeedbacks)
    setIsModalOpen(true)
  }

  // Transform grouped feedbacks to chart data - only show latest state per segment
  const chartData: ChartDataPoint[] = groupedFeedbacks.map((group, index) => {
    const latestFeedback = group.latestFeedback
    const eng = Number(latestFeedback.engagement)
    const engagementScore = eng > 1 ? eng : eng * 10
    return {
      id: latestFeedback.id,
      segmentId: group.segmentId,
      subContext: latestFeedback.segment?.name || `${latestFeedback.segment?.ageRange}-${latestFeedback.segment?.location}-${latestFeedback.segment?.gender}`,
      age: latestFeedback.segment?.ageRange || "",
      location: latestFeedback.segment?.location || "",
      gender: latestFeedback.segment?.gender || "",
      engagement: index,
      engagementDisplay: 10 - engagementScore,
      engagementOriginal: engagementScore,
      level: latestFeedback.level,
      recommendation: getRecommendation(engagementScore),
      feedback: latestFeedback.feedback,
      feedbackCount: group.totalCount,
    }
  })

  const getOceanFullName = (trait: string): string => {
    const names: Record<string, string> = {
      O: "Openness",
      C: "Conscientiousness",
      E: "Extraversion",
      A: "Agreeableness",
      N: "Neuroticism",
    }
    return names[trait] || trait
  }

  if (loading) {
    return (
      <div className="space-y-6 p-6">
        <div className="flex items-center gap-4">
          <Skeleton className="h-10 w-20" />
          <div>
            <Skeleton className="h-8 w-64" />
            <Skeleton className="h-4 w-48 mt-2" />
          </div>
        </div>
        <Skeleton className="h-48 w-full" />
        <Skeleton className="h-96 w-full" />
      </div>
    )
  }

  if (error) {
    return (
      <div className="space-y-6 p-6">
        <div className="flex items-center gap-4">
          <Button variant="ghost" onClick={() => router.back()}>
            <ArrowLeft className="h-4 w-4 mr-2" />
            Back
          </Button>
        </div>
        <Card>
          <CardContent className="p-6">
            <p className="text-red-500">{error}</p>
            <Button onClick={fetchData} className="mt-4">
              Retry
            </Button>
          </CardContent>
        </Card>
      </div>
    )
  }

  if (!model) {
    return (
      <div className="space-y-6 p-6">
        <div className="flex items-center gap-4">
          <Button variant="ghost" onClick={() => router.back()}>
            <ArrowLeft className="h-4 w-4 mr-2" />
            Back
          </Button>
        </div>
        <Card>
          <CardContent className="p-6">
            <p className="text-muted-foreground">Model not found</p>
          </CardContent>
        </Card>
      </div>
    )
  }

  return (
    <div className="space-y-6 p-6">
      {/* Header with Back Button */}
      <div className="flex items-center gap-4">
        <Button variant="ghost" onClick={() => router.back()}>
          <ArrowLeft className="h-4 w-4 mr-2" />
          Back
        </Button>
        <div>
          <h1 className="text-4xl font-bold">Model Verification</h1>
          <p className="text-lg font-medium text-muted-foreground">
            Analyze engagement scores and recommendations
          </p>
        </div>
      </div>

      {/* Combined Section: Model Info and Chart in One Row */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Section 1: Model Info Header */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-xl">
              <Badge variant="outline" className="text-xl px-4 py-1.5 font-bold">
                {model.ocean}
              </Badge>
              <span className="font-bold">{getOceanFullName(model.ocean)}</span>
            </CardTitle>
            <CardDescription className="text-base font-medium">Model Information & Current Strategy</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-1 gap-4">
              <div className="space-y-1">
                <p className="text-base font-semibold text-muted-foreground">Behavior</p>
                <p className="text-lg font-bold">{model.behavior}</p>
              </div>
              <div className="space-y-1">
                <p className="text-base font-semibold text-muted-foreground">Target Age</p>
                <p className="text-lg font-bold">{model.age}</p>
              </div>
              <div className="space-y-1">
                <p className="text-base font-semibold text-muted-foreground">Target Gender</p>
                <p className="text-lg font-bold">{model.gender}</p>
              </div>
              <div className="space-y-1">
                <p className="text-base font-semibold text-muted-foreground">Location</p>
                <p className="text-lg font-bold">{model.location}</p>
              </div>
              <div className="space-y-1">
                <p className="text-base font-semibold text-muted-foreground">Keywords (Current Strategy)</p>
                <p className="text-lg font-bold">{model.keywords}</p>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Section 2: Scatter Plot Chart - Latest State per Segment */}
        <Card>
          <CardHeader>
            <CardTitle className="text-xl font-bold">Engagement Score Analysis (Latest State per Segment)</CardTitle>
            <CardDescription className="text-base font-medium">
              Showing the most recent feedback state for each segment
            </CardDescription>
          </CardHeader>
          <CardContent>
            {chartData.length > 0 ? (
              <ResponsiveContainer width="100%" height={Math.max(300, chartData.length * 60)}>
                <ScatterChart
                  margin={{ top: 20, right: 30, bottom: 20, left: 20 }}
                >
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis
                    type="number"
                    dataKey="engagementDisplay"
                    name="Engagement Score"
                    domain={[0, 10]}
                    tickCount={11}
                    tickFormatter={(value) => `${10 - value}`}
                    label={{
                      value: "Engagement Score (10 → 0)",
                      position: "bottom",
                      offset: 0,
                    }}
                  />
                  <YAxis
                    type="number"
                    dataKey="engagement"
                    name="Index"
                    hide={true}
                    domain={[-0.5, chartData.length - 0.5]}
                  />
                  <Tooltip content={<CustomTooltip />} />

                  {/* Green bar at position 10 (display 0) - Best */}
                  <ReferenceLine
                    x={0}
                    stroke="#22c55e"
                    strokeWidth={4}
                    label={{
                      value: "10",
                      position: "top",
                      fill: "#22c55e",
                      fontSize: 14,
                      fontWeight: 700,
                    }}
                  />

                  {/* Red bar at position 0 (display 10) - Critical */}
                  <ReferenceLine
                    x={10}
                    stroke="#ef4444"
                    strokeWidth={4}
                    label={{
                      value: "0",
                      position: "top",
                      fill: "#ef4444",
                      fontSize: 14,
                      fontWeight: 700,
                    }}
                  />

                  <Scatter
                    name="Engagement Points"
                    data={chartData}
                    fill="#8884d8"
                  >
                    {chartData.map((entry, index) => (
                      <Cell
                        key={`cell-${index}`}
                        fill={getScoreColor(entry.engagementOriginal)}
                        stroke={getScoreColor(entry.engagementOriginal)}
                        strokeWidth={2}
                        r={35}
                      />
                    ))}
                    <LabelList
                      dataKey="subContext"
                      position="right"
                      offset={30}
                      style={{ fontSize: 15, fontWeight: 600 }}
                    />
                  </Scatter>
                </ScatterChart>
              </ResponsiveContainer>
            ) : (
              <div className="flex items-center justify-center h-64 text-muted-foreground text-lg font-medium">
                No feedback data available for chart
              </div>
            )}

            {/* Legend for colors */}
            <div className="flex flex-wrap gap-4 mt-4 justify-center">
              <div className="flex items-center gap-2">
                <div className="w-4 h-4 rounded-full bg-green-500" />
                <span className="text-base font-semibold">Score ≥ 7: Keep Strategy</span>
              </div>
              <div className="flex items-center gap-2">
                <div className="w-4 h-4 rounded-full bg-yellow-500" />
                <span className="text-base font-semibold">Score 5-7: Optimize</span>
              </div>
              <div className="flex items-center gap-2">
                <div className="w-4 h-4 rounded-full bg-red-500" />
                <span className="text-base font-semibold">Score &lt; 5: Change Strategy</span>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Section 3: Grouped Data Table by Segment */}
      <Card>
        <CardHeader>
          <CardTitle className="text-xl font-bold">Feedback Details by Segment</CardTitle>
          <CardDescription className="text-base font-medium">
            Each row represents a segment with its latest feedback state. Click View to see all feedbacks.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="rounded-md border">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead className="font-bold text-base">Segment</TableHead>
                  <TableHead className="font-bold text-base">Age</TableHead>
                  <TableHead className="font-bold text-base">Location</TableHead>
                  <TableHead className="font-bold text-base">Gender</TableHead>
                  <TableHead className="font-bold text-base">Latest Score</TableHead>
                  <TableHead className="font-bold text-base">Level</TableHead>
                  <TableHead className="font-bold text-base">AI Recommendation</TableHead>
                  <TableHead className="font-bold text-base">Feedbacks</TableHead>
                  <TableHead className="font-bold text-base">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {groupedFeedbacks.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={9} className="text-center text-muted-foreground text-base font-medium">
                      No feedbacks found
                    </TableCell>
                  </TableRow>
                ) : (
                  groupedFeedbacks.map((group) => {
                    const latestFeedback = group.latestFeedback
                    const eng = Number(latestFeedback.engagement)
                    const score = eng > 1 ? eng : eng * 10
                    const status = getRecommendationStatus(score)

                    return (
                      <TableRow key={group.segmentId}>
                        <TableCell className="font-bold text-base">
                          {latestFeedback.segment?.name || `${latestFeedback.segment?.ageRange}-${latestFeedback.segment?.location}-${latestFeedback.segment?.gender}`}
                        </TableCell>
                        <TableCell className="text-base font-medium">{extractAgeFromSegmentName(latestFeedback.segment?.name || "")}</TableCell>
                        <TableCell className="text-base font-medium">{latestFeedback.segment?.location || "-"}</TableCell>
                        <TableCell className="text-base font-medium">{latestFeedback.segment?.gender || "-"}</TableCell>
                        <TableCell>
                          <span
                            className="font-bold text-lg"
                            style={{ color: getScoreColor(score) }}
                          >
                            {score.toFixed(1)}
                          </span>
                        </TableCell>
                        <TableCell>
                          <Badge
                            variant={
                              latestFeedback.level === "critical_mismatch"
                                ? "destructive"
                                : latestFeedback.level === "excellent" || latestFeedback.level === "good" || latestFeedback.match
                                  ? "default"
                                  : "secondary"
                            }
                            className="text-sm font-bold"
                          >
                            {latestFeedback.level}
                          </Badge>
                        </TableCell>
                        <TableCell>
                          <div className="flex items-center gap-2">
                            {status.icon}
                            <Badge variant={status.variant} className={`${status.className} text-sm font-bold`}>
                              {status.text}
                            </Badge>
                          </div>
                        </TableCell>
                        <TableCell>
                          <Badge variant="outline" className="text-sm font-bold">
                            {group.totalCount} {group.totalCount === 1 ? 'feedback' : 'feedbacks'}
                          </Badge>
                        </TableCell>
                        <TableCell>
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => handleViewSegmentFeedbacks(group.feedbacks)}
                            className="font-semibold"
                          >
                            <Eye className="h-4 w-4 mr-2" />
                            View
                          </Button>
                        </TableCell>
                      </TableRow>
                    )
                  })
                )}
              </TableBody>
            </Table>
          </div>
        </CardContent>
      </Card>

      {/* Segment Feedbacks Modal - Shows all feedbacks for selected segment */}
      <Dialog open={isModalOpen} onOpenChange={setIsModalOpen}>
        <DialogContent className="w-[66vw] min-w-[66vw] max-w-[66vw] max-h-[85vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2 text-xl font-bold">
              <Eye className="h-5 w-5" />
              All Feedbacks for Segment
            </DialogTitle>
            <DialogDescription className="text-base font-medium">
              {selectedSegmentFeedbacks && selectedSegmentFeedbacks.length > 0
                ? `${selectedSegmentFeedbacks[0].segment?.name || "Segment"} - Total: ${selectedSegmentFeedbacks.length} feedbacks`
                : "Segment feedback information"}
            </DialogDescription>
          </DialogHeader>

          {selectedSegmentFeedbacks && selectedSegmentFeedbacks.length > 0 && (() => {
            const sortedFeedbacks = [...selectedSegmentFeedbacks].sort((a, b) => {
              const dateA = a.created_at ? new Date(a.created_at).getTime() : 0
              const dateB = b.created_at ? new Date(b.created_at).getTime() : 0
              return dateB - dateA
            })

            // Get the latest feedback for summary
            const latestFeedback = sortedFeedbacks[0]
            const latestScore = Number(latestFeedback.engagement) > 1 ? Number(latestFeedback.engagement) : Number(latestFeedback.engagement) * 10

            // Group and deduplicate mechanism feedbacks by metricType
            const groupedMechanismFeedbacks: Record<string, {
              metricType: string
              feedbacks: Array<{
                id: string
                awareness: string
                motivation: string
                capability: string
                opportunity: string
                createdAt: string
              }>
            }> = {}

            // Collect all unique mechanism feedbacks across all feedbacks
            sortedFeedbacks.forEach(feedback => {
              if (feedback.mechanismFeedbacks && Array.isArray(feedback.mechanismFeedbacks)) {
                feedback.mechanismFeedbacks.forEach((mf: any) => {
                  const mechanismData = typeof mf === "string" ? JSON.parse(mf) : mf
                  const metricType = mechanismData.metricType || "unknown"

                  if (!groupedMechanismFeedbacks[metricType]) {
                    groupedMechanismFeedbacks[metricType] = {
                      metricType,
                      feedbacks: []
                    }
                  }

                  if (mechanismData.mechanismFeedbacks && Array.isArray(mechanismData.mechanismFeedbacks)) {
                    mechanismData.mechanismFeedbacks.forEach((item: any) => {
                      // Check for duplicates by ID
                      const exists = groupedMechanismFeedbacks[metricType].feedbacks.some(
                        existing => existing.id === item.id
                      )
                      if (!exists && item.id) {
                        groupedMechanismFeedbacks[metricType].feedbacks.push({
                          id: item.id,
                          awareness: item.awareness || "",
                          motivation: item.motivation || "",
                          capability: item.capability || "",
                          opportunity: item.opportunity || "",
                          createdAt: item.createdAt || ""
                        })
                      }
                    })
                  }
                })
              }
            })

            // Sort each group's feedbacks by createdAt descending
            Object.values(groupedMechanismFeedbacks).forEach(group => {
              group.feedbacks.sort((a, b) => {
                const dateA = a.createdAt ? new Date(a.createdAt).getTime() : 0
                const dateB = b.createdAt ? new Date(b.createdAt).getTime() : 0
                return dateB - dateA
              })
            })

            return (
              <div className="space-y-6">
                {/* Segment Info */}
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4 p-4 bg-muted rounded-lg">
                  <div className="space-y-1">
                    <p className="text-sm font-bold text-muted-foreground">Segment Name</p>
                    <p className="text-base font-bold">{selectedSegmentFeedbacks[0].segment?.name || "-"}</p>
                  </div>
                  <div className="space-y-1">
                    <p className="text-sm font-bold text-muted-foreground">Location</p>
                    <p className="text-base font-bold">{selectedSegmentFeedbacks[0].segment?.location || "-"}</p>
                  </div>
                  <div className="space-y-1">
                    <p className="text-sm font-bold text-muted-foreground">Age</p>
                    <p className="text-base font-bold">{extractAgeFromSegmentName(selectedSegmentFeedbacks[0].segment?.name || "")}</p>
                  </div>
                  <div className="space-y-1">
                    <p className="text-sm font-bold text-muted-foreground">Gender</p>
                    <p className="text-base font-bold">{selectedSegmentFeedbacks[0].segment?.gender || "-"}</p>
                  </div>
                </div>

                {/* Section 1: General Feedback Summary (Latest) */}
                <div className="space-y-3">
                  <h4 className="font-bold text-lg border-b pb-2">General Feedback Summary (Latest)</h4>
                  <div className="border rounded-lg p-4 bg-card space-y-4">
                    <div className="flex items-center justify-between">
                      <div className="flex items-center gap-2">
                        <Badge variant="default" className="font-bold">Latest Feedback</Badge>
                        {latestFeedback.created_at && (
                          <span className="text-sm font-semibold text-muted-foreground">
                            {new Date(latestFeedback.created_at).toLocaleString()}
                          </span>
                        )}
                      </div>
                      <div className="flex items-center gap-2">
                        {getRecommendationStatus(latestScore).icon}
                        <Badge variant={getRecommendationStatus(latestScore).variant} className="font-bold">
                          {getRecommendationStatus(latestScore).text}
                        </Badge>
                      </div>
                    </div>

                    {/* Score Metrics Grid */}
                    <div className="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-8 gap-3">
                      <div className="space-y-1">
                        <p className="text-xs font-bold text-muted-foreground">Trait</p>
                        <Badge variant="outline" className="text-sm font-bold">{latestFeedback.trait_checked}</Badge>
                      </div>
                      <div className="space-y-1">
                        <p className="text-xs font-bold text-muted-foreground">Expected</p>
                        <p className="text-sm font-bold">{formatNumber(latestFeedback.expected)}</p>
                      </div>
                      <div className="space-y-1">
                        <p className="text-xs font-bold text-muted-foreground">Actual</p>
                        <p className="text-sm font-bold">{formatNumber(latestFeedback.actual)}</p>
                      </div>
                      <div className="space-y-1">
                        <p className="text-xs font-bold text-muted-foreground">Deviation</p>
                        <p className={`text-sm font-bold ${Number(latestFeedback.deviation) > 0.2 ? "text-red-500" : "text-green-500"}`}>
                          {formatNumber(latestFeedback.deviation)}
                        </p>
                      </div>
                      <div className="space-y-1">
                        <p className="text-xs font-bold text-muted-foreground">Engagement</p>
                        <p className="text-sm font-bold" style={{ color: getScoreColor(latestScore) }}>
                          {formatNumber(latestScore, 1)}
                        </p>
                      </div>
                      <div className="space-y-1">
                        <p className="text-xs font-bold text-muted-foreground">Match</p>
                        <Badge variant={latestFeedback.match ? "default" : "destructive"} className="text-sm font-bold">
                          {latestFeedback.match ? "Yes" : "No"}
                        </Badge>
                      </div>
                      <div className="space-y-1">
                        <p className="text-xs font-bold text-muted-foreground">Level</p>
                        <Badge
                          variant={
                            latestFeedback.level === "critical_mismatch"
                              ? "destructive"
                              : latestFeedback.level === "excellent" || latestFeedback.level === "good"
                                ? "default"
                                : "secondary"
                          }
                          className="text-sm font-bold"
                        >
                          {latestFeedback.level}
                        </Badge>
                      </div>
                      <div className="space-y-1">
                        <p className="text-xs font-bold text-muted-foreground">Total Feedbacks</p>
                        <Badge variant="outline" className="text-sm font-bold">{sortedFeedbacks.length}</Badge>
                      </div>
                    </div>

                    {/* Suggestions */}
                    {latestFeedback.feedback && latestFeedback.feedback.length > 0 && (
                      <div className="pt-3 border-t">
                        <p className="text-sm font-bold text-muted-foreground mb-2">AI Suggestions:</p>
                        <ul className="space-y-1">
                          {latestFeedback.feedback.map((item, idx) => (
                            <li key={idx} className="flex items-start gap-2 text-sm font-medium">
                              <span className="text-primary">•</span>
                              <span>{item}</span>
                            </li>
                          ))}
                        </ul>
                      </div>
                    )}
                  </div>
                </div>

                {/* Section 2: Mechanism Feedbacks Grouped by Type */}
                {Object.keys(groupedMechanismFeedbacks).length > 0 && (
                  <div className="space-y-3">
                    <h4 className="font-bold text-lg border-b pb-2">Mechanism Feedbacks by Type</h4>
                    <div className="space-y-4">
                      {Object.entries(groupedMechanismFeedbacks).map(([metricType, group]) => (
                        <div key={metricType} className="border rounded-lg overflow-hidden">
                          {/* Metric Type Header */}
                          <div className="bg-muted p-3 flex items-center justify-between">
                            <div className="flex items-center gap-2">
                              <Badge variant="secondary" className="font-mono text-sm font-bold">
                                {metricType.replace(/_/g, ' ').toUpperCase()}
                              </Badge>
                              <span className="text-sm text-muted-foreground">
                                ({group.feedbacks.length} unique entries)
                              </span>
                            </div>
                          </div>

                          {/* Feedbacks for this type - show all entries */}
                          <div className="p-3 space-y-2 max-h-[400px] overflow-y-auto">
                            {group.feedbacks.map((item, itemIdx) => (
                              <div key={item.id || itemIdx} className="p-3 rounded-lg bg-muted/30 space-y-2 border">
                                <div className="grid grid-cols-1 md:grid-cols-2 gap-2 text-sm">
                                  {item.awareness && (
                                    <div className="flex items-start gap-2">
                                      <span className="font-bold text-muted-foreground min-w-[90px]">Awareness:</span>
                                      <span className="font-medium">{item.awareness}</span>
                                    </div>
                                  )}
                                  {item.motivation && (
                                    <div className="flex items-start gap-2">
                                      <span className="font-bold text-muted-foreground min-w-[90px]">Motivation:</span>
                                      <span className="font-medium">{item.motivation}</span>
                                    </div>
                                  )}
                                  {item.capability && (
                                    <div className="flex items-start gap-2">
                                      <span className="font-bold text-muted-foreground min-w-[90px]">Capability:</span>
                                      <span className="font-medium">{item.capability}</span>
                                    </div>
                                  )}
                                  {item.opportunity && (
                                    <div className="flex items-start gap-2">
                                      <span className="font-bold text-muted-foreground min-w-[90px]">Opportunity:</span>
                                      <span className="font-medium">{item.opportunity}</span>
                                    </div>
                                  )}
                                </div>
                                {item.createdAt && (
                                  <div className="text-xs font-semibold text-muted-foreground pt-2 border-t flex items-center gap-1">
                                    {new Date(item.createdAt).toLocaleString()}
                                    {itemIdx === 0 && <Badge variant="default" className="ml-2 text-xs">Latest</Badge>}
                                  </div>
                                )}
                              </div>
                            ))}

                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                )}

                {/* Section 3: Feedback History Timeline (Collapsed summary) */}
                <div className="space-y-3">
                  <h4 className="font-bold text-lg border-b pb-2">Feedback History ({sortedFeedbacks.length} entries)</h4>
                  <div className="space-y-2 max-h-[200px] overflow-y-auto">
                    {sortedFeedbacks.map((feedback, index) => {
                      const score = Number(feedback.engagement) > 1 ? Number(feedback.engagement) : Number(feedback.engagement) * 10
                      return (
                        <div key={feedback.id} className="flex items-center justify-between p-2 rounded-lg bg-muted/50 text-sm">
                          <div className="flex items-center gap-3">
                            <span className="font-mono text-muted-foreground">#{sortedFeedbacks.length - index}</span>
                            {index === 0 && <Badge variant="default" className="text-xs">Latest</Badge>}
                            <span className="font-medium" style={{ color: getScoreColor(score) }}>
                              Score: {formatNumber(score, 1)}
                            </span>
                            <Badge variant={feedback.match ? "outline" : "destructive"} className="text-xs">
                              {feedback.match ? "Match" : "Mismatch"}
                            </Badge>
                          </div>
                          {feedback.created_at && (
                            <span className="text-xs text-muted-foreground">
                              {new Date(feedback.created_at).toLocaleString()}
                            </span>
                          )}
                        </div>
                      )
                    })}
                  </div>
                </div>
              </div>
            )
          })()}
        </DialogContent>
      </Dialog>
    </div>
  )
}
