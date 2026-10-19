const NOMINATIM_BASE = "/api/nominatim/reverse";

// Client-side cache for geocoded addresses - use localStorage for persistence
const addressCache = new Map<string, string>();
const CACHE_MAX_SIZE = 500;

// Track pending requests to avoid duplicate calls
const pendingRequests = new Map<string, Promise<string | null>>();

// Track last request time to avoid rate limiting
let lastRequestTime = 0;
const MIN_REQUEST_INTERVAL = 1000; // 1 second between requests (Nominatim limit is 1 req/sec)

// Initialize cache from localStorage on load
function initCacheFromStorage(): void {
  if (typeof window === "undefined") return;
  try {
    const stored = localStorage.getItem("geocode_cache");
    if (stored) {
      const parsed = JSON.parse(stored);
      Object.entries(parsed).forEach(([key, value]) => {
        addressCache.set(key, value as string);
      });
    }
  } catch {
    // Ignore parse errors
  }
}

// Save cache to localStorage periodically
function saveCacheToStorage(): void {
  if (typeof window === "undefined") return;
  try {
    const obj = Object.fromEntries(addressCache);
    localStorage.setItem("geocode_cache", JSON.stringify(obj));
  } catch {
    // Ignore storage errors
  }
}

// Periodic save every 30 seconds
let saveInterval: ReturnType<typeof setInterval> | null = null;
function startPeriodicSave(): void {
  if (typeof window === "undefined") return;
  if (saveInterval) return;
  initCacheFromStorage();
  saveInterval = setInterval(saveCacheToStorage, 30000);
  // Also save on page unload
  window.addEventListener("beforeunload", saveCacheToStorage);
}

// Rate limiter: wait until enough time has passed since last request
async function waitForRateLimit(): Promise<void> {
  const now = Date.now();
  const elapsed = now - lastRequestTime;
  if (elapsed < MIN_REQUEST_INTERVAL) {
    await new Promise(resolve => setTimeout(resolve, MIN_REQUEST_INTERVAL - elapsed));
  }
  lastRequestTime = Date.now();
}

export async function reverseGeocode(lat: number, lng: number): Promise<string | null> {
  const cacheKey = `${lat.toFixed(6)}_${lng.toFixed(6)}`;

  // Start periodic save on first use
  startPeriodicSave();

  // Check cache first
  if (addressCache.has(cacheKey)) {
    return addressCache.get(cacheKey)!;
  }

  // Check if there's already a pending request for this coordinate
  if (pendingRequests.has(cacheKey)) {
    return pendingRequests.get(cacheKey)!;
  }

  // Create the request promise
  const requestPromise = (async () => {
    try {
      // Wait for rate limit before making request
      await waitForRateLimit();

      const url = `${NOMINATIM_BASE}?lat=${lat}&lng=${lng}`;

      // Try multiple times with delay if rate limited
      let lastError = null;
      for (let attempt = 0; attempt < 3; attempt++) {
        if (attempt > 0) {
          // Wait before retry (exponential backoff)
          await new Promise(resolve => setTimeout(resolve, 2000 * attempt));
        }

        const res = await fetch(url);
        const data = await res.json();

        // If API returns rate limit error, continue to retry
        if (res.status === 429 || data.error?.includes("429")) {
          lastError = "Rate limited";
          continue;
        }

        // If API returns other error, continue to retry or return null
        if (data.error) {
          lastError = data.error;
          continue;
        }

        const addr = data.address;

        // Build a readable address from address components
        if (addr) {
          const parts = [
            addr.road,
            addr.house_number,
            addr.quarter,
            addr.suburb,
            addr.neighbourhood,
            addr.ward,
            addr.district,
            addr.city,
          ].filter(Boolean);

          if (parts.length > 0) {
            const result = parts.join(", ");
            // Cache the result
            addressCache.set(cacheKey, result);
            saveCacheToStorage(); // Persist immediately on new entry
            return result;
          }
        }

        // Fallback to display_name if address components not available
        if (data.display_name) {
          const parts = data.display_name.split(",");
          const shortAddr = parts.slice(0, 3).join(",").trim();
          return shortAddr;
        }

        return null;
      }

      // All attempts failed
      console.warn(`[geocode] Failed after 3 attempts: ${lastError}`);
      return null;
    } catch (err) {
      console.warn(`[geocode] Error: ${err}`);
      return null;
    } finally {
      // Remove from pending requests
      pendingRequests.delete(cacheKey);
    }
  })();

  // Store the promise so duplicate calls can use the same request
  pendingRequests.set(cacheKey, requestPromise);

  return requestPromise;
}

// Clear cache (useful for testing or when needed)
export function clearGeocodeCache(): void {
  addressCache.clear();
}