import { WasteReport, CampaignRegion } from "@/types/waste-report";

/**
 * Calculates the distance between two coordinate points in meters using the Haversine formula.
 * @param lat1 Latitude of point 1
 * @param lon1 Longitude of point 1
 * @param lat2 Latitude of point 2
 * @param lon2 Longitude of point 2
 * @returns Distance in meters
 */
export function calculateHaversineDistance(lat1: number, lon1: number, lat2: number, lon2: number): number {
  const R = 6371e3; // Earth radius in meters
  const dLat = (lat2 - lat1) * Math.PI / 180;
  const dLon = (lon2 - lon1) * Math.PI / 180;
  
  const a = 
    Math.sin(dLat/2) * Math.sin(dLat/2) +
    Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) * 
    Math.sin(dLon/2) * Math.sin(dLon/2);
  
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
  
  return R * c;
}

/**
 * Clusters reports into regions based on a given radius in meters.
 * Uses a greedy approach: takes an unassigned point, groups all unassigned
 * points within the radius, and creates a region.
 * @param reports The list of WasteReport objects to cluster
 * @param radiusInMeters The clustering radius in meters (default 500)
 * @returns Array of CampaignRegion objects
 */
export function clusterReports(reports: WasteReport[], radiusInMeters: number = 500): CampaignRegion[] {
  const unassigned = [...reports];
  const regions: CampaignRegion[] = [];
  let regionCounter = 1;

  while (unassigned.length > 0) {
    // Pick the first unassigned report as the center
    const centerReport = unassigned.shift()!;
    const currentRegionReports = [centerReport];

    // Find all other unassigned reports within the radius
    for (let i = unassigned.length - 1; i >= 0; i--) {
      const otherReport = unassigned[i];
      const distance = calculateHaversineDistance(
        centerReport.lat,
        centerReport.lng,
        otherReport.lat,
        otherReport.lng
      );

      if (distance <= radiusInMeters) {
        currentRegionReports.push(otherReport);
        // Remove from unassigned
        unassigned.splice(i, 1);
      }
    }

    // Create the region
    regions.push({
      id: `region-${regionCounter}`,
      name: `Region ${String.fromCharCode(64 + regionCounter)}`, // Region A, Region B, ...
      center: {
        lat: centerReport.lat,
        lng: centerReport.lng
      },
      reports: currentRegionReports
    });

    regionCounter++;
  }

  // Sort regions by number of reports descending
  regions.sort((a, b) => b.reports.length - a.reports.length);

  return regions;
}
