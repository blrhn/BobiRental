// src/App.tsx
import { BrowserRouter, Routes, Route } from "react-router-dom";
import { AuthProvider, useAuth } from "@/auth/AuthContext";
import { ProtectedRoute } from "@/auth/ProtectedRoute";
import LoginPage from "@/pages/LoginPage";
import HomePage from "@/pages/HomePage";
import Warehouse from "@/pages/Warehouse";
import Clients from "@/pages/Clients";
import Navbar from "@/components/Navbar";
import RentalAgreement from "@/pages/RentalAgreement";
import { Toaster } from 'react-hot-toast';

function AppRoutes() {
  const { auth } = useAuth();

  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route
        path="/*"
        element={
          <ProtectedRoute>
            <Navbar />
            <Toaster />
            <Routes>
              <Route path="/" element={<HomePage />} />
              {/* All employees can access Warehouse page */}
              <Route path="/warehouse" element={<Warehouse />} />
              <Route path="/clients" element={<Clients/>} />
              <Route path="/rental-agreements" element={<RentalAgreement/>} />
              {/* Only warehouse managers can see orders or admin pages */}
              {auth?.role === "WAREHOUSE_MANAGER" && (
                <>
                  {/* <Route path="/orders" element={<OrdersPage />} /> */}
                  {/* <Route path="/reports" element={<ReportsPage />} /> */}
                </>
              )}
            </Routes>
          </ProtectedRoute>
        }
      />
    </Routes>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <AppRoutes />
      </BrowserRouter>
    </AuthProvider>
  );
}
