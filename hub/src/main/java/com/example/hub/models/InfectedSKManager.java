package com.example.hub.models;

import com.example.hub.database.PostgreSQLJDBC;
import com.example.hub.grpc.Hub.*;
import com.google.protobuf.ByteString;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class InfectedSKManager {
	public static void insertSKs(List<SKEpochDayPair> sks) throws SQLException {
		Connection conn = PostgreSQLJDBC.getConnection();
		String stmtString = "INSERT INTO infected_sks (epoch_day, sk, ins_epoch) VALUES (?, ?, ?);";
		long epoch = Instant.now().toEpochMilli();

		try (PreparedStatement stmt = conn.prepareStatement(stmtString)) {
			for (SKEpochDayPair pair: sks){
				stmt.setInt(1, (int)pair.getEpochDay());
				stmt.setBytes(2, pair.getSk().toByteArray());
				stmt.setLong(3, epoch);
				stmt.addBatch();
				stmt.executeBatch();
			}
		}
	}

	public static List<SKEpochDayPair> queryInfectedSKs(long lastQueryEpoch) throws SQLException {
		Connection conn = PostgreSQLJDBC.getConnection();
		String stmtString = "SELECT epoch_day, sk FROM infected_sks WHERE ins_epoch > ?";
		ResultSet rs;
		ArrayList<SKEpochDayPair> infectedSKs = new ArrayList<>();
		try (PreparedStatement stmt = conn.prepareStatement(stmtString)) {
			stmt.setLong(1, lastQueryEpoch);
			rs = stmt.executeQuery();
			while (rs.next()) {
				int epochDay = rs.getInt("epoch_day");
				byte[] sk = rs.getBytes("sk");
				SKEpochDayPair pair = SKEpochDayPair.newBuilder()
						.setEpochDay(epochDay)
						.setSk(ByteString.copyFrom(sk))
						.build();
				infectedSKs.add(pair);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return infectedSKs;
	}

	public static long queryMaxInsEpoch() throws SQLException {
		Connection conn = PostgreSQLJDBC.getConnection();
		String stmtString = "SELECT MAX(ins_epoch) FROM infected_sks;";

		PreparedStatement stmt = conn.prepareStatement(stmtString);
		ResultSet rs = stmt.executeQuery();
		if (!rs.next()) return 0;

		return rs.getInt(1);
	}

	public static void removeExpiredSks(int expirationEpochDay) throws SQLException {
		Connection conn = PostgreSQLJDBC.getConnection();
		String stmtString = "DELETE FROM infected_sks WHERE epoch_day < ?;";

		PreparedStatement ps = conn.prepareStatement(stmtString);
		ps.setInt(1, expirationEpochDay);
		ps.executeUpdate();
	}
}
