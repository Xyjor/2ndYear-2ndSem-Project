package com.companyproject.dao;

import com.companyproject.config.DatabaseConnection;
import com.companyproject.model.Vehicle;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class VehicleDAO {

    public Vehicle create(Vehicle vehicle) throws SQLException {
        String sql = "INSERT INTO vehicles "
                + "(customer_id, plate_no, engine_no, chassis_no, make, model, model_year, color) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?) "
                + "RETURNING vehicle_id, created_at, updated_at, version";

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindVehicle(statement, vehicle);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    vehicle.setVehicleId(resultSet.getObject("vehicle_id", UUID.class));
                    vehicle.setCreatedAt(toLocalDateTime(resultSet.getTimestamp("created_at")));
                    vehicle.setUpdatedAt(toLocalDateTime(resultSet.getTimestamp("updated_at")));
                    vehicle.setVersion(resultSet.getInt("version"));
                }
            }
            connection.commit();
            return vehicle;
        }
    }

    public Optional<Vehicle> findById(UUID vehicleId) throws SQLException {
        String sql = selectVehicleSql() + " WHERE v.vehicle_id = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, vehicleId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    connection.commit();
                    return Optional.of(mapVehicle(resultSet));
                }
            }
            connection.commit();
            return Optional.empty();
        }
    }

    public List<Vehicle> search(String keyword, int limit, int offset) throws SQLException {
        String sql = selectVehicleSql()
                + " WHERE (? IS NULL OR v.plate_no ILIKE ? OR v.engine_no ILIKE ? OR v.chassis_no ILIKE ? "
                + "OR v.make ILIKE ? OR v.model ILIKE ? OR c.first_name ILIKE ? OR c.last_name ILIKE ?) "
                + "ORDER BY v.updated_at DESC LIMIT ? OFFSET ?";
        String searchValue = keyword == null || keyword.trim().isEmpty() ? null : "%" + keyword.trim() + "%";

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 1; i <= 8; i++) {
                statement.setString(i, searchValue);
            }
            statement.setInt(9, limit);
            statement.setInt(10, offset);

            List<Vehicle> vehicles = new ArrayList<>();
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    vehicles.add(mapVehicle(resultSet));
                }
            }
            connection.commit();
            return vehicles;
        }
    }

    public Vehicle update(Vehicle vehicle) throws SQLException {
        String sql = "UPDATE vehicles SET "
                + "customer_id = ?, plate_no = ?, engine_no = ?, chassis_no = ?, make = ?, model = ?, "
                + "model_year = ?, color = ?, version = version + 1 "
                + "WHERE vehicle_id = ? AND version = ? "
                + "RETURNING updated_at, version";

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindVehicle(statement, vehicle);
            statement.setObject(9, vehicle.getVehicleId());
            statement.setInt(10, vehicle.getVersion());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    connection.rollback();
                    throw new ConcurrentModificationException("Vehicle was changed by another user.");
                }
                vehicle.setUpdatedAt(toLocalDateTime(resultSet.getTimestamp("updated_at")));
                vehicle.setVersion(resultSet.getInt("version"));
            }
            connection.commit();
            return vehicle;
        }
    }

    public boolean delete(UUID vehicleId) throws SQLException {
        String sql = "DELETE FROM vehicles WHERE vehicle_id = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, vehicleId);
            boolean deleted = statement.executeUpdate() > 0;
            connection.commit();
            return deleted;
        }
    }

    private static String selectVehicleSql() {
        return "SELECT v.vehicle_id, v.customer_id, "
                + "trim(concat_ws(' ', c.first_name, c.middle_name, c.last_name, c.suffix)) AS customer_name, "
                + "v.plate_no, v.engine_no, v.chassis_no, v.make, v.model, v.model_year, v.color, "
                + "v.created_at, v.updated_at, v.version "
                + "FROM vehicles v "
                + "LEFT JOIN customers c ON c.customer_id = v.customer_id";
    }

    private static void bindVehicle(PreparedStatement statement, Vehicle vehicle) throws SQLException {
        statement.setObject(1, vehicle.getCustomerId());
        statement.setString(2, emptyToNull(vehicle.getPlateNo()));
        statement.setString(3, vehicle.getEngineNo());
        statement.setString(4, vehicle.getChassisNo());
        statement.setString(5, vehicle.getMake());
        statement.setString(6, vehicle.getModel());
        if (vehicle.getModelYear() == null) {
            statement.setNull(7, Types.INTEGER);
        } else {
            statement.setInt(7, vehicle.getModelYear());
        }
        statement.setString(8, emptyToNull(vehicle.getColor()));
    }

    private static Vehicle mapVehicle(ResultSet resultSet) throws SQLException {
        Vehicle vehicle = new Vehicle();
        vehicle.setVehicleId(resultSet.getObject("vehicle_id", UUID.class));
        vehicle.setCustomerId(resultSet.getObject("customer_id", UUID.class));
        vehicle.setCustomerName(resultSet.getString("customer_name"));
        vehicle.setPlateNo(resultSet.getString("plate_no"));
        vehicle.setEngineNo(resultSet.getString("engine_no"));
        vehicle.setChassisNo(resultSet.getString("chassis_no"));
        vehicle.setMake(resultSet.getString("make"));
        vehicle.setModel(resultSet.getString("model"));
        int modelYear = resultSet.getInt("model_year");
        vehicle.setModelYear(resultSet.wasNull() ? null : modelYear);
        vehicle.setColor(resultSet.getString("color"));
        vehicle.setCreatedAt(toLocalDateTime(resultSet.getTimestamp("created_at")));
        vehicle.setUpdatedAt(toLocalDateTime(resultSet.getTimestamp("updated_at")));
        vehicle.setVersion(resultSet.getInt("version"));
        return vehicle;
    }

    private static LocalDateTime toLocalDateTime(Timestamp value) {
        return value == null ? null : value.toLocalDateTime();
    }

    private static String emptyToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }
}
