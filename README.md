# 🧋 VTea - Hệ Thống Quản Lý Bán Hàng Trà Sữa (POS)

<div align="center">
  
  ![Build Status](https://img.shields.io/badge/Build-Passed-4CAF50?style=for-the-badge&logo=githubactions&logoColor=white)
  ![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=java&logoColor=white)
  ![JavaFX](https://img.shields.io/badge/JavaFX-17-007396?style=for-the-badge&logo=java&logoColor=white)
  ![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
  ![ArchUnit](https://img.shields.io/badge/Architecture-Verified-blue?style=for-the-badge)

  **Giải pháp quản lý bán hàng tối ưu, nhanh chóng và chính xác cho các cửa hàng Trà sữa.**
</div>

---

## 📖 Giới thiệu
**VTea** là một hệ thống Point of Sale (POS) mạnh mẽ được thiết kế chuyên biệt cho mô hình kinh doanh trà sữa. Ứng dụng tập trung vào trải nghiệm người dùng mượt mà, tính toán hóa đơn chính xác và quản lý dữ liệu chặt chẽ theo kiến trúc **Clean MVC**.

Dự án tích hợp hệ thống kiểm tra kiến trúc tự động (**ArchUnit**), đảm bảo mã nguồn luôn tuân thủ các tiêu chuẩn kỹ thuật cao nhất trong suốt quá trình phát triển.

---

## 👥 Đội Ngũ Phát Triển (Team)

| Thành viên | GitHub |
| :--- | :--- |
| **Tăng Chấn Hồng** | [@hongtc08](https://github.com/hongtc08) |
| **Phan Cao Minh Hiếu** | [@hieupcm03](https://github.com/hieupcm03) |
| **Nguyễn Hoàn Hải** | [@Haibrosh](https://github.com/Haibrosh) |
| **Hà Thảo Tiên** | [@Tienn203](https://github.com/Tienn203) |

---

## ✨ Tính Năng Nổi Bật

### 🛒 Quản Lý Bán Hàng (POS)
* **Giỏ hàng thông minh:** Thêm món, tăng giảm số lượng và tự động tính tiền theo thời gian thực.
* **Tùy chỉnh món:** Hỗ trợ xử lý topping, mức đường/đá cho từng đơn hàng.
* **Thanh toán nhanh:** Xuất hóa đơn và lưu trữ lịch sử giao dịch tức thì.

### 🔐 Bảo Mật & Phân Quyền
* **Đăng nhập an toàn:** Hệ thống xác thực người dùng dựa trên cơ sở dữ liệu thật.
* **Phân quyền rõ ràng:** Giao diện riêng biệt cho **Quản lý (Admin)** và **Nhân viên (Staff)**.

### 📦 Quản Lý Kho & Menu
* **CRUD Menu:** Quản lý danh mục trà sữa và sản phẩm linh hoạt.
* **Báo cáo doanh thu:** Thống kê hóa đơn, món bán chạy và hiệu suất làm việc của nhân viên.

---

## 🛠️ Công Nghệ Sử Dụng (Tech Stack)

Dự án được xây dựng dựa trên các công nghệ và thư viện hiện đại dành cho ứng dụng Desktop:

* **Ngôn ngữ lập trình:** Java 17
* **Giao diện (GUI):** JavaFX 17 (kết hợp FXML & CSS để tùy biến UI/UX)
* **Cơ sở dữ liệu:** MySQL 8.0 & JDBC
* **Quản lý dự án & Build Tool:** Apache Maven
* **Kiểm thử & Đảm bảo chất lượng code:** * **JUnit 5:** Framework chạy Unit Test.
  * **ArchUnit:** Kiểm tra và ép buộc tuân thủ kiến trúc MVC tự động.
* **DevOps / CI-CD:** GitHub Actions (Tự động quét cấu trúc code mỗi khi có Pull Request)
* **Kiến trúc & Pattern:** Clean MVC (Model - View - Controller), DAO Pattern (Data Access Object), Singleton Pattern (Quản lý Database Connection).

---
  
## 📂 Cấu Trúc Dự Án (Project Structure)

Dự án được tổ chức theo chuẩn Maven và chia lớp MVC rõ ràng nhằm tối ưu hóa việc quản lý code và làm việc nhóm:

```text
📦 VTea
 ┣ 📂 .github/workflows      # Chứa cấu hình Bot CI/CD (ArchUnit, Build App)
 ┣ 📂 src
 ┃ ┣ 📂 main
 ┃ ┃ ┣ 📂 java/com/vtea
 ┃ ┃ ┃ ┣ 📂 controller     # Đón nhận sự kiện giao diện (Cấm chứa code SQL)
 ┃ ┃ ┃ ┣ 📂 dao            # Tương tác Database (Tên file bắt buộc có đuôi DAO)
 ┃ ┃ ┃ ┣ 📂 main           # Chứa MainApp.java (Điểm bắt đầu chạy ứng dụng)
 ┃ ┃ ┃ ┣ 📂 model          # Khuôn mẫu dữ liệu (Order, User, Product...)
 ┃ ┃ ┃ ┣ 📂 service        # Xử lý logic nghiệp vụ (Tính tiền, Kiểm tra đăng nhập)
 ┃ ┃ ┃ ┗ 📂 utils          # Các công cụ dùng chung (DBConnection, Helper...)
 ┃ ┃ ┗ 📂 resources/com/vtea
 ┃ ┃   ┣ 📂 css            # File style (.css) để trang trí giao diện
 ┃ ┃   ┗ 📂 view           # File thiết kế giao diện (.fxml) của JavaFX
 ┃ ┗ 📂 test/java/com/vtea   # Chứa các file kiểm thử tự động (ArchitectureTest.java)
 ┣ 📜 pom.xml                # Cấu hình thư viện Maven (JavaFX, JUnit, ArchUnit)
 ┗ 📜 README.md              # Tài liệu hướng dẫn dự án
