"use client"

import { useState, useEffect } from "react"
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Skeleton } from "@/components/ui/skeleton"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { AlertCircle, Eye, MessageSquare } from "lucide-react"

interface User {
  id: string
  fullName: string
  location: string
  age: number
  dateOfBirth: string
  gender: string
}

interface MechanismFeedback {
  id: string
  awareness: string
  motivation: string
  capability: string
  opportunity: string
  createdAt: string
}

interface UserFeedbackData {
  user: User
  metricType: string
  mechanismFeedbacks: MechanismFeedback[]
}

interface GroupedUserFeedback {
  user: User
  feedbacks: {
    metricType: string
    mechanismFeedbacks: MechanismFeedback[]
  }[]
  totalFeedbacks: number
}

export default function DailyFeedbackPage() {
  const [groupedData, setGroupedData] = useState<GroupedUserFeedback[]>([])
  const [loading, setLoading] = useState(true)
  const [selectedUser, setSelectedUser] = useState<GroupedUserFeedback | null>(null)
  const [isDialogOpen, setIsDialogOpen] = useState(false)

  useEffect(() => {
    fetchData()
  }, [])

  const fetchData = async () => {
    try {
      setLoading(true)
      const token = localStorage.getItem("access_token")

      if (!token) {
        console.error("No access token found")
        setLoading(false)
        return
      }

      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL || "https://green-api.khoav4.com"}/behavior-feedbacks/users`, {
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
      })

      const result = await response.json()

      if (result.success && Array.isArray(result.data)) {
        // Group feedbacks by user
        const userMap = new Map<string, GroupedUserFeedback>()

        result.data.forEach((item: UserFeedbackData) => {
          const userId = item.user.id

          if (userMap.has(userId)) {
            const existing = userMap.get(userId)!
            existing.feedbacks.push({
              metricType: item.metricType,
              mechanismFeedbacks: item.mechanismFeedbacks
            })
            existing.totalFeedbacks += item.mechanismFeedbacks.length
          } else {
            userMap.set(userId, {
              user: item.user,
              feedbacks: [{
                metricType: item.metricType,
                mechanismFeedbacks: item.mechanismFeedbacks
              }],
              totalFeedbacks: item.mechanismFeedbacks.length
            })
          }
        })

        setGroupedData(Array.from(userMap.values()))
      }
    } catch (error) {
      console.error("Error fetching data:", error)
    } finally {
      setLoading(false)
    }
  }

  const handleViewFeedback = (userData: GroupedUserFeedback) => {
    setSelectedUser(userData)
    setIsDialogOpen(true)
  }

  const formatMetricType = (metricType: string) => {
    return metricType
      .split('_')
      .map(word => word.charAt(0).toUpperCase() + word.slice(1))
      .join(' ')
  }

  if (loading) {
    return (
      <div className="p-6 space-y-4">
        <Skeleton className="h-10 w-full" />
        <Skeleton className="h-64 w-full" />
      </div>
    )
  }

  return (
    <div className="p-6 space-y-6">
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <MessageSquare className="h-5 w-5" />
            Daily Feedback
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="rounded-md border">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>User ID</TableHead>
                  <TableHead>Full Name</TableHead>
                  <TableHead>Location</TableHead>
                  <TableHead>Age</TableHead>
                  <TableHead>Gender</TableHead>
                  <TableHead>Total Feedbacks</TableHead>
                  <TableHead>Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {groupedData.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={7} className="text-center text-muted-foreground">
                      No feedback data found
                    </TableCell>
                  </TableRow>
                ) : (
                  groupedData.map((item) => (
                    <TableRow key={item.user.id}>
                      <TableCell className="font-mono text-xs">
                        {item.user.id.substring(0, 8)}...
                      </TableCell>
                      <TableCell className="font-medium">
                        {item.user.fullName}
                      </TableCell>
                      <TableCell>{item.user.location}</TableCell>
                      <TableCell>{item.user.age}</TableCell>
                      <TableCell>
                        <Badge variant="outline">
                          {item.user.gender === 'male' ? 'Nam' : item.user.gender === 'female' ? 'Nữ' : item.user.gender}
                        </Badge>
                      </TableCell>
                      <TableCell>
                        <Badge variant="secondary">{item.totalFeedbacks}</Badge>
                      </TableCell>
                      <TableCell>
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => handleViewFeedback(item)}
                        >
                          <Eye className="h-4 w-4 mr-2" />
                          View
                        </Button>
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </div>
        </CardContent>
      </Card>

      <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
        <DialogContent className="max-w-4xl max-h-[80vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>User Feedbacks</DialogTitle>
          </DialogHeader>

          {selectedUser && (
            <div className="space-y-6">
              <div className="grid grid-cols-2 gap-4 p-4 border rounded-lg bg-muted/50">
                <div>
                  <p className="text-sm text-muted-foreground">User ID</p>
                  <p className="font-medium font-mono text-sm">{selectedUser.user.id}</p>
                </div>
                <div>
                  <p className="text-sm text-muted-foreground">Full Name</p>
                  <p className="font-medium">{selectedUser.user.fullName}</p>
                </div>
                <div>
                  <p className="text-sm text-muted-foreground">Location</p>
                  <p className="font-medium">{selectedUser.user.location}</p>
                </div>
                <div>
                  <p className="text-sm text-muted-foreground">Age</p>
                  <p className="font-medium">{selectedUser.user.age}</p>
                </div>
                <div>
                  <p className="text-sm text-muted-foreground">Gender</p>
                  <p className="font-medium">{selectedUser.user.gender === 'male' ? 'Nam' : selectedUser.user.gender === 'female' ? 'Nữ' : selectedUser.user.gender}</p>
                </div>
                <div>
                  <p className="text-sm text-muted-foreground">Date of Birth</p>
                  <p className="font-medium">{new Date(selectedUser.user.dateOfBirth).toLocaleDateString()}</p>
                </div>
              </div>

              {selectedUser.feedbacks.length === 0 ? (
                <Alert>
                  <AlertCircle className="h-4 w-4" />
                  <AlertDescription>
                    No feedback available for this user.
                  </AlertDescription>
                </Alert>
              ) : (
                <div className="space-y-4">
                  <h3 className="font-semibold">Feedback by Metric Type</h3>
                  {selectedUser.feedbacks.map((feedbackGroup, groupIndex) => (
                    <div key={groupIndex} className="border rounded-lg p-4 space-y-3">
                      <div className="flex items-center gap-2">
                        <Badge variant="default" className="bg-blue-500">
                          {formatMetricType(feedbackGroup.metricType)}
                        </Badge>
                        <span className="text-sm text-muted-foreground">
                          ({feedbackGroup.mechanismFeedbacks.length} feedbacks)
                        </span>
                      </div>

                      {feedbackGroup.mechanismFeedbacks.map((mecha, index) => (
                        <div key={mecha.id || index} className="border rounded-lg p-3 bg-muted/30 space-y-2">
                          <div className="flex items-center justify-between">
                            <p className="text-xs font-semibold text-muted-foreground">
                              Feedback {index + 1}
                            </p>
                            <p className="text-xs text-muted-foreground">
                              {new Date(mecha.createdAt).toLocaleString()}
                            </p>
                          </div>
                          <div className="grid grid-cols-1 gap-3 text-sm">
                            <div>
                              <p className="text-muted-foreground text-xs font-medium">Awareness</p>
                              <p className="font-medium">{mecha.awareness}</p>
                            </div>
                            <div>
                              <p className="text-muted-foreground text-xs font-medium">Motivation</p>
                              <p className="font-medium">{mecha.motivation}</p>
                            </div>
                            <div>
                              <p className="text-muted-foreground text-xs font-medium">Capability</p>
                              <p className="font-medium">{mecha.capability}</p>
                            </div>
                            <div>
                              <p className="text-muted-foreground text-xs font-medium">Opportunity</p>
                              <p className="font-medium">{mecha.opportunity}</p>
                            </div>
                          </div>
                        </div>
                      ))}
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}
        </DialogContent>
      </Dialog>
    </div>
  )
}
