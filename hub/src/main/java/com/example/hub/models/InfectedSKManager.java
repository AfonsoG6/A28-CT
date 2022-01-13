package com.example.hub.models;

import com.example.hub.database.PostgreSQLJDBC;
import com.example.hub.grpc.Hub.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InfectedSKManager {
	private InfectedSKManager() {}

	public static void insertSKs(List<SKEpochDayPair> sks) throws SQLException {
		Connection conn = PostgreSQLJDBC.getConnection();
		String stmtString = "INSERT INTO infected_sks (epoch_day, sk, ins_epoch) VALUES (?, ?, ?);";

		try (PreparedStatement stmt = conn.prepareStatement(stmtString)) {
			for (SKEpochDayPair pair: sks){
				stmt.setLong(1, pair.getEpochDay());
				stmt.setString(2, pair.getSk());
				stmt.addBatch();
				stmt.executeBatch();
			}
		}
	}

	public static List<SKEpochDayPair> queryInfectedSKs(long lastQueryEpoch) throws SQLException {
		Connection conn = PostgreSQLJDBC.getConnection();
		String stmtString = "SELECT epoch_day, sk FROM infected_sks WHERE ins_epoch > ?;";

		ResultSet rs;
		try (PreparedStatement stmt = conn.prepareStatement(stmtString)) {
			stmt.setLong(1, lastQueryEpoch);
			rs = stmt.executeQuery();
		}

		ArrayList<SKEpochDayPair> infectedSKs = new ArrayList<>();
		while (rs.next()) {
			int epochDay = rs.getInt("epoch_day");
			String sk = rs.getString("sk");
			SKEpochDayPair pair = SKEpochDayPair.newBuilder().setEpochDay(epochDay).setSk(sk).build();
			infectedSKs.add(pair);
		}
		return infectedSKs;
	}
}
