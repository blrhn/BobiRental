// src/pages/LoginPage.tsx
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";

export default function LoginPage() {
  const { login } = useAuth();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const navigate = useNavigate();

  const handleLogin = async () => {
    await login(username, password);
    navigate("/"); // go to warehouse after login
  };

  return (
    <div className="h-screen flex items-center justify-center bg-gray-100">
      <Card className="w-96">
        <CardContent className="p-6 grid gap-4">
          <h1 className="text-xl font-bold">Login</h1>
          <Input placeholder="Username" onChange={(e) => setUsername(e.target.value)} />
          <Input type="password" placeholder="Password" onChange={(e) => setPassword(e.target.value)} />
          <Button onClick={handleLogin}>Login</Button>
        </CardContent>
      </Card>
    </div>
  );
}
