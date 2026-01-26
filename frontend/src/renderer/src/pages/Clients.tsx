// src/pages/Clients.tsx
import { useEffect, useState, useMemo } from "react";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { useAuth } from "@/auth/AuthContext";
import { authFetch } from "@/api";
import { toast } from "react-hot-toast";
import { Search, PlusCircle } from "lucide-react";

interface Fee {
  id: number;
  feeCategory: "RENTAL_FEE" | "PENALTY";
  actualFee: number;
  agreement?: {
    tool?: {
      id: number;
    };
  };
  isFeePaid: boolean;
}

interface Client {
  id: number;
  name: string;
  surname: string;
  clientAddress: string;
  clientMail: string;
  clientRemovalDate?: string;
  new: boolean;
  fees?: number;
  debt?: number;
  canCreate?: boolean;
  unpaidFees?: Fee[];
}

const Clients = () => {
  const { auth } = useAuth();

  const [clients, setClients] = useState<Client[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState("");

  const [showAddForm, setShowAddForm] = useState(false);
  const [newClient, setNewClient] = useState({
    name: "",
    surname: "",
    clientAddress: "",
    clientMail: "",
  });

  const fetchClients = async () => {
    setIsLoading(true);
    try {
      const res = await authFetch("/clients", {}, auth?.token);
      if (!res.ok) throw new Error("Failed to fetch clients");
      const data = await res.json();

      const enrichedClients = await Promise.all(
        data.map(async (client: Client) => {
          try {
            const [
              feesRes,
              debtRes,
              canCreateRes,
              unpaidFeesRes,
              overdueFeesRes,
            ] = await Promise.all([
              authFetch(`/clients/fees/${client.id}`, {}, auth?.token),
              authFetch(`/clients/debt/${client.id}`, {}, auth?.token),
              authFetch(`/clients/can_create/${client.id}`, {}, auth?.token),
              authFetch(`/fees/unpaid/${client.id}`, {}, auth?.token),
              authFetch(`/fees/overdue/${client.id}`, {}, auth?.token),
            ]);

            const unpaidFees = unpaidFeesRes.ok
              ? await unpaidFeesRes.json()
              : [];
            const overdueFees = overdueFeesRes.ok
              ? await overdueFeesRes.json()
              : [];

            return {
              ...client,
              fees: feesRes.ok ? await feesRes.json() : 0,
              debt: debtRes.ok ? await debtRes.json() : 0,
              canCreate: canCreateRes.ok ? await canCreateRes.json() : false,
              unpaidFees: [...unpaidFees, ...overdueFees],
            };
          } catch {
            return {
              ...client,
              fees: 0,
              debt: 0,
              canCreate: false,
              unpaidFees: [],
            };
          }
        })
      );

      setClients(enrichedClients);
    } catch (err) {
      console.error(err);
      toast.error("Failed to load clients");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchClients();
  }, []);

  const filteredClients = useMemo(() => {
    return clients.filter((c) =>
      `${c.name} ${c.surname} ${c.clientMail} ${c.clientAddress}`
        .toLowerCase()
        .includes(searchTerm.toLowerCase())
    );
  }, [clients, searchTerm]);

  // Funkcja do oznaczenia opłaty jako zapłaconej
  const setFeePaid = async (fee: Fee) => {
    try {
      const res = await authFetch(
        `/fees/${fee.id}`,
        {
          method: "PUT",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            feeCategory: fee.feeCategory,
            rentalAgreementId: fee.agreement?.tool?.id ?? 0, // dostosuj jeśli potrzebne
            clientId: fee.agreement?.tool?.id ?? 0, // albo użyj odpowiedniego clientId
            employeeId: auth?.id ?? 0,
            actualFee: fee.actualFee,
            feeDutyDate: new Date().toISOString().split("T")[0],
            feeFinalizedDate: new Date().toISOString().split("T")[0],
            isFeePaid: true,
          }),
        },
        auth?.token
      );

      if (!res.ok) throw new Error("Failed to mark fee as paid");

      toast.success("Fee marked as paid");
      fetchClients(); // odśwież listę
    } catch (err) {
      console.error(err);
      toast.error("Failed to set fee as paid");
    }
  };

  const createClient = async () => {
    if (
      !newClient.name.trim() ||
      !newClient.surname.trim() ||
      !newClient.clientAddress.trim() ||
      !newClient.clientMail.trim()
    ) {
      toast.error("FIll all required fields");
      return;
    }

    try {
      const res = await authFetch(
        "/clients",
        {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(newClient),
        },
        auth?.token
      );

      if (!res.ok) throw new Error("Failed to create client");

      toast.success("Client created successfully");
      setNewClient({
        name: "",
        surname: "",
        clientAddress: "",
        clientMail: "",
      });
      setShowAddForm(false);
      fetchClients();
    } catch {
      toast.error("Failed to create client");
    }
  };

  return (
    <div className="p-6 space-y-6 max-w-7xl mx-auto">
      <div className="flex justify-between items-center">
        <h1 className="text-3xl font-bold">Clients Management</h1>
        <Button onClick={() => setShowAddForm(!showAddForm)}>
          <PlusCircle className="mr-2 h-4 w-4" />
          {showAddForm ? "Cancel" : "Add New Client"}
        </Button>
      </div>

      <Card>
        <CardContent className="pt-6">
          <div className="relative">
            <Search className="absolute left-3 top-3 h-4 w-4 text-gray-400" />
            <Input
              placeholder="Search clients..."
              className="pl-10"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>
        </CardContent>
      </Card>
      {showAddForm && (
        <Card>
          <CardContent className="pt-6">
            <h2 className="text-lg font-semibold mb-4">Add New Client</h2>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <Input
                placeholder="Name *"
                value={newClient.name}
                onChange={(e) =>
                  setNewClient({ ...newClient, name: e.target.value })
                }
              />

              <Input
                placeholder="Surname *"
                value={newClient.surname}
                onChange={(e) =>
                  setNewClient({ ...newClient, surname: e.target.value })
                }
              />

              <Input
                placeholder="Address *"
                value={newClient.clientAddress}
                onChange={(e) =>
                  setNewClient({ ...newClient, clientAddress: e.target.value })
                }
              />

              <Input
                placeholder="Email *"
                value={newClient.clientMail}
                onChange={(e) =>
                  setNewClient({ ...newClient, clientMail: e.target.value })
                }
              />
            </div>

            <div className="flex gap-2 pt-4">
              <Button onClick={createClient} className="flex-1">
                Create Client
              </Button>
              <Button
                variant="outline"
                className="flex-1"
                onClick={() => setShowAddForm(false)}
              >
                Cancel
              </Button>
            </div>
          </CardContent>
        </Card>
      )}

      <Card>
        <CardContent className="pt-6">
          {isLoading ? (
            <div className="text-center py-8">Loading clients...</div>
          ) : (
            <div className="space-y-4">
              {filteredClients.map((c) => (
                <Card key={c.id} className="p-4 space-y-4">
                  <div>
                    <p className="font-semibold text-lg">
                      {c.name} {c.surname}{" "}
                      {c.new && (
                        <span className="text-sm text-green-600">(New)</span>
                      )}
                    </p>
                    <p className="text-sm text-gray-600">{c.clientMail}</p>
                    <p className="text-sm text-gray-600">{c.clientAddress}</p>

                    <div className="flex gap-2 mt-2 text-sm">
                      <span className="px-2 py-1 bg-blue-100 text-blue-800 rounded">
                        Fees: $
                        {(
                          c.unpaidFees
                            ?.filter((f) => !f.isFeePaid) // tylko nieopłacone
                            .reduce((sum, f) => sum + f.actualFee, 0) ?? 0
                        ).toFixed(2)}
                      </span>
                    </div>
                  </div>

                  {c.unpaidFees && c.unpaidFees.some((f) => !f.isFeePaid) && (
                    <div className="border-t pt-4 space-y-2">
                      <h3 className="font-semibold text-sm">Unpaid Fees</h3>

                      {c.unpaidFees.map((fee) => (
                        <div
                          key={fee.id}
                          className="flex items-center justify-between bg-gray-50 p-3 rounded"
                        >
                          <div className="text-sm">
                            <p>
                              <strong>Type:</strong> {fee.feeCategory}
                            </p>
                            <p>
                              <strong>Amount:</strong> $
                              {fee.actualFee.toFixed(2)}
                            </p>
                            <p>
                              <strong>Tool ID:</strong>{" "}
                              {fee.agreement?.tool?.id ?? "—"}
                            </p>
                          </div>

                          <Button
                            size="sm"
                            variant="destructive"
                            onClick={() => setFeePaid(fee)}
                          >
                            Set fee paid
                          </Button>
                        </div>
                      ))}
                    </div>
                  )}
                </Card>
              ))}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
};

export default Clients;
