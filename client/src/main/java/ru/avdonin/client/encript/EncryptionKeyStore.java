package ru.avdonin.client.encript;

import java.sql.*;

public class EncryptionKeyStore {
    private static final String DB_URL = "jdbc:sqlite:keys.db";
    private final EncryptionService encryptionService = new EncryptionService();

    public EncryptionKeyStore() {
        initializeDatabase();
    }

    public void saveKey(String roomId, String key) {
        String sql = """
                insert or replace into room_keys (room_id, encryption_key)
                values (?, ?)
                """;



        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            byte[] encryptKey = encryptionService.encrypt(key).getBytes();
            pstmt.setString(1, roomId);
            pstmt.setBytes(2, encryptKey);
            pstmt.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Key save failed", e);
        }
    }

    public String getKey(String roomId, String userId) {
        String sql = """
        select encryption_key from room_keys
        where room_id = ?
        """;
        String key = null;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, roomId);

            try (ResultSet rs = pstmt.executeQuery()) {

                if (rs.next()) {
                    String encryptKey = rs.getString("encryption_key");
                    key = encryptionService.decrypt(encryptKey);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Key retrieval failed", e);
        }
        return key;
    }

    private void initializeDatabase() {
        String sql = """
                create table if not exists room_keys (
                    room_id text not null,
                    encryption_key blob not null,
                    primary key (room_id)
                );
                """;
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            stmt.execute(sql);
        } catch (Exception e) {
            throw new RuntimeException("Database initialization failed", e);
        }
    }
}
