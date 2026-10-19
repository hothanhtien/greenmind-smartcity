import type { UrbanArea, Household } from "@/types/waste-report";

// ---------------------------------------------------------------------------
// CẤU TRÚC ĐỊA LÝ ĐÀ NẴNG (tham khảo OpenStreetMap + Google Maps):
//
// Sông Hàn chạy Bắc-Nam, ranh giới hai bờ:
//   Bờ Tây (Hải Châu, Thanh Khê) : lng < 108.220
//   Sông Hàn (TRÁNH)             : lng 108.220 – 108.228
//   Bờ Đông (Sơn Trà)            : lng 108.229 – 108.248
//   Biển Đông (TRÁNH)            : lng > 108.248 (khu Sơn Trà)
//
// Ngũ Hành Sơn (phía Nam):
//   Khu dân cư Mỹ An / Ngũ Hành Sơn: lng 108.210 – 108.235
//   Bãi biển Mỹ Khê (TRÁNH)         : lng > 108.240
//
// Thanh Khê (phía Tây):
//   Khu dân cư                      : lng 108.190 – 108.213
// ---------------------------------------------------------------------------
//
// THANG ĐO RÁC THỰC TẾ:
//   Hộ gia đình:
//     - Trung bình: 1.5 – 4.0 kg/ngày
//     - Cảnh báo (warning): ≥ 4.0 kg/hộ/ngày
//     - Nghiêm trọng (critical): ≥ 5.0 kg/hộ/ngày
//   Phường (ward):
//     - 2,000–5,000 hộ × ~2.5 kg/hộ/ngày
//     - Nhỏ:   4,000 – 6,000 kg/ngày
//     - Trung: 7,000 – 10,000 kg/ngày
//     - Lớn:  10,000 – 15,000 kg/ngày
//
//   Status logic:
//     if (waste_per_hh >= 5.0) status = "red"    (critical)
//     else if (waste_per_hh >= 4.0) status = "yellow" (warning)
//     else status = "green"  (normal)
// ---------------------------------------------------------------------------

export const WARDS: UrbanArea[] = [
  // ── Quận Hải Châu (bờ TÂY sông Hàn) ──────────────────────────────────────
  {
    id: 1,
    name: "Phường Hải Châu 1",
    district: "Hải Châu",
    population: 14200,
    areaKm2: 0.82,
    lat: 16.0668,
    lng: 108.2125,
    // ~3,550 hộ × 3.8 kg/hộ/ngày ≈ 13,490 kg → ~13.5 tấn/ngày
    totalWaste: 13490,
    status: "red",
    reports: 12,
    bounds: [
      [16.0720, 108.2060],
      [16.0720, 108.2200],
      [16.0630, 108.2200],
      [16.0630, 108.2060],
    ],
  },
  {
    id: 2,
    name: "Phường Hải Châu 2",
    district: "Hải Châu",
    population: 12600,
    areaKm2: 0.74,
    lat: 16.0590,
    lng: 108.2118,
    // ~3,150 hộ × 3.5 kg/hộ/ngày ≈ 11,025 kg → ~11.0 tấn/ngày
    totalWaste: 11025,
    status: "red",
    reports: 9,
    bounds: [
      [16.0630, 108.2060],
      [16.0630, 108.2195],
      [16.0555, 108.2195],
      [16.0555, 108.2060],
    ],
  },
  {
    id: 3,
    name: "Phường Thạch Thang",
    district: "Hải Châu",
    population: 9700,
    areaKm2: 0.91,
    lat: 16.0755,
    lng: 108.2140,
    // ~2,425 hộ × 3.0 kg/hộ/ngày ≈ 7,275 kg → ~7.3 tấn/ngày
    totalWaste: 7275,
    status: "yellow",
    reports: 6,
    bounds: [
      [16.0800, 108.2075],
      [16.0800, 108.2210],
      [16.0720, 108.2210],
      [16.0720, 108.2075],
    ],
  },
  {
    id: 4,
    name: "Phường Thanh Bình",
    district: "Hải Châu",
    population: 7000,
    areaKm2: 1.12,
    lat: 16.0835,
    lng: 108.2130,
    // ~1,750 hộ × 3.0 kg/hộ/ngày ≈ 5,250 kg → ~5.3 tấn/ngày
    totalWaste: 5250,
    status: "yellow",
    reports: 5,
    bounds: [
      [16.0880, 108.2070],
      [16.0880, 108.2210],
      [16.0800, 108.2210],
      [16.0800, 108.2070],
    ],
  },
  {
    id: 5,
    name: "Phường Thuận Phước",
    district: "Hải Châu",
    population: 5300,
    areaKm2: 1.45,
    lat: 16.0692,
    lng: 108.2040,
    // ~1,325 hộ × 3.2 kg/hộ/ngày ≈ 4,240 kg → ~4.2 tấn/ngày
    totalWaste: 4240,
    status: "yellow",
    reports: 4,
    bounds: [
      [16.0760, 108.1980],
      [16.0760, 108.2110],
      [16.0635, 108.2110],
      [16.0635, 108.1980],
    ],
  },

  // ── Quận Ngũ Hành Sơn (phía Nam, khu dân cư phía tây bãi biển) ──────────
  {
    id: 6,
    name: "Phường Mỹ An",
    district: "Ngũ Hành Sơn",
    population: 3200,
    areaKm2: 2.10,
    lat: 16.0460,
    lng: 108.2195,
    // ~800 hộ × 2.2 kg/hộ/ngày ≈ 1,760 kg → ~1.8 tấn/ngày
    totalWaste: 1760,
    status: "green",
    reports: 2,
    bounds: [
      [16.0520, 108.2100],
      [16.0520, 108.2300],
      [16.0410, 108.2300],
      [16.0410, 108.2100],
    ],
  },
  {
    id: 7,
    name: "Phường Khuê Mỹ",
    district: "Ngũ Hành Sơn",
    population: 2600,
    areaKm2: 1.75,
    lat: 16.0528,
    lng: 108.2270,
    // ~650 hộ × 2.0 kg/hộ/ngày ≈ 1,300 kg → ~1.3 tấn/ngày
    totalWaste: 1300,
    status: "green",
    reports: 1,
    bounds: [
      [16.0590, 108.2195],
      [16.0590, 108.2355],
      [16.0470, 108.2355],
      [16.0470, 108.2195],
    ],
  },

  // ── Quận Sơn Trà (bờ ĐÔNG sông Hàn) ──────────────────────────────────────
  {
    id: 8,
    name: "Phường An Hải Bắc",
    district: "Sơn Trà",
    population: 11200,
    areaKm2: 1.03,
    lat: 16.0748,
    lng: 108.2365,
    // ~2,800 hộ × 3.6 kg/hộ/ngày ≈ 10,080 kg → ~10.1 tấn/ngày
    totalWaste: 10080,
    status: "red",
    reports: 8,
    bounds: [
      [16.0810, 108.2295],
      [16.0810, 108.2445],
      [16.0700, 108.2445],
      [16.0700, 108.2295],
    ],
  },
  {
    id: 9,
    name: "Phường Mân Thái",
    district: "Sơn Trà",
    population: 5800,
    areaKm2: 1.28,
    lat: 16.0898,
    lng: 108.2370,
    // ~1,450 hộ × 2.8 kg/hộ/ngày ≈ 4,060 kg → ~4.1 tấn/ngày
    totalWaste: 4060,
    status: "yellow",
    reports: 4,
    bounds: [
      [16.0960, 108.2295],
      [16.0960, 108.2445],
      [16.0845, 108.2445],
      [16.0845, 108.2295],
    ],
  },
  {
    id: 10,
    name: "Phường Phước Mỹ",
    district: "Sơn Trà",
    population: 4000,
    areaKm2: 0.98,
    lat: 16.0822,
    lng: 108.2420,
    // ~1,000 hộ × 2.3 kg/hộ/ngày ≈ 2,300 kg → ~2.3 tấn/ngày
    totalWaste: 2300,
    status: "green",
    reports: 2,
    bounds: [
      [16.0870, 108.2345],
      [16.0870, 108.2490],
      [16.0770, 108.2490],
      [16.0770, 108.2345],
    ],
  },

  // ── Quận Thanh Khê (phía TÂY) ─────────────────────────────────────────────
  {
    id: 11,
    name: "Phường Xuân Hà",
    district: "Thanh Khê",
    population: 8700,
    areaKm2: 1.06,
    lat: 16.0590,
    lng: 108.2015,
    // ~2,175 hộ × 3.2 kg/hộ/ngày ≈ 6,960 kg → ~7.0 tấn/ngày
    totalWaste: 6960,
    status: "yellow",
    reports: 5,
    bounds: [
      [16.0640, 108.1940],
      [16.0640, 108.2095],
      [16.0545, 108.2095],
      [16.0545, 108.1940],
    ],
  },
  {
    id: 12,
    name: "Phường Tân Chính",
    district: "Thanh Khê",
    population: 2900,
    areaKm2: 0.87,
    lat: 16.0485,
    lng: 108.2060,
    // ~725 hộ × 2.0 kg/hộ/ngày ≈ 1,450 kg → ~1.5 tấn/ngày
    totalWaste: 1450,
    status: "green",
    reports: 1,
    bounds: [
      [16.0545, 108.1980],
      [16.0545, 108.2140],
      [16.0430, 108.2140],
      [16.0430, 108.1980],
    ],
  },
];

// ---------------------------------------------------------------------------
// HOUSEHOLDS — waste tính theo kg/ngày (thực tế: 1.5 – 5.5 kg/hộ/ngày)
//
// Status dựa trên ngưỡng kg/hộ/ngày:
//   >= 5.0 kg → "red"    (critical  — thu gom khẩn)
//   >= 4.0 kg → "yellow" (warning   — cần theo dõi)
//   <  4.0 kg → "green"  (normal    — bình thường)
// ---------------------------------------------------------------------------
export const HOUSEHOLDS: Household[] = [

  // ── Ward 1 – Phường Hải Châu 1 | bờ Tây sông Hàn | lng: 108.206–108.219 ──
  { id: 101, wardId: 1, name: "Hộ Nguyễn Văn An",    address: "12 Trần Phú",            lat: 16.0680, lng: 108.2095, waste: 5.2, status: "red",    reportCount: 3 },
  { id: 102, wardId: 1, name: "Hộ Trần Thị Bình",    address: "45 Lý Tự Trọng",         lat: 16.0665, lng: 108.2140, waste: 4.9, status: "red",    reportCount: 2 },
  { id: 103, wardId: 1, name: "Hộ Lê Minh Cường",    address: "8 Hùng Vương",            lat: 16.0700, lng: 108.2078, waste: 3.8, status: "green",  reportCount: 1 },
  { id: 104, wardId: 1, name: "Hộ Phạm Thị Dung",    address: "23 Nguyễn Chí Thanh",    lat: 16.0650, lng: 108.2168, waste: 3.1, status: "green",  reportCount: 1 },
  { id: 105, wardId: 1, name: "Hộ Hoàng Văn Em",     address: "5 Pasteur",               lat: 16.0712, lng: 108.2115, waste: 4.2, status: "yellow", reportCount: 2 },
  { id: 106, wardId: 1, name: "Hộ Đỗ Thị Fương",     address: "17 Trần Quốc Toản",      lat: 16.0668, lng: 108.2185, waste: 2.9, status: "green",  reportCount: 1 },
  { id: 107, wardId: 1, name: "Hộ Bùi Văn Giang",    address: "30 Ngô Gia Tự",          lat: 16.0690, lng: 108.2065, waste: 5.4, status: "red",    reportCount: 3 },
  { id: 108, wardId: 1, name: "Hộ Vũ Thị Hải",       address: "9 Yên Bái",              lat: 16.0638, lng: 108.2108, waste: 2.0, status: "green",  reportCount: 1 },
  { id: 109, wardId: 1, name: "Hộ Ngô Văn Inh",      address: "61 Phan Châu Trinh",     lat: 16.0716, lng: 108.2155, waste: 4.6, status: "red",    reportCount: 2 },
  { id: 110, wardId: 1, name: "Hộ Đinh Thị Kiều",    address: "3 Lê Hồng Phong",        lat: 16.0648, lng: 108.2082, waste: 3.7, status: "green",  reportCount: 1 },
  { id: 111, wardId: 1, name: "Hộ Phan Văn Long",    address: "88 Duy Tân",             lat: 16.0635, lng: 108.2195, waste: 2.7, status: "green",  reportCount: 1 },

  // ── Ward 2 – Phường Hải Châu 2 | bờ Tây | lng: 108.206–108.218 ────────────
  { id: 201, wardId: 2, name: "Hộ Nguyễn Thị Mai",   address: "14 Lê Duẩn",             lat: 16.0618, lng: 108.2088, waste: 4.8, status: "red",    reportCount: 2 },
  { id: 202, wardId: 2, name: "Hộ Trần Văn Nam",      address: "27 Trường Chinh",        lat: 16.0605, lng: 108.2125, waste: 4.4, status: "yellow", reportCount: 2 },
  { id: 203, wardId: 2, name: "Hộ Lê Thị Oanh",      address: "5 Núi Thành",            lat: 16.0580, lng: 108.2158, waste: 3.5, status: "green",  reportCount: 1 },
  { id: 204, wardId: 2, name: "Hộ Phạm Văn Phát",    address: "19 Điện Biên Phủ",       lat: 16.0562, lng: 108.2175, waste: 5.0, status: "red",    reportCount: 3 },
  { id: 205, wardId: 2, name: "Hộ Hoàng Thị Quỳnh",  address: "42 Lê Lợi",             lat: 16.0628, lng: 108.2155, waste: 2.8, status: "green",  reportCount: 1 },
  { id: 206, wardId: 2, name: "Hộ Đỗ Văn Rộng",      address: "7 Thái Phiên",           lat: 16.0610, lng: 108.2075, waste: 4.1, status: "yellow", reportCount: 2 },
  { id: 207, wardId: 2, name: "Hộ Bùi Thị Sương",    address: "31 Ông Ích Khiêm",       lat: 16.0572, lng: 108.2098, waste: 3.1, status: "green",  reportCount: 1 },
  { id: 208, wardId: 2, name: "Hộ Vũ Văn Tuấn",      address: "56 Trần Bình Trọng",     lat: 16.0592, lng: 108.2185, waste: 3.9, status: "green",  reportCount: 2 },
  { id: 209, wardId: 2, name: "Hộ Ngô Thị Uyên",     address: "10 Nguyễn Tất Thành",   lat: 16.0624, lng: 108.2068, waste: 1.5, status: "green",  reportCount: 0 },
  { id: 210, wardId: 2, name: "Hộ Đinh Văn Vĩnh",    address: "23 Nguyễn Hoàng",        lat: 16.0558, lng: 108.2142, waste: 4.7, status: "red",    reportCount: 3 },
  { id: 211, wardId: 2, name: "Hộ Phan Thị Ý",       address: "8 Trưng Nữ Vương",       lat: 16.0601, lng: 108.2110, waste: 3.2, status: "green",  reportCount: 1 },

  // ── Ward 3 – Phường Thạch Thang | bờ Tây | lng: 108.208–108.220 ──────────
  { id: 301, wardId: 3, name: "Hộ Nguyễn Văn Xuân",  address: "14 Nguyễn Thị Minh Khai",lat: 16.0748, lng: 108.2095, waste: 2.8, status: "green",  reportCount: 1 },
  { id: 302, wardId: 3, name: "Hộ Trần Thị Yến",     address: "22 Phan Đình Phùng",     lat: 16.0762, lng: 108.2148, waste: 3.2, status: "green",  reportCount: 1 },
  { id: 303, wardId: 3, name: "Hộ Lê Văn Ý",         address: "5 Hoàng Diệu",           lat: 16.0732, lng: 108.2178, waste: 1.8, status: "green",  reportCount: 0 },
  { id: 304, wardId: 3, name: "Hộ Phạm Thị Ánh",     address: "39 Nguyễn Tri Phương",   lat: 16.0775, lng: 108.2118, waste: 3.5, status: "green",  reportCount: 1 },
  { id: 305, wardId: 3, name: "Hộ Hoàng Văn Bắc",    address: "11 Quang Trung",         lat: 16.0790, lng: 108.2082, waste: 2.1, status: "green",  reportCount: 0 },
  { id: 306, wardId: 3, name: "Hộ Đỗ Thị Cẩm",       address: "47 Lê Thánh Tôn",       lat: 16.0725, lng: 108.2202, waste: 4.3, status: "yellow", reportCount: 2 },
  { id: 307, wardId: 3, name: "Hộ Bùi Văn Dũng",     address: "3 Đống Đa",              lat: 16.0758, lng: 108.2158, waste: 2.6, status: "green",  reportCount: 1 },
  { id: 308, wardId: 3, name: "Hộ Vũ Thị Én",        address: "18 Trần Cao Vân",        lat: 16.0782, lng: 108.2102, waste: 1.4, status: "green",  reportCount: 0 },
  { id: 309, wardId: 3, name: "Hộ Ngô Văn Gấm",      address: "65 Lý Chính Thắng",     lat: 16.0795, lng: 108.2135, waste: 3.8, status: "green",  reportCount: 1 },
  { id: 310, wardId: 3, name: "Hộ Đinh Thị Hạnh",    address: "9 Nguyễn Văn Linh",     lat: 16.0740, lng: 108.2172, waste: 2.95, status: "green", reportCount: 1 },

  // ── Ward 4 – Phường Thanh Bình | bờ Tây | lng: 108.208–108.220 ──────────
  { id: 401, wardId: 4, name: "Hộ Nguyễn Thị Hiền",  address: "7 Hùng Vương",           lat: 16.0812, lng: 108.2088, waste: 2.4, status: "green",  reportCount: 1 },
  { id: 402, wardId: 4, name: "Hộ Trần Văn Hùng",    address: "15 Nguyễn Chí Thanh",    lat: 16.0845, lng: 108.2115, waste: 3.1, status: "green",  reportCount: 1 },
  { id: 403, wardId: 4, name: "Hộ Lê Thị Hoa",       address: "28 Nguyễn Thái Học",     lat: 16.0822, lng: 108.2168, waste: 1.6, status: "green",  reportCount: 0 },
  { id: 404, wardId: 4, name: "Hộ Phạm Văn Khoa",    address: "40 Bạch Đằng",           lat: 16.0858, lng: 108.2135, waste: 3.9, status: "green",  reportCount: 2 },
  { id: 405, wardId: 4, name: "Hộ Hoàng Thị Lan",    address: "3 Ngô Quyền",            lat: 16.0872, lng: 108.2092, waste: 1.2, status: "green",  reportCount: 0 },
  { id: 406, wardId: 4, name: "Hộ Đỗ Văn Mạnh",      address: "52 Chi Lăng",            lat: 16.0815, lng: 108.2145, waste: 2.8, status: "green",  reportCount: 1 },
  { id: 407, wardId: 4, name: "Hộ Bùi Thị Nga",      address: "17 Tống Duy Tân",        lat: 16.0850, lng: 108.2195, waste: 2.0, status: "green",  reportCount: 0 },
  { id: 408, wardId: 4, name: "Hộ Vũ Văn Oanh",      address: "9 Nguyễn Biểu",          lat: 16.0808, lng: 108.2075, waste: 3.5, status: "green",  reportCount: 1 },
  { id: 409, wardId: 4, name: "Hộ Ngô Thị Phương",   address: "34 Trần Hưng Đạo",       lat: 16.0868, lng: 108.2178, waste: 1.8, status: "green",  reportCount: 0 },
  { id: 410, wardId: 4, name: "Hộ Đinh Văn Quyền",   address: "6 Hải Phòng",            lat: 16.0835, lng: 108.2208, waste: 2.7, status: "green",  reportCount: 1 },

  // ── Ward 5 – Phường Thuận Phước | cửa sông phía Tây | lng: 108.198–108.210 ─
  { id: 501, wardId: 5, name: "Hộ Nguyễn Văn Sang",  address: "22 Trần Thị Lý",         lat: 16.0688, lng: 108.2012, waste: 2.0, status: "green",  reportCount: 1 },
  { id: 502, wardId: 5, name: "Hộ Trần Thị Thắm",    address: "5 Đinh Tiên Hoàng",      lat: 16.0702, lng: 108.2048, waste: 1.8, status: "green",  reportCount: 0 },
  { id: 503, wardId: 5, name: "Hộ Lê Minh Thành",    address: "38 Nguyễn Cư Trinh",     lat: 16.0665, lng: 108.2075, waste: 2.6, status: "green",  reportCount: 1 },
  { id: 504, wardId: 5, name: "Hộ Phạm Thị Thuận",   address: "11 Nguyễn Hữu Thọ",      lat: 16.0650, lng: 108.2092, waste: 3.1, status: "green",  reportCount: 1 },
  { id: 505, wardId: 5, name: "Hộ Hoàng Văn Tín",    address: "43 Lê Văn Hưu",          lat: 16.0718, lng: 108.2025, waste: 1.5, status: "green",  reportCount: 0 },
  { id: 506, wardId: 5, name: "Hộ Đỗ Thị Uyên",      address: "7 Hải Phòng",            lat: 16.0642, lng: 108.2062, waste: 2.2, status: "green",  reportCount: 1 },
  { id: 507, wardId: 5, name: "Hộ Bùi Văn Vượng",    address: "29 Lê Hồng Phong",       lat: 16.0730, lng: 108.2008, waste: 3.5, status: "green",  reportCount: 1 },
  { id: 508, wardId: 5, name: "Hộ Vũ Thị Xinh",      address: "14 Trần Văn Trà",        lat: 16.0660, lng: 108.1998, waste: 1.2, status: "green",  reportCount: 0 },
  { id: 509, wardId: 5, name: "Hộ Ngô Văn Yên",      address: "6 Dương Bạch Mai",       lat: 16.0710, lng: 108.2100, waste: 1.9, status: "green",  reportCount: 0 },

  // ── Ward 6 – Phường Mỹ An | Ngũ Hành Sơn | lng: 108.212–108.228 ──────────
  // Khu đô thị Mỹ An, phía tây Mỹ Khê — KHÔNG được vượt lng 108.235
  { id: 601, wardId: 6, name: "Hộ Nguyễn Thị Thanh", address: "5 Huỳnh Ngọc Huệ",       lat: 16.0462, lng: 108.2128, waste: 1.9, status: "green",  reportCount: 0 },
  { id: 602, wardId: 6, name: "Hộ Trần Văn Thịnh",   address: "18 Hà Bổng",             lat: 16.0478, lng: 108.2168, waste: 2.2, status: "green",  reportCount: 0 },
  { id: 603, wardId: 6, name: "Hộ Lê Thị Thu",       address: "7 Lâm Hoành",            lat: 16.0445, lng: 108.2202, waste: 1.6, status: "green",  reportCount: 0 },
  { id: 604, wardId: 6, name: "Hộ Phạm Văn Toàn",    address: "31 Nguyễn Đình Tứ",      lat: 16.0512, lng: 108.2145, waste: 3.0, status: "green",  reportCount: 1 },
  { id: 605, wardId: 6, name: "Hộ Hoàng Thị Trà",    address: "12 Lưu Quý Kỳ",          lat: 16.0420, lng: 108.2252, waste: 2.2, status: "green",  reportCount: 1 },
  { id: 606, wardId: 6, name: "Hộ Đỗ Văn Tùng",      address: "8 Phạm Văn Đồng",        lat: 16.0490, lng: 108.2272, waste: 1.4, status: "green",  reportCount: 0 },
  { id: 607, wardId: 6, name: "Hộ Bùi Thị Uyên",     address: "25 Nguyễn Văn Thoại",    lat: 16.0508, lng: 108.2228, waste: 1.9, status: "green",  reportCount: 0 },
  { id: 608, wardId: 6, name: "Hộ Vũ Văn Vân",       address: "44 Hà Huy Tập",          lat: 16.0435, lng: 108.2115, waste: 2.6, status: "green",  reportCount: 0 },

  // ── Ward 7 – Phường Khuê Mỹ | Ngũ Hành Sơn | lng: 108.220–108.234 ────────
  // Khu dân cư gần cầu Nguyễn Văn Trỗi; KHÔNG được vượt lng 108.238
  { id: 701, wardId: 7, name: "Hộ Nguyễn Văn Xuân",  address: "3 An Thượng 1",          lat: 16.0535, lng: 108.2215, waste: 1.5, status: "green",  reportCount: 0 },
  { id: 702, wardId: 7, name: "Hộ Trần Thị Yến",     address: "11 An Thượng 4",         lat: 16.0552, lng: 108.2258, waste: 1.7, status: "green",  reportCount: 0 },
  { id: 703, wardId: 7, name: "Hộ Lê Văn Ý",         address: "22 Hồ Xuân Hương",       lat: 16.0518, lng: 108.2292, waste: 2.4, status: "green",  reportCount: 0 },
  { id: 704, wardId: 7, name: "Hộ Phạm Thị Ánh",     address: "7 An Trung 3",           lat: 16.0572, lng: 108.2240, waste: 2.8, status: "green",  reportCount: 1 },
  { id: 705, wardId: 7, name: "Hộ Hoàng Văn Bảo",    address: "15 Ngô Thì Nhậm",        lat: 16.0502, lng: 108.2208, waste: 1.5, status: "green",  reportCount: 0 },
  { id: 706, wardId: 7, name: "Hộ Đỗ Thị Cúc",       address: "2 An Thượng 2",          lat: 16.0545, lng: 108.2325, waste: 2.0, status: "green",  reportCount: 0 },
  { id: 707, wardId: 7, name: "Hộ Bùi Văn Đạt",      address: "38 Trường Sa",           lat: 16.0482, lng: 108.2348, waste: 3.6, status: "green",  reportCount: 1 },
  { id: 708, wardId: 7, name: "Hộ Vũ Thị Oanh",      address: "9 An Trung 1",           lat: 16.0560, lng: 108.2270, waste: 1.8, status: "green",  reportCount: 0 },

  // ── Ward 8 – Phường An Hải Bắc | Sơn Trà | lng: 108.230–108.243 ──────────
  // Bờ ĐÔNG sông Hàn — KHÔNG được < lng 108.230
  { id: 801, wardId: 8, name: "Hộ Nguyễn Văn Quang", address: "15 Lê Đình Lý",          lat: 16.0760, lng: 108.2312, waste: 4.3, status: "yellow", reportCount: 2 },
  { id: 802, wardId: 8, name: "Hộ Trần Thị Thúy",    address: "8 Đỗ Quang",             lat: 16.0778, lng: 108.2342, waste: 3.8, status: "green",  reportCount: 2 },
  { id: 803, wardId: 8, name: "Hộ Lê Văn Sơn",       address: "27 Hoàng Sa",            lat: 16.0742, lng: 108.2368, waste: 2.7, status: "green",  reportCount: 1 },
  { id: 804, wardId: 8, name: "Hộ Phạm Thị Tiên",    address: "42 Đỗ Bá",              lat: 16.0768, lng: 108.2398, waste: 4.6, status: "red",    reportCount: 3 },
  { id: 805, wardId: 8, name: "Hộ Hoàng Văn Ung",    address: "6 Trần Đại Nghĩa",       lat: 16.0798, lng: 108.2325, waste: 3.2, status: "green",  reportCount: 1 },
  { id: 806, wardId: 8, name: "Hộ Đỗ Thị Vân",       address: "33 Lê Hữu Trác",         lat: 16.0728, lng: 108.2302, waste: 3.9, status: "green",  reportCount: 2 },
  { id: 807, wardId: 8, name: "Hộ Bùi Văn Vương",    address: "11 An Hải Bắc",          lat: 16.0752, lng: 108.2428, waste: 2.5, status: "green",  reportCount: 1 },
  { id: 808, wardId: 8, name: "Hộ Vũ Thị Ý",         address: "19 Trần Bạch Đằng",      lat: 16.0788, lng: 108.2382, waste: 4.1, status: "yellow", reportCount: 2 },
  { id: 809, wardId: 8, name: "Hộ Ngô Văn Yên",      address: "5 Đỗ Mậu",              lat: 16.0710, lng: 108.2408, waste: 1.8, status: "green",  reportCount: 0 },
  { id: 810, wardId: 8, name: "Hộ Đinh Thị Lan",     address: "24 Hồ Nghinh",           lat: 16.0775, lng: 108.2355, waste: 3.4, status: "green",  reportCount: 1 },

  // ── Ward 9 – Phường Mân Thái | Sơn Trà | lng: 108.230–108.244 ──────────
  { id: 901, wardId: 9, name: "Hộ Nguyễn Thị Ánh",   address: "7 Mân Thái 1",           lat: 16.0858, lng: 108.2318, waste: 2.1, status: "green",  reportCount: 1 },
  { id: 902, wardId: 9, name: "Hộ Trần Văn Biên",     address: "18 Mân Thái 2",          lat: 16.0878, lng: 108.2355, waste: 2.5, status: "green",  reportCount: 1 },
  { id: 903, wardId: 9, name: "Hộ Lê Thị Cúc",        address: "4 Yên Thế",              lat: 16.0848, lng: 108.2382, waste: 1.8, status: "green",  reportCount: 0 },
  { id: 904, wardId: 9, name: "Hộ Phạm Văn Đức",      address: "31 Hoàng Văn Thái",      lat: 16.0908, lng: 108.2308, waste: 2.9, status: "green",  reportCount: 1 },
  { id: 905, wardId: 9, name: "Hộ Hoàng Thị Phượng",  address: "9 Trần Đình Tri",        lat: 16.0868, lng: 108.2418, waste: 1.4, status: "green",  reportCount: 0 },
  { id: 906, wardId: 9, name: "Hộ Đỗ Văn Tiến",       address: "22 Mân Thái 3",          lat: 16.0925, lng: 108.2342, waste: 3.1, status: "green",  reportCount: 1 },
  { id: 907, wardId: 9, name: "Hộ Bùi Thị Thu",       address: "5 Nguyễn Tử Đang",       lat: 16.0842, lng: 108.2305, waste: 1.6, status: "green",  reportCount: 0 },
  { id: 908, wardId: 9, name: "Hộ Vũ Văn Thể",        address: "14 Lê Văn Thứ",          lat: 16.0942, lng: 108.2365, waste: 2.0, status: "green",  reportCount: 1 },
  { id: 909, wardId: 9, name: "Hộ Ngô Thị Phúc",      address: "6 Nguyễn Văn Khánh",     lat: 16.0895, lng: 108.2440, waste: 2.4, status: "green",  reportCount: 1 },

  // ── Ward 10 – Phường Phước Mỹ | Sơn Trà | lng: 108.235–108.248 ──────────
  // Khu dân cư nội địa, phía tây đường Trường Sa — KHÔNG vượt lng 108.248
  { id: 1001, wardId: 10, name: "Hộ Nguyễn Văn Linh",  address: "3 Phước Mỹ 1",          lat: 16.0788, lng: 108.2368, waste: 1.6, status: "green",  reportCount: 0 },
  { id: 1002, wardId: 10, name: "Hộ Trần Thị Loan",    address: "12 Phước Mỹ 2",          lat: 16.0808, lng: 108.2398, waste: 2.1, status: "green",  reportCount: 0 },
  { id: 1003, wardId: 10, name: "Hộ Lê Văn Minh",      address: "7 Trường Sa",             lat: 16.0778, lng: 108.2428, waste: 1.8, status: "green",  reportCount: 0 },
  { id: 1004, wardId: 10, name: "Hộ Phạm Thị Ngọc",    address: "25 Phước Mỹ 3",          lat: 16.0845, lng: 108.2408, waste: 2.4, status: "green",  reportCount: 1 },
  { id: 1005, wardId: 10, name: "Hộ Hoàng Văn Nghĩa",  address: "9 Nguyễn Phan Vinh",     lat: 16.0815, lng: 108.2352, waste: 2.8, status: "green",  reportCount: 1 },
  { id: 1006, wardId: 10, name: "Hộ Đỗ Thị Oanh",      address: "18 Phước Mỹ 4",          lat: 16.0862, lng: 108.2462, waste: 1.5, status: "green",  reportCount: 0 },
  { id: 1007, wardId: 10, name: "Hộ Bùi Văn Phúc",     address: "4 Lê Đình Lý (nhánh)",   lat: 16.0832, lng: 108.2478, waste: 1.9, status: "green",  reportCount: 0 },
  { id: 1008, wardId: 10, name: "Hộ Vũ Thị Quyên",     address: "31 An Thượng 5",         lat: 16.0795, lng: 108.2448, waste: 2.2, status: "green",  reportCount: 0 },

  // ── Ward 11 – Phường Xuân Hà | Thanh Khê | lng: 108.195–108.208 ──────────
  { id: 1101, wardId: 11, name: "Hộ Nguyễn Thị Rạng",  address: "9 Xuân Hà",             lat: 16.0598, lng: 108.1975, waste: 2.8, status: "green",  reportCount: 1 },
  { id: 1102, wardId: 11, name: "Hộ Trần Văn Sen",      address: "24 Đinh Tiên Hoàng",     lat: 16.0612, lng: 108.2005, waste: 3.2, status: "green",  reportCount: 1 },
  { id: 1103, wardId: 11, name: "Hộ Lê Thị Thanh",      address: "6 Lê Đình Dương",        lat: 16.0578, lng: 108.2035, waste: 1.9, status: "green",  reportCount: 0 },
  { id: 1104, wardId: 11, name: "Hộ Phạm Văn Úc",       address: "37 Nguyễn Hữu Thọ",     lat: 16.0628, lng: 108.1958, waste: 3.5, status: "green",  reportCount: 1 },
  { id: 1105, wardId: 11, name: "Hộ Hoàng Thị Vân",     address: "12 Phan Châu Trinh",     lat: 16.0558, lng: 108.2068, waste: 2.4, status: "green",  reportCount: 1 },
  { id: 1106, wardId: 11, name: "Hộ Đỗ Văn Vương",      address: "5 Thi Sách",             lat: 16.0585, lng: 108.2088, waste: 1.6, status: "green",  reportCount: 0 },
  { id: 1107, wardId: 11, name: "Hộ Bùi Thị Xuân",      address: "48 Phạm Ngũ Lão",        lat: 16.0618, lng: 108.2048, waste: 4.2, status: "yellow", reportCount: 2 },
  { id: 1108, wardId: 11, name: "Hộ Vũ Văn Yến",        address: "16 Tôn Thất Thuyết",     lat: 16.0552, lng: 108.1972, waste: 2.0, status: "green",  reportCount: 1 },
  { id: 1109, wardId: 11, name: "Hộ Ngô Thị Thắm",      address: "3 Nguyễn Huy Tự",        lat: 16.0600, lng: 108.2078, waste: 1.4, status: "green",  reportCount: 0 },

  // ── Ward 12 – Phường Tân Chính | Thanh Khê | lng: 108.200–108.212 ────────
  { id: 1201, wardId: 12, name: "Hộ Nguyễn Văn Bình",   address: "7 Tân Chính",            lat: 16.0495, lng: 108.2008, waste: 1.5, status: "green",  reportCount: 0 },
  { id: 1202, wardId: 12, name: "Hộ Trần Thị Châu",     address: "19 Nguyễn Văn Linh",     lat: 16.0510, lng: 108.2042, waste: 2.2, status: "green",  reportCount: 0 },
  { id: 1203, wardId: 12, name: "Hộ Lê Văn Cảnh",       address: "4 Ông Ích Khiêm",        lat: 16.0475, lng: 108.2072, waste: 1.8, status: "green",  reportCount: 0 },
  { id: 1204, wardId: 12, name: "Hộ Phạm Thị Đào",      address: "28 Hàn Mặc Tử",          lat: 16.0525, lng: 108.2018, waste: 2.6, status: "green",  reportCount: 1 },
  { id: 1205, wardId: 12, name: "Hộ Hoàng Văn Giang",   address: "11 Tô Hiến Thành",        lat: 16.0452, lng: 108.2108, waste: 1.6, status: "green",  reportCount: 0 },
  { id: 1206, wardId: 12, name: "Hộ Đỗ Thị Hoàng",      address: "33 Phùng Khắc Khoan",    lat: 16.0502, lng: 108.2128, waste: 2.2, status: "green",  reportCount: 1 },
  { id: 1207, wardId: 12, name: "Hộ Bùi Văn Khánh",     address: "6 Trần Nhân Tông",        lat: 16.0515, lng: 108.2088, waste: 1.5, status: "green",  reportCount: 0 },
  { id: 1208, wardId: 12, name: "Hộ Vũ Thị Hồng",       address: "15 Huỳnh Bá Chánh",      lat: 16.0460, lng: 108.2055, waste: 1.7, status: "green",  reportCount: 0 },
];
