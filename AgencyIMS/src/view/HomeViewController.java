/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

import agencyims.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 *
 * @author Xyjor
 */
public class HomeViewController {

    private static final Logger LOGGER = Logger.getLogger(HomeViewController.class.getName());

    @FXML
    private Label lblTotalCustomers;
    @FXML
    private Label lblTotalPermits;
    @FXML
    private Label lblTotalLicenses;
    @FXML
    private Label lblTotalInsurances;

    @FXML
    public void initialize() {
        loadStatistics();
    }

    private void loadStatistics() {
        // We use the SQL COUNT() function to quickly get the number of rows from each table
        lblTotalCustomers.setText(String.valueOf(getCount("SELECT COUNT(*) FROM Customers")));
        lblTotalPermits.setText(String.valueOf(getCount("SELECT COUNT(*) FROM StudentPermits")));
        lblTotalLicenses.setText(String.valueOf(getCount("SELECT COUNT(*) FROM DriversLicenses")));
        lblTotalInsurances.setText(String.valueOf(getCount("SELECT COUNT(*) FROM CarInsurances")));
    }

    // Helper method so we don't have to write try-catch blocks 4 times!
    private int getCount(String query) {
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(query); ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1); // Grabs the count number
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error fetching dashboard count.", e);
        }
        return 0;
    }
}
