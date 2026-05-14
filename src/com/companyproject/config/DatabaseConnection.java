package com.companyproject.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DatabaseConnection {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);
    private final HikariDataSource dataSource;

    private DatabaseConnection() {
        HikariConfig config = new HikariConfig();
        config.setPoolName("company-dms-pool");
        config.setDriverClassName("org.postgresql.Driver");
        config.setJdbcUrl(ConfigLoader.get("DB_URL", "jdbc:postgresql://localhost:5432/company_dms"));
        config.setUsername(ConfigLoader.get("DB_USER", "company_app"));
        config.setPassword(ConfigLoader.get("DB_PASSWORD", "company_app_dev"));
        config.setMaximumPoolSize(ConfigLoader.getInt("DB_POOL_MAX", 10));
        config.setMinimumIdle(ConfigLoader.getInt("DB_POOL_MIN_IDLE", 2));
        config.setConnectionTimeout(30_000);
        config.setIdleTimeout(600_000);
        config.setMaxLifetime(1_800_000);
        config.setAutoCommit(false);
        config.addDataSourceProperty("ApplicationName", "CompanyProject");

        dataSource = new HikariDataSource(config);
        logger.info("Database connection pool initialized: {}", config.getJdbcUrl());
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
