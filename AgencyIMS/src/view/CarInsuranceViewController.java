/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

import dao.CarInsuranceDAO;
import model.CarInsurance;
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
public class CarInsuranceViewController {

    private static final Logger LOGGER = Logger.getLogger(CarInsuranceViewController.class.getName());

    @FXML
    private TextField txtSearch;
    @FXML
    private TableView<CarInsurance> tvInsurance;
    @FXML
    private TableColumn<CarInsurance, String> colPolicyNum;
    @FXML
    private TableColumn<CarInsurance, Integer> colCustomerId;
    @FXML
    private TableColumn<CarInsurance, String> colMake;
    @FXML
    private TableColumn<CarInsurance, String> colModel;
    @FXML
    private TableColumn<CarInsurance, String> colPlate;
    @FXML
    private TableColumn<CarInsurance, String> colCoverage;
    @FXML
    private TableColumn<CarInsurance, Date> colIssueDate;
    @FXML
    private TableColumn<CarInsurance, Date> colExpiryDate;

    @FXML
    private TextField txtPolicyNum;
    @FXML
    private TextField txtCustomerId;
    @FXML
    private TextField txtMake;
    @FXML
    private TextField txtModel;
    @FXML
    private TextField txtPlate;
    @FXML
    private ComboBox<String> cmbCoverage;
    @FXML
    private DatePicker dpIssueDate;
    @FXML
    private DatePicker dpExpiryDate;

    private CarInsuranceDAO insuranceDAO = new CarInsuranceDAO();
    private ObservableList<CarInsurance> insuranceList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        loadInsurance();
        setupSearchFilter();
        cmbCoverage.setItems(FXCollections.observableArrayList("Comprehensive", "Third-Party"));
        cmbCoverage.setPromptText("Select Coverage");
    }

    private void setupTable() {
        colPolicyNum.setCellValueFactory(new PropertyValueFactory<>("policyNumber"));
        colCustomerId.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        colMake.setCellValueFactory(new PropertyValueFactory<>("vehicleMake"));
        colModel.setCellValueFactory(new PropertyValueFactory<>("vehicleModel"));
        colPlate.setCellValueFactory(new PropertyValueFactory<>("plateNumber"));
        colCoverage.setCellValueFactory(new PropertyValueFactory<>("coverageType"));
        colIssueDate.setCellValueFactory(new PropertyValueFactory<>("issueDate"));
        colExpiryDate.setCellValueFactory(new PropertyValueFactory<>("expiryDate"));
    }

    private void loadInsurance() {
        insuranceList.clear();
        try {
            insuranceList.addAll(insuranceDAO.getAllCarInsurances());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error loading car insurance records.", e);
        }
    }

    private void setupSearchFilter() {
        if (txtSearch == null) {
            tvInsurance.setItems(insuranceList);
            return;
        }

        FilteredList<CarInsurance> filteredData = new FilteredList<>(insuranceList, b -> true);

        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(insurance -> {
                if (newValue == null || newValue.trim().isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase().trim();

                if (insurance.getPolicyNumber() != null && insurance.getPolicyNumber().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (String.valueOf(insurance.getCustomerId()).contains(lowerCaseFilter)) {
                    return true;
                } else if (insurance.getVehicleMake() != null && insurance.getVehicleMake().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (insurance.getVehicleModel() != null && insurance.getVehicleModel().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (insurance.getPlateNumber() != null && insurance.getPlateNumber().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (insurance.getCoverageType() != null && insurance.getCoverageType().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (insurance.getIssueDate() != null && insurance.getIssueDate().toString().contains(lowerCaseFilter)) {
                    return true;
                } else if (insurance.getExpiryDate() != null && insurance.getExpiryDate().toString().contains(lowerCaseFilter)) {
                    return true;
                }

                return false;
            });
        });

        SortedList<CarInsurance> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tvInsurance.comparatorProperty());
        tvInsurance.setItems(sortedData);
    }

    @FXML
    private void handleTableClick(MouseEvent event) {
        CarInsurance selected = tvInsurance.getSelectionModel().getSelectedItem();
        if (selected != null) {
            txtPolicyNum.setText(selected.getPolicyNumber());
            txtPolicyNum.setDisable(true);
            txtCustomerId.setText(String.valueOf(selected.getCustomerId()));
            txtMake.setText(selected.getVehicleMake());
            txtModel.setText(selected.getVehicleModel());
            txtPlate.setText(selected.getPlateNumber());
            cmbCoverage.setValue(selected.getCoverageType());
            dpIssueDate.setValue(selected.getIssueDate().toLocalDate());
            dpExpiryDate.setValue(selected.getExpiryDate().toLocalDate());
        }
    }

    @FXML
    private void handleSaveInsurance(ActionEvent event) {
        try {
            if (InputValidators.isBlank(txtPolicyNum.getText()) || InputValidators.isBlank(txtCustomerId.getText())
                    || cmbCoverage.getValue() == null || dpIssueDate.getValue() == null || dpExpiryDate.getValue() == null) {
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

            CarInsurance newIns = new CarInsurance(
                    txtPolicyNum.getText().trim(), customerId,
                    txtMake.getText() == null ? "" : txtMake.getText().trim(),
                    txtModel.getText() == null ? "" : txtModel.getText().trim(),
                    txtPlate.getText() == null ? "" : txtPlate.getText().trim(), cmbCoverage.getValue(),
                    Date.valueOf(dpIssueDate.getValue()), Date.valueOf(dpExpiryDate.getValue())
            );

            if (insuranceDAO.addCarInsurance(newIns)) {
                AlertUtils.show("Success", "Insurance saved successfully!", AlertType.INFORMATION);
                loadInsurance();
                handleClearForm(null);
            } else {
                AlertUtils.show("Database Error", "Failed to save. Check if Customer ID exists.", AlertType.ERROR);
            }
        } catch (NumberFormatException e) {
            AlertUtils.show("Input Error", "Customer ID must be a valid number.", AlertType.ERROR);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error saving car insurance.", e);
            AlertUtils.show("System Error", "An error occurred while saving insurance.", AlertType.ERROR);
        }
    }

    @FXML
    private void handleUpdateInsurance(ActionEvent event) {
        if (txtPolicyNum.getText().isEmpty()) {
            AlertUtils.show("Warning", "Select a policy to update.", AlertType.WARNING);
            return;
        }
        if (InputValidators.isBlank(txtCustomerId.getText()) || cmbCoverage.getValue() == null || dpIssueDate.getValue() == null || dpExpiryDate.getValue() == null) {
            AlertUtils.show("Validation Error", "Customer ID, coverage, issue date, and expiry date are required.", AlertType.WARNING);
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
            CarInsurance updated = new CarInsurance(
                    txtPolicyNum.getText(), customerId,
                    txtMake.getText() == null ? "" : txtMake.getText().trim(),
                    txtModel.getText() == null ? "" : txtModel.getText().trim(),
                    txtPlate.getText() == null ? "" : txtPlate.getText().trim(), cmbCoverage.getValue(),
                    Date.valueOf(dpIssueDate.getValue()), Date.valueOf(dpExpiryDate.getValue())
            );

            if (insuranceDAO.updateCarInsurance(updated)) {
                AlertUtils.show("Success", "Insurance updated!", AlertType.INFORMATION);
                loadInsurance();
                handleClearForm(null);
            } else {
                AlertUtils.show("Error", "Failed to update.", AlertType.ERROR);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error updating car insurance.", e);
            AlertUtils.show("Error", "Check inputs and try again.", AlertType.ERROR);
        }
    }

    @FXML
    private void handleDeleteInsurance(ActionEvent event) {
        if (txtPolicyNum.getText().isEmpty()) {
            return;
        }
        if (!AlertUtils.confirm("Delete Car Insurance", "Delete selected policy record?")) {
            return;
        }
        if (insuranceDAO.deleteCarInsurance(txtPolicyNum.getText())) {
            AlertUtils.show("Success", "Policy deleted!", AlertType.INFORMATION);
            loadInsurance();
            handleClearForm(null);
        } else {
            AlertUtils.show("Error", "Failed to delete policy.", AlertType.ERROR);
        }
    }

    @FXML
    private void handleClearForm(ActionEvent event) {
        txtPolicyNum.clear();
        txtPolicyNum.setDisable(false);
        txtCustomerId.clear();
        txtMake.clear();
        txtModel.clear();
        txtPlate.clear();
        cmbCoverage.setValue(null);
        dpIssueDate.setValue(null);
        dpExpiryDate.setValue(null);
    }
}
