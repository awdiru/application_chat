package ru.avdonin.client.client.gui.helpers;

import ru.avdonin.client.client.Client;
import ru.avdonin.client.client.gui.MainFrame;
import ru.avdonin.client.settings.language.BaseDictionary;
import ru.avdonin.template.constatns.Constants;
import ru.avdonin.template.model.chat.dto.ChatDto;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

public class FrameHelper {

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

    public static String formatDateTime(OffsetDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yy HH:mm "));
    }

    public static String getChatName(ChatDto chat) {
        if (chat == null) return "";
        return chat.getCustomName() == null || chat.getCustomName().isEmpty()
                ? chat.getChatName()
                : chat.getCustomName() + " (" + chat.getChatName() + ")";
    }

    public static String attachImage(BaseDictionary dictionary) throws IOException {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(dictionary.getAttachImage());
        fileChooser.setFileFilter(new FileNameExtensionFilter(dictionary.getImages(), "jpg", "png"));

        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
                BufferedImage originalImage = ImageIO.read(selectedFile);
                if (originalImage == null) throw new IOException(dictionary.getCannotBeRead());

                int originalWidth = originalImage.getWidth();
                int originalHeight = originalImage.getHeight();

                Integer targetWidth = (Integer) Constants.COMPRESSION_IMAGES.getValue();
                int targetHeight = (int) (originalHeight * (targetWidth / (double) originalWidth));

                int imageType = originalImage.getTransparency() == Transparency.OPAQUE ?
                        BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;

                BufferedImage scaledImage = new BufferedImage(targetWidth, targetHeight, imageType);

                Graphics2D g2d = scaledImage.createGraphics();
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                        RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                if (imageType == BufferedImage.TYPE_INT_RGB) {
                    g2d.setColor(Color.WHITE);
                    g2d.fillRect(0, 0, targetWidth, targetHeight);
                }

                g2d.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
                g2d.dispose();

                String formatName = (imageType == BufferedImage.TYPE_INT_ARGB) ? "png" : "jpg";
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(scaledImage, formatName, baos);

                return Base64.getEncoder().encodeToString(baos.toByteArray());
        }
        return null;
    }

    public static void repaintComponents(JComponent... components) {
        for (JComponent component : components) {
            component.revalidate();
            component.repaint();
        }
    }
}
