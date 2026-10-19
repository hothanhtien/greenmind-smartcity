import type { WasteReport } from "./waste-report";

export interface Campaign {
  id: string;
  name: string;
  description: string;
  startDate: string; // ISO date string
  endDate: string; // ISO date string
  lat: number;
  lng: number;
  radius: number;
  reportIds?: string[];
  reports?: WasteReport[];
  participants?: CampaignParticipant[];
  participantsCount?: number;
  createdAt?: string;
  updatedAt?: string;
  createdByUserId?: string;
  createdBy?: {
    id: string;
    fullName: string;
    username?: string;
  };
  status?: "PENDING" | "ONGOING" | "COMPLETED" | "CANCELLED";
}

export type ParticipantStatus = "PENDING" | "APPROVED" | "REJECTED" | "CHECKED_IN" | "COMPLETED";

export interface CampaignParticipant {
  id: string;
  campaignId: string;
  userId: string;
  user?: {
    id: string;
    fullName: string;
    username?: string;
    email?: string;
    phoneNumber?: string;
  };
  checkInTime?: string; // ISO string
  checkInLat?: number;
  checkInLng?: number;
  checkOutTime?: string;
  checkOutLat?: number;
  checkOutLng?: number;
  status: ParticipantStatus;
  createdAt: string;
  updatedAt: string;
}
