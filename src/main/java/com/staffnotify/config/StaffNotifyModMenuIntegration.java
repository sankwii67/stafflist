package com.staffnotify.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class StaffNotifyModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return this::buildScreen;
    }

    private Screen buildScreen(Screen parent) {
        StaffNotifyConfig cfg = StaffNotifyConfig.getInstance();

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.translatable("staffnotify.config.title"))
                .setSavingRunnable(StaffNotifyConfig::save);

        ConfigEntryBuilder eb = builder.entryBuilder();

        ConfigCategory general = builder.getOrCreateCategory(Text.translatable("staffnotify.config.category.general"));

        general.addEntry(eb.startBooleanToggle(Text.translatable("staffnotify.config.sound_join"), cfg.soundJoinEnabled)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("staffnotify.config.sound_join.tooltip"))
                .setSaveConsumer(v -> cfg.soundJoinEnabled = v)
                .build());

        general.addEntry(eb.startBooleanToggle(Text.translatable("staffnotify.config.sound_leave"), cfg.soundLeaveEnabled)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("staffnotify.config.sound_leave.tooltip"))
                .setSaveConsumer(v -> cfg.soundLeaveEnabled = v)
                .build());

        general.addEntry(eb.startBooleanToggle(Text.translatable("staffnotify.config.join_notification"), cfg.joinNotification)
                .setDefaultValue(true)
                .setSaveConsumer(v -> cfg.joinNotification = v)
                .build());

        general.addEntry(eb.startBooleanToggle(Text.translatable("staffnotify.config.leave_notification"), cfg.leaveNotification)
                .setDefaultValue(true)
                .setSaveConsumer(v -> cfg.leaveNotification = v)
                .build());

        return builder.build();
    }
}
