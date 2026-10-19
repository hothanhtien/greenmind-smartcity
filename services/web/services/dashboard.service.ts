import { apiGet } from "@/lib/auth"

export type ActivityType =
    | "WASTE_CLASSIFICATION"
    | "GREEN_MEAL"
    | "TRASH_DISPOSAL"
    | "NOTE"

export interface Area {
    id: string
    name: string
    total_households: number
    total_waste: number
    plastic_percentage: number
    risk_level: "LOW" | "MEDIUM" | "HIGH"
}

export interface Activity {
    id: string
    activity_type: ActivityType
    user: { id: string; name: string }
    area: { id: string; name: string }
    metadata: Record<string, any>
    media_url?: string
    location?: { lat: number; lng: number }
    created_at: string
}

export interface Stats {
    total_activities: number
    waste_classification_count: number
    green_meal_count: number
    totalWaste: number
    totalReports: number
    totalHouseholds: number
    avgPlastic: number
    wasteByType?: Record<string, number>
    total_areas?: number
    areaCount?: number
}

interface WasteMonitorReport {
    id: string
    reportedByName?: string
    reportedBy?: { fullName?: string; username?: string }
    wardName?: string
    wasteType?: string
    status?: string
    createdAt?: string
}

export interface ActivityResponse {
    data: Activity[]
    pagination: {
        page: number
        limit: number
        total: number
    }
}

export interface AreaDetailResponse {
    area: Area
    wasteTrend: { date: string; value: number }[]
    plasticTrend: { month: string; value: number }[]
}

const normalizeWasteReportToActivity = (report: WasteMonitorReport): Activity => {
    const userName = report.reportedByName || report.reportedBy?.fullName || report.reportedBy?.username || 'Người dùng'
    const areaName = report.wardName || 'Khu vực'
    const activityType = report.wasteType?.toUpperCase() as ActivityType || 'NOTE'

    return {
        id: report.id,
        activity_type: activityType,
        user: { id: report.id, name: userName },
        area: { id: areaName, name: areaName },
        metadata: {},
        created_at: report.createdAt || new Date().toISOString(),
    }
}

function isRecord(obj: unknown): obj is Record<string, unknown> {
    return obj !== null && typeof obj === 'object' && !Array.isArray(obj)
}

function aggregateWasteByType(reports: any[]): Record<string, number> {
    return reports.reduce((acc, report) => {
        if (!report || !isRecord(report)) return acc

        const payload = report.wasteByType || report.wasteByTypeKg || report.wasteByTypeMass
        if (!isRecord(payload)) return acc

        Object.entries(payload).forEach(([key, value]) => {
            const numeric = typeof value === 'number' ? value : Number(value)
            if (Number.isFinite(numeric)) {
                acc[key] = (acc[key] || 0) + numeric
            }
        })

        return acc
    }, {} as Record<string, number>)
}

function getTotalMassFromReports(reports: any[]): number {
    return reports.reduce((sum, report) => {
        if (report == null) return sum

        if (typeof report.totalMassKg === 'number' && Number.isFinite(report.totalMassKg)) {
            return sum + report.totalMassKg
        }

        if (isRecord(report) && typeof report.totalMassKg === 'string') {
            const parsed = Number(report.totalMassKg)
            return Number.isFinite(parsed) ? sum + parsed : sum
        }

        return sum
    }, 0)
}

export const dashboardService = {
    getActivities: async (params: {
        page?: number
        limit?: number
        sort?: 'newest' | 'oldest'
    } = {}): Promise<ActivityResponse> => {
        const response = await apiGet('/waste-monitoring', { params })
        const rawData = Array.isArray(response)
            ? response
            : response?.data ?? []

        const reports = Array.isArray(rawData) ? rawData : []
        const activities = reports.map(normalizeWasteReportToActivity)

        return {
            data: activities,
            pagination: {
                page: params.page || 1,
                limit: params.limit || activities.length,
                total: response?.total ?? activities.length,
            },
        }
    },

    getOverview: async (): Promise<Stats> => {
        const [householdsRes, wasteRes, campaignsRes] = await Promise.all([
            apiGet('/households/get-all-households'),
            apiGet('/waste-monitoring', { params: { limit: 100 } }),
            apiGet('/campaigns', { params: { limit: 100 } }),
        ])

        const households = Array.isArray(householdsRes)
            ? householdsRes
            : householdsRes?.data ?? []

        const reports = Array.isArray(wasteRes)
            ? wasteRes
            : wasteRes?.data ?? []

        const campaigns = Array.isArray(campaignsRes)
            ? campaignsRes
            : campaignsRes?.data ?? []

        const totalUsers = households.length
        const totalActivities = campaigns.length
        const wasteByType = aggregateWasteByType(reports)
        const totalMassFromReports = getTotalMassFromReports(reports)
        const totalWaste = totalMassFromReports > 0 ? Number(totalMassFromReports.toFixed(2)) : reports.length
        const plasticMass = wasteByType.plastic ?? wasteByType.Plastic ?? wasteByType['plastic'] ?? 0
        const avgPlastic = totalWaste > 0 ? Math.round((plasticMass / totalWaste) * 100) : 0
        const uniqueAreas = new Set(reports.map((r: any) => r.wardName || 'unknown')).size

        return {
            total_activities: totalActivities,
            waste_classification_count: 0,
            green_meal_count: 0,
            totalWaste,
            totalReports: reports.length,
            totalHouseholds: totalUsers,
            avgPlastic,
            wasteByType,
            total_areas: uniqueAreas,
            areaCount: uniqueAreas,
        }
    },

    getAreaDetail: async (areaId: string): Promise<AreaDetailResponse | null> => {
        try {
            const response = (await apiGet(`/areas/${encodeURIComponent(areaId)}`)) as AreaDetailResponse | null
            return response
        } catch (error) {
            return null
        }
    },
}

