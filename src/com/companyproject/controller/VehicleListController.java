package com.companyproject.controller;

import com.companyproject.dao.VehicleDAO;
import com.companyproject.model.Vehicle;
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

public class VehicleListController implements Initializable {

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final VehicleDAO vehicleDAO = new VehicleDAO();

    @FXML private JFXTextField searchField;
    @FXML private JFXButton searchButton;
    @FXML private JFXButton refreshButton;
    @FXML private JFXButton newVehicleButton;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Label statusLabel;
    @FXML private TableView<Vehicle> vehicleTable;
    @FXML private TableColumn<Vehicle, String> plateColumn;
    @FXML private TableColumn<Vehicle, String> ownerColumn;
    @FXML private TableColumn<Vehicle, String> makeModelColumn;
    @FXML private TableColumn<Vehicle, String> engineColumn;
    @FXML private TableColumn<Vehicle, String> chassisColumn;
    @FXML private TableColumn<Vehicle, String> updatedColumn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configureTable();
        progressIndicator.setVisible(false);
        loadVehicles();
    }

    @FXML
    private void handleSearch() {
        loadVehicles();
    }

    @FXML
    private void handleRefresh() {
        searchField.clear();
        loadVehicles();
    }

    @FXML
    private void handleNewVehicle() {
        try {
            BorderPane dashboard = (BorderPane) vehicleTable.getScene().getRoot();
            Node content = FXMLLoader.load(getClass().getResource("/com/companyproject/view/AddVehicle.fxml"));
            dashboard.setCenter(content);
            Node titleNode = dashboard.lookup("#pageTitleLabel");
            if (titleNode instanceof Label) {
                ((Label) titleNode).setText("Add Vehicle");
            }
        } catch (Exception exception) {
            statusLabel.setText("Unable to open the vehicle form.");
            exception.printStackTrace();
        }
    }

    private void configureTable() {
        plateColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(valueOrBlank(data.getValue().getPlateNo())));
        ownerColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(valueOrBlank(data.getValue().getCustomerName())));
        makeModelColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(formatMakeModel(data.getValue())));
        engineColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(valueOrBlank(data.getValue().getEngineNo())));
        chassisColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(valueOrBlank(data.getValue().getChassisNo())));
        updatedColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(formatUpdatedAt(data.getValue())));
        vehicleTable.setPlaceholder(new Label("No vehicles found."));
    }

    private void loadVehicles() {
        String keyword = searchField.getText();
        Task<List<Vehicle>> task = new Task<List<Vehicle>>() {
            @Override
            protected List<Vehicle> call() throws Exception {
                return vehicleDAO.search(keyword, 100, 0);
            }
        };

        task.setOnRunning(event -> setBusy(true));
        task.setOnSucceeded(event -> {
            setBusy(false);
            List<Vehicle> vehicles = task.getValue();
            vehicleTable.setItems(FXCollections.observableArrayList(vehicles));
            statusLabel.setText(vehicles.size() + " vehicle record(s)");
        });
        task.setOnFailed(event -> {
            setBusy(false);
            statusLabel.setText("Unable to load vehicles.");
            task.getException().printStackTrace();
        });

        Thread thread = new Thread(task, "vehicle-search-task");
        thread.setDaemon(true);
        thread.start();
    }

    private void setBusy(boolean busy) {
        progressIndicator.setVisible(busy);
        searchButton.setDisable(busy);
        refreshButton.setDisable(busy);
        newVehicleButton.setDisable(busy);
        searchField.setDisable(busy);
    }

    private static String formatMakeModel(Vehicle vehicle) {
        String year = vehicle.getModelYear() == null ? "" : vehicle.getModelYear().toString();
        return (year + " " + valueOrBlank(vehicle.getMake()) + " " + valueOrBlank(vehicle.getModel())).trim();
    }

    private static String formatUpdatedAt(Vehicle vehicle) {
        return vehicle.getUpdatedAt() == null ? "" : DATE_TIME_FORMAT.format(vehicle.getUpdatedAt());
    }

    private static String valueOrBlank(String value) {
        return value == null ? "" : value;
    }
}
