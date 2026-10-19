import axios, { AxiosRequestConfig } from 'axios';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'https://vodang-api.gauas.com';
const BROWSER_API_BASE_URL = '/api/backend';

const getApiBaseURL = () => {
  return typeof window !== 'undefined' ? BROWSER_API_BASE_URL : API_BASE_URL;
};

// API utility functions for authentication
export interface LoginResponse {
  message: string;
  user: {
    id: string;
    username: string;
    email: string;
    fullName: string;
    role: string;
  };
  access_token: string;
  refresh_token: string;
}

export interface EmailLoginPayload {
  email: string;
  password: string;
}

export interface GoogleLoginPayload {
  token: string;
}

// Get stored access token - Define this first before using in interceptors
export const getAccessToken = (): string | null => {
  if (typeof window !== 'undefined') {
    return localStorage.getItem('access_token');
  }
  return null;
};

// Get stored user data
export const getStoredUser = () => {
  if (typeof window !== 'undefined') {
    const user = localStorage.getItem('user');
    return user ? JSON.parse(user) : null;
  }
  return null;
};

// Clear authentication data
export const clearAuthData = () => {
  if (typeof window !== 'undefined') {
    localStorage.removeItem('access_token');
    localStorage.removeItem('refresh_token');
    localStorage.removeItem('user');
  }
};

// Create axios instance for authenticated requests (main API)
const apiClient = axios.create({
  baseURL: getApiBaseURL(),
  headers: {
    'Content-Type': 'application/json',
  },
});

// Create axios instance for AI API (separate base URL)
const aiApiClient = axios.create({
  baseURL: process.env.NEXT_PUBLIC_AI_URL || 'https://ai-greenmind.khoav4.com',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to add auth token for main API
apiClient.interceptors.request.use(
  (config) => {
    const token = getAccessToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Request interceptor to add auth token for AI API
aiApiClient.interceptors.request.use(
  (config) => {
    const token = getAccessToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor to handle errors for main API
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Token expired or invalid, redirect to login
      clearAuthData();
      if (typeof window !== 'undefined') {
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

// Response interceptor to handle errors for AI API
aiApiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Token expired or invalid, redirect to login
      clearAuthData();
      if (typeof window !== 'undefined') {
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

// Create authenticated axios wrapper
export const authenticatedRequest = async (config: AxiosRequestConfig) => {
  try {
    const response = await apiClient(config);
    return response.data;
  } catch (error) {
    if (axios.isAxiosError(error)) {
      throw new Error(error.response?.data?.message || error.message);
    }
    throw error;
  }
};

// Create authenticated AI API wrapper
export const authenticatedAiRequest = async (config: AxiosRequestConfig) => {
  try {
    const response = await aiApiClient(config);
    return response.data;
  } catch (error) {
    if (axios.isAxiosError(error)) {
      throw new Error(error.response?.data?.message || error.message);
    }
    throw error;
  }
};

// Helper functions for common HTTP methods with automatic auth headers
export const apiGet = async (url: string, config?: AxiosRequestConfig) => {
  return authenticatedRequest({ method: 'GET', url, ...config });
};

export const apiPost = async (url: string, data?: any, config?: AxiosRequestConfig) => {
  return authenticatedRequest({ method: 'POST', url, data, ...config });
};

export const apiPut = async (url: string, data?: any, config?: AxiosRequestConfig) => {
  return authenticatedRequest({ method: 'PUT', url, data, ...config });
};

export const apiDelete = async (url: string, config?: AxiosRequestConfig) => {
  return authenticatedRequest({ method: 'DELETE', url, ...config });
};

export const apiPatch = async (url: string, data?: any, config?: AxiosRequestConfig) => {
  return authenticatedRequest({ method: 'PATCH', url, data, ...config });
};

// Helper functions for AI API calls
export const aiApiGet = async (url: string, config?: AxiosRequestConfig) => {
  return authenticatedAiRequest({ method: 'GET', url, ...config });
};

export const aiApiPost = async (url: string, data?: any, config?: AxiosRequestConfig) => {
  return authenticatedAiRequest({ method: 'POST', url, data, ...config });
};

// Email/Password login
export const loginWithEmail = async (payload: EmailLoginPayload): Promise<LoginResponse> => {
  try {
    const response = await axios.post(`${getApiBaseURL()}/auth/login/email`, payload, {
      headers: {
        'Content-Type': 'application/json',
      },
    });
    return response.data;
  } catch (error) {
    if (axios.isAxiosError(error)) {
      throw new Error(error.response?.data?.message || 'Login failed');
    }
    throw new Error('Login failed');
  }
};

// Google login
export const loginWithGoogle = async (payload: GoogleLoginPayload): Promise<LoginResponse> => {
  try {
    const response = await axios.post(`${getApiBaseURL()}/auth/login/google`, payload, {
      headers: {
        'Content-Type': 'application/json',
      },
    });
    return response.data;
  } catch (error) {
    if (axios.isAxiosError(error)) {
      throw new Error(error.response?.data?.message || 'Google login failed');
    }
    throw new Error('Google login failed');
  }
};

// Check if user is authenticated
export const isAuthenticated = (): boolean => {
  return !!getAccessToken();
};

// Logout function
export const logout = () => {
  clearAuthData();
  window.location.href = '/login';
};

export const getAllModels = async (filters?: any) => {
  try {
    const token = getAccessToken();

    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };

    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const config: AxiosRequestConfig = {
      method: 'GET',
      url: '/models/getAll',
      headers: {
        Authorization: 'Bearer ' + token,
      },
    };

    if (filters) {
      config.params = filters;
    }

    // Use apiClient directly instead of going through authenticatedRequest
    const response = await apiClient(config);
    return response.data;
  } catch (error) {
    console.error('Error in getAllModels:', error);
    throw error;
  }
};

export const getModelById = async (id: string) => {
  return apiGet(`/models/${id}`);
};

export const createModel = async (modelData: any) => {
  return apiPost('/models/create', modelData);
};

export const updateModel = async (id: string, modelData: any) => {
  return apiPut(`/models/${id}`, modelData);
};

export const deleteModel = async (id: string) => {
  return apiDelete(`/models/${id}`);
};

// Questions API functions
export const getAllQuestions = async (filters?: any) => {
  try {
    const token = getAccessToken();
    // Explicitly create headers with Authorization
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };

    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const config: AxiosRequestConfig = {
      method: 'GET',
      url: '/questions',
      headers: headers,
    };

    if (filters) {
      config.params = filters;
    }

    // Use apiClient directly
    const response = await apiClient(config);
    return response.data;
  } catch (error) {
    console.error('❌ Error in getAllQuestions:', error);
    throw error;
  }
};

export const getQuestionById = async (id: string) => {
  return apiGet(`/questions/${id}`);
};

export const createQuestion = async (questionData: any) => {
  return apiPost('/questions/create', questionData);
};

// Create multiple questions at once
export const createQuestions = async (questionsData: any) => {
  try {
    const token = getAccessToken();

    // Explicitly create headers with Authorization
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };

    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const config: AxiosRequestConfig = {
      method: 'POST',
      url: '/questions/createQuestions',
      headers: headers,
      data: questionsData,
    };

    // Use apiClient directly
    const response = await apiClient(config);
    return response.data;
  } catch (error) {
    console.error('Error in createQuestions:', error);
    throw error;
  }
};

export const updateQuestion = async (id: string, questionData: any) => {
  return apiPut(`/questions/${id}`, questionData);
};

export const deleteQuestion = async (id: string) => {
  return apiDelete(`/questions/${id}`);
};

// Templates API functions
export const createTemplates = async (templatesData: any) => {
  try {
    const token = getAccessToken();

    // Explicitly create headers with Authorization
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };

    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const config: AxiosRequestConfig = {
      method: 'POST',
      url: '/templates/createTemplates',
      headers: headers,
      data: templatesData,
    };

    // Use apiClient directly
    const response = await apiClient(config);
    return response.data;
  } catch (error) {
    console.error('❌ Error in createTemplates:', error);
    throw error;
  }
};

export const getAllTemplates = async (filters?: any) => {
  return apiGet('/templates', filters ? { params: filters } : {});
};

export const getTemplateById = async (id: string) => {
  return apiGet(`/templates/${id}`);
};

export const updateTemplate = async (id: string, templateData: any) => {
  return apiPut(`/templates/${id}`, templateData);
};

export const deleteTemplate = async (id: string) => {
  return apiDelete(`/templates/${id}`);
};

// AI API functions (using https://ai-greenmind.khoav4.com)
export const generateKeywords = async (keywordData: any) => {
  return aiApiPost('/gen_keyword_ver2', keywordData);
};

export const generateTemplate = async (templateData: any) => {
  return aiApiPost('/gen_template', templateData);
};

export const combineQuestion = async (questionData: any) => {
  return aiApiPost('/combine_question', questionData);
};


// Campaign participant management
export const getPendingParticipants = async (campaignId: string) => {
  return apiGet(`/campaigns/${campaignId}/participants/pending`);
};

export const approveCampaignParticipant = async (campaignId: string, participantId: string) => {
  return apiPost(`/campaigns/${campaignId}/participants/${participantId}/approve`);
};

export const rejectCampaignParticipant = async (campaignId: string, participantId: string) => {
  return apiPost(`/campaigns/${campaignId}/participants/${participantId}/reject`);
};

export const getUsers = async () => {
  return apiGet('/auth/get-alls');
}

export { apiClient, aiApiClient };
