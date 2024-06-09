package hu.kitsoo.gkillstats.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import hu.kitsoo.gkillstats.util.ConfigUtil;
import org.bukkit.Bukkit;

import java.sql.*;

public class SQLiteDatabaseManager implements DatabaseInterface {
    private HikariDataSource dataSource;

    public void initialize() {
        HikariConfig config = new HikariConfig();
        String jdbcUrl = "jdbc:sqlite:plugins/gKillStats/database.db";
        config.setJdbcUrl(jdbcUrl);

        dataSource = new HikariDataSource(config);

        try {
            createDatabaseTables();
            Bukkit.getLogger().info("SQLite player table created successfully");
        } catch (SQLException e) {
            Bukkit.getLogger().info("Failed to create SQLite player table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(ConfigUtil configUtil) {
        throw new UnsupportedOperationException("Config not used for SQLite initialization");
    }

    @Override
    public void createDatabaseTables() throws SQLException {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS gks_players (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "player_name TEXT NOT NULL, " +
                    "kills INT DEFAULT 0, " +
                    "deaths INT DEFAULT 0, " +
                    "killstreak INT DEFAULT 0)");
        }
    }

    @Override
    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    @Override
    public void updatePlayerStats(String playerName, int kills, int deaths) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement("SELECT * FROM gks_players WHERE player_name = ?");
             PreparedStatement updateStmt = conn.prepareStatement("UPDATE gks_players SET kills = kills + ?, deaths = deaths + ? WHERE player_name = ?");
             PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO gks_players (player_name, kills, deaths) VALUES (?, ?, ?)")) {

            checkStmt.setString(1, playerName);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                updateStmt.setInt(1, kills);
                updateStmt.setInt(2, deaths);
                updateStmt.setString(3, playerName);
                updateStmt.executeUpdate();
            } else {
                insertStmt.setString(1, playerName);
                insertStmt.setInt(2, kills);
                insertStmt.setInt(3, deaths);
                insertStmt.executeUpdate();
            }
        }
    }

    @Override
    public int getPlayerDeaths(String playerName) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT deaths FROM gks_players WHERE player_name = ?")) {
            stmt.setString(1, playerName);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt("deaths") : 0;
        }
    }

    @Override
    public int getPlayerKills(String playerName) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT kills FROM gks_players WHERE player_name = ?")) {
            stmt.setString(1, playerName);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt("kills") : 0;
        }
    }

    @Override
    public String getPlayerWithNthMostKills(int position) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT player_name FROM gks_players ORDER BY kills DESC LIMIT 1 OFFSET ?")) {
            stmt.setInt(1, position - 1);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getString("player_name") : "---";
        }
    }

    @Override
    public int getNthMostKillsValue(int position) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT kills FROM gks_players ORDER BY kills DESC LIMIT 1 OFFSET ?")) {
            stmt.setInt(1, position - 1);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt("kills") : 0;
        }
    }

    @Override
    public String getPlayerWithNthMostDeaths(int position) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT player_name FROM gks_players ORDER BY deaths DESC LIMIT 1 OFFSET ?")) {
            stmt.setInt(1, position - 1);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getString("player_name") : "---";
        }
    }

    @Override
    public int getNthMostDeathsValue(int position) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT deaths FROM gks_players ORDER BY deaths DESC LIMIT 1 OFFSET ?")) {
            stmt.setInt(1, position - 1);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt("deaths") : 0;
        }
    }

    @Override
    public void resetPlayerStats(String playerName) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE gks_players SET kills = 0, deaths = 0 WHERE player_name = ?")) {
            stmt.setString(1, playerName);
            stmt.executeUpdate();
        }
    }

    @Override
    public void resetAllPlayerStats() throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE gks_players SET kills = 0, deaths = 0, killstreak = 0")) {
            stmt.executeUpdate();
        }
    }

    @Override
    public void updateKillstreak(String playerName, int killstreak) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE gks_players SET killstreak = ? WHERE player_name = ?")) {
            stmt.setInt(1, killstreak);
            stmt.setString(2, playerName);
            stmt.executeUpdate();
        }
    }

    @Override
    public int getKillstreak(String playerName) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT killstreak FROM gks_players WHERE player_name = ?")) {
            stmt.setString(1, playerName);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt("killstreak") : 0;
        }
    }
}
