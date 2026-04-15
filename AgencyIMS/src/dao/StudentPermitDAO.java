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
import model.StudentPermit;
import agencyims.DBConnection;

/**
 *
 * @author Xyjor
 */
public class StudentPermitDAO {

    private static final Logger LOGGER = Logger.getLogger(StudentPermitDAO.class.getName());

    public boolean addStudentPermit(StudentPermit permit) {
        // FIXED: Changed CustomerID to Customers_CustomerID to match your database diagram
        String query = "INSERT INTO StudentPermits (PermitNumber, Customers_CustomerID, IssueDate, ExpiryDate, Status) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, permit.getPermitNumber());
            pstmt.setInt(2, permit.getCustomerId());
            pstmt.setDate(3, permit.getIssueDate());
            pstmt.setDate(4, permit.getExpiryDate());
            pstmt.setString(5, permit.getStatus());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error adding student permit.", e);
            return false;
        }
    }

    public ObservableList<StudentPermit> getAllStudentPermits() {
        ObservableList<StudentPermit> permitList = FXCollections.observableArrayList();
        String query = "SELECT PermitNumber, Customers_CustomerID, IssueDate, ExpiryDate, Status FROM StudentPermits";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(query); ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                StudentPermit permit = new StudentPermit(
                        rs.getString("PermitNumber"),
                        rs.getInt("Customers_CustomerID"), // FIXED: Fetching from the correct column name
                        rs.getDate("IssueDate"),
                        rs.getDate("ExpiryDate"),
                        rs.getString("Status")
                );
                permitList.add(permit);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error fetching student permits.", e);
        }
        return permitList;
    }

    // 1. UPDATE an existing permit
    public boolean updateStudentPermit(StudentPermit permit) {
        // We update everything EXCEPT the PermitNumber, which is our unique identifier (WHERE clause)
        String query = "UPDATE StudentPermits SET Customers_CustomerID = ?, IssueDate = ?, ExpiryDate = ?, Status = ? WHERE PermitNumber = ?";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, permit.getCustomerId());
            pstmt.setDate(2, permit.getIssueDate());
            pstmt.setDate(3, permit.getExpiryDate());
            pstmt.setString(4, permit.getStatus());
            pstmt.setString(5, permit.getPermitNumber()); // This goes in the WHERE clause

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error updating student permit.", e);
            return false;
        }
    }

    // 2. DELETE an existing permit
    public boolean deleteStudentPermit(String permitNumber) {
        String query = "DELETE FROM StudentPermits WHERE PermitNumber = ?";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, permitNumber);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error deleting student permit.", e);
            return false;
        }
    }

    // 3. CHECK if a customer already has a permit
    public boolean hasExistingPermit(int customerId) {
        String query = "SELECT COUNT(*) FROM StudentPermits WHERE Customers_CustomerID = ?";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0; // Returns true if count is 1 or more
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error checking for existing student permit.", e);
        }
        return false;
    }
}
