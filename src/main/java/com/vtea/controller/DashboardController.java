package com.vtea.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import java.io.IOException;

public class DashboardController {

    @FXML
    private VBox vboxRecentOrders;

    @FXML
    private VBox vboxTopProducts;

    @FXML
    private VBox vboxLowStock;

    //Hàm khởi tạo, t để mấy cái dữ liệu mẫu thôi
    @FXML
    public void initialize() {
        System.out.println("Init Dashboard Controller...");

        // Thêm dữ liệu cho Item 1 (Đơn hàng)
        vboxRecentOrders.getChildren().add(loadOrderItem("#001", "Nguyễn Văn A", "Trà sữa trân châu, Cafe sữa", "85,000đ", "10:30"));
        vboxRecentOrders.getChildren().add(loadOrderItem("#002", "Trần Thị B", "Matcha latte", "55,000đ", "10:45"));

        // Thêm dữ liệu cho Item 2 (Sản phẩm bán chạy)
        vboxTopProducts.getChildren().add(loadTopProductItem("1", "Trà sữa trân châu", "45", "2,250,000đ"));
        vboxTopProducts.getChildren().add(loadTopProductItem("2", "Cafe sữa", "38", "1,520,000đ"));

        // TODO: Backend - Gọi IngredientDAO.getLowStockAlerts() lấy danh sách nguyên liệu sắp hết
        // TODO: Backend - Lặp qua danh sách và nạp vào vboxLowStock. Dữ liệu mẫu:
        vboxLowStock.getChildren().add(loadLowStockItem("Trân châu đen", "0.5 kg", "5 kg"));
        vboxLowStock.getChildren().add(loadLowStockItem("Đường đen", "1 kg", "2 kg"));
    }

    //Hàm load thông tin vào OrderItem
    private HBox loadOrderItem(String id, String name, String items, String total, String time) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vtea/view/OrderItem.fxml"));
            HBox itemNode = loader.load();

            ((Label) itemNode.lookup("#lblOrderId")).setText(id);
            ((Label) itemNode.lookup("#lblCustomer")).setText("- " + name);
            ((Label) itemNode.lookup("#lblItems")).setText(items);
            ((Label) itemNode.lookup("#lblTotal")).setText(total);
            ((Label) itemNode.lookup("#lblTime")).setText(time);

            return itemNode;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    //Hàm load thông tin vào TopProductItem
    private HBox loadTopProductItem(String rank, String productName, String soldCount, String totalRevenue) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vtea/view/TopProductItem.fxml"));
            HBox itemNode = loader.load();

            ((Label) itemNode.lookup("#lblRank")).setText(rank);
            ((Label) itemNode.lookup("#lblProductName")).setText(productName);
            ((Label) itemNode.lookup("#lblSold")).setText(soldCount + " đã bán");
            ((Label) itemNode.lookup("#lblRevenue")).setText(totalRevenue);

            return itemNode;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    //Hàm load thông tin vào LowStockItem
    private HBox loadLowStockItem(String ingredientName, String currentStock, String minStock) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vtea/view/LowStockItem.fxml"));
            HBox itemNode = loader.load();

            ((Label) itemNode.lookup("#lblIngredientName")).setText(ingredientName);
            ((Label) itemNode.lookup("#lblStockInfo")).setText("Còn lại: " + currentStock + " (Tối thiểu: " + minStock + ")");

            return itemNode;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}