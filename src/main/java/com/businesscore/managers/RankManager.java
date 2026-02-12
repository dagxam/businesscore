package com.businesscore.managers;

import com.businesscore.BusinessCore;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.*;

import static com.businesscore.BusinessCore.color;

public class RankManager {

    private final BusinessCore plugin;

    // Ordered from highest to lowest
    private final List<RankEntry> rankTable = new ArrayList<>();

    public record RankEntry(String id, int requiredPoints, String display) {}

    public RankManager(BusinessCore plugin) {
        this.plugin = plugin;
        loadRankTable();
    }

    private void loadRankTable() {
        // Build from config, sorted by points descending
        var cfg = plugin.getConfig().getConfigurationSection("ranks");
        if (cfg == null) return;

        List<RankEntry> entries = new ArrayList<>();
        for (String key : cfg.getKeys(false)) {
            int pts = cfg.getInt(key + ".points", 0);
            String display = cfg.getString(key + ".display", key);
            entries.add(new RankEntry(key, pts, display));
        }
        entries.sort(Comparator.comparingInt(RankEntry::requiredPoints).reversed());
        rankTable.addAll(entries);
    }

    public void checkRankUp(Player p) {
        DataManager dm = plugin.getDataManager();
        String uuid = p.getUniqueId().toString();

        String currentRank = dm.getRank(uuid);

        // Skip manually assigned ranks
        if (currentRank.equals("prime_minister") || currentRank.equals("president")) {
            return;
        }

        int pts = dm.getPoints(uuid);

        // Find the new rank
        String newRank = "default";
        String rankDisplay = "§7§lНовичок";

        for (RankEntry entry : rankTable) {
            if (pts >= entry.requiredPoints()) {
                newRank = entry.id();
                rankDisplay = entry.display();
                break;
            }
        }

        if (!currentRank.equals(newRank)) {
            dm.setRank(uuid, newRank);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                    "lp user " + p.getName() + " parent set " + newRank);

            p.sendMessage("");
            p.sendMessage(color("&6&l╔══════════════════════════════╗"));
            p.sendMessage(color("&6&l║     &e&l★ ПОВЫШЕНИЕ! ★          &6&l║"));
            p.sendMessage(color("&6&l║  &eТы теперь " + rankDisplay + "&e!  &6&l║"));
            p.sendMessage(color("&6&l╚══════════════════════════════╝"));
            p.sendMessage("");
            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);

            // Update skin after rank change
            Bukkit.getScheduler().runTaskLater(plugin, () ->
                    plugin.getGenderManager().setSkinByGroup(p), 20L);
        }
    }

    public String getNextRankInfo(Player p) {
        int pts = plugin.getDataManager().getPoints(p.getUniqueId().toString());

        // Find ranks sorted ascending
        List<RankEntry> ascending = new ArrayList<>(rankTable);
        ascending.sort(Comparator.comparingInt(RankEntry::requiredPoints));

        for (RankEntry entry : ascending) {
            if (pts < entry.requiredPoints()) {
                int need = entry.requiredPoints() - pts;
                return color(entry.display() + " §7(нужно ещё §6" + need + " §7очков)");
            }
        }
        return color("§dМаксимальный ранг достигнут!");
    }

    public int getPoints(Player p) {
        return plugin.getDataManager().getPoints(p.getUniqueId().toString());
    }

    public void addPoints(Player p, int amount) {
        plugin.getDataManager().addPoints(p.getUniqueId().toString(), amount);
    }
}
