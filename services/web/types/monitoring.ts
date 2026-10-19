export type AreaStatus = "red" | "yellow" | "green";

export interface Household {
  id: number;
  wardId: number;
  externalId?: string;
  name: string;
  address: string; // tên đường thực tế
  lat: number;
  lng: number;
  waste: number; // kg/day (thực tế: 1.5–4.0 bình thường, ≥4.0 cảnh báo, ≥5.0 nguy hiểm)
  status: AreaStatus;
  reportCount: number; // số báo cáo đã gửi
  greenScore?: number;
}

export interface HouseholdMember {
  name: string;
  wasteKg: number;
  role: string;
}

export interface HouseholdWasteHistory {
  month: string; // YYYY-MM
  totalWasteKg: number;
  plasticKg: number;
  organicKg: number;
  mixedKg: number;
  hazardousKg: number;
  pollution?: PollutionMetrics;
  pollutionCO2: number;
  pollutionDioxin: number;
  pollutionMicroplastic: number;
  pollutionNonBiodegradable: number;
}

export interface PollutionMetrics {
  Cd: number;
  Hg: number;
  Pb: number;
  CH4: number;
  CO2: number;
  NOx: number;
  SO2: number;
  'PM2.5': number;
  dioxin: number;
  nitrate: number;
  styrene: number;
  microplastic: number;
  toxic_chemicals: number;
  chemical_residue: number;
  non_biodegradable: number;
}

export interface HouseholdImageHistory {
  id: string | number;
  uploadedAt: string;
  imageUrl: string;
  resultImageUrl?: string;
  label: string;
  sender?: string;
  items?: WasteReportItem[];
  total_objects?: number;
  pollution?: PollutionMetrics;
  caption?: string;
}

export interface HouseholdGreenScoreItem {
  id: string;
  previousScore: number;
  delta: number;
  finalScore: number;
  householdId: string;
  items?: WasteReportItem[] | null;
  reasons?: string[] | null;
  createdAt: string;
}

export interface HouseholdProfile extends Household {
  familySize: number;
  members: HouseholdMember[];
  wasteHistory: HouseholdWasteHistory[];
  imageHistory: HouseholdImageHistory[];
  greenScore?: number;
  greenScores?: HouseholdGreenScoreItem[];
}

export interface UrbanArea {
  id: number;
  name: string;
  district: string; // quận
  population: number; // số dân
  areaKm2: number; // diện tích km²
  lat: number;
  lng: number;
  totalWaste: number; // kg/day (phường: 1,300–13,500 kg/ngày tùy quy mô)
  status: AreaStatus;
  reports: number; // pending reports
  bounds?: [number, number][]; // polygon corners [[lat, lng], ...]
}

export interface HeatmapPoint {
  lat: number;
  lng: number;
  weight: number;
}

// ─── Waste Report (Báo cáo rác từ hộ dân) ──────────────────────────────────

export type WasteType = "plastic" | "organic" | "mixed" | "hazardous";
export type ReportStatus = "pending" | "assigned" | "done";

export interface WasteReportItem {
  name: string;
  quantity: number;
  area: number;
}

export interface WasteReport {
  id: string;
  householdId: number;
  householdName: string;
  wardId: number;
  wardName: string;
  lat: number;
  lng: number;
  wasteKg: number;
  wasteType: WasteType;
  description: string;
  status: ReportStatus;
  reportedAt: string;
  reportedBy?: string;
  assignedTo: string | null;
  collectorId: number | null;
  resolvedAt: string | null;
  imageUrl?: string;
  items?: WasteReportItem[];
  total_objects?: number;
  pollution?: PollutionMetrics;
}

export interface Collector {
  id: number;
  name: string;
  phone: string;
  zones: number[]; // wardId[] phụ trách
  vehicleId: string;
  activeReports: number;
}

// ─── Report (legacy — dùng cho ReportList từ API) ──────────────────────────

export interface Report {
  id: number;
  area: string;
  desc: string;
  status: ReportStatus;
  time: string;
}

export interface AreaDetail {
  name: string;
  households: number;
  totalWaste: number;
  plasticRatio: number;
  pendingReports: number;
  wasteTrend: { date: string; waste: number }[];
  plasticByMonth: { month: string; plastic: number }[];
  electricityByMonth: { month: string; kwh: number }[];
  emissionTrend: { date: string; emission: number }[];
}

export interface Summary {
  totalWaste: number;
  urbanAreas: number;
  pendingReports: number;
  wasteDistribution: {
    plastic: number;
    organic: number;
    other: number;
  };
}
