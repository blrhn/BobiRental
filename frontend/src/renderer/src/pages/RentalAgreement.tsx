import { useEffect, useState } from "react";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { useAuth } from "@/auth/AuthContext";
import { authFetch } from "@/api";
import { toast } from "react-hot-toast";

/* ================= TYPES ================= */

interface Client {
  id: number;
  name: string;
  surname: string;
}

interface Tool {
  id: number;
  toolName: string;
  toolCategory: string;
  toolDescription: string;
  toolPrice: number;
  toolAvailabilityStatus: string;
}

interface RentalAgreement {
    id: number;
    toBeReviewed: boolean;
    agreementActualTerminationDate: string | null;
    hasPenalty: boolean;
    client: Client;
    tool: Tool;
  }
  
  

/* ================= COMPONENT ================= */

const RentalAgreementPage = () => {
  const { auth } = useAuth();
  const isManager = auth?.role === "WAREHOUSE_MANAGER";

  /* ===== CREATE AGREEMENT STATE ===== */
  const [clients, setClients] = useState<Client[]>([]);
  const [tools, setTools] = useState<Tool[]>([]);
  const [selectedClientId, setSelectedClientId] = useState<number | null>(null);
  const [selectedToolId, setSelectedToolId] = useState<number | null>(null);
  const [estimatedTerminationDate, setEstimatedTerminationDate] = useState("");
  const [agreementComment, setAgreementComment] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  /* ===== AGREEMENTS STATE ===== */
  const [agreements, setAgreements] = useState<RentalAgreement[]>([]);
  const [feeAmount, setFeeAmount] = useState<number>(0);
  const [loadingId, setLoadingId] = useState<number | null>(null);

  /* ================= FETCH ================= */

  const fetchClients = async () => {
    try {
      const res = await authFetch("/clients", {}, auth?.token);
      const data: Client[] = await res.json();

      const allowed = await Promise.all(
        data.map(async c => {
          const res = await authFetch(`/clients/can_create/${c.id}`, {}, auth?.token);
          return res.ok && (await res.json()) ? c : null;
        })
      );

      setClients(allowed.filter(Boolean) as Client[]);
    } catch {
      toast.error("Failed to load clients");
    }
  };

  const fetchTools = async () => {
    try {
      const res = await authFetch("/tools", {}, auth?.token);
      if (!res.ok) throw new Error("Failed to fetch tools");
  
      const data: Tool[] = await res.json();
  
      const availableTools = data.filter(
        (tool) => tool.toolAvailabilityStatus === "AVAILABLE"
      );
  
      setTools(availableTools);
    } catch (err) {
      console.error(err);
      toast.error("Failed to load tools");
    }
  };
  

  const fetchAgreements = async () => {
    try {
      const res = await authFetch("/rental_agreements", {}, auth?.token);
      setAgreements(await res.json());
    } catch {
      toast.error("Failed to load agreements");
    }
  };

  useEffect(() => {
    fetchClients();
    fetchTools();
    fetchAgreements();
  }, []);

  /* ================= CREATE AGREEMENT ================= */

  const createAgreement = async () => {
    if (!selectedClientId || !selectedToolId || !estimatedTerminationDate) {
      toast.error("Fill all required fields");
      return;
    }

    setIsSubmitting(true);
    try {
      const payload = {
        clientId: selectedClientId,
        toolId: selectedToolId,
        employeeId: auth?.id,
        agreementEstimatedTerminationDate: estimatedTerminationDate,
        agreementComment: agreementComment.trim(),
      };

      const res = await authFetch(
        "/rental_agreements/create",
        {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(payload),
        },
        auth?.token
      );

      if (!res.ok) throw new Error();

      toast.success("Rental agreement created");
      setSelectedClientId(null);
      setSelectedToolId(null);
      setEstimatedTerminationDate("");
      setAgreementComment("");
      fetchTools();
      fetchAgreements();
    } catch {
      toast.error("Failed to create agreement");
    } finally {
      setIsSubmitting(false);
    }
  };

  /* ================= ACTIONS ================= */

  const initiateReturn = async (agreementId: number, clientId: number) => {
    setLoadingId(agreementId);
    try {
      const res = await authFetch(
        `/rental_agreements/initiate-return/${agreementId}?clientId=${clientId}`,
        { method: "POST" },
        auth?.token
      );
  
      if (!res.ok) {
        const errText = await res.text();
        console.error("Initiate return error:", errText);
        toast.error(errText || "Failed to initialize return");
        return;
      }
  
      toast.success("Return initialized");
      fetchAgreements();
    } catch (err) {
      console.error(err);
      toast.error("Failed");
    } finally {
      setLoadingId(null);
    }
  };
  

  const closeAgreement = async (agreementId: number) => {
    const employeeId = auth?.id;
    if (!employeeId) {
      toast.error("Employee not identified");
      return;
    }
  
    setLoadingId(agreementId);
    try {
      const res = await authFetch(
        `/rental_agreements/close/${agreementId}?employeeId=${employeeId}`,
        { method: "POST" },
        auth?.token
      );
  
      if (!res.ok) {
        const errText = await res.text();
        console.error("Close agreement error:", errText);
        toast.error(errText || "Failed to close agreement");
        return;
      }
      toast.success("Agreement closed");
      fetchAgreements();
    } catch (err) {
      console.error(err);
      toast.error("Failed");
    } finally {
      setLoadingId(null);
    }
  };
  


  const reportDamagedTool = async (a: RentalAgreement) => {
    if (feeAmount <= 0) {
      toast.error("Fee must be > 0");
      return;
    }

    setLoadingId(a.id);
    try {
      await authFetch(
        `/tools/${a.tool.id}`,
        {
          method: "PUT",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            toolName: a.tool.toolName,
            toolCategory: a.tool.toolCategory,
            toolDescription: a.tool.toolDescription,
            toolPrice: a.tool.toolPrice,
            availabilityStatus: "UNAVAILABLE",
          }),
        },
        auth?.token
      );

      await authFetch(
        "/fees/create",
        {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            feeCategory: "PENALTY",
            rentalAgreementId: a.id,
            clientId: a.client.id,
            employeeId: auth?.id,
            actualFee: feeAmount,
            feeDutyDate: new Date().toISOString().split("T")[0],
            feeFinalizedDate: new Date().toISOString().split("T")[0],
          }),
        },
        auth?.token
      );

      await authFetch(
        `/rental_agreements/toggle-has-penalty/${a.id}`,
        { method: "PUT" },
        auth?.token
      );
      toast.success("Damaged tool reported");
      setFeeAmount(0);
      fetchAgreements();
    } catch {
      toast.error("Failed");
    } finally {
      setLoadingId(null);
    }
  };

  /* ================= UI ================= */

  const visibleAgreements = agreements.filter(
    a =>
      a.agreementActualTerminationDate !== null &&
      !a.hasPenalty
  );
  
  
  const active = visibleAgreements.filter(a => !a.toBeReviewed);
  const review = visibleAgreements.filter(a => a.toBeReviewed);
  

  return (
    <div className="p-6 max-w-5xl mx-auto space-y-10">

      {/* ===== CREATE AGREEMENT ===== */}
      <Card>
        <CardContent className="space-y-4">
          <h2 className="text-xl font-semibold">Create Rental Agreement</h2>

          <select value={selectedClientId ?? ""} onChange={e => setSelectedClientId(Number(e.target.value))}>
            <option value="">Select client</option>
            {clients.map(c => (
              <option key={c.id} value={c.id}>{c.name} {c.surname}</option>
            ))}
          </select>

          <select value={selectedToolId ?? ""} onChange={e => setSelectedToolId(Number(e.target.value))}>
            <option value="">Select tool</option>
            {tools.map(t => (
              <option key={t.id} value={t.id}>{t.toolName}</option>
            ))}
          </select>

          <Input type="date" value={estimatedTerminationDate} onChange={e => setEstimatedTerminationDate(e.target.value)} />

          <Textarea
            placeholder="Comment *"
            required
            value={agreementComment}
            onChange={e => setAgreementComment(e.target.value)}
          />

          <Button onClick={createAgreement} disabled={isSubmitting}>
            Create
          </Button>
        </CardContent>
      </Card>

      {/* ===== ACTIVE ===== */}
      <Card>
        <CardContent>
          <h2 className="text-xl font-semibold mb-4">Active Agreements</h2>
          {active.map(a => (
            <div key={a.id} className="flex justify-between mb-2">
              <span>{a.client.name} – {a.tool.toolName}</span>
              <Button onClick={() => initiateReturn(a.id, a.client.id)}>Initialize return</Button>
            </div>
          ))}
        </CardContent>
      </Card>

      {/* ===== TO REVIEW ===== */}
      {isManager && (
        <Card>
          <CardContent>
            <h2 className="text-xl font-semibold mb-4">To Review</h2>
            {review.map(a => (
              <div key={a.id} className="space-y-2 mb-4">
                <p>{a.client.name} – {a.tool.toolName} - {a.id}</p>
                <div className="flex gap-2">
                  <Button variant="outline" onClick={() => closeAgreement(a.id)}>Close</Button>
                  <Input type="number" placeholder="Fee" onChange={e => setFeeAmount(+e.target.value)} />
                  <Button variant="destructive" onClick={() => reportDamagedTool(a)}>
                    Report damaged
                  </Button>
                </div>
              </div>
            ))}
          </CardContent>
        </Card>
      )}
    </div>
  );
};

export default RentalAgreementPage;
