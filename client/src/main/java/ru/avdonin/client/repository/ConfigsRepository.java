package ru.avdonin.client.repository;

import java.sql.*;
import java.util.Map;

public class ConfigsRepository extends BaseRepository {
    private final Map<String, String> configs = Map.of(
            "language", "SYSTEM",
            "time_zone", "SYSTEM",
            "http-uri", "http://localhost:8080",
            "ws-uri", "ws://localhost:8080"
    );

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

        return executeSelect(sql, "config_value", configName);
    }

    public void updateOrCreateConfig(String configName, String configValue) {
        String sql = """
                insert or replace into configs (config_name, config_value)
                values (?, ?)
                """;
        execute(sql, configName, configValue);
    }

    protected void createConfigsIfNotExist(){
        for (String configName : configs.keySet()) {
            if (getConfig(configName) == null)
                updateOrCreateConfig(configName, configs.get(configName));
        }
    }
}
