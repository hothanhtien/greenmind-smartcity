import axios from "axios"
import type { EnvironmentalPayload, TimeRange, UrbanArea, WardBounds } from "@/types/environmental"
import { getAccessToken } from "@/lib/auth"

const RANGE_DAY_COUNT: Record<TimeRange, number> = {
  day: 1,
  week: 7,
  month: 30,
}

/** Returns YYYY-MM-DD for a given Date */
export function toISODate(d: Date): string {
  return d.toISOString().split("T")[0]
}

/** Returns { startDate, endDate } ISO strings for the given time range (ending today) */
export function getDateRange(timeRange: TimeRange): { startDate: string; endDate: string } {
  const today = new Date()
  const days = RANGE_DAY_COUNT[timeRange]
  const start = new Date(today)
  start.setDate(today.getDate() - (days - 1))
  return { startDate: toISODate(start), endDate: toISODate(today) }
}

function isValidPayload(payload: unknown): payload is EnvironmentalPayload {
  if (!payload || typeof payload !== "object") return false
  const p = payload as Record<string, unknown>
  return (
    p.pollution !== null &&
    typeof p.pollution === "object" &&
    p.impact !== null &&
    typeof p.impact === "object" &&
    Array.isArray(p.timeSeries)
  )
}



/**
 * Fetch environmental impact data from the real API.
 * - Uses /environmental-impact/all (admin dashboard view)
 * - Optionally filter by ward geographic bounds (latMin/latMax/lngMin/lngMax)
 * - Returns null if not authenticated or if the API fails
 */
export async function fetchEnvironmentalData(
  timeRange: TimeRange,
  bounds?: WardBounds
): Promise<(EnvironmentalPayload & { isMock: false; recordCount?: number }) | null> {
  const apiUrl = process.env.NEXT_PUBLIC_API_URL || "https://green-api.khoav4.com"
  const token = getAccessToken()
  if (!token) return null

  const { startDate, endDate } = getDateRange(timeRange)

  const response = await axios.get(`${apiUrl}/environmental-impact/all`, {
    params: {
      range: timeRange,
      startDate,
      endDate,
      ...(bounds ? bounds : {}),
    },
    headers: { Authorization: `Bearer ${token}` },
    timeout: 10000,
  })

  const payload = response.data?.data
  if (!isValidPayload(payload)) return null
  return { ...payload, isMock: false }
}

/** Fetch list of all urban areas for the filter dropdown */
export async function fetchUrbanAreas(): Promise<UrbanArea[]> {
  const apiUrl = process.env.NEXT_PUBLIC_API_URL || "https://green-api.khoav4.com"
  const token = getAccessToken()
  if (!token) return []

  try {
    const response = await axios.get(`${apiUrl}/environmental-impact/urban-areas`, {
      headers: { Authorization: `Bearer ${token}` },
      timeout: 5000,
    })
    return (response.data?.data ?? []) as UrbanArea[]
  } catch {
    return []
  }
}
