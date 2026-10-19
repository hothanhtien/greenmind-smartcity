import { AxiosRequestConfig } from "axios"
import { apiClient, apiPost, apiPut, apiDelete, getAccessToken } from "./auth";

export interface CreateSurveyScenarioDTO {
    minAge: number;
    maxAge: number;
    location: string;
    percentage: number;
    gender?: string | null;
}
export const getAllSurveyScenarios = async (filters?: any) => {
    try {
        const token = getAccessToken();
        const headers: Record<string, string> = { "Content-Type": "application/json" };
        if (token) headers["Authorization"] = `Bearer ${token}`;

        const config: AxiosRequestConfig = {
            method: "GET",
            url: "/scenarios-survey/get-survey-scenario",
            headers,
            params: filters ?? undefined,
        };

        const res = await apiClient(config);
        return res.data;
    } catch (error) {
        throw error;
    }
};

export const createSurveyScenario = async (scenarioData: any) => {
    return apiPost('/scenarios-survey/create-survey-scenario', scenarioData)
}

export const attachQuestionSet = async (scenarioId: string, questionSetId: string) => {
    return apiPut(`/scenarios-survey/attach-question/${scenarioId}`, { questionSetId })
}


export const simulateSurveyScenario = async (scenarioId: string) => {
    return apiPost(`/scenarios-survey/simulate-scenario/${scenarioId}`, {})
}

export const getSimulatedScenario = async (scenarioId: string) => {
    try {
        const token = getAccessToken();
        const headers: Record<string, string> = { "Content-Type": "application/json" };
        if (token) headers["Authorization"] = `Bearer ${token}`;

        const config: AxiosRequestConfig = {
            method: "GET",
            url: `/scenarios-survey/get-simulated/${scenarioId}`,
            headers,
        };

        const res = await apiClient(config);
        return res.data;
    } catch (error: any) {
        console.error("Error in getSimulatedScenario:", error.response || error);
        throw error;
    }
}

export const deleteSurveyScenario = async (scenarioId: string) => {
    return apiDelete(`/scenarios-survey/delete-survey-scenarios/${scenarioId}`)
}