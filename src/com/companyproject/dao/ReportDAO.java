package com.companyproject.dao;

import com.companyproject.config.DatabaseConnection;
import com.companyproject.model.ReportSummary;
import com.companyproject.model.ServiceCount;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReportDAO {

    public ReportSummary loadSummary() throws SQLException {
        String sql = "SELECT "
                + "(SELECT count(*) FROM customers) AS customers, "
                + "(SELECT count(*) FROM vehicles) AS vehicles, "
                + "(SELECT count(*) FROM transactions WHERE status IN ('PENDING', 'PROCESSING')) AS pending_transactions, "
                + "(SELECT count(*) FROM transactions WHERE status = 'COMPLETED') AS completed_transactions, "
                + "(SELECT COALESCE(sum(amount), 0) FROM transactions WHERE status = 'COMPLETED') AS completed_revenue";

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            ReportSummary summary = new ReportSummary();
            if (resultSet.next()) {
                summary.setCustomerCount(resultSet.getInt("customers"));
                summary.setVehicleCount(resultSet.getInt("vehicles"));
                summary.setPendingTransactionCount(resultSet.getInt("pending_transactions"));
                summary.setCompletedTransactionCount(resultSet.getInt("completed_transactions"));
                BigDecimal revenue = resultSet.getBigDecimal("completed_revenue");
                summary.setCompletedRevenue(revenue == null ? BigDecimal.ZERO : revenue);
            }
            connection.commit();
            return summary;
        }
    }

    public List<ServiceCount> loadServiceCounts() throws SQLException {
        String sql = "SELECT service_type::text AS service_type, count(*) AS total "
                + "FROM transactions GROUP BY service_type ORDER BY service_type";

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<ServiceCount> counts = new ArrayList<>();
            while (resultSet.next()) {
                counts.add(new ServiceCount(resultSet.getString("service_type"), resultSet.getInt("total")));
            }
            connection.commit();
            return counts;
        }
    }
}
