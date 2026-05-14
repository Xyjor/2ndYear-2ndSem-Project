package com.companyproject.dao;

import com.companyproject.config.DatabaseConnection;
import com.companyproject.model.ServiceTransaction;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TransactionDAO {

    public ServiceTransaction create(ServiceTransaction transaction) throws SQLException {
        String sql = "INSERT INTO transactions "
                + "(transaction_no, customer_id, vehicle_id, service_type, status, amount, remarks) "
                + "VALUES (?, ?, ?, ?::service_type, ?::transaction_status, ?, ?) "
                + "RETURNING transaction_id, submitted_at, created_at, updated_at, version";

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, transaction.getTransactionNo());
            statement.setObject(2, transaction.getCustomerId());
            if (transaction.getVehicleId() == null) {
                statement.setNull(3, Types.OTHER);
            } else {
                statement.setObject(3, transaction.getVehicleId());
            }
            statement.setString(4, transaction.getServiceType());
            statement.setString(5, transaction.getStatus());
            statement.setBigDecimal(6, transaction.getAmount() == null ? BigDecimal.ZERO : transaction.getAmount());
            statement.setString(7, emptyToNull(transaction.getRemarks()));

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    transaction.setTransactionId(resultSet.getObject("transaction_id", UUID.class));
                    transaction.setSubmittedAt(toLocalDateTime(resultSet.getTimestamp("submitted_at")));
                    transaction.setCreatedAt(toLocalDateTime(resultSet.getTimestamp("created_at")));
                    transaction.setUpdatedAt(toLocalDateTime(resultSet.getTimestamp("updated_at")));
                    transaction.setVersion(resultSet.getInt("version"));
                }
            }
            connection.commit();
            return transaction;
        }
    }

    public List<ServiceTransaction> search(String keyword, String status, int limit, int offset) throws SQLException {
        String sql = selectTransactionSql()
                + " WHERE (? IS NULL OR t.transaction_no ILIKE ? OR c.first_name ILIKE ? OR c.last_name ILIKE ? "
                + "OR v.plate_no ILIKE ? OR v.engine_no ILIKE ? OR v.chassis_no ILIKE ?) "
                + "AND (? IS NULL OR t.status = ?::transaction_status) "
                + "ORDER BY t.submitted_at DESC LIMIT ? OFFSET ?";
        String searchValue = keyword == null || keyword.trim().isEmpty() ? null : "%" + keyword.trim() + "%";
        String statusValue = status == null || status.trim().isEmpty() || "ALL".equals(status) ? null : status;

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 1; i <= 7; i++) {
                statement.setString(i, searchValue);
            }
            statement.setString(8, statusValue);
            statement.setString(9, statusValue);
            statement.setInt(10, limit);
            statement.setInt(11, offset);

            List<ServiceTransaction> transactions = new ArrayList<>();
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    transactions.add(mapTransaction(resultSet));
                }
            }
            connection.commit();
            return transactions;
        }
    }

    public void updateStatus(UUID transactionId, String status) throws SQLException {
        String sql = "UPDATE transactions SET status = ?::transaction_status, "
                + "completed_at = CASE WHEN ? = 'COMPLETED' THEN now() ELSE completed_at END, "
                + "version = version + 1 WHERE transaction_id = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            statement.setString(2, status);
            statement.setObject(3, transactionId);
            statement.executeUpdate();
            connection.commit();
        }
    }

    private static String selectTransactionSql() {
        return "SELECT t.transaction_id, t.transaction_no, t.customer_id, "
                + "trim(concat_ws(' ', c.first_name, c.middle_name, c.last_name, c.suffix)) AS customer_name, "
                + "t.vehicle_id, trim(concat_ws(' - ', v.plate_no, v.make, v.model)) AS vehicle_name, "
                + "t.service_type::text AS service_type, t.status::text AS status, t.amount, "
                + "t.submitted_at, t.completed_at, t.remarks, t.created_at, t.updated_at, t.version "
                + "FROM transactions t "
                + "JOIN customers c ON c.customer_id = t.customer_id "
                + "LEFT JOIN vehicles v ON v.vehicle_id = t.vehicle_id";
    }

    private static ServiceTransaction mapTransaction(ResultSet resultSet) throws SQLException {
        ServiceTransaction transaction = new ServiceTransaction();
        transaction.setTransactionId(resultSet.getObject("transaction_id", UUID.class));
        transaction.setTransactionNo(resultSet.getString("transaction_no"));
        transaction.setCustomerId(resultSet.getObject("customer_id", UUID.class));
        transaction.setCustomerName(resultSet.getString("customer_name"));
        transaction.setVehicleId(resultSet.getObject("vehicle_id", UUID.class));
        transaction.setVehicleName(resultSet.getString("vehicle_name"));
        transaction.setServiceType(resultSet.getString("service_type"));
        transaction.setStatus(resultSet.getString("status"));
        transaction.setAmount(resultSet.getBigDecimal("amount"));
        transaction.setSubmittedAt(toLocalDateTime(resultSet.getTimestamp("submitted_at")));
        transaction.setCompletedAt(toLocalDateTime(resultSet.getTimestamp("completed_at")));
        transaction.setRemarks(resultSet.getString("remarks"));
        transaction.setCreatedAt(toLocalDateTime(resultSet.getTimestamp("created_at")));
        transaction.setUpdatedAt(toLocalDateTime(resultSet.getTimestamp("updated_at")));
        transaction.setVersion(resultSet.getInt("version"));
        return transaction;
    }

    private static LocalDateTime toLocalDateTime(Timestamp value) {
        return value == null ? null : value.toLocalDateTime();
    }

    private static String emptyToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }
}
