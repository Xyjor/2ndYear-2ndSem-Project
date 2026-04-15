/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package agencyims;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Xyjor
 */
public class DBConnection {

    private static final Logger LOGGER = Logger.getLogger(DBConnection.class.getName());
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/agencyims";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "";
    private static final Properties APP_PROPERTIES = loadAppProperties();

    public static Connection getConnection() throws SQLException {
        String url = readValue("AGENCYIMS_DB_URL", "db.url", DEFAULT_URL);
        String user = readValue("AGENCYIMS_DB_USER", "db.user", DEFAULT_USER);
        String password = readValue("AGENCYIMS_DB_PASSWORD", "db.password", DEFAULT_PASSWORD);
        return DriverManager.getConnection(url, user, password);
    }

    private static String readValue(String envKey, String propertyKey, String fallback) {
        String envValue = System.getenv(envKey);
        if (envValue != null && !envValue.trim().isEmpty()) {
            return envValue.trim();
        }

        String propertyValue = APP_PROPERTIES.getProperty(propertyKey);
        if (propertyValue != null && !propertyValue.trim().isEmpty()) {
            return propertyValue.trim();
        }

        return fallback;
    }

    private static Properties loadAppProperties() {
        Properties properties = new Properties();
        try (InputStream stream = DBConnection.class.getResourceAsStream("/agencyims.properties")) {
            if (stream != null) {
                properties.load(stream);
            }
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Unable to load /agencyims.properties. Defaults/environment values will be used.", ex);
        }
        return properties;
    }

    public static void main(String[] args) {
        try (Connection ignored = getConnection()) {
            LOGGER.info("Database connection test succeeded.");
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Database connection test failed.", ex);
        }
    }
}
