/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package view;

import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import dao.CustomerDAO;
import model.Customer;
import java.sql.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import util.InputValidators;

/**
 * FXML Controller class
 *
 * @author Xyjor
 */
public class CustomerViewController {

    private static final Logger LOGGER = Logger.getLogger(CustomerViewController.class.getName());

    @FXML
    private TextField txtSearch;
    @FXML
    private TableView<Customer> tvCustomers;
    @FXML
    private TableColumn<Customer, Integer> colCustomerId;
    @FXML
    private TableColumn<Customer, String> colFirstName;
    @FXML
    private TableColumn<Customer, String> colLastName;
    @FXML
    private TableColumn<Customer, Date> colDob;
    @FXML
    private TableColumn<Customer, String> colAddress;
    @FXML
    private TableColumn<Customer, String> colContact;

    @FXML
    private TextField txtCustomerId; // Read-only
    @FXML
    private TextField txtFirstName;
    @FXML
    private TextField txtLastName;
    @FXML
    private DatePicker dpDob;
    @FXML
    private TextField txtAddress;
    @FXML
    private TextField txtContact;

    private CustomerDAO customerDAO = new CustomerDAO();
    private ObservableList<Customer> customerList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        loadCustomers();
        setupSearchFilter();
        setupValidationListeners(); // NEW: Turn on the dynamic error listeners!
        txtCustomerId.setEditable(false);
    }

    private void setupValidationListeners() {
        // Remove the red error highlight the moment the user starts typing
        txtFirstName.textProperty().addListener((observable, oldValue, newValue) -> {
            txtFirstName.getStyleClass().remove("error-field");
        });

        txtLastName.textProperty().addListener((observable, oldValue, newValue) -> {
            txtLastName.getStyleClass().remove("error-field");
        });

        dpDob.valueProperty().addListener((observable, oldValue, newValue) -> {
            dpDob.getStyleClass().remove("error-field");
        });

        // NEW: Contact Number Listener
        txtContact.textProperty().addListener((observable, oldValue, newValue) -> {
            txtContact.getStyleClass().remove("error-field"); // Remove red border on typing

            // MAGIC TRICK: If the new text contains anything that is NOT a number (\d), strip it out!
            if (!newValue.matches("\\d*")) {
                txtContact.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }

    // A helper to quickly wipe all red borders (useful when clearing the form)
    private void clearErrorStyles() {
        txtFirstName.getStyleClass().remove("error-field");
        txtLastName.getStyleClass().remove("error-field");
        dpDob.getStyleClass().remove("error-field");
    }

    private void setupTable() {
        colCustomerId.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        colFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        colLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colDob.setCellValueFactory(new PropertyValueFactory<>("dateOfBirth"));
        colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        colContact.setCellValueFactory(new PropertyValueFactory<>("contactNumber"));
    }

    private void loadCustomers() {
        customerList.clear();
        try {
            customerList.addAll(customerDAO.getAllCustomers());
            // REMOVED: tvCustomers.setItems(customerList); -> The search filter will handle setting the items now!
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error loading customers.", e);
        }
    }

    @FXML
    private void handleTableClick(MouseEvent event) {
        Customer selected = tvCustomers.getSelectionModel().getSelectedItem();
        if (selected != null) {
            txtCustomerId.setText(String.valueOf(selected.getCustomerId()));
            txtFirstName.setText(selected.getFirstName());
            txtLastName.setText(selected.getLastName());
            dpDob.setValue(selected.getDateOfBirth().toLocalDate());
            txtAddress.setText(selected.getAddress());
            txtContact.setText(selected.getContactNumber());
        }
    }

    @FXML
    private void handleSaveCustomer(ActionEvent event) {
        try {
            clearErrorStyles(); // Always start fresh!
            boolean hasError = false;

            // Check First Name
            if (InputValidators.isBlank(txtFirstName.getText())) {
                if (!txtFirstName.getStyleClass().contains("error-field")) {
                    txtFirstName.getStyleClass().add("error-field");
                }
                hasError = true;
            }

            // Check Last Name
            if (InputValidators.isBlank(txtLastName.getText())) {
                if (!txtLastName.getStyleClass().contains("error-field")) {
                    txtLastName.getStyleClass().add("error-field");
                }
                hasError = true;
            }

            // Check Date of Birth
            if (dpDob.getValue() == null) {
                if (!dpDob.getStyleClass().contains("error-field")) {
                    dpDob.getStyleClass().add("error-field");
                }
                hasError = true;
            }

            // If any field was empty, stop the save process and warn the user!
            if (hasError) {
                AlertUtils.show("Validation Error", "Please fill in the highlighted fields.", AlertType.WARNING);
                return;
            }

            // --- THE REST OF YOUR SAVE LOGIC REMAINS EXACTLY THE SAME BELOW THIS LINE ---
            Customer newCustomer = new Customer(
                    0,
                    txtFirstName.getText().trim(),
                    txtLastName.getText().trim(),
                    java.sql.Date.valueOf(dpDob.getValue()),
                    txtAddress.getText() == null ? "" : txtAddress.getText().trim(),
                    txtContact.getText() == null ? "" : txtContact.getText().trim()
            );

            if (customerDAO.addCustomer(newCustomer)) {
                AlertUtils.show("Success", "Customer added successfully!", AlertType.INFORMATION);
                loadCustomers();
                handleClearForm(null);
            } else {
                AlertUtils.show("Error", "Failed to add customer.", AlertType.ERROR);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error saving customer.", e);
            AlertUtils.show("System Error", "An error occurred while saving the customer.", AlertType.ERROR);
        }
    }

    @FXML
    private void handleUpdateCustomer(ActionEvent event) {
        if (txtCustomerId.getText().isEmpty()) {
            AlertUtils.show("Warning", "Please select a customer from the table to update.", AlertType.WARNING);
            return;
        }
        if (InputValidators.isBlank(txtFirstName.getText()) || InputValidators.isBlank(txtLastName.getText()) || dpDob.getValue() == null) {
            AlertUtils.show("Validation Error", "First Name, Last Name, and Date of Birth are required.", AlertType.WARNING);
            return;
        }

        try {
            Customer updatedCustomer = new Customer(
                    Integer.parseInt(txtCustomerId.getText()),
                    txtFirstName.getText().trim(),
                    txtLastName.getText().trim(),
                    Date.valueOf(dpDob.getValue()),
                    txtAddress.getText() == null ? "" : txtAddress.getText().trim(),
                    txtContact.getText() == null ? "" : txtContact.getText().trim()
            );

            if (customerDAO.updateCustomer(updatedCustomer)) {
                AlertUtils.show("Success", "Customer updated successfully!", AlertType.INFORMATION);
                loadCustomers();
                handleClearForm(null);
            } else {
                AlertUtils.show("Error", "Failed to update customer.", AlertType.ERROR);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error updating customer.", e);
            AlertUtils.show("Error", "Check your inputs and try again.", AlertType.ERROR);
        }
    }

    @FXML
    private void handleDeleteCustomer(ActionEvent event) {
        if (txtCustomerId.getText().isEmpty()) {
            AlertUtils.show("Warning", "Please select a customer to delete.", AlertType.WARNING);
            return;
        }
        if (!AlertUtils.confirm("Delete Customer", "Delete selected customer record?")) {
            return;
        }

        int id = Integer.parseInt(txtCustomerId.getText());

        // Safety warning since Foreign Keys might block deletion!
        if (customerDAO.deleteCustomer(id)) {
            AlertUtils.show("Success", "Customer deleted successfully!", AlertType.INFORMATION);
            loadCustomers();
            handleClearForm(null);
        } else {
            AlertUtils.show("Database Constraint", "Cannot delete this customer. They likely have an active Student Permit, Driver's License, or Car Insurance attached to their ID. Delete those records first!", AlertType.ERROR);
        }
    }

    @FXML
    private void handleClearForm(ActionEvent event) {
        clearErrorStyles();

        txtCustomerId.clear();
        txtFirstName.clear();
        txtLastName.clear();
        dpDob.setValue(null);
        txtAddress.clear();
        txtContact.clear();
    }

    private void setupSearchFilter() {
        // 1. Wrap the ObservableList in a FilteredList (initially displays all data)
        FilteredList<Customer> filteredData = new FilteredList<>(customerList, b -> true);

        // 2. Add a listener to the Search Bar to detect typing
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(customer -> {
                // If filter text is empty, display all customers
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                // Convert search string to lowercase to make it case-insensitive
                String lowerCaseFilter = newValue.toLowerCase();

                // Check if the search matches First Name, Last Name, ID, or Phone Number
                if (customer.getFirstName().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (customer.getLastName().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (String.valueOf(customer.getCustomerId()).contains(lowerCaseFilter)) {
                    return true;
                } else if (customer.getContactNumber().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }

                return false; // Does not match anything
            });
        });

        // 3. Wrap the FilteredList in a SortedList (so column clicking to sort still works)
        SortedList<Customer> sortedData = new SortedList<>(filteredData);

        // 4. Bind the SortedList comparator to the TableView comparator
        sortedData.comparatorProperty().bind(tvCustomers.comparatorProperty());

        // 5. Add the sorted and filtered data to the table
        tvCustomers.setItems(sortedData);
    }

}
