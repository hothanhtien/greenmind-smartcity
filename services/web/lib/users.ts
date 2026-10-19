import { apiGet, apiPost, apiDelete, apiPut, getAccessToken } from "./auth";
import { AxiosRequestConfig } from "axios";

export interface UserWithOCEAN {
    id: string;
    userId: string;
    username: string;
    fullName: string;
    email: string;
    age: number;
    gender: string;
    location: string;

    // OCEAN scores (0-100)
    ocean: {
        openness: number;
        conscientiousness: number;
        extraversion: number;
        agreeableness: number;
        neuroticism: number;
    };

    // Survey/Scenario info
    scenariosAssigned: number;
    scenariosCompleted: number;
    completionRate: number;

    // Metadata
    createdAt: string;
    updatedAt: string;
    lastActive?: string;
}

export interface UserStats {
    totalUsers: number;
    activeUsers: number;
    averageOceanScores: {
        openness: number;
        conscientiousness: number;
        extraversion: number;
        agreeableness: number;
        neuroticism: number;
    };
}

// Fetch all users with OCEAN scores
export const getAllUsersWithOCEAN = async (filters?: any) => {
    try {
        const token = getAccessToken();
        const headers: Record<string, string> = {
            "Content-Type": "application/json",
        };

        if (token) {
            headers["Authorization"] = `Bearer ${token}`;
        }

        const config: AxiosRequestConfig = {
            method: "GET",
            url: "/big-five",
            headers,
        };

        if (filters) {
            config.params = filters;
        }

        const response = await apiGet("/big-five", filters ? { params: filters } : {});
        return response;
    } catch (error) {
        console.error("Error fetching users with OCEAN:", error);
        throw error;
    }
};


