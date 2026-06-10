package com.vtea.service;

import com.vtea.dao.UserDAO;
import com.vtea.dto.LoginRequestDTO;
import com.vtea.dto.UserSessionDTO;
import com.vtea.model.User;
import org.mindrot.jbcrypt.BCrypt;

public class AuthService {

    // Gọi anh DAO lên để chuẩn bị cho việc lấy dữ liệu
    private final UserDAO userDAO = new UserDAO();

    /**
     * Hàm xử lý logic đăng nhập
     * Ném ra Exception với thông báo cụ thể để Controller biết đường hiện Popup lỗi
     */
    public UserSessionDTO login(LoginRequestDTO request) throws Exception {

        // 1. Gọi DAO đi tìm user theo username người dùng gõ
        User user = userDAO.getUserByUsername(request.getUsername());

        // 2. Kiểm tra tài khoản có tồn tại không
        if (user == null) {
            throw new Exception("Tài khoản không tồn tại!");
        }

        // 3. Kiểm tra tài khoản có bị Admin khóa không
        if (User.STATUS_LOCKED.equalsIgnoreCase(user.getStatus())) {
            throw new Exception("Tài khoản của bạn đã bị khóa. Vui lòng liên hệ Admin!");
        }

        // 4. Kiểm tra mật khẩu
        // Giải thích: BCrypt.checkpw sẽ tự động băm cái mật khẩu thô (người dùng vừa gõ)
        // và đem so sánh với cái chuỗi băm loằng ngoằng lấy từ DB lên.
        if (!BCrypt.checkpw(request.getRawPassword(), user.getPassWord())) {
            throw new Exception("Sai mật khẩu!");
        }

        // 5. Nếu vượt qua mọi cửa ải -> Đăng nhập thành công!
        // Đóng gói thông tin sạch sẽ, an toàn vào DTO để trả lên cho Controller
        return new UserSessionDTO(
                user.getUserId(),
                user.getUserName(),
                user.getFullName(),
                user.getRole()
        );
    }

    /**
     * Thêm nhân viên mới (Chỉ Admin được làm)
     */
    public boolean createNewStaff(User newStaff, int performerId) throws Exception {
        // 1. Chỉ Admin mới có quyền tạo tài khoản cho người khác
        checkAdminPermission(performerId);

        // 2. Kiểm tra xem username định tạo đã bị trùng dưới DB chưa
        User existingUser = userDAO.getUserByUsername(newStaff.getUserName());
        if (existingUser != null) {
            throw new Exception("Tên tài khoản này đã tồn tại trong hệ thống!");
        }

        // 3. Bảo mật: Băm mật khẩu thô trước khi lưu xuống DB
        String hashedPassword = BCrypt.hashpw(newStaff.getPassWord(), BCrypt.gensalt(12));
        newStaff.setPassWord(hashedPassword);

        // 4. Thiết lập trạng thái mặc định khi tạo mới là ACTIVE
        newStaff.setStatus(User.STATUS_ACTIVE);

        // 5. Gọi DAO để Insert xuống Database
        return userDAO.insertUser(newStaff);
    }

    /**
     * Khóa tài khoản (Chỉ Admin được làm)
     */

    public boolean lockAccount(int targetUserId, int performerId) throws Exception {
        // Kiểm tra quyền Admin
        checkAdminPermission(performerId);

        if (targetUserId == performerId) {
            throw new IllegalArgumentException("Bạn không thể khóa chính mình!");
        }

        return userDAO.updateStatus(targetUserId, User.STATUS_LOCKED);
    }

    /**
     * Mở khóa tài khoản (chỉ Admin được làm
     */

    public boolean unlockAccount(int targetUserId, int performerId) throws Exception {
        checkAdminPermission(performerId);

        return userDAO.updateStatus(targetUserId, User.STATUS_ACTIVE);
    }

    // ============== HELPER METHOS =======================

    /**
     * Hàm tiện ích kiểm tra quyền Admin
     */
    private void checkAdminPermission(int userId) throws Exception {
        User user = userDAO.getUserById(userId);

        if(user == null) {
            throw new Exception("Lỗi hệ thống: Người dùng không tồn tại!");
        }

        if (user.getRole() == null || !User.ROLE_ADMIN.equalsIgnoreCase(user.getRole())) {
            throw new Exception("Bạn không có quyền thực hiện hành động này!");
        }

        if (User.STATUS_LOCKED.equalsIgnoreCase(user.getStatus())) {
            throw new Exception("Tài khoản của bạn đã bị khóa. Vui lòng liên hệ Admin khác!");
        }

    }
}