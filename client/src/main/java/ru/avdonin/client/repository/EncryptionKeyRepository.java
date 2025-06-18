package ru.avdonin.client.repository;

import ru.avdonin.client.encript.EncryptionService;

import java.sql.*;

public class EncryptionKeyRepository extends BaseRepository {
    private final EncryptionService encryptionService = new EncryptionService();

    public EncryptionKeyRepository() {
        String sql = """
                create table if not exists chat_keys (
                    chat_id text not null,
                    encryption_key blob not null,
                    primary key (chat_id)
                );
                """;
        execute(sql);
    }

    public void saveKey(String chatId, String key) {
        String oldKey = getKey(chatId);
        if (key.equals(oldKey)) return;

        String sql = """
                insert or replace into chat_keys (chat_id, encryption_key)
                values (?, ?)
                """;


        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            byte[] encryptKey = encryptionService.encrypt(key).getBytes();
            pstmt.setString(1, chatId);
            pstmt.setBytes(2, encryptKey);
            pstmt.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Key save failed", e);
        }
    }

    public String getKey(String chatId) {
        String sql = """
                select encryption_key from chat_keys
                where chat_id = ?
                """;
        String key = null;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, chatId);
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


}
