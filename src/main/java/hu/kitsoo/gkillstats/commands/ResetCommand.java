package hu.kitsoo.gkillstats.commands;

import hu.kitsoo.gkillstats.GKillStats;
import hu.kitsoo.gkillstats.database.DatabaseManager;
import hu.kitsoo.gkillstats.util.ChatUtil;
import hu.kitsoo.gkillstats.util.ConfigUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;

import java.sql.SQLException;

public class ResetCommand implements CommandExecutor, Listener {

    private final GKillStats plugin;
    private final ConfigUtil configUtil;

    public ResetCommand(GKillStats plugin, ConfigUtil configUtil) {
        this.plugin = plugin;
        this.configUtil = configUtil;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender.hasPermission("gkillstats.reset") || sender.hasPermission("gkillstats.*") || sender.isOp())) {
            sender.sendMessage(ChatUtil.colorizeHex(configUtil.getMessageWithPrefix("messages.no-permission")));
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage(ChatUtil.colorizeHex(configUtil.getMessageWithPrefix("messages.too-many-args")));
            return true;
        }

        String targetPlayer = args[1];
        try {
            if (targetPlayer.equalsIgnoreCase("*")) {
                DatabaseManager.resetAllPlayerStats();
                sender.sendMessage(ChatUtil.colorizeHex(configUtil.getMessageWithPrefix("messages.reset-all")));
            } else {
                DatabaseManager.resetPlayerStats(sender, targetPlayer);
                sender.sendMessage(ChatUtil.colorizeHex(configUtil.getMessageWithPrefix("messages.reset-player").replace("%player%", targetPlayer)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            sender.sendMessage(ChatColor.RED + "Error resetting player stats.");
        }

        return true;
    }
}