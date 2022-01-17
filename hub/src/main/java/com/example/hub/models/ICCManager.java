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
	public static final int ICC_LENGTH = 20;

	private ICCManager() {}

	public static String generateICC() throws NoSuchAlgorithmException {
		SecureRandom random = SecureRandom.getInstanceStrong();
		char[] charset = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
		char[] icc = new char[ICC_LENGTH];
		for (int i=0; i<icc.length; i++) {
			int idx = random.nextInt(charset.length);
			icc[i] = charset[idx];
		}
		return new String(icc);
	}

	public static void insertICC(String icc) {
		Connection conn = PostgreSQLJDBC.getConnection();
		String stmtString = "INSERT INTO usable_iccs (code) VALUES (?);";
		try (PreparedStatement stmt = conn.prepareStatement(stmtString)) {
			stmt.setString(1, icc);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static boolean existsICC(String icc) {
		Connection conn = PostgreSQLJDBC.getConnection();
		String stmtString = "SELECT * FROM usable_iccs WHERE code = ?;";
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
		String stmtString = "DELETE FROM usable_iccs WHERE code = ?;";
		try (PreparedStatement stmt = conn.prepareStatement(stmtString)) {
			stmt.setString(1, icc);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
