package com.companyproject.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public final class DatabaseConnection {

    private final HikariDataSource dataSource;

    private DatabaseConnection() {
        HikariConfig config = new HikariConfig();
        config.setPoolName("company-dms-pool");
        config.setDriverClassName("org.postgresql.Driver");
        config.setJdbcUrl(getEnv("DB_URL", "jdbc:postgresql://localhost:5432/company_dms"));
        config.setUsername(getEnv("DB_USER", "company_app"));
        config.setPassword(getEnv("DB_PASSWORD", "company_app_dev"));
        config.setMaximumPoolSize(Integer.parseInt(getEnv("DB_POOL_MAX", "10")));
        config.setMinimumIdle(Integer.parseInt(getEnv("DB_POOL_MIN_IDLE", "2")));
        config.setConnectionTimeout(30_000);
        config.setIdleTimeout(600_000);
        config.setMaxLifetime(1_800_000);
        config.setAutoCommit(false);
        config.addDataSourceProperty("ApplicationName", "CompanyProject");

        dataSource = new HikariDataSource(config);
    }

    private static String getEnv(String key, String defaultValue) {
        String value = System.getenv(key);
        return value == null || value.trim().isEmpty() ? defaultValue : value.trim();
    }

    private static class Holder {
        private static final DatabaseConnection INSTANCE = new DatabaseConnection();
    }

    public static DatabaseConnection getInstance() {
        return Holder.INSTANCE;
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
