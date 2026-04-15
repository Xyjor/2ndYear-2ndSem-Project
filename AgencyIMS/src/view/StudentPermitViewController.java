/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;


import dao.StudentPermitDAO;
import model.StudentPermit;
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
public class StudentPermitViewController {

    private static final Logger LOGGER = Logger.getLogger(StudentPermitViewController.class.getName());

    @FXML
    private TextField txtSearch;
    @FXML
    private TableView<StudentPermit> tvPermits;
    @FXML
    private TableColumn<StudentPermit, String> colPermitNum;
    @FXML
    private TableColumn<StudentPermit, Integer> colCustomerId;
    @FXML
    private TableColumn<StudentPermit, Date> colIssueDate;
    @FXML
    private TableColumn<StudentPermit, Date> colExpiryDate;
    @FXML
    private TableColumn<StudentPermit, String> colStatus;
    @FXML
    private TextField txtPermitNum;
    @FXML
    private TextField txtCustomerId;
    @FXML
    private DatePicker dpIssueDate;
    @FXML
    private DatePicker dpExpiryDate;
    @FXML
    private ComboBox<String> cmbStatus;

    private final StudentPermitDAO permitDAO = new StudentPermitDAO();
    private ObservableList<StudentPermit> permitList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Set the valid ENUM options for your database!
        cmbStatus.setItems(FXCollections.observableArrayList("Active", "Expired", "Revoked"));
        cmbStatus.setPromptText("Select Status");

        setupTable();
        loadPermits();
        setupSearchFilter();

        setupValidationListeners();
    }

    private void setupTable() {
        colPermitNum.setCellValueFactory(new PropertyValueFactory<>("permitNumber"));
        colCustomerId.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        colIssueDate.setCellValueFactory(new PropertyValueFactory<>("issueDate"));
        colExpiryDate.setCellValueFactory(new PropertyValueFactory<>("expiryDate"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void loadPermits() {
        permitList.clear();
        try {
            permitList.addAll(permitDAO.getAllStudentPermits());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error loading student permits.", e);
        }
    }

    private void setupSearchFilter() {
        if (txtSearch == null) {
            tvPermits.setItems(permitList);
            return;
        }

        FilteredList<StudentPermit> filteredData = new FilteredList<>(permitList, b -> true);

        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(permit -> {
                if (newValue == null || newValue.trim().isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase().trim();

                if (permit.getPermitNumber() != null && permit.getPermitNumber().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (String.valueOf(permit.getCustomerId()).contains(lowerCaseFilter)) {
                    return true;
                } else if (permit.getStatus() != null && permit.getStatus().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (permit.getIssueDate() != null && permit.getIssueDate().toString().contains(lowerCaseFilter)) {
                    return true;
                } else if (permit.getExpiryDate() != null && permit.getExpiryDate().toString().contains(lowerCaseFilter)) {
                    return true;
                }

                return false;
            });
        });

        SortedList<StudentPermit> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tvPermits.comparatorProperty());
        tvPermits.setItems(sortedData);
    }

    @FXML
    private void handleSavePermit(ActionEvent event) {
        try {
            clearErrorStyles(); // Always start with a clean slate!
            boolean hasError = false;

            // 1. Check Permit Number
            if (InputValidators.isBlank(txtPermitNum.getText())) {
                if (!txtPermitNum.getStyleClass().contains("error-field")) {
                    txtPermitNum.getStyleClass().add("error-field");
                }
                hasError = true;
            }

            // 2. Check Customer ID
            if (InputValidators.isBlank(txtCustomerId.getText())) {
                if (!txtCustomerId.getStyleClass().contains("error-field")) {
                    txtCustomerId.getStyleClass().add("error-field");
                }
                hasError = true;
            }

            // 3. Check Status Dropdown
            if (cmbStatus.getValue() == null || InputValidators.isBlank(cmbStatus.getValue().toString())) {
                if (!cmbStatus.getStyleClass().contains("error-field")) {
                    cmbStatus.getStyleClass().add("error-field");
                }
                hasError = true;
            }

            // 4. Check Issue Date
            if (dpIssueDate.getValue() == null) {
                if (!dpIssueDate.getStyleClass().contains("error-field")) {
                    dpIssueDate.getStyleClass().add("error-field");
                }
                hasError = true;
            }

            // 5. Check Expiry Date
            if (dpExpiryDate.getValue() == null) {
                if (!dpExpiryDate.getStyleClass().contains("error-field")) {
                    dpExpiryDate.getStyleClass().add("error-field");
                }
                hasError = true;
            }

            // Stop here if ANY field is empty!
            if (hasError) {
                AlertUtils.show("Validation Error", "Please fill in all highlighted fields.", AlertType.WARNING);
                return;
            }

            // 6. Safe Parsing for Customer ID (Make sure they typed a real number!)
            int customerId;
            try {
                customerId = Integer.parseInt(txtCustomerId.getText().trim());
                if (customerId <= 0) {
                    throw new NumberFormatException("Customer ID must be positive.");
                }
            } catch (NumberFormatException e) {
                txtCustomerId.getStyleClass().add("error-field");
                AlertUtils.show("Validation Error", "Customer ID must be a valid positive number.", AlertType.WARNING);
                return;
            }

            if (!InputValidators.hasValidDateRange(dpIssueDate.getValue(), dpExpiryDate.getValue())) {
                dpIssueDate.getStyleClass().add("error-field");
                dpExpiryDate.getStyleClass().add("error-field");
                AlertUtils.show("Validation Error", "Expiry Date cannot be before Issue Date.", AlertType.WARNING);
                return;
            }

            // 7. Create the Permit Object
            StudentPermit newPermit = new StudentPermit(
                    txtPermitNum.getText().trim(),
                    customerId,
                    java.sql.Date.valueOf(dpIssueDate.getValue()),
                    java.sql.Date.valueOf(dpExpiryDate.getValue()),
                    cmbStatus.getValue().toString()
            );

            // 8. Save to Database
            if (permitDAO.addStudentPermit(newPermit)) {
                AlertUtils.show("Success", "Student Permit added successfully!", AlertType.INFORMATION);
                loadPermits(); // Refresh the table
                handleClearForm(null); // Wipe the boxes clean
            } else {
                // If it fails here, it's usually because the CustomerID doesn't exist in the Customers table!
                AlertUtils.show("Database Error", "Failed to add Permit. Ensure the Customer ID exists first!", AlertType.ERROR);
                txtCustomerId.getStyleClass().add("error-field");
            }

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error saving student permit.", e);
            AlertUtils.show("System Error", "An unexpected error occurred while saving permit.", AlertType.ERROR);
        }
    }

    @FXML
    private void handleTableClick(MouseEvent event) {
        StudentPermit selectedPermit = tvPermits.getSelectionModel().getSelectedItem();

        if (selectedPermit != null) {
            txtPermitNum.setText(selectedPermit.getPermitNumber());
            txtPermitNum.setDisable(true); // Lock the Permit Number so they don't change the Primary Key!

            txtCustomerId.setText(String.valueOf(selectedPermit.getCustomerId()));
            cmbStatus.setValue(selectedPermit.getStatus());

            // Convert java.sql.Date back to LocalDate for the DatePicker
            dpIssueDate.setValue(selectedPermit.getIssueDate().toLocalDate());
            dpExpiryDate.setValue(selectedPermit.getExpiryDate().toLocalDate());
        }
    }

    // --- Update Button Logic ---
    @FXML
    private void handleUpdatePermit(ActionEvent event) {
        if (txtPermitNum.getText().isEmpty()) {
            AlertUtils.show("Warning", "Please select a permit from the table to update.", AlertType.WARNING);
            return;
        }
        if (InputValidators.isBlank(txtCustomerId.getText()) || cmbStatus.getValue() == null || dpIssueDate.getValue() == null || dpExpiryDate.getValue() == null) {
            AlertUtils.show("Validation Error", "Customer ID, status, issue date, and expiry date are required.", AlertType.WARNING);
            return;
        }
        if (!InputValidators.hasValidDateRange(dpIssueDate.getValue(), dpExpiryDate.getValue())) {
            AlertUtils.show("Validation Error", "Expiry Date cannot be before Issue Date.", AlertType.WARNING);
            return;
        }

        try {
            String permitNum = txtPermitNum.getText();
            int customerId = Integer.parseInt(txtCustomerId.getText());
            if (customerId <= 0) {
                AlertUtils.show("Validation Error", "Customer ID must be a valid positive number.", AlertType.WARNING);
                return;
            }
            Date issueDate = Date.valueOf(dpIssueDate.getValue());
            Date expiryDate = Date.valueOf(dpExpiryDate.getValue());
            String status = cmbStatus.getValue();

            StudentPermit updatedPermit = new StudentPermit(permitNum, customerId, issueDate, expiryDate, status);

            if (permitDAO.updateStudentPermit(updatedPermit)) {
                AlertUtils.show("Success", "Permit updated successfully!", AlertType.INFORMATION);
                loadPermits();
                handleClearForm(null); // Clear form and unlock ID
            } else {
                AlertUtils.show("Error", "Failed to update permit.", AlertType.ERROR);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error updating student permit.", e);
            AlertUtils.show("Error", "Check your inputs and try again.", AlertType.ERROR);
        }
    }

    // --- Delete Button Logic ---
    @FXML
    private void handleDeletePermit(ActionEvent event) {
        String permitNum = txtPermitNum.getText();

        if (permitNum.isEmpty()) {
            AlertUtils.show("Warning", "Please select a permit from the table to delete.", AlertType.WARNING);
            return;
        }
        if (!AlertUtils.confirm("Delete Student Permit", "Delete selected permit record?")) {
            return;
        }

        if (permitDAO.deleteStudentPermit(permitNum)) {
            AlertUtils.show("Success", "Permit deleted successfully!", AlertType.INFORMATION);
            loadPermits();
            handleClearForm(null);
        } else {
            AlertUtils.show("Error", "Failed to delete permit.", AlertType.ERROR);
        }
    }

    // --- Clear Button Logic ---
    @FXML
    private void handleClearForm(ActionEvent event) {
        txtPermitNum.clear();
        txtPermitNum.setDisable(false); // Unlock the Permit Number field so they can type a new one
        txtCustomerId.clear();
        dpIssueDate.setValue(null);
        dpExpiryDate.setValue(null);
        cmbStatus.setValue(null);
    }

    private void clearForm() {
        clearErrorStyles();

        txtPermitNum.clear();
        txtCustomerId.clear();
        dpIssueDate.setValue(null);
        dpExpiryDate.setValue(null);
        cmbStatus.setValue(null);
    }

    private void setupValidationListeners() {
        txtPermitNum.textProperty().addListener((observable, oldValue, newValue) -> txtPermitNum.getStyleClass().remove("error-field"));
        txtCustomerId.textProperty().addListener((observable, oldValue, newValue) -> txtCustomerId.getStyleClass().remove("error-field"));
        cmbStatus.valueProperty().addListener((observable, oldValue, newValue) -> cmbStatus.getStyleClass().remove("error-field"));
    }

    private void clearErrorStyles() {
        txtPermitNum.getStyleClass().remove("error-field");
        txtCustomerId.getStyleClass().remove("error-field");
        cmbStatus.getStyleClass().remove("error-field");
    }

}
