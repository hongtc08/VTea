package com.vtea.service;

import com.vtea.dao.VoucherDAO;
import com.vtea.model.Voucher;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

public class VoucherService {

    private final VoucherDAO voucherDAO = new VoucherDAO();

    // DANH CHO THU NGAN

    /**
     * Kiem tra ma va tinh toan so tien duoc giam.
     * Chi tinh toan hien thi, khong tru luot su dung
     */
    public BigDecimal calculateDiscount(String code, BigDecimal orderTotal) throws Exception {
        if (code == null || code.trim().isEmpty()) {
            throw new Exception("Vui long nhap ma giam gia!");
        }

        // 1. Kiem tra ton tai
        Voucher voucher = voucherDAO.getVoucherByCode(code.trim());
        if (voucher == null) {
            throw new Exception("Ma giam gia khong ton tai!");
        }

        // 2. Kiem tra trang thai hoat dong
        if (!voucher.isActive()) {
            throw new Exception("Ma giam gia nay da bi vo hieu hoa!");
        }

        // 3. Kiem tra thoi han su dung
        LocalDateTime now = LocalDateTime.now();
        if (voucher.getStartDate() != null && now.isBefore(voucher.getStartDate())) {
            throw new Exception("Ma giam gia chua den thoi gian su dung!");
        }
        if (voucher.getEndDate() != null && now.isAfter(voucher.getEndDate())) {
            throw new Exception("Ma giam gia da het han su dung!");
        }

        // 4. Kiem tra so luot su dung
        if (voucher.getUsedCount() >= voucher.getUsageLimit()) {
            throw new Exception("Rat tiec! Ma giam gia da het luot su dung.");
        }

        // 5. Kiem tra dieu kien don hang toi thieu
        if (voucher.getMinOrderValue() != null && orderTotal.compareTo(voucher.getMinOrderValue()) < 0) {
            throw new Exception("Don hang phai tu " + voucher.getMinOrderValue() + " de ap dung ma nay.");
        }

        // 6. TINH TOAN TIEN GIAM
        BigDecimal discountAmount = BigDecimal.ZERO;

        if ("FIXED".equalsIgnoreCase(voucher.getDiscountType())) {
            discountAmount = voucher.getDiscountValue();
        } else if ("PERCENTAGE".equalsIgnoreCase(voucher.getDiscountType())) {
            BigDecimal percent = voucher.getDiscountValue().divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            discountAmount = orderTotal.multiply(percent);

            // Kiem tra gioi han giam toi da
            if (voucher.getMaxDiscountAmount() != null && voucher.getMaxDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
                if (discountAmount.compareTo(voucher.getMaxDiscountAmount()) > 0) {
                    discountAmount = voucher.getMaxDiscountAmount();
                }
            }
        } else {
            throw new Exception("Loai giam gia khong hop le trong he thong.");
        }

        // Dam bao khong giam am tien
        if (discountAmount.compareTo(orderTotal) > 0) {
            discountAmount = orderTotal;
        }

        return discountAmount;
    }

    /**
     * Tạo mã giảm giá chào mừng 20% cho khách hàng mới đăng ký.
     * Mã được lưu trực tiếp vào Database và có hiệu lực trong vòng 24 giờ.
     */
    public String createWelcomeVoucher(String phoneNumber) throws Exception {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new Exception("Số điện thoại không hợp lệ.");
        }

        String code = "NEW_" + phoneNumber.trim();

        // Kiểm tra xem mã này đã từng được tạo chưa
        Voucher existing = voucherDAO.getVoucherByCode(code);
        if (existing != null) {
            return code;
        }

        Voucher welcomeVoucher = new Voucher();
        welcomeVoucher.setCode(code);
        welcomeVoucher.setDiscountType("PERCENTAGE");
        welcomeVoucher.setDiscountValue(new BigDecimal("20"));
        welcomeVoucher.setMinOrderValue(BigDecimal.ZERO);
        // Giới hạn giảm tối đa 50k để tránh rủi ro
        welcomeVoucher.setMaxDiscountAmount(new BigDecimal("50000"));
        
        LocalDateTime now = LocalDateTime.now();
        welcomeVoucher.setStartDate(now);
        // Tồn tại trong vòng 24 giờ
        welcomeVoucher.setEndDate(now.plusHours(24));
        
        welcomeVoucher.setUsageLimit(1);
        welcomeVoucher.setUsedCount(0);
        welcomeVoucher.setActive(true);

        boolean success = voucherDAO.insertVoucher(welcomeVoucher);
        if (!success) {
            throw new Exception("Không thể tạo voucher chào mừng.");
        }

        return code;
    }

    // DANH CHO QUAN LY

    /**
     * Lay toan bo danh sach Voucher cho man hinh Admin
     */
    public List<Voucher> getAllVouchers() {
        return voucherDAO.getAllVouchers();
    }

    /**
     * Tao mot ma giam gia moi
     */
    public boolean createMarketingVoucher(Voucher voucher) throws Exception {
        validateVoucherData(voucher);

        // Kiem tra trung ma code
        if (voucherDAO.getVoucherByCode(voucher.getCode()) != null) {
            throw new Exception("Ma code nay da ton tai. Vui long chon ma khac.");
        }

        // Thiet lap mac dinh khi tao moi
        voucher.setUsedCount(0);
        voucher.setActive(true);

        return voucherDAO.insertVoucher(voucher);
    }

    /**
     * Cap nhat thong tin Voucher
     */
    public boolean updateVoucherInfo(Voucher voucher) throws Exception {
        if (voucher.getVoucherId() <= 0) {
            throw new IllegalArgumentException("ID Voucher khong hop le de cap nhat.");
        }

        validateVoucherData(voucher);

        return voucherDAO.updateVoucher(voucher);
    }

    /**
     * Ngung kich hoat mot ma giam gia
     */
    public boolean disableVoucher(int voucherId) throws Exception {
        if (voucherId <= 0) {
            throw new IllegalArgumentException("ID Voucher khong hop le.");
        }
        return voucherDAO.deactivateVoucher(voucherId);
    }


    /**
     * Kiem tra tinh hop le cua du lieu truoc khi day xuong Database
     */
    private void validateVoucherData(Voucher voucher) throws Exception {
        if (voucher == null) {
            throw new Exception("Du lieu Voucher khong duoc de trong.");
        }
        if (voucher.getCode() == null || voucher.getCode().trim().isEmpty()) {
            throw new Exception("Ma Voucher khong duoc de trong.");
        }
        if (voucher.getDiscountType() == null ||
                (!voucher.getDiscountType().equalsIgnoreCase("FIXED") && !voucher.getDiscountType().equalsIgnoreCase("PERCENTAGE"))) {
            throw new Exception("Loai giam gia phai la FIXED hoac PERCENTAGE.");
        }
        if (voucher.getDiscountValue() == null || voucher.getDiscountValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new Exception("Muc giam gia phai lon hon 0.");
        }
        if (voucher.getUsageLimit() <= 0) {
            throw new Exception("So luong gioi han su dung phai lon hon 0.");
        }
        if (voucher.getStartDate() != null && voucher.getEndDate() != null) {
            if (voucher.getEndDate().isBefore(voucher.getStartDate())) {
                throw new Exception("Ngay het han khong duoc nho hon ngay bat dau.");
            }
        }
    }
}