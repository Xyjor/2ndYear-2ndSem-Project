package com.companyproject.controller;

import com.companyproject.dao.TransactionDAO;
import com.companyproject.model.ServiceTransaction;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;

public class TransactionListController implements Initializable {

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final TransactionDAO transactionDAO = new TransactionDAO();

    @FXML private JFXTextField searchField;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private JFXButton searchButton;
    @FXML private JFXButton refreshButton;
    @FXML private JFXButton newTransactionButton;
    @FXML private JFXButton processingButton;
    @FXML private JFXButton completeButton;
    @FXML private JFXButton cancelButton;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Label statusLabel;
    @FXML private TableView<ServiceTransaction> transactionTable;
    @FXML private TableColumn<ServiceTransaction, String> transactionNoColumn;
    @FXML private TableColumn<ServiceTransaction, String> customerColumn;
    @FXML private TableColumn<ServiceTransaction, String> serviceColumn;
    @FXML private TableColumn<ServiceTransaction, String> vehicleColumn;
    @FXML private TableColumn<ServiceTransaction, String> statusColumn;
    @FXML private TableColumn<ServiceTransaction, String> amountColumn;
    @FXML private TableColumn<ServiceTransaction, String> submittedColumn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        statusComboBox.getItems().setAll("ALL", "DRAFT", "PENDING", "PROCESSING", "COMPLETED", "CANCELLED");
        statusComboBox.setValue("ALL");
        configureTable();
        progressIndicator.setVisible(false);
        loadTransactions();
    }

    @FXML
    private void handleSearch() {
        loadTransactions();
    }

    @FXML
    private void handleRefresh() {
        searchField.clear();
        statusComboBox.setValue("ALL");
        loadTransactions();
    }

    @FXML
    private void handleNewTransaction() {
        try {
            BorderPane dashboard = (BorderPane) transactionTable.getScene().getRoot();
            Node content = FXMLLoader.load(getClass().getResource("/com/companyproject/view/AddTransaction.fxml"));
            dashboard.setCenter(content);
            Node titleNode = dashboard.lookup("#pageTitleLabel");
            if (titleNode instanceof Label) {
                ((Label) titleNode).setText("New Transaction");
            }
        } catch (Exception exception) {
            statusLabel.setText("Unable to open the transaction form.");
            exception.printStackTrace();
        }
    }

    @FXML
    private void markProcessing() {
        updateSelectedStatus("PROCESSING");
    }

    @FXML
    private void markCompleted() {
        updateSelectedStatus("COMPLETED");
    }

    @FXML
    private void markCancelled() {
        updateSelectedStatus("CANCELLED");
    }

    private void configureTable() {
        transactionNoColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getTransactionNo()));
        customerColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(valueOrBlank(data.getValue().getCustomerName())));
        serviceColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(pretty(data.getValue().getServiceType())));
        vehicleColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(valueOrBlank(data.getValue().getVehicleName())));
        statusColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(pretty(data.getValue().getStatus())));
        amountColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(formatAmount(data.getValue().getAmount())));
        submittedColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(formatSubmitted(data.getValue())));
        transactionTable.setPlaceholder(new Label("No transactions found."));
    }

    private void loadTransactions() {
        String keyword = searchField.getText();
        String status = statusComboBox.getValue();
        Task<List<ServiceTransaction>> task = new Task<List<ServiceTransaction>>() {
            @Override
            protected List<ServiceTransaction> call() throws Exception {
                return transactionDAO.search(keyword, status, 150, 0);
            }
        };

        task.setOnRunning(event -> setBusy(true));
        task.setOnSucceeded(event -> {
            setBusy(false);
            List<ServiceTransaction> transactions = task.getValue();
            transactionTable.setItems(FXCollections.observableArrayList(transactions));
            statusLabel.setText(transactions.size() + " transaction record(s)");
        });
        task.setOnFailed(event -> {
            setBusy(false);
            statusLabel.setText("Unable to load transactions.");
            task.getException().printStackTrace();
        });

        Thread thread = new Thread(task, "transaction-search-task");
        thread.setDaemon(true);
        thread.start();
    }

    private void updateSelectedStatus(String status) {
        ServiceTransaction selected = transactionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select a transaction first.");
            return;
        }

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                transactionDAO.updateStatus(selected.getTransactionId(), status);
                return null;
            }
        };

        task.setOnRunning(event -> setBusy(true));
        task.setOnSucceeded(event -> {
            setBusy(false);
            statusLabel.setText("Transaction marked " + pretty(status) + ".");
            loadTransactions();
        });
        task.setOnFailed(event -> {
            setBusy(false);
            statusLabel.setText("Unable to update transaction status.");
            task.getException().printStackTrace();
        });

        Thread thread = new Thread(task, "transaction-status-task");
        thread.setDaemon(true);
        thread.start();
    }

    private void setBusy(boolean busy) {
        progressIndicator.setVisible(busy);
        searchButton.setDisable(busy);
        refreshButton.setDisable(busy);
        newTransactionButton.setDisable(busy);
        processingButton.setDisable(busy);
        completeButton.setDisable(busy);
        cancelButton.setDisable(busy);
        searchField.setDisable(busy);
        statusComboBox.setDisable(busy);
    }

    private static String pretty(String value) {
        return value == null ? "" : value.replace('_', ' ');
    }

    private static String formatAmount(BigDecimal amount) {
        return amount == null ? "0.00" : amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private static String formatSubmitted(ServiceTransaction transaction) {
        return transaction.getSubmittedAt() == null ? "" : DATE_TIME_FORMAT.format(transaction.getSubmittedAt());
    }

    private static String valueOrBlank(String value) {
        return value == null ? "" : value;
    }
}
