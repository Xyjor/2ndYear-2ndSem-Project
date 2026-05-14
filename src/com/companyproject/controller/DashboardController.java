package com.companyproject.controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class DashboardController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    @FXML
    private BorderPane rootPane;

    @FXML
    private Label userLabel;

    @FXML
    private Label roleLabel;

    @FXML
    private Label pageTitleLabel;

    @FXML
    private com.jfoenix.controls.JFXButton customerRecordsButton;

    @FXML
    private com.jfoenix.controls.JFXButton addCustomerButton;

    @FXML
    private com.jfoenix.controls.JFXButton vehicleRecordsButton;

    @FXML
    private com.jfoenix.controls.JFXButton addVehicleButton;

    @FXML
    private com.jfoenix.controls.JFXButton transactionsButton;

    @FXML
    private com.jfoenix.controls.JFXButton newTransactionButton;

    @FXML
    private com.jfoenix.controls.JFXButton reportsButton;

    private String userRole;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        showCustomerRecords();
    }

    public void setAuthenticatedUser(String fullName, String role) {
        userLabel.setText(fullName);
        roleLabel.setText(role);
        this.userRole = role;
        applyRoleBasedAccessControl();
    }

    private void applyRoleBasedAccessControl() {
        if ("CLERK".equals(userRole)) {
            // CLERK can view records but cannot create/edit/delete
            addCustomerButton.setDisable(true);
            addVehicleButton.setDisable(true);
            newTransactionButton.setDisable(true);
            
            // Provide tooltip explanation
            addCustomerButton.setStyle("-fx-opacity: 0.5;");
            addVehicleButton.setStyle("-fx-opacity: 0.5;");
            newTransactionButton.setStyle("-fx-opacity: 0.5;");
            
            logger.info("RBAC applied: CLERK role - limited to view-only operations");
        } else if ("MANAGER".equals(userRole)) {
            // MANAGER has full access
            addCustomerButton.setDisable(false);
            addVehicleButton.setDisable(false);
            newTransactionButton.setDisable(false);
            
            addCustomerButton.setStyle("");
            addVehicleButton.setStyle("");
            newTransactionButton.setStyle("");
            
            logger.info("RBAC applied: MANAGER role - full access");
        }
    }

    @FXML
    private void showAddCustomer() {
        loadCenter("/com/companyproject/view/AddCustomer.fxml", "Add New Customer");
    }

    @FXML
    private void showCustomerRecords() {
        loadCenter("/com/companyproject/view/CustomerList.fxml", "Customer Records");
    }

    @FXML
    private void showVehicleRecords() {
        loadCenter("/com/companyproject/view/VehicleList.fxml", "Vehicle Records");
    }

    @FXML
    private void showAddVehicle() {
        loadCenter("/com/companyproject/view/AddVehicle.fxml", "Add Vehicle");
    }

    @FXML
    private void showTransactions() {
        loadCenter("/com/companyproject/view/TransactionList.fxml", "Transactions");
    }

    @FXML
    private void showNewTransaction() {
        loadCenter("/com/companyproject/view/AddTransaction.fxml", "New Transaction");
    }

    @FXML
    private void showReports() {
        loadCenter("/com/companyproject/view/Reports.fxml", "Reports");
    }

    @FXML
    private void handleLogout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/companyproject/view/Login.fxml"));
            Stage stage = (Stage) rootPane.getScene().getWindow();
            Scene scene = new Scene(root, stage.getScene().getWidth(), stage.getScene().getHeight());
            scene.getStylesheets().add(getClass().getResource("/com/companyproject/view/style.css").toExternalForm());
            stage.setScene(scene);
        } catch (Exception exception) {
            logger.error("Failed to load Login form", exception);
        }
    }

    private void loadCenter(String fxmlPath, String title) {
        try {
            Node content = FXMLLoader.load(getClass().getResource(fxmlPath));
            rootPane.setCenter(content);
            pageTitleLabel.setText(title);
        } catch (Exception exception) {
            logger.error("Failed to load screen: {}", fxmlPath, exception);
        }
    }
}
