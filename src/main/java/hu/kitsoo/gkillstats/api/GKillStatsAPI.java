package hu.kitsoo.gkillstats.api;

public interface GKillStatsAPI {
    int getKills(String playerName);
    boolean isValidKill(String killerName, String victimName);
}
