import type { EnvAlert } from "@/types/waste-report";

// ---------------------------------------------------------------------------
// Dữ liệu cảnh báo môi trường theo phường tại Đà Nẵng
// level:
//   "normal"   → bình thường (xanh)
//   "warning"  → cần chú ý (vàng)
//   "critical" → nguy hiểm (đỏ)
// ---------------------------------------------------------------------------

export const ENV_ALERTS: EnvAlert[] = [
  // ── Phường Hải Châu 1 ─────────────────────────────────────────────────────
  { id: 1001, lat: 16.0680, lng: 108.2095, wardName: "Phường Hải Châu 1", level: "critical",  description: "Điểm tập kết rác quá tải" },
  { id: 1002, lat: 16.0665, lng: 108.2140, wardName: "Phường Hải Châu 1", level: "warning",   description: "Nước thải sinh hoạt tràn" },
  { id: 1003, lat: 16.0700, lng: 108.2078, wardName: "Phường Hải Châu 1", level: "normal",    description: "Khu vực sạch sẽ" },
  { id: 1004, lat: 16.0650, lng: 108.2168, wardName: "Phường Hải Châu 1", level: "warning",   description: "Rác nhựa tồn đọng" },
  { id: 1005, lat: 16.0712, lng: 108.2115, wardName: "Phường Hải Châu 1", level: "critical",  description: "Ô nhiễm mùi nghiêm trọng" },
  { id: 1006, lat: 16.0668, lng: 108.2185, wardName: "Phường Hải Châu 1", level: "normal",    description: "Đã thu gom hoàn thành" },
  { id: 1007, lat: 16.0638, lng: 108.2108, wardName: "Phường Hải Châu 1", level: "warning",   description: "Ứ đọng trước cống" },

  // ── Phường Hải Châu 2 ─────────────────────────────────────────────────────
  { id: 2001, lat: 16.0618, lng: 108.2088, wardName: "Phường Hải Châu 2", level: "critical",  description: "Bãi rác tự phát" },
  { id: 2002, lat: 16.0605, lng: 108.2125, wardName: "Phường Hải Châu 2", level: "warning",   description: "Rò rỉ nước đen" },
  { id: 2003, lat: 16.0580, lng: 108.2158, wardName: "Phường Hải Châu 2", level: "normal",    description: "Đường phố sạch" },
  { id: 2004, lat: 16.0562, lng: 108.2175, wardName: "Phường Hải Châu 2", level: "critical",  description: "Phế liệu xây dựng" },
  { id: 2005, lat: 16.0628, lng: 108.2155, wardName: "Phường Hải Châu 2", level: "warning",   description: "Rác hữu cơ phân hủy" },
  { id: 2006, lat: 16.0601, lng: 108.2110, wardName: "Phường Hải Châu 2", level: "normal",    description: "Khu vực ổn định" },

  // ── Phường Thạch Thang ────────────────────────────────────────────────────
  { id: 3001, lat: 16.0748, lng: 108.2095, wardName: "Phường Thạch Thang", level: "normal",   description: "Môi trường tốt" },
  { id: 3002, lat: 16.0762, lng: 108.2148, wardName: "Phường Thạch Thang", level: "warning",  description: "Rác không phân loại" },
  { id: 3003, lat: 16.0732, lng: 108.2178, wardName: "Phường Thạch Thang", level: "normal",   description: "Đã xử lý" },
  { id: 3004, lat: 16.0775, lng: 108.2118, wardName: "Phường Thạch Thang", level: "warning",  description: "Cống thoát nước bị nghẽn" },
  { id: 3005, lat: 16.0790, lng: 108.2082, wardName: "Phường Thạch Thang", level: "normal",   description: "Bình thường" },

  // ── Phường Thanh Bình ─────────────────────────────────────────────────────
  { id: 4001, lat: 16.0812, lng: 108.2088, wardName: "Phường Thanh Bình", level: "normal",    description: "Khu dân cư sạch" },
  { id: 4002, lat: 16.0845, lng: 108.2115, wardName: "Phường Thanh Bình", level: "warning",   description: "Nguy cơ ngập úng" },
  { id: 4003, lat: 16.0822, lng: 108.2168, wardName: "Phường Thanh Bình", level: "normal",    description: "Vừa thu gom" },
  { id: 4004, lat: 16.0858, lng: 108.2135, wardName: "Phường Thanh Bình", level: "warning",   description: "Rác đường phố nhiều" },
  { id: 4005, lat: 16.0872, lng: 108.2092, wardName: "Phường Thanh Bình", level: "normal",    description: "Khu vực xanh" },

  // ── Phường Thuận Phước ────────────────────────────────────────────────────
  { id: 5001, lat: 16.0688, lng: 108.2012, wardName: "Phường Thuận Phước", level: "warning",  description: "Rác ven biển tích tụ" },
  { id: 5002, lat: 16.0702, lng: 108.2048, wardName: "Phường Thuận Phước", level: "normal",   description: "Bình thường" },
  { id: 5003, lat: 16.0665, lng: 108.2075, wardName: "Phường Thuận Phước", level: "warning",  description: "Túi nylon ven sông" },
  { id: 5004, lat: 16.0650, lng: 108.2092, wardName: "Phường Thuận Phước", level: "normal",   description: "Đã làm sạch" },
  { id: 5005, lat: 16.0718, lng: 108.2025, wardName: "Phường Thuận Phước", level: "critical", description: "Điểm đen rác thải" },

  // ── Phường Mỹ An ──────────────────────────────────────────────────────────
  { id: 6001, lat: 16.0462, lng: 108.2128, wardName: "Phường Mỹ An", level: "normal",         description: "Khu đô thị sạch" },
  { id: 6002, lat: 16.0478, lng: 108.2168, wardName: "Phường Mỹ An", level: "normal",         description: "Không có vấn đề" },
  { id: 6003, lat: 16.0445, lng: 108.2202, wardName: "Phường Mỹ An", level: "warning",        description: "Cần kiểm tra" },
  { id: 6004, lat: 16.0512, lng: 108.2145, wardName: "Phường Mỹ An", level: "normal",         description: "Tốt" },
  { id: 6005, lat: 16.0490, lng: 108.2272, wardName: "Phường Mỹ An", level: "normal",         description: "Sạch sẽ" },

  // ── Phường Khuê Mỹ ───────────────────────────────────────────────────────
  { id: 7001, lat: 16.0535, lng: 108.2215, wardName: "Phường Khuê Mỹ", level: "normal",       description: "Ổn định" },
  { id: 7002, lat: 16.0552, lng: 108.2258, wardName: "Phường Khuê Mỹ", level: "normal",       description: "Bình thường" },
  { id: 7003, lat: 16.0518, lng: 108.2292, wardName: "Phường Khuê Mỹ", level: "warning",      description: "Khói đốt rác gia đình" },
  { id: 7004, lat: 16.0572, lng: 108.2240, wardName: "Phường Khuê Mỹ", level: "normal",       description: "Sạch sẽ" },

  // ── Phường An Hải Bắc ────────────────────────────────────────────────────
  { id: 8001, lat: 16.0760, lng: 108.2312, wardName: "Phường An Hải Bắc", level: "warning",   description: "Rác tập kết sai quy định" },
  { id: 8002, lat: 16.0778, lng: 108.2342, wardName: "Phường An Hải Bắc", level: "critical",  description: "Ô nhiễm nghiêm trọng" },
  { id: 8003, lat: 16.0742, lng: 108.2368, wardName: "Phường An Hải Bắc", level: "normal",    description: "Đã thu gom" },
  { id: 8004, lat: 16.0768, lng: 108.2398, wardName: "Phường An Hải Bắc", level: "critical",  description: "Đốt rác trái phép" },
  { id: 8005, lat: 16.0798, lng: 108.2325, wardName: "Phường An Hải Bắc", level: "warning",   description: "Túi rác ứ đọng" },
  { id: 8006, lat: 16.0728, lng: 108.2302, wardName: "Phường An Hải Bắc", level: "normal",    description: "Ổn định" },

  // ── Phường Mân Thái ──────────────────────────────────────────────────────
  { id: 9001, lat: 16.0858, lng: 108.2318, wardName: "Phường Mân Thái", level: "normal",      description: "Bình thường" },
  { id: 9002, lat: 16.0878, lng: 108.2355, wardName: "Phường Mân Thái", level: "warning",     description: "Cần theo dõi" },
  { id: 9003, lat: 16.0848, lng: 108.2382, wardName: "Phường Mân Thái", level: "normal",      description: "Đạt tiêu chuẩn" },
  { id: 9004, lat: 16.0908, lng: 108.2308, wardName: "Phường Mân Thái", level: "warning",     description: "Ô nhiễm nhẹ" },
  { id: 9005, lat: 16.0925, lng: 108.2342, wardName: "Phường Mân Thái", level: "normal",      description: "Sạch" },

  // ── Phường Phước Mỹ ──────────────────────────────────────────────────────
  { id: 10001, lat: 16.0788, lng: 108.2368, wardName: "Phường Phước Mỹ", level: "normal",    description: "Khu vực xanh" },
  { id: 10002, lat: 16.0808, lng: 108.2398, wardName: "Phường Phước Mỹ", level: "normal",    description: "Tốt" },
  { id: 10003, lat: 16.0778, lng: 108.2428, wardName: "Phường Phước Mỹ", level: "warning",   description: "Rác dọc đường Trường Sa" },
  { id: 10004, lat: 16.0845, lng: 108.2408, wardName: "Phường Phước Mỹ", level: "normal",    description: "Bình thường" },

  // ── Phường Xuân Hà ───────────────────────────────────────────────────────
  { id: 11001, lat: 16.0598, lng: 108.1975, wardName: "Phường Xuân Hà", level: "warning",    description: "Nước thải khu chợ" },
  { id: 11002, lat: 16.0612, lng: 108.2005, wardName: "Phường Xuân Hà", level: "normal",     description: "Đường sạch" },
  { id: 11003, lat: 16.0578, lng: 108.2035, wardName: "Phường Xuân Hà", level: "warning",    description: "Rác hữu cơ phân hủy" },
  { id: 11004, lat: 16.0628, lng: 108.1958, wardName: "Phường Xuân Hà", level: "critical",   description: "Bãi rác trái phép" },
  { id: 11005, lat: 16.0618, lng: 108.2048, wardName: "Phường Xuân Hà", level: "warning",    description: "Khói bụi xe cơ giới" },

  // ── Phường Tân Chính ─────────────────────────────────────────────────────
  { id: 12001, lat: 16.0495, lng: 108.2008, wardName: "Phường Tân Chính", level: "normal",   description: "Xanh sạch đẹp" },
  { id: 12002, lat: 16.0510, lng: 108.2042, wardName: "Phường Tân Chính", level: "normal",   description: "Bình thường" },
  { id: 12003, lat: 16.0475, lng: 108.2072, wardName: "Phường Tân Chính", level: "warning",  description: "Mương thoát nước ô nhiễm" },
  { id: 12004, lat: 16.0525, lng: 108.2018, wardName: "Phường Tân Chính", level: "normal",   description: "Đẹp" },
];
