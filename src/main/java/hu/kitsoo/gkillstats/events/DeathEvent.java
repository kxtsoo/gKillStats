package hu.kitsoo.gkillstats.events;

import hu.kitsoo.gkillstats.GKillStats;
import hu.kitsoo.gkillstats.api.GKillStatsAPI;
import hu.kitsoo.gkillstats.api.GKillStatsAPIImpl;
import hu.kitsoo.gkillstats.database.DatabaseManager;
import hu.kitsoo.gkillstats.util.ChatUtil;
import hu.kitsoo.gkillstats.util.ConfigUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class DeathEvent implements Listener {

    private final GKillStats plugin;
    private final KillstreakHandler killstreakHandler;
    private final ConfigUtil configUtil;
    private final GKillStatsAPI gKillStatsAPI;

    public DeathEvent(GKillStats plugin, ConfigUtil configUtil) {
        this.plugin = plugin;
        this.configUtil = configUtil;
        this.killstreakHandler = new KillstreakHandler(plugin, configUtil);
        this.gKillStatsAPI = new GKillStatsAPIImpl(plugin);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer == null || killer.equals(victim)) {
            handleNonPlayerDeath(event, victim);
            return;
        }

        if (!gKillStatsAPI.isValidKill(killer.getName(), victim.getName())) {
            event.setDeathMessage(null);
            return;
        }

        handlePlayerDeath(event, victim, killer);
    }

    private void handleNonPlayerDeath(PlayerDeathEvent event, Player victim) {
        try {
            DatabaseManager.updatePlayerStats(victim.getName(), 0, 1);
            DatabaseManager.updateKillstreak(victim.getName(), 0);

            int victimDeaths = DatabaseManager.getPlayerDeaths(victim.getName());
            String deathMessage = ChatUtil.colorizeHex(configUtil.getMessageWithPrefix("messages.non-player-death-message")
                    .replace("%player%", victim.getName())
                    .replace("%player_deaths%", String.valueOf(victimDeaths)));

            event.setDeathMessage(deathMessage);
            if (configUtil.getConfig().getBoolean("death-lightning", true)) {
                victim.getWorld().strikeLightningEffect(victim.getLocation());
            }

            handleDeathLoss(victim);
        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getLogger().severe("Error updating non-player death stats: " + e.getMessage());
        }
    }

    private void handlePlayerDeath(PlayerDeathEvent event, Player victim, Player killer) {
        try {
            DatabaseManager.updatePlayerStats(killer.getName(), 1, 0);
            DatabaseManager.updatePlayerStats(victim.getName(), 0, 1);

            int killerKills = DatabaseManager.getPlayerKills(killer.getName());
            int victimDeaths = DatabaseManager.getPlayerDeaths(victim.getName());

            logKillEvent(killer, victim);

            String killMessageTemplate = configUtil.getMessageWithPrefix("messages.kill-message");
            String weaponName = killer.getInventory().getItemInMainHand().getType().name();

            String killMessage = ChatUtil.colorizeHex(killMessageTemplate
                    .replace("%killer%", killer.getName())
                    .replace("%victim%", victim.getName())
                    .replace("%weapon%", weaponName)
                    .replace("%killer_kills%", String.valueOf(killerKills))
                    .replace("%victim_deaths%", String.valueOf(victimDeaths)));

            event.setDeathMessage(killMessage);
            if (configUtil.getConfig().getBoolean("death-lightning", true)) {
                victim.getWorld().strikeLightningEffect(victim.getLocation());
            }

            handleDeathLoss(victim);
            handleKillRewards(killer);
            handleKillstreak(killer);
            handlePlayerKill(killer, victim, event);
            killstreakHandler.resetKillstreak(victim);
        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getLogger().severe("Error handling player death: " + e.getMessage());
        }
    }

    private void logKillEvent(Player killer, Player victim) throws SQLException {
        long currentTime = System.currentTimeMillis();
        String formattedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(currentTime));
        String logFileName = plugin.getDataFolder().getAbsolutePath() + "/logs/" + new SimpleDateFormat("yyyy-MM-dd").format(new Date(currentTime)) + ".log";

        String logEntry = formattedDate + " - " +
                killer.getName() + " killed " +
                victim.getName() + " (" +
                victim.getWorld().getName() + ", " +
                victim.getLocation().getBlockX() + ", " +
                victim.getLocation().getBlockY() + ", " +
                victim.getLocation().getBlockZ() + ")";

        try {
            File logFile = new File(logFileName);
            logFile.getParentFile().mkdirs();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
                writer.write(logEntry);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleKillRewards(Player killer) {
        List<String> killRewards = configUtil.getConfig().getStringList("kill-reward");

        new BukkitRunnable() {
            @Override
            public void run() {
                for (String rewardCommand : killRewards) {
                    String processedCommand = rewardCommand.replace("%player%", killer.getName());
                    plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), processedCommand);
                }
            }
        }.runTask(plugin);
    }

    private void handleDeathLoss(Player victim) {
        List<String> deathLoss = configUtil.getConfig().getStringList("death-loss");

        new BukkitRunnable() {
            @Override
            public void run() {
                for (String rewardCommand : deathLoss) {
                    String processedCommand = rewardCommand.replace("%player%", victim.getName());
                    plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), processedCommand);
                }
            }
        }.runTask(plugin);
    }

    private void handleKillstreak(Player killer) {
        try {
            killstreakHandler.handleKill(killer);
        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getLogger().severe("Error handling killstreak: " + e.getMessage());
        }
    }

    private void handlePlayerKill(Player killer, Player victim, PlayerDeathEvent event) {
        List<String> commands = configUtil.getConfig().getStringList("death-commands");
        if (commands != null && !commands.isEmpty()) {
            String commandTemplate = commands.get(new Random().nextInt(commands.size()));

            String command = commandTemplate
                    .replace("%killer%", killer.getName())
                    .replace("%victim%", victim.getName())
                    .replace("%world%", victim.getWorld().getName())
                    .replace("%x%", String.valueOf(victim.getLocation().getBlockX()))
                    .replace("%y%", String.valueOf(victim.getLocation().getBlockY()))
                    .replace("%z%", String.valueOf(victim.getLocation().getBlockZ()));

            new BukkitRunnable() {
                @Override
                public void run() {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                }
            }.runTask(plugin);
        }
    }
}
