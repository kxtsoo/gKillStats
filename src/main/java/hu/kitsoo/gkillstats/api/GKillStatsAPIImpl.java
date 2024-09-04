package hu.kitsoo.gkillstats.api;

import hu.kitsoo.gkillstats.database.DatabaseManager;
import hu.kitsoo.gkillstats.util.ConfigUtil;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.stream.Stream;

public class GKillStatsAPIImpl implements GKillStatsAPI {

    private final ConfigUtil configUtil;

    public GKillStatsAPIImpl(JavaPlugin plugin) {
        this.configUtil = new ConfigUtil(plugin);
        this.configUtil.setupConfig();
    }

    @Override
    public int getKills(String playerName) {
        try {
            return DatabaseManager.getPlayerKills(playerName);
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public boolean isValidKill(String killerName, String victimName) {
        long currentTime = System.currentTimeMillis();
        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date(currentTime));
        String logFileName = "plugins/gKillStats/logs/" + date + ".log";
        int killCount = 0;
        int timePeriodMilliseconds = configUtil.getConfig().getInt("kill-limits.time-period-seconds") * 1000;
        int maxKills = configUtil.getConfig().getInt("kill-limits.max-kills");

        File logFile = new File(logFileName);
        if (!logFile.exists()) {
            return true;
        }

        try (Stream<String> lines = Files.lines(Paths.get(logFileName))) {
            for (String line : (Iterable<String>) lines::iterator) {
                if (line.contains(killerName + " killed " + victimName)) {
                    String[] parts = line.split(" - ");
                    String logTimeStr = parts[0].trim();

                    try {
                        long logTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(logTimeStr).getTime();
                        if (currentTime - logTime <= timePeriodMilliseconds) {
                            killCount++;
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return killCount < maxKills;
    }
}