package com.vtea.service;

import com.vtea.dao.UserDAO;
import com.vtea.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;

public class UserService {

    private final UserDAO userDAO = new UserDAO();

    // ==================== THÊM MỚI NHÂN VIÊN ====================

    public boolean createNewStaff(User newStaff, int performerId) throws Exception {
        // 1. Chỉ Admin mới được tạo
        checkAdminPermission(performerId);

        // 2. Kiểm tra trùng Username
        User existingUser = userDAO.getUserByUsername(newStaff.getUserName());
        if (existingUser != null) {
            throw new Exception("Tên tài khoản này đã tồn tại trong hệ thống!");
        }

        // 3. Kiểm tra trùng Email (Nếu có nhập)
        if (newStaff.getEmail() != null && !newStaff.getEmail().trim().isEmpty()) {
            if (userDAO.getUserByEmail(newStaff.getEmail().trim()) != null) {
                throw new Exception("Email này đã được sử dụng bởi một nhân viên khác!");
            }
        }

        // 4. Kiểm tra trùng Số điện thoại (Nếu có nhập)
        if (newStaff.getPhone() != null && !newStaff.getPhone().trim().isEmpty()) {
            if (userDAO.getUserByPhone(newStaff.getPhone().trim()) != null) {
                throw new Exception("Số điện thoại này đã được đăng ký trong hệ thống!");
            }
        }

        // 5. Băm mật khẩu và set trạng thái
        String hashedPassword = BCrypt.hashpw(newStaff.getPassWord(), BCrypt.gensalt(12));
        newStaff.setPassWord(hashedPassword);
        newStaff.setStatus(User.STATUS_ACTIVE);

        return userDAO.insertUser(newStaff);
    }

    // ==================== TRUY VẤN DANH SÁCH ====================

    public List<User> getAllUsers() {
        return userDAO.getAllUsers();
    }

    public List<User> searchUsersByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return userDAO.getAllUsers();
        }
        return userDAO.getUsersByName(name.trim());
    }

    public User getUserByEmail(String email) throws Exception {
        if (email == null || email.trim().isEmpty()) {
            throw new Exception("Email không được để trống!");
        }
        return userDAO.getUserByEmail(email.trim());
    }

    public User getUserByPhone(String phone) throws Exception {
        if (phone == null || phone.trim().isEmpty()) {
            throw new Exception("Số điện thoại không được để trống!");
        }
        return userDAO.getUserByPhone(phone.trim());
    }

    // ==================== CẬP NHẬT THÔNG TIN ====================

    /**
     * Cập nhật thông tin cơ bản của nhân viên (Họ tên, Vai trò, Email, Phone, Lương, Ngày làm)
     */
    public boolean updateUserInfo(User updatedUser, int performerId) throws Exception {
        // Chỉ Admin hoặc chính nhân viên đó mới được sửa thông tin của mình
        if (updatedUser.getUserId() != performerId) {
            checkAdminPermission(performerId);
        }

        if (updatedUser.getFullName() == null || updatedUser.getFullName().trim().isEmpty()) {
            throw new Exception("Họ và tên không được để trống!");
        }

        // Kiểm tra xem Email mới sửa có bị trùng với người khác không
        if (updatedUser.getEmail() != null && !updatedUser.getEmail().trim().isEmpty()) {
            User existEmail = userDAO.getUserByEmail(updatedUser.getEmail().trim());
            if (existEmail != null && existEmail.getUserId() != updatedUser.getUserId()) {
                throw new Exception("Email này đã được sử dụng bởi một nhân viên khác!");
            }
        }

        // Kiểm tra xem SĐT mới sửa có bị trùng với người khác không
        if (updatedUser.getPhone() != null && !updatedUser.getPhone().trim().isEmpty()) {
            User existPhone = userDAO.getUserByPhone(updatedUser.getPhone().trim());
            if (existPhone != null && existPhone.getUserId() != updatedUser.getUserId()) {
                throw new Exception("Số điện thoại này đã được sử dụng bởi một nhân viên khác!");
            }
        }

        return userDAO.updateUser(updatedUser);
    }

    /**
     * Đổi mật khẩu cho nhân viên
     */
    public boolean changePassword(int userId, String oldPassword, String newPassword) throws Exception {
        if (newPassword == null || newPassword.length() < 6) {
            throw new Exception("Mật khẩu mới phải có ít nhất 6 ký tự!");
        }

        // 1. Lấy thông tin user hiện tại từ DB
        User currentUser = userDAO.getUserById(userId);
        if (currentUser == null) {
            throw new Exception("Người dùng không tồn tại!");
        }

        // 2. Xác thực mật khẩu cũ có đúng không
        if (!BCrypt.checkpw(oldPassword, currentUser.getPassWord())) {
            throw new Exception("Mật khẩu cũ không chính xác!");
        }

        // 3. Băm mật khẩu mới và lưu xuống DB
        String hashedNewPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt(12));
        return userDAO.updatePassword(userId, hashedNewPassword);
    }

    // ==================== QUẢN LÝ TRẠNG THÁI ====================

    public boolean lockAccount(int targetUserId, int performerId) throws Exception {
        checkAdminPermission(performerId);
        if (targetUserId == performerId) {
            throw new IllegalArgumentException("Bạn không thể khóa chính mình!");
        }
        return userDAO.updateStatus(targetUserId, User.STATUS_LOCKED);
    }

    public boolean unlockAccount(int targetUserId, int performerId) throws Exception {
        checkAdminPermission(performerId);
        return userDAO.updateStatus(targetUserId, User.STATUS_ACTIVE);
    }

    // ==================== HELPER METHODS ====================

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