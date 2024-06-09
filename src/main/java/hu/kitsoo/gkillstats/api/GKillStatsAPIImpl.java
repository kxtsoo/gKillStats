package hu.kitsoo.gkillstats.api;

import hu.kitsoo.gkillstats.database.DatabaseManager;

import java.sql.SQLException;

public class GKillStatsAPIImpl implements GKillStatsAPI {

    @Override
    public int getKills(String playerName) {
        try {
            return DatabaseManager.getPlayerKills(playerName);
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
}