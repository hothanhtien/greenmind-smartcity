"use client"

import { useState, useEffect } from "react"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { useToast } from "@/hooks/use-toast"
import { apiGet } from "@/lib/auth"
import { Eye, Loader2 } from "lucide-react"

interface User {
  id: string
  username: string
  fullName: string
  email: string
  age: number
  gender: string
  location: string
}

export function UserTable() {
  const { toast } = useToast()
  const [users, setUsers] = useState<User[]>([])
  const [loading, setLoading] = useState(true)
  const [locationFilter, setLocationFilter] = useState("all")
  const [locations, setLocations] = useState<string[]>([])

  useEffect(() => {
    fetchUsers()
  }, [])

  const fetchUsers = async () => {
    setLoading(true)
    try {
      const response = await apiGet("/auth/get-alls")
      const usersArray = Array.isArray(response.data)
        ? response.data
        : (response.data?.data ? response.data.data : [])

      const mappedUsers = usersArray.map((user: any) => ({
        id: user.id,
        username: user.username,
        fullName: user.fullName,
        email: user.email,
        age: new Date().getFullYear() - new Date(user.dateOfBirth).getFullYear() || 25,
        gender: user.gender || "unknown",
        location: user.location || "Unknown",
      }))

      setUsers(mappedUsers)
      const uniqueLocations = [...new Set(mappedUsers.map(u => u.location))].sort() as string[]
      setLocations(uniqueLocations)
    } catch (error) {
      console.error("Error fetching users:", error)
      toast({
        title: "Error",
        description: "Failed to load users",
        variant: "destructive",
      })
    } finally {
      setLoading(false)
    }
  }

  const filteredUsers = users.filter((user) => {
    if (locationFilter !== "all" && user.location !== locationFilter) return false
    return true
  })

  return (
    <Card className="shadow-sm">
      <CardHeader className="border-b bg-gray-50/50">
        <div className="flex items-center justify-between">
          <div>
            <CardTitle className="text-xl">All Users</CardTitle>
            <p className="text-sm text-muted-foreground mt-1">Total: {users.length} users</p>
          </div>
          <Select value={locationFilter} onValueChange={setLocationFilter}>
            <SelectTrigger className="w-[200px]">
              <SelectValue placeholder="Filter by location" />
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
      </CardHeader>
      <CardContent className="pt-6">
        {loading ? (
          <div className="flex items-center justify-center py-12">
            <Loader2 className="h-6 w-6 animate-spin text-muted-foreground" />
          </div>
        ) : filteredUsers.length === 0 ? (
          <div className="rounded-lg border-2 border-dashed border-gray-200 bg-gray-50/50 py-12 text-center">
            <p className="text-sm text-muted-foreground">No users found</p>
          </div>
        ) : (
          <div className="overflow-hidden rounded-lg border">
            <Table>
              <TableHeader>
                <TableRow className="bg-gray-50/50">
                  <TableHead className="font-semibold">Name</TableHead>
                  <TableHead className="font-semibold">Email</TableHead>
                  <TableHead className="font-semibold">Age</TableHead>
                  <TableHead className="font-semibold">Gender</TableHead>
                  <TableHead className="font-semibold">Location</TableHead>
                  <TableHead className="text-right font-semibold">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {filteredUsers.map((user) => (
                  <TableRow key={user.id} className="hover:bg-gray-50/50">
                    <TableCell className="font-medium">{user.fullName}</TableCell>
                    <TableCell className="text-muted-foreground">{user.email}</TableCell>
                    <TableCell>{user.age}</TableCell>
                    <TableCell>
                      <Badge variant="outline" className="font-medium">
                        {user.gender.charAt(0).toUpperCase() + user.gender.slice(1)}
                      </Badge>
                    </TableCell>
                    <TableCell>{user.location}</TableCell>
                    <TableCell className="text-right">
                      <Button variant="ghost" size="sm" className="shadow-sm">
                        <Eye className="h-4 w-4" />
                      </Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>
        )}
      </CardContent>
    </Card>
  )
}
