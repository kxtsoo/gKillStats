package hu.kitsoo.gkillstats.commands;

import hu.kitsoo.gkillstats.GKillStats;
import hu.kitsoo.gkillstats.database.DatabaseManager;
import hu.kitsoo.gkillstats.util.ChatUtil;
import hu.kitsoo.gkillstats.util.ConfigUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.sql.SQLException;
import java.util.List;

public class StatsCommand implements CommandExecutor, Listener {

    private final GKillStats plugin;
    private final ConfigUtil configUtil;

    public StatsCommand(GKillStats plugin, ConfigUtil configUtil) {
        this.plugin = plugin;
        this.configUtil = configUtil;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender.hasPermission("gkillstats.stats") || sender.hasPermission("gkillstats.*") || sender.isOp())) {
            sender.sendMessage(ChatUtil.colorizeHex(configUtil.getMessageWithPrefix("messages.no-permission")));
            return true;
        }

        String subCommand = (args.length > 0) ? args[0].toLowerCase() : "";

        if (subCommand.equals("stats")) {
            String playerName = (args.length > 1) ? args[1] : (sender instanceof Player ? ((Player) sender).getName() : "");
            if (playerName.isEmpty()) {
                sender.sendMessage(ChatUtil.colorizeHex(configUtil.getMessageWithPrefix("messages.player-not-found")));
                return true;
            }
            sendPlayerStats(sender, playerName);
        } else {
            sender.sendMessage(ChatUtil.colorizeHex(configUtil.getMessageWithPrefix("messages.unknown-command")));
        }

        return true;
    }

    private void sendPlayerStats(CommandSender sender, String playerName) {
        try {
            int kills = DatabaseManager.getPlayerKills(playerName);
            int deaths = DatabaseManager.getPlayerDeaths(playerName);

            List<String> statMessageList = configUtil.getMessages().getStringList("messages.stat-message");
            StringBuilder statMessageBuilder = new StringBuilder();

            for (String line : statMessageList) {
                statMessageBuilder.append(line).append("\n");
            }

            String statMessage = statMessageBuilder.toString().trim();
            statMessage = ChatUtil.colorizeHex(statMessage);

            double kdRatio = deaths == 0 ? kills : (double) kills / deaths;

            statMessage = statMessage.replace("%player%", playerName);
            statMessage = statMessage.replace("%kills%", String.valueOf(kills));
            statMessage = statMessage.replace("%deaths%", String.valueOf(deaths));
            statMessage = statMessage.replace("%kd%", String.format("%.2f", kdRatio));

            sender.sendMessage(statMessage);
        } catch (SQLException e) {
            sender.sendMessage(ChatUtil.colorizeHex(configUtil.getMessageWithPrefix("messages.database-error")));
            e.printStackTrace();
        }
    }
}