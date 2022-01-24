package com.example.hub.database;

import java.io.*;
import java.sql.*;

public class PostgreSQLJDBC {
	private static Connection connection;

	public static void connect() {
		try {
			Class.forName("org.postgresql.Driver");
			PostgreSQLJDBC.connection = DriverManager.getConnection(
					"jdbc:postgresql://192.168.1.99:5432/sirs?sslmode=require",
					"cthub",
					"a28cthubsirs"
			);
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
	}

	public static void setupDatabase() throws SQLException, IOException {
		System.out.println("Setting up");
		connect();

		StringBuilder schemaStringBuilder = new StringBuilder();
		InputStream in = PostgreSQLJDBC.class.getResourceAsStream("/sql/schema.sql");
		if (in == null) {
			System.out.println("Could not find schema.sql");
			return;
		}
		try (BufferedReader sqlReader = new BufferedReader(new InputStreamReader(in))) {
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
