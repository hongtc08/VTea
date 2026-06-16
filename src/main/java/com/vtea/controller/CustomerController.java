package com.vtea.controller;

import com.vtea.dto.CustomerDTO;
import com.vtea.dto.CustomerStatsDTO;
import com.vtea.service.CustomerService;
import com.vtea.utils.FormatUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.stream.Collectors;

public class CustomerController {

    @FXML private Label lblTotalCustomers;
    @FXML private Label lblDiamondCount;
    @FXML private Label lblGoldCount;
    @FXML private Label lblAvgPoints;

    @FXML private TextField searchField;
    @FXML private Button btnFilterAll;
    @FXML private Button btnFilterDong;
    @FXML private Button btnFilterBac;
    @FXML private Button btnFilterVang;
    @FXML private Button btnFilterKimCuong;

    @FXML private VBox vboxCustomerList;
    @FXML private VBox vboxLoading;

    private final CustomerService customerService = new CustomerService();
    private List<CustomerDTO> allCustomers;
    private int currentTierId = -1;

    @FXML
    public void initialize() {
        btnFilterAll.getStyleClass().add("pill-btn-active");
        setupFilters();
        loadData();
    }

    private void setupFilters() {
        searchField.textProperty().addListener((obs, old, newVal) -> filterData());

        btnFilterAll.setOnAction(e -> setFilter(-1, btnFilterAll));
        btnFilterDong.setOnAction(e -> setFilter(1, btnFilterDong));
        btnFilterBac.setOnAction(e -> setFilter(2, btnFilterBac));
        btnFilterVang.setOnAction(e -> setFilter(3, btnFilterVang));
        btnFilterKimCuong.setOnAction(e -> setFilter(4, btnFilterKimCuong));
    }

    private void setFilter(int tierId, Button activeBtn) {
        currentTierId = tierId;
        
        // Reset styles
        Button[] btns = {btnFilterAll, btnFilterDong, btnFilterBac, btnFilterVang, btnFilterKimCuong};
        for (Button b : btns) {
            b.getStyleClass().remove("pill-btn-active");
        }
        activeBtn.getStyleClass().add("pill-btn-active");
        
        filterData();
    }

    public void loadData() {
        vboxLoading.setVisible(true);
        
        new Thread(() -> {
            try {
                CustomerStatsDTO stats = customerService.getCustomerStatistics();
                List<CustomerDTO> customers = customerService.getAllCustomers();
                
                javafx.application.Platform.runLater(() -> {
                    // Update stats
                    lblTotalCustomers.setText(String.valueOf(stats.getTotalCustomers()));
                    lblDiamondCount.setText(String.valueOf(stats.getDiamondCount()));
                    lblGoldCount.setText(String.valueOf(stats.getGoldCount()));
                    lblAvgPoints.setText(FormatUtils.formatNumber(stats.getAvgPoints()));
                    
                    allCustomers = customers;
                    filterData();
                    vboxLoading.setVisible(false);
                });
            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    vboxLoading.setVisible(false);
                });
            }
        }).start();
    }

    private void filterData() {
        if (allCustomers == null) return;

        String searchTxt = searchField.getText() == null ? "" : searchField.getText().toLowerCase().trim();

        List<CustomerDTO> filtered = allCustomers.stream().filter(c -> {
            boolean matchSearch = searchTxt.isEmpty() || 
                                  (c.getFullName() != null && removeAccents(c.getFullName().toLowerCase()).contains(removeAccents(searchTxt))) ||
                                  (c.getPhoneNumber() != null && c.getPhoneNumber().contains(searchTxt));
                                  
            boolean matchTier = (currentTierId == -1) || (c.getTierId() == currentTierId);
                                
            return matchSearch && matchTier;
        }).collect(Collectors.toList());

        renderList(filtered);
    }

    private String removeAccents(String text) {
        if (text == null) return "";
        String normalized = java.text.Normalizer.normalize(text, java.text.Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "").replace('đ', 'd').replace('Đ', 'D');
    }

    private void renderList(List<CustomerDTO> list) {
        vboxCustomerList.getChildren().clear();
        try {
            for (CustomerDTO c : list) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vtea/view/CustomerRow.fxml"));
                Parent row = loader.load();
                
                CustomerRowController controller = loader.getController();
                controller.setData(c, this);
                
                vboxCustomerList.getChildren().add(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
            javafx.application.Platform.runLater(() -> {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                alert.setTitle("Error Rendering List");
                alert.setContentText(e.toString() + "\n" + (e.getCause() != null ? e.getCause().toString() : ""));
                alert.show();
            });
        }
    }
}
