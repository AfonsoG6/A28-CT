package com.example.hub.models;

import com.example.hub.database.PostgreSQLJDBC;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ICC {
	public static void insertICC(String icc) {
		Connection conn = PostgreSQLJDBC.getConnection();
		try {
			String stmtString = "INSERT INTO icc (code) VALUES (?);";
			PreparedStatement stmt = conn.prepareStatement(stmtString);
			stmt.setString(1, icc);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public boolean existsICC(String icc) {
		Connection conn = PostgreSQLJDBC.getConnection();
		try {
			String stmtString = "SELECT * FROM icc WHERE code = ?;";
			PreparedStatement stmt = conn.prepareStatement(stmtString);
			stmt.setString(1, icc);
			ResultSet rs = stmt.executeQuery();
			return rs.next();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public void deleteICC(String icc) {
		Connection conn = PostgreSQLJDBC.getConnection();
		try {
			String stmtString = "DELETE FROM icc WHERE code = ?;";
			PreparedStatement stmt = conn.prepareStatement(stmtString);
			stmt.setString(1, icc);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
