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

        if (args.length == 0) {
            return true;
        }

        String subCommand = args[0].toLowerCase();

        if (subCommand.equals("stats")) {
            if (args.length > 2) {
                sender.sendMessage(ChatUtil.colorizeHex(configUtil.getMessageWithPrefix("messages.too-many-args")));
                return true;
            }
            String playerName = (args.length > 1) ? args[1] : (sender instanceof Player ? ((Player) sender).getName() : "");
            sendPlayerStats(sender, playerName);
        } else {
            sender.sendMessage(ChatUtil.colorizeHex(configUtil.getMessageWithPrefix("messages.too-many-args")));
        }

        return true;
    }

    private void sendPlayerStats(CommandSender sender, String playerName) {
        try {
            int kills = DatabaseManager.getPlayerKills(playerName);
            int deaths = DatabaseManager.getPlayerDeaths(playerName);

            List<String> helpMenuList = configUtil.getMessages().getStringList("stat-message");
            StringBuilder helpMenuBuilder = new StringBuilder();

            for (String line : helpMenuList) {
                helpMenuBuilder.append(line).append("\n");
            }

            String helpMenu = helpMenuBuilder.toString().trim();
            helpMenu = ChatUtil.colorizeHex(helpMenu);

            double kdRatio = deaths == 0 ? kills : (double) kills / deaths;

            helpMenu = helpMenu.replace("%player%", playerName);
            helpMenu = helpMenu.replace("%kills%", String.valueOf(kills));
            helpMenu = helpMenu.replace("%deaths%", String.valueOf(deaths));
            helpMenu = helpMenu.replace("%kd%", String.format("%.2f", kdRatio));

            sender.sendMessage(helpMenu);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}