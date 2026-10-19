"use client"

import React, { useState, useEffect } from "react"
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Checkbox } from "@/components/ui/checkbox"
import { Input } from "@/components/ui/input"
import { Textarea } from "@/components/ui/textarea"
import { Badge } from "@/components/ui/badge"
import { useToast } from "@/hooks/use-toast"
import { Edit, Trash2, MoreHorizontal, GripVertical } from "lucide-react"
import {
  DropdownMenu,
  DropdownMenuTrigger,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
} from "@/components/ui/dropdown-menu"
import {
  getMyQuestions,
  getMyQuestionSets,
  createQuestionSet as apiCreateQuestionSet,
  updateQuestionSet as apiUpdateQuestionSet,
  deleteQuestionSet as apiDeleteQuestionSet,
  updateQuestion as apiUpdateQuestion,
  deleteQuestion as apiDeleteQuestion,
} from "@/lib/question-set"
import { useRouter } from "next/navigation"

interface QuestionOption {
  id: string
  text: string
  value: string
  order: number
}

interface Template {
  id: string
  name: string
  intent: string
  filled_prompt: string
  question_type: string
}

interface Question {
  id: string
  question: string
  templateId: string
  behaviorInput: string
  trait: string
  template: Template
  questionOptions: QuestionOption[]
}

export default function ManageQuestionsPage() {
  const router = useRouter();
  const [dbQuestions, setDbQuestions] = useState<Question[]>([])
  const [expertSelected, setExpertSelected] = useState<Record<string, boolean>>({})
  const [name, setName] = useState("")
  const [description, setDescription] = useState("")
  const [searchQuery, setSearchQuery] = useState("")
  const [intentFilter, setIntentFilter] = useState("")
  const [loading, setLoading] = useState(true)
  const { toast } = useToast()
  const [draggedId, setDraggedId] = useState<string | null>(null)

  useEffect(() => {
    fetchMyQuestions()
    fetchMyQuestionSets()
  }, [])

  const fetchMyQuestions = async () => {
    try {
      setLoading(true)
      const response = await getMyQuestions()

      if (response.data && Array.isArray(response.data)) {
        setDbQuestions(response.data)
      }
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to load questions from database",
        variant: "destructive",
      })
    } finally {
      setLoading(false)
    }
  }

  const fetchMyQuestionSets = async () => {
    try {
      const response = await getMyQuestionSets()

      if (response.data && Array.isArray(response.data)) {
        setQuestionSets(response.data)
      }
    } catch (error) {
      console.error("Failed to load question sets:", error)
    }
  }

  const onDragStart = (e: React.DragEvent<HTMLDivElement>, id: string) => {
    setDraggedId(id)
    e.dataTransfer.effectAllowed = "move"
    try { e.dataTransfer.setData("text/plain", id) } catch (err) { }
  }

  const onDragOver = (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault()
    e.dataTransfer.dropEffect = "move"
  }

  const onDrop = (e: React.DragEvent<HTMLDivElement>, targetId: string) => {
    e.preventDefault()
    const sourceId = draggedId || e.dataTransfer.getData("text/plain")
    if (!sourceId || sourceId === targetId) {
      setDraggedId(null)
      return
    }

    setExpertQuestions(prev => {
      const copy = [...prev]
      const fromIndex = copy.findIndex(x => x.id === sourceId)
      const toIndex = copy.findIndex(x => x.id === targetId)
      if (fromIndex === -1 || toIndex === -1) return prev
      const [moved] = copy.splice(fromIndex, 1)
      copy.splice(toIndex, 0, moved)
      return copy
    })

    setDraggedId(null)
  }

  const [expertQuestions, setExpertQuestions] = useState<Question[]>([])
  const [editingId, setEditingId] = useState<string | null>(null)
  const [editingData, setEditingData] = useState<Partial<Question>>({})
  const [pendingMoveId, setPendingMoveId] = useState<string | null>(null)
  const [pendingDeleteSetId, setPendingDeleteSetId] = useState<string | null>(null)
  const [pendingDeleteQuestionId, setPendingDeleteQuestionId] = useState<string | null>(null)

  interface QuestionSet {
    id: string
    name: string
    description: string
    ownerId: string
    createdAt: string
    updatedAt: string
    items?: Question[]
    questionIds?: string[]
  }

  const [questionSets, setQuestionSets] = useState<QuestionSet[]>([])

  const toggleExpert = (id: string, value: boolean) => {
    setExpertSelected(prev => ({ ...prev, [id]: value }))
  }

  const selectAllExpert = () => {
    const allSelected: Record<string, boolean> = {}
    expertQuestions.forEach(q => {
      allSelected[q.id] = true
    })
    setExpertSelected(allSelected)
  }

  const clearAllExpert = () => {
    setExpertSelected({})
  }

  const moveDbToExpert = (id: string) => {
    const q = dbQuestions.find(d => d.id === id)
    if (!q) return
    setExpertQuestions(prev => [q, ...prev])
    setDbQuestions(prev => prev.filter(d => d.id !== id))
    setExpertSelected(prev => ({ ...prev }))
    toast({ title: "Moved", description: `Question moved to Expert list` })
  }

  const createQuestionSet = async () => {
    const selectedExpertIds = Object.entries(expertSelected).filter(([_, v]) => v).map(([k]) => k)
    const questionIds = [...selectedExpertIds]

    if (name.trim() === "") {
      toast({ title: "Error", description: "Please enter a name for the question set", variant: "destructive" })
      return
    }

    if (questionIds.length === 0) {
      toast({ title: "Error", description: "Please select at least one question", variant: "destructive" })
      return
    }

    try {
      const payload = {
        name: name.trim(),
        description: description.trim(),
        questionIds,
      }

      const response = await apiCreateQuestionSet(payload)

      if (response.data) {
        setQuestionSets(prev => [response.data, ...prev])
        toast({ title: "Success", description: `Question set "${payload.name}" created (${questionIds.length} questions)` })

        setName("")
        setDescription("")

        setDbQuestions(prev => {
          const expertIds = new Set(expertQuestions.map(q => q.id))
          const filteredPrev = prev.filter(p => !expertIds.has(p.id))
          return [...expertQuestions, ...filteredPrev]
        })

        setExpertQuestions([])
        setExpertSelected({})
      }

      router.push('/dashboard/survey')
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || error.message || "Failed to create question set"
      toast({
        title: "Error",
        description: errorMessage,
        variant: "destructive"
      })
      console.error("Create question set error:", error.response?.data || error)
    }
  }

  const handleDeleteQuestionSet = async (id: string) => {
    try {
      await apiDeleteQuestionSet(id)
      setQuestionSets(prev => prev.filter(s => s.id !== id))
      setPendingDeleteSetId(null)
      toast({ title: "Deleted", description: "Question set removed" })
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to delete question set",
        variant: "destructive"
      })
    }
  }

  const deleteExpertQuestion = (id: string) => {
    const q = expertQuestions.find(q => q.id === id)
    if (q) {
      setDbQuestions(prev => [q, ...prev])
    }

    setExpertQuestions(prev => prev.filter(q => q.id !== id))

    setExpertSelected(prev => {
      const copy = { ...prev }
      delete copy[id]
      return copy
    })

    toast({ title: "Moved", description: "Question moved back to Database Questions" })
  }

  const promptMoveBack = (id: string) => {
    setPendingDeleteQuestionId(id)
  }

  const confirmDeleteQuestion = async () => {
    if (!pendingDeleteQuestionId) return

    try {
      await apiDeleteQuestion(pendingDeleteQuestionId)
      setExpertQuestions(prev => prev.filter(q => q.id !== pendingDeleteQuestionId))
      toast({ title: "Deleted", description: "Question has been deleted successfully" })
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to delete question",
        variant: "destructive",
      })
    } finally {
      setPendingDeleteQuestionId(null)
    }
  }

  const startEdit = (q: Question) => {
    setEditingId(q.id)
    setEditingData({ ...q })
  }

  const cancelEdit = () => {
    setEditingId(null)
    setEditingData({})
  }

  const saveEdit = async () => {
    if (!editingId) return

    try {
      await apiUpdateQuestion(editingId, { question: editingData.question || "" })
      setExpertQuestions(prev => prev.map(q => (q.id === editingId ? { ...(q as Question), ...(editingData as Question) } : q)))
      setEditingId(null)
      setEditingData({})
      toast({ title: "Saved", description: "Question has been updated successfully" })
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to update question",
        variant: "destructive",
      })
    }
  }

  const selectedExpertQuestions = expertQuestions.filter(q => expertSelected[q.id])
  const selectedQuestions = [...selectedExpertQuestions]

  const intentColor = (intent?: string) => {
    switch (intent) {
      case "frequency":
        return "bg-blue-100 text-blue-800"
      case "yesno":
        return "bg-green-100 text-green-800"
      case "rating":
        return "bg-yellow-100 text-yellow-800"
      case "choice":
        return "bg-indigo-100 text-indigo-800"
      case "binary":
        return "bg-rose-100 text-rose-800"
      default:
        return "bg-gray-200 text-gray-800"
    }
  }

  const allIntents = Array.from(new Set(dbQuestions.concat(expertQuestions).map(q => q.template?.intent).filter(Boolean))).filter(Boolean)

  const filteredDbQuestions = dbQuestions.filter(q => {
    const matchQuery = searchQuery.trim() === "" || (q.template?.name + " " + q.question).toLowerCase().includes(searchQuery.toLowerCase())
    const matchIntent = intentFilter === "" || q.template?.intent === intentFilter
    return matchQuery && matchIntent
  })

  const filteredExpertQuestions = expertQuestions.filter(q => {
    const matchQuery = searchQuery.trim() === "" || (q.template?.name + " " + q.question).toLowerCase().includes(searchQuery.toLowerCase())
    const matchIntent = intentFilter === "" || q.template?.intent === intentFilter
    return matchQuery && matchIntent
  })

  return (
    <div className="min-h-screen p-8 bg-gradient-to-br from-gray-50 to-gray-100/50">
      <div className="mx-auto max-w-7xl space-y-6">
        <div className="space-y-3">
          <div className="flex items-center justify-between gap-4">
            <div>
              <h1 className="text-3xl font-bold">Question Management</h1>
              <p className="text-sm text-muted-foreground">Select questions from Database and Expert to create a new question set</p>
            </div>
            <div className="hidden md:flex items-center gap-2">
              <Input placeholder="Search questions..." value={searchQuery} onChange={(e) => setSearchQuery(e.target.value)} className="w-[320px]" />
              <select value={intentFilter} onChange={(e) => setIntentFilter(e.target.value)} className="text-sm border rounded px-2 py-1">
                <option value="">All intents</option>
                {allIntents.map(i => (<option key={i} value={i}>{i}</option>))}
              </select>
            </div>
          </div>
          <div className="md:hidden">
            <Input placeholder="Search questions..." value={searchQuery} onChange={(e) => setSearchQuery(e.target.value)} />
          </div>
        </div>

        <div className="grid gap-6 lg:grid-cols-2">
          <Card className="shadow-lg rounded-xl overflow-hidden">
            <CardHeader>
              <CardTitle>Expert Questions</CardTitle>
              <CardDescription>Questions from the database</CardDescription>
            </CardHeader>
            <CardContent className="p-4">
              {loading ? (
                <div className="flex items-center justify-center h-[520px]">
                  <div className="text-sm text-muted-foreground">Loading questions...</div>
                </div>
              ) : (
                <div className="space-y-3 max-h-[520px] overflow-y-auto">
                  {filteredDbQuestions.map((q) => (
                    <div key={q.id} className="flex items-start gap-3 p-3 rounded-lg bg-white border border-gray-200 hover:border-blue-300 hover:shadow-md transition transform hover:-translate-y-0.5">
                      <div className="flex-1">
                        <div className="flex items-center gap-3 mb-1 justify-between">
                          <div className="flex items-center gap-2">
                            <Badge className={`text-xs ${intentColor(q.template?.intent)}`}>{q.template?.intent}</Badge>
                            <div className="font-medium">{q.template?.name}</div>
                          </div>
                          <div>
                            <Button size="sm" variant="ghost" onClick={() => moveDbToExpert(q.id)}>Add</Button>
                          </div>
                        </div>
                        <div className="text-sm text-muted-foreground italic">{q.question}</div>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </CardContent>
          </Card>

          <Card className="shadow-lg rounded-xl overflow-hidden">
            <CardHeader>
              <div className="flex items-center justify-between gap-4">
                <div>
                  <CardTitle>Question Management</CardTitle>
                  <CardDescription>Manage, edit, reorder and delete questions</CardDescription>
                </div>
                <div className="flex items-center gap-2">
                  <Button size="sm" variant="outline" onClick={selectAllExpert}>Select All</Button>
                  <Button size="sm" variant="outline" onClick={clearAllExpert}>Clear</Button>
                </div>
              </div>
            </CardHeader>
            <CardContent className="p-4">
              <div className="space-y-3 max-h-[520px] overflow-y-auto">
                {filteredExpertQuestions.map((q) => (
                  <div
                    key={q.id}
                    draggable
                    onDragStart={(e) => onDragStart(e, q.id)}
                    onDragOver={onDragOver}
                    onDrop={(e) => onDrop(e, q.id)}
                    className={`flex items-start gap-3 p-3 rounded-lg border-2 transition-all transform ${draggedId === q.id
                      ? "opacity-100 bg-blue-50 border-solid border-blue-500 shadow-lg scale-102"
                      : "bg-white border-transparent hover:border-blue-300 hover:shadow-md hover:-translate-y-0.5 cursor-grab active:cursor-grabbing"
                      }`}>
                    <div className="flex items-center gap-2">
                      <div title="Drag to reorder">
                        <GripVertical className="w-4 h-4 text-gray-400 flex-shrink-0" />
                      </div>
                      <Checkbox id={`exp-${q.id}`} checked={expertSelected[q.id]} onCheckedChange={(v) => toggleExpert(q.id, v as boolean)} />
                    </div>

                    <div className="flex-1">
                      <div className="flex items-center gap-2 mb-1 justify-between">
                        <div className="flex items-center gap-2">
                          <Badge className={`text-xs ${intentColor(q.template?.intent)}`}>{q.template?.intent}</Badge>
                          <div className="font-medium">{q.template?.name}</div>
                        </div>

                        <div className="flex items-center gap-2">
                          <DropdownMenu>
                            <DropdownMenuTrigger asChild>
                              <Button variant="ghost" size="sm" title="Actions" className="w-8 h-8 rounded-full">
                                <MoreHorizontal className="w-4 h-4" />
                              </Button>
                            </DropdownMenuTrigger>
                            <DropdownMenuContent align="end" className="w-52">
                              <DropdownMenuLabel>Actions</DropdownMenuLabel>
                              <DropdownMenuSeparator />

                              <DropdownMenuItem onClick={() => startEdit(q)}>
                                <Edit className="mr-2 h-4 w-4" />
                                Edit
                              </DropdownMenuItem>
                              <DropdownMenuItem onClick={() => promptMoveBack(q.id)} variant="destructive">
                                <Trash2 className="mr-2 h-4 w-4" />
                                Delete
                              </DropdownMenuItem>
                            </DropdownMenuContent>
                          </DropdownMenu>
                        </div>
                      </div>

                      {editingId === q.id ? (
                        <div className="space-y-2">
                          <Input value={editingData.template?.name || ""} onChange={(e) => setEditingData(prev => ({ ...prev, template: { ...prev.template!, name: e.target.value } }))} placeholder="Template name" />
                          <Textarea value={editingData.question || ""} onChange={(e) => setEditingData(prev => ({ ...prev, question: e.target.value }))} placeholder="Question" />
                          <div className="flex items-center gap-2">
                            <Input value={editingData.template?.intent || ""} onChange={(e) => setEditingData(prev => ({ ...prev, template: { ...prev.template!, intent: e.target.value } }))} placeholder="intent" />
                            <Input value={editingData.trait || ""} onChange={(e) => setEditingData(prev => ({ ...prev, trait: e.target.value }))} placeholder="trait" />
                          </div>
                          <div className="flex gap-2 pt-2">
                            <Button size="sm" onClick={saveEdit} className="bg-green-600">Save</Button>
                            <Button size="sm" onClick={cancelEdit} variant="outline">Cancel</Button>
                          </div>
                        </div>
                      ) : (
                        <>
                          <div className="text-sm text-muted-foreground italic">{q.question}</div>
                          <div className="text-xs text-muted-foreground mt-2">ID: {q.id}</div>
                        </>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>
        </div>

        <Card>
          <CardHeader>
            <CardTitle>Create New Question Set</CardTitle>
            <CardDescription>Enter name, description and create a question set from selected questions</CardDescription>
          </CardHeader>
          <CardContent className="p-4 space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <Input placeholder="Question set name" value={name} onChange={(e) => setName(e.target.value)} />
              <Textarea placeholder="Description (optional)" value={description} onChange={(e) => setDescription(e.target.value)} />
            </div>

            <div className="flex items-center justify-between">
              <div className="text-sm text-muted-foreground">Selected total: {(Object.values(expertSelected).filter(Boolean).length)} questions</div>
              <Button onClick={createQuestionSet} className="bg-gradient-to-r from-green-500 to-green-600 hover:from-green-600 hover:to-green-700">Create Question Set</Button>
            </div>
          </CardContent>
        </Card>

        <Card className="shadow-md rounded-lg">
          <CardHeader>
            <CardTitle>Selected Questions</CardTitle>
            <CardDescription>Preview of selected questions for the new set</CardDescription>
          </CardHeader>
          <CardContent className="p-4">
            {selectedQuestions.length === 0 ? (
              <div className="text-sm text-muted-foreground">No questions selected yet</div>
            ) : (
              <div>
                <div className="flex items-center justify-between mb-4">
                  <div>
                    <div className="text-sm font-medium">Question Set: {name || "<name>"}</div>
                    <div className="text-xs text-muted-foreground">{description}</div>
                  </div>
                  <div className="text-sm text-muted-foreground">Total: <span className="font-semibold text-700">{selectedQuestions.length}</span></div>
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
                  {selectedQuestions.map((q) => (
                    <div key={q.id} className="bg-white border border-gray-200 rounded-lg p-3 shadow-sm hover:shadow-md transition">
                      <div className="flex items-start justify-between gap-2">
                        <div className="flex items-center gap-2">
                          <Badge className={`text-xs ${intentColor(q.template?.intent)}`}>{q.template?.intent}</Badge>
                          <div className="font-medium">{q.template?.name}</div>
                        </div>
                        <div className="text-right">
                          <div className="text-xs text-muted-foreground">Drag to reorder</div>
                        </div>
                      </div>

                      <div className="mt-2 text-sm text-muted-foreground line-clamp-3">{q.question}</div>
                      <div className="mt-3 flex items-center justify-between text-xs text-muted-foreground">
                        <div>ID: <span className="font-mono text-[11px]">{q.id.slice(0, 8)}…</span></div>
                        <div className="flex items-center gap-2">
                          <div className="text-xs text-muted-foreground">Trait: <span className="font-medium">{q.trait}</span></div>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </CardContent>
        </Card>

        <Card className="mt-4">
          <CardHeader>
            <CardTitle>Saved Question Sets</CardTitle>
            <CardDescription>List of created question sets (preview)</CardDescription>
          </CardHeader>
          <CardContent className="p-4">
            {questionSets.length === 0 ? (
              <div className="text-sm text-muted-foreground">No saved question sets yet</div>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {questionSets.map((set) => (
                  <div key={set.id} className="bg-white border rounded-lg p-4 shadow-sm hover:shadow-md transition">
                    <div className="flex items-start justify-between gap-4">
                      <div>
                        <div className="text-sm font-semibold">{set.name}</div>
                        <div className="text-xs text-muted-foreground">{set.description || <span className="italic text-muted-foreground">No description</span>}</div>
                      </div>
                      <div className="flex flex-col items-end gap-2">
                        <div className="text-xs text-muted-foreground">{set.items?.length || set.questionIds?.length || 0} questions</div>
                        <div className="flex items-center gap-2">
                          <Button size="sm" variant="ghost" onClick={() => navigator.clipboard.writeText(set.id)}>
                            Copy ID
                          </Button>
                          <Button size="sm" variant="destructive" onClick={() => setPendingDeleteSetId(set.id)}>
                            <Trash2 className="w-4 h-4 mr-1" />
                            Delete
                          </Button>
                        </div>
                      </div>
                    </div>

                    <div className="mt-3 flex flex-wrap gap-2">
                      {set.items?.map((question) => (
                        <div key={question.id} className="px-3 py-1 rounded-full bg-gray-100 text-sm flex items-center gap-2">
                          <Badge className={`text-xs ${intentColor(question.template?.intent)}`}>{question.template?.intent ?? "-"}</Badge>
                          <div className="truncate max-w-[160px]">{question.question}</div>
                        </div>
                      ))}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>

        {pendingMoveId && (() => {
          const q = expertQuestions.find(x => x.id === pendingMoveId) || dbQuestions.find(x => x.id === pendingMoveId)
          return (
            <div className="fixed inset-0 z-50 flex items-center justify-center">
              <div className="absolute inset-0 bg-black/40" onClick={() => setPendingMoveId(null)} />

              <div className="relative w-full max-w-lg px-4">
                <Card className="shadow-2xl">
                  <CardHeader>
                    <CardTitle>Delete Question</CardTitle>
                    <CardDescription>Are you sure you want to delete this question?</CardDescription>
                  </CardHeader>
                  <CardContent>
                    {q ? (
                      <div className="space-y-4">
                        <div className="font-medium text-lg">{q.template?.name}</div>
                        <div className="text-sm text-muted-foreground italic line-clamp-3">{q.question}</div>
                        <div className="text-xs text-muted-foreground">ID: <span className="font-mono">{q.id}</span></div>

                        <div className="flex items-center justify-end gap-3 pt-6 border-t mt-4">
                          <Button
                            variant="outline"
                            onClick={() => setPendingMoveId(null)}
                            className="min-w-24"
                          >
                            Cancel
                          </Button>
                          <Button
                            variant="destructive"
                            onClick={() => { if (pendingMoveId) deleteExpertQuestion(pendingMoveId); setPendingMoveId(null) }}
                            className="min-w-24"
                          >
                            <Trash2 className="w-4 h-4 mr-2" />
                            Delete
                          </Button>
                        </div>
                      </div>
                    ) : (
                      <div>Question not found.</div>
                    )}
                  </CardContent>
                </Card>
              </div>
            </div>
          )
        })()}

        {pendingDeleteSetId && (() => {
          const set = questionSets.find(s => s.id === pendingDeleteSetId)
          return (
            <div className="fixed inset-0 z-50 flex items-center justify-center">
              <div className="absolute inset-0 bg-black/40" onClick={() => setPendingDeleteSetId(null)} />

              <div className="relative w-full max-w-lg px-4">
                <Card className="shadow-2xl">
                  <CardHeader className="bg-red-50 border-b">
                    <CardTitle className="text-red-900 flex items-center gap-2">
                      <Trash2 className="w-5 h-5" />
                      Delete Question Set
                    </CardTitle>
                    <CardDescription className="text-red-700">
                      This action cannot be undone. All questions in this set will be permanently removed.
                    </CardDescription>
                  </CardHeader>
                  <CardContent className="pt-6">
                    {set ? (
                      <div className="space-y-4">
                        <div className="bg-gray-50 rounded-lg p-4 border space-y-3">
                          <div className="flex items-center justify-between">
                            <span className="text-sm font-medium text-gray-700">Questions to be deleted:</span>
                            <Badge variant="outline" className="shrink-0">
                              {set.items?.length || 0} questions
                            </Badge>
                          </div>

                          <div className="space-y-2 max-h-64 overflow-y-auto">
                            {set.items?.map((question, index) => (
                              <div key={question.id} className="bg-white rounded-md p-3 border border-gray-200">
                                <div className="flex items-start gap-3">
                                  <span className="text-xs font-medium text-gray-500 mt-0.5">#{index + 1}</span>
                                  <div className="flex-1 min-w-0">
                                    <p className="text-sm text-gray-900 line-clamp-2">{question.question}</p>
                                    <div className="flex gap-2 mt-2">
                                      <Badge className={`text-xs ${intentColor(question.template?.intent)}`}>
                                        {question.template?.intent}
                                      </Badge>
                                      <Badge className="text-xs bg-purple-100 text-purple-800">
                                        {question.trait}
                                      </Badge>
                                    </div>
                                  </div>
                                </div>
                              </div>
                            ))}
                          </div>
                        </div>

                        <div className="flex items-center justify-end gap-3 pt-4">
                          <Button
                            variant="outline"
                            onClick={() => setPendingDeleteSetId(null)}
                            className="min-w-28"
                          >
                            Cancel
                          </Button>
                          <Button
                            variant="destructive"
                            onClick={() => { if (pendingDeleteSetId) handleDeleteQuestionSet(pendingDeleteSetId) }}
                            className="min-w-28"
                          >
                            <Trash2 className="w-4 h-4 mr-2" />
                            Delete Set
                          </Button>
                        </div>
                      </div>
                    ) : (
                      <div>Question set not found.</div>
                    )}
                  </CardContent>
                </Card>
              </div>
            </div>
          )
        })()}

        {pendingDeleteQuestionId && (() => {
          const question = expertQuestions.find(q => q.id === pendingDeleteQuestionId)
          return (
            <div className="fixed inset-0 z-50 flex items-center justify-center">
              <div className="absolute inset-0 bg-black/40" onClick={() => setPendingDeleteQuestionId(null)} />

              <div className="relative w-full max-w-lg px-4">
                <Card className="shadow-2xl">
                  <CardHeader className="bg-red-50 border-b">
                    <CardTitle className="text-red-900 flex items-center gap-2">
                      <Trash2 className="w-5 h-5" />
                      Delete Question
                    </CardTitle>
                    <CardDescription className="text-red-700">
                      This action cannot be undone. The question will be permanently deleted.
                    </CardDescription>
                  </CardHeader>
                  <CardContent className="pt-6">
                    {question ? (
                      <div className="space-y-4">
                        <div className="bg-gray-50 rounded-lg p-4 border space-y-2">
                          <div className="text-sm font-medium text-gray-700">Question to be deleted:</div>
                          <p className="text-sm text-gray-900 line-clamp-3">{question.question}</p>
                          <div className="flex gap-2 mt-2">
                            <Badge className={`text-xs ${intentColor(question.template?.intent)}`}>
                              {question.template?.intent}
                            </Badge>
                            <Badge variant="outline" className="text-xs">
                              {question.template?.name}
                            </Badge>
                          </div>
                        </div>

                        <div className="flex items-center justify-end gap-3 pt-4">
                          <Button
                            variant="outline"
                            onClick={() => setPendingDeleteQuestionId(null)}
                            className="min-w-28"
                          >
                            Cancel
                          </Button>
                          <Button
                            variant="destructive"
                            onClick={confirmDeleteQuestion}
                            className="min-w-28"
                          >
                            <Trash2 className="w-4 h-4 mr-2" />
                            Delete
                          </Button>
                        </div>
                      </div>
                    ) : (
                      <div>Question not found.</div>
                    )}
                  </CardContent>
                </Card>
              </div>
            </div>
          )
        })()}
      </div>
    </div>
  )
}
