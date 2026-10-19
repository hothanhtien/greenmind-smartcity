"use client"

import React, { useEffect, useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { activityService, WasteReport } from "@/services/activity.service";
import { Select, SelectTrigger, SelectContent, SelectItem, SelectValue } from "@/components/ui/select";
import { Input } from "@/components/ui/input";

interface Props {
  areaId?: string | null;
}

export const WasteReports: React.FC<Props> = ({ areaId }) => {
  const [reports, setReports] = useState<WasteReport[]>([]);
  const [loading, setLoading] = useState(false);
  const [filter, setFilter] = useState<string>("all");
  const [assigning, setAssigning] = useState<{ id: string; name: string } | null>(null);
  const [collectorName, setCollectorName] = useState("");

  const load = async () => {
    setLoading(true);
    try {
      const res = await activityService.getWasteReports(filter === "all" ? undefined : { status: filter });
      const list = Array.isArray(res) ? res : [];
      setReports(areaId ? list.filter((r) => r.area.id === areaId) : list);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, [filter, areaId]);

  const handleAssign = async (reportId: string) => {
    if (!collectorName) return;
    try {
      await activityService.assignCollector(reportId, { id: `c-${Date.now()}`, name: collectorName });
      setCollectorName("");
      setAssigning(null);
      load();
    } catch (e) {
      console.error(e);
    }
  };

  const handleUpdateStatus = async (reportId: string, status: "pending" | "assigned" | "collected") => {
    try {
      await activityService.updateReportStatus(reportId, status);
      load();
    } catch (e) {
      console.error(e);
    }
  };

  return (
    <Card className="rounded-2xl shadow-sm">
      <CardHeader>
        <div className="flex items-center justify-between">
          <CardTitle>Waste Reports</CardTitle>
          <div className="flex items-center gap-2">
            <Select value={filter} onValueChange={(v) => setFilter(v)}>
              <SelectTrigger className="w-[160px]">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">All</SelectItem>
                <SelectItem value="pending">Pending</SelectItem>
                <SelectItem value="assigned">Assigned</SelectItem>
                <SelectItem value="collected">Collected</SelectItem>
              </SelectContent>
            </Select>
            <Button variant="ghost" size="sm" onClick={load}>Refresh</Button>
          </div>
        </div>
      </CardHeader>
      <CardContent>
        {loading && <div className="text-sm text-muted-foreground">Loading reports...</div>}

        {!loading && reports.length === 0 && <div className="text-sm text-muted-foreground">No reports found.</div>}

        <div className="space-y-3">
          {reports.map((r) => (
            <div key={r.id} className="p-3 border rounded-md">
              <div className="flex items-center justify-between">
                <div>
                  <div className="font-medium">{r.description}</div>
                  <div className="text-xs text-muted-foreground">{r.area.name} • {new Date(r.reported_at).toLocaleString()}</div>
                </div>
                <div className="flex items-center gap-2">
                  <div className="text-sm">
                    {r.status === "pending" ? <span className="text-red-600">Pending</span> : r.status === "assigned" ? <span className="text-yellow-600">Assigned</span> : <span className="text-green-600">Collected</span>}
                  </div>
                  {r.assigned_collector && <div className="text-xs text-muted-foreground">{r.assigned_collector.name}</div>}
                </div>
              </div>

              <div className="mt-2 flex items-center gap-2">
                {r.status !== "collected" && (
                  <>
                    <Button size="sm" onClick={() => handleUpdateStatus(r.id, r.status === "pending" ? "assigned" : "collected")}>{r.status === "pending" ? "Mark Assigned" : "Mark Collected"}</Button>
                    <Button size="sm" variant="outline" onClick={() => setAssigning({ id: r.id, name: "" })}>Assign Collector</Button>
                  </>
                )}
              </div>

              {assigning?.id === r.id && (
                <div className="mt-2 flex gap-2">
                  <Input placeholder="Collector name" value={collectorName} onChange={(e) => setCollectorName(e.target.value)} />
                  <Button size="sm" onClick={() => handleAssign(r.id)}>Confirm</Button>
                  <Button size="sm" variant="ghost" onClick={() => setAssigning(null)}>Cancel</Button>
                </div>
              )}
            </div>
          ))}
        </div>
      </CardContent>
    </Card>
  );
};

export default WasteReports;

