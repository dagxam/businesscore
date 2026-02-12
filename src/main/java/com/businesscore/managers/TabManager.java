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

public class TabManager {

    private final BusinessCore plugin;
    private final Map<UUID, String> playerTeamNames = new ConcurrentHashMap<>();

    public TabManager(BusinessCore plugin) {
        this.plugin = plugin;
    }

    public void updateAll() {
        if (!plugin.getConfig().getBoolean("tab.enabled", true)) return;
        for (Player p : Bukkit.getOnlinePlayers()) {
            updatePlayer(p);
        }
    }

    public void updatePlayer(Player player) {
        if (!plugin.getConfig().getBoolean("tab.enabled", true)) return;

        Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();

        String teamName = playerTeamNames.computeIfAbsent(player.getUniqueId(), u -> makeTeamName(player));
        Team team = sb.getTeam(teamName);
        if (team == null) team = sb.registerNewTeam(teamName);

        String prefix = plugin.getConfig().getString("tab.prefix", "%gender_prefix% %rank_prefix%");
        String suffix = plugin.getConfig().getString("tab.suffix", " &7| &6%skript_balance%%currency%");

        String rankPrefix = getRankPrefix(player);
        String genderPrefix = getGenderPrefix(player);

        prefix = prefix.replace("%rank_prefix%", rankPrefix).replace("%gender_prefix%", genderPrefix);
        suffix = suffix.replace("%rank_prefix%", rankPrefix).replace("%gender_prefix%", genderPrefix);

        prefix = plugin.replacePlaceholders(player, prefix);
        suffix = plugin.replacePlaceholders(player, suffix);

        // Limit (legacy) scoreboard prefix/suffix lengths (Spigot usually trims, but we do safe cut)
        team.setPrefix(color(cut(prefix, 64)));
        team.setSuffix(color(cut(suffix, 64)));

        if (!team.hasEntry(player.getName())) {
            team.addEntry(player.getName());
        }
    }

    private String makeTeamName(Player player) {
        // Team name max 16 in vanilla scoreboard
        String base = "bc" + Integer.toHexString(player.getUniqueId().hashCode());
        if (base.length() > 16) base = base.substring(0, 16);
        return base;
    }

    private String getGenderPrefix(Player p) {
        if (p.hasPermission("gender.male")) return "&b&l♂";
        if (p.hasPermission("gender.female")) return "&d&l♀";
        return "&7?";
    }

    private String getRankPrefix(Player p) {
        // We store rank id in DataManager, and RankManager maps to display
        String uuid = p.getUniqueId().toString();
        String rankId = plugin.getDataManager().getRank(uuid);
        String display = plugin.getConfig().getString("ranks." + rankId + ".display", rankId);
        return display + " ";
    }

    private static String cut(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max);
    }

    public void shutdown() {
        // Optional: do nothing (we keep teams on main scoreboard)
    }
}
