package com.companyproject;

import com.companyproject.config.DatabaseConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    private static final String STYLESHEET = "/com/companyproject/view/style.css";

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/com/companyproject/view/Login.fxml"));
        Scene scene = new Scene(root, 1024, 680);
        scene.getStylesheets().add(getClass().getResource(STYLESHEET).toExternalForm());

        primaryStage.setTitle("Company Data Management System");
        primaryStage.setMinWidth(960);
        primaryStage.setMinHeight(620);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     *
     */
    @Override
    public void stop() {
        DatabaseConnection.getInstance().shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
