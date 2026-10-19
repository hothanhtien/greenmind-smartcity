// Mock activity service — realistic domain model for dashboard

export type ActivityType =
  | "WASTE_CLASSIFICATION"
  | "GREEN_MEAL"
  | "ELECTRICITY_USAGE"
  | "TRASH_DISPOSAL"
  | "NOTE";

export interface Area {
  id: string;
  name: string;
  total_households: number;
  total_waste: number; // kg
  plastic_percentage: number; // %
  electricity_usage: number; // kWh
  risk_level: "LOW" | "MEDIUM" | "HIGH";
}

export interface Activity {
  id: string;
  activity_type: ActivityType;
  user: { id: string; name: string };
  area: { id: string; name: string };
  metadata: Record<string, any>;
  media_url?: string;
  location?: { lat: number; lng: number };
  created_at: string;
}

export interface Stats {
  total_activities: number;
  waste_classification_count: number;
  green_meal_count: number;
  electricity_kwh: number;
  totalWaste: number;
  totalHouseholds: number;
  avgPlastic: number;
}

export interface WasteReport {
  id: string;
  description: string;
  area: { id: string; name: string };
  lat: number;
  lng: number;
  status: "pending" | "assigned" | "collected";
  assigned_collector?: { id: string; name: string } | null;
  reported_at: string;
  household_id?: string;
}

// Realistic areas (consistent numbers)
export const mockAreas: Area[] = [
  {
    id: "a1",
    name: "Khu A",
    total_households: 120,
    total_waste: 6000,
    plastic_percentage: 55,
    electricity_usage: 20000,
    risk_level: "HIGH",
  },
  {
    id: "a2",
    name: "Khu B",
    total_households: 80,
    total_waste: 1800,
    plastic_percentage: 30,
    electricity_usage: 12000,
    risk_level: "LOW",
  },
  {
    id: "a3",
    name: "Khu C",
    total_households: 120,
    total_waste: 3700,
    plastic_percentage: 40,
    electricity_usage: 22000,
    risk_level: "MEDIUM",
  },
];

// Activities derived from areas (sample, logically consistent)
export const mockActivities: Activity[] = [
  {
    id: "act-1",
    activity_type: "GREEN_MEAL",
    user: { id: "u1", name: "Nguyen Van A" },
    area: { id: "a1", name: "Khu A" },
    metadata: { meal_type: "vegetarian", calories_saved: 300 },
    media_url: "https://via.placeholder.com/200",
    location: { lat: 16.0705, lng: 108.221 },
    created_at: "2026-03-17T10:00:00Z",
  },
  {
    id: "act-2",
    activity_type: "WASTE_CLASSIFICATION",
    user: { id: "u2", name: "Tran Thi B" },
    area: { id: "a2", name: "Khu B" },
    metadata: { waste_type: "plastic", weight_kg: 2.3, bins_used: ["recycle"] },
    media_url: "https://via.placeholder.com/200",
    location: { lat: 16.051, lng: 108.198 },
    created_at: "2026-03-16T09:00:00Z",
  },
  // add a few electricity usage activities
  {
    id: "act-3",
    activity_type: "ELECTRICITY_USAGE",
    user: { id: "u3", name: "Le Van C" },
    area: { id: "a1", name: "Khu A" },
    metadata: { kwh: 4.5, device: "AC" },
    created_at: "2026-03-17T08:30:00Z",
  },
  {
    id: "act-4",
    activity_type: "TRASH_DISPOSAL",
    user: { id: "u4", name: "Pham Thi D" },
    area: { id: "a3", name: "Khu C" },
    metadata: { disposed: "bulk_waste", notes: "sofa pickup" },
    location: { lat: 16.063, lng: 108.205 },
    created_at: "2026-03-15T14:20:00Z",
  },
  ...Array.from({ length: 24 }).map((_, i) => ({
    id: `act-${5 + i}`,
    activity_type: (i % 4 === 0
      ? "WASTE_CLASSIFICATION"
      : i % 4 === 1
      ? "GREEN_MEAL"
      : i % 4 === 2
      ? "ELECTRICITY_USAGE"
      : "TRASH_DISPOSAL") as ActivityType,
    user: { id: `u${10 + i}`, name: `User ${10 + i}` },
    area: mockAreas[i % mockAreas.length] ? { id: mockAreas[i % mockAreas.length].id, name: mockAreas[i % mockAreas.length].name } : { id: "a1", name: "Khu A" },
    metadata: { info: `auto generated ${i}` },
    media_url: i % 5 === 0 ? "https://via.placeholder.com/200" : undefined,
    location: { lat: 16.05 + Math.random() * 0.04, lng: 108.18 + Math.random() * 0.06 },
    created_at: new Date(Date.now() - i * 3600 * 1000).toISOString(),
  })),
];

// Derive overview stats from areas and activities to keep consistency
export const mockOverview = (() => {
  const totalWaste = mockAreas.reduce((s, a) => s + a.total_waste, 0);
  const totalHouseholds = mockAreas.reduce((s, a) => s + a.total_households, 0);
  const avgPlastic = Math.round(mockAreas.reduce((s, a) => s + a.plastic_percentage, 0) / mockAreas.length);
  const totalElectricity = mockAreas.reduce((s, a) => s + a.electricity_usage, 0);
  const wasteClassifyCount = mockActivities.filter((a) => a.activity_type === "WASTE_CLASSIFICATION").length;
  const greenMealCount = mockActivities.filter((a) => a.activity_type === "GREEN_MEAL").length;
  const electricityKwh = mockActivities
    .filter((a) => a.activity_type === "ELECTRICITY_USAGE")
    .reduce((s, a) => s + (a.metadata?.kwh || 0), 0);

  return {
    totalWaste,
    totalHouseholds,
    avgPlastic,
    totalElectricity,
    total_activities: mockActivities.length,
    waste_classification_count: wasteClassifyCount,
    green_meal_count: greenMealCount,
    electricity_kwh: Math.round(electricityKwh),
  };
})();

// Waste reports reported by households (consistent with areas)
export const mockWasteReports: WasteReport[] = [
  {
    id: "r1",
    description: "Overflowing bin near block 3",
    area: { id: "a1", name: "Khu A" },
    lat: 16.0708,
    lng: 108.2215,
    status: "pending",
    assigned_collector: null,
    reported_at: "2026-03-17T07:15:00Z",
    household_id: "h-1001",
  },
  {
    id: "r2",
    description: "Illegal dumping behind market",
    area: { id: "a2", name: "Khu B" },
    lat: 16.0515,
    lng: 108.199,
    status: "assigned",
    assigned_collector: { id: "c1", name: "Collector 1" },
    reported_at: "2026-03-16T11:20:00Z",
    household_id: "h-2002",
  },
  {
    id: "r3",
    description: "Household bulk disposal - sofa",
    area: { id: "a3", name: "Khu C" },
    lat: 16.0632,
    lng: 108.2047,
    status: "collected",
    assigned_collector: { id: "c2", name: "Collector 2" },
    reported_at: "2026-03-14T09:30:00Z",
    household_id: "h-3003",
  },
];

// Heatmap data derived from reports + activity clustering
export const mockHeatmap = (() => {
  const points: { lat: number; lng: number; count: number }[] = [];
  // use report locations
  mockWasteReports.forEach((r) => points.push({ lat: r.lat, lng: r.lng, count: r.status === "pending" ? 8 : 3 }));
  // add a couple activity clusters
  points.push({ lat: 16.07, lng: 108.22, count: 12 });
  points.push({ lat: 16.055, lng: 108.195, count: 6 });
  return points;
})();

// Simulated network latency
const delay = (ms = 400) => new Promise((res) => setTimeout(res, ms));

export const activityService = {
  getAreas: async (): Promise<Area[]> => {
    await delay(200);
    return mockAreas;
  },

  getActivities: async (params: {
    page?: number;
    limit?: number;
    area_id?: string;
    activity_type?: ActivityType | "" | undefined;
    from_date?: string | null;
    to_date?: string | null;
    keyword?: string | null;
    sort?: "newest" | "oldest";
  }) => {
    await delay(350);
    const page = params.page || 1;
    const limit = params.limit || 10;

    let data = [...mockActivities];

    if (params.area_id) data = data.filter((d) => d.area.id === params.area_id);
    if (params.activity_type) data = data.filter((d) => d.activity_type === params.activity_type);
    if (params.keyword)
      data = data.filter((d) => JSON.stringify(d.metadata).toLowerCase().includes(params.keyword!.toLowerCase()));

    if (params.from_date) data = data.filter((d) => new Date(d.created_at) >= new Date(params.from_date!));
    if (params.to_date) data = data.filter((d) => new Date(d.created_at) <= new Date(params.to_date!));

    if (params.sort === "newest") data.sort((a, b) => +new Date(b.created_at) - +new Date(a.created_at));
    else if (params.sort === "oldest") data.sort((a, b) => +new Date(a.created_at) - +new Date(b.created_at));

    const total = data.length;
    const start = (page - 1) * limit;
    const paged = data.slice(start, start + limit);

    return {
      data: paged,
      pagination: {
        page,
        limit,
        total,
      },
    };
  },

  getOverview: async (): Promise<Stats> => {
    await delay(200);
    return {
      total_activities: mockActivities.length,
      waste_classification_count: mockActivities.filter((a) => a.activity_type === "WASTE_CLASSIFICATION").length,
      green_meal_count: mockActivities.filter((a) => a.activity_type === "GREEN_MEAL").length,
      electricity_kwh: Math.round(mockActivities.filter((a) => a.activity_type === "ELECTRICITY_USAGE").reduce((s, a) => s + (a.metadata?.kwh || 0), 0)),
      totalWaste: mockOverview.totalWaste,
      totalHouseholds: mockOverview.totalHouseholds,
      avgPlastic: mockOverview.avgPlastic,
    } as Stats;
  },

  getStats: async () => {
    await delay(200);
    return {
      total_activities: mockActivities.length,
      waste_classification_count: mockActivities.filter((a) => a.activity_type === "WASTE_CLASSIFICATION").length,
      green_meal_count: mockActivities.filter((a) => a.activity_type === "GREEN_MEAL").length,
      electricity_kwh: Math.round(mockActivities.filter((a) => a.activity_type === "ELECTRICITY_USAGE").reduce((s, a) => s + (a.metadata?.kwh || 0), 0)),
    };
  },

  getHeatmap: async () => {
    await delay(250);
    return mockHeatmap;
  },

  getAreaDetail: async (areaId: string) => {
    await delay(300);
    const area = mockAreas.find((a) => a.id === areaId) || mockAreas[0];

    // generate realistic daily waste trend for last 14 days centered around area's avg per day
    const avgDaily = Math.round(area.total_waste / 30);
    const wasteTrend = Array.from({ length: 14 }).map((_, i) => ({
      date: new Date(Date.now() - (13 - i) * 24 * 3600 * 1000).toISOString().slice(0, 10),
      value: Math.max(10, Math.round(avgDaily * (0.7 + Math.random() * 0.6))),
    }));

    const plasticTrend = [
      { month: "Jan", value: Math.max(10, area.plastic_percentage - 5) },
      { month: "Feb", value: Math.max(10, area.plastic_percentage - 2) },
      { month: "Mar", value: area.plastic_percentage },
    ];

    const electricityTrend = Array.from({ length: 14 }).map((_, i) => ({
      date: new Date(Date.now() - (13 - i) * 24 * 3600 * 1000).toISOString().slice(0, 10),
      value: Math.round((area.electricity_usage / 30) * (0.8 + Math.random() * 0.5)),
    }));

    return {
      area,
      wasteTrend,
      plasticTrend,
      electricityTrend,
    };
  },

  getWasteReports: async (params?: { status?: string }) => {
    await delay(250);
    if (!params || !params.status) return mockWasteReports;
    return mockWasteReports.filter((r) => r.status === params.status);
  },

  assignCollector: async (reportId: string, collector: { id: string; name: string }) => {
    await delay(150);
    const r = mockWasteReports.find((x) => x.id === reportId);
    if (r) {
      r.assigned_collector = collector;
      r.status = "assigned";
      return r;
    }
    throw new Error("Report not found");
  },

  updateReportStatus: async (reportId: string, status: "pending" | "assigned" | "collected") => {
    await delay(150);
    const r = mockWasteReports.find((x) => x.id === reportId);
    if (r) {
      r.status = status;
      return r;
    }
    throw new Error("Report not found");
  },
};
