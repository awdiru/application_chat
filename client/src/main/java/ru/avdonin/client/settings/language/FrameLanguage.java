package ru.avdonin.client.settings.language;

import lombok.Getter;
import ru.avdonin.client.settings.FrameSettings;
import ru.avdonin.client.settings.Settings;
import ru.avdonin.client.settings.language.list.LanguageEN;
import ru.avdonin.client.settings.language.list.LanguageIT;
import ru.avdonin.client.settings.language.list.LanguageRU;
import ru.avdonin.client.settings.language.list.LanguageSP;

import javax.swing.*;
import java.awt.*;

@Getter
public enum FrameLanguage implements FrameSettings {
    RU(new LanguageRU()),
    EN(new LanguageEN()),
    SP(new LanguageSP()),
    IT(new LanguageIT());

    private final BaseLanguage language;
    private final FactoryLanguage factory = FactoryLanguage.getFactory();

    FrameLanguage(BaseLanguage language) {
        this.language = language;
    }

    @Override
    public void getFrame() {
        JFrame main = new JFrame();
        main.setTitle(FactoryLanguage.getFactory().getSettings().getSettingsLanguage());
        main.setSize(200, 300);
        main.setLocationRelativeTo(null);

        DefaultListModel<String> settingsModel = new DefaultListModel<>();
        JList<String> settingsList = new JList<>(settingsModel);
        for (FrameLanguage l : FrameLanguage.values())
            settingsModel.addElement(l.getLanguage().getCustomization());

        settingsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = settingsList.getSelectedValue();
                if (selected != null) {
                    factory.setLanguage(selected);
                    main.dispose();
                    JOptionPane.showMessageDialog(main,
                            language.getRestartProgram(), language.getWarning(),
                            JOptionPane.WARNING_MESSAGE);
                    Settings.getFrame();
                }
            }
        });
        JScrollPane scrollPane = new JScrollPane(settingsList);
        scrollPane.setPreferredSize(new Dimension(200, 300));
        main.add(scrollPane);
        main.setVisible(true);
    }

    @Override
    public String getFrameName() {
        return language.getCustomization();
    }
}
