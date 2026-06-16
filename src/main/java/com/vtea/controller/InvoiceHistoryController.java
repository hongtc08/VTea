package com.vtea.controller;

import com.vtea.dto.BillDTO;
import com.vtea.dto.OrderHistoryDTO;
import com.vtea.service.BillService;
import com.vtea.utils.DialogHelper;
import com.vtea.utils.FormatUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller cho tab lịch sử hóa đơn.
 * Load danh sách hóa đơn cơ bản, khi bấm Chi tiết mới query full bill.
 */
public class InvoiceHistoryController {

    @FXML private TextField searchField;
    @FXML private VBox invoiceGroupsContainer;

    private final BillService billService = new BillService();

    private final DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final DateTimeFormatter monthFormatter =
            DateTimeFormatter.ofPattern("MM/yyyy");

    @FXML
    public void initialize() {
        loadInvoiceData();

        searchField.textProperty().addListener((obs, oldValue, newValue) -> handleSearch());
    }

    /**
     * Load danh sách hóa đơn ban đầu.
     */
    private void loadInvoiceData() {
        try {
            List<OrderHistoryDTO> invoices = billService.getOrderHistory();
            renderInvoiceGroups(invoices);
        } catch (Exception e) {
            e.printStackTrace();
            DialogHelper.showInfo("Lỗi", "Không thể tải lịch sử hóa đơn: " + e.getMessage());
        }
    }

    /**
     * Tìm kiếm hóa đơn theo mã hóa đơn, tên khách hàng, SĐT hoặc nhân viên.
     */
    private void handleSearch() {
        try {
            String keyword = searchField.getText();

            List<OrderHistoryDTO> invoices;

            if (keyword == null || keyword.isBlank()) {
                invoices = billService.getOrderHistory();
            } else {
                invoices = billService.searchOrderHistory(keyword.trim());
            }

            renderInvoiceGroups(invoices);
        } catch (Exception e) {
            e.printStackTrace();
            DialogHelper.showInfo("Lỗi", "Không thể tìm kiếm hóa đơn: " + e.getMessage());
        }
    }

    /**
     * Render danh sách hóa đơn theo từng nhóm thời gian.
     */
    private void renderInvoiceGroups(List<OrderHistoryDTO> invoices) {
        invoiceGroupsContainer.getChildren().clear();

        if (invoices == null || invoices.isEmpty()) {
            javafx.scene.layout.VBox emptyState = new javafx.scene.layout.VBox(16);
            emptyState.setAlignment(javafx.geometry.Pos.CENTER);
            emptyState.setPadding(new javafx.geometry.Insets(100, 0, 0, 0));
            
            org.kordamp.ikonli.javafx.FontIcon icon = new org.kordamp.ikonli.javafx.FontIcon("fth-inbox");
            icon.setIconSize(64);
            icon.setIconColor(javafx.scene.paint.Color.web("#d6d3d1"));
            
            javafx.scene.control.Label lbl = new javafx.scene.control.Label("Không có hóa đơn nào");
            lbl.setStyle("-fx-text-fill: #a8a29e; -fx-font-size: 18px; -fx-font-weight: bold;");
            
            emptyState.getChildren().addAll(icon, lbl);
            invoiceGroupsContainer.getChildren().add(emptyState);
            return;
        }

        Map<String, List<OrderHistoryDTO>> groupedInvoices = groupInvoices(invoices);

        for (Map.Entry<String, List<OrderHistoryDTO>> entry : groupedInvoices.entrySet()) {
            try {
                VBox groupNode = createInvoiceGroup(entry.getKey(), entry.getValue());
                invoiceGroupsContainer.getChildren().add(groupNode);
            } catch (IOException e) {
                e.printStackTrace();
                DialogHelper.showInfo("Lỗi", "Không thể tạo nhóm hóa đơn: " + e.getMessage());
            }
        }
    }

    /**
     * Gom hóa đơn theo Hôm nay hoặc Tháng MM/yyyy.
     */
    private Map<String, List<OrderHistoryDTO>> groupInvoices(List<OrderHistoryDTO> invoices) {
        Map<String, List<OrderHistoryDTO>> groupedInvoices = new LinkedHashMap<>();

        for (OrderHistoryDTO invoice : invoices) {
            String groupName = getGroupName(invoice.getCreatedAt());

            groupedInvoices
                    .computeIfAbsent(groupName, key -> new java.util.ArrayList<>())
                    .add(invoice);
        }

        return groupedInvoices;
    }

    private VBox createInvoiceGroup(String groupName, List<OrderHistoryDTO> invoices) throws IOException {
        FXMLLoader groupLoader = new FXMLLoader(getClass().getResource("/com/vtea/view/InvoiceGroup.fxml"));
        VBox groupNode = groupLoader.load();

        Label lblGroupName = (Label) groupNode.lookup("#lblGroupName");
        Label lblGroupSub = (Label) groupNode.lookup("#lblGroupSub");
        Label lblInvoiceCount = (Label) groupNode.lookup("#lblInvoiceCount");

        VBox groupContent = (VBox) groupNode.lookup("#groupContent");
        VBox invoiceListContainer = (VBox) groupNode.lookup("#invoiceListContainer");
        FontIcon iconArrow = (FontIcon) groupNode.lookup("#iconArrow");
        HBox groupHeader = (HBox) groupNode.lookup("#groupHeader");

        if (lblGroupName != null) {
            lblGroupName.setText(groupName);
        }

        if (lblGroupSub != null) {
            lblGroupSub.setText("Danh sách hóa đơn");
        }

        if (lblInvoiceCount != null) {
            lblInvoiceCount.setText(invoices.size() + " hóa đơn");
        }

        if (invoiceListContainer != null) {
            invoiceListContainer.getChildren().clear();

            for (OrderHistoryDTO invoice : invoices) {
                invoiceListContainer.getChildren().add(createInvoiceRow(invoice));
            }
        }

        setupCollapseEvent(groupHeader, groupContent, iconArrow);

        return groupNode;
    }

    /**
     * Tạo một dòng hóa đơn cơ bản.
     */
    private HBox createInvoiceRow(OrderHistoryDTO invoice) throws IOException {
        FXMLLoader rowLoader = new FXMLLoader(getClass().getResource("/com/vtea/view/InvoiceRow.fxml"));
        HBox rowNode = rowLoader.load();

        Label lblInvoiceId = (Label) rowNode.lookup("#lblInvoiceId");
        Label lblTime = (Label) rowNode.lookup("#lblTime");
        Label lblEmployee = (Label) rowNode.lookup("#lblEmployee");
        Label lblCustomerName = (Label) rowNode.lookup("#lblCustomerName");
        Label lblCustomerPhone = (Label) rowNode.lookup("#lblCustomerPhone");
        Label lblPaymentMethod = (Label) rowNode.lookup("#lblPaymentMethod");
        Label lblTotalAmount = (Label) rowNode.lookup("#lblTotalAmount");
        Label btnDetails = (Label) rowNode.lookup("#btnDetails");

        if (lblInvoiceId != null) {
            lblInvoiceId.setText("#" + invoice.getOrderId());
        }

        if (lblTime != null) {
            lblTime.setText(formatDate(invoice.getCreatedAt()));
        }

        if (lblEmployee != null) {
            lblEmployee.setText(nullToDefault(invoice.getStaffName(), "Không rõ"));
        }

        if (lblCustomerName != null) {
            lblCustomerName.setText(nullToDefault(invoice.getCustomerName(), "Khách vãng lai"));
        }

        if (lblCustomerPhone != null) {
            lblCustomerPhone.setText(nullToDefault(invoice.getCustomerPhone(), ""));
        }

        if (lblPaymentMethod != null) {
            lblPaymentMethod.setText(nullToDefault(invoice.getPaymentMethod(), "Không rõ"));
        }

        if (lblTotalAmount != null) {
            lblTotalAmount.setText(FormatUtils.formatPrice(invoice.getTotalAmount()));
        }

        if (btnDetails != null) {
            btnDetails.setOnMouseClicked(event -> openBillPreview(invoice.getOrderId()));
        }

        return rowNode;
    }

    /**
     * Bấm Chi tiết mới query full bill theo order_id.
     */
    private void openBillPreview(int orderId) {
        try {
            BillDTO bill = billService.getBillByOrderId(orderId);

            if (bill == null) {
                DialogHelper.showInfo("Thông báo", "Không tìm thấy hóa đơn #" + orderId);
                return;
            }

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/vtea/view/bill-preview.fxml")
            );

            Parent root = loader.load();

            BillPreviewController controller = loader.getController();
            controller.setBill(bill);

            Stage stage = new Stage();
            stage.setTitle("Chi tiết hóa đơn");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            
            com.vtea.utils.DialogHelper.applyBlurBackground(true);
            com.vtea.utils.DialogHelper.animateDialog(root);
            stage.showAndWait();
            com.vtea.utils.DialogHelper.applyBlurBackground(false);
        } catch (Exception e) {
            e.printStackTrace();
            DialogHelper.showInfo("Lỗi", "Không thể mở chi tiết hóa đơn: " + e.getMessage());
        }
    }

    /**
     * Xử lý đóng/mở từng nhóm hóa đơn.
     */
    private void setupCollapseEvent(HBox groupHeader, VBox groupContent, FontIcon iconArrow) {
        if (groupHeader == null || groupContent == null || iconArrow == null) {
            return;
        }

        groupContent.managedProperty().bind(groupContent.visibleProperty());

        groupHeader.setOnMouseClicked(event -> {
            boolean isVisible = groupContent.isVisible();

            groupContent.setVisible(!isVisible);
            iconArrow.setIconLiteral(isVisible ? "fth-chevron-right" : "fth-chevron-down");
        });
    }

    private String getGroupName(LocalDateTime createdAt) {
        if (createdAt == null) {
            return "Không rõ thời gian";
        }

        if (createdAt.toLocalDate().equals(LocalDate.now())) {
            return "Hôm nay";
        }

        return "Tháng " + createdAt.format(monthFormatter);
    }

    private String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }

        return dateTime.format(dateFormatter);
    }



    private String nullToDefault(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        return value;
    }
}