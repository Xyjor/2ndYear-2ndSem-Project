package com.companyproject.controller;

import com.companyproject.dao.CustomerDAO;
import com.companyproject.model.Customer;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;

public class CustomerListController implements Initializable {

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final CustomerDAO customerDAO = new CustomerDAO();

    @FXML private JFXTextField searchField;
    @FXML private JFXButton searchButton;
    @FXML private JFXButton refreshButton;
    @FXML private JFXButton newCustomerButton;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Label statusLabel;
    @FXML private TableView<Customer> customerTable;
    @FXML private TableColumn<Customer, String> nameColumn;
    @FXML private TableColumn<Customer, String> mobileColumn;
    @FXML private TableColumn<Customer, String> emailColumn;
    @FXML private TableColumn<Customer, String> cityColumn;
    @FXML private TableColumn<Customer, String> idColumn;
    @FXML private TableColumn<Customer, String> updatedColumn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configureTable();
        progressIndicator.setVisible(false);
        loadCustomers();
    }

    @FXML
    private void handleSearch() {
        loadCustomers();
    }

    @FXML
    private void handleRefresh() {
        searchField.clear();
        loadCustomers();
    }

    @FXML
    private void handleNewCustomer() {
        try {
            BorderPane dashboard = (BorderPane) customerTable.getScene().getRoot();
            Node content = FXMLLoader.load(getClass().getResource("/com/companyproject/view/AddCustomer.fxml"));
            dashboard.setCenter(content);

            Node titleNode = dashboard.lookup("#pageTitleLabel");
            if (titleNode instanceof Label) {
                ((Label) titleNode).setText("Add New Customer");
            }
        } catch (Exception exception) {
            statusLabel.setText("Unable to open the customer form.");
            exception.printStackTrace();
        }
    }

    private void configureTable() {
        nameColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getDisplayName()));
        mobileColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(valueOrBlank(data.getValue().getMobileNumber())));
        emailColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(valueOrBlank(data.getValue().getEmail())));
        cityColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(valueOrBlank(data.getValue().getCity())));
        idColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(formatId(data.getValue())));
        updatedColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(formatUpdatedAt(data.getValue())));
        customerTable.setPlaceholder(new Label("No customers found."));
    }

    private void loadCustomers() {
        String keyword = searchField.getText();
        Task<List<Customer>> task = new Task<List<Customer>>() {
            @Override
            protected List<Customer> call() throws Exception {
                return customerDAO.search(keyword, 100, 0);
            }
        };

        task.setOnRunning(event -> setBusy(true));
        task.setOnSucceeded(event -> {
            setBusy(false);
            List<Customer> customers = task.getValue();
            customerTable.setItems(FXCollections.observableArrayList(customers));
            statusLabel.setText(customers.size() + " customer record(s)");
        });
        task.setOnFailed(event -> {
            setBusy(false);
            statusLabel.setText("Unable to load customers.");
            task.getException().printStackTrace();
        });

        Thread thread = new Thread(task, "customer-search-task");
        thread.setDaemon(true);
        thread.start();
    }

    private void setBusy(boolean busy) {
        progressIndicator.setVisible(busy);
        searchButton.setDisable(busy);
        refreshButton.setDisable(busy);
        newCustomerButton.setDisable(busy);
        searchField.setDisable(busy);
    }

    private static String formatId(Customer customer) {
        if (isBlank(customer.getPrimaryIdType()) && isBlank(customer.getPrimaryIdNumber())) {
            return "";
        }
        if (isBlank(customer.getPrimaryIdType())) {
            return customer.getPrimaryIdNumber();
        }
        if (isBlank(customer.getPrimaryIdNumber())) {
            return customer.getPrimaryIdType();
        }
        return customer.getPrimaryIdType() + " - " + customer.getPrimaryIdNumber();
    }

    private static String formatUpdatedAt(Customer customer) {
        return customer.getUpdatedAt() == null ? "" : DATE_TIME_FORMAT.format(customer.getUpdatedAt());
    }

    private static String valueOrBlank(String value) {
        return value == null ? "" : value;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
