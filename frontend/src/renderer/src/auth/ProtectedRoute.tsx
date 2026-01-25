// src/auth/ProtectedRoute.tsx
import { ReactNode } from "react";
import { Navigate } from "react-router-dom";
import { useAuth } from "./AuthContext";

export const ProtectedRoute = ({ children }: { children: ReactNode }) => {
  const { auth } = useAuth();

  if (!auth) {
    return <Navigate to="/login" replace />;
  }

  return <>{children}</>;
};
