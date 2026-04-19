package com.staffnotify.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class StaffNotifyConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path configDir = FabricLoader.getInstance().getConfigDir().resolve("staffnotify");
    private static Path configFile = configDir.resolve("default.staff");

    public boolean soundJoinEnabled = true;
    public boolean soundLeaveEnabled = true;
    public boolean joinNotification = true;
    public boolean leaveNotification = true;
    public List<String> staffList = new ArrayList<>();

    private static StaffNotifyConfig instance;

    public static StaffNotifyConfig getInstance() {
        if (instance == null) load();
        return instance;
    }

    public static void load() {
        load(configFile);
    }

    public static void load(Path file) {
        try {
            if (!Files.exists(file)) {
                instance = new StaffNotifyConfig();
                save();
                return;
            }
            try (Reader reader = Files.newBufferedReader(file)) {
                instance = GSON.fromJson(reader, StaffNotifyConfig.class);
                if (instance == null) instance = new StaffNotifyConfig();
            }
        } catch (IOException e) {
            System.err.println("[StaffNotify] Failed to load config: " + e.getMessage());
            instance = new StaffNotifyConfig();
        }
    }

    public static void loadByName(String name) {
        String fileName = name.endsWith(".staff") ? name : name + ".staff";
        load(configDir.resolve(fileName));
        configFile = configDir.resolve(fileName);
    }

    public static void save() {
        save(configFile);
    }

    public static void saveByName(String name) {
        String fileName = name.endsWith(".staff") ? name : name + ".staff";
        Path target = configDir.resolve(fileName);
        configFile = target;
        save(target);
    }

    public static void save(Path file) {
        try {
            Files.createDirectories(file.getParent());
            try (Writer writer = Files.newBufferedWriter(file)) {
                GSON.toJson(instance, writer);
            }
        } catch (IOException e) {
            System.err.println("[StaffNotify] Failed to save config: " + e.getMessage());
        }
    }

    public static List<String> listConfigs() {
        List<String> names = new ArrayList<>();
        try {
            if (Files.exists(configDir)) {
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(configDir, "*.staff")) {
                    for (Path p : stream) {
                        names.add(p.getFileName().toString());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("[StaffNotify] Failed to list configs: " + e.getMessage());
        }
        return names;
    }

    public static Path getConfigDir() {
        return configDir;
    }

    public static void setConfigDir(Path dir) {
        configDir = dir;
        configFile = dir.resolve("default.staff");
    }

    public boolean isStaff(String nickname) {
        return staffList.stream().anyMatch(n -> n.equalsIgnoreCase(nickname));
    }

    public boolean addStaff(String nickname) {
        if (isStaff(nickname)) return false;
        staffList.add(nickname);
        save();
        return true;
    }

    public boolean removeStaff(String nickname) {
        boolean removed = staffList.removeIf(n -> n.equalsIgnoreCase(nickname));
        if (removed) save();
        return removed;
    }
}
