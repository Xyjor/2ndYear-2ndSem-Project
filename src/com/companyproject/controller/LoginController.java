package com.companyproject.controller;

import com.companyproject.config.DatabaseConnection;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.stage.Stage;

public class LoginController implements Initializable {

    @FXML
    private JFXTextField usernameField;

    @FXML
    private JFXPasswordField passwordField;

    @FXML
    private JFXButton loginButton;

    @FXML
    private ProgressIndicator progressIndicator;

    @FXML
    private Label messageLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        progressIndicator.setVisible(false);
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (isBlank(username) || isBlank(password)) {
            showMessage("Username and password are required.");
            return;
        }

        Task<AuthenticatedUser> task = new Task<AuthenticatedUser>() {
            @Override
            protected AuthenticatedUser call() throws Exception {
                return authenticate(username.trim(), password);
            }
        };

        task.setOnRunning(event -> setBusy(true));
        task.setOnSucceeded(event -> {
            setBusy(false);
            AuthenticatedUser user = task.getValue();
            if (user == null) {
                showMessage("Invalid credentials or inactive account.");
                return;
            }
            openDashboard(user);
        });
        task.setOnFailed(event -> {
            setBusy(false);
            showMessage("Unable to connect to the database.");
            task.getException().printStackTrace();
        });

        Thread thread = new Thread(task, "login-task");
        thread.setDaemon(true);
        thread.start();
    }

    private AuthenticatedUser authenticate(String username, String password) throws Exception {
        String sql = "SELECT user_id, full_name, role FROM app_users "
                + "WHERE username = ? AND password_hash = crypt(?, password_hash) AND active = TRUE";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, password);
            try (ResultSet resultSet = statement.executeQuery()) {
                AuthenticatedUser user = null;
                if (resultSet.next()) {
                    user = new AuthenticatedUser(
                            resultSet.getString("user_id"),
                            resultSet.getString("full_name"),
                            resultSet.getString("role"));
                }
                connection.commit();
                return user;
            }
        }
    }

    private void openDashboard(AuthenticatedUser user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/companyproject/view/Dashboard.fxml"));
            Parent root = loader.load();
            DashboardController controller = loader.getController();
            controller.setAuthenticatedUser(user.fullName, user.role);

            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(root, stage.getScene().getWidth(), stage.getScene().getHeight());
            scene.getStylesheets().add(getClass().getResource("/com/companyproject/view/style.css").toExternalForm());
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (Exception exception) {
            showMessage("Unable to open dashboard.");
            exception.printStackTrace();
        }
    }

    private void setBusy(boolean busy) {
        progressIndicator.setVisible(busy);
        loginButton.setDisable(busy);
        usernameField.setDisable(busy);
        passwordField.setDisable(busy);
    }

    private void showMessage(String message) {
        messageLabel.setText(message);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static final class AuthenticatedUser {
        private final String userId;
        private final String fullName;
        private final String role;

        private AuthenticatedUser(String userId, String fullName, String role) {
            this.userId = userId;
            this.fullName = fullName;
            this.role = role;
        }
    }
}
