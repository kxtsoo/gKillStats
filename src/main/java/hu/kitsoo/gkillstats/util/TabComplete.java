package hu.kitsoo.gkillstats.util;

import hu.kitsoo.gkillstats.GKillStats;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class TabComplete implements TabCompleter {

    private final GKillStats plugin;

    public TabComplete (GKillStats plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("reload");
            completions.add("stats");
            completions.add("reset");
        } else if (args.length == 2 && "reset".equalsIgnoreCase(args[0])) {
            completions.add("*");
            completions.addAll(getOnlinePlayerNames());
        } else if (args.length == 2 && "stats".equalsIgnoreCase(args[0])) {
            completions.addAll(getOnlinePlayerNames());
        }

        return completions;
    }
    private List<String> getOnlinePlayerNames() {
        List<String> onlinePlayerNames = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            onlinePlayerNames.add(player.getName());
        }
        return onlinePlayerNames;
    }

}
