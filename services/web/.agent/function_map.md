POST | app/api/auth/google/route.ts | json, post, isAxiosError
POST | app/api/auth/login/route.ts | json, post, isAxiosError
DailyFeedbackPage | app/dashboard/daily-feedback/page.tsx | fetchData, map, substring, handleViewFeedback, toLocaleDateString, formatMetricType, toLocaleString
fetchData | app/dashboard/daily-feedback/page.tsx | setLoading, getItem, fetch, json, isArray, forEach, has, get, push, set, setGroupedData, from, values
handleViewFeedback | app/dashboard/daily-feedback/page.tsx | setSelectedUser, setIsDialogOpen
formatMetricType | app/dashboard/daily-feedback/page.tsx | join, map, split, toUpperCase, charAt, slice
SortedTooltip | app/dashboard/garbage-analytics/page.tsx | sort, map, toLocaleString
GarbageAnalyticsPage | app/dashboard/garbage-analytics/page.tsx | reduce, forEach, sort, find, map, filter, toLocaleTimeString, toLocaleString, setSelectedView, setSelectedMonth, setSelectedZone
DashboardLayout | app/dashboard/layout.tsx | -
ModelsVerifyPage | app/dashboard/models-verify/page.tsx | -
formatNumber | app/dashboard/models-verify/[id]/page.tsx | parseFloat, isNaN, toFixed
getScoreColor | app/dashboard/models-verify/[id]/page.tsx | -
getRecommendation | app/dashboard/models-verify/[id]/page.tsx | -
getRecommendationStatus | app/dashboard/models-verify/[id]/page.tsx | -
CustomTooltip | app/dashboard/models-verify/[id]/page.tsx | getScoreColor, toFixed, getRecommendation, map, slice
ModelVerifyDetailPage | app/dashboard/models-verify/[id]/page.tsx | useParams, useRouter, fetchData, values, reduce, push, map, getRecommendation, back, getOceanFullName, max, getScoreColor, getRecommendationStatus, extractAgeFromSegmentName, toFixed, handleViewSegmentFeedbacks, sort, getTime, forEach, isArray, parse, some, toLocaleString, formatNumber, keys, entries, toUpperCase, replace
fetchData | app/dashboard/models-verify/[id]/page.tsx | setLoading, setError, getItem, fetch, json, setModel, setFeedbacks
extractAgeFromSegmentName | app/dashboard/models-verify/[id]/page.tsx | split
handleViewSegmentFeedbacks | app/dashboard/models-verify/[id]/page.tsx | setSelectedSegmentFeedbacks, setIsModalOpen
getOceanFullName | app/dashboard/models-verify/[id]/page.tsx | -
DashboardPage | app/dashboard/page.tsx | -
ProfilePage | app/dashboard/profile/page.tsx | useAuth, useToast, fetchUserProfile, handleSelectChange
fetchUserProfile | app/dashboard/profile/page.tsx | setPageLoading, getItem, fetch, json, setFormData, toString, toast
handleInputChange | app/dashboard/profile/page.tsx | setFormData
handleSelectChange | app/dashboard/profile/page.tsx | setFormData
handleSave | app/dashboard/profile/page.tsx | setIsSaving, getItem, fetch, stringify, json, login, toast
ProfileSkeleton | app/dashboard/profile/page.tsx | -
QuestionBuilderPage | app/dashboard/questions/page.tsx | useRouter, useToast, loadModels, map, generateTemplate, some, handleTemplateSelect, getIntentBadgeColor
loadModels | app/dashboard/questions/page.tsx | setLoadingModels, getAllModels, setModels, toast
generateTemplate | app/dashboard/questions/page.tsx | setGeneratingModelId, setLoading, setSelectedModel, apiGenerateTemplate, setTemplates, toast
saveTemplates | app/dashboard/questions/page.tsx | toast, setSaving, map, createTemplates, setTemplates
handleTemplateSelect | app/dashboard/questions/page.tsx | setSelectedTemplates, filter
generateQuestions | app/dashboard/questions/page.tsx | toast, combineQuestion, map, setGeneratedQuestions
saveGeneratedQuestions | app/dashboard/questions/page.tsx | toast, setSavingQuestions, map, toUpperCase, charAt, createQuestions, setGeneratedQuestions, setSelectedTemplates, push
getIntentBadgeColor | app/dashboard/questions/page.tsx | -
ManageQuestionsPage | app/dashboard/questions-manage/page.tsx | useRouter, useToast, fetchMyQuestions, fetchMyQuestionSets, filter, from, map, concat, trim, includes, toLowerCase, setSearchQuery, setIntentFilter, intentColor, moveDbToExpert, onDragStart, onDrop, toggleExpert, startEdit, promptMoveBack, setEditingData, setName, setDescription, values, slice, writeText, setPendingDeleteSetId, find, setPendingMoveId, deleteExpertQuestion, handleDeleteQuestionSet, setPendingDeleteQuestionId
fetchMyQuestions | app/dashboard/questions-manage/page.tsx | setLoading, getMyQuestions, isArray, setDbQuestions, toast
fetchMyQuestionSets | app/dashboard/questions-manage/page.tsx | getMyQuestionSets, isArray, setQuestionSets
onDragStart | app/dashboard/questions-manage/page.tsx | setDraggedId, setData
onDragOver | app/dashboard/questions-manage/page.tsx | preventDefault
onDrop | app/dashboard/questions-manage/page.tsx | preventDefault, getData, setDraggedId, setExpertQuestions, findIndex, splice
toggleExpert | app/dashboard/questions-manage/page.tsx | setExpertSelected
selectAllExpert | app/dashboard/questions-manage/page.tsx | forEach, setExpertSelected
clearAllExpert | app/dashboard/questions-manage/page.tsx | setExpertSelected
moveDbToExpert | app/dashboard/questions-manage/page.tsx | find, setExpertQuestions, setDbQuestions, filter, setExpertSelected, toast
createQuestionSet | app/dashboard/questions-manage/page.tsx | map, filter, entries, trim, toast, apiCreateQuestionSet, setQuestionSets, setName, setDescription, setDbQuestions, has, setExpertQuestions, setExpertSelected, push
handleDeleteQuestionSet | app/dashboard/questions-manage/page.tsx | apiDeleteQuestionSet, setQuestionSets, filter, setPendingDeleteSetId, toast
deleteExpertQuestion | app/dashboard/questions-manage/page.tsx | find, setDbQuestions, setExpertQuestions, filter, setExpertSelected, toast
promptMoveBack | app/dashboard/questions-manage/page.tsx | setPendingDeleteQuestionId
confirmDeleteQuestion | app/dashboard/questions-manage/page.tsx | apiDeleteQuestion, setExpertQuestions, filter, toast, setPendingDeleteQuestionId
startEdit | app/dashboard/questions-manage/page.tsx | setEditingId, setEditingData
cancelEdit | app/dashboard/questions-manage/page.tsx | setEditingId, setEditingData
saveEdit | app/dashboard/questions-manage/page.tsx | apiUpdateQuestion, setExpertQuestions, map, setEditingId, setEditingData, toast
intentColor | app/dashboard/questions-manage/page.tsx | -
ScenarioPage | app/dashboard/scenario/page.tsx | -
SurveyPage | app/dashboard/survey/page.tsx | -
handleViewResult | app/dashboard/survey/page.tsx | setSelectedScenarioId, setSelectedScenario
handleScenarioDeleted | app/dashboard/survey/page.tsx | setSelectedScenarioId, setSelectedScenario
handleScenarioCreated | app/dashboard/survey/page.tsx | setRefreshKey
SurveyResultsPage | app/dashboard/survey-results/page.tsx | useToast, fetchScenarios, filter, round, map, toUpperCase, charAt, slice
fetchScenarios | app/dashboard/survey-results/page.tsx | setLoading, getAllSurveyScenarios, setScenarios, setSelectedScenarioId, fetchScenarioResults, toast
fetchScenarioResults | app/dashboard/survey-results/page.tsx | setScenarioLoading, getSimulatedScenario, isArray, map, setSimulatedUsers, toast
handleScenarioChange | app/dashboard/survey-results/page.tsx | setSelectedScenarioId, fetchScenarioResults
TreeModelBuilder | app/dashboard/tree/page.tsx | useOceanModelStore
handleDragStart | app/dashboard/tree/page.tsx | setActiveId, setDraggedItem
handleDragEnd | app/dashboard/tree/page.tsx | setSelectedOcean, setSelectedBehavior, setActiveId, setDraggedItem
handleDragCancel | app/dashboard/tree/page.tsx | setActiveId, setDraggedItem
UserManagementPage | app/dashboard/users-ocean/page.tsx | useToast, fetchUsers, sort, filter, includes, toLowerCase, localeCompare, round, reduce, from, map, setSearchQuery, setHoveredTrait, setHoveredUserId, setSelectedUser, setShowModal
fetchUsers | app/dashboard/users-ocean/page.tsx | setLoading, apiGet, isArray, map, getFullYear, toISOString, round, setUsers, toast
MonitoringPage | app/dashboard/waste-report/page.tsx | fetchAll, setSelectedArea, setSelectedWardName, setHighlightAreaName, find, toLocaleTimeString
fetchAll | app/dashboard/waste-report/page.tsx | setLoading, getAccessToken, all, catch, fetch, json, isArray, map, setCollectors, toISOString, setReports, reduce, countByType, toRaw, max, setSummary, filter
countByType | app/dashboard/waste-report/page.tsx | filter
toRaw | app/dashboard/waste-report/page.tsx | round
handleAssignCollector | app/dashboard/waste-report/page.tsx | find, getAccessToken, fetch, stringify, setReports, map, alert
RootLayout | app/layout.tsx | -
LoginPage | app/login/page.tsx | useRouter, useAuth, useForm, zodResolver, handleSubmit, register, setShowPassword
onEmailLogin | app/login/page.tsx | setIsLoading, setError, loginWithEmail, login, push
onGoogleLogin | app/login/page.tsx | setIsLoading, setError, loginWithGoogle, login, push
Home | app/page.tsx | redirect
ChartsSection | components/charts-section.tsx | -
AreaDetail | components/dashboard/activity-monitoring/AreaDetail.tsx | setData, setLoading, setError, finally, catch, then, getAreaDetail, toLocaleString
AreaList | components/dashboard/activity-monitoring/AreaList.tsx | map, toLocaleString, onSelect
FilterBar | components/dashboard/activity-monitoring/FilterBar.tsx | setTimeout, onChange, clearTimeout, setArea, map, setActivityType, setKeyword, setSort, setFromDate, setToDate
OverviewCards | components/dashboard/activity-monitoring/OverviewCards.tsx | toLocaleString
WasteReports | components/dashboard/activity-monitoring/WasteReports.tsx | load, setFilter, map, toLocaleString, handleUpdateStatus, setAssigning, setCollectorName, handleAssign
load | components/dashboard/activity-monitoring/WasteReports.tsx | setLoading, getWasteReports, isArray, setReports, filter
handleAssign | components/dashboard/activity-monitoring/WasteReports.tsx | assignCollector, now, setCollectorName, setAssigning, load
handleUpdateStatus | components/dashboard/activity-monitoring/WasteReports.tsx | updateReportStatus, load
DashboardHeader | components/dashboard-header.tsx | -
DashboardSidebar | components/dashboard-sidebar.tsx | usePathname, map, cn
Sidebar | components/layout/Sidebar.tsx | -
SidebarNav | components/layout/SidebarNav.tsx | usePathname, map, startsWith, cn
TopNavbar | components/layout/TopNavbar.tsx | usePathname, useAuth, map, startsWith, cn
UserNav | components/layout/UserNav.tsx | useAuth
CreateModelDialog | components/models/CreateModelDialog.tsx | resetForm, setFormData, setIsEditingBehavior, setEditBehaviorValue, map, handleConfirmEditBehavior, handleCancelEditBehavior, getAvailableBehaviors, handleSelectBehavior, includes, handleGenderToggle, setNewLocation, preventDefault, handleAddLocation, handleRemoveLocation, onOpenChange
getAvailableBehaviors | components/models/CreateModelDialog.tsx | -
handleCreateModel | components/models/CreateModelDialog.tsx | setCreateLoading, getItem, fetch, stringify, json, onModelCreated
resetForm | components/models/CreateModelDialog.tsx | setFormData, setNewLocation, setIsEditingBehavior, setEditBehaviorValue
handleAddLocation | components/models/CreateModelDialog.tsx | trim, includes, setFormData, setNewLocation
handleRemoveLocation | components/models/CreateModelDialog.tsx | setFormData, filter
handleGenderToggle | components/models/CreateModelDialog.tsx | setFormData, includes, filter
handleSelectBehavior | components/models/CreateModelDialog.tsx | setFormData, setEditBehaviorValue, setBehaviorPopoverOpen
handleStartEditBehavior | components/models/CreateModelDialog.tsx | stopPropagation, setIsEditingBehavior, setEditBehaviorValue
handleConfirmEditBehavior | components/models/CreateModelDialog.tsx | trim, setFormData, setIsEditingBehavior, setBehaviorPopoverOpen
handleCancelEditBehavior | components/models/CreateModelDialog.tsx | setIsEditingBehavior, setEditBehaviorValue
ModelsTable | components/models/ModelsTable.tsx | useRouter, fetchData, filter, includes, toLowerCase, slice, ceil, setSearchTerm, setIsCreateDialogOpen, map, getModelFeedbackCount, push, handlePageChange
fetchData | components/models/ModelsTable.tsx | setLoading, getItem, all, fetch, json, setModels, isArray, setFeedbacks
getModelFeedbackCount | components/models/ModelsTable.tsx | filter
handlePageChange | components/models/ModelsTable.tsx | setCurrentPage
handleModelCreated | components/models/ModelsTable.tsx | setIsCreateDialogOpen, fetchData
EmptyState | components/models/ModelsTable.tsx | -
ModelsTableSkeleton | components/models/ModelsTable.tsx | -
FloatingTooltip | components/ocean/FloatingTooltip.tsx | -
OceanChart | components/ocean/OceanChart.tsx | setTooltipPos, setHoveredBarTrait
ProtectedRoute | components/ProtectedRoute.tsx | useAuth, useRouter, usePathname, setMounted, includes, push
RecentActivity | components/recent-activity.tsx | map
ScenarioForm | components/scenario/ScenarioForm.tsx | useScenarioStore, useToast, map, setPercentage
handleGenerate | components/scenario/ScenarioForm.tsx | toast, find, parseInt, generateScenario, setTrait, setBehavior, setDemographic, setPercentage
ScenarioTable | components/scenario/ScenarioTable.tsx | useScenarioStore, useToast, map, toFixed, handleSendToApp
handleSendToApp | components/scenario/ScenarioTable.tsx | simulateDistribution, toast
StatsCards | components/stats-cards.tsx | map
QuestionModal | components/survey/QuestionModal.tsx | useToast, loadQuestionSets, map, setSelectedSetId, onOpenChange
loadQuestionSets | components/survey/QuestionModal.tsx | setIsLoading, getMyQuestionSets, setQuestionSets, isArray, toast
handleConfirm | components/survey/QuestionModal.tsx | toast, setIsSubmitting, attachQuestionSet, find, setSelectedSetId, setTimeout, onOpenChange, onSuccess
SurveyForm | components/survey/SurveyForm.tsx | useToast, fetchLocations, setMinAge, setMaxAge, setPercentage, setSearchLocation, filter, includes, toLowerCase, map, setSelectedAddresses
fetchLocations | components/survey/SurveyForm.tsx | getUsers, isArray, sort, from, map, filter, trim, setLocations, toast, setLoadingLocs
handleGenerate | components/survey/SurveyForm.tsx | parseInt, toast, isNaN, setSubmitting, createSurveyScenario, setMinAge, setMaxAge, setPercentage, setSelectedAddresses, setSelectedGender, onScenarioCreated
SurveyScenarioTable | components/survey/SurveyScenarioTable.tsx | useToast, fetchScenarios, find, map, toUpperCase, charAt, slice, isArray, split, handleDelete, handleSelectQuestions, handleSimulate, handleViewResult
fetchScenarios | components/survey/SurveyScenarioTable.tsx | setLoading, getAllSurveyScenarios, setScenarios, toast
handleSimulate | components/survey/SurveyScenarioTable.tsx | find, toast, simulateSurveyScenario, fetchScenarios, getAllSurveyScenarios, onViewResult, setErrorScenarios
handleDelete | components/survey/SurveyScenarioTable.tsx | deleteSurveyScenario, fetchScenarios, setErrorScenarios, onScenarioDeleted, toast
handleViewResult | components/survey/SurveyScenarioTable.tsx | find, toast, onViewResult
handleSelectQuestions | components/survey/SurveyScenarioTable.tsx | setSelectedScenarioId, setModalOpen
handleModalOpenChange | components/survey/SurveyScenarioTable.tsx | setModalOpen, fetchScenarios
handleExport | components/survey/SurveyScenarioTable.tsx | stringify, createObjectURL, createElement, toISOString, click, revokeObjectURL
SurveySimulator | components/survey/SurveySimulator.tsx | useToast, fetchSimulatedData, setSimulatedData, toFixed, map, toUpperCase, charAt, slice
fetchSimulatedData | components/survey/SurveySimulator.tsx | setLoading, getSimulatedScenario, setSimulatedData, isArray, map, filter, includes, toast
ThemeProvider | components/theme-provider.tsx | -
DetailEditor | components/tree/DetailEditor.tsx | useRouter, useOceanModelStore, setPopulation, map, getSelectedGenderOption, handleGenderSelect, setNewLocation, preventDefault, handleAddLocation, handleRemoveLocation, setContext, setKeywords
handleAddLocation | components/tree/DetailEditor.tsx | trim, includes, setPopulation, setNewLocation
handleRemoveLocation | components/tree/DetailEditor.tsx | setPopulation, filter
handleGenderSelect | components/tree/DetailEditor.tsx | setPopulation
getSelectedGenderOption | components/tree/DetailEditor.tsx | includes
getAgeRange | components/tree/DetailEditor.tsx | -
handleGenerateKeywords | components/tree/DetailEditor.tsx | setIsGenerating, generateKeywords, getAgeRange, setGeneratedKeywords, success
handleSaveModel | components/tree/DetailEditor.tsx | trim, setIsSaving, getAgeRange, createModel, toString, now, addModel, reset, setSelectedGeneratedKeyword, success, push
handleKeywordSelection | components/tree/DetailEditor.tsx | setSelectedGeneratedKeyword, setKeywords
TreeCanvas | components/tree/TreeCanvas.tsx | useOceanModelStore, useDroppable, forEach, entries, push, setEditBehaviorValue, handleConfirmEditBehavior, handleCancelEditBehavior, map
clearSelection | components/tree/TreeCanvas.tsx | setSelectedOcean, setSelectedBehavior, setIsEditingBehavior
handleBehaviorChange | components/tree/TreeCanvas.tsx | setSelectedBehavior, setEditBehaviorValue
handleStartEditBehavior | components/tree/TreeCanvas.tsx | setEditBehaviorValue, setIsEditingBehavior
handleConfirmEditBehavior | components/tree/TreeCanvas.tsx | trim, setSelectedBehavior, setIsEditingBehavior
handleCancelEditBehavior | components/tree/TreeCanvas.tsx | setIsEditingBehavior, setEditBehaviorValue
DraggableItem | components/tree/TreeToolbox.tsx | useDraggable
TreeToolbox | components/tree/TreeToolbox.tsx | map, entries, toggleSection
toggleSection | components/tree/TreeToolbox.tsx | setOpenSections
Accordion | components/ui/accordion.tsx | -
AccordionItem | components/ui/accordion.tsx | cn
AccordionTrigger | components/ui/accordion.tsx | cn
AccordionContent | components/ui/accordion.tsx | cn
AlertDialog | components/ui/alert-dialog.tsx | -
AlertDialogTrigger | components/ui/alert-dialog.tsx | -
AlertDialogPortal | components/ui/alert-dialog.tsx | -
AlertDialogOverlay | components/ui/alert-dialog.tsx | cn
AlertDialogContent | components/ui/alert-dialog.tsx | cn
AlertDialogHeader | components/ui/alert-dialog.tsx | cn
AlertDialogFooter | components/ui/alert-dialog.tsx | cn
AlertDialogTitle | components/ui/alert-dialog.tsx | cn
AlertDialogDescription | components/ui/alert-dialog.tsx | cn
AlertDialogAction | components/ui/alert-dialog.tsx | cn, buttonVariants
AlertDialogCancel | components/ui/alert-dialog.tsx | cn, buttonVariants
Alert | components/ui/alert.tsx | cn, alertVariants
AlertTitle | components/ui/alert.tsx | cn
AlertDescription | components/ui/alert.tsx | cn
AspectRatio | components/ui/aspect-ratio.tsx | -
Avatar | components/ui/avatar.tsx | cn
AvatarImage | components/ui/avatar.tsx | cn
AvatarFallback | components/ui/avatar.tsx | cn
Badge | components/ui/badge.tsx | cn, badgeVariants
Breadcrumb | components/ui/breadcrumb.tsx | -
BreadcrumbList | components/ui/breadcrumb.tsx | cn
BreadcrumbItem | components/ui/breadcrumb.tsx | cn
BreadcrumbLink | components/ui/breadcrumb.tsx | cn
BreadcrumbPage | components/ui/breadcrumb.tsx | cn
BreadcrumbSeparator | components/ui/breadcrumb.tsx | cn
BreadcrumbEllipsis | components/ui/breadcrumb.tsx | cn
ButtonGroup | components/ui/button-group.tsx | cn, buttonGroupVariants
ButtonGroupText | components/ui/button-group.tsx | cn
ButtonGroupSeparator | components/ui/button-group.tsx | cn
Button | components/ui/button.tsx | cn, buttonVariants
Calendar | components/ui/calendar.tsx | getDefaultClassNames, cn, buttonVariants
formatMonthDropdown | components/ui/calendar.tsx | toLocaleString
Root | components/ui/calendar.tsx | cn
Chevron | components/ui/calendar.tsx | cn
WeekNumber | components/ui/calendar.tsx | -
CalendarDayButton | components/ui/calendar.tsx | getDefaultClassNames, focus, toLocaleDateString, cn
Card | components/ui/card.tsx | cn
CardHeader | components/ui/card.tsx | cn
CardTitle | components/ui/card.tsx | cn
CardDescription | components/ui/card.tsx | cn
CardAction | components/ui/card.tsx | cn
CardContent | components/ui/card.tsx | cn
CardFooter | components/ui/card.tsx | cn
useCarousel | components/ui/carousel.tsx | -
Carousel | components/ui/carousel.tsx | useEmblaCarousel, setCanScrollPrev, canScrollPrev, setCanScrollNext, canScrollNext, scrollPrev, scrollNext, preventDefault, setApi, onSelect, on, off, cn
CarouselContent | components/ui/carousel.tsx | useCarousel, cn
CarouselItem | components/ui/carousel.tsx | useCarousel, cn
CarouselPrevious | components/ui/carousel.tsx | useCarousel, cn
CarouselNext | components/ui/carousel.tsx | useCarousel, cn
useChart | components/ui/chart.tsx | -
ChartContainer | components/ui/chart.tsx | useId, replace, cn
ChartStyle | components/ui/chart.tsx | filter, entries, join, map
ChartTooltipContent | components/ui/chart.tsx | useChart, getPayloadConfigFromPayload, cn, labelFormatter, map, formatter, toLocaleString
ChartLegendContent | components/ui/chart.tsx | useChart, cn, map, getPayloadConfigFromPayload
getPayloadConfigFromPayload | components/ui/chart.tsx | -
Checkbox | components/ui/checkbox.tsx | cn
Collapsible | components/ui/collapsible.tsx | -
CollapsibleTrigger | components/ui/collapsible.tsx | -
CollapsibleContent | components/ui/collapsible.tsx | -
Command | components/ui/command.tsx | cn
CommandDialog | components/ui/command.tsx | cn
CommandInput | components/ui/command.tsx | cn
CommandList | components/ui/command.tsx | cn
CommandEmpty | components/ui/command.tsx | -
CommandGroup | components/ui/command.tsx | cn
CommandSeparator | components/ui/command.tsx | cn
CommandItem | components/ui/command.tsx | cn
CommandShortcut | components/ui/command.tsx | cn
ContextMenu | components/ui/context-menu.tsx | -
ContextMenuTrigger | components/ui/context-menu.tsx | -
ContextMenuGroup | components/ui/context-menu.tsx | -
ContextMenuPortal | components/ui/context-menu.tsx | -
ContextMenuSub | components/ui/context-menu.tsx | -
ContextMenuRadioGroup | components/ui/context-menu.tsx | -
ContextMenuSubTrigger | components/ui/context-menu.tsx | cn
ContextMenuSubContent | components/ui/context-menu.tsx | cn
ContextMenuContent | components/ui/context-menu.tsx | cn
ContextMenuItem | components/ui/context-menu.tsx | cn
ContextMenuCheckboxItem | components/ui/context-menu.tsx | cn
ContextMenuRadioItem | components/ui/context-menu.tsx | cn
ContextMenuLabel | components/ui/context-menu.tsx | cn
ContextMenuSeparator | components/ui/context-menu.tsx | cn
ContextMenuShortcut | components/ui/context-menu.tsx | cn
Dialog | components/ui/dialog.tsx | -
DialogTrigger | components/ui/dialog.tsx | -
DialogPortal | components/ui/dialog.tsx | -
DialogClose | components/ui/dialog.tsx | -
DialogOverlay | components/ui/dialog.tsx | cn
DialogContent | components/ui/dialog.tsx | cn
DialogHeader | components/ui/dialog.tsx | cn
DialogFooter | components/ui/dialog.tsx | cn
DialogTitle | components/ui/dialog.tsx | cn
DialogDescription | components/ui/dialog.tsx | cn
Drawer | components/ui/drawer.tsx | -
DrawerTrigger | components/ui/drawer.tsx | -
DrawerPortal | components/ui/drawer.tsx | -
DrawerClose | components/ui/drawer.tsx | -
DrawerOverlay | components/ui/drawer.tsx | cn
DrawerContent | components/ui/drawer.tsx | cn
DrawerHeader | components/ui/drawer.tsx | cn
DrawerFooter | components/ui/drawer.tsx | cn
DrawerTitle | components/ui/drawer.tsx | cn
DrawerDescription | components/ui/drawer.tsx | cn
DropdownMenu | components/ui/dropdown-menu.tsx | -
DropdownMenuPortal | components/ui/dropdown-menu.tsx | -
DropdownMenuTrigger | components/ui/dropdown-menu.tsx | -
DropdownMenuContent | components/ui/dropdown-menu.tsx | cn
DropdownMenuGroup | components/ui/dropdown-menu.tsx | -
DropdownMenuItem | components/ui/dropdown-menu.tsx | cn
DropdownMenuCheckboxItem | components/ui/dropdown-menu.tsx | cn
DropdownMenuRadioGroup | components/ui/dropdown-menu.tsx | -
DropdownMenuRadioItem | components/ui/dropdown-menu.tsx | cn
DropdownMenuLabel | components/ui/dropdown-menu.tsx | cn
DropdownMenuSeparator | components/ui/dropdown-menu.tsx | cn
DropdownMenuShortcut | components/ui/dropdown-menu.tsx | cn
DropdownMenuSub | components/ui/dropdown-menu.tsx | -
DropdownMenuSubTrigger | components/ui/dropdown-menu.tsx | cn
DropdownMenuSubContent | components/ui/dropdown-menu.tsx | cn
Empty | components/ui/empty.tsx | cn
EmptyHeader | components/ui/empty.tsx | cn
EmptyMedia | components/ui/empty.tsx | cn, emptyMediaVariants
EmptyTitle | components/ui/empty.tsx | cn
EmptyDescription | components/ui/empty.tsx | cn
EmptyContent | components/ui/empty.tsx | cn
FieldSet | components/ui/field.tsx | cn
FieldLegend | components/ui/field.tsx | cn
FieldGroup | components/ui/field.tsx | cn
Field | components/ui/field.tsx | cn, fieldVariants
FieldContent | components/ui/field.tsx | cn
FieldLabel | components/ui/field.tsx | cn
FieldTitle | components/ui/field.tsx | cn
FieldDescription | components/ui/field.tsx | cn
FieldSeparator | components/ui/field.tsx | cn
FieldError | components/ui/field.tsx | map, cn
FormField | components/ui/form.tsx | -
useFormField | components/ui/form.tsx | useFormContext, useFormState, getFieldState
FormItem | components/ui/form.tsx | useId, cn
FormLabel | components/ui/form.tsx | useFormField, cn
FormControl | components/ui/form.tsx | useFormField
FormDescription | components/ui/form.tsx | useFormField, cn
FormMessage | components/ui/form.tsx | useFormField, cn
HoverCard | components/ui/hover-card.tsx | -
HoverCardTrigger | components/ui/hover-card.tsx | -
HoverCardContent | components/ui/hover-card.tsx | cn
InputGroup | components/ui/input-group.tsx | cn
InputGroupAddon | components/ui/input-group.tsx | cn, inputGroupAddonVariants, closest, focus, querySelector
InputGroupButton | components/ui/input-group.tsx | cn, inputGroupButtonVariants
InputGroupText | components/ui/input-group.tsx | cn
InputGroupInput | components/ui/input-group.tsx | cn
InputGroupTextarea | components/ui/input-group.tsx | cn
InputOTP | components/ui/input-otp.tsx | cn
InputOTPGroup | components/ui/input-otp.tsx | cn
InputOTPSlot | components/ui/input-otp.tsx | cn
InputOTPSeparator | components/ui/input-otp.tsx | -
Input | components/ui/input.tsx | cn
ItemGroup | components/ui/item.tsx | cn
ItemSeparator | components/ui/item.tsx | cn
Item | components/ui/item.tsx | cn, itemVariants
ItemMedia | components/ui/item.tsx | cn, itemMediaVariants
ItemContent | components/ui/item.tsx | cn
ItemTitle | components/ui/item.tsx | cn
ItemDescription | components/ui/item.tsx | cn
ItemActions | components/ui/item.tsx | cn
ItemHeader | components/ui/item.tsx | cn
ItemFooter | components/ui/item.tsx | cn
Kbd | components/ui/kbd.tsx | cn
KbdGroup | components/ui/kbd.tsx | cn
Label | components/ui/label.tsx | cn
Menubar | components/ui/menubar.tsx | cn
MenubarMenu | components/ui/menubar.tsx | -
MenubarGroup | components/ui/menubar.tsx | -
MenubarPortal | components/ui/menubar.tsx | -
MenubarRadioGroup | components/ui/menubar.tsx | -
MenubarTrigger | components/ui/menubar.tsx | cn
MenubarContent | components/ui/menubar.tsx | cn
MenubarItem | components/ui/menubar.tsx | cn
MenubarCheckboxItem | components/ui/menubar.tsx | cn
MenubarRadioItem | components/ui/menubar.tsx | cn
MenubarLabel | components/ui/menubar.tsx | cn
MenubarSeparator | components/ui/menubar.tsx | cn
MenubarShortcut | components/ui/menubar.tsx | cn
MenubarSub | components/ui/menubar.tsx | -
MenubarSubTrigger | components/ui/menubar.tsx | cn
MenubarSubContent | components/ui/menubar.tsx | cn
NavigationMenu | components/ui/navigation-menu.tsx | cn
NavigationMenuList | components/ui/navigation-menu.tsx | cn
NavigationMenuItem | components/ui/navigation-menu.tsx | cn
NavigationMenuTrigger | components/ui/navigation-menu.tsx | cn, navigationMenuTriggerStyle
NavigationMenuContent | components/ui/navigation-menu.tsx | cn
NavigationMenuViewport | components/ui/navigation-menu.tsx | cn
NavigationMenuLink | components/ui/navigation-menu.tsx | cn
NavigationMenuIndicator | components/ui/navigation-menu.tsx | cn
Pagination | components/ui/pagination.tsx | cn
PaginationContent | components/ui/pagination.tsx | cn
PaginationItem | components/ui/pagination.tsx | -
PaginationLink | components/ui/pagination.tsx | cn, buttonVariants
PaginationPrevious | components/ui/pagination.tsx | cn
PaginationNext | components/ui/pagination.tsx | cn
PaginationEllipsis | components/ui/pagination.tsx | cn
Popover | components/ui/popover.tsx | -
PopoverTrigger | components/ui/popover.tsx | -
PopoverContent | components/ui/popover.tsx | cn
PopoverAnchor | components/ui/popover.tsx | -
Progress | components/ui/progress.tsx | cn
RadioGroup | components/ui/radio-group.tsx | cn
RadioGroupItem | components/ui/radio-group.tsx | cn
ResizablePanelGroup | components/ui/resizable.tsx | cn
ResizablePanel | components/ui/resizable.tsx | -
ResizableHandle | components/ui/resizable.tsx | cn
ScrollArea | components/ui/scroll-area.tsx | cn
ScrollBar | components/ui/scroll-area.tsx | cn
Select | components/ui/select.tsx | -
SelectGroup | components/ui/select.tsx | -
SelectValue | components/ui/select.tsx | -
SelectTrigger | components/ui/select.tsx | cn
SelectContent | components/ui/select.tsx | cn
SelectLabel | components/ui/select.tsx | cn
SelectItem | components/ui/select.tsx | cn
SelectSeparator | components/ui/select.tsx | cn
SelectScrollUpButton | components/ui/select.tsx | cn
SelectScrollDownButton | components/ui/select.tsx | cn
Separator | components/ui/separator.tsx | cn
Sheet | components/ui/sheet.tsx | -
SheetTrigger | components/ui/sheet.tsx | -
SheetClose | components/ui/sheet.tsx | -
SheetPortal | components/ui/sheet.tsx | -
SheetOverlay | components/ui/sheet.tsx | cn
SheetContent | components/ui/sheet.tsx | cn
SheetHeader | components/ui/sheet.tsx | cn
SheetFooter | components/ui/sheet.tsx | cn
SheetTitle | components/ui/sheet.tsx | cn
SheetDescription | components/ui/sheet.tsx | cn
useSidebar | components/ui/sidebar.tsx | -
SidebarProvider | components/ui/sidebar.tsx | useIsMobile, value, setOpenProp, _setOpen, setOpenMobile, setOpen, addEventListener, removeEventListener, cn
handleKeyDown | components/ui/sidebar.tsx | preventDefault, toggleSidebar
Sidebar | components/ui/sidebar.tsx | useSidebar, cn
SidebarTrigger | components/ui/sidebar.tsx | useSidebar, cn, onClick, toggleSidebar
SidebarRail | components/ui/sidebar.tsx | useSidebar, cn
SidebarInset | components/ui/sidebar.tsx | cn
SidebarInput | components/ui/sidebar.tsx | cn
SidebarHeader | components/ui/sidebar.tsx | cn
SidebarFooter | components/ui/sidebar.tsx | cn
SidebarSeparator | components/ui/sidebar.tsx | cn
SidebarContent | components/ui/sidebar.tsx | cn
SidebarGroup | components/ui/sidebar.tsx | cn
SidebarGroupLabel | components/ui/sidebar.tsx | cn
SidebarGroupAction | components/ui/sidebar.tsx | cn
SidebarGroupContent | components/ui/sidebar.tsx | cn
SidebarMenu | components/ui/sidebar.tsx | cn
SidebarMenuItem | components/ui/sidebar.tsx | cn
SidebarMenuButton | components/ui/sidebar.tsx | useSidebar, cn, sidebarMenuButtonVariants
SidebarMenuAction | components/ui/sidebar.tsx | cn
SidebarMenuBadge | components/ui/sidebar.tsx | cn
SidebarMenuSkeleton | components/ui/sidebar.tsx | floor, random, cn
SidebarMenuSub | components/ui/sidebar.tsx | cn
SidebarMenuSubItem | components/ui/sidebar.tsx | cn
SidebarMenuSubButton | components/ui/sidebar.tsx | cn
Skeleton | components/ui/skeleton.tsx | cn
Slider | components/ui/slider.tsx | isArray, cn, from
Toaster | components/ui/sonner.tsx | useTheme
Spinner | components/ui/spinner.tsx | cn
Switch | components/ui/switch.tsx | cn
Table | components/ui/table.tsx | cn
TableHeader | components/ui/table.tsx | cn
TableBody | components/ui/table.tsx | cn
TableFooter | components/ui/table.tsx | cn
TableRow | components/ui/table.tsx | cn
TableHead | components/ui/table.tsx | cn
TableCell | components/ui/table.tsx | cn
TableCaption | components/ui/table.tsx | cn
Tabs | components/ui/tabs.tsx | cn
TabsList | components/ui/tabs.tsx | cn
TabsTrigger | components/ui/tabs.tsx | cn
TabsContent | components/ui/tabs.tsx | cn
Textarea | components/ui/textarea.tsx | cn
Toaster | components/ui/toaster.tsx | useToast, map
ToggleGroup | components/ui/toggle-group.tsx | cn
ToggleGroupItem | components/ui/toggle-group.tsx | cn, toggleVariants
Toggle | components/ui/toggle.tsx | cn, toggleVariants
TooltipProvider | components/ui/tooltip.tsx | -
Tooltip | components/ui/tooltip.tsx | -
TooltipTrigger | components/ui/tooltip.tsx | -
TooltipContent | components/ui/tooltip.tsx | cn
useIsMobile | components/ui/use-mobile.tsx | matchMedia, addEventListener, setIsMobile, removeEventListener
onChange | components/ui/use-mobile.tsx | setIsMobile
genId | components/ui/use-toast.ts | toString
addToRemoveQueue | components/ui/use-toast.ts | has, setTimeout, delete, dispatch, set
reducer | components/ui/use-toast.ts | slice, map, addToRemoveQueue, forEach, filter
dispatch | components/ui/use-toast.ts | reducer, forEach, listener
toast | components/ui/use-toast.ts | genId, dispatch
update | components/ui/use-toast.ts | dispatch
dismiss | components/ui/use-toast.ts | dispatch
onOpenChange | components/ui/use-toast.ts | dismiss
useToast | components/ui/use-toast.ts | push, indexOf, splice
dismiss | components/ui/use-toast.ts | dispatch
CompletionChart | components/users/CompletionChart.tsx | fetchUsers, reduce, map, entries, substring, toUpperCase, charAt, slice
fetchUsers | components/users/CompletionChart.tsx | apiGet, isArray, setUsers, setLoading
UserDetailModal | components/users/UserDetailModal.tsx | calculateAge, convertToPercentage, toLocaleDateString
calculateAge | components/users/UserDetailModal.tsx | getFullYear, getMonth, getDate
convertToPercentage | components/users/UserDetailModal.tsx | round
UserTable | components/users/UserTable.tsx | useToast, fetchUsers, filter, map, toUpperCase, charAt, slice
fetchUsers | components/users/UserTable.tsx | setLoading, apiGet, isArray, map, getFullYear, setUsers, sort, setLocations, toast
defaultDetail | components/waste-report/AreaDrawer.tsx | round, toFixed
ChartCard | components/waste-report/AreaDrawer.tsx | -
AreaDrawer | components/waste-report/AreaDrawer.tsx | setLocalReports, find, addEventListener, removeEventListener, defaultDetail, filter, map, toLocaleTimeString, setSelectedReportId, setSelectedCollectorId, sort, toLocaleString
handleAssign | components/waste-report/AreaDrawer.tsx | find, onAssign, setLocalReports, map, setAssignedFlash, setTimeout, setSelectedReportId, setSelectedCollectorId
handleKey | components/waste-report/AreaDrawer.tsx | setSelectedReportId, setSelectedCollectorId, onClose
buildWardMarkerIcon | components/waste-report/MapView.tsx | replace, divIcon
buildAlertIcon | components/waste-report/MapView.tsx | divIcon
buildReportIcon | components/waste-report/MapView.tsx | divIcon
MapView | components/waste-report/MapView.tsx | getElementById, createElement, appendChild, map, addTo, tileLayer, zoom, attribution, layerGroup, setMapLoaded, remove, clearLayers, forEach, filter, buildWardMarkerIcon, on, marker, onAreaSelect, buildAlertIcon, bindPopup, toFixed, buildReportIcon, toUpperCase, slice, onReportSelect, drawWardOverview, drawReportPins, flyTo, drawAlertDetail, find, latLngBounds, latLng, flyToBounds, pad, onClearSelection
ReportList | components/waste-report/ReportList.tsx | filter, map, setFilter, setExpandedReportId, toLocaleTimeString, handleBoxClick, setSelectedCollectorId, handleAssign, pop, split
handleAssign | components/waste-report/ReportList.tsx | onAssign, setExpandedReportId, setSelectedCollectorId
handleBoxClick | components/waste-report/ReportList.tsx | setExpandedReportId, setSelectedCollectorId
SummaryCards | components/waste-report/SummaryCards.tsx | map, toLocaleString, filter
useApi | hooks/use-api.ts | setLoading, setError, authenticatedRequest, onSuccess, onError, apiGet, apiPost, apiPut, apiDelete, apiPatch
clearError | hooks/use-api.ts | setError
useIsMobile | hooks/use-mobile.ts | matchMedia, addEventListener, setIsMobile, removeEventListener
onChange | hooks/use-mobile.ts | setIsMobile
genId | hooks/use-toast.ts | toString
addToRemoveQueue | hooks/use-toast.ts | has, setTimeout, delete, dispatch, set
reducer | hooks/use-toast.ts | slice, map, addToRemoveQueue, forEach, filter
dispatch | hooks/use-toast.ts | reducer, forEach, listener
toast | hooks/use-toast.ts | genId, dispatch
update | hooks/use-toast.ts | dispatch
dismiss | hooks/use-toast.ts | dispatch
onOpenChange | hooks/use-toast.ts | dismiss
useToast | hooks/use-toast.ts | push, indexOf, splice
dismiss | hooks/use-toast.ts | dispatch
getAccessToken | lib/auth.ts | getItem
getStoredUser | lib/auth.ts | getItem, parse
clearAuthData | lib/auth.ts | removeItem
authenticatedRequest | lib/auth.ts | apiClient, isAxiosError
authenticatedAiRequest | lib/auth.ts | aiApiClient, isAxiosError
apiGet | lib/auth.ts | authenticatedRequest
apiPost | lib/auth.ts | authenticatedRequest
apiPut | lib/auth.ts | authenticatedRequest
apiDelete | lib/auth.ts | authenticatedRequest
apiPatch | lib/auth.ts | authenticatedRequest
aiApiGet | lib/auth.ts | authenticatedAiRequest
aiApiPost | lib/auth.ts | authenticatedAiRequest
loginWithEmail | lib/auth.ts | post, isAxiosError
loginWithGoogle | lib/auth.ts | post, isAxiosError
isAuthenticated | lib/auth.ts | getAccessToken
logout | lib/auth.ts | clearAuthData
getAllModels | lib/auth.ts | getAccessToken, apiClient
getModelById | lib/auth.ts | apiGet
createModel | lib/auth.ts | apiPost
updateModel | lib/auth.ts | apiPut
deleteModel | lib/auth.ts | apiDelete
getAllQuestions | lib/auth.ts | getAccessToken, apiClient
getQuestionById | lib/auth.ts | apiGet
createQuestion | lib/auth.ts | apiPost
createQuestions | lib/auth.ts | getAccessToken, apiClient
updateQuestion | lib/auth.ts | apiPut
deleteQuestion | lib/auth.ts | apiDelete
createTemplates | lib/auth.ts | getAccessToken, apiClient
getAllTemplates | lib/auth.ts | apiGet
getTemplateById | lib/auth.ts | apiGet
updateTemplate | lib/auth.ts | apiPut
deleteTemplate | lib/auth.ts | apiDelete
generateKeywords | lib/auth.ts | aiApiPost
generateTemplate | lib/auth.ts | aiApiPost
combineQuestion | lib/auth.ts | aiApiPost
getUsers | lib/auth.ts | apiGet
getMyQuestions | lib/question-set.ts | apiGet
getMyQuestionSets | lib/question-set.ts | apiGet
createQuestionSet | lib/question-set.ts | apiPost
updateQuestionSet | lib/question-set.ts | apiPut
deleteQuestionSet | lib/question-set.ts | apiDelete
updateQuestion | lib/question-set.ts | apiPut
deleteQuestion | lib/question-set.ts | apiDelete
getAllSurveyScenarios | lib/survey.ts | getAccessToken, apiClient
createSurveyScenario | lib/survey.ts | apiPost
attachQuestionSet | lib/survey.ts | apiPut
simulateSurveyScenario | lib/survey.ts | apiPost
getSimulatedScenario | lib/survey.ts | getAccessToken, apiClient
deleteSurveyScenario | lib/survey.ts | apiDelete
getAllUsersWithOCEAN | lib/users.ts | getAccessToken, apiGet
cn | lib/utils.ts | twMerge, clsx
delay | services/activity.service.ts | setTimeout
getAreas | services/activity.service.ts | delay
getActivities | services/activity.service.ts | delay, filter, includes, toLowerCase, stringify, sort, slice
getOverview | services/activity.service.ts | delay, filter, round, reduce
getStats | services/activity.service.ts | delay, filter, round, reduce
getHeatmap | services/activity.service.ts | delay
getAreaDetail | services/activity.service.ts | delay, find, round, map, from, slice, toISOString, now, max, random
getWasteReports | services/activity.service.ts | delay, filter
assignCollector | services/activity.service.ts | delay, find
updateReportStatus | services/activity.service.ts | delay, find
setSelectedOcean | store/useOceanModelStore.ts | set
setSelectedBehavior | store/useOceanModelStore.ts | set
setContext | store/useOceanModelStore.ts | set
setPopulation | store/useOceanModelStore.ts | set
setKeywords | store/useOceanModelStore.ts | set
setGeneratedKeywords | store/useOceanModelStore.ts | set
setIsGenerating | store/useOceanModelStore.ts | set
addModel | store/useOceanModelStore.ts | set
reset | store/useOceanModelStore.ts | set
setTree | store/useScenarioStore.ts | set
addNode | store/useScenarioStore.ts | set, push, addToParent
addToParent | store/useScenarioStore.ts | map, addToParent
removeNode | store/useScenarioStore.ts | set, removeFromTree
removeFromTree | store/useScenarioStore.ts | map, filter, removeFromTree
updateNode | store/useScenarioStore.ts | set, updateInTree
updateInTree | store/useScenarioStore.ts | map, updateInTree
generateScenario | store/useScenarioStore.ts | now, toFixed, set
selectQuestions | store/useScenarioStore.ts | set, map
simulateDistribution | store/useScenarioStore.ts | set, find, filter, ceil, sort, random, slice, map
deleteScenario | store/useScenarioStore.ts | set, filter
updateUserStatus | store/useScenarioStore.ts | set, map
exportTreeAsJSON | store/useScenarioStore.ts | get, stringify
exportScenariosAsJSON | store/useScenarioStore.ts | get, stringify
importTree | store/useScenarioStore.ts | parse, isArray, set
AuthProvider | contexts/AuthContext.tsx | setIsHydrated, getStoredUser, isAuthenticated, setUser, setIsLoading
login | contexts/AuthContext.tsx | setItem, stringify, setUser
logout | contexts/AuthContext.tsx | clearAuthData, setUser
useAuth | contexts/AuthContext.tsx | -