package com.vtea.service;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;
import com.vtea.dto.BillDTO;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Service chuyển đổi hóa đơn thành file PDF.
 * Nhiệm vụ: Lấy nội dung text đã được dàn trang từ BillReceiptFormatter và in nó ra file PDF vật lý.
 */
public class BillPdfService {

    // Gọi đến class Formatter để lấy khung text của hóa đơn
    private final BillReceiptFormatter receiptFormatter = new BillReceiptFormatter();

    // ==================== 1. HÀM XUẤT PDF CHÍNH ====================

    /**
     * Đọc dữ liệu hóa đơn (BillDTO) và xuất ra file PDF tại vị trí outputFile.
     */
    public void exportBillToPdf(BillDTO bill, File outputFile) {
        if (bill == null) {
            throw new IllegalArgumentException("Dữ liệu hóa đơn không được để trống.");
        }

        if (outputFile == null) {
            throw new IllegalArgumentException("File xuất PDF không được để trống.");
        }

        // 1. Nhờ Formatter dàn trang hóa đơn thành một khối văn bản Text thuần
        String receiptContent = receiptFormatter.format(bill);

        // 2. Khởi tạo tài liệu PDF (Sử dụng khổ giấy A5, lề 28)
        Document document = new Document(PageSize.A5, 28, 28, 28, 28);

        try {
            // 3. Chuẩn bị luồng ghi file
            PdfWriter.getInstance(document, new FileOutputStream(outputFile));
            document.open();

            // 4. Lấy font chữ tiếng Việt (để không bị lỗi dấu)
            Font font = createVietnameseFont();

            // 5. Đổ khối văn bản hóa đơn vào file PDF
            Paragraph paragraph = new Paragraph(receiptContent, font);
            paragraph.setLeading(15f); // Chỉnh khoảng cách dòng cho dễ đọc

            document.add(paragraph);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi xuất PDF hóa đơn", e);
        } finally {
            // 6. Đóng tài liệu để giải phóng bộ nhớ và hoàn tất việc ghi file
            if (document.isOpen()) {
                document.close();
            }
        }
    }

    // ==================== 2. HÀM CẤU HÌNH FONT CHỮ ====================

    /**
     * Tạo font chữ hỗ trợ tiếng Việt.
     * Sử dụng Font Monospace (các ký tự có độ rộng bằng nhau) để hóa đơn giữ nguyên cột ngay ngắn.
     */
    private Font createVietnameseFont() {
        try {
            String fontPath = findFontPath();

            if (fontPath != null) {
                // Nếu tìm thấy font trên máy, nạp nó vào PDF
                BaseFont baseFont = BaseFont.createFont(
                        fontPath,
                        BaseFont.IDENTITY_H,
                        BaseFont.EMBEDDED
                );

                return new Font(baseFont, 10);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Dự phòng: Trả về font Courier mặc định nếu không tìm thấy font nào trên máy
        return new Font(Font.COURIER, 10);
    }

    /**
     * Tự động quét và tìm file Font hỗ trợ tiếng Việt có sẵn trên hệ điều hành (Windows/Linux).
     * Cách này giúp code chạy được mà không cần phải nhét file Font nặng nề vào trong Source Code.
     */
    private String findFontPath() {
        String[] possiblePaths = {
                "/usr/share/fonts/truetype/dejavu/DejaVuSansMono.ttf", // Linux
                "/usr/share/fonts/TTF/DejaVuSansMono.ttf",             // Linux alternate
                "C:/Windows/Fonts/consola.ttf",                        // Windows Consolas (Monospace)
                "C:/Windows/Fonts/arial.ttf"                           // Windows Arial (Fallback)
        };

        for (String path : possiblePaths) {
            if (Files.exists(Path.of(path))) {
                return path;
            }
        }

        return null;
    }
}