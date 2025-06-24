package ru.avdonin.client.client.gui.additional_frames.list;

import ru.avdonin.client.client.gui.MainFrame;
import ru.avdonin.client.client.gui.additional_frames.BaseAdditionalFrame;
import ru.avdonin.client.client.gui.helpers.MainFrameHelper;
import ru.avdonin.template.model.message.dto.MessageDto;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.*;

public class ChangeMessageFrame extends BaseAdditionalFrame {
    private final MainFrame parent;
    private final MessageDto oldMessageDto;
    private final Set<String> oldSentImagesBase64;
    private final Map<String, Boolean> sentImagesBase64;
    private JPanel imagesPanel;

    private JTextArea messageArea;

    public ChangeMessageFrame(MainFrame parent, MessageDto oldMessageDto) {
        initFrame(parent.getDictionary().getChangeText(),
                new Dimension(240, 110));
        this.parent = parent;
        this.oldMessageDto = oldMessageDto;
        this.oldSentImagesBase64 = oldMessageDto.getImagesBase64() == null ? new HashSet<>() : oldMessageDto.getImagesBase64();
        this.sentImagesBase64 = initMap();

        JPanel inputPanel = getMessagePanel();
        add(inputPanel);
    }

    private JPanel getMessagePanel() {
        messageArea = new JTextArea(3, 20);
        messageArea.setText(oldMessageDto.getTextContent());
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);

        JScrollPane textScroll = new JScrollPane(messageArea);
        textScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        JButton attachButton = getAttachButton();


        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.add(textScroll, BorderLayout.CENTER);
        textPanel.add(attachButton, BorderLayout.EAST);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(getImages(), BorderLayout.NORTH);
        inputPanel.add(textPanel, BorderLayout.CENTER);

        setupKeyBindings(parent);

        return inputPanel;
    }

    private JButton getAttachButton() {
        JButton attachButton = new JButton(parent.getDictionary().getPaperClip());
        attachButton.setMargin(new Insets(0, 5, 0, 5));
        attachButton.addActionListener(e -> {
            try {
                Set<String> newImage = new HashSet<>();
                MainFrameHelper.attachImage(attachButton, newImage, parent.getDictionary());
                for (String image : newImage) sentImagesBase64.put(image, false);
                imagesPanel.revalidate();
                imagesPanel.repaint();
            } catch (IOException ex) {
                MainFrameHelper.errorHandler(ex, parent.getDictionary(), parent);
            }
        });
        return attachButton;
    }

    private void setupKeyBindings(MainFrame parent) {
        KeyStroke ctrlEnter = KeyStroke.getKeyStroke(
                KeyEvent.VK_ENTER,
                InputEvent.CTRL_DOWN_MASK
        );

        messageArea.getInputMap().put(ctrlEnter, "sendAction");
        messageArea.getActionMap().put("sendAction", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (sentImagesBase64.keySet().equals(oldSentImagesBase64)) sentImagesBase64.clear();
                    Set<String> sentImages = getSentImages();
                    MessageDto messageDto = MessageDto.builder()
                            .id(oldMessageDto.getId())
                            .chatId(oldMessageDto.getChatId())
                            .sender(parent.getUsername())
                            .textContent(messageArea.getText())
                            .imagesBase64(sentImages.isEmpty() ? null : sentImages)
                            .locale(parent.getDictionary().getLocale())
                            .build();

                    parent.getClient().changeMessage(messageDto);
                    parent.loadChatHistory();
                    dispose();
                } catch (Exception ex) {
                    MainFrameHelper.errorHandler(ex, parent.getDictionary(), parent);
                }
            }
        });
    }

    private JScrollPane getImages() {
        imagesPanel = new JPanel();
        imagesPanel.setLayout(new BoxLayout(imagesPanel, BoxLayout.X_AXIS));

        for (String imageBase64 : oldSentImagesBase64) {
            ImageIcon image;
            try {
                byte[] imageData = Base64.getDecoder().decode(imageBase64);
                Image scaledImage = new ImageIcon(imageData).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
                image = new ImageIcon(scaledImage);
            } catch (Exception e) {
                image = parent.getDictionary().getDefaultImage();
            }

            JButton deleteButton = getDeleteButton(imageBase64);

            JPanel imagePanel = new JPanel(new BorderLayout());
            imagePanel.add(new JLabel(image), BorderLayout.WEST);
            imagePanel.add(deleteButton, BorderLayout.EAST);

            imagesPanel.add(imagePanel);
        }
        JScrollPane scrollPane = new JScrollPane(imagesPanel);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        return scrollPane;
    }

    private JButton getDeleteButton(String imageBase64) {
        JButton deleteButton = new JButton(parent.getDictionary().getDelete());
        deleteButton.addActionListener(e -> {
            if (sentImagesBase64.get(imageBase64)) {
                sentImagesBase64.put(imageBase64, false);
                deleteButton.setIcon(parent.getDictionary().getDelete());

            } else {
                sentImagesBase64.put(imageBase64, true);
                deleteButton.setIcon(parent.getDictionary().getCompleteDeletion());
            }
            deleteButton.revalidate();
            deleteButton.repaint();
        });
        return deleteButton;
    }

    private Map<String, Boolean> initMap() {
        Map<String, Boolean> images = new HashMap<>();
        for (String image : oldSentImagesBase64) images.put(image, false);
        return images;
    }

    private Set<String> getSentImages() {
        Set<String> sentImages = new HashSet<>();

        for (String imageBase64 : sentImagesBase64.keySet())
            if (!sentImagesBase64.get(imageBase64))
                sentImages.add(imageBase64);

        return sentImages;
    }
}