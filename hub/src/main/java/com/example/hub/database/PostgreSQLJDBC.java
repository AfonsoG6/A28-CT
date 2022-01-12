package com.example.hub.database;

import java.sql.*;

public class PostgreSQLJDBC {
	private static Connection connection;

	public static void connect() {
		try {
			Class.forName("org.postgresql.Driver");
			Connection conn = DriverManager.getConnection(
					"jdbc:postgresql://localhost:5432/sirs", "postgres", "postgres"
			);
			PostgreSQLJDBC.connection = conn;
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
	}

	public static void setupDatabase() throws SQLException {
		connect();
		String sql = "CREATE TABLE icc (" +
				"id INT PRIMARY KEY NOT NULL," +
				"code VARCHAR(256) NOT NULL UNIQUE);" +

				"CREATE TABLE infected_sks (" +
				"id INT PRIMARY KEY NOT NULL," +
				"sk VARCHAR(256) NOT NULL," +
				"query_id INT NOT NULL);" +

				"CREATE TABLE health_services (" +
				"id INT PRIMARY KEY NOT NULL," +
				"email VARCHAR(100) NOT NULL UNIQUE," +
				"hashed_password VARCHAR(256) NOT NULL)";

		Statement statement = connection.createStatement();
		statement.execute(sql);
	}

	public static Connection getConnection() {
		return PostgreSQLJDBC.connection;
	}

	public static void main(String[] args) {
		try {
			setupDatabase();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
