import type { Collector, WasteReport } from "@/types/monitoring";
// ---------------------------------------------------------------------------
// COLLECTORS — Đội thu gom rác thành phố Đà Nẵng
// ---------------------------------------------------------------------------
export const COLLECTORS: Collector[] = [
    { id: 1, name: "Nguyễn Thanh Hải", phone: "0905 123 456", zones: [1, 2], vehicleId: "43C-001.23", activeReports: 5 },
    { id: 2, name: "Trần Văn Mạnh", phone: "0905 234 567", zones: [3, 4], vehicleId: "43C-002.45", activeReports: 3 },
    { id: 3, name: "Lê Thị Hoa", phone: "0905 345 678", zones: [5, 11], vehicleId: "43C-003.67", activeReports: 4 },
    { id: 4, name: "Phạm Quốc Bình", phone: "0905 456 789", zones: [8, 9], vehicleId: "43C-004.89", activeReports: 6 },
    { id: 5, name: "Hoàng Thị Liên", phone: "0905 567 890", zones: [6, 7], vehicleId: "43C-005.12", activeReports: 2 },
    { id: 6, name: "Đỗ Minh Tuấn", phone: "0905 678 901", zones: [10, 12], vehicleId: "43C-006.34", activeReports: 1 },
];

const D = "2026-03-20";
const h = (hh: string) => `${D}T${hh}:00+07:00`;

// ---------------------------------------------------------------------------
// WASTE_REPORTS — wasteKg là kg ước tính tại điểm báo cáo (thực tế: 3–15 kg)
//   Bình thường:   3–7 kg/báo cáo
//   Tồn đọng:      8–12 kg/báo cáo
//   Nghiêm trọng: 13–15+ kg/báo cáo
// ---------------------------------------------------------------------------
export const WASTE_REPORTS: WasteReport[] = [
    // ── Phường Hải Châu 1 (ward 1) ─────────────────────────────────────────
    {
        id: "RPT-001", householdId: 107, householdName: "Hộ Bùi Văn Giang",
        wardId: 1, wardName: "Phường Hải Châu 1",
        lat: 16.0690, lng: 108.2065, wasteKg: 14.5, wasteType: "mixed",
        description: "Rác tràn ra vỉa hè, chưa được thu gom từ hôm qua",
        status: "pending", reportedAt: h("07:12"),
        assignedTo: null, collectorId: null, resolvedAt: null,
        imageUrl: "https://images.unsplash.com/photo-1522708323590-d24dbb6b0267",
        items: [
            { name: "Plastic film", quantity: 9, area: 147757 },
            { name: "Single-use carrier bag", quantity: 2, area: 18706 },
        ],
        total_objects: 11,
        pollution: {
            CO2: 0.6931471805569416,
            microplastic: 0.6931471805569416,
            dioxin: 0.5365526341607301,
            non_biodegradable: 0.6931471805569416,
            CH4: 0,
            "PM2.5": 0,
            NOx: 0,
            SO2: 0,
            Pb: 0,
            Hg: 0,
            Cd: 0,
            nitrate: 0,
            chemical_residue: 0,
            toxic_chemicals: 0,
            styrene: 0,
        },
    },
    {
        id: "RPT-002", householdId: 101, householdName: "Hộ Nguyễn Văn An",
        wardId: 1, wardName: "Phường Hải Châu 1",
        lat: 16.0680, lng: 108.2095, wasteKg: 10.8, wasteType: "plastic",
        description: "Nhiều túi nylon vứt bừa bãi trước hẻm",
        status: "assigned", reportedAt: h("06:30"),
        assignedTo: "Nguyễn Thanh Hải", collectorId: 1, resolvedAt: null,
    },
    {
        id: "RPT-003", householdId: 109, householdName: "Hộ Ngô Văn Inh",
        wardId: 1, wardName: "Phường Hải Châu 1",
        lat: 16.0716, lng: 108.2155, wasteKg: 9.2, wasteType: "organic",
        description: "Rác hữu cơ bốc mùi cần xử lý khẩn",
        status: "done", reportedAt: h("05:45"),
        assignedTo: "Nguyễn Thanh Hải", collectorId: 1, resolvedAt: h("08:20"),
    },
    {
        id: "RPT-004", householdId: 105, householdName: "Hộ Hoàng Văn Em",
        wardId: 1, wardName: "Phường Hải Châu 1",
        lat: 16.0712, lng: 108.2115, wasteKg: 11.5, wasteType: "mixed",
        description: "Điểm tập kết rác không đúng quy định",
        status: "done", reportedAt: h("06:00"),
        assignedTo: "Nguyễn Thanh Hải", collectorId: 1, resolvedAt: h("09:10"),
    },

    // ── Phường Hải Châu 2 (ward 2) ─────────────────────────────────────────
    {
        id: "RPT-005", householdId: 204, householdName: "Hộ Phạm Văn Phát",
        wardId: 2, wardName: "Phường Hải Châu 2",
        lat: 16.0562, lng: 108.2175, wasteKg: 13.0, wasteType: "mixed",
        description: "Rác chất thành đống 2 ngày chưa dọn",
        status: "pending", reportedAt: h("07:30"),
        assignedTo: null, collectorId: null, resolvedAt: null,
    },
    {
        id: "RPT-006", householdId: 210, householdName: "Hộ Đinh Văn Vĩnh",
        wardId: 2, wardName: "Phường Hải Châu 2",
        lat: 16.0558, lng: 108.2142, wasteKg: 11.8, wasteType: "plastic",
        description: "Vỏ chai nhựa, hộp xốp vứt trước cửa nhà hàng xóm",
        status: "pending", reportedAt: h("08:05"),
        assignedTo: null, collectorId: null, resolvedAt: null,
    },
    {
        id: "RPT-007", householdId: 202, householdName: "Hộ Trần Văn Nam",
        wardId: 2, wardName: "Phường Hải Châu 2",
        lat: 16.0605, lng: 108.2125, wasteKg: 8.8, wasteType: "organic",
        description: "Rác thực phẩm cũ, cần thu gom ngay",
        status: "assigned", reportedAt: h("06:15"),
        assignedTo: "Nguyễn Thanh Hải", collectorId: 1, resolvedAt: null,
    },
    {
        id: "RPT-008", householdId: 206, householdName: "Hộ Đỗ Văn Rộng",
        wardId: 2, wardName: "Phường Hải Châu 2",
        lat: 16.0610, lng: 108.2075, wasteKg: 10.2, wasteType: "mixed",
        description: "Điểm tập kết rác tự phát",
        status: "done", reportedAt: h("05:30"),
        assignedTo: "Nguyễn Thanh Hải", collectorId: 1, resolvedAt: h("07:50"),
    },

    // ── Phường Thạch Thang (ward 3) ────────────────────────────────────────
    {
        id: "RPT-009", householdId: 306, householdName: "Hộ Đỗ Thị Cẩm",
        wardId: 3, wardName: "Phường Thạch Thang",
        lat: 16.0725, lng: 108.2202, wasteKg: 7.5, wasteType: "mixed",
        description: "Thùng rác công cộng đầy, tràn ra lòng đường",
        status: "pending", reportedAt: h("07:55"),
        assignedTo: null, collectorId: null, resolvedAt: null,
    },
    {
        id: "RPT-010", householdId: 309, householdName: "Hộ Ngô Văn Gấm",
        wardId: 3, wardName: "Phường Thạch Thang",
        lat: 16.0795, lng: 108.2135, wasteKg: 8.0, wasteType: "plastic",
        description: "Nhiều rác nhựa vứt quanh gốc cây",
        status: "assigned", reportedAt: h("07:10"),
        assignedTo: "Trần Văn Mạnh", collectorId: 2, resolvedAt: null,
    },
    {
        id: "RPT-011", householdId: 304, householdName: "Hộ Phạm Thị Ánh",
        wardId: 3, wardName: "Phường Thạch Thang",
        lat: 16.0775, lng: 108.2118, wasteKg: 6.5, wasteType: "organic",
        description: "Rác hữu cơ từ chợ đầu mối bị vứt bỏ",
        status: "done", reportedAt: h("05:00"),
        assignedTo: "Trần Văn Mạnh", collectorId: 2, resolvedAt: h("08:30"),
    },
    {
        id: "RPT-042", householdId: 302, householdName: "Hộ Trần Thị Yến",
        wardId: 3, wardName: "Phường Thạch Thang",
        lat: 16.0762, lng: 108.2148, wasteKg: 6.0, wasteType: "organic",
        description: "Rác hữu cơ từ cơ sở sản xuất bún tươi",
        status: "done", reportedAt: h("04:50"),
        assignedTo: "Trần Văn Mạnh", collectorId: 2, resolvedAt: h("07:00"),
    },

    // ── Phường Thanh Bình (ward 4) ─────────────────────────────────────────
    {
        id: "RPT-012", householdId: 404, householdName: "Hộ Phạm Văn Khoa",
        wardId: 4, wardName: "Phường Thanh Bình",
        lat: 16.0858, lng: 108.2135, wasteKg: 9.5, wasteType: "mixed",
        description: "Rác xây dựng vứt ra vỉa hè tái phạm lần 2",
        status: "pending", reportedAt: h("08:20"),
        assignedTo: null, collectorId: null, resolvedAt: null,
    },
    {
        id: "RPT-013", householdId: 408, householdName: "Hộ Vũ Văn Oanh",
        wardId: 4, wardName: "Phường Thanh Bình",
        lat: 16.0808, lng: 108.2075, wasteKg: 7.0, wasteType: "plastic",
        description: "Túi rác chưa đúng giờ quy định",
        status: "assigned", reportedAt: h("06:50"),
        assignedTo: "Trần Văn Mạnh", collectorId: 2, resolvedAt: null,
    },
    {
        id: "RPT-014", householdId: 402, householdName: "Hộ Trần Văn Hùng",
        wardId: 4, wardName: "Phường Thanh Bình",
        lat: 16.0845, lng: 108.2115, wasteKg: 5.8, wasteType: "organic",
        description: "Rác hữu cơ từ nhà hàng tại khu vực ẩm thực",
        status: "done", reportedAt: h("05:20"),
        assignedTo: "Trần Văn Mạnh", collectorId: 2, resolvedAt: h("08:00"),
    },

    // ── Phường Thuận Phước (ward 5) ────────────────────────────────────────
    {
        id: "RPT-015", householdId: 504, householdName: "Hộ Phạm Thị Thuận",
        wardId: 5, wardName: "Phường Thuận Phước",
        lat: 16.0650, lng: 108.2092, wasteKg: 7.2, wasteType: "mixed",
        description: "Rác ven cảng chưa được thu gom",
        status: "pending", reportedAt: h("08:45"),
        assignedTo: null, collectorId: null, resolvedAt: null,
    },
    {
        id: "RPT-016", householdId: 507, householdName: "Hộ Bùi Văn Vượng",
        wardId: 5, wardName: "Phường Thuận Phước",
        lat: 16.0730, lng: 108.2008, wasteKg: 6.8, wasteType: "organic",
        description: "Rác cá từ khu vực chợ cá nhỏ gần cảng",
        status: "assigned", reportedAt: h("07:00"),
        assignedTo: "Lê Thị Hoa", collectorId: 3, resolvedAt: null,
    },
    {
        id: "RPT-017", householdId: 506, householdName: "Hộ Đỗ Thị Uyên",
        wardId: 5, wardName: "Phường Thuận Phước",
        lat: 16.0642, lng: 108.2062, wasteKg: 5.0, wasteType: "plastic",
        description: "Túi nilon vứt gần khu dân cư ven đường",
        status: "done", reportedAt: h("05:15"),
        assignedTo: "Lê Thị Hoa", collectorId: 3, resolvedAt: h("07:40"),
    },

    // ── Phường Mỹ An (ward 6) ──────────────────────────────────────────────
    {
        id: "RPT-018", householdId: 605, householdName: "Hộ Hoàng Thị Trà",
        wardId: 6, wardName: "Phường Mỹ An",
        lat: 16.0420, lng: 108.2252, wasteKg: 4.5, wasteType: "mixed",
        description: "Điểm rác nhỏ tự phát trong ngõ dân cư",
        status: "pending", reportedAt: h("09:00"),
        assignedTo: null, collectorId: null, resolvedAt: null,
    },
    {
        id: "RPT-019", householdId: 604, householdName: "Hộ Phạm Văn Toàn",
        wardId: 6, wardName: "Phường Mỹ An",
        lat: 16.0512, lng: 108.2145, wasteKg: 3.2, wasteType: "organic",
        description: "Rác thức ăn thừa từ hộ gia đình",
        status: "done", reportedAt: h("06:00"),
        assignedTo: "Hoàng Thị Liên", collectorId: 5, resolvedAt: h("08:45"),
    },

    // ── Phường Khuê Mỹ (ward 7) ────────────────────────────────────────────
    {
        id: "RPT-020", householdId: 707, householdName: "Hộ Bùi Văn Đạt",
        wardId: 7, wardName: "Phường Khuê Mỹ",
        lat: 16.0482, lng: 108.2348, wasteKg: 5.5, wasteType: "mixed",
        description: "Rác từ khu vực bãi xe chưa được dọn",
        status: "assigned", reportedAt: h("07:30"),
        assignedTo: "Hoàng Thị Liên", collectorId: 5, resolvedAt: null,
    },
    {
        id: "RPT-021", householdId: 704, householdName: "Hộ Phạm Thị Ánh",
        wardId: 7, wardName: "Phường Khuê Mỹ",
        lat: 16.0572, lng: 108.2240, wasteKg: 3.8, wasteType: "plastic",
        description: "Vỏ chai nước nhựa vứt gần bãi đỗ xe",
        status: "done", reportedAt: h("06:10"),
        assignedTo: "Hoàng Thị Liên", collectorId: 5, resolvedAt: h("08:30"),
    },

    // ── Phường An Hải Bắc (ward 8) ─────────────────────────────────────────
    {
        id: "RPT-022", householdId: 804, householdName: "Hộ Phạm Thị Tiên",
        wardId: 8, wardName: "Phường An Hải Bắc",
        lat: 16.0768, lng: 108.2398, wasteKg: 12.0, wasteType: "mixed",
        description: "Rác chồng thành đống cao trước cổng khu dân cư",
        status: "pending", reportedAt: h("07:20"),
        assignedTo: null, collectorId: null, resolvedAt: null,
    },
    {
        id: "RPT-023", householdId: 801, householdName: "Hộ Nguyễn Văn Quang",
        wardId: 8, wardName: "Phường An Hải Bắc",
        lat: 16.0760, lng: 108.2312, wasteKg: 10.5, wasteType: "organic",
        description: "Rác thực phẩm từ hàng quán ẩm thực đêm",
        status: "pending", reportedAt: h("08:10"),
        assignedTo: null, collectorId: null, resolvedAt: null,
    },
    {
        id: "RPT-024", householdId: 808, householdName: "Hộ Vũ Thị Ý",
        wardId: 8, wardName: "Phường An Hải Bắc",
        lat: 16.0788, lng: 108.2382, wasteKg: 9.8, wasteType: "plastic",
        description: "Bao tải nylon to vứt gần cống thoát nước",
        status: "assigned", reportedAt: h("06:40"),
        assignedTo: "Phạm Quốc Bình", collectorId: 4, resolvedAt: null,
    },
    {
        id: "RPT-025", householdId: 806, householdName: "Hộ Đỗ Thị Vân",
        wardId: 8, wardName: "Phường An Hải Bắc",
        lat: 16.0728, lng: 108.2302, wasteKg: 9.2, wasteType: "mixed",
        description: "Rác tồn đọng, xe rác chỉ qua 1 lần/ngày",
        status: "assigned", reportedAt: h("07:00"),
        assignedTo: "Phạm Quốc Bình", collectorId: 4, resolvedAt: null,
    },
    {
        id: "RPT-026", householdId: 802, householdName: "Hộ Trần Thị Thúy",
        wardId: 8, wardName: "Phường An Hải Bắc",
        lat: 16.0778, lng: 108.2342, wasteKg: 8.5, wasteType: "organic",
        description: "Đã được thu dọn sau khi phản ánh",
        status: "done", reportedAt: h("05:00"),
        assignedTo: "Phạm Quốc Bình", collectorId: 4, resolvedAt: h("08:00"),
    },

    // ── Phường Mân Thái (ward 9) ────────────────────────────────────────────
    {
        id: "RPT-027", householdId: 906, householdName: "Hộ Đỗ Văn Tiến",
        wardId: 9, wardName: "Phường Mân Thái",
        lat: 16.0925, lng: 108.2342, wasteKg: 7.5, wasteType: "mixed",
        description: "Rác tồn đọng 2 ngày tại ngõ hẹp",
        status: "pending", reportedAt: h("08:00"),
        assignedTo: null, collectorId: null, resolvedAt: null,
    },
    {
        id: "RPT-028", householdId: 904, householdName: "Hộ Phạm Văn Đức",
        wardId: 9, wardName: "Phường Mân Thái",
        lat: 16.0908, lng: 108.2308, wasteKg: 6.2, wasteType: "organic",
        description: "Rác hữu cơ từ vườn cây gia đình",
        status: "assigned", reportedAt: h("06:30"),
        assignedTo: "Phạm Quốc Bình", collectorId: 4, resolvedAt: null,
    },
    {
        id: "RPT-029", householdId: 901, householdName: "Hộ Nguyễn Thị Ánh",
        wardId: 9, wardName: "Phường Mân Thái",
        lat: 16.0858, lng: 108.2318, wasteKg: 5.0, wasteType: "plastic",
        description: "Thu gom xong sau 2 giờ",
        status: "done", reportedAt: h("05:30"),
        assignedTo: "Phạm Quốc Bình", collectorId: 4, resolvedAt: h("07:35"),
    },

    // ── Phường Phước Mỹ (ward 10) ──────────────────────────────────────────
    {
        id: "RPT-030", householdId: 1005, householdName: "Hộ Hoàng Văn Nghĩa",
        wardId: 10, wardName: "Phường Phước Mỹ",
        lat: 16.0815, lng: 108.2352, wasteKg: 5.5, wasteType: "mixed",
        description: "Rác thải nhỏ tại điểm tập kết mới",
        status: "assigned", reportedAt: h("07:45"),
        assignedTo: "Đỗ Minh Tuấn", collectorId: 6, resolvedAt: null,
    },
    {
        id: "RPT-031", householdId: 1004, householdName: "Hộ Phạm Thị Ngọc",
        wardId: 10, wardName: "Phường Phước Mỹ",
        lat: 16.0845, lng: 108.2408, wasteKg: 4.2, wasteType: "plastic",
        description: "Vứt rác trước giờ thu gom quy định",
        status: "done", reportedAt: h("06:00"),
        assignedTo: "Đỗ Minh Tuấn", collectorId: 6, resolvedAt: h("08:10"),
    },

    // ── Phường Xuân Hà (ward 11) ────────────────────────────────────────────
    {
        id: "RPT-032", householdId: 1107, householdName: "Hộ Bùi Thị Xuân",
        wardId: 11, wardName: "Phường Xuân Hà",
        lat: 16.0618, lng: 108.2048, wasteKg: 10.5, wasteType: "mixed",
        description: "Điểm rác tự phát tái phát lần 3 trong tháng",
        status: "pending", reportedAt: h("07:50"),
        assignedTo: null, collectorId: null, resolvedAt: null,
    },
    {
        id: "RPT-033", householdId: 1104, householdName: "Hộ Phạm Văn Úc",
        wardId: 11, wardName: "Phường Xuân Hà",
        lat: 16.0628, lng: 108.1958, wasteKg: 8.8, wasteType: "organic",
        description: "Rác chợ sáng tràn ra vỉa hè",
        status: "pending", reportedAt: h("08:30"),
        assignedTo: null, collectorId: null, resolvedAt: null,
    },
    {
        id: "RPT-034", householdId: 1102, householdName: "Hộ Trần Văn Sen",
        wardId: 11, wardName: "Phường Xuân Hà",
        lat: 16.0612, lng: 108.2005, wasteKg: 7.5, wasteType: "plastic",
        description: "Túi rác thải nhựa từ cửa hàng tiện lợi",
        status: "assigned", reportedAt: h("06:20"),
        assignedTo: "Lê Thị Hoa", collectorId: 3, resolvedAt: null,
    },
    {
        id: "RPT-035", householdId: 1101, householdName: "Hộ Nguyễn Thị Rạng",
        wardId: 11, wardName: "Phường Xuân Hà",
        lat: 16.0598, lng: 108.1975, wasteKg: 6.0, wasteType: "organic",
        description: "Xử lý xong, cảm ơn đội thu gom",
        status: "done", reportedAt: h("05:00"),
        assignedTo: "Lê Thị Hoa", collectorId: 3, resolvedAt: h("07:30"),
    },

    // ── Phường Tân Chính (ward 12) ─────────────────────────────────────────
    {
        id: "RPT-036", householdId: 1206, householdName: "Hộ Đỗ Thị Hoàng",
        wardId: 12, wardName: "Phường Tân Chính",
        lat: 16.0502, lng: 108.2128, wasteKg: 4.8, wasteType: "mixed",
        description: "Rác vươn ra lòng ngõ gây cản trở",
        status: "pending", reportedAt: h("09:10"),
        assignedTo: null, collectorId: null, resolvedAt: null,
    },
    {
        id: "RPT-037", householdId: 1204, householdName: "Hộ Phạm Thị Đào",
        wardId: 12, wardName: "Phường Tân Chính",
        lat: 16.0525, lng: 108.2018, wasteKg: 3.5, wasteType: "plastic",
        description: "Vỏ bình nước nhựa bị vứt bỏ sai nơi",
        status: "done", reportedAt: h("06:05"),
        assignedTo: "Đỗ Minh Tuấn", collectorId: 6, resolvedAt: h("08:20"),
    },

    // ── Báo cáo nguy hại ────────────────────────────────────────────────────
    {
        id: "RPT-038", householdId: 103, householdName: "Hộ Lê Minh Cường",
        wardId: 1, wardName: "Phường Hải Châu 1",
        lat: 16.0700, lng: 108.2078, wasteKg: 3.5, wasteType: "hazardous",
        description: "Pin cũ, bình ắc-quy thải bỏ không đúng điểm thu gom",
        status: "pending", reportedAt: h("08:50"),
        assignedTo: null, collectorId: null, resolvedAt: null,
    },
    {
        id: "RPT-039", householdId: 803, householdName: "Hộ Lê Văn Sơn",
        wardId: 8, wardName: "Phường An Hải Bắc",
        lat: 16.0742, lng: 108.2368, wasteKg: 2.8, wasteType: "hazardous",
        description: "Sơn cũ, dung môi hóa học vứt gần rãnh thoát nước",
        status: "assigned", reportedAt: h("07:15"),
        assignedTo: "Phạm Quốc Bình", collectorId: 4, resolvedAt: null,
    },
    {
        id: "RPT-040", householdId: 110, householdName: "Hộ Đinh Thị Kiều",
        wardId: 1, wardName: "Phường Hải Châu 1",
        lat: 16.0648, lng: 108.2082, wasteKg: 2.2, wasteType: "hazardous",
        description: "Bóng đèn huỳnh quang cũ đã được thu gom đúng điểm",
        status: "done", reportedAt: h("05:10"),
        assignedTo: "Nguyễn Thanh Hải", collectorId: 1, resolvedAt: h("09:00"),
    },
    {
        id: "RPT-041", householdId: 810, householdName: "Hộ Đinh Thị Lan",
        wardId: 8, wardName: "Phường An Hải Bắc",
        lat: 16.0775, lng: 108.2355, wasteKg: 8.2, wasteType: "mixed",
        description: "Rác từ quán ăn đêm chưa dọn đến sáng",
        status: "pending", reportedAt: h("08:40"),
        assignedTo: null, collectorId: null, resolvedAt: null,
    },
];