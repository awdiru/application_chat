package ru.avdonin.client.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

public class ConfigsRepository extends BaseRepository {
    public ConfigsRepository() {
        String sql = """
                create table if not exists configs (
                    config_name text not null,
                    config_value text not null,
                    primary key (config_name)
                );
                """;
        execute(sql);
        createConfigsIfNotExist();
    }

    public String getConfig(String configName) {
        String sql = """
                select config_value from configs
                where config_name = ?
                """;
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String configValue = null;
            pstmt.setString(1, configName);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) configValue = rs.getString("config_value");
            }
            return configValue;
        } catch (Exception e) {
            throw new RuntimeException("Config retrieval failed", e);
        }
    }

    public void updateOrCreateConfig(String configName, String configValue) {
        String sql = """
                insert or replace into configs (config_name, config_value)
                values (?, ?)
                """;
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, configName);
                pstmt.setString(2, configValue);
                pstmt.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Config save failed", e);
        }
    }

    private void createConfigsIfNotExist(){
        Map<String, String> configs = Map.of(
                "language", "SYSTEM",
                "time_zone", "SYSTEM"
        );

        for (String configName : configs.keySet()) {
            if (getConfig(configName) == null)
                updateOrCreateConfig(configName, configs.get(configName));
        }
    }
}
