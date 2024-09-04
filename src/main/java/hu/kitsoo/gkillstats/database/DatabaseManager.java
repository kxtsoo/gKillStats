package hu.kitsoo.gkillstats.database;

import hu.kitsoo.gkillstats.util.ConfigUtil;
import org.bukkit.command.CommandSender;

import java.sql.SQLException;

public class DatabaseManager {
    private static DatabaseInterface database;

    public static void initialize(ConfigUtil configUtil) throws SQLException {
        String driver = configUtil.getConfig().getString("database.driver");
        switch (driver.toLowerCase()) {
            case "sqlite":
                database = new SQLiteDatabaseManager();
                database.initialize();
                break;
            case "mysql":
                database = new MySQLDatabaseManager();
                database.initialize(configUtil);
                break;
            default:
                throw new IllegalArgumentException("Unsupported database driver: " + driver);
        }
        database.createDatabaseTables();
    }

    public static void updatePlayerStats(String playerName, int kills, int deaths) throws SQLException {
        database.updatePlayerStats(playerName, kills, deaths);
    }

    public static int getPlayerDeaths(String playerName) throws SQLException {
        return database.getPlayerDeaths(playerName);
    }

    public static int getPlayerKills(String playerName) throws SQLException {
        return database.getPlayerKills(playerName);
    }

    public static String getPlayerWithNthMostKills(int position) throws SQLException {
        return database.getPlayerWithNthMostKills(position);
    }

    public static int getNthMostKillsValue(int position) throws SQLException {
        return database.getNthMostKillsValue(position);
    }

    public static String getPlayerWithNthMostDeaths(int position) throws SQLException {
        return database.getPlayerWithNthMostDeaths(position);
    }

    public static int getNthMostDeathsValue(int position) throws SQLException {
        return database.getNthMostDeathsValue(position);
    }

    public static void resetPlayerStats(CommandSender sender, String playerName) throws SQLException {
        database.resetPlayerStats(playerName);
    }

    public static void resetAllPlayerStats() throws SQLException {
        database.resetAllPlayerStats();
    }

    public static void updateKillstreak(String playerName, int killstreak) throws SQLException {
        database.updateKillstreak(playerName, killstreak);
    }

    public static int getKillstreak(String playerName) throws SQLException {
        return database.getKillstreak(playerName);
    }

    public static void close() throws SQLException {
        if (database != null) {
            database.close();
        }
    }

    public static String getDatabaseType() {
        return database instanceof MySQLDatabaseManager ? "mysql" : "sqlite";
    }
}
