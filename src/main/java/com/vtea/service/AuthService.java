package com.vtea.service;

import com.vtea.dao.UserDAO;
import com.vtea.dto.LoginRequestDTO;
import com.vtea.dto.UserSessionDTO;
import com.vtea.model.User;
import org.mindrot.jbcrypt.BCrypt;

public class AuthService {

    // Gọi anh DAO lên để chuẩn bị cho việc lấy dữ liệu
    private UserDAO userDAO = new UserDAO();

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
        if ("Locked".equalsIgnoreCase(user.getStatus())) {
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
}