import { useState, useCallback } from 'react';
import { authenticatedRequest, apiGet, apiPost, apiPut, apiDelete, apiPatch } from '@/lib/auth';
import { AxiosRequestConfig } from 'axios';

interface UseApiOptions {
  onSuccess?: (data: any) => void;
  onError?: (error: string) => void;
}

export function useApi() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const request = useCallback(async (
    config: AxiosRequestConfig,
    callbacks?: UseApiOptions
  ) => {
    setLoading(true);
    setError(null);

    try {
      const data = await authenticatedRequest(config);
      callbacks?.onSuccess?.(data);
      return data;
    } catch (err: any) {
      const errorMessage = err.message || 'An unexpected error occurred';
      setError(errorMessage);
      callbacks?.onError?.(errorMessage);
      return null;
    } finally {
      setLoading(false);
    }
  }, []);

  // Helper methods for common HTTP operations
  const get = useCallback(async (url: string, callbacks?: UseApiOptions) => {
    setLoading(true);
    setError(null);

    try {
      const data = await apiGet(url);
      callbacks?.onSuccess?.(data);
      return data;
    } catch (err: any) {
      const errorMessage = err.message || 'An unexpected error occurred';
      setError(errorMessage);
      callbacks?.onError?.(errorMessage);
      return null;
    } finally {
      setLoading(false);
    }
  }, []);

  const post = useCallback(async (url: string, data?: any, callbacks?: UseApiOptions) => {
    setLoading(true);
    setError(null);

    try {
      const result = await apiPost(url, data);
      callbacks?.onSuccess?.(result);
      return result;
    } catch (err: any) {
      const errorMessage = err.message || 'An unexpected error occurred';
      setError(errorMessage);
      callbacks?.onError?.(errorMessage);
      return null;
    } finally {
      setLoading(false);
    }
  }, []);

  const put = useCallback(async (url: string, data?: any, callbacks?: UseApiOptions) => {
    setLoading(true);
    setError(null);

    try {
      const result = await apiPut(url, data);
      callbacks?.onSuccess?.(result);
      return result;
    } catch (err: any) {
      const errorMessage = err.message || 'An unexpected error occurred';
      setError(errorMessage);
      callbacks?.onError?.(errorMessage);
      return null;
    } finally {
      setLoading(false);
    }
  }, []);

  const del = useCallback(async (url: string, callbacks?: UseApiOptions) => {
    setLoading(true);
    setError(null);

    try {
      const result = await apiDelete(url);
      callbacks?.onSuccess?.(result);
      return result;
    } catch (err: any) {
      const errorMessage = err.message || 'An unexpected error occurred';
      setError(errorMessage);
      callbacks?.onError?.(errorMessage);
      return null;
    } finally {
      setLoading(false);
    }
  }, []);

  const patch = useCallback(async (url: string, data?: any, callbacks?: UseApiOptions) => {
    setLoading(true);
    setError(null);

    try {
      const result = await apiPatch(url, data);
      callbacks?.onSuccess?.(result);
      return result;
    } catch (err: any) {
      const errorMessage = err.message || 'An unexpected error occurred';
      setError(errorMessage);
      callbacks?.onError?.(errorMessage);
      return null;
    } finally {
      setLoading(false);
    }
  }, []);

  return {
    request,
    get,
    post,
    put,
    delete: del,
    patch,
    loading,
    error,
    clearError: () => setError(null),
  };
}
