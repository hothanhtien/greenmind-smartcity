"use client"

import { useState, useEffect } from "react"
import { useRouter } from "next/navigation"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Loader2, Save } from "lucide-react"
import { Sparkles } from "lucide-react"
import { useToast } from "@/hooks/use-toast"
import { getAllModels, generateTemplate as apiGenerateTemplate, createTemplates, combineQuestion, createQuestions } from "@/lib/auth"
import { Checkbox } from "@/components/ui/checkbox"

interface Model {
  id: string
  ocean: string
  behavior: string
  age: string
  location: string
  gender: string
  keywords: string
}

interface Template {
  id: string
  name: string
  description: string
  intent: string
  prompt: string
  question_type: string
  filled_prompt: string
  answer: {
    type: string
    scale?: number[]
    labels?: string[]
    options?: string[]
  }
  placeholders?: {
    required: string[]
    optional: string[]
    used_placeholders: string[]
  }
}

interface GeneratedTemplateResponse {
  input: Model
  templates: Template[]
}

export default function QuestionBuilderPage() {
  const router = useRouter()
  const [models, setModels] = useState<Model[]>([])
  const [loading, setLoading] = useState(false)
  const [loadingModels, setLoadingModels] = useState(true)
  const [generatingModelId, setGeneratingModelId] = useState<number | null>(null)
  const [templates, setTemplates] = useState<Template[]>([])
  const [selectedModel, setSelectedModel] = useState<Model | null>(null)
  const [saving, setSaving] = useState(false)
  const [selectedTemplates, setSelectedTemplates] = useState<Template[]>([])
  const [generatedQuestions, setGeneratedQuestions] = useState<any[]>([])
  const [savingQuestions, setSavingQuestions] = useState(false)

  const { toast } = useToast()

  useEffect(() => {
    loadModels()
  }, [])

  const loadModels = async () => {
    try {
      setLoadingModels(true)
      const response = await getAllModels()


      const modelsData = response.data || response || []
      setModels(modelsData)
    } catch (error) {
      toast({
        title: "Error",
        description: "Unable to load model list",
        variant: "destructive",
      })
    } finally {
      setLoadingModels(false)
    }
  }

  const generateTemplate = async (model: Model, index: number) => {
    try {
      setGeneratingModelId(index)
      setLoading(true)
      setSelectedModel(model)

      const data: GeneratedTemplateResponse = await apiGenerateTemplate(model)
      setTemplates(data.templates)

      toast({
        title: "Success",
        description: `Created ${data.templates.length} templates`,
      })
    } catch (error) {
      toast({
        title: "Error",
        description: "Unable to create template",
        variant: "destructive",
      })
    } finally {
      setLoading(false)
      setGeneratingModelId(null)
    }
  }

  const saveTemplates = async () => {
    if (templates.length === 0) return

    if (!selectedModel?.id) {
      toast({
        title: "Error",
        description: "No model selected",
        variant: "destructive",
      })
      return
    }

    try {
      setSaving(true)

      // Add trait (ocean) and modelId to each template
      const templatesWithTraitAndModel = templates.map(template => ({
        ...template,
        trait: selectedModel.ocean,
        modelId: selectedModel.id
      }))

      const payload = {
        templates: templatesWithTraitAndModel,
        defaultModelId: selectedModel.id
      }

      const data = await createTemplates(payload)

      toast({
        title: "Success",
        description: data.message || `Saved ${data.count || templates.length} templates`,
      })

      setTemplates([])
    } catch (error: any) {
      toast({
        title: "Error",
        description: error.message || "Unable to save templates",
        variant: "destructive",
      })
    } finally {
      setSaving(false)
    }
  }

  const handleTemplateSelect = (template: Template, isSelected: boolean) => {
    if (isSelected) {
      setSelectedTemplates(prev => [...prev, template])
    } else {
      setSelectedTemplates(prev => prev.filter(t => t.id !== template.id))
    }
  }

  const generateQuestions = async () => {
    if (selectedTemplates.length === 0) {
      toast({
        title: "Error",
        description: "Please select at least one template to generate questions",
        variant: "destructive",
      })
      return
    }

    if (!selectedModel) {
      toast({
        title: "Error",
        description: "No model selected",
        variant: "destructive",
      })
      return
    }

    try {
      const payload = {
        input: selectedModel,
        templates: selectedTemplates
      }

      const response = await combineQuestion(payload)

      const questionsData = response.questions || response.data || response || []

      const questionsWithModel = questionsData.map((q: any) => ({
        ...q,
        model: selectedModel
      }))

      setGeneratedQuestions(questionsWithModel)

      toast({
        title: "Success",
        description: `Generated ${questionsData.length} questions from ${selectedTemplates.length} templates`,
      })
    } catch (error) {
      toast({
        title: "Error",
        description: "Unable to generate questions from templates",
        variant: "destructive",
      })
    }
  }

  const saveGeneratedQuestions = async () => {
    if (generatedQuestions.length === 0) return

    if (!selectedModel?.id) {
      toast({
        title: "Error",
        description: "No model selected or model has no ID",
        variant: "destructive",
      })
      return
    }

    setSavingQuestions(true)
    try {
      const questionsPayload = {
        questions: generatedQuestions.map(question => {
          let trait = selectedModel?.ocean?.charAt(0).toUpperCase();
          if (!trait && question.intent) {
            trait = question.intent.charAt(0).toUpperCase();
          }

          return {
            id: question.id,
            name: question.name,
            intent: question.intent,
            question_type: question.question_type,
            filled_prompt: question.filled_prompt,
            answer: question.answer,
            trait: trait,
            modelId: selectedModel.id
          }
        }),
        defaultModelId: selectedModel.id
      }

      const response = await createQuestions(questionsPayload)

      toast({
        title: "Success",
        description: `Saved ${generatedQuestions.length} questions`,
      })

      setGeneratedQuestions([])
      setSelectedTemplates([])

      router.push('/dashboard/questions-manage')
    } catch (error) {
      toast({
        title: "Error",
        description: "Unable to save questions",
        variant: "destructive",
      })
    } finally {
      setSavingQuestions(false)
    }
  }

  const getIntentBadgeColor = (intent: string) => {
    switch (intent) {
      case "frequency":
        return "bg-blue-500"
      case "yesno":
        return "bg-green-500"
      case "likert5":
        return "bg-purple-500"
      case "rating":
        return "bg-orange-500"
      default:
        return "bg-gray-500"
    }
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-50 to-gray-100/50 p-8">
      <div className="mx-auto max-w-7xl space-y-8">
        <div className="space-y-2">
          <h1 className="text-3xl font-bold tracking-tight">Question Builder</h1>
          <p className="text-base text-muted-foreground">
            Create question templates from models and generate survey questions
          </p>
        </div>

        <div className="grid gap-8 lg:grid-cols-2">
          <Card className="shadow-lg">
            <CardHeader className="border-b">
              <CardTitle className="text-2xl">Models List</CardTitle>
              <CardDescription className="text-base">Select a model to create question templates</CardDescription>
            </CardHeader>
            <CardContent className="p-6">
              {loadingModels ? (
                <div className="flex items-center justify-center py-12">
                  <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
                </div>
              ) : models.length === 0 ? (
                <div className="text-center py-12 text-muted-foreground">No models available</div>
              ) : (
                <div className="space-y-3 max-h-[650px] overflow-y-auto pr-2">
                  {models.map((model, index) => (
                    <Card key={index} className="border hover:shadow-md transition-shadow">
                      <CardContent className="p-4">
                        <div className="space-y-3">
                          <div className="flex items-start justify-between gap-2">
                            <div className="space-y-2 flex-1">
                              <div className="flex items-center gap-2">
                                <Badge variant="outline" className="font-semibold">{model.ocean}</Badge>
                                <span className="font-semibold text-sm">{model.behavior}</span>
                              </div>
                              <div className="text-sm text-muted-foreground space-y-1">
                                <div>
                                  <span className="font-medium">Age:</span> {model.age} • <span className="font-medium">Gender:</span> {model.gender}
                                </div>
                                <div>
                                  <span className="font-medium">Location:</span> {model.location}
                                </div>
                                <div className="text-xs italic text-gray-500">"{model.keywords}"</div>
                              </div>
                            </div>
                          </div>
                          <Button
                            onClick={() => generateTemplate(model, index)}
                            disabled={loading}
                            className="w-full bg-gradient-to-r from-blue-500 to-blue-600 hover:from-blue-600 hover:to-blue-700"
                            size="sm"
                          >
                            {generatingModelId === index ? (
                              <>
                                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                                Generating...
                              </>
                            ) : (
                              <>
                                <Sparkles className="mr-2 h-4 w-4" />
                                Generate Template
                              </>
                            )}
                          </Button>
                        </div>
                      </CardContent>
                    </Card>
                  ))}
                </div>
              )}
            </CardContent>
          </Card>

          <Card className="shadow-lg">
            <CardHeader className="border-b">
              <div className="flex items-center justify-between gap-4">
                <div className="flex-1">
                  <CardTitle className="text-2xl">Generated Templates</CardTitle>
                  <CardDescription className="text-base">
                    {templates.length > 0 ? `${templates.length} templates from selected model` : "No templates yet"}
                  </CardDescription>
                </div>
                {templates.length > 0 && (
                  <Button
                    onClick={saveTemplates}
                    disabled={saving}
                    className="bg-gradient-to-r from-green-500 to-green-600 hover:from-green-600 hover:to-green-700"
                    size="sm"
                  >
                    {saving ? (
                      <>
                        <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                        Saving...
                      </>
                    ) : (
                      <>
                        <Save className="mr-2 h-4 w-4" />
                        Save
                      </>
                    )}
                  </Button>
                )}
              </div>
            </CardHeader>
            <CardContent className="p-6">
              {selectedModel && templates.length > 0 && (
                <div className="mb-4 p-3 bg-blue-50 rounded-lg border border-blue-200">
                  <div className="text-sm font-semibold mb-1 text-blue-900">Selected Model:</div>
                  <div className="text-xs text-blue-800">
                    {selectedModel.ocean} • {selectedModel.behavior} • {selectedModel.age} years • {selectedModel.gender} •{" "}
                    {selectedModel.location}
                  </div>
                </div>
              )}

              {templates.length === 0 ? (
                <div className="text-center py-16 text-muted-foreground">
                  <Sparkles className="h-12 w-12 mx-auto mb-3 opacity-30" />
                  <p className="text-base">Select a model and click "Generate Template"</p>
                </div>
              ) : (
                <div className="space-y-4">
                  {/* Templates list */}
                  <div className="space-y-3 max-h-[520px] overflow-y-auto pr-2">
                    {templates.map((template, index) => (
                      <Card key={index} className="border hover:shadow-sm transition-shadow">
                        <CardContent className="p-4">
                          <div className="space-y-2">
                            <div className="flex items-start gap-3">
                              <Checkbox
                                id={`template-${index}`}
                                checked={selectedTemplates.some(t => t.id === template.id)}
                                onCheckedChange={(checked) => handleTemplateSelect(template, checked as boolean)}
                                className="mt-1"
                              />
                              <div className="flex-1">
                                <div className="flex items-center gap-2 mb-1 flex-wrap">
                                  <Badge className={getIntentBadgeColor(template.intent)}>{template.intent}</Badge>
                                  <span className="font-medium text-sm">{template.name}</span>
                                </div>
                                <p className="text-xs text-muted-foreground mb-2">{template.description}</p>
                              </div>
                            </div>

                            <div className="bg-gray-100 p-2 rounded text-sm ml-7 italic">{template.filled_prompt}</div>

                            <div className="text-xs space-y-1 ml-7">
                              <div className="font-semibold text-gray-700">Answer Type:</div>
                              {template.answer.type === "scale" && (
                                <div className="flex flex-wrap gap-1">
                                  {template.answer.labels?.map((label, i) => (
                                    <Badge key={i} variant="outline" className="text-xs">
                                      {template.answer.scale?.[i]}: {label}
                                    </Badge>
                                  ))}
                                </div>
                              )}
                              {template.answer.type === "binary" && (
                                <div className="flex gap-1">
                                  {template.answer.options?.map((option, i) => (
                                    <Badge key={i} variant="outline" className="text-xs">
                                      {option}
                                    </Badge>
                                  ))}
                                </div>
                              )}
                            </div>
                          </div>
                        </CardContent>
                      </Card>
                    ))}
                  </div>

                  {/* Generate Questions Button */}
                  {selectedTemplates.length > 0 && (
                    <div className="flex justify-center pt-4 border-t mt-4">
                      <Button
                        onClick={generateQuestions}
                        disabled={selectedTemplates.length === 0}
                        size="lg"
                        className="w-full bg-gradient-to-r from-purple-500 to-purple-600 hover:from-purple-600 hover:to-purple-700"
                      >
                        <Sparkles className="mr-2 h-4 w-4" />
                        Generate Questions ({selectedTemplates.length})
                      </Button>
                    </div>
                  )}
                </div>
              )}
            </CardContent>
          </Card>
        </div>

        {/* Generated Questions Section */}
        <Card className="shadow-lg">
          <CardHeader className="border-b">
            <div className="flex items-center justify-between gap-4">
              <div className="flex-1">
                <CardTitle className="text-2xl">Generated Questions</CardTitle>
                <CardDescription className="text-base">
                  {generatedQuestions.length > 0
                    ? `${generatedQuestions.length} questions generated`
                    : "No questions yet"}
                </CardDescription>
              </div>
              {generatedQuestions.length > 0 && (
                <Button
                  onClick={saveGeneratedQuestions}
                  disabled={savingQuestions}
                  className="bg-gradient-to-r from-green-500 to-green-600 hover:from-green-600 hover:to-green-700"
                  size="sm"
                >
                  {savingQuestions ? (
                    <>
                      <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                      Saving...
                    </>
                  ) : (
                    <>
                      <Save className="mr-2 h-4 w-4" />
                      Save Questions
                    </>
                  )}
                </Button>
              )}
            </div>
          </CardHeader>
          <CardContent className="p-6">
            {generatedQuestions.length === 0 ? (
              <div className="text-center py-16 text-muted-foreground">
                <Sparkles className="h-12 w-12 mx-auto mb-3 opacity-30" />
                <p className="text-base">Select at least one template and click "Generate Questions"</p>
              </div>
            ) : (
              <div className="space-y-3 max-h-[600px] overflow-y-auto pr-2">
                {generatedQuestions.map((question, index) => (
                  <Card key={index} className="border hover:shadow-sm transition-shadow">
                    <CardContent className="p-4">
                      <div className="space-y-2">
                        <div className="flex items-start justify-between gap-2">
                          <div className="flex-1">
                            <div className="flex items-center gap-2 mb-1 flex-wrap">
                              <Badge className={getIntentBadgeColor(question.intent)}>{question.intent}</Badge>
                              <span className="font-medium text-sm">{question.name}</span>
                            </div>
                            <p className="text-xs text-muted-foreground">ID: {question.id}</p>
                          </div>
                        </div>

                        <div className="bg-gray-100 p-2 rounded text-sm italic">{question.filled_prompt}</div>

                        <div className="text-xs space-y-1">
                          <div className="font-semibold text-gray-700">Answer Type: {question.question_type}</div>
                          {question.answer.type === "scale" && question.answer.labels && (
                            <div className="flex flex-wrap gap-1">
                              {question.answer.labels.map((label: string, i: number) => (
                                <Badge key={i} variant="outline" className="text-xs">
                                  {question.answer.scale?.[i]}: {label}
                                </Badge>
                              ))}
                            </div>
                          )}
                          {question.answer.type === "binary" && question.answer.options && (
                            <div className="flex gap-1">
                              {question.answer.options.map((option: string, i: number) => (
                                <Badge key={i} variant="outline" className="text-xs">
                                  {option}
                                </Badge>
                              ))}
                            </div>
                          )}
                        </div>
                      </div>
                    </CardContent>
                  </Card>
                ))}
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
