package ru.avdonin.client.repository;

import java.sql.*;

public class BaseRepository {
    protected static final String DB_URL = "jdbc:sqlite:client.db";

    protected void execute(String sql) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (Exception e) {
            throw new RuntimeException("Execution error", e);
        }
    }

    protected void execute(String sql, String... params) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (int i = 1; i <= params.length; i++)
                pstmt.setString(i, params[i - 1]);

            pstmt.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Execution error", e);
        }
    }

    protected String executeSelect(String sql, String searchParam, String... params) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (int i = 1; i <= params.length; i++)
                pstmt.setString(i, params[i - 1]);

            try (ResultSet rs = pstmt.executeQuery()) {
                String configValue = null;
                if (rs.next()) configValue = rs.getString(searchParam);
                return configValue;
            }

        } catch (Exception e) {
            throw new RuntimeException("Execution error", e);
        }
    }
}
