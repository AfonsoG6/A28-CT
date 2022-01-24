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

    private static final String SALT = "Y%/DdSs&d4$5";
    private static final int SALT_ITERATIONS = 10;

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
        String passwordLen = String.valueOf(password.length());
        String saltedPassword = password;

        for (int i = 0; i < SALT_ITERATIONS; i++) {
            saltedPassword = passwordLen + SALT + saltedPassword + passwordLen;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(
                    saltedPassword.getBytes(StandardCharsets.UTF_8)
            );
            saltedPassword = Base64.getEncoder().encodeToString(encodedHash);
        }
        return saltedPassword;
    }
}
