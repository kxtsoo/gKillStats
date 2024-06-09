package hu.kitsoo.gkillstats.events;

import hu.kitsoo.gkillstats.GKillStats;
import hu.kitsoo.gkillstats.database.DatabaseManager;
import hu.kitsoo.gkillstats.util.ChatUtil;
import hu.kitsoo.gkillstats.util.ConfigUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.util.List;
import java.util.Random;

public class DeathEvent implements Listener {

    private final GKillStats plugin;
    private final KillstreakHandler killstreakHandler;
    private final KillCooldownManager cooldownManager;
    private final ConfigUtil configUtil;

    public DeathEvent(GKillStats plugin, ConfigUtil configUtil) {
        this.plugin = plugin;
        this.configUtil = configUtil;
        this.killstreakHandler = new KillstreakHandler(plugin, configUtil);
        this.cooldownManager = new KillCooldownManager(configUtil.getConfig().getInt("kill-cooldown-seconds", 60));
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) throws SQLException {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if ((killer instanceof Player && killer.equals(victim)) || killer == null) {
            DatabaseManager.updatePlayerStats(victim.getName(), 0, 1);
            DatabaseManager.updateKillstreak(victim.getName(), 0);

            int victimDeaths = DatabaseManager.getPlayerDeaths(victim.getName());
            String deathMessage = ChatUtil.colorizeHex(configUtil.getMessageWithPrefix("non-player-death-message")
                    .replace("%player%", victim.getName())
                    .replace("%player_deaths%", String.valueOf(victimDeaths)));

            String deathLightning = configUtil.getConfig().getString("death-lightning", "true");

            if (deathLightning.equals("true")) {
                victim.getWorld().strikeLightningEffect(victim.getLocation());
            }

            event.setDeathMessage(deathMessage);
            handleDeathLoss(victim);
            return;
        }

        if (!cooldownManager.canPlayerKill(killer, victim)) {
            event.setDeathMessage(null);
            return;
        }

        String killedPlayerName = victim.getName();
        String killerName = killer.getName();
        ItemStack killerWeapon = killer.getInventory().getItemInMainHand();

        String killMessage = ChatUtil.colorizeHex(configUtil.getConfig().getString("kill-message")
                .replace("%killer%", killerName)
                .replace("%victim%", killedPlayerName));

        if (killerWeapon.getType() != Material.AIR) {
            String weaponName = (killerWeapon.hasItemMeta() && killerWeapon.getItemMeta().hasDisplayName()) ?
                    killerWeapon.getItemMeta().getDisplayName() : killerWeapon.getType().name();
            killMessage = killMessage.replace("%weapon%", weaponName);
        } else {
            killMessage = killMessage.replace("%weapon%", "AIR");
        }

        DatabaseManager.updatePlayerStats(killerName, 1, 0);
        DatabaseManager.updatePlayerStats(killedPlayerName, 0, 1);

        int killerKills = DatabaseManager.getPlayerKills(killerName);
        int victimDeaths = DatabaseManager.getPlayerDeaths(killedPlayerName);

        killMessage = killMessage.replace("%killer_kills%", String.valueOf(killerKills));
        killMessage = killMessage.replace("%victim_deaths%", String.valueOf(victimDeaths));

        String deathLightning = configUtil.getConfig().getString("death-lightning", "true");

        if (deathLightning.equals("true")) {
            victim.getWorld().strikeLightningEffect(victim.getLocation());
        }

        event.setDeathMessage(killMessage);
        handleDeathLoss(victim);
        handleKillRewards(killer);
        handleKillstreak(killer);
        handlePlayerKill(killer, victim, event);
        killstreakHandler.resetKillstreak(victim);
        cooldownManager.updateKillCooldown(killer, victim);

    }

    private void handleKillRewards(Player killer) {
        List<String> killRewards = configUtil.getConfig().getStringList("kill-reward");

        for (String rewardCommand : killRewards) {
            String processedCommand = rewardCommand.replace("%player%", killer.getName());
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), processedCommand);
        }
    }

    private void handleDeathLoss(Player victim) {
        List<String> deathLoss = configUtil.getConfig().getStringList("death-loss");

        for (String rewardCommand : deathLoss) {
            String processedCommand = rewardCommand.replace("%player%", victim.getName());
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), processedCommand);
        }
    }

    private void handleKillstreak(Player killer) throws SQLException {
        killstreakHandler.handleKill(killer);
    }

    private void handlePlayerKill(Player killer, Player victim, PlayerDeathEvent event) {

        List<String> commands = configUtil.getConfig().getStringList("death-commands");
        if (commands != null && !commands.isEmpty()) {
            Random random = new Random();
            String commandTemplate = commands.get(random.nextInt(commands.size()));

            String command = commandTemplate
                    .replace("%killer%", killer.getName())
                    .replace("%victim%", victim.getName())
                    .replace("%world%", victim.getWorld().getName())
                    .replace("%x%", String.valueOf(victim.getLocation().getBlockX()))
                    .replace("%y%", String.valueOf(victim.getLocation().getBlockY()))
                    .replace("%z%", String.valueOf(victim.getLocation().getBlockZ()));

            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }
    }

}