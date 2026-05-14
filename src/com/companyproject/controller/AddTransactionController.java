package com.companyproject.controller;

import com.companyproject.dao.CustomerDAO;
import com.companyproject.dao.TransactionDAO;
import com.companyproject.dao.VehicleDAO;
import com.companyproject.model.Customer;
import com.companyproject.model.ServiceTransaction;
import com.companyproject.model.Vehicle;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.util.StringConverter;

public class AddTransactionController implements Initializable {

    private static final DateTimeFormatter TRANSACTION_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final CustomerDAO customerDAO = new CustomerDAO();
    private final VehicleDAO vehicleDAO = new VehicleDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();

    @FXML private JFXTextField transactionNoField;
    @FXML private ComboBox<Customer> customerComboBox;
    @FXML private ComboBox<Vehicle> vehicleComboBox;
    @FXML private ComboBox<String> serviceComboBox;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private JFXTextField amountField;
    @FXML private TextArea remarksArea;
    @FXML private JFXButton saveButton;
    @FXML private JFXButton clearButton;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Label statusLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        progressIndicator.setVisible(false);
        transactionNoField.setText(generateTransactionNo());
        configureComboBoxes();
        loadLookups();
    }

    @FXML
    private void handleSave() {
        if (!isValid()) {
            return;
        }

        ServiceTransaction transaction = buildTransaction();
        Task<ServiceTransaction> task = new Task<ServiceTransaction>() {
            @Override
            protected ServiceTransaction call() throws SQLException {
                return transactionDAO.create(transaction);
            }
        };

        task.setOnRunning(event -> setBusy(true));
        task.setOnSucceeded(event -> {
            setBusy(false);
            statusLabel.setText("Transaction saved: " + task.getValue().getTransactionNo());
            clearForm();
        });
        task.setOnFailed(event -> {
            setBusy(false);
            statusLabel.setText("Save failed. Check transaction number and required fields.");
            task.getException().printStackTrace();
        });

        Thread thread = new Thread(task, "create-transaction-task");
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void clearForm() {
        transactionNoField.setText(generateTransactionNo());
        customerComboBox.setValue(null);
        vehicleComboBox.setValue(null);
        serviceComboBox.setValue(null);
        statusComboBox.setValue("PENDING");
        amountField.setText("0.00");
        remarksArea.clear();
    }

    private void configureComboBoxes() {
        serviceComboBox.getItems().setAll("DRIVERS_LICENSE", "STUDENT_PERMIT", "CAR_INSURANCE", "VEHICLE_REGISTRATION");
        statusComboBox.getItems().setAll("DRAFT", "PENDING", "PROCESSING");
        statusComboBox.setValue("PENDING");
        amountField.setText("0.00");

        customerComboBox.setConverter(new StringConverter<Customer>() {
            @Override
            public String toString(Customer customer) {
                return customer == null ? "" : customer.getDisplayName();
            }

            @Override
            public Customer fromString(String value) {
                return null;
            }
        });

        vehicleComboBox.setConverter(new StringConverter<Vehicle>() {
            @Override
            public String toString(Vehicle vehicle) {
                return vehicle == null ? "" : vehicle.getDisplayName();
            }

            @Override
            public Vehicle fromString(String value) {
                return null;
            }
        });
    }

    private void loadLookups() {
        Task<LookupData> task = new Task<LookupData>() {
            @Override
            protected LookupData call() throws Exception {
                return new LookupData(customerDAO.search(null, 500, 0), vehicleDAO.search(null, 500, 0));
            }
        };

        task.setOnRunning(event -> setBusy(true));
        task.setOnSucceeded(event -> {
            setBusy(false);
            customerComboBox.setItems(FXCollections.observableArrayList(task.getValue().customers));
            vehicleComboBox.setItems(FXCollections.observableArrayList(task.getValue().vehicles));
            statusLabel.setText(task.getValue().customers.isEmpty()
                    ? "Add a customer before creating a transaction."
                    : "Ready");
        });
        task.setOnFailed(event -> {
            setBusy(false);
            statusLabel.setText("Unable to load customers and vehicles.");
            task.getException().printStackTrace();
        });

        Thread thread = new Thread(task, "transaction-lookup-task");
        thread.setDaemon(true);
        thread.start();
    }

    private boolean isValid() {
        if (isBlank(transactionNoField.getText()) || customerComboBox.getValue() == null || serviceComboBox.getValue() == null) {
            statusLabel.setText("Transaction number, customer, and service are required.");
            return false;
        }
        if (requiresVehicle(serviceComboBox.getValue()) && vehicleComboBox.getValue() == null) {
            statusLabel.setText("Vehicle is required for insurance and OR/CR transactions.");
            return false;
        }
        try {
            new BigDecimal(amountField.getText().trim());
        } catch (Exception exception) {
            statusLabel.setText("Amount must be a valid number.");
            return false;
        }
        return true;
    }

    private ServiceTransaction buildTransaction() {
        ServiceTransaction transaction = new ServiceTransaction();
        transaction.setTransactionNo(transactionNoField.getText().trim());
        transaction.setCustomerId(customerComboBox.getValue().getCustomerId());
        transaction.setCustomerName(customerComboBox.getValue().getDisplayName());
        transaction.setVehicleId(vehicleComboBox.getValue() == null ? null : vehicleComboBox.getValue().getVehicleId());
        transaction.setVehicleName(vehicleComboBox.getValue() == null ? null : vehicleComboBox.getValue().getDisplayName());
        transaction.setServiceType(serviceComboBox.getValue());
        transaction.setStatus(statusComboBox.getValue());
        transaction.setAmount(new BigDecimal(amountField.getText().trim()));
        transaction.setRemarks(remarksArea.getText());
        return transaction;
    }

    private void setBusy(boolean busy) {
        progressIndicator.setVisible(busy);
        saveButton.setDisable(busy);
        clearButton.setDisable(busy);
        customerComboBox.setDisable(busy);
        vehicleComboBox.setDisable(busy);
        serviceComboBox.setDisable(busy);
        statusComboBox.setDisable(busy);
    }

    private static boolean requiresVehicle(String serviceType) {
        return "CAR_INSURANCE".equals(serviceType) || "VEHICLE_REGISTRATION".equals(serviceType);
    }

    private static String generateTransactionNo() {
        int suffix = Math.abs((int) (System.nanoTime() % 10_000));
        return "TX-" + TRANSACTION_FORMAT.format(LocalDateTime.now()) + "-" + String.format("%04d", suffix);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static final class LookupData {
        private final List<Customer> customers;
        private final List<Vehicle> vehicles;

        private LookupData(List<Customer> customers, List<Vehicle> vehicles) {
            this.customers = customers;
            this.vehicles = vehicles;
        }
    }
}
