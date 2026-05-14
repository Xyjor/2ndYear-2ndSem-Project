package com.companyproject.controller;

import com.companyproject.dao.ReportDAO;
import com.companyproject.model.ReportSummary;
import com.companyproject.model.ServiceCount;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class ReportsController implements Initializable {

    private final ReportDAO reportDAO = new ReportDAO();

    @FXML private Label customersLabel;
    @FXML private Label vehiclesLabel;
    @FXML private Label pendingLabel;
    @FXML private Label completedLabel;
    @FXML private Label revenueLabel;
    @FXML private Label statusLabel;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private TableView<ServiceCount> serviceTable;
    @FXML private TableColumn<ServiceCount, String> serviceColumn;
    @FXML private TableColumn<ServiceCount, String> countColumn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        serviceColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(pretty(data.getValue().getServiceType())));
        countColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(Integer.toString(data.getValue().getCount())));
        serviceTable.setPlaceholder(new Label("No service transactions yet."));
        progressIndicator.setVisible(false);
        loadReports();
    }

    @FXML
    private void loadReports() {
        Task<ReportData> task = new Task<ReportData>() {
            @Override
            protected ReportData call() throws Exception {
                return new ReportData(reportDAO.loadSummary(), reportDAO.loadServiceCounts());
            }
        };

        task.setOnRunning(event -> {
            progressIndicator.setVisible(true);
            statusLabel.setText("Loading report...");
        });
        task.setOnSucceeded(event -> {
            progressIndicator.setVisible(false);
            showSummary(task.getValue().summary);
            serviceTable.setItems(FXCollections.observableArrayList(task.getValue().serviceCounts));
            statusLabel.setText("Updated");
        });
        task.setOnFailed(event -> {
            progressIndicator.setVisible(false);
            statusLabel.setText("Unable to load report.");
            task.getException().printStackTrace();
        });

        Thread thread = new Thread(task, "reports-load-task");
        thread.setDaemon(true);
        thread.start();
    }

    private void showSummary(ReportSummary summary) {
        customersLabel.setText(Integer.toString(summary.getCustomerCount()));
        vehiclesLabel.setText(Integer.toString(summary.getVehicleCount()));
        pendingLabel.setText(Integer.toString(summary.getPendingTransactionCount()));
        completedLabel.setText(Integer.toString(summary.getCompletedTransactionCount()));
        revenueLabel.setText(formatAmount(summary.getCompletedRevenue()));
    }

    private static String pretty(String value) {
        return value == null ? "" : value.replace('_', ' ');
    }

    private static String formatAmount(BigDecimal value) {
        BigDecimal safeValue = value == null ? BigDecimal.ZERO : value;
        return safeValue.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private static final class ReportData {
        private final ReportSummary summary;
        private final List<ServiceCount> serviceCounts;

        private ReportData(ReportSummary summary, List<ServiceCount> serviceCounts) {
            this.summary = summary;
            this.serviceCounts = serviceCounts;
        }
    }
}
