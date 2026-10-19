// Vietnam Islands Data (Hoàng Sa & Trường Sa)
// Data sourced from Natural Earth and public GIS datasets
// Each island includes coordinates, name (Vietnamese), and English name

import type * as GeoJSON from "geojson";

export interface IslandFeature {
  type: "Feature";
  properties: {
    name: string;
    nameEn: string;
    type: "island" | "reef" | "atoll" | "shoal";
    coordinates: [number, number];
  };
  geometry: {
    type: "Point";
    coordinates: [number, number];
  };
}

export interface IslandPolygonFeature {
  type: "Feature";
  properties: {
    name: string;
    nameEn: string;
    type: "island" | "reef" | "atoll" | "shoal";
  };
  geometry: {
    type: "Polygon" | "MultiPolygon";
    coordinates: number[][][] | number[][][][];
  };
}

// Hoàng Sa (Paracel Islands) - 16°15'N - 17°15'N, 111°E - 113°E
export const HOANG_SA_ISLANDS: IslandFeature[] = [
  // Main islands
  {
    type: "Feature",
    properties: { name: "Tri Tôn", nameEn: "Triton Island", type: "island", coordinates: [111.833, 16.067] },
    geometry: { type: "Point", coordinates: [111.833, 16.067] },
  },
  {
    type: "Feature",
    properties: { name: "Phú Lâm", nameEn: "Woody Island", type: "island", coordinates: [112.333, 16.533] },
    geometry: { type: "Point", coordinates: [112.333, 16.533] },
  },
  {
    type: "Feature",
    properties: { name: "Quang Hòa", nameEn: "Quang Hoa", type: "island", coordinates: [112.717, 16.267] },
    geometry: { type: "Point", coordinates: [112.717, 16.267] },
  },
  {
    type: "Feature",
    properties: { name: "Cao Vân", nameEn: "Gilles Montagnac Island", type: "island", coordinates: [111.633, 16.450] },
    geometry: { type: "Point", coordinates: [111.633, 16.450] },
  },
  {
    type: "Feature",
    properties: { name: "Sinh Tồn", nameEn: "Sinh Ton Island", type: "island", coordinates: [112.033, 16.350] },
    geometry: { type: "Point", coordinates: [112.033, 16.350] },
  },
  {
    type: "Feature",
    properties: { name: "Duyên Hòa", nameEn: "Duyen Hoa", type: "island", coordinates: [111.883, 16.100] },
    geometry: { type: "Point", coordinates: [111.883, 16.100] },
  },
  {
    type: "Feature",
    properties: { name: "Lưỡi Liềm", nameEn: "Lumiere Montagnac Island", type: "island", coordinates: [111.717, 16.117] },
    geometry: { type: "Point", coordinates: [111.717, 16.117] },
  },
  {
    type: "Feature",
    properties: { name: "Bình Nguyên", nameEn: "Binh Nguyen", type: "island", coordinates: [111.567, 16.300] },
    geometry: { type: "Point", coordinates: [111.567, 16.300] },
  },
  {
    type: "Feature",
    properties: { name: "Bạc Vĩnh", nameEn: "Bac Vinh", type: "island", coordinates: [111.450, 16.667] },
    geometry: { type: "Point", coordinates: [111.450, 16.667] },
  },
  {
    type: "Feature",
    properties: { name: "Vĩnh Thực", nameEn: "Vinh Thuc", type: "island", coordinates: [111.533, 16.533] },
    geometry: { type: "Point", coordinates: [111.533, 16.533] },
  },
  {
    type: "Feature",
    properties: { name: "Song Tử", nameEn: "Song Tu", type: "island", coordinates: [111.617, 16.017] },
    geometry: { type: "Point", coordinates: [111.617, 16.017] },
  },
  {
    type: "Feature",
    properties: { name: "Tử Công", nameEn: "Tu Cong", type: "island", coordinates: [111.767, 16.233] },
    geometry: { type: "Point", coordinates: [111.767, 16.233] },
  },
  {
    type: "Feature",
    properties: { name: "Hữu Nhật", nameEn: "Huu Nhat", type: "island", coordinates: [112.133, 16.300] },
    geometry: { type: "Point", coordinates: [112.133, 16.300] },
  },
  {
    type: "Feature",
    properties: { name: "Nam Yết", nameEn: "Nam Yet", type: "island", coordinates: [111.550, 16.417] },
    geometry: { type: "Point", coordinates: [111.550, 16.417] },
  },
  {
    type: "Feature",
    properties: { name: "Hải Sơn", nameEn: "Hai Son", type: "island", coordinates: [111.350, 16.450] },
    geometry: { type: "Point", coordinates: [111.350, 16.450] },
  },
  {
    type: "Feature",
    properties: { name: "Sơn Hà", nameEn: "Son Ha", type: "island", coordinates: [111.400, 16.567] },
    geometry: { type: "Point", coordinates: [111.400, 16.567] },
  },
  {
    type: "Feature",
    properties: { name: "Đông Mae", nameEn: "East Sand", type: "island", coordinates: [111.267, 16.483] },
    geometry: { type: "Point", coordinates: [111.267, 16.483] },
  },
  {
    type: "Feature",
    properties: { name: "Cỏ Mây", nameEn: "Co May", type: "island", coordinates: [111.383, 16.383] },
    geometry: { type: "Point", coordinates: [111.383, 16.383] },
  },
  {
    type: "Feature",
    properties: { name: "Bãi Bến", nameEn: "Bai Ben", type: "island", coordinates: [112.000, 16.400] },
    geometry: { type: "Point", coordinates: [112.000, 16.400] },
  },
  {
    type: "Feature",
    properties: { name: "Bãi Cát Vàng", nameEn: "Bai Cat Vang", type: "island", coordinates: [112.267, 16.500] },
    geometry: { type: "Point", coordinates: [112.267, 16.500] },
  },
  {
    type: "Feature",
    properties: { name: "Bãi Từ", nameEn: "Bai Tu", type: "island", coordinates: [112.117, 16.417] },
    geometry: { type: "Point", coordinates: [112.117, 16.417] },
  },
  {
    type: "Feature",
    properties: { name: "Hòn Đen", nameEn: "Hon Den", type: "island", coordinates: [111.283, 16.533] },
    geometry: { type: "Point", coordinates: [111.283, 16.533] },
  },
  {
    type: "Feature",
    properties: { name: "Hòn Trứ", nameEn: "Hon Tru", type: "island", coordinates: [111.450, 16.217] },
    geometry: { type: "Point", coordinates: [111.450, 16.217] },
  },
  {
    type: "Feature",
    properties: { name: "Hòn Mỏ", nameEn: "Hon Mo", type: "island", coordinates: [112.450, 16.450] },
    geometry: { type: "Point", coordinates: [112.450, 16.450] },
  },
  {
    type: "Feature",
    properties: { name: "Hòn Nghê", nameEn: "Hon Nghe", type: "island", coordinates: [112.183, 16.533] },
    geometry: { type: "Point", coordinates: [112.183, 16.533] },
  },
  {
    type: "Feature",
    properties: { name: "Hòn Thìa", nameEn: "Hon Thia", type: "island", coordinates: [112.283, 16.350] },
    geometry: { type: "Point", coordinates: [112.283, 16.350] },
  },
  {
    type: "Feature",
    properties: { name: "Hòn Bắc", nameEn: "Hon Bac", type: "island", coordinates: [111.483, 16.350] },
    geometry: { type: "Point", coordinates: [111.483, 16.350] },
  },
  {
    type: "Feature",
    properties: { name: "Hòn Đông", nameEn: "Hon Dong", type: "island", coordinates: [111.233, 16.450] },
    geometry: { type: "Point", coordinates: [111.233, 16.450] },
  },
  {
    type: "Feature",
    properties: { name: "Hòn Tây", nameEn: "Hon Tay", type: "island", coordinates: [111.550, 16.583] },
    geometry: { type: "Point", coordinates: [111.550, 16.583] },
  },
  {
    type: "Feature",
    properties: { name: "Hòn Bồ Đề", nameEn: "Hon Bo De", type: "island", coordinates: [111.200, 16.517] },
    geometry: { type: "Point", coordinates: [111.200, 16.517] },
  },
  // Reefs
  {
    type: "Feature",
    properties: { name: "Đá Lồi", nameEn: "Da Loi", type: "reef", coordinates: [111.650, 16.267] },
    geometry: { type: "Point", coordinates: [111.650, 16.267] },
  },
  {
    type: "Feature",
    properties: { name: "Đá Bắc", nameEn: "Da Bac", type: "reef", coordinates: [112.050, 16.400] },
    geometry: { type: "Point", coordinates: [112.050, 16.400] },
  },
  {
    type: "Feature",
    properties: { name: "Đá Nam", nameEn: "Da Nam", type: "reef", coordinates: [112.150, 16.350] },
    geometry: { type: "Point", coordinates: [112.150, 16.350] },
  },
  {
    type: "Feature",
    properties: { name: "Đá Tây", nameEn: "Da Tay", type: "reef", coordinates: [112.250, 16.450] },
    geometry: { type: "Point", coordinates: [112.250, 16.450] },
  },
  {
    type: "Feature",
    properties: { name: "Đá Đông", nameEn: "Da Dong", type: "reef", coordinates: [111.333, 16.350] },
    geometry: { type: "Point", coordinates: [111.333, 16.350] },
  },
];

// Trường Sa (Spratly Islands) - 6°N - 11°N, 111°E - 117°E
export const TRUONG_SA_ISLANDS: IslandFeature[] = [
  // Major islands
  {
    type: "Feature",
    properties: { name: "Trường Sa", nameEn: "Spratly Island", type: "island", coordinates: [111.917, 10.350] },
    geometry: { type: "Point", coordinates: [111.917, 10.350] },
  },
  {
    type: "Feature",
    properties: { name: "Song Tử Tây", nameEn: "Southwest Cay", type: "island", coordinates: [111.700, 8.950] },
    geometry: { type: "Point", coordinates: [111.700, 8.950] },
  },
  {
    type: "Feature",
    properties: { name: "Song Tử Đông", nameEn: "Northeast Cay", type: "island", coordinates: [111.750, 9.033] },
    geometry: { type: "Point", coordinates: [111.750, 9.033] },
  },
  {
    type: "Feature",
    properties: { name: "Truờng Sa Đông", nameEn: "Truong Sa Dong", type: "island", coordinates: [112.917, 10.350] },
    geometry: { type: "Point", coordinates: [112.917, 10.350] },
  },
  {
    type: "Feature",
    properties: { name: "An Bang", nameEn: "An Bang", type: "island", coordinates: [112.283, 10.417] },
    geometry: { type: "Point", coordinates: [112.283, 10.417] },
  },
  {
    type: "Feature",
    properties: { name: "Nam Át", nameEn: "Nam At", type: "island", coordinates: [112.533, 10.383] },
    geometry: { type: "Point", coordinates: [112.533, 10.383] },
  },
  {
    type: "Feature",
    properties: { name: "Sơn À", nameEn: "Son A", type: "island", coordinates: [113.017, 10.583] },
    geometry: { type: "Point", coordinates: [113.017, 10.583] },
  },
  {
    type: "Feature",
    properties: { name: "Sinh Tồn Đông", nameEn: "Sinh Ton Dong", type: "island", coordinates: [113.400, 10.733] },
    geometry: { type: "Point", coordinates: [113.400, 10.733] },
  },
  {
    type: "Feature",
    properties: { name: "Sinh Tồn Tây", nameEn: "Sinh Ton Tay", type: "island", coordinates: [113.350, 10.717] },
    geometry: { type: "Point", coordinates: [113.350, 10.717] },
  },
  {
    type: "Feature",
    properties: { name: "Tân Hải", nameEn: "Tan Hai", type: "island", coordinates: [112.950, 10.800] },
    geometry: { type: "Point", coordinates: [112.950, 10.800] },
  },
  {
    type: "Feature",
    properties: { name: "Bình Minh", nameEn: "Binh Minh", type: "island", coordinates: [113.017, 10.733] },
    geometry: { type: "Point", coordinates: [113.017, 10.733] },
  },
  {
    type: "Feature",
    properties: { name: "Nam Thị", nameEn: "Nam Thi", type: "island", coordinates: [113.117, 10.783] },
    geometry: { type: "Point", coordinates: [113.117, 10.783] },
  },
  {
    type: "Feature",
    properties: { name: "Vĩnh Viễn", nameEn: "Vinh Vien", type: "island", coordinates: [113.167, 10.817] },
    geometry: { type: "Point", coordinates: [113.167, 10.817] },
  },
  {
    type: "Feature",
    properties: { name: "Thiên Hạ", nameEn: "Tien Ha", type: "island", coordinates: [113.217, 10.850] },
    geometry: { type: "Point", coordinates: [113.217, 10.850] },
  },
  {
    type: "Feature",
    properties: { name: "Tiên Nữ", nameEn: "Tien Nu", type: "island", coordinates: [113.267, 10.883] },
    geometry: { type: "Point", coordinates: [113.267, 10.883] },
  },
  {
    type: "Feature",
    properties: { name: "Bãi Tụ", nameEn: "Bai Tu", type: "island", coordinates: [113.033, 10.667] },
    geometry: { type: "Point", coordinates: [113.033, 10.667] },
  },
  {
    type: "Feature",
    properties: { name: "Trà Vờn", nameEn: "Tra Von", type: "island", coordinates: [112.867, 10.517] },
    geometry: { type: "Point", coordinates: [112.867, 10.517] },
  },
  {
    type: "Feature",
    properties: { name: "Nam Cam", nameEn: "Nam Cam", type: "island", coordinates: [112.750, 10.467] },
    geometry: { type: "Point", coordinates: [112.750, 10.467] },
  },
  {
    type: "Feature",
    properties: { name: "Phú Sĩ", nameEn: "Phu Si", type: "island", coordinates: [112.617, 10.400] },
    geometry: { type: "Point", coordinates: [112.617, 10.400] },
  },
  {
    type: "Feature",
    properties: { name: "Hướng Hải", nameEn: "Huong Hai", type: "island", coordinates: [113.117, 10.533] },
    geometry: { type: "Point", coordinates: [113.117, 10.533] },
  },
  {
    type: "Feature",
    properties: { name: "Đô Đốc", nameEn: "Do Doc", type: "island", coordinates: [112.183, 9.967] },
    geometry: { type: "Point", coordinates: [112.183, 9.967] },
  },
  {
    type: "Feature",
    properties: { name: "Long Thọ", nameEn: "Long Tho", type: "island", coordinates: [112.083, 9.933] },
    geometry: { type: "Point", coordinates: [112.083, 9.933] },
  },
  {
    type: "Feature",
    properties: { name: "Nam Du", nameEn: "Nam Du", type: "island", coordinates: [114.283, 8.033] },
    geometry: { type: "Point", coordinates: [114.283, 8.033] },
  },
  {
    type: "Feature",
    properties: { name: "Lắk", nameEn: "Lak", type: "island", coordinates: [114.233, 7.967] },
    geometry: { type: "Point", coordinates: [114.233, 7.967] },
  },
  {
    type: "Feature",
    properties: { name: "Châu Viễn", nameEn: "Chau Vien", type: "island", coordinates: [114.333, 8.417] },
    geometry: { type: "Point", coordinates: [114.333, 8.417] },
  },
  {
    type: "Feature",
    properties: { name: "Sơn Hòa", nameEn: "Son Hoa", type: "island", coordinates: [114.283, 8.500] },
    geometry: { type: "Point", coordinates: [114.283, 8.500] },
  },
  {
    type: "Feature",
    properties: { name: "Giá Vô", nameEn: "Gia Vo", type: "island", coordinates: [114.200, 8.350] },
    geometry: { type: "Point", coordinates: [114.200, 8.350] },
  },
  {
    type: "Feature",
    properties: { name: "Bình Sơn", nameEn: "Binh Son", type: "island", coordinates: [113.767, 10.333] },
    geometry: { type: "Point", coordinates: [113.767, 10.333] },
  },
  // Atolls and reefs
  {
    type: "Feature",
    properties: { name: "Trường Sa Thất", nameEn: "Truong Sa That", type: "atoll", coordinates: [111.950, 9.950] },
    geometry: { type: "Point", coordinates: [111.950, 9.950] },
  },
  {
    type: "Feature",
    properties: { name: "Bãi Thương", nameEn: "Bai Thuong", type: "atoll", coordinates: [112.417, 10.800] },
    geometry: { type: "Point", coordinates: [112.417, 10.800] },
  },
  {
    type: "Feature",
    properties: { name: "Đá Bắc Trường Sa", nameEn: "Da Bac Truong Sa", type: "reef", coordinates: [112.017, 10.517] },
    geometry: { type: "Point", coordinates: [112.017, 10.517] },
  },
  {
    type: "Feature",
    properties: { name: "Đá Nam Trường Sa", nameEn: "Da Nam Truong Sa", type: "reef", coordinates: [113.317, 10.417] },
    geometry: { type: "Point", coordinates: [113.317, 10.417] },
  },
  {
    type: "Feature",
    properties: { name: "Đá Tây Trường Sa", nameEn: "Da Tay Truong Sa", type: "reef", coordinates: [113.500, 10.583] },
    geometry: { type: "Point", coordinates: [113.500, 10.583] },
  },
  {
    type: "Feature",
    properties: { name: "Bãi cá", nameEn: "Bai Ca", type: "shoal", coordinates: [112.200, 10.200] },
    geometry: { type: "Point", coordinates: [112.200, 10.200] },
  },
  {
    type: "Feature",
    properties: { name: "Bãi ngầm", nameEn: "Bai Ngam", type: "shoal", coordinates: [113.600, 9.800] },
    geometry: { type: "Point", coordinates: [113.600, 9.800] },
  },
  {
    type: "Feature",
    properties: { name: "Đá Hòa Long", nameEn: "Da Hoa Long", type: "reef", coordinates: [111.833, 10.533] },
    geometry: { type: "Point", coordinates: [111.833, 10.533] },
  },
  {
    type: "Feature",
    properties: { name: "Đá Lệ", nameEn: "Da Le", type: "reef", coordinates: [112.217, 9.500] },
    geometry: { type: "Point", coordinates: [112.217, 9.500] },
  },
  {
    type: "Feature",
    properties: { name: "Đá Cá VUa", nameEn: "Da Ca Vu", type: "reef", coordinates: [113.267, 9.417] },
    geometry: { type: "Point", coordinates: [113.267, 9.417] },
  },
  {
    type: "Feature",
    properties: { name: "Đá Ba Cạnh", nameEn: "Da Ba Canh", type: "reef", coordinates: [112.350, 9.583] },
    geometry: { type: "Point", coordinates: [112.350, 9.583] },
  },
  {
    type: "Feature",
    properties: { name: "Đá Bông", nameEn: "Da Bong", type: "reef", coordinates: [112.400, 9.917] },
    geometry: { type: "Point", coordinates: [112.400, 9.917] },
  },
  {
    type: "Feature",
    properties: { name: "Đá Cô Linh", nameEn: "Da Co Linh", type: "reef", coordinates: [112.483, 10.017] },
    geometry: { type: "Point", coordinates: [112.483, 10.017] },
  },
  {
    type: "Feature",
    properties: { name: "Đá Kỳ Lạ", nameEn: "Da Ky La", type: "reef", coordinates: [112.683, 9.933] },
    geometry: { type: "Point", coordinates: [112.683, 9.933] },
  },
  {
    type: "Feature",
    properties: { name: "Đá Len Đao", nameEn: "Da Len Dao", type: "reef", coordinates: [114.433, 8.833] },
    geometry: { type: "Point", coordinates: [114.433, 8.833] },
  },
];

// Combined islands GeoJSON FeatureCollection (only point features, no boundary polygons)
export const VIETNAM_ISLANDS_GEOJSON: GeoJSON.FeatureCollection = {
  type: "FeatureCollection",
  features: [
    // Point features for individual islands only
    ...HOANG_SA_ISLANDS.map(island => island as GeoJSON.Feature),
    ...TRUONG_SA_ISLANDS.map(island => island as GeoJSON.Feature),
  ],
};