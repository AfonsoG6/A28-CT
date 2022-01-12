package com.example.hub.models;

import com.example.hub.database.PostgreSQLJDBC;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class InfectedSKs {
	private String sk;
	private int queryId;

	public InfectedSKs(String sk, int queryId) {
		this.sk = sk;
		this.queryId = queryId;
	}

	public static void insertSKs(List<InfectedSKs> sks) {
		try {
			Connection conn = PostgreSQLJDBC.getConnection();
			int queryId = getLastQueryId();
			String stmtString = "INSERT INTO infectedSks (sk, queryId) VALUES (?, ?);";
			PreparedStatement ps = conn.prepareStatement(stmtString);
			for (InfectedSKs sk: sks){
				ps.setString(1, sk.sk);
				ps.setInt(2, queryId+1);
				ps.addBatch();
			}
			ps.executeBatch();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static List<InfectedSKs> queryInfectedSKs(int queryId) {
		try {
			Connection conn = PostgreSQLJDBC.getConnection();
			String stmtString = "SELECT sk, queryId FROM infectedSks WHERE queryId > ?;";
			PreparedStatement stmt = conn.prepareStatement(stmtString);
			stmt.setInt(1, queryId);
			ResultSet rs = stmt.executeQuery();

			ArrayList<InfectedSKs> infectedSKs = new ArrayList<>();
			while (rs.next()) {
				String sk = rs.getString("sk");
				int qId = rs.getInt("queryId");
				infectedSKs.add(new InfectedSKs(sk, qId));
			}
			return infectedSKs;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static int getLastQueryId() {
		try {
			Connection conn = PostgreSQLJDBC.getConnection();
			String stmtString = "SELECT MAX(queryId) FROM infectedSKs;";
			ResultSet rs = conn.createStatement().executeQuery(stmtString);
			if (rs.next())
				return rs.getInt("queryId");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}
}
