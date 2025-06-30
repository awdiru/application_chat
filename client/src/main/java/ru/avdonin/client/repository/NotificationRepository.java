package ru.avdonin.client.repository;

public class NotificationRepository extends BaseRepository{
    public NotificationRepository() {
        String sql = """
                create table if not exists notifications (
                    chat_id text not null,
                    notification_num text not null,
                    primary key (chat_id)
                );
                """;
        execute(sql);
    }

    public Integer getNotificationsNum(String chatId) {
        String sql = """
                select notification_num from notifications
                where chat_id = ?
                """;

        String num = executeSelect(sql, "notification_num", chatId);
        return Integer.parseInt(num);
    }

    public void updateOrCreateNotificationNum(String chatId, Integer notificationNum) {
        String sql = """
                insert or replace into notifications (chat_id, notification_num)
                values (?, ?)
                """;
        execute(sql, chatId, String.valueOf(notificationNum));
    }
}
