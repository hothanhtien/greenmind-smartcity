"use client";

import React from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";

export interface AreaCard {
  id: string;
  name: string;
  total_households: number;
  total_waste: number;
  plastic_percentage: number;
  electricity_usage: number;
  risk_level: "LOW" | "MEDIUM" | "HIGH";
}

interface Props {
  areas: AreaCard[];
  onSelect: (areaId: string) => void;
}

export default function AreaList({ areas, onSelect }: Props) {
  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
      {areas.map((a) => (
        <Card key={a.id} className="rounded-2xl shadow-sm">
          <CardHeader>
            <div className="flex items-center justify-between">
              <CardTitle className="text-sm font-medium">{a.name}</CardTitle>
              <div className="text-xs font-semibold">
                {a.risk_level === "HIGH" ? (
                  <span className="text-red-600">🔴 HIGH</span>
                ) : a.risk_level === "MEDIUM" ? (
                  <span className="text-yellow-600">🟡 MEDIUM</span>
                ) : (
                  <span className="text-green-600">🟢 LOW</span>
                )}
              </div>
            </div>
          </CardHeader>
          <CardContent>
            <div className="space-y-2">
              <div className="flex items-center justify-between">
                <div className="text-sm text-muted-foreground">Households</div>
                <div className="font-medium">{a.total_households}</div>
              </div>

              <div className="flex items-center justify-between">
                <div className="text-sm text-muted-foreground">Total Waste</div>
                <div className="font-medium">
                  {a.total_waste.toLocaleString()} kg
                </div>
              </div>

              <div className="flex items-center justify-between">
                <div className="text-sm text-muted-foreground">Plastic %</div>
                <div className="font-medium">{a.plastic_percentage}%</div>
              </div>

              <div className="flex items-center justify-between">
                <div className="text-sm text-muted-foreground">Electricity</div>
                <div className="font-medium">
                  {a.electricity_usage.toLocaleString()} kWh
                </div>
              </div>

              <div className="pt-2 flex justify-end">
                <Button size="sm" onClick={() => onSelect(a.id)}>
                  View details
                </Button>
              </div>
            </div>
          </CardContent>
        </Card>
      ))}
    </div>
  );
}
