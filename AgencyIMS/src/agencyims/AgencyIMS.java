/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMain.java to edit this template
 */
package agencyims;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.net.URL;
import javafx.scene.control.Alert;
import view.AlertUtils;

/**
 *
 * @author Xyjor
 */
public class AgencyIMS extends Application {

    
    @Override
    public void start(Stage primaryStage) throws Exception {

       // 1. Load the FXML file
        // IMPORTANT: The path here must match where your CustomerView.fxml is located!
        // If it's inside a package named 'view', use "/view/CustomerView.fxml"
        URL dashboardFxml = getClass().getResource("/view/MainDashboard.fxml");
        if (dashboardFxml == null) {
            AlertUtils.show("Startup Error", "Could not find /view/MainDashboard.fxml in classpath.", Alert.AlertType.ERROR);
            return;
        }
        Parent root = FXMLLoader.load(dashboardFxml);

        // 2. Create the Scene (the content inside the window)
        Scene scene = new Scene(root);
        // 3. Configure the Stage (the actual window)
        primaryStage.setTitle("Agency IMS - Customer Management");
        primaryStage.setScene(scene);
        
        // Optional: Set the window to open maximized or at a specific size
        // primaryStage.setWidth(1000);
        // primaryStage.setHeight(600);
        
        // 4. Show the window!
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
