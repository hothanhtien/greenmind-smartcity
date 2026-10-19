"use client"

import { useState, useEffect } from "react"
import {
    Card,
    CardContent,
    CardDescription,
    CardHeader,
    CardTitle,
} from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "@/components/ui/select"
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from "@/components/ui/table"
import { Badge } from "@/components/ui/badge"
import { Progress } from "@/components/ui/progress"
import { useToast } from "@/hooks/use-toast"
import { UserDetailModal } from "@/components/users/UserDetailModal"
import { FloatingTooltip, OceanChart } from "@/components/ocean"
import { apiGet } from "@/lib/auth"
import {
    Eye,
    Trash2,
    Download,
    Filter,
    Users,
    TrendingUp,
    RefreshCw,
} from "lucide-react"

interface User {
    id: string
    userId?: string
    username: string
    fullName: string
    email: string
    age: number
    gender: string
    location: string
    phoneNumber?: string
    role?: string
    dateOfBirth: string
    ocean?: {
        openness: number
        conscientiousness: number
        extraversion: number
        agreeableness: number
        neuroticism: number
    }
    createdAt: string
    updatedAt: string
}

export default function UserManagementPage() {
    const { toast } = useToast()
    const [users, setUsers] = useState<User[]>([])
    const [loading, setLoading] = useState(true)
    const [searchQuery, setSearchQuery] = useState("")
    const [filterGender, setFilterGender] = useState("all")
    const [filterLocation, setFilterLocation] = useState("all")
    const [sortBy, setSortBy] = useState("name")
    const [selectedUser, setSelectedUser] = useState<User | null>(null)
    const [showModal, setShowModal] = useState(false)
    const [hoveredTrait, setHoveredTrait] = useState<string | null>(null)
    const [hoveredUserId, setHoveredUserId] = useState<string | null>(null)
    const [hoveredBarTrait, setHoveredBarTrait] = useState<string | null>(null)
    const [tooltipPos, setTooltipPos] = useState<{ x: number; y: number } | null>(null)

    useEffect(() => {
        fetchUsers()
    }, [])

    const fetchUsers = async () => {
        setLoading(true)
        try {
            // Fetch user data with bigFive scores from single API
            const response = await apiGet("/auth/get-alls")

            // Get users array - handle both response.data and response.data.data
            const usersArray = Array.isArray(response.data)
                ? response.data
                : (response.data?.data ? response.data.data : [])

            if (usersArray.length > 0) {
                const mappedUsers: User[] = usersArray.map((user: any, index: number) => {
                    const bigFive = user.bigFive

                    return {
                        id: user.id || `user_${index}`,
                        userId: user.id,
                        username: user.username || `user_${index}`,
                        fullName: user.fullName || `User ${index + 1}`,
                        email: user.email || `${user.username || `user_${index}`}@example.com`,
                        age: new Date().getFullYear() - new Date(user.dateOfBirth).getFullYear() || 25,
                        gender: user.gender || "unknown",
                        location: user.location || "Unknown",
                        dateOfBirth: user.dateOfBirth || new Date().toISOString(),
                        ocean: {
                            openness: (bigFive && typeof bigFive.openness === 'number') ? Math.round(bigFive.openness * 100) : 0,
                            conscientiousness: (bigFive && typeof bigFive.conscientiousness === 'number') ? Math.round(bigFive.conscientiousness * 100) : 0,
                            extraversion: (bigFive && typeof bigFive.extraversion === 'number') ? Math.round(bigFive.extraversion * 100) : 0,
                            agreeableness: (bigFive && typeof bigFive.agreeableness === 'number') ? Math.round(bigFive.agreeableness * 100) : 0,
                            neuroticism: (bigFive && typeof bigFive.neuroticism === 'number') ? Math.round(bigFive.neuroticism * 100) : 0,
                        },
                        createdAt: user.createdAt || new Date().toISOString(),
                        updatedAt: user.updatedAt || new Date().toISOString(),
                    }
                })
                setUsers(mappedUsers)
            } else {
                setUsers([])
            }
        } catch (error) {
            console.error("Error fetching users:", error)
            toast({
                title: "Error",
                description: "Failed to load users",
                variant: "destructive",
            })
            setUsers([])
        } finally {
            setLoading(false)
        }
    }    // Filter and sort users
    const filteredUsers = users
        .filter((user) => {
            if (searchQuery && !user.fullName.toLowerCase().includes(searchQuery.toLowerCase()) &&
                !user.username.toLowerCase().includes(searchQuery.toLowerCase())) {
                return false
            }
            if (filterGender !== "all" && user.gender.toLowerCase() !== filterGender.toLowerCase()) {
                return false
            }
            if (filterLocation !== "all" && user.location !== filterLocation) {
                return false
            }
            return true
        })
        .sort((a, b) => {
            switch (sortBy) {
                case "name":
                    return a.fullName.localeCompare(b.fullName)
                case "age":
                    return a.age - b.age
                case "openness":
                    return (b.ocean?.openness || 0) - (a.ocean?.openness || 0)
                default:
                    return 0
            }
        })

    // Calculate average OCEAN scores
    const avgOCEAN = users.length
        ? {
            openness: Math.round(
                users.reduce((sum, u) => sum + (u.ocean?.openness || 0), 0) / users.length
            ),
            conscientiousness: Math.round(
                users.reduce((sum, u) => sum + (u.ocean?.conscientiousness || 0), 0) / users.length
            ),
            extraversion: Math.round(
                users.reduce((sum, u) => sum + (u.ocean?.extraversion || 0), 0) / users.length
            ),
            agreeableness: Math.round(
                users.reduce((sum, u) => sum + (u.ocean?.agreeableness || 0), 0) / users.length
            ),
            neuroticism: Math.round(
                users.reduce((sum, u) => sum + (u.ocean?.neuroticism || 0), 0) / users.length
            ),
        }
        : { openness: 0, conscientiousness: 0, extraversion: 0, agreeableness: 0, neuroticism: 0 }

    // Prepare chart data
    const oceanChartData = [
        {
            name: "Average OCEAN",
            Openness: avgOCEAN.openness,
            Conscientiousness: avgOCEAN.conscientiousness,
            Extraversion: avgOCEAN.extraversion,
            Agreeableness: avgOCEAN.agreeableness,
            Neuroticism: avgOCEAN.neuroticism,
        },
    ]

    // Prepare data
    const locations = Array.from(new Set(users.map((u) => u.location)))
    const genders = Array.from(new Set(users.map((u) => u.gender)))

    return (
        <div className="min-h-screen bg-gradient-to-br from-slate-50 via-white to-blue-50 px-4 py-6 sm:px-6 md:px-8 lg:px-10">
            <div className="space-y-8 max-w-7xl mx-auto">
                <div className="space-y-3 mb-6 md:mb-8">
                    <div className="flex flex-col sm:flex-row items-start sm:items-center gap-3 sm:gap-4">
                        <div className="w-14 h-14 rounded-2xl bg-gradient-to-br from-blue-500 to-blue-700 flex items-center justify-center shadow-lg">
                            <Users className="h-7 w-7 text-white" />
                        </div>
                        <div>
                            <h1 className="text-3xl sm:text-4xl font-bold tracking-tight bg-clip-text text-transparent bg-gradient-to-r from-blue-600 to-blue-900">
                                User Management
                            </h1>
                            <p className="text-sm sm:text-base text-muted-foreground mt-2">Analyze and manage user OCEAN personality profiles</p>
                        </div>
                    </div>
                </div>

                {/* Statistics Cards */}
                <div className="grid gap-4 grid-cols-1 sm:grid-cols-2 lg:grid-cols-5">
                    <Card className="border-0 shadow-lg hover:shadow-xl transition-all duration-300 bg-gradient-to-br from-blue-50 to-blue-100/50">
                        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                            <CardTitle className="text-sm font-semibold text-blue-900">Total Users</CardTitle>
                            <div className="w-10 h-10 rounded-lg bg-blue-500/20 flex items-center justify-center">
                                <Users className="h-5 w-5 text-blue-600" />
                            </div>
                        </CardHeader>
                        <CardContent>
                            <div className="text-3xl font-bold text-blue-900">{users.length}</div>
                            <p className="text-xs text-blue-700 mt-2">Registered users</p>
                        </CardContent>
                    </Card>

                    <Card className="border-0 shadow-lg hover:shadow-xl transition-all duration-300 bg-gradient-to-br from-purple-50 to-purple-100/50">
                        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                            <CardTitle className="text-sm font-semibold text-purple-900">Avg Openness</CardTitle>
                            <div className="w-10 h-10 rounded-lg bg-purple-500/20 flex items-center justify-center">
                                <TrendingUp className="h-5 w-5 text-purple-600" />
                            </div>
                        </CardHeader>
                        <CardContent>
                            <div className="text-3xl font-bold text-purple-900">{avgOCEAN.openness}</div>
                            <Progress value={avgOCEAN.openness} className="mt-3 h-2" />
                        </CardContent>
                    </Card>

                    <Card className="border-0 shadow-lg hover:shadow-xl transition-all duration-300 bg-gradient-to-br from-green-50 to-green-100/50">
                        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                            <CardTitle className="text-sm font-semibold text-green-900">Avg Conscientiousness</CardTitle>
                            <div className="w-10 h-10 rounded-lg bg-green-500/20 flex items-center justify-center">
                                <TrendingUp className="h-5 w-5 text-green-600" />
                            </div>
                        </CardHeader>
                        <CardContent>
                            <div className="text-3xl font-bold text-green-900">{avgOCEAN.conscientiousness}</div>
                            <Progress value={avgOCEAN.conscientiousness} className="mt-3 h-2" />
                        </CardContent>
                    </Card>

                    <Card className="border-0 shadow-lg hover:shadow-xl transition-all duration-300 bg-gradient-to-br from-red-50 to-red-100/50">
                        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                            <CardTitle className="text-sm font-semibold text-red-900">Avg Extraversion</CardTitle>
                            <div className="w-10 h-10 rounded-lg bg-red-500/20 flex items-center justify-center">
                                <TrendingUp className="h-5 w-5 text-red-600" />
                            </div>
                        </CardHeader>
                        <CardContent>
                            <div className="text-3xl font-bold text-red-900">{avgOCEAN.extraversion}</div>
                            <Progress value={avgOCEAN.extraversion} className="mt-3 h-2" />
                        </CardContent>
                    </Card>

                    <Card className="border-0 shadow-lg hover:shadow-xl transition-all duration-300 bg-gradient-to-br from-pink-50 to-pink-100/50">
                        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                            <CardTitle className="text-sm font-semibold text-pink-900">Avg Agreeableness</CardTitle>
                            <div className="w-10 h-10 rounded-lg bg-pink-500/20 flex items-center justify-center">
                                <TrendingUp className="h-5 w-5 text-pink-600" />
                            </div>
                        </CardHeader>
                        <CardContent>
                            <div className="text-3xl font-bold text-pink-900">{avgOCEAN.agreeableness}</div>
                            <Progress value={avgOCEAN.agreeableness} className="mt-3 h-2" />
                        </CardContent>
                    </Card>

                    <Card className="border-0 shadow-lg hover:shadow-xl transition-all duration-300 bg-gradient-to-br from-amber-50 to-amber-100/50">
                        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                            <CardTitle className="text-sm font-semibold text-amber-900">Avg Neuroticism</CardTitle>
                            <div className="w-10 h-10 rounded-lg bg-amber-500/20 flex items-center justify-center">
                                <TrendingUp className="h-5 w-5 text-amber-600" />
                            </div>
                        </CardHeader>
                        <CardContent>
                            <div className="text-3xl font-bold text-amber-900">{avgOCEAN.neuroticism}</div>
                            <Progress value={avgOCEAN.neuroticism} className="mt-3 h-2" />
                        </CardContent>
                    </Card>

                    <Card className="border-0 shadow-lg hover:shadow-xl transition-all duration-300 bg-gradient-to-br from-orange-50 to-orange-100/50">
                        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                            <CardTitle className="text-sm font-semibold text-orange-900">Locations</CardTitle>
                            <div className="w-10 h-10 rounded-lg bg-orange-500/20 flex items-center justify-center">
                                <Filter className="h-5 w-5 text-orange-600" />
                            </div>
                        </CardHeader>
                        <CardContent>
                            <div className="text-3xl font-bold text-orange-900">{locations.length}</div>
                            <p className="text-xs text-orange-700 mt-2">Unique locations</p>
                        </CardContent>
                    </Card>
                </div>

                {/* OCEAN Chart */}
                <Card className="border-0 shadow-lg">
                    <CardHeader>
                        <CardTitle className="text-xl">Average OCEAN Personality Scores</CardTitle>
                        <CardDescription>Aggregated trait distribution across all users</CardDescription>
                    </CardHeader>
                    <CardContent>
                        <FloatingTooltip
                            hoveredBarTrait={hoveredBarTrait}
                            tooltipPos={tooltipPos}
                            oceanChartData={oceanChartData}
                        />
                        <div className="relative">
                            <OceanChart
                                oceanChartData={oceanChartData}
                                hoveredBarTrait={hoveredBarTrait}
                                setHoveredBarTrait={setHoveredBarTrait}
                                setTooltipPos={setTooltipPos}
                            />
                        </div>
                    </CardContent>
                </Card>

                {/* Filters and Search */}
                <Card className="border-0 shadow-lg">
                    <CardHeader className="pb-4">
                        <CardTitle className="text-lg flex items-center gap-2">
                            <Filter className="h-5 w-5" />
                            Filters & Search
                        </CardTitle>
                    </CardHeader>
                    <CardContent className="space-y-5">
                        <div className="grid gap-4 grid-cols-1 sm:grid-cols-2 lg:grid-cols-4">
                            <div className="space-y-2">
                                <label className="text-sm font-semibold text-gray-700">Search</label>
                                <Input
                                    placeholder="Search by name..."
                                    value={searchQuery}
                                    onChange={(e) => setSearchQuery(e.target.value)}
                                    className="border-gray-200 focus:border-blue-500 focus:ring-blue-500"
                                />
                            </div>

                            <div className="space-y-2">
                                <label className="text-sm font-semibold text-gray-700">Gender</label>
                                <Select value={filterGender} onValueChange={setFilterGender}>
                                    <SelectTrigger className="border-gray-200">
                                        <SelectValue />
                                    </SelectTrigger>
                                    <SelectContent>
                                        <SelectItem value="all">All Genders</SelectItem>
                                        {genders.map((gender) => (
                                            <SelectItem key={gender} value={gender.toLowerCase()}>
                                                {gender}
                                            </SelectItem>
                                        ))}
                                    </SelectContent>
                                </Select>
                            </div>

                            <div className="space-y-2">
                                <label className="text-sm font-semibold text-gray-700">Location</label>
                                <Select value={filterLocation} onValueChange={setFilterLocation}>
                                    <SelectTrigger className="border-gray-200">
                                        <SelectValue />
                                    </SelectTrigger>
                                    <SelectContent>
                                        <SelectItem value="all">All Locations</SelectItem>
                                        {locations.map((location) => (
                                            <SelectItem key={location} value={location}>
                                                {location}
                                            </SelectItem>
                                        ))}
                                    </SelectContent>
                                </Select>
                            </div>

                            <div className="space-y-2">
                                <label className="text-sm font-semibold text-gray-700">Sort By</label>
                                <Select value={sortBy} onValueChange={setSortBy}>
                                    <SelectTrigger className="border-gray-200">
                                        <SelectValue />
                                    </SelectTrigger>
                                    <SelectContent>
                                        <SelectItem value="name">Name</SelectItem>
                                        <SelectItem value="age">Age</SelectItem>
                                        <SelectItem value="openness">Openness</SelectItem>
                                    </SelectContent>
                                </Select>
                            </div>
                        </div>

                        <div className="flex gap-2 pt-2">
                            <Button
                                onClick={fetchUsers}
                                variant="outline"
                                size="sm"
                                disabled={loading}
                                className="border-blue-200 hover:bg-blue-50 text-blue-600 font-medium"
                            >
                                <RefreshCw className={`mr-2 h-4 w-4 ${loading ? "animate-spin" : ""}`} />
                                Refresh
                            </Button>
                            <Button
                                variant="outline"
                                size="sm"
                                className="border-blue-200 hover:bg-blue-50 text-blue-600 font-medium"
                            >
                                <Download className="mr-2 h-4 w-4" />
                                Export
                            </Button>
                        </div>
                    </CardContent>
                </Card>

                {/* Users Table */}
                <Card className="border-0 shadow-lg">
                    <CardHeader className="pb-4">
                        <div className="flex items-center justify-between">
                            <div>
                                <CardTitle className="text-lg">Users with OCEAN Scores</CardTitle>
                                <CardDescription className="mt-1">{filteredUsers.length} users found</CardDescription>
                            </div>
                        </div>
                    </CardHeader>
                    <CardContent>
                        {loading ? (
                            <div className="flex items-center justify-center py-16">
                                <div className="text-center">
                                    <div className="w-12 h-12 rounded-full border-4 border-blue-200 border-t-blue-600 animate-spin mx-auto mb-4"></div>
                                    <div className="text-muted-foreground font-medium">Loading users...</div>
                                </div>
                            </div>
                        ) : filteredUsers.length === 0 ? (
                            <div className="flex items-center justify-center py-16">
                                <div className="text-center">
                                    <Users className="h-12 w-12 text-gray-300 mx-auto mb-3" />
                                    <div className="text-muted-foreground font-medium">No users found</div>
                                </div>
                            </div>
                        ) : (
                            <div className="w-full overflow-x-auto -mx-4 sm:mx-0 rounded-lg">
                                <div className="px-4 sm:px-0">
                                    <Table className="min-w-full">
                                        <TableHeader>
                                            <TableRow className="bg-gradient-to-r from-blue-50 to-purple-50 border-b-2 border-gray-300">
                                                <TableHead className="font-bold text-gray-800 py-4 text-xs sm:text-sm">User</TableHead>
                                                <TableHead className="font-bold text-gray-800 text-xs sm:text-sm">Email</TableHead>
                                                <TableHead className="font-bold text-gray-800 text-xs sm:text-sm">Age</TableHead>
                                                <TableHead className="font-bold text-gray-800 text-xs sm:text-sm">Gender</TableHead>
                                                <TableHead className="font-bold text-gray-800 text-xs sm:text-sm">Location</TableHead>
                                                <TableHead className="font-bold text-gray-800 text-xs sm:text-sm">OCEAN</TableHead>
                                                <TableHead className="text-right font-bold text-gray-800 text-xs sm:text-sm">Actions</TableHead>
                                            </TableRow>
                                        </TableHeader>
                                        <TableBody>
                                            {filteredUsers.map((user, idx) => (
                                                <TableRow
                                                    key={user.id}
                                                    className={`${idx % 2 === 0 ? 'bg-white' : 'bg-gray-50/70'} transition-colors duration-200 hover:bg-blue-50/60 border-b border-gray-100`}
                                                >
                                                    <TableCell className="font-semibold text-gray-900 py-4 text-xs sm:text-sm">{user.fullName}</TableCell>
                                                    <TableCell className="text-xs sm:text-sm text-muted-foreground">{user.email}</TableCell>
                                                    <TableCell className="font-medium text-gray-700 text-xs sm:text-sm">{user.age}</TableCell>
                                                    <TableCell>
                                                        <Badge variant="secondary" className="bg-blue-100 text-blue-700 font-medium hover:bg-blue-200">{user.gender}</Badge>
                                                    </TableCell>
                                                    <TableCell className="text-xs sm:text-sm text-gray-700">{user.location}</TableCell>
                                                    <TableCell
                                                        onMouseLeave={() => {
                                                            setHoveredTrait(null)
                                                            setHoveredUserId(null)
                                                        }}
                                                    >
                                                        <div className="space-y-1 text-xs font-semibold">
                                                            {user.ocean && (
                                                                <>
                                                                    <div
                                                                        className={`flex gap-1 text-blue-600 cursor-pointer transition-all ${hoveredUserId === user.id && hoveredTrait === "O"
                                                                            ? "scale-110 font-bold"
                                                                            : hoveredUserId === user.id && hoveredTrait
                                                                                ? "opacity-30"
                                                                                : ""
                                                                            }`}
                                                                        onMouseEnter={() => {
                                                                            setHoveredTrait("O")
                                                                            setHoveredUserId(user.id)
                                                                        }}
                                                                    >
                                                                        <span className="w-8">O:</span>
                                                                        <span className="w-8 text-right">{user.ocean.openness}</span>
                                                                    </div>
                                                                    <div
                                                                        className={`flex gap-1 text-green-600 cursor-pointer transition-all ${hoveredUserId === user.id && hoveredTrait === "C"
                                                                            ? "scale-110 font-bold"
                                                                            : hoveredUserId === user.id && hoveredTrait
                                                                                ? "opacity-30"
                                                                                : ""
                                                                            }`}
                                                                        onMouseEnter={() => {
                                                                            setHoveredTrait("C")
                                                                            setHoveredUserId(user.id)
                                                                        }}
                                                                    >
                                                                        <span className="w-8">C:</span>
                                                                        <span className="w-8 text-right">{user.ocean.conscientiousness}</span>
                                                                    </div>
                                                                    <div
                                                                        className={`flex gap-1 text-amber-600 cursor-pointer transition-all ${hoveredUserId === user.id && hoveredTrait === "E"
                                                                            ? "scale-110 font-bold"
                                                                            : hoveredUserId === user.id && hoveredTrait
                                                                                ? "opacity-30"
                                                                                : ""
                                                                            }`}
                                                                        onMouseEnter={() => {
                                                                            setHoveredTrait("E")
                                                                            setHoveredUserId(user.id)
                                                                        }}
                                                                    >
                                                                        <span className="w-8">E:</span>
                                                                        <span className="w-8 text-right">{user.ocean.extraversion}</span>
                                                                    </div>
                                                                    <div
                                                                        className={`flex gap-1 text-pink-600 cursor-pointer transition-all ${hoveredUserId === user.id && hoveredTrait === "A"
                                                                            ? "scale-110 font-bold"
                                                                            : hoveredUserId === user.id && hoveredTrait
                                                                                ? "opacity-30"
                                                                                : ""
                                                                            }`}
                                                                        onMouseEnter={() => {
                                                                            setHoveredTrait("A")
                                                                            setHoveredUserId(user.id)
                                                                        }}
                                                                    >
                                                                        <span className="w-8">A:</span>
                                                                        <span className="w-8 text-right">{user.ocean.agreeableness}</span>
                                                                    </div>
                                                                    <div
                                                                        className={`flex gap-1 text-red-600 cursor-pointer transition-all ${hoveredUserId === user.id && hoveredTrait === "N"
                                                                            ? "scale-110 font-bold"
                                                                            : hoveredUserId === user.id && hoveredTrait
                                                                                ? "opacity-30"
                                                                                : ""
                                                                            }`}
                                                                        onMouseEnter={() => {
                                                                            setHoveredTrait("N")
                                                                            setHoveredUserId(user.id)
                                                                        }}
                                                                    >
                                                                        <span className="w-8">N:</span>
                                                                        <span className="w-8 text-right">{user.ocean.neuroticism}</span>
                                                                    </div>
                                                                </>
                                                            )}
                                                        </div>
                                                    </TableCell>
                                                    <TableCell className="text-right">
                                                        <div className="flex justify-end gap-2">
                                                            <Button
                                                                size="sm"
                                                                variant="ghost"
                                                                title="View user profile"
                                                                onClick={() => {
                                                                    setSelectedUser(user)
                                                                    setShowModal(true)
                                                                }}
                                                                className="hover:bg-blue-100 hover:text-blue-600 text-blue-600 transition-colors"
                                                            >
                                                                <Eye className="h-4 w-4" />
                                                            </Button>
                                                            <Button
                                                                size="sm"
                                                                variant="ghost"
                                                                className="text-red-600 hover:bg-red-100 transition-colors"
                                                                title="Delete user"
                                                            >
                                                                <Trash2 className="h-4 w-4" />
                                                            </Button>
                                                        </div>
                                                    </TableCell>
                                                </TableRow>
                                            ))}
                                        </TableBody>
                                    </Table>
                                </div>
                            </div>
                        )}
                    </CardContent>
                </Card>

                {/* User Detail Modal */}
                <UserDetailModal
                    user={selectedUser}
                    onClose={() => {
                        setShowModal(false)
                        setSelectedUser(null)
                    }}
                />
            </div>
        </div>
    )
}