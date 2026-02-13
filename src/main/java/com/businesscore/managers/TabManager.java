package com.businesscore.managers;

import com.businesscore.BusinessCore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.businesscore.BusinessCore.color;

/**
 * Lightweight TAB implementation (no TAB plugin required).
 * Shows gender + rank + money in the player list using scoreboard team prefix/suffix.
 */
public class TabManager {

    private final BusinessCore plugin;
    private final Scoreboard board;

    // One team per player (stable name based on UUID hash; max 16 chars).
    private final Map<UUID, String> teamNames = new ConcurrentHashMap<>();

    public TabManager(BusinessCore plugin) {
        this.plugin = plugin;
        this.board = Bukkit.getScoreboardManager().getMainScoreboard();
    }

    public void updateAll() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            updatePlayer(p);
        }
    }

    public void updatePlayer(Player player) {
        if (player == null) return;

        // Make sure player sees main scoreboard (teams affect TAB)
        try {
            player.setScoreboard(board);
        } catch (Throwable ignored) {
        }

        String teamName = teamNames.computeIfAbsent(player.getUniqueId(), this::makeTeamName);
        Team team = board.getTeam(teamName);
        if (team == null) {
            team = board.registerNewTeam(teamName);
        }

        if (!team.hasEntry(player.getName())) {
            // Remove from old teams (if player renamed teams)
            for (Team t : board.getTeams()) {
                if (t != team && t.hasEntry(player.getName())) {
                    t.removeEntry(player.getName());
                }
            }
            team.addEntry(player.getName());
        }

        String prefix = plugin.getConfig().getString(
                "tab-prefix-format",
                "%businesscore_gender% %businesscore_rank% &7"
        );
        String suffix = plugin.getConfig().getString(
                "tab-suffix-format",
                " &8| &e%skript_balance%" + plugin.getCurrencySymbol()
        );

        prefix = color(plugin.replacePlaceholders(player, prefix));
        suffix = color(plugin.replacePlaceholders(player, suffix));

        // Scoreboard prefix/suffix limits are implementation dependent; keep reasonably short.
        if (prefix.length() > 64) prefix = prefix.substring(0, 64);
        if (suffix.length() > 64) suffix = suffix.substring(0, 64);

        team.setPrefix(prefix);
        team.setSuffix(suffix);

        // Optional header/footer
        if (plugin.getConfig().getBoolean("tab-header-footer-enabled", true)) {
            String header = plugin.getConfig().getString("tab-header", "&6&lBUSINESSCORE");
            String footer = plugin.getConfig().getString("tab-footer", "&eБаланс: &6%skript_balance%" + plugin.getCurrencySymbol());

            header = color(plugin.replacePlaceholders(player, header));
            footer = color(plugin.replacePlaceholders(player, footer));

            try {
                player.setPlayerListHeaderFooter(header, footer);
            } catch (Throwable ignored) {
            }
        }
    }

    public void shutdown() {
        // Clean up our teams to avoid leaving junk after /reload.
        for (String name : teamNames.values()) {
            Team t = board.getTeam(name);
            if (t != null) {
                try {
                    t.unregister();
                } catch (Throwable ignored) {
                }
            }
        }
        teamNames.clear();
    }

    private String makeTeamName(UUID uuid) {
        // 16 chars max. Example: bc_1a2b3c4d
        String hex = uuid.toString().replace("-", "");
        return ("bc_" + hex.substring(0, 8)).toLowerCase();
    }
}
