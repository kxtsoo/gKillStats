package hu.kitsoo.gkillstats.commands;

import hu.kitsoo.gkillstats.GKillStats;
import hu.kitsoo.gkillstats.util.ChatUtil;
import hu.kitsoo.gkillstats.util.ConfigUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;

import java.util.List;

public class ReloadCommand implements CommandExecutor, Listener {

    private final GKillStats plugin;
    private final ConfigUtil configUtil;

    public ReloadCommand(GKillStats plugin, ConfigUtil configUtil) {
        this.plugin = plugin;
        this.configUtil = configUtil;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender.hasPermission("gkillstats.reload") || sender.hasPermission("gkillstats.*") || sender.isOp())) {
            sender.sendMessage(ChatUtil.colorizeHex(configUtil.getMessageWithPrefix("messages.no-permission")));
            return true;
        }

        List<String> helpMenuList = configUtil.getMessages().getStringList("messages.help-menu");
        StringBuilder helpMenuBuilder = new StringBuilder();

        for (String line : helpMenuList) {
            helpMenuBuilder.append(line).append("\n");
        }

        String helpMenu = helpMenuBuilder.toString().trim();

        helpMenu = ChatUtil.colorizeHex(helpMenu);

        if (args.length != 1 || !args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage(helpMenu);
            return false;
        }

        reloadPlugin(sender);

        return true;
    }

    private void reloadPlugin(CommandSender sender) {
        try {
            configUtil.reloadConfig();
            sender.sendMessage(ChatUtil.colorizeHex(configUtil.getMessageWithPrefix("messages.reload-success")));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
