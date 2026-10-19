"use client"

import React, { useEffect, useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { activityService } from "@/services/activity.service";
import { Skeleton } from "@/components/ui/skeleton";
import {
  ResponsiveContainer,
  LineChart,
  Line,
  XAxis,
  YAxis,
  Tooltip,
  CartesianGrid,
  BarChart,
  Bar,
} from "recharts";
import { Button } from "@/components/ui/button";

interface Props {
  areaId: string | null;
  onClose?: () => void;
}

export default function AreaDetail({ areaId, onClose }: Props) {
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState<any | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!areaId) {
      setData(null);
      return;
    }

    let mounted = true;
    setLoading(true);
    setError(null);

    activityService
      .getAreaDetail(areaId)
      .then((res) => {
        if (!mounted) return;
        setData(res);
      })
      .catch((e) => {
        if (!mounted) return;
        setError(String(e));
      })
      .finally(() => {
        if (!mounted) return;
        setLoading(false);
      });

    return () => {
      mounted = false;
    };
  }, [areaId]);

  if (!areaId) {
    return (
      <Card className="rounded-2xl shadow-sm">
        <CardHeader>
          <CardTitle className="text-sm">Area Detail</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="text-sm text-muted-foreground">Select an area to view detailed analytics.</div>
        </CardContent>
      </Card>
    );
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h2 className="text-lg font-semibold">Area Detail</h2>
        {onClose && (
          <Button variant="ghost" size="sm" onClick={onClose}>
            Close
          </Button>
        )}
      </div>

      {loading && (
        <div className="grid grid-cols-1 gap-4">
          <Skeleton className="h-24 rounded-2xl" />
          <Skeleton className="h-48 rounded-2xl" />
        </div>
      )}

      {error && <div className="text-red-600">{error}</div>}

      {data && (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
          <Card className="rounded-2xl shadow-sm">
            <CardHeader>
              <CardTitle className="text-sm">Metrics — {data.area.name}</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <div className="text-sm text-muted-foreground">Total Waste</div>
                  <div className="font-medium">{data.area.total_waste.toLocaleString()} kg</div>
                </div>
                <div>
                  <div className="text-sm text-muted-foreground">Households</div>
                  <div className="font-medium">{data.area.total_households}</div>
                </div>
                <div>
                  <div className="text-sm text-muted-foreground">Plastic %</div>
                  <div className="font-medium">{data.area.plastic_percentage}%</div>
                </div>
                <div>
                  <div className="text-sm text-muted-foreground">Electricity</div>
                  <div className="font-medium">{data.area.electricity_usage.toLocaleString()} kWh</div>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card className="rounded-2xl shadow-sm">
            <CardHeader>
              <CardTitle className="text-sm">Waste Trend (14 days)</CardTitle>
            </CardHeader>
            <CardContent>
              <div style={{ width: "100%", height: 220 }}>
                <ResponsiveContainer>
                  <LineChart data={data.wasteTrend}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="date" />
                    <YAxis />
                    <Tooltip />
                    <Line type="monotone" dataKey="value" stroke="#10B981" strokeWidth={2} dot={false} />
                  </LineChart>
                </ResponsiveContainer>
              </div>
            </CardContent>
          </Card>

          <Card className="rounded-2xl shadow-sm lg:col-span-2">
            <CardHeader>
              <CardTitle className="text-sm">Plastic (monthly) & Electricity Trend</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
                <div style={{ width: "100%", height: 200 }}>
                  <ResponsiveContainer>
                    <BarChart data={data.plasticTrend}>
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis dataKey="month" />
                      <YAxis />
                      <Tooltip />
                      <Bar dataKey="value" fill="#F59E0B" />
                    </BarChart>
                  </ResponsiveContainer>
                </div>

                <div style={{ width: "100%", height: 200 }}>
                  <ResponsiveContainer>
                    <LineChart data={data.electricityTrend}>
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis dataKey="date" />
                      <YAxis />
                      <Tooltip />
                      <Line type="monotone" dataKey="value" stroke="#6366F1" strokeWidth={2} dot={false} />
                    </LineChart>
                  </ResponsiveContainer>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      )}
    </div>
  );
}

