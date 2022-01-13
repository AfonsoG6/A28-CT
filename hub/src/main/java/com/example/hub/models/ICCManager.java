package com.example.hub.models;

import com.example.hub.database.PostgreSQLJDBC;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

public class ICCManager {
	private ICCManager() {}

	public static String generateICC() throws NoSuchAlgorithmException {
		byte[] array = new byte[40]; // Java stores strings using 2 bytes per char, and we want a 20 char string
		Random random = SecureRandom.getInstanceStrong(); // SecureRandom is preferred to Random
		random.nextBytes(array);
		return new String(array, StandardCharsets.US_ASCII);
	}

	public static void insertICC(String icc) {
		Connection conn = PostgreSQLJDBC.getConnection();
		String stmtString = "INSERT INTO icc (value) VALUES (?);";
		try (PreparedStatement stmt = conn.prepareStatement(stmtString)) {
			stmt.setString(1, icc);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static boolean existsICC(String icc) {
		Connection conn = PostgreSQLJDBC.getConnection();
		String stmtString = "SELECT * FROM icc WHERE value = ?;";
		try (PreparedStatement stmt = conn.prepareStatement(stmtString)) {
			stmt.setString(1, icc);
			ResultSet rs = stmt.executeQuery();
			return rs.next();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static void deleteICC(String icc) {
		Connection conn = PostgreSQLJDBC.getConnection();
		String stmtString = "DELETE FROM icc WHERE value = ?;";
		try (PreparedStatement stmt = conn.prepareStatement(stmtString)) {
			stmt.setString(1, icc);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
