import axios from "axios"
import { getAccessToken } from "@/lib/auth"

export type PaymentStatus = "succeeded" | "pending" | "failed" | "refunded"

export interface PaymentTransaction {
  id: string
  amount: number         // in cents
  currency: string
  status: PaymentStatus
  customer: string
  email: string
  description: string
  createdAt: string      // ISO string
  stripeId?: string
}

export interface PaymentMetrics {
  totalRevenue: number         // cents
  totalTransactions: number
  successRate: number          // 0–100
  avgTransactionValue: number  // cents
  refundedAmount: number       // cents
  pendingAmount: number        // cents
}

export interface RevenuePoint {
  date: string     // "D/M"
  revenue: number  // cents
  count: number
}

export interface PaymentPayload {
  metrics: PaymentMetrics
  recentTransactions: PaymentTransaction[]
  revenueSeries: RevenuePoint[]
  statusBreakdown: { status: PaymentStatus; count: number; amount: number }[]
  isMock?: boolean
}

// ──────────────────────────────────────────────
//  Mock data generator
// ──────────────────────────────────────────────
const NAMES = ["Nguyen Van A", "Tran Thi B", "Le Van C", "Pham Thi D", "Hoang Van E", "Do Thi F"]
const DESCS = ["Subscription", "One-time donation", "Campaign contribution", "Premium plan", "Eco package"]

function rand(min: number, max: number) {
  return Math.floor(Math.random() * (max - min + 1)) + min
}

function seededValue(seed: number, min: number, max: number) {
  const x = Math.sin(seed) * 10000
  const t = x - Math.floor(x)
  return Math.floor(t * (max - min + 1)) + min
}

export function generateMockPayments(days = 30): PaymentPayload {
  const today = new Date()
  const statuses: PaymentStatus[] = ["succeeded", "succeeded", "succeeded", "succeeded", "pending", "failed", "refunded"]

  // Build recent transactions
  const recentTransactions: PaymentTransaction[] = Array.from({ length: 20 }, (_, i) => {
    const d = new Date(today)
    d.setDate(today.getDate() - seededValue(i * 3, 0, days))
    const status = statuses[seededValue(i, 0, statuses.length - 1)]
    const amount = seededValue(i * 7, 9900, 1990000)  // $99 – $19,900
    return {
      id: `pay_${Math.random().toString(36).slice(2, 10)}`,
      amount,
      currency: "usd",
      status,
      customer: NAMES[seededValue(i, 0, NAMES.length - 1)],
      email: `user${i + 1}@example.com`,
      description: DESCS[seededValue(i * 2, 0, DESCS.length - 1)],
      createdAt: d.toISOString(),
      stripeId: `pi_${Math.random().toString(36).slice(2, 18)}`,
    }
  }).sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())

  // Build revenue time series
  const revenueSeries: RevenuePoint[] = Array.from({ length: days }, (_, i) => {
    const d = new Date(today)
    d.setDate(today.getDate() - (days - 1 - i))
    const label = `${d.getDate()}/${d.getMonth() + 1}`
    const count = seededValue(i * 5, 0, 8)
    const revenue = count * seededValue(i * 3 + 1, 9900, 299000)
    return { date: label, revenue, count }
  })

  const succeeded = recentTransactions.filter(t => t.status === "succeeded")
  const totalRevenue = succeeded.reduce((acc, t) => acc + t.amount, 0)
  const refundedAmount = recentTransactions
    .filter(t => t.status === "refunded")
    .reduce((acc, t) => acc + t.amount, 0)
  const pendingAmount = recentTransactions
    .filter(t => t.status === "pending")
    .reduce((acc, t) => acc + t.amount, 0)

  const statusBreakdown: { status: PaymentStatus; count: number; amount: number }[] = [
    "succeeded", "pending", "failed", "refunded"
  ].map(s => ({
    status: s as PaymentStatus,
    count: recentTransactions.filter(t => t.status === s).length,
    amount: recentTransactions.filter(t => t.status === s).reduce((a, t) => a + t.amount, 0),
  }))

  return {
    metrics: {
      totalRevenue,
      totalTransactions: recentTransactions.length,
      successRate: Math.round((succeeded.length / recentTransactions.length) * 100),
      avgTransactionValue: succeeded.length > 0 ? Math.round(totalRevenue / succeeded.length) : 0,
      refundedAmount,
      pendingAmount,
    },
    recentTransactions,
    revenueSeries,
    statusBreakdown,
    isMock: true,
  }
}

// ──────────────────────────────────────────────
//  Real API fetch
// ──────────────────────────────────────────────
export async function fetchPaymentData(days = 30): Promise<PaymentPayload> {
  const apiUrl = process.env.NEXT_PUBLIC_API_URL || "https://green-api.khoav4.com"
  const token = getAccessToken()

  if (!token) return generateMockPayments(days)

  try {
    const res = await axios.get(`${apiUrl}/payments/analytics`, {
      params: { days },
      headers: { Authorization: `Bearer ${token}` },
      timeout: 8000,
    })
    const data = res.data?.data
    if (!data || !data.metrics) return generateMockPayments(days)
    return { ...data, isMock: false }
  } catch {
    return generateMockPayments(days)
  }
}

/** Create a Stripe checkout session via backend */
export async function createCheckoutSession(payload: {
  amount: number
  currency: string
  description: string
  successUrl: string
  cancelUrl: string
}): Promise<{ url: string }> {
  const apiUrl = process.env.NEXT_PUBLIC_API_URL || "https://green-api.khoav4.com"
  const token = getAccessToken()
  const res = await axios.post(`${apiUrl}/payments/create-checkout`, payload, {
    headers: { Authorization: `Bearer ${token}` },
  })
  return res.data?.data
}

export function formatCents(cents: number, currency = "USD"): string {
  return new Intl.NumberFormat("en-US", {
    style: "currency",
    currency,
    minimumFractionDigits: 0,
  }).format(cents / 100)
}
