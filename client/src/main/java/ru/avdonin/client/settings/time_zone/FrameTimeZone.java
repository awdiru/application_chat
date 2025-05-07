package ru.avdonin.client.settings.time_zone;

import lombok.Getter;
import ru.avdonin.client.settings.FrameSettings;
import ru.avdonin.client.settings.Settings;
import ru.avdonin.client.settings.language.FactoryLanguage;
import ru.avdonin.client.settings.language.FrameLanguage;

import javax.swing.*;

@Getter
public enum FrameTimeZone implements FrameSettings {
    GMT_13_00(new BaseTimeZone("GMT 13:00", 13)),
    GMT_12_00(new BaseTimeZone("GMT 12:00", 12)),
    GMT_11_00(new BaseTimeZone("GMT 11:00", 11)),
    GMT_10_00(new BaseTimeZone("GMT 10:00", 10)),
    GMT_09_00(new BaseTimeZone("GMT 09:00", 9)),
    GMT_08_00(new BaseTimeZone("GMT 08:00", 8)),
    GMT_07_00(new BaseTimeZone("GMT 07:00", 7)),
    GMT_06_00(new BaseTimeZone("GMT 06:00", 6)),
    GMT_05_00(new BaseTimeZone("GMT 05:00", 5)),
    GMT_04_00(new BaseTimeZone("GMT 04:00", 4)),
    GMT_03_00(new BaseTimeZone("GMT 03:00", 3)),
    GMT_02_00(new BaseTimeZone("GMT 02:00", 2)),
    GMT_01_00(new BaseTimeZone("GMT 01:00", 1)),
    GMT_00_00(new BaseTimeZone("GMT", 0)),
    GMT_MINUS_01_00(new BaseTimeZone("GMT -01:00", -1)),
    GMT_MINUS_02_00(new BaseTimeZone("GMT -02:00", -2)),
    GMT_MINUS_03_00(new BaseTimeZone("GMT -03:00", -3)),
    GMT_MINUS_04_00(new BaseTimeZone("GMT -04:00", -4)),
    GMT_MINUS_05_00(new BaseTimeZone("GMT -05:00", -5)),
    GMT_MINUS_06_00(new BaseTimeZone("GMT -06:00", -6)),
    GMT_MINUS_07_00(new BaseTimeZone("GMT -07:00", -7)),
    GMT_MINUS_08_00(new BaseTimeZone("GMT -08:00", -8)),
    GMT_MINUS_09_00(new BaseTimeZone("GMT -09:00", -9)),
    GMT_MINUS_10_00(new BaseTimeZone("GMT -10:00", -10)),
    GMT_MINUS_11_00(new BaseTimeZone("GMT -11:00", -11)),
    GMT_MINUS_12_00(new BaseTimeZone("GMT -12:00", -12)),
    SYSTEM(new BaseTimeZone("System time zone"));

    private final BaseTimeZone timeZone;
    private final FactoryTimeZone factory = FactoryTimeZone.getFactory();

    FrameTimeZone(BaseTimeZone timeZone) {
        this.timeZone = timeZone;
    }

    @Override
    public void getFrame() {
        JFrame main = new JFrame();
        main.setTitle(FactoryLanguage.getFactory().getSettings().getSettingTimeZone());
        main.setSize(250, 300);
        main.setLocationRelativeTo(null);

        DefaultListModel<String> settingsModel = new DefaultListModel<>();
        JList<String> settingsList = new JList<>(settingsModel);
        for (FrameTimeZone l : FrameTimeZone.values())
            settingsModel.addElement(l.getSelectedSetting());

        settingsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = settingsList.getSelectedValue();
                if (selected != null) {
                    factory.setTimeZone(selected);
                    main.dispose();
                    JOptionPane.showMessageDialog(main,
                            FactoryLanguage.getFactory().getFrameSettings().getLanguage().getRestartProgram(), FactoryLanguage.getFactory().getFrameSettings().getLanguage().getWarning(),
                            JOptionPane.WARNING_MESSAGE);
                    Settings.getFrame();
                }
            }
        });
    }

    @Override
    public String getSelectedSetting() {
        return timeZone.getCustomization();
    }
}
