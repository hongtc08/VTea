<div align="center">
  <img src="https://raw.githubusercontent.com/hongtc08/VTea/main/src/main/resources/com/vtea/image/logo.png" alt="VTea Logo" width="150" onError="this.style.display='none'"/>

  # 🧋 VTea - Hệ Thống Quản Lý Bán Hàng Trà Sữa (POS)

  **Giải pháp quản lý bán hàng tối ưu, nhanh chóng và chính xác cho các cửa hàng Trà sữa.**

  ![Build Status](https://img.shields.io/badge/Build-Passed-4CAF50?style=for-the-badge&logo=githubactions&logoColor=white)
  ![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=java&logoColor=white)
  ![JavaFX](https://img.shields.io/badge/JavaFX-17-007396?style=for-the-badge&logo=java&logoColor=white)
  ![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
  ![ArchUnit](https://img.shields.io/badge/Architecture-Verified-blue?style=for-the-badge)
</div>

---

## 📖 Giới thiệu

**VTea** là một hệ thống phần mềm Point of Sale (POS) mạnh mẽ được thiết kế chuyên biệt cho mô hình kinh doanh quán trà sữa. Ứng dụng tập trung vào trải nghiệm người dùng mượt mà, tính toán hóa đơn siêu tốc, độ chính xác cao và quản lý dữ liệu chặt chẽ theo tiêu chuẩn kiến trúc **Clean MVC**.

Dự án đặc biệt tích hợp hệ thống kiểm tra kiến trúc tự động (**ArchUnit**), đảm bảo mã nguồn luôn tuân thủ các tiêu chuẩn kỹ thuật phần mềm cao nhất trong suốt quá trình phát triển.

---

## ✨ Tính Năng Nổi Bật

### 🛒 1. Màn Hình Bán Hàng (POS)
- **Giỏ hàng thông minh:** Thêm món, tăng giảm số lượng, tự động tính tiền và thuế VAT theo thời gian thực.
- **Tùy chỉnh Topping:** Xử lý thêm/bớt các loại topping (Trân châu, Thạch...) cực kỳ linh hoạt và tính tiền tự động.
- **Tích điểm & Giảm giá:** Hỗ trợ tính năng Thành viên (Membership), tự động chiết khấu theo hạng và sử dụng điểm thưởng để giảm giá tối đa 50% bill.

### 💳 2. Thanh Toán Đa Dạng & Hiện Đại
- **Thanh toán tiền mặt:** Thao tác truyền thống, nhanh gọn.
- **Tích hợp PayOS (QR Pay):** Tự động tạo mã QR giao dịch, *kiểm tra trạng thái thanh toán tự động (Polling)* mà nhân viên không cần xác nhận tay.
- **Xuất hóa đơn PDF:** Xuất bill điện tử (PDF) rõ nét, hỗ trợ tiếng Việt chuẩn xác để gửi cho khách hàng.

### 🔐 3. Bảo Mật & Quản Trị Hệ Thống
- **Đăng nhập an toàn:** Hệ thống xác thực người dùng an toàn.
- **Phân quyền (RBAC):** Giao diện riêng biệt cho **Quản lý (Admin)** và **Nhân viên (Staff)**.
- **Báo cáo doanh thu:** Thống kê hóa đơn, doanh thu theo thời gian, theo nhân viên để dễ dàng đối soát.

---

## 🛠️ Công Nghệ Sử Dụng (Tech Stack)

Dự án được xây dựng dựa trên các công nghệ và thư viện hiện đại nhất dành cho ứng dụng Desktop:

| Lĩnh vực | Công nghệ |
| :--- | :--- |
| **Ngôn ngữ lõi** | Java 17 |
| **Giao diện (UI/UX)** | JavaFX 17, FXML, CSS |
| **Cơ sở dữ liệu** | MySQL 8.0 & JDBC (Quản lý Connection Singleton) |
| **Quản lý thư viện** | Apache Maven |
| **Testing & CI/CD** | JUnit 5, ArchUnit, GitHub Actions |
| **Mẫu thiết kế (Patterns)** | Clean MVC, DAO Pattern, DTO Pattern, Singleton |

---
  
## 📂 Cấu Trúc Dự Án (Project Structure)

Dự án được phân lớp cực kỳ chặt chẽ nhằm tối ưu hóa việc quản lý code và làm việc nhóm:

```text
📦 VTea
 ┣ 📂 .github/workflows      # Cấu hình Bot CI/CD (Quét ArchUnit tự động)
 ┣ 📂 src/main/java/com/vtea
 ┃ ┣ 📂 controller           # Đón sự kiện UI (Không chứa code SQL/Logic nặng)
 ┃ ┣ 📂 dao                  # Tương tác trực tiếp Database (Truy vấn SQL)
 ┃ ┣ 📂 model                # Khuôn mẫu dữ liệu ánh xạ từ Database (Entity)
 ┃ ┣ 📂 dto                  # Vận chuyển dữ liệu giữa các lớp (Data Transfer Object)
 ┃ ┣ 📂 service              # Chứa toàn bộ Logic nghiệp vụ (Tính tiền, Xử lý giao dịch)
 ┃ ┣ 📂 utils                # Công cụ hỗ trợ (DBConnection, Format, Dialog...)
 ┃ ┗ 📂 main                 # Chứa MainApp.java (Điểm khởi chạy ứng dụng)
 ┣ 📂 src/main/resources     # Chứa file giao diện (.fxml), thiết kế (.css) và ảnh
 ┗ 📜 pom.xml                # File quản lý thư viện Maven
```

---

## 🚀 Hướng Dẫn Cài Đặt & Chạy Dự Án

**1. Chuẩn bị môi trường:**
- Cài đặt [JDK 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
- Cài đặt [MySQL Server](https://dev.mysql.com/downloads/mysql/)
- Cài đặt IDE (IntelliJ IDEA / Eclipse / VS Code)

**2. Khởi tạo Database:**
- Chạy script SQL (nếu có) trong thư mục `database/` để tạo các bảng và dữ liệu mẫu.
- Sửa lại thông tin cấu hình Database (Username, Password) trong file `database.properties` hoặc class `DBConnection`.

**3. Khởi chạy dự án:**
```bash
# Clone dự án về máy
git clone https://github.com/hongtc08/VTea.git

# Mở Terminal tại thư mục dự án và chạy
mvn clean javafx:run
```

---

## 👥 Đội Ngũ Phát Triển (Team)

Sản phẩm được phát triển bằng tất cả tâm huyết bởi:

| Thành viên | Trách nhiệm | GitHub |
| :--- | :--- | :--- |
| **Tăng Chấn Hồng** | Trưởng nhóm / Fullstack | [@hongtc08](https://github.com/hongtc08) |
| **Phan Cao Minh Hiếu** | Developer | [@hieupcm03](https://github.com/hieupcm03) |
| **Nguyễn Hoàn Hải** | Developer | [@Haibrosh](https://github.com/Haibrosh) |
| **Hà Thảo Tiên** | Developer | [@Tienn203](https://github.com/Tienn203) |

<div align="center">
  <i>Đồ án môn học - Xây dựng với ❤️ bởi team VTea.</i>
</div>
