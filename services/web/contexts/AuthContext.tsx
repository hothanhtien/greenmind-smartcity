"use client";

import { createContext, useContext, useEffect, useState, ReactNode } from "react";
import { getStoredUser, clearAuthData, isAuthenticated } from "@/lib/auth";

interface User {
  id: string;
  username: string;
  email: string;
  fullName: string;
  role: string;
}

interface AuthContextType {
  user: User | null;
  isLoading: boolean;
  login: (user: User, accessToken: string, refreshToken: string) => void;
  logout: () => void;
  isAuth: boolean;
  isHydrated: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isHydrated, setIsHydrated] = useState(false);

  useEffect(() => {
    // Mark as hydrated first to prevent hydration mismatch
    setIsHydrated(true);

    // Check if user is logged in on app start
    const storedUser = getStoredUser();
    const authenticated = isAuthenticated();

    if (authenticated && storedUser) {
      setUser(storedUser);
    }

    setIsLoading(false);
  }, []);

  const login = (userData: User, accessToken: string, refreshToken: string) => {
    localStorage.setItem("access_token", accessToken);
    localStorage.setItem("refresh_token", refreshToken);
    localStorage.setItem("user", JSON.stringify(userData));
    setUser(userData);
  };

  const logout = () => {
    clearAuthData();
    setUser(null);
  };

  const value = {
    user,
    isLoading,
    login,
    logout,
    isAuth: !!user,
    isHydrated,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
}
