"use client";

import {
  Dialog, DialogContent, DialogHeader,
  DialogTitle, DialogDescription, DialogFooter,
} from "@/components/ui/dialog";
import { Button }   from "@/components/ui/button";
import { Input }    from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Label }    from "@/components/ui/label";
import { CampaignRegion } from "@/types/waste-report";
import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { apiPost } from "@/lib/auth";
import { AlertCircle, CheckCircle2, Loader2, MapPin } from "lucide-react";
import { createBlog } from "@/services/blog.service";
import { reverseGeocode } from "@/lib/geocode";

interface CampaignModalProps {
  isOpen: boolean;
  onClose: () => void;
  region: CampaignRegion | null;
  onSuccess?: () => void;          // callback để reload list sau khi tạo xong
}

export function CampaignModal({ isOpen, onClose, region, onSuccess }: CampaignModalProps) {
  const router = useRouter();

  const [name,        setName]        = useState("");
  const [description, setDescription] = useState("");
  const [startDate,   setStartDate]   = useState("");
  const [endDate,     setEndDate]     = useState("");
  const [radius,      setRadius]      = useState(500);

  const [loading, setLoading] = useState(false);
  const [error,   setError]   = useState<string | null>(null);
  const [success, setSuccess] = useState(false);
  const [successCampaignId, setSuccessCampaignId] = useState<string>("");

  // Address from reverse geocoding
  const [address, setAddress] = useState<string | null>(null);
  const [addressLoading, setAddressLoading] = useState(false);

  // Reverse geocode the center coordinates to display address
  useEffect(() => {
    if (!region?.center) return;

    setAddressLoading(true);
    setAddress(null);

    reverseGeocode(region.center.lat, region.center.lng)
      .then(addr => {
        setAddress(addr);
      })
      .catch(() => {
        setAddress(null);
      })
      .finally(() => {
        setAddressLoading(false);
      });
  }, [region?.center?.lat, region?.center?.lng]);

  if (!isOpen || !region) return null;

  const reportIds = region.reports.map((r) => r.id);

  const resetForm = () => {
    setName(""); setDescription(""); setStartDate(""); setEndDate("");
    setRadius(500); setError(null); setSuccess(false); setAddress(null); setSuccessCampaignId("");
  };

  const handleClose = () => {
    resetForm();
    onClose();
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    let newCampaignId: string = "";

    try {
      const body = {
        name,
        description,
        startDate,
        endDate,
        lat:       region.center.lat,
        lng:       region.center.lng,
        radius,
        reportIds,
      };

      const campaignData = await apiPost("/campaigns", body);
      newCampaignId = campaignData?.id ?? campaignData?.data?.id ?? "";

      // Auto-create a community blog post for this campaign (fire-and-forget)
      const startFormatted = startDate ? new Date(startDate).toLocaleDateString("vi-VN") : "?";
      const endFormatted   = endDate   ? new Date(endDate).toLocaleDateString("vi-VN")   : "?";
      const blogContent = `<p><strong>${name}</strong></p>
<p>${description}</p>
<p>Thời gian: <strong>${startFormatted}</strong> – <strong>${endFormatted}</strong></p>
<p>Khu vực: ${region.name}</p>
${newCampaignId ? `<p><a href="/dashboard/campaign-management?id=${newCampaignId}" target="_blank" rel="noopener">Xem chiến dịch: ${name}</a></p>` : ""}`;

      createBlog({
        title:   `[Chiến dịch] ${name}`,
        content: blogContent,
        tags:    ["chiến dịch", "tình nguyện"],
      }).catch(() => { /* silent – campaign already created */ });

      setSuccess(true);
      setSuccessCampaignId(newCampaignId);
      onSuccess?.();

      // Chuyển ngay lập tức sang trang campaign chi tiết
      if (newCampaignId) {
        window.location.href = `/dashboard/campaign-management?id=${newCampaignId}`;
      }
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : "Có lỗi xảy ra, vui lòng thử lại.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={handleClose}>
      <DialogContent className="sm:max-w-[600px]">
        <DialogHeader>
          <DialogTitle>Tạo chiến dịch dọn rác</DialogTitle>
          <DialogDescription>
            Khu vực: <span className="font-semibold text-gray-700">{region.name}</span>
          </DialogDescription>
        </DialogHeader>

        {/* ── Success state ── */}
        {success ? (
          <div className="flex flex-col items-center gap-3 py-8">
            <CheckCircle2 className="w-12 h-12 text-emerald-500" />
            <p className="text-base font-semibold text-emerald-700">Tạo chiến dịch thành công!</p>
            <p className="text-sm text-gray-400">Đang chuyển đến trang chi tiết...</p>
          </div>
        ) : (
          <form onSubmit={handleSubmit} className="grid gap-4 py-4">

            {/* Error banner */}
            {error && (
              <div className="bg-red-50 border border-red-200 text-red-600 rounded-lg p-3 flex items-start gap-2 text-sm">
                <AlertCircle className="w-4 h-4 flex-shrink-0 mt-0.5" />
                <span>{error}</span>
              </div>
            )}

            {/* Tên chiến dịch */}
            <div className="grid gap-2">
              <Label htmlFor="camp-name">Tên chiến dịch <span className="text-red-500">*</span></Label>
              <Input
                id="camp-name"
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="VD: Chủ nhật xanh khu vực A..."
                required
                disabled={loading}
              />
            </div>

            {/* Mô tả */}
            <div className="grid gap-2">
              <Label htmlFor="camp-desc">Mô tả <span className="text-red-500">*</span></Label>
              <Textarea
                id="camp-desc"
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                placeholder="Mô tả chi tiết mục tiêu chiến dịch..."
                rows={3}
                required
                disabled={loading}
              />
            </div>

            {/* Ngày bắt đầu / kết thúc */}
            <div className="grid grid-cols-2 gap-4">
              <div className="grid gap-2">
                <Label htmlFor="camp-start">Ngày bắt đầu <span className="text-red-500">*</span></Label>
                <Input
                  id="camp-start"
                  type="date"
                  value={startDate}
                  onChange={(e) => setStartDate(e.target.value)}
                  required
                  disabled={loading}
                />
              </div>
              <div className="grid gap-2">
                <Label htmlFor="camp-end">Ngày kết thúc <span className="text-red-500">*</span></Label>
                <Input
                  id="camp-end"
                  type="date"
                  value={endDate}
                  onChange={(e) => setEndDate(e.target.value)}
                  required
                  disabled={loading}
                />
              </div>
            </div>

            {/* Nơi báo cáo - reversed geocoded address */}
            <div className="grid gap-2">
              <Label>Nơi báo cáo</Label>
              <div className="flex items-center gap-2 bg-emerald-50 border border-emerald-200 rounded-lg p-3 min-h-[44px]">
                <MapPin className="w-4 h-4 text-emerald-600 flex-shrink-0" />
                {addressLoading ? (
                  <span className="text-sm text-gray-400 italic">Đang lấy địa chỉ...</span>
                ) : address ? (
                  <span className="text-sm text-gray-700">{address}</span>
                ) : (
                  <span className="text-xs text-gray-400 font-mono">
                    {region.center.lat.toFixed(6)}, {region.center.lng.toFixed(6)}
                  </span>
                )}
              </div>
            </div>

            
            <DialogFooter className="mt-2">
              <Button type="button" variant="outline" onClick={handleClose} disabled={loading}>
                Hủy
              </Button>
              <Button type="submit" disabled={loading} className="min-w-[130px]">
                {loading ? (
                  <span className="flex items-center gap-2">
                    <Loader2 className="w-4 h-4 animate-spin" />
                    Đang tạo...
                  </span>
                ) : "Tạo chiến dịch"}
              </Button>
            </DialogFooter>
          </form>
        )}
      </DialogContent>
    </Dialog>
  );
}
