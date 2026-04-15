/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

import dao.DriversLicenseDAO;
import model.DriversLicense;
import java.sql.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.ComboBox;
import util.InputValidators;

/**
 *
 * @author Xyjor
 */
public class DriversLicenseViewController {

    private static final Logger LOGGER = Logger.getLogger(DriversLicenseViewController.class.getName());

    @FXML
    private TextField txtSearch;
    @FXML
    private TableView<DriversLicense> tvLicenses;
    @FXML
    private TableColumn<DriversLicense, String> colLicenseNum;
    @FXML
    private TableColumn<DriversLicense, Integer> colCustomerId;
    @FXML
    private TableColumn<DriversLicense, String> colLicenseType;
    @FXML
    private TableColumn<DriversLicense, String> colRestrictions;
    @FXML
    private TableColumn<DriversLicense, Date> colIssueDate;
    @FXML
    private TableColumn<DriversLicense, Date> colExpiryDate;

    @FXML
    private TextField txtLicenseNum;
    @FXML
    private TextField txtCustomerId;
    @FXML
    private ComboBox<String> cmbLicenseType;
    @FXML
    private TextField txtRestrictions;
    @FXML
    private DatePicker dpIssueDate;
    @FXML
    private DatePicker dpExpiryDate;

    private DriversLicenseDAO licenseDAO = new DriversLicenseDAO();
    private ObservableList<DriversLicense> licenseList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        loadLicenses();
        setupSearchFilter();
        cmbLicenseType.setItems(FXCollections.observableArrayList("Non-Professional", "Professional"));
        cmbLicenseType.setPromptText("Type of License");
    }

    private void setupTable() {
        colLicenseNum.setCellValueFactory(new PropertyValueFactory<>("licenseNumber"));
        colCustomerId.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        colLicenseType.setCellValueFactory(new PropertyValueFactory<>("licenseType"));
        colRestrictions.setCellValueFactory(new PropertyValueFactory<>("restrictions"));
        colIssueDate.setCellValueFactory(new PropertyValueFactory<>("issueDate"));
        colExpiryDate.setCellValueFactory(new PropertyValueFactory<>("expiryDate"));
    }

    private void loadLicenses() {
        licenseList.clear();
        try {
            licenseList.addAll(licenseDAO.getAllDriversLicenses());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error loading driver's licenses.", e);
        }
    }

    private void setupSearchFilter() {
        if (txtSearch == null) {
            tvLicenses.setItems(licenseList);
            return;
        }

        FilteredList<DriversLicense> filteredData = new FilteredList<>(licenseList, b -> true);

        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(license -> {
                if (newValue == null || newValue.trim().isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase().trim();

                if (license.getLicenseNumber() != null && license.getLicenseNumber().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (String.valueOf(license.getCustomerId()).contains(lowerCaseFilter)) {
                    return true;
                } else if (license.getLicenseType() != null && license.getLicenseType().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (license.getRestrictions() != null && license.getRestrictions().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (license.getIssueDate() != null && license.getIssueDate().toString().contains(lowerCaseFilter)) {
                    return true;
                } else if (license.getExpiryDate() != null && license.getExpiryDate().toString().contains(lowerCaseFilter)) {
                    return true;
                }

                return false;
            });
        });

        SortedList<DriversLicense> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tvLicenses.comparatorProperty());
        tvLicenses.setItems(sortedData);
    }

    @FXML
    private void handleTableClick(MouseEvent event) {
        DriversLicense selectedLicense = tvLicenses.getSelectionModel().getSelectedItem();
        if (selectedLicense != null) {
            txtLicenseNum.setText(selectedLicense.getLicenseNumber());
            txtLicenseNum.setDisable(true); // Lock Primary Key
            txtCustomerId.setText(String.valueOf(selectedLicense.getCustomerId()));
            cmbLicenseType.setValue(selectedLicense.getLicenseType());
            txtRestrictions.setText(selectedLicense.getRestrictions());
            dpIssueDate.setValue(selectedLicense.getIssueDate().toLocalDate());
            dpExpiryDate.setValue(selectedLicense.getExpiryDate().toLocalDate());
        }
    }

    @FXML
    private void handleSaveLicense(ActionEvent event) {
        try {
            if (InputValidators.isBlank(txtLicenseNum.getText()) || InputValidators.isBlank(txtCustomerId.getText())
                    || cmbLicenseType.getValue() == null || dpIssueDate.getValue() == null || dpExpiryDate.getValue() == null) {
                AlertUtils.show("Validation Error", "Please fill in all required fields.", AlertType.WARNING);
                return;
            }
            if (!InputValidators.hasValidDateRange(dpIssueDate.getValue(), dpExpiryDate.getValue())) {
                AlertUtils.show("Validation Error", "Expiry Date cannot be before Issue Date.", AlertType.WARNING);
                return;
            }

            int customerId = Integer.parseInt(txtCustomerId.getText());
            if (customerId <= 0) {
                AlertUtils.show("Validation Error", "Customer ID must be a valid positive number.", AlertType.WARNING);
                return;
            }

            if (licenseDAO.hasExistingLicense(customerId)) {
                AlertUtils.show("Duplicate Error", "Customer ID " + customerId + " already has a Driver's License!", AlertType.ERROR);
                return;
            }

            DriversLicense newLicense = new DriversLicense(
                    txtLicenseNum.getText().trim(), customerId, cmbLicenseType.getValue(),
                    txtRestrictions.getText() == null ? "" : txtRestrictions.getText().trim(), Date.valueOf(dpIssueDate.getValue()), Date.valueOf(dpExpiryDate.getValue())
            );

            if (licenseDAO.addDriversLicense(newLicense)) {
                AlertUtils.show("Success", "License saved successfully!", AlertType.INFORMATION);
                loadLicenses();
                handleClearForm(null);
            } else {
                AlertUtils.show("Database Error", "Failed to save. Check if Customer ID exists.", AlertType.ERROR);
            }
        } catch (NumberFormatException e) {
            AlertUtils.show("Input Error", "Customer ID must be a valid number.", AlertType.ERROR);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error saving driver's license.", e);
            AlertUtils.show("System Error", "An error occurred while saving license.", AlertType.ERROR);
        }
    }

    @FXML
    private void handleUpdateLicense(ActionEvent event) {
        if (txtLicenseNum.getText().isEmpty()) {
            AlertUtils.show("Warning", "Please select a license to update.", AlertType.WARNING);
            return;
        }
        if (InputValidators.isBlank(txtCustomerId.getText()) || cmbLicenseType.getValue() == null || dpIssueDate.getValue() == null || dpExpiryDate.getValue() == null) {
            AlertUtils.show("Validation Error", "Customer ID, license type, issue date and expiry date are required.", AlertType.WARNING);
            return;
        }
        if (!InputValidators.hasValidDateRange(dpIssueDate.getValue(), dpExpiryDate.getValue())) {
            AlertUtils.show("Validation Error", "Expiry Date cannot be before Issue Date.", AlertType.WARNING);
            return;
        }
        try {
            int customerId = Integer.parseInt(txtCustomerId.getText());
            if (customerId <= 0) {
                AlertUtils.show("Validation Error", "Customer ID must be a valid positive number.", AlertType.WARNING);
                return;
            }
            DriversLicense updatedLicense = new DriversLicense(
                    txtLicenseNum.getText(), customerId,
                    cmbLicenseType.getValue(), txtRestrictions.getText() == null ? "" : txtRestrictions.getText().trim(),
                    Date.valueOf(dpIssueDate.getValue()), Date.valueOf(dpExpiryDate.getValue())
            );

            if (licenseDAO.updateDriversLicense(updatedLicense)) {
                AlertUtils.show("Success", "License updated successfully!", AlertType.INFORMATION);
                loadLicenses();
                handleClearForm(null);
            } else {
                AlertUtils.show("Error", "Failed to update license.", AlertType.ERROR);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error updating driver's license.", e);
            AlertUtils.show("Error", "Check your inputs and try again.", AlertType.ERROR);
        }
    }

    @FXML
    private void handleDeleteLicense(ActionEvent event) {
        String licenseNum = txtLicenseNum.getText();
        if (licenseNum.isEmpty()) {
            AlertUtils.show("Warning", "Please select a license to delete.", AlertType.WARNING);
            return;
        }
        if (!AlertUtils.confirm("Delete Driver's License", "Delete selected license record?")) {
            return;
        }
        if (licenseDAO.deleteDriversLicense(licenseNum)) {
            AlertUtils.show("Success", "License deleted successfully!", AlertType.INFORMATION);
            loadLicenses();
            handleClearForm(null);
        } else {
            AlertUtils.show("Error", "Failed to delete license.", AlertType.ERROR);
        }
    }

    @FXML
    private void handleClearForm(ActionEvent event) {
        txtLicenseNum.clear();
        txtLicenseNum.setDisable(false);
        txtCustomerId.clear();
        cmbLicenseType.setValue(null);
        txtRestrictions.clear();
        dpIssueDate.setValue(null);
        dpExpiryDate.setValue(null);
    }
}
