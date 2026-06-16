package com.vtea.service;

import com.vtea.dao.UserDAO;
import com.vtea.dto.LoginRequestDTO;
import com.vtea.dto.UserSessionDTO;
import com.vtea.model.User;
import org.mindrot.jbcrypt.BCrypt;

public class AuthService {

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
        if (!BCrypt.checkpw(request.getRawPassword(), user.getPassWord())) {
            throw new Exception("Sai mật khẩu!");
        }

        // 5. Nếu vượt qua mọi cửa ải -> Đăng nhập thành công!
        return new UserSessionDTO(
                user.getUserId(),
                user.getUserName(),
                user.getFullName(),
                user.getRole()
        );
    }

    /**
     * Hàm lấy email theo username để gửi mã OTP khôi phục mật khẩu
     */
    public String getEmailByUsername(String username) {
        return userDAO.getEmailByUsername(username);
    }

    /**
     * Hàm cập nhật mật khẩu mới sau khi người dùng đã xác thực mã OTP thành công
     */
    public boolean updatePassword(String username, String newPassword) {
        // 1. Mã hóa mật khẩu mới bằng BCrypt trước khi đưa xuống Database
        String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());

        // 2. Gửi mật khẩu đã mã hóa (hashedPassword) xuống DAO
        return userDAO.updatePassword(username, hashedPassword);
    }
}