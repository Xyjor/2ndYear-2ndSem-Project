package com.companyproject.dao;

import com.companyproject.config.DatabaseConnection;
import com.companyproject.model.Customer;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CustomerDAO {

    public Customer create(Customer customer) throws SQLException {
        String sql = "INSERT INTO customers "
                + "(first_name, middle_name, last_name, suffix, birth_date, gender, civil_status, nationality) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?) "
                + "RETURNING customer_id, created_at, updated_at, version";

        try (Connection connection = DatabaseConnection.getInstance().getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                bindCustomerCore(statement, customer);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        customer.setCustomerId(resultSet.getObject("customer_id", UUID.class));
                        customer.setCreatedAt(toLocalDateTime(resultSet.getTimestamp("created_at")));
                        customer.setUpdatedAt(toLocalDateTime(resultSet.getTimestamp("updated_at")));
                        customer.setVersion(resultSet.getInt("version"));
                    }
                }
            }

            saveDetails(connection, customer);
            connection.commit();
            return customer;
        }
    }

    public Optional<Customer> findById(UUID customerId) throws SQLException {
        String sql = selectCustomerSql() + " WHERE c.customer_id = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, customerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    connection.commit();
                    return Optional.of(mapCustomer(resultSet));
                }
            }
            connection.commit();
            return Optional.empty();
        }
    }

    public List<Customer> search(String keyword, int limit, int offset) throws SQLException {
        String sql = selectCustomerSql()
                + " WHERE (? IS NULL OR c.first_name ILIKE ? OR c.last_name ILIKE ? "
                + "OR mobile.contact_value ILIKE ? OR email.contact_value ILIKE ?) "
                + "ORDER BY c.last_name, c.first_name LIMIT ? OFFSET ?";
        String searchValue = keyword == null || keyword.trim().isEmpty() ? null : "%" + keyword.trim() + "%";

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, searchValue);
            statement.setString(2, searchValue);
            statement.setString(3, searchValue);
            statement.setString(4, searchValue);
            statement.setString(5, searchValue);
            statement.setInt(6, limit);
            statement.setInt(7, offset);

            List<Customer> customers = new ArrayList<>();
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    customers.add(mapCustomer(resultSet));
                }
            }
            connection.commit();
            return customers;
        }
    }

    public Customer update(Customer customer) throws SQLException {
        String sql = "UPDATE customers SET "
                + "first_name = ?, middle_name = ?, last_name = ?, suffix = ?, birth_date = ?, "
                + "gender = ?, civil_status = ?, nationality = ?, version = version + 1 "
                + "WHERE customer_id = ? AND version = ? "
                + "RETURNING updated_at, version";

        try (Connection connection = DatabaseConnection.getInstance().getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                bindCustomerCore(statement, customer);
                statement.setObject(9, customer.getCustomerId());
                statement.setInt(10, customer.getVersion());

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (!resultSet.next()) {
                        connection.rollback();
                        throw new ConcurrentModificationException("Customer was changed by another user.");
                    }
                    customer.setUpdatedAt(toLocalDateTime(resultSet.getTimestamp("updated_at")));
                    customer.setVersion(resultSet.getInt("version"));
                }
            }

            replaceDetails(connection, customer);
            connection.commit();
            return customer;
        }
    }

    public boolean delete(UUID customerId) throws SQLException {
        String sql = "DELETE FROM customers WHERE customer_id = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, customerId);
            boolean deleted = statement.executeUpdate() > 0;
            connection.commit();
            return deleted;
        }
    }

    private static String selectCustomerSql() {
        return "SELECT c.customer_id, c.first_name, c.middle_name, c.last_name, c.suffix, "
                + "c.birth_date, c.gender, c.civil_status, c.nationality, c.created_at, c.updated_at, c.version, "
                + "mobile.contact_value AS mobile_number, phone.contact_value AS alternate_number, "
                + "email.contact_value AS email, addr.line1 AS address_line1, addr.line2 AS address_line2, "
                + "addr.barangay, addr.city, addr.province, addr.postal_code, "
                + "ident.id_type AS primary_id_type, ident.id_number AS primary_id_number "
                + "FROM customers c "
                + "LEFT JOIN LATERAL (SELECT contact_value FROM customer_contacts WHERE customer_id = c.customer_id "
                + "AND contact_type = 'MOBILE' ORDER BY is_primary DESC, created_at DESC LIMIT 1) mobile ON TRUE "
                + "LEFT JOIN LATERAL (SELECT contact_value FROM customer_contacts WHERE customer_id = c.customer_id "
                + "AND contact_type = 'PHONE' ORDER BY is_primary DESC, created_at DESC LIMIT 1) phone ON TRUE "
                + "LEFT JOIN LATERAL (SELECT contact_value FROM customer_contacts WHERE customer_id = c.customer_id "
                + "AND contact_type = 'EMAIL' ORDER BY is_primary DESC, created_at DESC LIMIT 1) email ON TRUE "
                + "LEFT JOIN LATERAL (SELECT line1, line2, barangay, city, province, postal_code FROM customer_addresses "
                + "WHERE customer_id = c.customer_id ORDER BY is_primary DESC, created_at DESC LIMIT 1) addr ON TRUE "
                + "LEFT JOIN LATERAL (SELECT id_type, id_number FROM customer_identifications WHERE customer_id = c.customer_id "
                + "ORDER BY is_primary DESC, created_at DESC LIMIT 1) ident ON TRUE";
    }

    private static void bindCustomerCore(PreparedStatement statement, Customer customer) throws SQLException {
        statement.setString(1, customer.getFirstName());
        statement.setString(2, emptyToNull(customer.getMiddleName()));
        statement.setString(3, customer.getLastName());
        statement.setString(4, emptyToNull(customer.getSuffix()));
        setDate(statement, 5, customer.getBirthDate());
        statement.setString(6, emptyToNull(customer.getGender()));
        statement.setString(7, emptyToNull(customer.getCivilStatus()));
        statement.setString(8, emptyToDefault(customer.getNationality(), "Filipino"));
    }

    private static void saveDetails(Connection connection, Customer customer) throws SQLException {
        insertContact(connection, customer.getCustomerId(), "MOBILE", customer.getMobileNumber(), true);
        insertContact(connection, customer.getCustomerId(), "PHONE", customer.getAlternateNumber(), false);
        insertContact(connection, customer.getCustomerId(), "EMAIL", customer.getEmail(), true);
        insertAddress(connection, customer);
        insertIdentification(connection, customer);
    }

    private static void replaceDetails(Connection connection, Customer customer) throws SQLException {
        deleteChildRows(connection, "customer_contacts", customer.getCustomerId());
        deleteChildRows(connection, "customer_addresses", customer.getCustomerId());
        deleteChildRows(connection, "customer_identifications", customer.getCustomerId());
        saveDetails(connection, customer);
    }

    private static void deleteChildRows(Connection connection, String tableName, UUID customerId) throws SQLException {
        String sql = "DELETE FROM " + tableName + " WHERE customer_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, customerId);
            statement.executeUpdate();
        }
    }

    private static void insertContact(Connection connection, UUID customerId, String type, String value, boolean primary) throws SQLException {
        if (isBlank(value)) {
            return;
        }
        String sql = "INSERT INTO customer_contacts (customer_id, contact_type, contact_value, is_primary) "
                + "VALUES (?, ?::contact_type, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, customerId);
            statement.setString(2, type);
            statement.setString(3, value.trim());
            statement.setBoolean(4, primary);
            statement.executeUpdate();
        }
    }

    private static void insertAddress(Connection connection, Customer customer) throws SQLException {
        if (isBlank(customer.getAddressLine1()) || isBlank(customer.getCity()) || isBlank(customer.getProvince())) {
            return;
        }
        String sql = "INSERT INTO customer_addresses "
                + "(customer_id, address_type, line1, line2, barangay, city, province, postal_code, is_primary) "
                + "VALUES (?, 'HOME', ?, ?, ?, ?, ?, ?, TRUE)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, customer.getCustomerId());
            statement.setString(2, customer.getAddressLine1().trim());
            statement.setString(3, emptyToNull(customer.getAddressLine2()));
            statement.setString(4, emptyToNull(customer.getBarangay()));
            statement.setString(5, customer.getCity().trim());
            statement.setString(6, customer.getProvince().trim());
            statement.setString(7, emptyToNull(customer.getPostalCode()));
            statement.executeUpdate();
        }
    }

    private static void insertIdentification(Connection connection, Customer customer) throws SQLException {
        if (isBlank(customer.getPrimaryIdType()) || isBlank(customer.getPrimaryIdNumber())) {
            return;
        }
        String sql = "INSERT INTO customer_identifications (customer_id, id_type, id_number, is_primary) "
                + "VALUES (?, ?, ?, TRUE)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, customer.getCustomerId());
            statement.setString(2, customer.getPrimaryIdType().trim());
            statement.setString(3, customer.getPrimaryIdNumber().trim());
            statement.executeUpdate();
        }
    }

    private static Customer mapCustomer(ResultSet resultSet) throws SQLException {
        Customer customer = new Customer();
        customer.setCustomerId(resultSet.getObject("customer_id", UUID.class));
        customer.setFirstName(resultSet.getString("first_name"));
        customer.setMiddleName(resultSet.getString("middle_name"));
        customer.setLastName(resultSet.getString("last_name"));
        customer.setSuffix(resultSet.getString("suffix"));
        customer.setBirthDate(toLocalDate(resultSet.getDate("birth_date")));
        customer.setGender(resultSet.getString("gender"));
        customer.setCivilStatus(resultSet.getString("civil_status"));
        customer.setNationality(resultSet.getString("nationality"));
        customer.setMobileNumber(resultSet.getString("mobile_number"));
        customer.setAlternateNumber(resultSet.getString("alternate_number"));
        customer.setEmail(resultSet.getString("email"));
        customer.setAddressLine1(resultSet.getString("address_line1"));
        customer.setAddressLine2(resultSet.getString("address_line2"));
        customer.setBarangay(resultSet.getString("barangay"));
        customer.setCity(resultSet.getString("city"));
        customer.setProvince(resultSet.getString("province"));
        customer.setPostalCode(resultSet.getString("postal_code"));
        customer.setPrimaryIdType(resultSet.getString("primary_id_type"));
        customer.setPrimaryIdNumber(resultSet.getString("primary_id_number"));
        customer.setCreatedAt(toLocalDateTime(resultSet.getTimestamp("created_at")));
        customer.setUpdatedAt(toLocalDateTime(resultSet.getTimestamp("updated_at")));
        customer.setVersion(resultSet.getInt("version"));
        return customer;
    }

    private static void setDate(PreparedStatement statement, int index, LocalDate value) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.DATE);
        } else {
            statement.setDate(index, Date.valueOf(value));
        }
    }

    private static LocalDate toLocalDate(Date value) {
        return value == null ? null : value.toLocalDate();
    }

    private static LocalDateTime toLocalDateTime(Timestamp value) {
        return value == null ? null : value.toLocalDateTime();
    }

    private static String emptyToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private static String emptyToDefault(String value, String defaultValue) {
        return isBlank(value) ? defaultValue : value.trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
