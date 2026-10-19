"use client";

import { useEffect, useState } from "react";
import { useRouter, usePathname } from "next/navigation";
import { useAuth } from "@/contexts/AuthContext";

interface ProtectedRouteProps {
  children: React.ReactNode;
}

const publicRoutes = ["/login"];

export default function ProtectedRoute({ children }: ProtectedRouteProps) {
  const { isAuth, isLoading, isHydrated } = useAuth();
  const router = useRouter();
  const pathname = usePathname();
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
  }, []);

  useEffect(() => {
    if (isHydrated && !isLoading && mounted) {
      const isPublicRoute = publicRoutes.includes(pathname);

      if (!isAuth && !isPublicRoute) {
        router.push("/login");
      } else if (isAuth && pathname === "/login") {
        router.push("/dashboard");
      }
    }
  }, [isAuth, isLoading, isHydrated, pathname, router, mounted]);

  // Always render the same structure to avoid hydration mismatch
  if (!mounted || !isHydrated || isLoading) {
    return (
      <div className="flex h-screen items-center justify-center bg-background">
        <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-primary"></div>
      </div>
    );
  }

  const isPublicRoute = publicRoutes.includes(pathname);
  if (!isAuth && !isPublicRoute) {
    return (
      <div className="flex h-screen items-center justify-center bg-background">
        <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-primary"></div>
      </div>
    );
  }

  return <>{children}</>;
}
