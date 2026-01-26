import { useEffect, useState, useMemo } from "react";
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
import { Search, Filter, PlusCircle } from "lucide-react";

const Warehouse = () => {
  const { auth } = useAuth();
  const isManager = auth?.role === "WAREHOUSE_MANAGER";

  const [tools, setTools] = useState<any[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  const [searchTerm, setSearchTerm] = useState("");
  const [filterCategory, setFilterCategory] = useState("ALL");
  const [filterStatus, setFilterStatus] = useState("ALL");

  const [editingToolId, setEditingToolId] = useState<number | null>(null);
  const [editToolData, setEditToolData] = useState({
    toolName: "",
    toolDescription: "",
    toolCategory: "DRILL",
    toolPrice: 0,
    availabilityStatus: "AVAILABLE",
  });

  const [newTool, setNewTool] = useState({
    toolName: "",
    toolDescription: "",
    toolCategory: "DRILL",
    toolPrice: 0,
    availabilityStatus: "AVAILABLE",
  });
  const [showAddForm, setShowAddForm] = useState(false);

  const fetchAllTools = async () => {
    setIsLoading(true);
    try {
      const res = await authFetch("/tools", {}, auth?.token);
      if (!res.ok) throw new Error("Failed to fetch tools");
      const data = await res.json();
      setTools(data);
    } catch (error) {
      toast.error("Failed to load tools");
      console.error(error);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchAllTools();
  }, []);

  const filteredTools = useMemo(() => {
    if (!tools.length) return [];

    return tools.filter((tool) => {
      const matchesSearch =
        searchTerm === "" ||
        tool.id.toString().includes(searchTerm) ||
        tool.toolName.toLowerCase().includes(searchTerm.toLowerCase()) ||
        tool.toolDescription?.toLowerCase().includes(searchTerm.toLowerCase());

      const matchesCategory =
        filterCategory === "ALL" || tool.toolCategory === filterCategory;

      const matchesStatus =
        filterStatus === "ALL" || tool.toolAvailabilityStatus === filterStatus;

      return matchesSearch && matchesCategory && matchesStatus;
    });
  }, [tools, searchTerm, filterCategory, filterStatus]);

  const handleResetFilters = () => {
    setSearchTerm("");
    setFilterCategory("ALL");
    setFilterStatus("ALL");
  };

  const createTool = async () => {
    if (!newTool.toolName?.trim()) {
      toast.error("Tool name is required");
      return;
    }

    if (Number(newTool.toolPrice) <= 0) {
      toast.error("Price must be greater than 0");
      return;
    }

    const payload = {
      toolName: newTool.toolName.trim(),
      availabilityStatus: ["AVAILABLE", "UNAVAILABLE"].includes(
        newTool.availabilityStatus
      )
        ? newTool.availabilityStatus
        : "AVAILABLE",
      toolDescription: newTool.toolDescription?.trim() || "",
      toolCategory: ["DRILL", "CUTTER", "SAW"].includes(newTool.toolCategory)
        ? newTool.toolCategory
        : "DRILL",
      toolPrice: Number(newTool.toolPrice),
    };

    try {
      const res = await authFetch(
        "/tools",
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
        throw new Error("Failed to create tool");
      }

      toast.success("Tool created successfully");
      fetchAllTools();

      setNewTool({
        toolName: "",
        toolDescription: "",
        toolCategory: "DRILL",
        toolPrice: 0,
        availabilityStatus: "AVAILABLE",
      });
      setShowAddForm(false);
    } catch (err) {
      console.error(err);
      toast.error("Failed to create tool. Check console for details.");
    }
  };

  const saveTool = async (id: number) => {
    if (!editToolData.toolName.trim()) {
      toast.error("Tool name is required");
      return;
    }

    if (editToolData.toolPrice <= 0) {
      toast.error("Price must be greater than 0");
      return;
    }

    try {
      const res = await authFetch(
        `/tools/${id}`,
        {
          method: "PUT",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(editToolData),
        },
        auth?.token
      );

      if (!res.ok) throw new Error();

      toast.success("Tool updated successfully");
      setEditingToolId(null);
      fetchAllTools();
    } catch {
      toast.error("Failed to update tool");
    }
  };

  const toggleAvailability = async (tool: any) => {
    if (tool.toolAvailabilityStatus === "RENTED") {
      toast.error("Cannot change status of a rented tool");
      return;
    }

    const newStatus =
      tool.toolAvailabilityStatus === "AVAILABLE" ? "UNAVAILABLE" : "AVAILABLE";

    try {
      const res = await authFetch(
        `/tools/${tool.id}`,
        {
          method: "PUT",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ ...tool, availabilityStatus: newStatus }),
        },
        auth?.token
      );

      if (!res.ok) throw new Error();

      toast.success(`Tool marked as ${newStatus}`);
      fetchAllTools();
    } catch {
      toast.error("Failed to update tool status");
    }
  };

  const categories = ["DRILL", "CUTTER", "SAW"];
  const statuses = ["AVAILABLE", "UNAVAILABLE", "RENTED"];

  return (
    <div className="p-6 space-y-6 max-w-7xl mx-auto">
      <div className="flex justify-between items-center">
        <h1 className="text-3xl font-bold">Warehouse Tool Management</h1>
        {isManager && (
          <Button onClick={() => setShowAddForm(!showAddForm)}>
            <PlusCircle className="mr-2 h-4 w-4" />
            {showAddForm ? "Cancel" : "Add New Tool"}
          </Button>
        )}
      </div>

      <Card>
        <CardContent className="pt-6">
          <div className="flex items-center gap-2 mb-4">
            <Filter className="h-5 w-5 text-gray-500" />
            <h2 className="text-lg font-semibold">Search & Filter Tools</h2>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
            <div className="relative">
              <Search className="absolute left-3 top-3 h-4 w-4 text-gray-400" />
              <Input
                placeholder="Search by ID, name, or description..."
                className="pl-10"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
            </div>

            <Select value={filterCategory} onValueChange={setFilterCategory}>
              <SelectTrigger>
                <SelectValue placeholder="Category" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="ALL">All Categories</SelectItem>
                {categories.map((cat) => (
                  <SelectItem key={cat} value={cat}>
                    {cat}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>

            <Select value={filterStatus} onValueChange={setFilterStatus}>
              <SelectTrigger>
                <SelectValue placeholder="Status" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="ALL">All Statuses</SelectItem>
                {statuses.map((status) => (
                  <SelectItem key={status} value={status}>
                    {status}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>

            <Button
              variant="outline"
              onClick={handleResetFilters}
              disabled={
                !searchTerm &&
                filterCategory === "ALL" &&
                filterStatus === "ALL"
              }
            >
              Reset Filters
            </Button>
          </div>

          <div className="mt-4 text-sm text-gray-600">
            Showing {filteredTools.length} of {tools.length} tools
            {searchTerm && ` matching "${searchTerm}"`}
            {filterCategory !== "ALL" && ` in category: ${filterCategory}`}
            {filterStatus !== "ALL" && ` with status: ${filterStatus}`}
          </div>
        </CardContent>
      </Card>

      {isManager && showAddForm && (
        <Card>
          <CardContent className="pt-6">
            <h2 className="text-lg font-semibold mb-4">Add New Tool</h2>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="space-y-4">
                <div>
                  <label className="text-sm font-medium mb-1 block">
                    Tool Name *
                  </label>
                  <Input
                    placeholder="Enter tool name"
                    value={newTool.toolName}
                    onChange={(e) =>
                      setNewTool({ ...newTool, toolName: e.target.value })
                    }
                  />
                </div>
                <div>
                  <label className="text-sm font-medium mb-1 block">
                    Description
                  </label>
                  <Input
                    placeholder="Enter description"
                    value={newTool.toolDescription}
                    onChange={(e) =>
                      setNewTool({
                        ...newTool,
                        toolDescription: e.target.value,
                      })
                    }
                  />
                </div>
                <div>
                  <label className="text-sm font-medium mb-1 block">
                    Price *
                  </label>
                  <Input
                    type="number"
                    min="0"
                    step="0.01"
                    placeholder="0.00"
                    value={newTool.toolPrice || ""}
                    onChange={(e) =>
                      setNewTool({
                        ...newTool,
                        toolPrice: Number(e.target.value),
                      })
                    }
                  />
                </div>
              </div>
              <div className="space-y-4">
                <div>
                  <label className="text-sm font-medium mb-1 block">
                    Category
                  </label>
                  <Select
                    value={newTool.toolCategory}
                    onValueChange={(v) =>
                      setNewTool({ ...newTool, toolCategory: v })
                    }
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Select category" />
                    </SelectTrigger>
                    <SelectContent>
                      {categories.map((cat) => (
                        <SelectItem key={cat} value={cat}>
                          {cat}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
                <div>
                  <label className="text-sm font-medium mb-1 block">
                    Initial Status
                  </label>
                  <Select
                    value={newTool.availabilityStatus}
                    onValueChange={(v) =>
                      setNewTool({ ...newTool, availabilityStatus: v })
                    }
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Select status" />
                    </SelectTrigger>
                    <SelectContent>
                      {statuses.map((status) => (
                        <SelectItem key={status} value={status}>
                          {status}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
                <div className="flex gap-2 pt-4">
                  <Button onClick={createTool} className="flex-1">
                    Create Tool
                  </Button>
                  <Button
                    variant="outline"
                    onClick={() => setShowAddForm(false)}
                    className="flex-1"
                  >
                    Cancel
                  </Button>
                </div>
              </div>
            </div>
          </CardContent>
        </Card>
      )}

      <Card>
        <CardContent className="pt-6">
          {isLoading ? (
            <div className="text-center py-8">Loading tools...</div>
          ) : filteredTools.length === 0 ? (
            <div className="text-center py-8 text-gray-500">
              {tools.length === 0
                ? "No tools found in warehouse."
                : "No tools match your search criteria."}
            </div>
          ) : (
            <div className="space-y-4">
              {filteredTools.map((tool) => {
                const isEditing = editingToolId === tool.id;
                const isUnavailable =
                  tool.toolAvailabilityStatus === "UNAVAILABLE";

                return (
                  <Card
                    key={tool.id}
                    className={`p-4 ${
                      isUnavailable ? "bg-gray-50 border-gray-200" : ""
                    }`}
                  >
                    <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
                      {isEditing ? (
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 w-full">
                          <div className="space-y-2">
                            <Input
                              placeholder="Tool Name"
                              value={editToolData.toolName}
                              onChange={(e) =>
                                setEditToolData({
                                  ...editToolData,
                                  toolName: e.target.value,
                                })
                              }
                            />
                            <Input
                              placeholder="Description"
                              value={editToolData.toolDescription}
                              onChange={(e) =>
                                setEditToolData({
                                  ...editToolData,
                                  toolDescription: e.target.value,
                                })
                              }
                            />
                          </div>
                          <div className="space-y-2">
                            <Select
                              value={editToolData.toolCategory}
                              onValueChange={(v) =>
                                setEditToolData({
                                  ...editToolData,
                                  toolCategory: v,
                                })
                              }
                            >
                              <SelectTrigger>
                                <SelectValue placeholder="Category" />
                              </SelectTrigger>
                              <SelectContent>
                                {categories.map((cat) => (
                                  <SelectItem key={cat} value={cat}>
                                    {cat}
                                  </SelectItem>
                                ))}
                              </SelectContent>
                            </Select>
                            <div className="flex gap-2">
                              <Input
                                type="number"
                                min="0"
                                step="0.01"
                                placeholder="Price"
                                value={editToolData.toolPrice || ""}
                                onChange={(e) =>
                                  setEditToolData({
                                    ...editToolData,
                                    toolPrice: Number(e.target.value),
                                  })
                                }
                              />
                              <Select
                                value={editToolData.availabilityStatus}
                                onValueChange={(v) =>
                                  setEditToolData({
                                    ...editToolData,
                                    availabilityStatus: v,
                                  })
                                }
                              >
                                <SelectTrigger className="w-[150px]">
                                  <SelectValue placeholder="Status" />
                                </SelectTrigger>
                                <SelectContent>
                                  {statuses.map((status) => (
                                    <SelectItem key={status} value={status}>
                                      {status}
                                    </SelectItem>
                                  ))}
                                </SelectContent>
                              </Select>
                            </div>
                            <div className="flex gap-2">
                              <Button onClick={() => saveTool(tool.id)}>
                                Save Changes
                              </Button>
                              <Button
                                variant="outline"
                                onClick={() => setEditingToolId(null)}
                              >
                                Cancel
                              </Button>
                            </div>
                          </div>
                        </div>
                      ) : (
                        <>
                          <div className="flex-1">
                            <div className="flex items-start gap-3">
                              <div
                                className={`w-3 h-3 rounded-full mt-1 ${
                                  tool.toolAvailabilityStatus === "AVAILABLE"
                                    ? "bg-green-500"
                                    : tool.toolAvailabilityStatus ===
                                      "UNAVAILABLE"
                                    ? "bg-red-500"
                                    : tool.toolAvailabilityStatus ===
                                      "MAINTENANCE"
                                    ? "bg-yellow-500"
                                    : "bg-blue-500"
                                }`}
                              />
                              <div>
                                <div className="flex items-center gap-2">
                                  <p className="font-semibold text-lg">
                                    {tool.toolName}
                                  </p>
                                  <span className="text-xs px-2 py-1 bg-gray-100 rounded">
                                    ID: {tool.id}
                                  </span>
                                </div>
                                <p className="text-sm text-gray-600 mt-1">
                                  {tool.toolDescription}
                                </p>
                                <div className="flex flex-wrap gap-3 mt-2 text-sm">
                                  <span className="px-2 py-1 bg-blue-100 text-blue-800 rounded">
                                    Category: {tool.toolCategory}
                                  </span>
                                  <span
                                    className={`px-2 py-1 rounded ${
                                      tool.toolAvailabilityStatus ===
                                      "AVAILABLE"
                                        ? "bg-green-100 text-green-800"
                                        : tool.toolAvailabilityStatus ===
                                          "UNAVAILABLE"
                                        ? "bg-red-100 text-red-800"
                                        : tool.toolAvailabilityStatus ===
                                          "MAINTENANCE"
                                        ? "bg-yellow-100 text-yellow-800"
                                        : "bg-blue-100 text-blue-800"
                                    }`}
                                  >
                                    Status: {tool.toolAvailabilityStatus}
                                  </span>
                                  <span className="px-2 py-1 bg-purple-100 text-purple-800 rounded">
                                    Price: ${tool.toolPrice.toFixed(2)}
                                  </span>
                                </div>
                              </div>
                            </div>
                          </div>

                          {isManager && (
                            <div className="flex gap-2">
                              <Button
                                variant="outline"
                                onClick={() => {
                                  setEditingToolId(tool.id);
                                  setEditToolData({
                                    toolName: tool.toolName,
                                    toolDescription: tool.toolDescription || "",
                                    toolCategory: tool.toolCategory,
                                    toolPrice: tool.toolPrice,
                                    availabilityStatus:
                                      tool.toolAvailabilityStatus,
                                  });
                                }}
                              >
                                Edit
                              </Button>

                              <Button
                                variant={
                                  tool.toolAvailabilityStatus === "AVAILABLE"
                                    ? "destructive"
                                    : "secondary"
                                }
                                onClick={() => toggleAvailability(tool)}
                                disabled={
                                  tool.toolAvailabilityStatus === "RENTED"
                                }
                              >
                                {tool.toolAvailabilityStatus === "AVAILABLE"
                                  ? "Set as Unavailable"
                                  : tool.toolAvailabilityStatus ===
                                    "UNAVAILABLE"
                                  ? "Make Available"
                                  : "Rented"}
                              </Button>
                            </div>
                          )}
                        </>
                      )}
                    </div>
                  </Card>
                );
              })}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
};

export default Warehouse;
