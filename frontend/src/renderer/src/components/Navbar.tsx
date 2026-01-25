import { NavLink } from "react-router-dom";
import { useAuth } from "@/auth/AuthContext";
import Logo from "@/assets/logo.svg";

export default function Navbar() {
  const { logout, auth } = useAuth();

  return (
    <nav className="flex items-center gap-6 px-6 py-4 border-b bg-white shadow-sm">
      <div className="text-lg font-bold text-emerald-600">
        <img src={Logo} alt="BobiRental Logo" className="w-12" />
      </div>

      <NavLink
        to="/"
        className={({ isActive }) =>
          isActive ? "font-semibold text-black" : "text-gray-500"
        }
      >
        Home
      </NavLink>
      <NavLink
        to="/warehouse"
        className={({ isActive }) =>
          ` ${isActive ? "font-semibold text-black" : "text-gray-500"}`
        }
      >
        Warehouse
      </NavLink>

      <NavLink
        to="/clients"
        className={({ isActive }) =>
          ` ${isActive ? "font-semibold text-black" : "text-gray-500"}`
        }
      >
        Clients
      </NavLink>

      <NavLink
        to="/rental-agreements"
        className={({ isActive }) =>
          ` ${isActive ? "font-semibold text-black" : "text-gray-500"}`
        }
      >
        Rental Agreements
      </NavLink>
      {auth?.role === "WAREHOUSE_MANAGER" && (
        <NavLink
          to="/admin"
          className={({ isActive }) =>
            isActive ? "font-semibold text-black" : "text-gray-500"
          }
        >
          Admin
        </NavLink>
      )}
      <div className="flex flex-col ml-auto ">
        <span className="text-sm text-gray-600">Role: {auth?.role}</span>
        <span className="text-sm text-gray-600">
          Username: {auth?.username} {auth?.id}
        </span>
      </div>
      <button onClick={logout} className="ml-4 text-red-500">
        Logout
      </button>
    </nav>
  );
}
