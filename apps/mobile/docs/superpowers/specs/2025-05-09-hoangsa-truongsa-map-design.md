# Hoàng Sa & Trường Sa Boundary trên Leaflet Maps

## 1. Mục tiêu

Hiển thị ranh giới lãnh thổ Hoàng Sa và Trường Sa (Việt Nam) trên tất cả 3 loại map:
- GarbageHeatMapView (heatmap cho collector)
- RouteMapView (tuyến đường thu gom)
- CampaignMapView (bản đồ campaign)

## 2. Data Source

### Primary: Embedded GeoJSON (CDN fallback)
- **Local**: `composeApp/src/commonMain/resources/hoangsa_truongsa.json`
- **Remote CDN**: GitHub raw URL (sẽ chọn sau khi tìm stable source)
- **Fallback logic**: Load CDN trước → nếu fail dùng local embedded data

### Structure
```json
{
  "type": "FeatureCollection",
  "features": [
    {
      "type": "Feature",
      "properties": {
        "name": "Hoàng Sa",
        "name_en": "Paracel Islands",
        "country": "Vietnam",
        "type": "archipelago"
      },
      "geometry": { "type": "Polygon", "coordinates": [...] }
    },
    {
      "type": "Feature",
      "properties": {
        "name": "Trường Sa",
        "name_en": "Spratly Islands",
        "country": "Vietnam",
        "type": "archipelago"
      },
      "geometry": { "type": "MultiPolygon", "coordinates": [...] }
    }
  ]
}
```

## 3. Style

| Property | Value |
|----------|-------|
| Fill color | `#E53935` (đỏ) |
| Fill opacity | 0.15 |
| Stroke color | `#C62828` |
| Stroke width | 2px |
| Stroke opacity | 0.8 |

## 4. Popup khi tap

```
┌─────────────────────────────┐
│ 🏝️ Hoàng Sa                  │
│ Paracel Islands              │
│ 🏳️ Quốc gia: Việt Nam        │
└─────────────────────────────┘
```

Tương tự cho Trường Sa.

## 5. Implementation Plan

### Bước 1: Tạo data file
- Tạo `hoangsa_truongsa.json` trong `commonMain/resources/`
- Nếu không tìm được CDN ổn định → embed trực tiếp vào code

### Bước 2: Cập nhật HTML builders
- Thêm hàm `buildVietnamBoundaryLayer()` trả về JS code để load GeoJSON
- Sửa `buildLeafletHtml()`, `buildRoutingHtml()`, `buildCampaignMapHtml()` để include boundary layer

### Bước 3: Cập nhật actual implementations
- Android: Load local JSON → truyền vào WebView
- iOS: Tương tự

### Bước 4: Cập nhật CatalogueScreen
- Thêm feature tracking nếu cần

## 6. Technical Notes

### CDN Fallback Strategy
```javascript
// Ưu tiên CDN, fallback về embedded data
async function loadVietnamBoundary() {
  try {
    const resp = await fetch('CDN_URL');
    if (resp.ok) {
      const geojson = await resp.json();
      L.geoJSON(geojson, { style, onEachFeature }).addTo(map);
      return;
    }
  } catch (e) { /* fall through */ }

  // Dùng embedded data
  L.geoJSON(embeddedVietnamGeoJSON, { style, onEachFeature }).addTo(map);
}
```

### Map Tiles
- Tiếp tục dùng OSM tiles (`https://{s}.tile.openstreetmap.org/`)
- Không cần thay đổi tile provider

## 7. Files to Modify

| File | Change |
|------|--------|
| `hoangsa_truongsa.json` | New - GeoJSON data |
| `GarbageMapView.kt` | Thêm `buildVietnamBoundaryJs()` + update HTML builders |
| `GarbageMapView.android.kt` | Load local JSON → inject vào WebView |
| `GarbageMapView.ios.kt` | Tương tự Android |

## 8. Coordinate Data

### Hoàng Sa (Paracel Islands)
Centroid: ~16.5°N, 111.5°E
Gồm ~40 đảo, đ largest là Pagoda Island (Đảo Pagoda), Money Island (Đảo Tiền), etc.

### Trường Sa (Spratly Islands)
Centroid: ~9°N, 113°E
Gồm ~100+ đảo/bãi đá/bãi ngầm, đ largest là Grande Island (Đảo Lớn / Trường Sa Lớn).

*Note: Tọa độ chính xác sẽ được xác minh từ nguồn UNDP hoặc VN official GIS.*