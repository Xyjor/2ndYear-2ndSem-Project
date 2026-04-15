/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Xyjor
 */
public class MainDashboardController {

    private static final Logger LOGGER = Logger.getLogger(MainDashboardController.class.getName());

    @FXML
    private BorderPane mainBorderPane;
    @FXML
    private StackPane contentArea;

    @FXML
    private void showHomeScreen(ActionEvent event) {
        loadScreen("/view/HomeView.fxml");
    }

// Make sure the Dashboard automatically loads the Home Screen on startup!
    @FXML
    public void initialize() {
        loadScreen("/view/HomeView.fxml");
    }

    // 1. Action for the "Customers" button
    @FXML
    private void showCustomerScreen(ActionEvent event) {
        // This path must match exactly where your CustomerView.fxml is!
        loadScreen("/view/CustomerView.fxml");
    }

    // 2. Action for the "Student Permits" button
    @FXML
    private void showStudentPermitScreen(ActionEvent event) {
        // This swaps the center screen to the Permits view!
        loadScreen("/view/StudentPermitView.fxml");
    }

    // 3. Action for the "Driver's Licenses" button
    @FXML
    private void showLicenseScreen(ActionEvent event) {
        // We will create this FXML later!
        loadScreen("/view/DriversLicenseView.fxml");
    }

    @FXML
    private void showCarInsuranceScreen(ActionEvent event) {
        loadScreen("/view/CarInsuranceView.fxml");
    }

    // --- The Magic Screen Swapper ---
    // This helper method takes an FXML file path, loads it, and stuffs it into the center of our dashboard
    private void loadScreen(String fxmlPath) {
        try {
            Parent screen = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentArea.getChildren().clear(); // Remove the old screen
            contentArea.getChildren().add(screen); // Put in the new screen
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading screen: " + fxmlPath, e);
            AlertUtils.show("Navigation Error", "Unable to load screen: " + fxmlPath, javafx.scene.control.Alert.AlertType.ERROR);
        }
    }
}
