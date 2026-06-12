package com.vtea.controller;

import com.vtea.dto.*;
import com.vtea.service.ReportService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class ReportController implements Initializable {
    @FXML private DatePicker dpStartDate;
    @FXML private DatePicker dpEndDate;
    @FXML private Button btnFilter;

    @FXML private LineChart<String, Number> lineChartRevenue;
    @FXML private PieChart pieChartCategory;

    @FXML private BarChart<Number, String> barChartTopProducts;
    @FXML private TableView<StaffSalesDTO> tableStaff;
    @FXML private TableColumn<StaffSalesDTO, String> colStaffName;
    @FXML private TableColumn<StaffSalesDTO, Integer> colStaffOrders;
    @FXML private TableColumn<StaffSalesDTO, BigDecimal> colStaffRevenue;

    private ReportService reportService = new ReportService();
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Cài đặt thời gian mặc định (Từ đầu tháng đến hôm nay)
        dpStartDate.setValue(LocalDate.now().withDayOfMonth(1));
        dpEndDate.setValue(LocalDate.now());

        // Cấu hình các cột cho TableView Nhân viên
        setupTableColumns();

        btnFilter.setOnAction(event -> loadAllReports());

        loadAllReports();
    }

    private void setupTableColumns() {
        colStaffName.setCellValueFactory(new PropertyValueFactory<>("staffName"));
        colStaffOrders.setCellValueFactory(new PropertyValueFactory<>("totalOrders"));
        colStaffRevenue.setCellValueFactory(new PropertyValueFactory<>("totalRevenue"));

        // Format cột doanh thu thành tiền tệ
        colStaffRevenue.setCellFactory(column -> new TableCell<StaffSalesDTO, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    DecimalFormat df = new DecimalFormat("#,### đ");
                    setText(df.format(item));
                }
            }
        });
    }

    private void loadAllReports() {
        LocalDate start = dpStartDate.getValue();
        LocalDate end = dpEndDate.getValue();

        if (start == null || end == null || start.isAfter(end)) {
            showAlert("Lỗi chọn ngày", "Ngày bắt đầu không được lớn hơn ngày kết thúc!");
            return;
        }

        loadRevenueTrend(start, end);
        loadCategoryRevenue(start, end);
        loadTopProducts(start, end);
        loadStaffPerformance(start, end);
    }

    private void loadRevenueTrend(LocalDate start, LocalDate end) {
        lineChartRevenue.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Doanh thu");

        List<TimeRevenueDTO> dataList = reportService.getRevenueByDate(start, end);
        for (TimeRevenueDTO data : dataList) {
            series.getData().add(new XYChart.Data<>(data.getTimeLabel(), data.getTotalRevenue()));
        }

        lineChartRevenue.getData().add(series);
    }

    private void loadCategoryRevenue(LocalDate start, LocalDate end) {
        pieChartCategory.getData().clear();

        List<CategoryRevenueDTO> dataList = reportService.getRevenueByCategory(start, end);
        for (CategoryRevenueDTO data : dataList) {
            // PieChart yêu cầu giá trị double
            PieChart.Data slice = new PieChart.Data(data.getCategoryName(), data.getTotalRevenue().doubleValue());
            pieChartCategory.getData().add(slice);
        }
    }

    private void loadTopProducts(LocalDate start, LocalDate end) {
        barChartTopProducts.getData().clear();

        XYChart.Series<Number, String> series = new XYChart.Series<>();
        List<ProductSalesDTO> dataList = reportService.getTopSellingProducts(start, end);

        for (int i = dataList.size() - 1; i >= 0; i--) {
            ProductSalesDTO data = dataList.get(i);
            series.getData().add(new XYChart.Data<>(data.getTotalQuantitySold(), data.getProductName()));
        }

        barChartTopProducts.getData().add(series);
    }

    private void loadStaffPerformance(LocalDate start, LocalDate end) {
        List<StaffSalesDTO> dataList = reportService.getRevenueByStaff(start, end);
        // Đổ List vào ObservableList để TableView nhận diện được
        ObservableList<StaffSalesDTO> observableList = FXCollections.observableArrayList(dataList);
        tableStaff.setItems(observableList);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
