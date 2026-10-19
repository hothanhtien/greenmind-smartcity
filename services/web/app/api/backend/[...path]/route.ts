import { NextRequest, NextResponse } from "next/server";

const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_URL || "https://vodang-api.gauas.com";

type RouteContext = {
  params: Promise<{ path: string[] }>;
};

function buildTargetUrl(path: string[], search: string) {
  const base = API_BASE_URL.replace(/\/$/, "");
  const target = new URL(`${base}/${path.join("/")}`);
  target.search = search;
  return target;
}

function buildHeaders(request: NextRequest) {
  const headers = new Headers();
  const authorization = request.headers.get("authorization");
  const contentType = request.headers.get("content-type");
  const accept = request.headers.get("accept");

  if (authorization) headers.set("authorization", authorization);
  if (contentType) headers.set("content-type", contentType);
  if (accept) headers.set("accept", accept);

  return headers;
}

async function proxy(request: NextRequest, context: RouteContext) {
  try {
    const { path } = await context.params;
    const target = buildTargetUrl(path, request.nextUrl.search);
    const method = request.method;
    const hasBody = method !== "GET" && method !== "HEAD";

    const upstream = await fetch(target, {
      method,
      headers: buildHeaders(request),
      body: hasBody ? await request.arrayBuffer() : undefined,
      cache: "no-store",
    });

    const responseHeaders = new Headers();
    const contentType = upstream.headers.get("content-type");
    if (contentType) responseHeaders.set("content-type", contentType);

    const responseBody =
      upstream.status === 204 || upstream.status === 304
        ? null
        : await upstream.arrayBuffer();

    return new NextResponse(responseBody, {
      status: upstream.status,
      statusText: upstream.statusText,
      headers: responseHeaders,
    });
  } catch (error) {
    console.error("[api/backend] Proxy request failed:", error);
    return NextResponse.json(
      { message: "Không thể kết nối tới máy chủ API." },
      { status: 502 }
    );
  }
}

export const GET = proxy;
export const POST = proxy;
export const PUT = proxy;
export const PATCH = proxy;
export const DELETE = proxy;
