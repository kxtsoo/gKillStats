package hu.kitsoo.gkillstats.util;

import hu.kitsoo.gkillstats.GKillStats;
import hu.kitsoo.gkillstats.database.DatabaseManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;

public class PlaceholderAPISupport extends PlaceholderExpansion {

    private final GKillStats plugin;

    public PlaceholderAPISupport (GKillStats plugin) {
        this.plugin = plugin;
    }


    @Override
    public @NotNull String getIdentifier() {
        return "gKillStats";
    }

    @Override
    public @NotNull String getAuthor() {
        return "kitsoo_";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.1";
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return "---";
        }

        if(params.equals("deaths")) {
            int deaths = 0;
            try {
                deaths = DatabaseManager.getPlayerDeaths(player.getName());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return String.valueOf(deaths);
        }

        if(params.equals("kills")) {
            int kills = 0;
            try {
                kills = DatabaseManager.getPlayerKills(player.getName());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return String.valueOf(kills);
        }

        if (params.equals("killstreak")) {
            int killstreak = 0;
            try {
                killstreak = DatabaseManager.getKillstreak(player.getName());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return String.valueOf(killstreak);
        }

        if (params.equals("kd")) {
            int kills = 0;
            int deaths = 0;
            try {
                kills = DatabaseManager.getPlayerKills(player.getName());
                deaths = DatabaseManager.getPlayerDeaths(player.getName());
            } catch (SQLException e) {
                e.printStackTrace();
                return "Error";
            }

            if (deaths == 0) {
                return String.valueOf(kills);
            }

            double kdRatio = (double) kills / deaths;
            return String.format("%.2f", kdRatio);
        }

        if (params.startsWith("most_kills_name_")) {
            int position = Integer.parseInt(params.replace("most_kills_name_", ""));
            try {
                return DatabaseManager.getPlayerWithNthMostKills(position);
            } catch (SQLException e) {
                e.printStackTrace();
                return "Error";
            }
        }

        if (params.startsWith("most_kills_value_")) {
            int position = Integer.parseInt(params.replace("most_kills_value_", ""));
            try {
                return String.valueOf(DatabaseManager.getNthMostKillsValue(position));
            } catch (SQLException e) {
                e.printStackTrace();
                return "Error";
            }
        }

        if (params.startsWith("most_deaths_name_")) {
            int position = Integer.parseInt(params.replace("most_deaths_name_", ""));
            try {
                return DatabaseManager.getPlayerWithNthMostDeaths(position);
            } catch (SQLException e) {
                e.printStackTrace();
                return "Error";
            }
        }

        if (params.startsWith("most_deaths_value_")) {
            int position = Integer.parseInt(params.replace("most_deaths_value_", ""));
            try {
                return String.valueOf(DatabaseManager.getNthMostDeathsValue(position));
            } catch (SQLException e) {
                e.printStackTrace();
                return "Error";
            }
        }

        return null;
    }
}
