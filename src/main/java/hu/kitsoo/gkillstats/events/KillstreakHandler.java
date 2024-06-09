package hu.kitsoo.gkillstats.events;

import hu.kitsoo.gkillstats.GKillStats;
import hu.kitsoo.gkillstats.database.DatabaseManager;
import hu.kitsoo.gkillstats.util.ChatUtil;
import hu.kitsoo.gkillstats.util.ConfigUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.List;

public class KillstreakHandler {

    private final GKillStats plugin;
    private final ConfigUtil configUtil;

    public KillstreakHandler(GKillStats plugin, ConfigUtil configUtil) {
        this.plugin = plugin;
        this.configUtil = configUtil;
    }

    public void handleKill(Player player) throws SQLException {
        int currentKillstreak = DatabaseManager.getKillstreak(player.getName()) + 1;
        DatabaseManager.updateKillstreak(player.getName(), currentKillstreak);

        List<String> killstreakRewards = configUtil.getConfig().getStringList("killstreak-rewards." + currentKillstreak + ".rewards");
        String killstreakMessage = configUtil.getConfig().getString("killstreak-rewards." + currentKillstreak + ".message");
        String killstreakBroadcast = configUtil.getConfig().getString("killstreak-rewards." + currentKillstreak + ".broadcast");

        if (killstreakMessage != null && !killstreakMessage.isEmpty()) {
            player.sendMessage(ChatUtil.colorizeHex(killstreakMessage.replace("%player%", player.getName())));
        }

        if (killstreakBroadcast != null && !killstreakBroadcast.isEmpty()) {
            Bukkit.broadcastMessage(ChatUtil.colorizeHex(killstreakBroadcast.replace("%player%", player.getName())));
        }

        executeCommands(player, killstreakRewards);
    }

    public void resetKillstreak(Player player) throws SQLException {
        DatabaseManager.updateKillstreak(player.getName(), 0);
    }

    private void executeCommands(Player player, List<String> commands) {
        if (commands != null && !commands.isEmpty()) {
            for (String command : commands) {
                String processedCommand = command.replace("%player%", player.getName());
                plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), processedCommand);
            }
        }
    }
}