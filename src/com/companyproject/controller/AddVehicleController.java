package com.companyproject.controller;

import com.companyproject.dao.CustomerDAO;
import com.companyproject.dao.VehicleDAO;
import com.companyproject.model.Customer;
import com.companyproject.model.Vehicle;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.util.StringConverter;

public class AddVehicleController implements Initializable {

    private final CustomerDAO customerDAO = new CustomerDAO();
    private final VehicleDAO vehicleDAO = new VehicleDAO();

    @FXML private ComboBox<Customer> ownerComboBox;
    @FXML private JFXTextField plateNoField;
    @FXML private JFXTextField engineNoField;
    @FXML private JFXTextField chassisNoField;
    @FXML private JFXTextField makeField;
    @FXML private JFXTextField modelField;
    @FXML private JFXTextField modelYearField;
    @FXML private JFXTextField colorField;
    @FXML private JFXButton saveButton;
    @FXML private JFXButton clearButton;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Label statusLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        progressIndicator.setVisible(false);
        ownerComboBox.setConverter(new StringConverter<Customer>() {
            @Override
            public String toString(Customer customer) {
                return customer == null ? "" : customer.getDisplayName();
            }

            @Override
            public Customer fromString(String value) {
                return null;
            }
        });
        loadCustomers();
    }

    @FXML
    private void handleSave() {
        if (!isValid()) {
            return;
        }

        Vehicle vehicle = buildVehicle();
        Task<Vehicle> task = new Task<Vehicle>() {
            @Override
            protected Vehicle call() throws SQLException {
                return vehicleDAO.create(vehicle);
            }
        };

        task.setOnRunning(event -> setBusy(true));
        task.setOnSucceeded(event -> {
            setBusy(false);
            statusLabel.setText("Vehicle saved: " + task.getValue().getDisplayName());
            clearForm();
        });
        task.setOnFailed(event -> {
            setBusy(false);
            statusLabel.setText("Save failed. Check duplicate plate, engine, or chassis numbers.");
            task.getException().printStackTrace();
        });

        Thread thread = new Thread(task, "create-vehicle-task");
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void clearForm() {
        ownerComboBox.setValue(null);
        plateNoField.clear();
        engineNoField.clear();
        chassisNoField.clear();
        makeField.clear();
        modelField.clear();
        modelYearField.clear();
        colorField.clear();
    }

    private void loadCustomers() {
        Task<List<Customer>> task = new Task<List<Customer>>() {
            @Override
            protected List<Customer> call() throws Exception {
                return customerDAO.search(null, 500, 0);
            }
        };

        task.setOnRunning(event -> setBusy(true));
        task.setOnSucceeded(event -> {
            setBusy(false);
            ownerComboBox.setItems(FXCollections.observableArrayList(task.getValue()));
            statusLabel.setText(task.getValue().isEmpty()
                    ? "Add a customer before registering a vehicle."
                    : "Ready");
        });
        task.setOnFailed(event -> {
            setBusy(false);
            statusLabel.setText("Unable to load customers.");
            task.getException().printStackTrace();
        });

        Thread thread = new Thread(task, "vehicle-owner-load-task");
        thread.setDaemon(true);
        thread.start();
    }

    private boolean isValid() {
        if (ownerComboBox.getValue() == null) {
            statusLabel.setText("Select the vehicle owner.");
            return false;
        }
        if (isBlank(engineNoField.getText()) || isBlank(chassisNoField.getText())
                || isBlank(makeField.getText()) || isBlank(modelField.getText())) {
            statusLabel.setText("Engine number, chassis number, make, and model are required.");
            return false;
        }
        if (!isBlank(modelYearField.getText())) {
            try {
                Integer.parseInt(modelYearField.getText().trim());
            } catch (NumberFormatException exception) {
                statusLabel.setText("Model year must be a number.");
                return false;
            }
        }
        return true;
    }

    private Vehicle buildVehicle() {
        Vehicle vehicle = new Vehicle();
        vehicle.setCustomerId(ownerComboBox.getValue().getCustomerId());
        vehicle.setCustomerName(ownerComboBox.getValue().getDisplayName());
        vehicle.setPlateNo(text(plateNoField));
        vehicle.setEngineNo(text(engineNoField));
        vehicle.setChassisNo(text(chassisNoField));
        vehicle.setMake(text(makeField));
        vehicle.setModel(text(modelField));
        vehicle.setModelYear(isBlank(modelYearField.getText()) ? null : Integer.valueOf(modelYearField.getText().trim()));
        vehicle.setColor(text(colorField));
        return vehicle;
    }

    private void setBusy(boolean busy) {
        progressIndicator.setVisible(busy);
        saveButton.setDisable(busy);
        clearButton.setDisable(busy);
        ownerComboBox.setDisable(busy);
    }

    private static String text(JFXTextField field) {
        return field.getText() == null ? null : field.getText().trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
