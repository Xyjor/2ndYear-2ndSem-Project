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
import model.Customer;
import agencyims.DBConnection;

/**
 *
 * @author Xyjor
 */
public class CustomerDAO {

    private static final Logger LOGGER = Logger.getLogger(CustomerDAO.class.getName());

// 1. CREATE: Method to add a new customer to the database

    public boolean addCustomer(Customer customer) {
        String query = "INSERT INTO Customers (FirstName, LastName, DateOfBirth, Address, ContactNumber) VALUES (?, ?, ?, ?, ?)";

        // We use try-with-resources to automatically close the connection to prevent memory leaks
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {

            // The '?' symbols in the query are replaced by the actual data here
            pstmt.setString(1, customer.getFirstName());
            pstmt.setString(2, customer.getLastName());
            pstmt.setDate(3, customer.getDateOfBirth());
            pstmt.setString(4, customer.getAddress());
            pstmt.setString(5, customer.getContactNumber());

            // Execute the update. If rowsAffected is > 0, it was successful.
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error adding customer.", e);
            return false;
        }
    }

    // 2. READ: Method to fetch all customers to display in a JavaFX Table
    public ObservableList<Customer> getAllCustomers() {
        // ObservableList is specifically designed for JavaFX to update tables automatically
        ObservableList<Customer> customerList = FXCollections.observableArrayList();
        String query = "SELECT CustomerID, FirstName, LastName, DateOfBirth, Address, ContactNumber FROM Customers";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(query); ResultSet rs = pstmt.executeQuery()) {

            // Loop through every row the database returns
            while (rs.next()) {
                // Create a new Customer object for each row
                Customer customer = new Customer(
                        rs.getInt("CustomerID"),
                        rs.getString("FirstName"),
                        rs.getString("LastName"),
                        rs.getDate("DateOfBirth"),
                        rs.getString("Address"),
                        rs.getString("ContactNumber")
                );
                // Add the object to our list
                customerList.add(customer);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error fetching customers.", e);
        }

        return customerList;
    }

    // 3. UPDATE: Modify an existing customer
    public boolean updateCustomer(Customer customer) {
        String query = "UPDATE Customers SET FirstName = ?, LastName = ?, DateOfBirth = ?, Address = ?, ContactNumber = ? WHERE CustomerID = ?";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, customer.getFirstName());
            pstmt.setString(2, customer.getLastName());
            pstmt.setDate(3, customer.getDateOfBirth());
            pstmt.setString(4, customer.getAddress());
            pstmt.setString(5, customer.getContactNumber());
            pstmt.setInt(6, customer.getCustomerId()); // The WHERE clause

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error updating customer.", e);
            return false;
        }
    }

    // 4. DELETE: Remove a customer (Note: This will fail if they have connected permits/licenses due to Foreign Keys!)
    public boolean deleteCustomer(int customerId) {
        String query = "DELETE FROM Customers WHERE CustomerID = ?";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, customerId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error deleting customer.", e);
            return false;
        }
    }
}
