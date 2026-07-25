package net.phoenixvine.essentials.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class EssentialsSettings {

    private boolean showAfkOverlay = true;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path SETTINGS_FILE = Paths.get("config", "phoenix_essentials_settings.json");

    private static EssentialsSettings INSTANCE = null;

    public static EssentialsSettings get() {
        if (INSTANCE == null) INSTANCE = load();
        return INSTANCE;
    }

    public static EssentialsSettings load() {
        EssentialsSettings result;
        try {
            if (Files.exists(SETTINGS_FILE)) {
                String json = Files.readString(SETTINGS_FILE);
                result = GSON.fromJson(json, EssentialsSettings.class);
                if (result == null) result = new EssentialsSettings();
            } else {
                result = new EssentialsSettings();
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = new EssentialsSettings();
        }
        INSTANCE = result;
        return result;
    }

    public void save() {
        try {
            Files.createDirectories(SETTINGS_FILE.getParent());
            Files.writeString(SETTINGS_FILE, GSON.toJson(this));
            INSTANCE = this;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isShowAfkOverlay() {
        return showAfkOverlay;
    }

    public void setShowAfkOverlay(boolean v) {
        showAfkOverlay = v;
    }
}
