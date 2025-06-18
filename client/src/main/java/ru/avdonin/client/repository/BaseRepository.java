package ru.avdonin.client.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class BaseRepository {
    protected static final String DB_URL = "jdbc:sqlite:client.db";

    protected void execute(String sql) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (Exception e) {
            throw new RuntimeException("Database initialization failed", e);
        }
    }
}
