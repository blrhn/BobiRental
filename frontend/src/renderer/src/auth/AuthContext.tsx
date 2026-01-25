// src/auth/AuthContext.tsx
import { createContext, useContext, useState, ReactNode } from "react";
import { API_BASE } from "@/api";

type AuthType = {
  id: number;
  username: string;
  role: string;
  token: string;
};

type AuthContextType = {
  auth: AuthType | null;
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
};

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [auth, setAuth] = useState<AuthType | null>(() => {
    const saved = localStorage.getItem("auth");
    return saved ? JSON.parse(saved) : null;
  });

  const login = async (username: string, password: string) => {
    const res = await fetch(`${API_BASE}/employees/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ employeeLogin: username, employeePassword: password }),
    });

    if (!res.ok) throw new Error("Login failed");

    const data = await res.json();

    const authData: AuthType = {
      id: data.id,
      username: data.username,
      role: data.role,
      token: btoa(`${username}:${password}`), // basic auth for other requests
    };

    setAuth(authData);
    localStorage.setItem("auth", JSON.stringify(authData));
  };

  const logout = () => {
    setAuth(null);
    localStorage.removeItem("auth");
  };

  return (
    <AuthContext.Provider value={{ auth, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) throw new Error("useAuth must be used within AuthProvider");
  return context;
};
