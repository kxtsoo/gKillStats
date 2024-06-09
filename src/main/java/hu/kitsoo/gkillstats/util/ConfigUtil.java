package hu.kitsoo.gkillstats.util;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class ConfigUtil {

    private final JavaPlugin plugin;
    private YamlDocument config;
    private YamlDocument messages;

    public ConfigUtil(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void setupConfig() {
        try {
            File configFile = new File(plugin.getDataFolder(), "config.yml");
            config = YamlDocument.create(configFile, Objects.requireNonNull(plugin.getResource("config.yml")),
                    GeneralSettings.builder().setUseDefaults(false).build(),
                    LoaderSettings.DEFAULT, DumperSettings.DEFAULT,
                    UpdaterSettings.builder().setKeepAll(true)
                            .setVersioning(new BasicVersioning("config-version")).build());

            File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
            messages = YamlDocument.create(messagesFile, Objects.requireNonNull(plugin.getResource("messages.yml")),
                    GeneralSettings.builder().setUseDefaults(false).build(),
                    LoaderSettings.DEFAULT, DumperSettings.DEFAULT,
                    UpdaterSettings.builder().setKeepAll(true)
                            .setVersioning(new BasicVersioning("messages-version")).build());

            config.update();
            messages.update();
        } catch (IOException ex) {
            plugin.getLogger().severe("Error loading or creating config.yml file: " + ex.getMessage());
        }
    }

    public YamlDocument getConfig() {
        return config;
    }

    public YamlDocument getMessages() {
        return messages;
    }

    public void reloadConfig() {
        try {
            config.reload();
            messages.reload();
        } catch (IOException ex) {
            plugin.getLogger().severe("Error reloading config.yml file: " + ex.getMessage());
        }
    }

    public void saveConfig() {
        try {
            config.save();
            messages.save();
        } catch (IOException ex) {
            plugin.getLogger().severe("Error saving config.yml file: " + ex.getMessage());
        }
    }

    public String getMessageWithPrefix(String key) {
        String prefix = config.getString("prefix", "&7[&aGuild&7] &r");
        String message = messages.getString(key, "");
        if (message.contains("%prefix%")) {
            message = message.replace("%prefix%", prefix);
        } else {
            message = prefix + " " + message;
        }
        return ChatUtil.colorizeHex(message);
    }
}