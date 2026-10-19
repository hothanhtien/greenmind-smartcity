"use client"

import { useState, useEffect, useMemo } from "react"
import { useRouter } from "next/navigation"
import {
  FileText,
  Filter,
  MoreHorizontal,
  PlusCircle,
  Search,
} from "lucide-react"
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
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { Input } from "@/components/ui/input"
import { Skeleton } from "@/components/ui/skeleton"
// NOTE: Assuming the dialog and its complex form logic are still required.
// In a real refactor, this would likely be moved to its own component or even a separate page.
import { CreateModelDialog } from "./CreateModelDialog" // Placeholder for extracted dialog

interface Model {
  id: string
  ocean: string
  behavior: string
  age: string
  location: string
  gender: string
  keywords: string
  createdAt: string
  updatedAt: string
}

interface Feedback {
  id: string
  model_id: string
  // ... other feedback properties
}

const API_URL = process.env.NEXT_PUBLIC_API_URL || "https://green-api.khoav4.com"

export function ModelsTable() {
  const router = useRouter()
  const [models, setModels] = useState<Model[]>([])
  const [feedbacks, setFeedbacks] = useState<Feedback[]>([])
  const [loading, setLoading] = useState(true)
  const [searchTerm, setSearchTerm] = useState("")
  const [isCreateDialogOpen, setIsCreateDialogOpen] = useState(false)

  // Pagination state
  const [currentPage, setCurrentPage] = useState(1)
  const modelsPerPage = 7

  useEffect(() => {
    fetchData()
  }, [])

  const fetchData = async () => {
    try {
      setLoading(true)
      const token = localStorage.getItem("access_token")
      if (!token) return // Handle no token case

      const [modelsResponse, feedbacksResponse] = await Promise.all([
        fetch(`${API_URL}/models/getAll`, {
          headers: { Authorization: `Bearer ${token}` },
        }),
        fetch(`${API_URL}/models/feedbacks`, {
          headers: { Authorization: `Bearer ${token}` },
        }),
      ])

      const modelsData = await modelsResponse.json()
      const feedbacksData = await feedbacksResponse.json()

      if (modelsData.success) setModels(modelsData.data)
      if (Array.isArray(feedbacksData.data)) setFeedbacks(feedbacksData.data)
    } catch (error) {
      console.error("Error fetching data:", error)
      // Optionally, set an error state to show a message to the user
    } finally {
      setLoading(false)
    }
  }

  const filteredModels = useMemo(() => {
    return models.filter(model =>
      model.behavior.toLowerCase().includes(searchTerm.toLowerCase())
    )
  }, [models, searchTerm])

  const paginatedModels = useMemo(() => {
    const startIndex = (currentPage - 1) * modelsPerPage
    return filteredModels.slice(startIndex, startIndex + modelsPerPage)
  }, [filteredModels, currentPage, modelsPerPage])

  const totalPages = Math.ceil(filteredModels.length / modelsPerPage)

  const getModelFeedbackCount = (modelId: string) => {
    return feedbacks.filter(feedback => feedback.model_id === modelId).length
  }

  const handlePageChange = (page: number) => {
    if (page >= 1 && page <= totalPages) {
      setCurrentPage(page)
    }
  }

  const handleModelCreated = () => {
    setIsCreateDialogOpen(false)
    fetchData() // Refresh data after creation
  }

  if (loading) {
    return <ModelsTableSkeleton />
  }

  // NOTE: CreateModelDialog would need to be extracted into its own file
  // and imported. For this example, I'll assume it exists.
  // The old dialog logic from the original file should be placed there.
  // const [isCreateDialogOpen, setIsCreateDialogOpen] = useState(false) is needed.

  return (
    <>
      <Card>
        <CardHeader>
          <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
            <div>
              <CardTitle>Models</CardTitle>
              <CardDescription>
                Manage and review your AI models.
              </CardDescription>
            </div>
            <div className="flex w-full flex-col items-start gap-2 sm:w-auto sm:flex-row sm:items-center">
              <div className="relative w-full sm:w-auto">
                <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
                <Input
                  type="search"
                  placeholder="Search by behavior..."
                  className="pl-8 sm:w-[300px]"
                  value={searchTerm}
                  onChange={e => setSearchTerm(e.target.value)}
                />
              </div>
              <Button variant="outline" size="sm" className="w-full sm:w-auto">
                <Filter className="mr-2 h-4 w-4" />
                Filter
              </Button>
            </div>
          </div>
        </CardHeader>
        <CardContent>
          {models.length === 0 ? (
            <EmptyState onCreateModel={() => setIsCreateDialogOpen(true)} />
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead className="w-[30%]">Model</TableHead>
                  <TableHead className="hidden md:table-cell">
                    Demographics
                  </TableHead>
                  <TableHead className="hidden lg:table-cell">Keywords</TableHead>
                  <TableHead className="text-center">Feedback</TableHead>
                  <TableHead className="text-right">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {paginatedModels.map(model => (
                  <TableRow key={model.id}>
                    <TableCell>
                      <div className="flex items-center gap-3">
                        <Badge variant="outline">{model.ocean}</Badge>
                        <div className="font-medium truncate">{model.behavior}</div>
                      </div>
                    </TableCell>
                    <TableCell className="hidden md:table-cell text-sm text-muted-foreground">
                      {model.gender}, {model.age}, {model.location}
                    </TableCell>
                    <TableCell className="hidden lg:table-cell text-sm text-muted-foreground truncate max-w-[200px]">
                      {model.keywords || "N/A"}
                    </TableCell>
                    <TableCell className="text-center">
                      <Badge variant="secondary">
                        {getModelFeedbackCount(model.id)}
                      </Badge>
                    </TableCell>
                    <TableCell className="text-right">
                      <DropdownMenu>
                        <DropdownMenuTrigger asChild>
                          <Button size="icon" variant="ghost">
                            <MoreHorizontal className="h-4 w-4" />
                            <span className="sr-only">Actions</span>
                          </Button>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="end">
                          <DropdownMenuLabel>Actions</DropdownMenuLabel>
                          <DropdownMenuItem
                            onClick={() => router.push(`/dashboard/models-verify/${model.id}`)}
                          >
                            View Details
                          </DropdownMenuItem>
                          <DropdownMenuItem>Edit</DropdownMenuItem>
                          <DropdownMenuItem className="text-destructive">
                            Delete
                          </DropdownMenuItem>
                        </DropdownMenuContent>
                      </DropdownMenu>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
        {models.length > 0 && (
          <CardFooter>
            <div className="flex w-full items-center justify-between">
              <div className="text-xs text-muted-foreground">
                Showing {paginatedModels.length} of {filteredModels.length}{" "}
                models
              </div>
              <div className="flex items-center gap-2">
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => handlePageChange(currentPage - 1)}
                  disabled={currentPage === 1}
                >
                  Previous
                </Button>
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => handlePageChange(currentPage + 1)}
                  disabled={currentPage === totalPages}
                >
                  Next
                </Button>
              </div>
            </div>
          </CardFooter>
        )}
      </Card>

      {/* The Create Model Dialog is triggered from the empty state or a main button */}
      {/* For simplicity, assuming CreateModelDialog is a self-contained component */}
      {/* that takes `open` and `onOpenChange` props, and a `onModelCreated` callback. */}
      <CreateModelDialog
        open={isCreateDialogOpen}
        onOpenChange={setIsCreateDialogOpen}
        onModelCreated={handleModelCreated}
      />
    </>
  )
}

function EmptyState({ onCreateModel }: { onCreateModel: () => void }) {
  return (
    <div className="flex flex-col items-center justify-center rounded-lg border-2 border-dashed border-muted bg-background p-12 text-center">
      <FileText className="h-12 w-12 text-muted-foreground" />
      <h3 className="mt-4 text-lg font-semibold">No Models Created</h3>
      <p className="mt-2 text-sm text-muted-foreground">
        Get started by creating your first AI model.
      </p>
      <Button className="mt-6" onClick={onCreateModel}>
        <PlusCircle className="mr-2 h-4 w-4" />
        Create Model
      </Button>
    </div>
  )
}

function ModelsTableSkeleton() {
  return (
    <Card>
      <CardHeader>
        <Skeleton className="h-8 w-48" />
        <Skeleton className="h-4 w-64" />
      </CardHeader>
      <CardContent>
        <div className="space-y-4">
          <Skeleton className="h-10 w-full" />
          <Skeleton className="h-10 w-full" />
          <Skeleton className="h-10 w-full" />
          <Skeleton className="h-10 w-full" />
        </div>
      </CardContent>
      <CardFooter>
        <Skeleton className="h-6 w-32" />
        <div className="flex items-center gap-2 ml-auto">
          <Skeleton className="h-8 w-20" />
          <Skeleton className="h-8 w-20" />
        </div>
      </CardFooter>
    </Card>
  )
}
