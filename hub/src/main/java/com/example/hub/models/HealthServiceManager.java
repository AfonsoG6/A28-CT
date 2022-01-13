package com.example.hub.models;

import com.example.hub.database.PostgreSQLJDBC;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;

public class HealthServiceManager {

    public static boolean registerHealthService(@NotNull String email, @NotNull String password)
            throws NoSuchAlgorithmException {
        String hashedPassword = hashPassword(password);
        Connection conn = PostgreSQLJDBC.getConnection();
        String stmtString = "INSERT INTO health_services (email, hashed_password) VALUES (?, ?);";

        try (PreparedStatement stmt = conn.prepareStatement(stmtString)) {
            stmt.setString(1, email);
            stmt.setString(2, hashedPassword);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean logHealthService(@NotNull String email, @NotNull String password)
            throws NoSuchAlgorithmException {
        String hashedPassword = hashPassword(password);
        Connection conn = PostgreSQLJDBC.getConnection();
        String stmtString = "SELECT id FROM health_services WHERE email = ? AND hashed_password = ?;";

        try (PreparedStatement stmt = conn.prepareStatement(stmtString)) {
            stmt.setString(1, email);
            stmt.setString(2, hashedPassword);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static String hashPassword(@NotNull String password) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedHash = digest.digest(
                password.getBytes(StandardCharsets.UTF_8)
        );
        return Base64.getEncoder().encodeToString(encodedHash);
    }
}
