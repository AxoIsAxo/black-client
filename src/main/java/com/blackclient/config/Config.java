package com.blackclient.config;

import com.blackclient.hack.Hack;
import com.blackclient.hack.HackManager;
import com.blackclient.hack.setting.BoolSetting;
import com.blackclient.hack.setting.ModeSetting;
import com.blackclient.hack.setting.NumberSetting;
import com.blackclient.hack.setting.Setting;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.MinecraftClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Persists hack toggles and settings as JSON in
 * {@code <run-dir>/blackclient/config.json}.
 */
public enum Config {
    INSTANCE;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private boolean loaded;

    public synchronized void load() {
        if (loaded) {
            return;
        }
        loaded = true;
        Path file = getPath();
        if (!Files.exists(file)) {
            return;
        }
        try {
            JsonObject root = GSON.fromJson(Files.readString(file), JsonObject.class);
            if (root == null) {
                return;
            }
            for (Hack hack : HackManager.INSTANCE.getHacks()) {
                JsonObject hackJson = root.getAsJsonObject(hack.getName());
                if (hackJson == null) {
                    continue;
                }
                JsonElement enabled = hackJson.get("enabled");
                if (enabled != null && enabled.isJsonPrimitive()) {
                    hack.setEnabled(enabled.getAsBoolean());
                }
                JsonElement keybind = hackJson.get("keybind");
                if (keybind != null && keybind.isJsonPrimitive()) {
                    hack.setKeyBind(keybind.getAsInt());
                }
                JsonObject settingsJson = hackJson.getAsJsonObject("settings");
                if (settingsJson == null) {
                    continue;
                }
                for (Setting setting : hack.getSettings()) {
                    JsonElement value = settingsJson.get(setting.getName());
                    if (value == null || !value.isJsonPrimitive()) {
                        continue;
                    }
                    if (setting instanceof BoolSetting bool) {
                        bool.setValue(value.getAsBoolean());
                    } else if (setting instanceof NumberSetting number) {
                        number.setValue(value.getAsDouble());
                    } else if (setting instanceof ModeSetting mode) {
                        mode.setValue(value.getAsString());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void save() {
        JsonObject root = new JsonObject();
        for (Hack hack : HackManager.INSTANCE.getHacks()) {
            JsonObject hackJson = new JsonObject();
            hackJson.addProperty("enabled", hack.isEnabled());
            hackJson.addProperty("keybind", hack.getKeyBind());
            JsonObject settingsJson = new JsonObject();
            for (Setting setting : hack.getSettings()) {
                if (setting instanceof BoolSetting bool) {
                    settingsJson.addProperty(setting.getName(), bool.getValue());
                } else if (setting instanceof NumberSetting number) {
                    settingsJson.addProperty(setting.getName(), number.getValue());
                } else if (setting instanceof ModeSetting mode) {
                    settingsJson.addProperty(setting.getName(), mode.getValue());
                }
            }
            hackJson.add("settings", settingsJson);
            root.add(hack.getName(), hackJson);
        }

        try {
            Path file = getPath();
            Files.createDirectories(file.getParent());
            Files.writeString(file, GSON.toJson(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Path getPath() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.runDirectory == null) {
            return Paths.get("blackclient", "config.json");
        }
        return mc.runDirectory.toPath().resolve("blackclient").resolve("config.json");
    }
}
