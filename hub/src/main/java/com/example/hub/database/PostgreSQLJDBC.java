package com.example.hub.database;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;

public class PostgreSQLJDBC {
	private static Connection connection;

	public static void connect() {
		try {
			Class.forName("org.postgresql.Driver");
			PostgreSQLJDBC.connection = DriverManager.getConnection(
					"jdbc:postgresql://localhost:5432/sirs",
					"postgres",
					"postgres"
			);
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
	}

	public static void setupDatabase() throws SQLException, IOException {
		connect();

		StringBuilder schemaStringBuilder = new StringBuilder();
		try (BufferedReader sqlReader = new BufferedReader(new FileReader("src/main/resources/sql/schema.sql"))) {
			for (String line; (line = sqlReader.readLine()) != null; ) {
				schemaStringBuilder.append(line).append("\n");
			}
		}
		String sql = schemaStringBuilder.toString();

		try (Statement statement = connection.createStatement()) {
			statement.execute(sql);
		}
	}

	public static Connection getConnection() {
		return PostgreSQLJDBC.connection;
	}

	public static void main(String[] args) {
		try {
			setupDatabase();
		} catch (SQLException | IOException e) {
			e.printStackTrace();
		}
	}
}
