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
import model.CarInsurance;
import agencyims.DBConnection;

/**
 *
 * @author Xyjor
 */
public class CarInsuranceDAO {

    private static final Logger LOGGER = Logger.getLogger(CarInsuranceDAO.class.getName());

    public boolean addCarInsurance(CarInsurance insurance) {
        // Changed CustomerID to Customers_CustomerID to match your database ERD
        String query = "INSERT INTO CarInsurances (PolicyNumber, Customers_CustomerID, VehicleMake, VehicleModel, PlateNumber, CoverageType, IssueDate, ExpiryDate) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, insurance.getPolicyNumber());
            pstmt.setInt(2, insurance.getCustomerId());
            pstmt.setString(3, insurance.getVehicleMake());
            pstmt.setString(4, insurance.getVehicleModel());
            pstmt.setString(5, insurance.getPlateNumber());
            pstmt.setString(6, insurance.getCoverageType());
            pstmt.setDate(7, insurance.getIssueDate());
            pstmt.setDate(8, insurance.getExpiryDate());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error adding car insurance.", e);
            return false;
        }
    }

    // --- 2. READ ---
    public ObservableList<CarInsurance> getAllCarInsurances() {
        ObservableList<CarInsurance> insuranceList = FXCollections.observableArrayList();
        String query = "SELECT PolicyNumber, Customers_CustomerID, VehicleMake, VehicleModel, PlateNumber, CoverageType, IssueDate, ExpiryDate FROM CarInsurances";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(query); ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                CarInsurance insurance = new CarInsurance(
                        rs.getString("PolicyNumber"),
                        rs.getInt("Customers_CustomerID"), // FIXED
                        rs.getString("VehicleMake"),
                        rs.getString("VehicleModel"),
                        rs.getString("PlateNumber"),
                        rs.getString("CoverageType"),
                        rs.getDate("IssueDate"),
                        rs.getDate("ExpiryDate")
                );
                insuranceList.add(insurance);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error fetching car insurances.", e);
        }
        return insuranceList;
    }

    // --- 3. UPDATE ---
    public boolean updateCarInsurance(CarInsurance insurance) {
        String query = "UPDATE CarInsurances SET Customers_CustomerID = ?, VehicleMake = ?, VehicleModel = ?, PlateNumber = ?, CoverageType = ?, IssueDate = ?, ExpiryDate = ? WHERE PolicyNumber = ?";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, insurance.getCustomerId());
            pstmt.setString(2, insurance.getVehicleMake());
            pstmt.setString(3, insurance.getVehicleModel());
            pstmt.setString(4, insurance.getPlateNumber());
            pstmt.setString(5, insurance.getCoverageType());
            pstmt.setDate(6, insurance.getIssueDate());
            pstmt.setDate(7, insurance.getExpiryDate());
            pstmt.setString(8, insurance.getPolicyNumber());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error updating car insurance.", e);
            return false;
        }
    }

    // --- 4. DELETE ---
    public boolean deleteCarInsurance(String policyNumber) {
        String query = "DELETE FROM CarInsurances WHERE PolicyNumber = ?";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, policyNumber);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error deleting car insurance.", e);
            return false;
        }
    }
}
