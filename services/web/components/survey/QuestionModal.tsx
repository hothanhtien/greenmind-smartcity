"use client"

import { useState, useEffect } from "react"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { Button } from "@/components/ui/button"
import { useToast } from "@/hooks/use-toast"
import { CheckCircle2, Loader2 } from "lucide-react"
import { attachQuestionSet } from "@/lib/survey"
import { getMyQuestionSets } from "@/lib/question-set"

interface QuestionModalProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  scenarioId: string
  onSuccess?: () => void
}

export function QuestionModal({ open, onOpenChange, scenarioId, onSuccess }: QuestionModalProps) {
  const { toast } = useToast()
  const [isLoading, setIsLoading] = useState(false)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [questionSets, setQuestionSets] = useState<any[]>([])
  const [selectedSetId, setSelectedSetId] = useState<string>("")

  useEffect(() => {
    if (open) {
      loadQuestionSets()
    }
  }, [open])

  const loadQuestionSets = async () => {
    setIsLoading(true)
    try {
      const response = await getMyQuestionSets()
      const sets = response?.data || response || []
      setQuestionSets(Array.isArray(sets) ? sets : [])
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to load question sets",
        variant: "destructive",
      })
    } finally {
      setIsLoading(false)
    }
  }



  const handleConfirm = async () => {
    if (!selectedSetId) {
      toast({ title: "No Set Selected", description: "Please select a question set.", variant: "destructive" })
      return
    }

    setIsSubmitting(true)
    try {
      const response = await attachQuestionSet(scenarioId, selectedSetId)
      console.log('Attach response:', response)
      console.log('Response data:', response.data)
      console.log('QuestionSetId in response:', response.data?.questionSetId)

      const selectedSet = questionSets.find(s => s.id === selectedSetId)
      const questionCount = selectedSet?.items?.length || selectedSet?.questionIds?.length || 0
      toast({
        title: "Question Set Attached",
        description: `Successfully attached "${selectedSet?.name}" with ${questionCount} question(s).`,
      })
      setSelectedSetId("")

      // Small delay to ensure backend is updated
      await new Promise(resolve => setTimeout(resolve, 1000))

      onOpenChange(false)
      if (onSuccess) {
        console.log('Calling onSuccess callback')
        onSuccess()
      }
    } catch (error: any) {
      console.error('Attach error:', error)
      toast({
        title: "Error",
        description: "Failed to attach question set",
        variant: "destructive",
      })
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-2xl">
        <DialogHeader>
          <DialogTitle className="text-xl">Select Question Set</DialogTitle>
          <DialogDescription>Choose a question set to attach to this demographic scenario.</DialogDescription>
        </DialogHeader>

        <div className="space-y-3">
          {isLoading ? (
            <div className="flex items-center justify-center py-8">
              <Loader2 className="h-6 w-6 animate-spin text-muted-foreground" />
            </div>
          ) : questionSets.length === 0 ? (
            <div className="text-sm text-muted-foreground py-4 text-center">
              No question sets available. Create sets in Question Manager first.
            </div>
          ) : (
            <div className="max-h-96 overflow-y-auto space-y-2">
              {questionSets.map((set) => {
                const questionCount = set.items?.length || set.questionIds?.length || 0
                const isSelected = selectedSetId === set.id
                return (
                  <div
                    key={set.id}
                    onClick={() => setSelectedSetId(set.id)}
                    className={`cursor-pointer border rounded-lg p-4 transition-all ${isSelected
                      ? "border-primary bg-primary/5 shadow-sm"
                      : "border-gray-200 hover:border-gray-300 hover:bg-gray-50"
                      }`}
                  >
                    <div className="flex items-start justify-between gap-3">
                      <div className="flex-1">
                        <div className="flex items-center gap-2">
                          {isSelected && <CheckCircle2 className="h-4 w-4 text-primary" />}
                          <div className="font-semibold text-sm">{set.name}</div>
                        </div>
                        <div className="text-xs text-muted-foreground mt-1">
                          {set.description || "No description"}
                        </div>
                      </div>
                      <div className="text-xs font-medium text-muted-foreground bg-gray-100 px-2 py-1 rounded">
                        {questionCount} questions
                      </div>
                    </div>
                  </div>
                )
              })}
            </div>
          )}
        </div>

        <DialogFooter className="gap-2">
          <Button variant="outline" onClick={() => onOpenChange(false)} disabled={isSubmitting}>
            Cancel
          </Button>
          <Button onClick={handleConfirm} disabled={!selectedSetId || isSubmitting}>
            {isSubmitting ? (
              <>
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                Attaching...
              </>
            ) : (
              "Confirm Selection"
            )}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}