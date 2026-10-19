"use client"

import React, { useEffect, useMemo, useState } from "react";
import {
  Select,
  SelectTrigger,
  SelectContent,
  SelectItem,
  SelectValue,
} from "@/components/ui/select";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { LucideSearch } from "lucide-react";

export type ActivityType =
  | "WASTE_CLASSIFICATION"
  | "GREEN_MEAL"
  | "ELECTRICITY_USAGE"
  | "TRASH_DISPOSAL"
  | "NOTE";

export interface Area {
  id: string;
  name: string;
}

export interface Filters {
  area_id?: string;
  activity_type?: ActivityType | "__all" | undefined;
  from_date?: string | undefined;
  to_date?: string | undefined;
  keyword?: string | undefined;
  sort?: "newest" | "oldest";
}

interface Props {
  areas: Area[];
  onChange: (filters: Filters) => void;
}

export const FilterBar: React.FC<Props> = ({ areas, onChange }) => {
  const ALL = "__all";
  const [area, setArea] = useState<string>(ALL);
  const [activityType, setActivityType] = useState<string>(ALL);
  const [keyword, setKeyword] = useState<string>("");
  const [fromDate, setFromDate] = useState<string | undefined>(undefined);
  const [toDate, setToDate] = useState<string | undefined>(undefined);
  const [sort, setSort] = useState<"newest" | "oldest">("newest");

  useEffect(() => {
    const id = setTimeout(() => {
      onChange({
        area_id: area === ALL ? undefined : area,
        activity_type: activityType === ALL ? undefined : (activityType as ActivityType),
        from_date: fromDate,
        to_date: toDate,
        keyword: keyword || undefined,
        sort,
      });
    }, 400);

    return () => clearTimeout(id);
  }, [area, activityType, fromDate, toDate, keyword, sort, onChange]);

  const activityOptions = useMemo(
    () => [
      { value: ALL, label: "All Activities" },
      { value: "WASTE_CLASSIFICATION", label: "Waste Classification" },
      { value: "GREEN_MEAL", label: "Green Meal" },
      { value: "ELECTRICITY_USAGE", label: "Electricity Usage" },
      { value: "TRASH_DISPOSAL", label: "Trash Disposal" },
      { value: "NOTE", label: "Note" },
    ],
    []
  );

  return (
    <div className="w-full bg-transparent">
      <div className="flex flex-col md:flex-row gap-3 items-start md:items-center">
        <div className="flex gap-2 w-full md:w-auto">
          <Select value={area} onValueChange={(v) => setArea(v)}>
            <SelectTrigger className="w-[160px]">
              <SelectValue placeholder="Select area" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value={ALL}>All Areas</SelectItem>
              {areas.map((a) => (
                <SelectItem key={a.id} value={a.id}>
                  {a.name}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>

          <Select value={activityType} onValueChange={(v) => setActivityType(v)}>
            <SelectTrigger className="w-[200px]">
              <SelectValue placeholder="Activity type" />
            </SelectTrigger>
            <SelectContent>
              {activityOptions.map((o) => (
                <SelectItem key={o.value} value={o.value}>
                  {o.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>

        <div className="flex gap-2 w-full md:w-auto items-center">
          <div className="relative flex items-center w-full md:w-[260px]">
            <Input
              placeholder="Search metadata..."
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
            />
            <div className="absolute right-2 text-muted-foreground">
              <LucideSearch size={16} />
            </div>
          </div>

          <div className="flex items-center gap-2">
            <Select value={sort} onValueChange={(v) => setSort(v as any)}>
              <SelectTrigger className="w-[140px]">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="newest">Newest</SelectItem>
                <SelectItem value="oldest">Oldest</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </div>

        <div className="ml-auto">
          <Button variant="ghost" size="sm" onClick={() => {
            setArea(ALL);
            setActivityType(ALL);
            setKeyword("");
            setFromDate(undefined);
            setToDate(undefined);
            setSort("newest");
          }}>
            Reset
          </Button>
        </div>
      </div>
    </div>
  );
};

export default FilterBar;

