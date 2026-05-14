package com.companyproject;

import com.companyproject.config.DatabaseConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

public class FxmlSmokeTest extends Application {

    private static final String[] FXML_FILES = {
        "/com/companyproject/view/Login.fxml",
        "/com/companyproject/view/Dashboard.fxml",
        "/com/companyproject/view/CustomerList.fxml",
        "/com/companyproject/view/AddCustomer.fxml",
        "/com/companyproject/view/VehicleList.fxml",
        "/com/companyproject/view/AddVehicle.fxml",
        "/com/companyproject/view/TransactionList.fxml",
        "/com/companyproject/view/AddTransaction.fxml",
        "/com/companyproject/view/Reports.fxml"
    };

    @Override
    public void start(Stage stage) throws Exception {
        for (String fxml : FXML_FILES) {
            FXMLLoader.load(getClass().getResource(fxml));
            System.out.println("Loaded " + fxml);
        }
        DatabaseConnection.getInstance().shutdown();
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
