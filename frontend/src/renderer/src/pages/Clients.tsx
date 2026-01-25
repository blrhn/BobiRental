// src/pages/Clients.tsx
import { useEffect, useState, useMemo } from "react";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { useAuth } from "@/auth/AuthContext";
import { authFetch } from "@/api";
import { toast } from "react-hot-toast";
import { Search, PlusCircle } from "lucide-react";

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
}

const Clients = () => {
  const { auth } = useAuth();
  const isManager = auth?.role === "WAREHOUSE_MANAGER";

  const [clients, setClients] = useState<Client[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  const [searchTerm, setSearchTerm] = useState("");

  // Add client form
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

      // Fetch extra info (fees, debt, canCreate) for each client
      const enrichedClients = await Promise.all(
        data.map(async (client: Client) => {
          try {
            const [feesRes, debtRes, canCreateRes] = await Promise.all([
              authFetch(`/clients/fees/${client.id}`, {}, auth?.token),
              authFetch(`/clients/debt/${client.id}`, {}, auth?.token),
              authFetch(`/clients/can_create/${client.id}`, {}, auth?.token),
            ]);

            const fees = feesRes.ok ? await feesRes.json() : 0;
            const debt = debtRes.ok ? await debtRes.json() : 0;
            const canCreate = canCreateRes.ok ? await canCreateRes.json() : false;

            return { ...client, fees, debt, canCreate };
          } catch {
            return { ...client, fees: 0, debt: 0, canCreate: false };
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
    if (!clients.length) return [];
    return clients.filter((c) =>
      `${c.name} ${c.surname} ${c.clientMail} ${c.clientAddress}`
        .toLowerCase()
        .includes(searchTerm.toLowerCase())
    );
  }, [clients, searchTerm]);

  const createClient = async () => {
    if (!newClient.name.trim() || !newClient.surname.trim()) {
      toast.error("Name and surname are required");
      return;
    }

    try {
      const res = await authFetch("/clients", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(newClient),
      }, auth?.token);

      if (!res.ok) {
        const errText = await res.text();
        console.error("Backend error:", errText);
        throw new Error("Backend rejected the request");
      }

      toast.success("Client created successfully");
      setNewClient({ name: "", surname: "", clientAddress: "", clientMail: "" });
      setShowAddForm(false);
      fetchClients();
    } catch (err) {
      console.error(err);
      toast.error("Failed to create client");
    }
  };

  const deleteClient = async (id: number) => {
    if (!confirm("Are you sure you want to delete this client?")) return;

    try {
      const res = await authFetch(`/clients/${id}`, { method: "DELETE" }, auth?.token);
      if (!res.ok) throw new Error();
      toast.success("Client deleted successfully");
      fetchClients();
    } catch (err) {
      console.error(err);
      toast.error("Failed to delete client");
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

      {/* Search */}
      <Card>
        <CardContent className="pt-6">
          <div className="relative">
            <Search className="absolute left-3 top-3 h-4 w-4 text-gray-400" />
            <Input
              placeholder="Search clients by name, surname, mail, or address..."
              className="pl-10"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>
          <div className="mt-4 text-sm text-gray-600">
            Showing {filteredClients.length} of {clients.length} clients
            {searchTerm && ` matching "${searchTerm}"`}
          </div>
        </CardContent>
      </Card>

      {/* Add client form */}
      {showAddForm && (
        <Card>
          <CardContent className="pt-6">
            <h2 className="text-lg font-semibold mb-4">Add New Client</h2>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <Input
                placeholder="Name *"
                value={newClient.name}
                onChange={(e) => setNewClient({ ...newClient, name: e.target.value })}
              />
              <Input
                placeholder="Surname *"
                value={newClient.surname}
                onChange={(e) => setNewClient({ ...newClient, surname: e.target.value })}
              />
              <Input
                placeholder="Address"
                value={newClient.clientAddress}
                onChange={(e) => setNewClient({ ...newClient, clientAddress: e.target.value })}
              />
              <Input
                placeholder="Email"
                value={newClient.clientMail}
                onChange={(e) => setNewClient({ ...newClient, clientMail: e.target.value })}
              />
              <div className="flex gap-2 pt-4">
                <Button onClick={createClient} className="flex-1">Create Client</Button>
                <Button variant="outline" onClick={() => setShowAddForm(false)} className="flex-1">
                  Cancel
                </Button>
              </div>
            </div>
          </CardContent>
        </Card>
      )}

      {/* Client list */}
      <Card>
        <CardContent className="pt-6">
          {isLoading ? (
            <div className="text-center py-8">Loading clients...</div>
          ) : filteredClients.length === 0 ? (
            <div className="text-center py-8 text-gray-500">
              {clients.length === 0
                ? "No clients found."
                : "No clients match your search."}
            </div>
          ) : (
            <div className="space-y-4">
              {filteredClients.map((c) => (
                <Card key={c.id} className="p-4">
                  <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
                    <div className="flex-1">
                      <p className="font-semibold text-lg">
                        {c.name} {c.surname} {c.new && <span className="text-sm text-green-600">(New)</span>}
                      </p>
                      <p className="text-sm text-gray-600">{c.clientMail}</p>
                      <p className="text-sm text-gray-600">{c.clientAddress}</p>
                      <div className="flex gap-2 mt-2 text-sm">
                        <span className="px-2 py-1 bg-blue-100 text-blue-800 rounded">
                          Fees: ${c.fees?.toFixed(2) || 0}
                        </span>
                        <span className="px-2 py-1 bg-red-100 text-red-800 rounded">
                          Debt: ${c.debt?.toFixed(2) || 0}
                        </span>
                        <span className="px-2 py-1 bg-green-100 text-green-800 rounded">
                          Can Create: {c.canCreate ? "Yes" : "No"}
                        </span>
                      </div>
                    </div>
                    {isManager && (
                      <div className="flex gap-2">
                        <Button variant="destructive" onClick={() => deleteClient(c.id)}>
                          Delete
                        </Button>
                      </div>
                    )}
                  </div>
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
