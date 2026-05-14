package com.companyproject.controller;

import com.companyproject.dao.CustomerDAO;
import com.companyproject.model.Customer;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;

public class AddCustomerController implements Initializable {

    private final CustomerDAO customerDAO = new CustomerDAO();

    @FXML private JFXTextField firstNameField;
    @FXML private JFXTextField middleNameField;
    @FXML private JFXTextField lastNameField;
    @FXML private JFXTextField suffixField;
    @FXML private DatePicker birthDatePicker;
    @FXML private JFXComboBox<String> genderComboBox;
    @FXML private JFXComboBox<String> civilStatusComboBox;
    @FXML private JFXTextField nationalityField;
    @FXML private JFXTextField mobileField;
    @FXML private JFXTextField alternatePhoneField;
    @FXML private JFXTextField emailField;
    @FXML private JFXTextField idTypeField;
    @FXML private JFXTextField idNumberField;
    @FXML private JFXTextField addressLine1Field;
    @FXML private JFXTextField addressLine2Field;
    @FXML private JFXTextField barangayField;
    @FXML private JFXTextField cityField;
    @FXML private JFXTextField provinceField;
    @FXML private JFXTextField postalCodeField;
    @FXML private JFXButton saveButton;
    @FXML private JFXButton clearButton;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Label statusLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        genderComboBox.getItems().setAll("Female", "Male", "Prefer not to say");
        civilStatusComboBox.getItems().setAll("Single", "Married", "Widowed", "Separated");
        nationalityField.setText("Filipino");
        progressIndicator.setVisible(false);
    }

    @FXML
    private void handleSave() {
        if (!isValid()) {
            return;
        }

        Customer customer = buildCustomer();
        Task<Customer> task = new Task<Customer>() {
            @Override
            protected Customer call() throws SQLException {
                return customerDAO.create(customer);
            }
        };

        task.setOnRunning(event -> setBusy(true));
        task.setOnSucceeded(event -> {
            setBusy(false);
            Customer saved = task.getValue();
            statusLabel.setText("Customer saved: " + saved.getDisplayName());
            clearForm();
        });
        task.setOnFailed(event -> {
            setBusy(false);
            statusLabel.setText("Save failed. Please check the database connection and duplicate IDs.");
            task.getException().printStackTrace();
        });

        Thread thread = new Thread(task, "create-customer-task");
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void clearForm() {
        firstNameField.clear();
        middleNameField.clear();
        lastNameField.clear();
        suffixField.clear();
        birthDatePicker.setValue(null);
        genderComboBox.setValue(null);
        civilStatusComboBox.setValue(null);
        nationalityField.setText("Filipino");
        mobileField.clear();
        alternatePhoneField.clear();
        emailField.clear();
        idTypeField.clear();
        idNumberField.clear();
        addressLine1Field.clear();
        addressLine2Field.clear();
        barangayField.clear();
        cityField.clear();
        provinceField.clear();
        postalCodeField.clear();
    }

    private boolean isValid() {
        if (isBlank(firstNameField.getText()) || isBlank(lastNameField.getText())) {
            statusLabel.setText("First name and last name are required.");
            return false;
        }
        if (isBlank(addressLine1Field.getText()) || isBlank(cityField.getText()) || isBlank(provinceField.getText())) {
            statusLabel.setText("Primary address, city, and province are required.");
            return false;
        }
        return true;
    }

    private Customer buildCustomer() {
        Customer customer = new Customer();
        customer.setFirstName(text(firstNameField));
        customer.setMiddleName(text(middleNameField));
        customer.setLastName(text(lastNameField));
        customer.setSuffix(text(suffixField));
        customer.setBirthDate(birthDatePicker.getValue());
        customer.setGender(genderComboBox.getValue());
        customer.setCivilStatus(civilStatusComboBox.getValue());
        customer.setNationality(text(nationalityField));
        customer.setMobileNumber(text(mobileField));
        customer.setAlternateNumber(text(alternatePhoneField));
        customer.setEmail(text(emailField));
        customer.setPrimaryIdType(text(idTypeField));
        customer.setPrimaryIdNumber(text(idNumberField));
        customer.setAddressLine1(text(addressLine1Field));
        customer.setAddressLine2(text(addressLine2Field));
        customer.setBarangay(text(barangayField));
        customer.setCity(text(cityField));
        customer.setProvince(text(provinceField));
        customer.setPostalCode(text(postalCodeField));
        return customer;
    }

    private void setBusy(boolean busy) {
        progressIndicator.setVisible(busy);
        saveButton.setDisable(busy);
        clearButton.setDisable(busy);
    }

    private static String text(JFXTextField field) {
        return field.getText() == null ? null : field.getText().trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
