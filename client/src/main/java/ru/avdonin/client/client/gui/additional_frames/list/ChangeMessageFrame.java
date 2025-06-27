package ru.avdonin.client.client.gui.additional_frames.list;

import ru.avdonin.client.client.gui.MainFrame;
import ru.avdonin.client.client.gui.additional_frames.BaseAdditionalFrame;
import ru.avdonin.client.client.gui.helpers.FrameHelper;
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
        initFrame(parent.getDictionary().getChangeMessage(),
                new Dimension(300, 150));
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


        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.add(textScroll, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(getImages(), BorderLayout.NORTH);
        inputPanel.add(textPanel, BorderLayout.CENTER);

        setupKeyBindings(parent);

        return inputPanel;
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
                    Set<String> sentImages = getSentImages();
                    if (sentImages.equals(oldSentImagesBase64)) sentImages.clear();
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
                    FrameHelper.errorHandler(ex, parent.getDictionary(), parent);
                }
            }
        });
    }

    private JPanel getImages() {
        imagesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        for (String imageBase64 : oldSentImagesBase64) addImageComponent(imageBase64);

        JScrollPane scrollPane = new JScrollPane(imagesPanel);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        JButton attachButton = getAttachButton();

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(attachButton);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.add(scrollPane, BorderLayout.CENTER);
        headerPanel.add(buttonPanel, BorderLayout.EAST);

        return headerPanel;
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
            FrameHelper.repaintComponents(deleteButton);
            FrameHelper.repaintComponents(imagesPanel);
        });
        return deleteButton;
    }


    private JButton getAttachButton() {
        JButton attachButton = new JButton(parent.getDictionary().getPaperClip());
        attachButton.setMargin(new Insets(0, 5, 0, 5));
        attachButton.addActionListener(e -> {
            try {
                String image = FrameHelper.attachImage(parent.getDictionary());
                sentImagesBase64.put(image, false);
                addImageComponent(image);
                FrameHelper.repaintComponents(imagesPanel);
            } catch (IOException ex) {
                FrameHelper.errorHandler(ex, parent.getDictionary(), parent);
            }
        });
        return attachButton;
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

    private void addImageComponent(String imageBase64) {
        ImageIcon image;
        try {
            byte[] imageData = Base64.getDecoder().decode(imageBase64);
            Image scaledImage = new ImageIcon(imageData).getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
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
}