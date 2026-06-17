package com.vtea.controller;

import com.vtea.main.MainApp;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.layout.StackPane;
import com.vtea.utils.DialogHelper;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

public class MainLayoutController {

    @FXML
    private StackPane contentArea;

    @FXML private javafx.scene.control.Label userInitialLabel;
    @FXML private javafx.scene.control.Label userNameLabel;
    @FXML private javafx.scene.control.Label userRoleLabel;
    @FXML private FontIcon adminCrownIcon;

    @FXML private javafx.scene.control.Button btnDashboard;
    @FXML private javafx.scene.control.Button btnPOS;
    @FXML private javafx.scene.control.Button btnMenu;
    @FXML private javafx.scene.control.Button btnInventory;
    @FXML private javafx.scene.control.Button btnEmployee;
    @FXML private javafx.scene.control.Button btnCustomer;
    @FXML private javafx.scene.control.Button btnInvoice;
    @FXML private javafx.scene.control.Button btnVoucher;
    @FXML private javafx.scene.control.Button btnReport;

    private void applyRoleBasedAccessControl() {
        if (!com.vtea.utils.SessionManager.isAdmin()) {
            // Hide admin-only buttons for Staff (Menu, Employee, Report, Customer)
            if (btnMenu != null) {
                btnMenu.setVisible(false);
                btnMenu.setManaged(false);
            }
            if (btnEmployee != null) {
                btnEmployee.setVisible(false);
                btnEmployee.setManaged(false);
            }
            if (btnReport != null) {
                btnReport.setVisible(false);
                btnReport.setManaged(false);
            }
            if (btnCustomer != null) {
                btnCustomer.setVisible(false);
                btnCustomer.setManaged(false);
            }
            // Voucher cũng là chức năng quản lý – chỉ Admin mới thấy
            if (btnVoucher != null) {
                btnVoucher.setVisible(false);
                btnVoucher.setManaged(false);
            }
            if (adminCrownIcon != null) {
                adminCrownIcon.setVisible(false);
            }
        } else {
            if (adminCrownIcon != null) {
                adminCrownIcon.setVisible(true);
            }
        }
    }

    private void setActiveNav(javafx.scene.control.Button activeBtn) {
        javafx.scene.control.Button[] allBtns = {btnDashboard, btnPOS, btnMenu, btnInventory, btnEmployee, btnCustomer, btnInvoice, btnVoucher, btnReport};
        for (javafx.scene.control.Button b : allBtns) {
            if (b != null) b.getStyleClass().remove("nav-btn-active");
        }
        if (activeBtn != null) activeBtn.getStyleClass().add("nav-btn-active");
    }

    @FXML
    public void initialize() {
        // Cập nhật thông tin người dùng đang đăng nhập
        com.vtea.dto.UserSessionDTO user = com.vtea.utils.SessionManager.getCurrentUser();
        if (user != null) {
            userNameLabel.setText(user.getFullName());
            // Format role (ADMIN -> Quản lý, STAFF -> Nhân viên)
            String roleText = "ADMIN".equalsIgnoreCase(user.getRole()) ? "Quản lý" : "Nhân viên";
            userRoleLabel.setText(roleText);
            
            if (user.getFullName() != null && !user.getFullName().isEmpty()) {
                userInitialLabel.setText(user.getFullName().substring(0, 1).toUpperCase());
            }
        }

        applyRoleBasedAccessControl();

        // Mặc định load màn hình dashboard đầu tiên
        loadView("dashboard");
        setActiveNav(btnDashboard);

        contentArea.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, event -> {
                    if (event.getCode() == javafx.scene.input.KeyCode.F1) {
                        handlePOS(null);
                        event.consume();
                    }
                });
            }
        });
    }

    @FXML
    private void handleDashboard(ActionEvent event) {
        loadView("dashboard");
        setActiveNav(btnDashboard);
    }

    @FXML
    private void handlePOS(ActionEvent event) {
        loadView("pos");
        setActiveNav(btnPOS);
    }

    // Thêm hàm xử lý nút Thực đơn
    @FXML
    private void handleMenu(ActionEvent event) {
        loadView("menu");
        setActiveNav(btnMenu);
    }

    @FXML
    public void handleInventory(ActionEvent event) {
        loadView("inventory");
        setActiveNav(btnInventory);
    }

    @FXML
    public void handleEmployee(ActionEvent event) {
        loadView("employee");
        setActiveNav(btnEmployee);
    }

    @FXML
    public void handleCustomer(ActionEvent event) {
        loadView("customer");
        setActiveNav(btnCustomer);
    }

    @FXML
    public void handleInvoiceHistory(ActionEvent actionEvent) {
        loadView("invoiceHistory");
        setActiveNav(btnInvoice);
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        // Trở về màn hình đăng nhập
        MainApp.setRoot("login");
    }

    @FXML
    public void handleVoucher(ActionEvent event) {
        loadView("voucher");
        setActiveNav(btnVoucher);
    }

    @FXML
    public void handleReport(ActionEvent actionEvent) {
        loadView("report");
        setActiveNav(btnReport);
    }

    // Hàm load màn hình con siêu "xịn" giúp bạn phát hiện mọi lỗi
    private void loadView(String fxml) {
        try {
            // 1. Kiểm tra xem có tìm thấy file FXML không
            URL fileUrl = MainApp.class.getResource("/com/vtea/view/" + fxml + ".fxml");

            if (fileUrl == null) {
                // Nếu đường dẫn bị sai, hiển thị thông báo ngay
                DialogHelper.showInfo("Lỗi Đường Dẫn", "Không tìm thấy file: " + fxml + ".fxml\nHãy kiểm tra lại thư mục /com/vtea/view/");
                return;
            }

            // Hiển thị hiệu ứng loading nhỏ nhắn mượt mà
            javafx.scene.control.ProgressIndicator spinner = new javafx.scene.control.ProgressIndicator();
            spinner.setMaxSize(40, 40);
            
            StackPane loadingPane = new StackPane(spinner);
            contentArea.getChildren().clear();
            contentArea.getChildren().add(loadingPane);

            // Load giao diện ở background thread để vòng xoay không bị "đứng hình"
            javafx.concurrent.Task<Parent> loadTask = new javafx.concurrent.Task<Parent>() {
                @Override
                protected Parent call() throws Exception {
                    FXMLLoader loader = new FXMLLoader(fileUrl);
                    return loader.load();
                }
            };

            loadTask.setOnSucceeded(e -> {
                Parent view = loadTask.getValue();
                // Thêm hiệu ứng Fade In để UX mượt mà hơn
                view.setOpacity(0);
                FadeTransition fadeIn = new FadeTransition(Duration.millis(350), view);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);
                
                javafx.animation.TranslateTransition slideUp = new javafx.animation.TranslateTransition(Duration.millis(350), view);
                slideUp.setFromY(20);
                slideUp.setToY(0);
                
                javafx.animation.ParallelTransition pt = new javafx.animation.ParallelTransition(fadeIn, slideUp);
                pt.play();

                // Đưa vào màn hình chính
                contentArea.getChildren().clear();
                contentArea.getChildren().add(view);
            });

            loadTask.setOnFailed(e -> {
                Throwable ex = loadTask.getException();
                ex.printStackTrace();
                DialogHelper.showInfo("Lỗi Load FXML", "Có lỗi xảy ra khi tải giao diện: " + ex.getMessage());
            });

            Thread thread = new Thread(loadTask);
            thread.setDaemon(true);
            thread.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}