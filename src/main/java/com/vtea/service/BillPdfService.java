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
 * Service xuất hóa đơn ra PDF.
 * Service này không xử lý UI, chỉ nhận BillDTO và file đích để ghi PDF.
 */
public class BillPdfService {

    private final BillReceiptFormatter receiptFormatter = new BillReceiptFormatter();

    /**
     * Xuất bill ra file PDF.
     */
    public void exportBillToPdf(BillDTO bill, File outputFile) {
        if (bill == null) {
            throw new IllegalArgumentException("Dữ liệu hóa đơn không được để trống.");
        }

        if (outputFile == null) {
            throw new IllegalArgumentException("File xuất PDF không được để trống.");
        }

        String receiptContent = receiptFormatter.format(bill);

        Document document = new Document(PageSize.A5, 28, 28, 28, 28);

        try {
            PdfWriter.getInstance(document, new FileOutputStream(outputFile));
            document.open();

            Font font = createVietnameseFont();

            Paragraph paragraph = new Paragraph(receiptContent, font);
            paragraph.setLeading(15f);

            document.add(paragraph);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi xuất PDF hóa đơn", e);
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }
    }

    /**
     * Tạo font hỗ trợ tiếng Việt cho PDF.
     * Ưu tiên DejaVu Sans Mono vì hợp với bill dạng text.
     */
    private Font createVietnameseFont() {
        try {
            String fontPath = findFontPath();

            if (fontPath != null) {
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

        return new Font(Font.COURIER, 10);
    }

    /**
     * Tìm font mono hỗ trợ tiếng Việt trên máy.
     * Không copy font vào project, chỉ dùng font có sẵn trên hệ điều hành.
     */
    private String findFontPath() {
        String[] possiblePaths = {
                "/usr/share/fonts/truetype/dejavu/DejaVuSansMono.ttf",
                "/usr/share/fonts/TTF/DejaVuSansMono.ttf",
                "C:/Windows/Fonts/consola.ttf",
                "C:/Windows/Fonts/arial.ttf"
        };

        for (String path : possiblePaths) {
            if (Files.exists(Path.of(path))) {
                return path;
            }
        }

        return null;
    }
}