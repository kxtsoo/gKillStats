package hu.kitsoo.gkillstats.database;

import hu.kitsoo.gkillstats.util.ConfigUtil;

import java.sql.SQLException;

public interface DatabaseInterface {

    void initialize(ConfigUtil configUtil) throws SQLException;
    void initialize();

    void createDatabaseTables() throws SQLException;

    void updatePlayerStats(String playerName, int kills, int deaths) throws SQLException;
    int getPlayerDeaths(String playerName) throws SQLException;
    int getPlayerKills(String playerName) throws SQLException;
    String getPlayerWithNthMostKills(int position) throws SQLException;
    int getNthMostKillsValue(int position) throws SQLException;
    String getPlayerWithNthMostDeaths(int position) throws SQLException;
    int getNthMostDeathsValue(int position) throws SQLException;
    void resetPlayerStats(String playerName) throws SQLException;
    void resetAllPlayerStats() throws SQLException;
    void updateKillstreak(String playerName, int killstreak) throws SQLException;
    int getKillstreak(String playerName) throws SQLException;

    void close() throws SQLException;
}
