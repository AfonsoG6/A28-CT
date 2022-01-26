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

	public static Connection getConnection() {
		return PostgreSQLJDBC.connection;
	}
}
