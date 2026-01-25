// src/pages/RentalAgreement.tsx
import { useEffect, useState } from "react";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { useAuth } from "@/auth/AuthContext";
import { authFetch } from "@/api";
import { toast } from "react-hot-toast";

const MAX_RENTAL_DAYS = 30;

interface Client {
  id: number;
  name: string;
  surname: string;
  clientMail: string;
}

interface Tool {
  id: number;
  toolName: string;
  toolAvailabilityStatus: "AVAILABLE" | "UNAVAILABLE" | "RENTED";
  toolCategory: string;
  toolPrice: number;
}

const RentalAgreement = () => {
  const { auth } = useAuth();
  const [clients, setClients] = useState<Client[]>([]);
  const [tools, setTools] = useState<Tool[]>([]);
  const [selectedClientId, setSelectedClientId] = useState<number | null>(null);
  const [selectedToolId, setSelectedToolId] = useState<number | null>(null);
  const [estimatedTerminationDate, setEstimatedTerminationDate] = useState("");
  const [agreementComment, setAgreementComment] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const fetchClients = async () => {
    try {
      const res = await authFetch("/clients", {}, auth?.token);
      if (!res.ok) throw new Error("Failed to fetch clients");
      const data: Client[] = await res.json();

      const clientsWhoCanCreate = await Promise.all(
        data.map(async (client) => {
          try {
            const canCreateRes = await authFetch(
              `/clients/can_create/${client.id}`,
              {},
              auth?.token
            );
            const canCreate = canCreateRes.ok
              ? await canCreateRes.json()
              : false;
            return canCreate ? client : null;
          } catch {
            return null;
          }
        })
      );

      setClients(clientsWhoCanCreate.filter((c): c is Client => c !== null));
    } catch (err) {
      console.error(err);
      toast.error("Failed to load clients");
    }
  };

  const fetchTools = async () => {
    try {
      const res = await authFetch("/tools", {}, auth?.token);
      if (!res.ok) throw new Error("Failed to fetch tools");
      const data: Tool[] = await res.json();
      const availableTools = data.filter(
        (t) => t.toolAvailabilityStatus === "AVAILABLE"
      );
      setTools(availableTools);
    } catch (err) {
      console.error(err);
      toast.error("Failed to load tools");
    }
  };

  useEffect(() => {
    fetchClients();
    fetchTools();
  }, []);

  const createAgreement = async () => {
    if (!selectedClientId || !selectedToolId || !estimatedTerminationDate) {
      toast.error("Please fill all required fields");
      return;
    }

    const executionDate = new Date();
    const estimatedDate = new Date(estimatedTerminationDate);

    executionDate.setHours(0, 0, 0, 0);
    estimatedDate.setHours(0, 0, 0, 0);

    // --- Validate estimated termination date ---
    if (estimatedDate <= executionDate) {
      toast.error("Estimated termination date must be after today");
      setEstimatedTerminationDate(""); // ❌ Reset invalid date
      return;
    }

    const diffDays =
      (estimatedDate.getTime() - executionDate.getTime()) /
      (1000 * 60 * 60 * 24);

    if (diffDays > MAX_RENTAL_DAYS) {
      toast.error(`Rental period cannot exceed ${MAX_RENTAL_DAYS} days`);
      // Optionally reset to a max-allowed date
      const maxDate = new Date(executionDate);
      maxDate.setDate(maxDate.getDate() + MAX_RENTAL_DAYS);
      setEstimatedTerminationDate(maxDate.toISOString().split("T")[0]);
      return;
    }

    setIsSubmitting(true);

    try {
      const employeeId = auth?.id;
      if (!employeeId) {
        toast.error("Employee not identified");
        return;
      }

      const payload = {
        clientId: Number(selectedClientId),
        toolId: Number(selectedToolId),
        employeeId: Number(employeeId),
        agreementEstimatedTerminationDate: estimatedTerminationDate,
        agreementComment: agreementComment.trim(),
      };

      console.log("Creating rental agreement:", payload);

      const res = await authFetch(
        "/rental_agreements/create",
        {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(payload),
        },
        auth?.token
      );

      if (!res.ok) {
        const errText = await res.text();
        console.error("Backend error:", errText);
        toast.error(errText || "Failed to create rental agreement");
        return;
      }

      const agreementId = await res.json();
      toast.success(`Rental Agreement #${agreementId} created`);

      // Reset form
      setSelectedClientId(null);
      setSelectedToolId(null);
      setEstimatedTerminationDate("");
      setAgreementComment("");
      fetchTools();
    } catch (err) {
      console.error(err);
      toast.error("Failed to create rental agreement");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="p-6 max-w-3xl mx-auto space-y-6">
      <h1 className="text-3xl font-bold">Create Rental Agreement</h1>

      <Card>
        <CardContent className="space-y-4">
          {/* Client Select */}
          <div>
            <label className="text-sm font-medium mb-1 block">
              Select Client *
            </label>
            <Select
              value={selectedClientId?.toString() || ""}
              onValueChange={(v) => setSelectedClientId(Number(v))}
            >
              <SelectTrigger>
                <SelectValue placeholder="Select a client" />
              </SelectTrigger>
              <SelectContent>
                {clients.map((c) => (
                  <SelectItem key={c.id} value={c.id.toString()}>
                    {c.name} {c.surname} ({c.clientMail})
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          {/* Tool Select */}
          <div>
            <label className="text-sm font-medium mb-1 block">
              Select Tool *
            </label>
            <Select
              value={selectedToolId?.toString() || ""}
              onValueChange={(v) => setSelectedToolId(Number(v))}
            >
              <SelectTrigger>
                <SelectValue placeholder="Select a tool" />
              </SelectTrigger>
              <SelectContent>
                {tools.map((t) => (
                  <SelectItem key={t.id} value={t.id.toString()}>
                    {t.toolName} ({t.toolCategory}) - ${t.toolPrice.toFixed(2)}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          {/* Estimated Termination Date */}
          <div>
            <label className="text-sm font-medium mb-1 block">
              Estimated Termination Date - max 30 days from today*
            </label>
            <Input
              type="date"
              value={estimatedTerminationDate}
              onChange={(e) => setEstimatedTerminationDate(e.target.value)}
            />
          </div>

          {/* Comment */}
          <div>
            <label className="text-sm font-medium mb-1 block">
              Description / Comment * {/* <-- Add the * */}
            </label>
            <Input
              placeholder="Enter a comment"
              value={agreementComment}
              onChange={(e) => setAgreementComment(e.target.value)}
              className={
                !agreementComment.trim() && isSubmitting ? "border-red-500" : ""
              }
            />
          </div>

          {/* Submit */}
          <Button onClick={createAgreement} disabled={isSubmitting}>
            {isSubmitting ? "Creating..." : "Create Rental Agreement"}
          </Button>
        </CardContent>
      </Card>
    </div>
  );
};

export default RentalAgreement;
