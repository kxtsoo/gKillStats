package hu.kitsoo.gkillstats.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import hu.kitsoo.gkillstats.util.ConfigUtil;

import java.sql.*;
import java.util.logging.Logger;

public class MySQLDatabaseManager implements DatabaseInterface {
    private HikariDataSource dataSource;
    private static final Logger LOGGER = Logger.getLogger(MySQLDatabaseManager.class.getName());

    @Override
    public void initialize(ConfigUtil configUtil) throws SQLException {
        HikariConfig config = new HikariConfig();
        String host = configUtil.getConfig().getString("database.host");
        int port = configUtil.getConfig().getInt("database.port");
        String databaseName = configUtil.getConfig().getString("database.name");
        String username = configUtil.getConfig().getString("database.username");
        String password = configUtil.getConfig().getString("database.password");

        String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + databaseName + "?useSSL=true";
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);

        config.setMaximumPoolSize(configUtil.getConfig().getInt("database.pool.maximumPoolSize", 10));
        config.setMinimumIdle(configUtil.getConfig().getInt("database.pool.minimumIdle", 5));
        config.setConnectionTimeout(configUtil.getConfig().getLong("database.pool.connectionTimeout", 30000L));
        config.setMaxLifetime(configUtil.getConfig().getLong("database.pool.maxLifetime", 1800000L));
        config.setIdleTimeout(configUtil.getConfig().getLong("database.pool.idleTimeout", 600000L));

        dataSource = new HikariDataSource(config);
        createDatabaseTables();
    }

    @Override
    public void initialize() {
        throw new UnsupportedOperationException("No default initialization for MySQL");
    }

    @Override
    public void createDatabaseTables() throws SQLException {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS gks_players (" +
                    "id INTEGER PRIMARY KEY AUTO_INCREMENT, " +
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
