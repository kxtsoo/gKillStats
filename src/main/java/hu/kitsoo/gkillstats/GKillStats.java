package hu.kitsoo.gkillstats;

import hu.kitsoo.gkillstats.api.GKillStatsAPI;
import hu.kitsoo.gkillstats.api.GKillStatsAPIImpl;
import hu.kitsoo.gkillstats.database.DatabaseManager;
import hu.kitsoo.gkillstats.events.DeathEvent;
import hu.kitsoo.gkillstats.util.ConfigUtil;
import hu.kitsoo.gkillstats.util.PlaceholderAPISupport;
import hu.kitsoo.gkillstats.util.TabComplete;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.SQLException;
import java.util.logging.Level;

public final class GKillStats extends JavaPlugin {

    private static GKillStats instance;
    private static GKillStatsAPI api;
    private ConfigUtil configUtil;

    @Override
    public void onEnable() {
        instance = this;
        configUtil = new ConfigUtil(this);
        configUtil.setupConfig();

        File logsDir = new File(getDataFolder(), "logs");
        if (!logsDir.exists()) {
            logsDir.mkdirs();
        }

        System.out.println(ChatColor.GREEN + "The plugin successfully enabled.");
        System.out.println(ChatColor.GREEN + "Plugin developed by Glowing Studios. https://discord.gg/esxwNC4DmZ");

        try {
            DatabaseManager.initialize(configUtil);
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to initialize database", e);
        }

        api = new GKillStatsAPIImpl(this);

        getServer().getPluginManager().registerEvents(new DeathEvent(this, configUtil), this);
        getCommand("gkillstats").setTabCompleter(new TabComplete(this));

        GKillStatsCommand commandExecutor = new GKillStatsCommand(this, configUtil);
        getCommand("gkillstats").setExecutor(commandExecutor);

        getLogger().info("Selected database type: " + DatabaseManager.getDatabaseType());

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderAPISupport(this).register();
        }
    }

    @Override
    public void onDisable() {
        try {
            DatabaseManager.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static GKillStatsAPI getAPI() {
        return api;
    }
    public static GKillStats getInstance() {
        return instance;
    }
}
