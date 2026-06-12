package com.vtea.service;

import com.vtea.dao.ToppingDAO;
import com.vtea.dto.ToppingDTO;
import com.vtea.model.Topping;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ToppingService {

    private final ToppingDAO toppingDAO = new ToppingDAO();

    // =================================
    // NHÓM LẤY DỮ LIỆU VÀ TÍNH TIỀN
    // =================================

    /**
     * 1. Lấy danh sách tất cả các topping đang bán cho máy POS (Thu ngân)
     */
    public List<ToppingDTO> getAllActiveToppings() {
        List<Topping> models = toppingDAO.getAllActiveToppings();
        return mapListToDTO(models);
    }


    // =========================================
    // NHÓM 2: THÊM, XÓA, SỬA TOPPING
    // =========================================

    /**
     * 4. Thêm topping mới
     */
    public void createTopping(ToppingDTO dto) throws Exception {
        // Kiểm duyệt dữ liệu đầu vào
        if(dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new Exception("Lỗi: Tên topping không được để trống!");
        }
        if(dto.getPrice() == null || dto.getPrice().compareTo(BigDecimal.ZERO) <= 0){
            throw new Exception("Lỗi: Giá topping phải lớn hơn 0!");
        }

        // Chuyển DTO thành model
        Topping model = new Topping();
        model.setName(dto.getName().trim());
        model.setPrice(dto.getPrice());
        model.setImageUrl(dto.getImageUrl());

        boolean isSuccess = toppingDAO.insertTopping(model);
        if(!isSuccess) {
            throw new Exception("Lỗi hệ thống: Không thể thêm topping vào Database!");
        }
    }

    /**
     * 5. Cập nhật thông tin topping
     */
    public void updateTopping(ToppingDTO dto) throws Exception {
        if (dto.getToppingId() <= 0) {
            throw new Exception("Lỗi: Không xác định được topping cần sửa!");
        }
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new Exception("Lỗi: Tên topping không được để trống!");
        }
        if (dto.getPrice() == null || dto.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new Exception("Lỗi: Giá topping phải lớn hơn 0!");
        }

        Topping model = new Topping();
        model.setToppingId(dto.getToppingId());
        model.setName(dto.getName().trim());
        model.setPrice(dto.getPrice());
        model.setImageUrl(dto.getImageUrl());
        model.setAvailable(dto.getAvailable());

        boolean isSuccess = toppingDAO.updateTopping(model);
        if (!isSuccess) {
            throw new Exception("Lỗi hệ thống: Không thể cập nhật Topping!");

        }
    }

    /**
     * 6. Xóa Topping (cập nhật trạng thái ngừng bán)
     */
    public void softDeleteTopping(int toppingId) throws Exception {
        if (toppingId <= 0) {
            throw new Exception("Lỗi: Không xác định được topping cần xóa!");
        }

        boolean isSuccess = toppingDAO.deleteTopping(toppingId);
        if(!isSuccess) {
            throw new Exception("Lỗi hệ thống: Không thể xóa Topping!");
        }
    }

    // ====================================
    // HÀM TIỆN ÍCH ÁNH XẠ MODEL <-> DTO
    // ====================================

    private List<ToppingDTO> mapListToDTO(List<Topping> models) {
        List<ToppingDTO> dtoList = new ArrayList<>();
        for(Topping model : models) {
            ToppingDTO dto = new ToppingDTO();
            dto.setToppingId(model.getToppingId());
            dto.setName(model.getName());
            dto.setPrice(model.getPrice());
            dto.setImageUrl(model.getImageUrl());
            dto.setAvailable(model.getAvailable());
            dtoList.add(dto);
        }
        return dtoList;
    }

}
