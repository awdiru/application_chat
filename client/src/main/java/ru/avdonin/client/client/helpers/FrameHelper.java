package ru.avdonin.client.client.helpers;

import ru.avdonin.client.client.gui.MainFrame;
import ru.avdonin.client.client.context.Context;
import ru.avdonin.client.client.settings.dictionary.BaseDictionary;
import ru.avdonin.template.constatns.Constants;
import ru.avdonin.template.model.chat.dto.ChatDto;
import ru.avdonin.template.model.message.dto.MessageDto;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

import static ru.avdonin.client.client.context.ContextKeysEnum.*;

public class FrameHelper {
    private static final BaseDictionary dictionary = Context.get(DICTIONARY);
    public static final Color BACKGROUNG_COLOR = UIManager.getColor("Panel.background");


    public static String getMonth(OffsetDateTime date) {
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

    public static String getDayOfWeek(OffsetDateTime date) {
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

    public static void errorHandler(Exception e, JFrame parent) {
        if (e.getMessage() == null || e.getMessage().isEmpty()) return;
        JOptionPane.showMessageDialog(parent, e.getMessage(), dictionary.getError(), JOptionPane.ERROR_MESSAGE);
    }

    public static <F extends JFrame> void restart(F parent) {
        parent.dispose();
        F frame;
        if (parent instanceof  MainFrame) frame = (F) new MainFrame();
        else throw new IllegalArgumentException("unknown class");
        frame.setVisible(true);
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

    public static String attachImage() throws IOException {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(dictionary.getAttachImage());
        fileChooser.setFileFilter(new FileNameExtensionFilter(dictionary.getImages(), "jpg", "png"));

        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            BufferedImage originalImage = ImageIO.read(selectedFile);
            if (originalImage == null) throw new IOException(dictionary.getCannotBeRead());

            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();

            Integer targetWidth = Constants.COMPRESSION_IMAGES.getValue();
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
            if (component != null) {
                component.revalidate();
                component.repaint();
            }
        }
    }

    public static ImageIcon getScaledIcon(String imageBase64, int x, int y) {
        try {
            byte[] imageData = Base64.getDecoder().decode(imageBase64);
            ImageIcon icon = new ImageIcon(imageData);
            Image scaledImage = icon.getImage().getScaledInstance(x, y, Image.SCALE_SMOOTH);
            return new ImageIcon(scaledImage);

        } catch (Exception ignored) {
            return dictionary.getDefaultImage();
        }
    }

    public static ImageIcon getIcon(String imageBase64) {
        try {
            byte[] imageData = Base64.getDecoder().decode(imageBase64);
            return new ImageIcon(imageData);

        } catch (Exception ignored) {
            return dictionary.getDefaultImage();
        }
    }

    public static ImageIcon getNumber(Integer num) {
        return switch (num) {
            case 0 -> dictionary.getEnvelope();
            case 1 -> dictionary.getOne();
            case 2 -> dictionary.getTwo();
            case 3 -> dictionary.getThree();
            case 4 -> dictionary.getFour();
            case 5 -> dictionary.getFive();
            case 6 -> dictionary.getSix();
            case 7 -> dictionary.getSeven();
            case 8 -> dictionary.getEight();
            case 9 -> dictionary.getNine();
            default -> dictionary.getPlusNum();
        };
    }

    public static JTextPane getTextPaneHtml(String title) {
        JTextPane textPane = new JTextPane();
        textPane.setContentType("text/html");
        editTextPane(textPane, title);
        return textPane;
    }

    public static JTextPane getTextPane(String title) {
        JTextPane textPane = new JTextPane();
        editTextPane(textPane, title);
        return textPane;
    }

    private static void editTextPane(JTextPane textPane, String title) {
        textPane.setText(title);
        textPane.setEditable(false);
        textPane.setOpaque(false);
        textPane.setBorder(null);
        textPane.setMargin(new Insets(0, 0, 0, 0));
    }

    public static JTextArea getTextArea(String title, MouseAdapter selectListener) {
        JTextArea textArea = new JTextArea(title);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setFocusable(false);
        textArea.setBorder(null);
        textArea.setBackground(BACKGROUNG_COLOR);
        textArea.addMouseListener(selectListener);
        return textArea;
    }

    public static void setupKeyBindings(JComponent component,
                                        KeyStroke keyStroke,
                                        String actionKey,
                                        ActionListener action) {

        component.getInputMap().put(keyStroke, actionKey);
        component.getActionMap().put(actionKey, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                action.actionPerformed(e);
            }
        });
    }

    public static boolean isEmptyMessage(MessageDto messageDto) {
        return (messageDto.getTextContent() == null || messageDto.getTextContent().isEmpty())
                && (messageDto.getImagesBase64() == null || messageDto.getImagesBase64().isEmpty());
    }
}
