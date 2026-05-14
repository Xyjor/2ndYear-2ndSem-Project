package com.companyproject.controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class DashboardController implements Initializable {

    @FXML
    private BorderPane rootPane;

    @FXML
    private Label userLabel;

    @FXML
    private Label roleLabel;

    @FXML
    private Label pageTitleLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        showCustomerRecords();
    }

    public void setAuthenticatedUser(String fullName, String role) {
        userLabel.setText(fullName);
        roleLabel.setText(role);
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
            exception.printStackTrace();
        }
    }

    private void loadCenter(String fxmlPath, String title) {
        try {
            Node content = FXMLLoader.load(getClass().getResource(fxmlPath));
            rootPane.setCenter(content);
            pageTitleLabel.setText(title);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
