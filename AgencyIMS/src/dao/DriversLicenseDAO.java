/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.DriversLicense;
import agencyims.DBConnection;

/**
 *
 * @author Xyjor
 */
public class DriversLicenseDAO {

    private static final Logger LOGGER = Logger.getLogger(DriversLicenseDAO.class.getName());

    public boolean addDriversLicense(DriversLicense license) {
        // FIXED: Changed CustomerID to Customers_CustomerID
        String query = "INSERT INTO DriversLicenses (LicenseNumber, Customers_CustomerID, LicenseType, Restrictions, IssueDate, ExpiryDate) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, license.getLicenseNumber());
            pstmt.setInt(2, license.getCustomerId());
            pstmt.setString(3, license.getLicenseType());
            pstmt.setString(4, license.getRestrictions());
            pstmt.setDate(5, license.getIssueDate());
            pstmt.setDate(6, license.getExpiryDate());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error adding driver's license.", e);
            return false;
        }
    }

    // --- 2. READ ---
    public ObservableList<DriversLicense> getAllDriversLicenses() {
        ObservableList<DriversLicense> licenseList = FXCollections.observableArrayList();
        String query = "SELECT LicenseNumber, Customers_CustomerID, LicenseType, Restrictions, IssueDate, ExpiryDate FROM DriversLicenses";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(query); ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                DriversLicense license = new DriversLicense(
                        rs.getString("LicenseNumber"),
                        rs.getInt("Customers_CustomerID"), // FIXED: Fetching from the correct column!
                        rs.getString("LicenseType"),
                        rs.getString("Restrictions"),
                        rs.getDate("IssueDate"),
                        rs.getDate("ExpiryDate")
                );
                licenseList.add(license);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error fetching driver's licenses.", e);
        }
        return licenseList;
    }

    // --- 3. UPDATE ---
    public boolean updateDriversLicense(DriversLicense license) {
        // FIXED: Changed CustomerID to Customers_CustomerID
        String query = "UPDATE DriversLicenses SET Customers_CustomerID = ?, LicenseType = ?, Restrictions = ?, IssueDate = ?, ExpiryDate = ? WHERE LicenseNumber = ?";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, license.getCustomerId());
            pstmt.setString(2, license.getLicenseType());
            pstmt.setString(3, license.getRestrictions());
            pstmt.setDate(4, license.getIssueDate());
            pstmt.setDate(5, license.getExpiryDate());
            pstmt.setString(6, license.getLicenseNumber());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error updating driver's license.", e);
            return false;
        }
    }

    // --- 4. DELETE ---
    public boolean deleteDriversLicense(String licenseNumber) {
        String query = "DELETE FROM DriversLicenses WHERE LicenseNumber = ?";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, licenseNumber);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error deleting driver's license.", e);
            return false;
        }
    }

    // --- 5. DUPLICATE CHECK ---
    public boolean hasExistingLicense(int customerId) {
        // FIXED: Changed CustomerID to Customers_CustomerID
        String query = "SELECT COUNT(*) FROM DriversLicenses WHERE Customers_CustomerID = ?";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error checking for existing driver's license.", e);
        }
        return false;
    }
}
