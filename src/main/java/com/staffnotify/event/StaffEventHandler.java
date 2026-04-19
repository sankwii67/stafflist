package com.staffnotify.event;

import com.staffnotify.config.StaffNotifyConfig;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.Set;

public class StaffEventHandler {

    private static final Set<String> onlinePlayers = new HashSet<>();

    private static final SoundEvent SOUND_JOIN = SoundEvent.of(Identifier.of("staffnotify", "staff_join"));
    private static final SoundEvent SOUND_LEAVE = SoundEvent.of(Identifier.of("staffnotify", "staff_leave"));

    public static void register() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> onlinePlayers.clear());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> onlinePlayers.clear());
    }

    public static void onClientTick(MinecraftClient client) {
        if (client.player == null || client.getNetworkHandler() == null) return;

        Set<String> current = new HashSet<>();
        for (PlayerListEntry entry : client.getNetworkHandler().getPlayerList()) {
            String name = entry.getProfile().getName();
            if (name != null && !name.isEmpty()) current.add(name);
        }

        StaffNotifyConfig cfg = StaffNotifyConfig.getInstance();

        for (String name : current) {
            if (!onlinePlayers.contains(name) && cfg.isStaff(name)) {
                if (cfg.joinNotification) sendJoinMessage(client, name, cfg);
            }
        }

        for (String name : onlinePlayers) {
            if (!current.contains(name) && cfg.isStaff(name)) {
                if (cfg.leaveNotification) sendLeaveMessage(client, name, cfg);
            }
        }

        onlinePlayers.clear();
        onlinePlayers.addAll(current);
    }

    private static void sendJoinMessage(MinecraftClient client, String nick, StaffNotifyConfig cfg) {
        client.player.sendMessage(Text.literal(formatMsg(Text.translatable("staffnotify.notification.join", nick).getString(), nick)), false);
        if (cfg.soundJoinEnabled) playSound(client, SOUND_JOIN);
    }

    private static void sendLeaveMessage(MinecraftClient client, String nick, StaffNotifyConfig cfg) {
        client.player.sendMessage(Text.literal(formatMsg(Text.translatable("staffnotify.notification.leave", nick).getString(), nick)), false);
        if (cfg.soundLeaveEnabled) playSound(client, SOUND_LEAVE);
    }

    private static String formatMsg(String raw, String nick) {
        return "§6" + raw.replace(nick, "§a" + nick + "§6");
    }

    private static void playSound(MinecraftClient client, SoundEvent event) {
        if (client.getSoundManager() != null) {
            client.getSoundManager().play(PositionedSoundInstance.master(event, 1.0f, 1.0f));
        }
    }
}
