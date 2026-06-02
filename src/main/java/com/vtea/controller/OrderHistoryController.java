package com.vtea.controller;

import com.vtea.dto.BillDTO;
import com.vtea.dto.OrderHistoryDTO;
import com.vtea.service.BillPdfService;
import com.vtea.service.BillReceiptFormatter;
import com.vtea.service.BillService;
import com.vtea.utils.DialogHelper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller cho màn hình lịch sử hóa đơn.
 * Màn này hiển thị danh sách hóa đơn và chi tiết full bill theo order_id.
 */
public class OrderHistoryController {

    @FXML private TextField txtKeyword;
    @FXML private DatePicker dpFromDate;
    @FXML private DatePicker dpToDate;

    @FXML private TableView<OrderHistoryDTO> orderHistoryTable;
    @FXML private TableColumn<OrderHistoryDTO, Integer> colOrderId;
    @FXML private TableColumn<OrderHistoryDTO, String> colCreatedAt;
    @FXML private TableColumn<OrderHistoryDTO, String> colStaffName;
    @FXML private TableColumn<OrderHistoryDTO, String> colCustomerName;
    @FXML private TableColumn<OrderHistoryDTO, String> colPaymentMethod;
    @FXML private TableColumn<OrderHistoryDTO, String> colTotalAmount;
    @FXML private TableColumn<OrderHistoryDTO, String> colStatus;

    @FXML private TextArea billDetailTextArea;

    private final BillService billService = new BillService();
    private final BillReceiptFormatter receiptFormatter = new BillReceiptFormatter();
    private final BillPdfService billPdfService = new BillPdfService();

    private BillDTO selectedBill;

    private final DateTimeFormatter dateTimeFormatter =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Khởi tạo màn hình lịch sử hóa đơn.
     */
    @FXML
    public void initialize() {
        setupTableColumns();
        setupTableSelectionEvent();
        loadOrderHistory();
    }

    /**
     * Setup dữ liệu cho từng cột trong bảng hóa đơn.
     */
    private void setupTableColumns() {
        colOrderId.setCellValueFactory(data ->
                new SimpleObjectProperty<>(data.getValue().getOrderId())
        );

        colCreatedAt.setCellValueFactory(data ->
                new SimpleStringProperty(formatDateTime(data.getValue().getCreatedAt()))
        );

        colStaffName.setCellValueFactory(data ->
                new SimpleStringProperty(nullToDefault(data.getValue().getStaffName(), "Không rõ"))
        );

        colCustomerName.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getCustomerName())
        );

        colPaymentMethod.setCellValueFactory(data ->
                new SimpleStringProperty(nullToDefault(data.getValue().getPaymentMethod(), "Không rõ"))
        );

        colTotalAmount.setCellValueFactory(data ->
                new SimpleStringProperty(formatPrice(data.getValue().getTotalAmount()))
        );

        colStatus.setCellValueFactory(data ->
                new SimpleStringProperty(nullToDefault(data.getValue().getStatus(), "Không rõ"))
        );
    }

    /**
     * Khi chọn một dòng hóa đơn, tự load full bill theo order_id.
     */
    private void setupTableSelectionEvent() {
        orderHistoryTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldValue, selectedOrder) -> {
                    if (selectedOrder != null) {
                        loadBillDetail(selectedOrder.getOrderId());
                    }
                });
    }

    /**
     * Load toàn bộ lịch sử hóa đơn.
     */
    private void loadOrderHistory() {
        try {
            List<OrderHistoryDTO> histories = billService.getOrderHistory();
            orderHistoryTable.setItems(FXCollections.observableArrayList(histories));

            selectedBill = null;
            billDetailTextArea.clear();
        } catch (Exception e) {
            e.printStackTrace();
            DialogHelper.showInfo("Lỗi", "Không thể tải lịch sử hóa đơn: " + e.getMessage());
        }
    }

    /**
     * Load chi tiết full bill theo order_id.
     */
    private void loadBillDetail(int orderId) {
        try {
            selectedBill = billService.getBillByOrderId(orderId);
            billDetailTextArea.setText(receiptFormatter.format(selectedBill));
            billDetailTextArea.positionCaret(0);
        } catch (Exception e) {
            e.printStackTrace();
            selectedBill = null;
            billDetailTextArea.clear();
            DialogHelper.showInfo("Lỗi", "Không thể tải chi tiết hóa đơn: " + e.getMessage());
        }
    }

    /**
     * Tìm kiếm hóa đơn theo mã hóa đơn, tên khách hàng, SĐT hoặc nhân viên.
     */
    @FXML
    private void handleSearch() {
        try {
            String keyword = txtKeyword.getText();

            List<OrderHistoryDTO> result = billService.searchOrderHistory(keyword);
            orderHistoryTable.setItems(FXCollections.observableArrayList(result));

            selectedBill = null;
            billDetailTextArea.clear();
        } catch (Exception e) {
            e.printStackTrace();
            DialogHelper.showInfo("Lỗi", "Không thể tìm kiếm hóa đơn: " + e.getMessage());
        }
    }

    /**
     * Lọc hóa đơn theo khoảng ngày.
     */
    @FXML
    private void handleFilterByDate() {
        try {
            if (dpFromDate.getValue() == null || dpToDate.getValue() == null) {
                DialogHelper.showInfo("Thông báo", "Vui lòng chọn đủ từ ngày và đến ngày.");
                return;
            }

            List<OrderHistoryDTO> result = billService.getOrderHistoryByDateRange(
                    dpFromDate.getValue(),
                    dpToDate.getValue()
            );

            orderHistoryTable.setItems(FXCollections.observableArrayList(result));

            selectedBill = null;
            billDetailTextArea.clear();
        } catch (Exception e) {
            e.printStackTrace();
            DialogHelper.showInfo("Lỗi", "Không thể lọc hóa đơn theo ngày: " + e.getMessage());
        }
    }

    /**
     * Tải lại toàn bộ lịch sử hóa đơn.
     */
    @FXML
    private void handleReload() {
        txtKeyword.clear();
        dpFromDate.setValue(null);
        dpToDate.setValue(null);
        loadOrderHistory();
    }

    /**
     * Mở bill preview bằng màn hình preview đã có.
     */
    @FXML
    private void handleOpenBillPreview() {
        if (selectedBill == null) {
            DialogHelper.showInfo("Thông báo", "Vui lòng chọn hóa đơn trước.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/vtea/view/bill-preview.fxml")
            );

            Parent root = loader.load();

            BillPreviewController controller = loader.getController();
            controller.setBill(selectedBill);

            Stage stage = new Stage();
            stage.setTitle("Chi tiết hóa đơn");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            DialogHelper.showInfo("Lỗi", "Không thể mở chi tiết hóa đơn: " + e.getMessage());
        }
    }

    /**
     * Xuất PDF cho hóa đơn đang được chọn.
     */
    @FXML
    private void handleExportPdf() {
        if (selectedBill == null) {
            DialogHelper.showInfo("Thông báo", "Vui lòng chọn hóa đơn trước.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Lưu hóa đơn PDF");
        fileChooser.setInitialFileName("bill-" + selectedBill.getOrderId() + ".pdf");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF files", "*.pdf")
        );

        File file = fileChooser.showSaveDialog(orderHistoryTable.getScene().getWindow());

        if (file == null) {
            return;
        }

        try {
            billPdfService.exportBillToPdf(selectedBill, file);
            DialogHelper.showInfo("Thành công", "Xuất PDF hóa đơn thành công.");
        } catch (Exception e) {
            e.printStackTrace();
            DialogHelper.showInfo("Lỗi", "Không thể xuất PDF: " + e.getMessage());
        }
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }

        return dateTime.format(dateTimeFormatter);
    }

    private String formatPrice(BigDecimal price) {
        if (price == null) {
            return "0 đ";
        }

        return String.format("%,.0f đ", price);
    }

    private String nullToDefault(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        return value;
    }
}