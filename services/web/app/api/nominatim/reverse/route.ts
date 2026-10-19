import { NextRequest, NextResponse } from "next/server";

// Server-side cache
const serverCache = new Map<string, { data: any; timestamp: number }>();
const CACHE_TTL = 60 * 60 * 24 * 1000; // 1 day

// Track last request time to respect Nominatim rate limit
let lastRequestTime = 0;
const MIN_REQUEST_INTERVAL = 1100; // 1.1 seconds

async function waitForRateLimit(): Promise<void> {
  const now = Date.now();
  const elapsed = now - lastRequestTime;
  if (elapsed < MIN_REQUEST_INTERVAL) {
    await new Promise(resolve => setTimeout(resolve, MIN_REQUEST_INTERVAL - elapsed));
  }
  lastRequestTime = Date.now();
}

function getCacheKey(lat: string, lng: string): string {
  return `${parseFloat(lat).toFixed(5)}_${parseFloat(lng).toFixed(5)}`;
}

/**
 * GET /api/nominatim/reverse?lat=<lat>&lng=<lng>
 * Proxy server-side cho Nominatim reverse geocoding để tránh CORS.
 */
export async function GET(req: NextRequest) {
  const lat = req.nextUrl.searchParams.get("lat");
  const lng = req.nextUrl.searchParams.get("lng");

  if (!lat || !lng) {
    return NextResponse.json({ error: "Missing lat or lng param" }, { status: 400 });
  }

  const cacheKey = getCacheKey(lat, lng);

  // Check server cache first
  const cached = serverCache.get(cacheKey);
  if (cached && Date.now() - cached.timestamp < CACHE_TTL) {
    return NextResponse.json(cached.data);
  }

  // Wait for rate limit
  await waitForRateLimit();

  // Retry up to 3 times on 429 errors
  for (let attempt = 0; attempt < 3; attempt++) {
    if (attempt > 0) {
      // Wait before retry (exponential backoff)
      await new Promise(resolve => setTimeout(resolve, 3000 * attempt));
    }

    try {
      const url = `https://nominatim.openstreetmap.org/reverse?lat=${lat}&lon=${lng}&format=json&addressdetails=1`;

      const res = await fetch(url, {
        headers: {
          "User-Agent": "GreenMind-Dashboard/1.0 (contact@greenmind.vn)",
          "Accept-Language": "vi",
        },
      });

      if (res.status === 429) {
        // Rate limited, retry
        console.log(`[nominatim reverse] Rate limited, attempt ${attempt + 1}/3`);
        continue;
      }

      if (!res.ok) {
        return NextResponse.json({ error: `Nominatim error ${res.status}` }, { status: 502 });
      }

      const data = await res.json();

      // Cache the result
      serverCache.set(cacheKey, { data, timestamp: Date.now() });

      return NextResponse.json(data);
    } catch (err) {
      console.error(`[nominatim reverse] attempt ${attempt + 1} failed:`, err);
      if (attempt === 2) {
        return NextResponse.json({ error: "Proxy fetch failed" }, { status: 500 });
      }
    }
  }

  return NextResponse.json({ error: "Nominatim error 429" }, { status: 502 });
}