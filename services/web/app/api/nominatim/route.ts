import { NextRequest, NextResponse } from "next/server";

/**
 * GET /api/nominatim?q=<ward name>
 * Proxy server-side cho Nominatim để tránh CORS khi gọi từ browser.
 * Next.js server → Nominatim (không bị CORS block).
 */
export async function GET(req: NextRequest) {
  const q = req.nextUrl.searchParams.get("q");
  if (!q) {
    return NextResponse.json({ error: "Missing q param" }, { status: 400 });
  }

  try {
    const url = `https://nominatim.openstreetmap.org/search?q=${encodeURIComponent(q)}&format=json&polygon_geojson=1&limit=1`;

    const res = await fetch(url, {
      headers: {
        // Nominatim yêu cầu User-Agent hợp lệ
        "User-Agent": "GreenMind-Dashboard/1.0 (contact@greenmind.vn)",
        "Accept-Language": "vi",
      },
      // Cache ở Next.js server 7 ngày — boundary hiếm khi thay đổi
      next: { revalidate: 60 * 60 * 24 * 7 },
    });

    if (!res.ok) {
      return NextResponse.json({ error: `Nominatim error ${res.status}` }, { status: 502 });
    }

    const data = await res.json();

    return NextResponse.json(data, {
      headers: {
        // Cache thêm ở browser 1 ngày
        "Cache-Control": "public, max-age=86400, stale-while-revalidate=3600",
      },
    });
  } catch (err) {
    console.error("[nominatim proxy] fetch failed:", err);
    return NextResponse.json({ error: "Proxy fetch failed" }, { status: 500 });
  }
}
