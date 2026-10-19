export type TimeRange = "day" | "week" | "month"

/** Geographic bounding box for a ward — matches WARDS[].bounds in wardData.ts */
export interface WardBounds {
  latMin: number
  latMax: number
  lngMin: number
  lngMax: number
}

export interface UrbanArea {
  id: string
  name: string
  city: string
}

export interface PollutionData {
  CO2: number
  dioxin: number
  microplastic: number
  toxic_chemicals: number
  non_biodegradable: number
  NOx: number
  SO2: number
  CH4: number
  "PM2.5": number
  Pb: number
  Hg: number
  Cd: number
  nitrate: number
  chemical_residue: number
  styrene: number
}

export interface ImpactData {
  air: number
  water: number
  soil: number
}

export interface ImpactPoint {
  day: number
  date?: string   // actual date label e.g. "12/5"
  air: number
  water: number
  soil: number
}

export interface EnvironmentalPayload {
  pollution: PollutionData
  impact: ImpactData
  timeSeries: ImpactPoint[]
  isMock?: boolean
}
