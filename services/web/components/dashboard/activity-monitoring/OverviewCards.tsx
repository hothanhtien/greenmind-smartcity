"use client"

import React from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

interface Props {
  totalWaste: number;
  totalHouseholds: number;
  avgPlastic: number;
  totalElectricity: number;
}

export const OverviewCards: React.FC<Props> = ({ totalWaste, totalHouseholds, avgPlastic, totalElectricity }) => {
  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      <Card className="rounded-2xl shadow-sm">
        <CardHeader>
          <CardTitle className="text-sm">Total Waste</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="text-2xl font-semibold">{totalWaste.toLocaleString()} kg</div>
          <div className="text-sm text-muted-foreground">Aggregated across all areas</div>
        </CardContent>
      </Card>

      <Card className="rounded-2xl shadow-sm">
        <CardHeader>
          <CardTitle className="text-sm">Total Households</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="text-2xl font-semibold">{totalHouseholds}</div>
          <div className="text-sm text-muted-foreground">Registered households</div>
        </CardContent>
      </Card>

      <Card className="rounded-2xl shadow-sm">
        <CardHeader>
          <CardTitle className="text-sm">Average Plastic %</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="text-2xl font-semibold">{avgPlastic}%</div>
          <div className="text-sm text-muted-foreground">Average plastic share</div>
        </CardContent>
      </Card>

      <Card className="rounded-2xl shadow-sm">
        <CardHeader>
          <CardTitle className="text-sm">Electricity Usage</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="text-2xl font-semibold">{totalElectricity.toLocaleString()} kWh</div>
          <div className="text-sm text-muted-foreground">Monthly total (mock)</div>
        </CardContent>
      </Card>
    </div>
  );
};

export default OverviewCards;

