package hu.kitsoo.gkillstats;

import hu.kitsoo.gkillstats.commands.ReloadCommand;
import hu.kitsoo.gkillstats.commands.ResetCommand;
import hu.kitsoo.gkillstats.commands.StatsCommand;
import hu.kitsoo.gkillstats.util.ChatUtil;
import hu.kitsoo.gkillstats.util.ConfigUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GKillStatsCommand implements CommandExecutor {

    private final GKillStats plugin;
    private final ConfigUtil configUtil;
    private final Map<String, Map<String, CommandExecutor>> commands = new HashMap<>();

    public GKillStatsCommand(GKillStats plugin, ConfigUtil configUtil) {
        this.plugin = plugin;
        this.configUtil = configUtil;
        initializeCommands();
    }

    private void initializeCommands() {
        Map<String, CommandExecutor> guildCommands = new HashMap<>();
        guildCommands.put("reload", new ReloadCommand(plugin, configUtil));
        guildCommands.put("reset", new ResetCommand(plugin, configUtil));
        guildCommands.put("stats", new StatsCommand(plugin, configUtil));

        commands.put("gkillstats", guildCommands);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            List<String> helpMenu = configUtil.getMessages().getStringList("messages.help-menu");
            for (String line : helpMenu) {
                sender.sendMessage(ChatUtil.colorizeHex(line));
            }
            return true;
        }

        String subCommand = args[0].toLowerCase();
        Map<String, CommandExecutor> availableCommands = commands.get(command.getName().toLowerCase());

        if (availableCommands != null) {
            CommandExecutor executor = availableCommands.get(subCommand);
            if (executor != null) {
                return executor.onCommand(sender, command, label, args);
            } else {
                List<String> helpMenu = configUtil.getMessages().getStringList("messages.help-menu");
                for (String line : helpMenu) {
                    sender.sendMessage(ChatUtil.colorizeHex(line));
                }
                return true;
            }
        } else {
            sender.sendMessage("No commands registered for " + command.getName());
            return true;
        }
    }
}