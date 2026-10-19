import { apiGet, apiPost, apiDelete, apiPut } from "./auth";

export const getMyQuestions = async () => {
    return apiGet('/questions/my-questions');
}

export const getMyQuestionSets = async () => {
    return apiGet('/question-sets/my-sets');
}

export const createQuestionSet = async (data: {
    name: string
    description: string
    questionIds: string[]
}) => {
    return apiPost('/question-sets', data);
}

export const updateQuestionSet = async (id: string, data: {
    name: string
    description: string
    questionIds: string[]
}) => {
    return apiPut(`/question-sets/${id}`, data);
}

export const deleteQuestionSet = async (id: string) => {
    return apiDelete(`/question-sets/${id}`);
}

export const updateQuestion = async (id: string, data: {
    question: string
}) => {
    return apiPut(`/questions/${id}`, data);
}

export const deleteQuestion = async (id: string) => {
    return apiDelete(`/questions/${id}`);
}
