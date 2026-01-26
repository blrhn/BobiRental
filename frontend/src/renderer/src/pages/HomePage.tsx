import { Card, CardContent } from "@/components/ui/card";

export default function HomePage() {
  return (
    <div className="p-6">
      <Card>
        <CardContent className="p-6">
          <h1 className="text-3xl font-bold text-emerald-600">
            Welcome to Warehouse Dashboard
          </h1>
          <p className="mt-2 text-gray-600">
            Use the navigation bar to access tools, orders, reports, and more.
          </p>
        </CardContent>
      </Card>
    </div>
  );
}
