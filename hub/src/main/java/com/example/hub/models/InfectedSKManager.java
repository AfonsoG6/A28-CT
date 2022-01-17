package com.example.hub.models;

import com.example.hub.database.PostgreSQLJDBC;
import com.example.hub.grpc.Hub.*;
import com.google.protobuf.ByteString;

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
				stmt.setBytes(2, pair.getSk().toByteArray());
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
			byte[] sk = rs.getBytes("sk");
			SKEpochDayPair pair = SKEpochDayPair.newBuilder()
					.setEpochDay(epochDay)
					.setSk(ByteString.copyFrom(sk))
					.build();
			infectedSKs.add(pair);
		}
		return infectedSKs;
	}
}
