package ru.avdonin.client.client.gui.helpers;

import ru.avdonin.client.client.Client;
import ru.avdonin.client.client.gui.MainFrame;
import ru.avdonin.client.settings.language.BaseDictionary;
import ru.avdonin.template.model.chat.dto.ChatDto;

import javax.swing.*;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class MainFrameHelper {

    public static String getMonth(OffsetDateTime date, BaseDictionary dictionary) {
        return switch (date.getMonth()) {
            case JANUARY -> dictionary.getJanuary();
            case FEBRUARY -> dictionary.getFebruary();
            case MARCH -> dictionary.getMarch();
            case APRIL -> dictionary.getApril();
            case MAY -> dictionary.getMay();
            case JUNE -> dictionary.getJune();
            case JULY -> dictionary.getJuly();
            case AUGUST -> dictionary.getAugust();
            case SEPTEMBER -> dictionary.getSeptember();
            case OCTOBER -> dictionary.getOctober();
            case NOVEMBER -> dictionary.getNovember();
            default -> dictionary.getDecember();
        };
    }

    public static String getDayOfWeek(OffsetDateTime date, BaseDictionary dictionary) {
        return switch (date.getDayOfWeek()) {
            case MONDAY -> dictionary.getMonday();
            case TUESDAY -> dictionary.getTuesday();
            case WEDNESDAY -> dictionary.getWednesday();
            case THURSDAY -> dictionary.getThursday();
            case FRIDAY -> dictionary.getFriday();
            case SATURDAY -> dictionary.getSaturday();
            default -> dictionary.getSunday();
        };
    }

    public static void errorHandler(Exception e, BaseDictionary dictionary, JFrame parent) {
        if (e.getMessage() == null || e.getMessage().isEmpty()) return;
        JOptionPane.showMessageDialog(parent, e.getMessage(), dictionary.getError(), JOptionPane.ERROR_MESSAGE);
    }

    public static void restart(JFrame parent, Client client, String username) {
        parent.dispose();
        MainFrame mainFrame = new MainFrame(client, username);
        client.setGui(mainFrame);
        mainFrame.setVisible(true);
    }

    public static void addDate(OffsetDateTime dateTime, JTextArea chatArea, BaseDictionary dictionary) {
        SwingUtilities.invokeLater(() -> {
            String date = MainFrameHelper.getDayOfWeek(dateTime, dictionary)
                    + ", " + dateTime.getDayOfMonth() + " "
                    + MainFrameHelper.getMonth(dateTime, dictionary) + "\n";
            chatArea.append(date);
        });
    }

    public static void addTime(OffsetDateTime dateTime, JTextArea chatArea) {
        SwingUtilities.invokeLater(() -> {
            String time = dateTime.format(DateTimeFormatter.ofPattern("HH:mm "));
            chatArea.append(time);
        });
    }

    public static String getChatName(ChatDto chat) {
        if (chat == null) return "";
        return chat.getCustomName() == null || chat.getCustomName().isEmpty()
                ? chat.getChatName()
                : chat.getCustomName() + " (" + chat.getChatName() + ")";
    }
}
