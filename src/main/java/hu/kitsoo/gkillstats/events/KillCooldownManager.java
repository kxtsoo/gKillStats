package hu.kitsoo.gkillstats.events;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class KillCooldownManager {

    private final Map<String, Long> killCooldowns;
    private final long cooldownDuration;

    public KillCooldownManager(int cooldownSeconds) {
        this.killCooldowns = new HashMap<>();
        this.cooldownDuration = cooldownSeconds * 1000;
    }

    public boolean canPlayerKill(Player killer, Player victim) {
        String key = generateCooldownKey(killer, victim);

        if (killCooldowns.containsKey(key)) {
            long lastKillTime = killCooldowns.get(key);
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - lastKillTime;

            return elapsedTime >= cooldownDuration;
        }

        return true;
    }

    public void updateKillCooldown(Player killer, Player victim) {
        String key = generateCooldownKey(killer, victim);
        killCooldowns.put(key, System.currentTimeMillis());
    }

    private String generateCooldownKey(Player killer, Player victim) {
        return killer.getName() + "-" + victim.getName();
    }
}