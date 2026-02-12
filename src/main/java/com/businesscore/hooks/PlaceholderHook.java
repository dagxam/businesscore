package com.businesscore.hooks;

import com.businesscore.BusinessCore;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaceholderHook extends PlaceholderExpansion {

    private final BusinessCore plugin;

    public PlaceholderHook(BusinessCore plugin) {
        this.plugin = plugin;
    }

    @Override public @NotNull String getIdentifier() { return "businesscore"; }
    @Override public @NotNull String getAuthor() { return "BusinessCore"; }
    @Override public @NotNull String getVersion() { return plugin.getDescription().getVersion(); }
    @Override public boolean persist() { return true; }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) return "";

        String uuid = player.getUniqueId().toString();
        String sym = plugin.getCurrencySymbol();

        return switch (params.toLowerCase()) {
            // Legacy compatibility: %businesscore_money%
            case "money", "skript_money" ->
                    String.valueOf(plugin.getEconomyManager().getBalance(player));

            // %businesscore_balance% (with symbol)
            case "balance", "skript_balance" ->
                    plugin.getEconomyManager().getBalance(player) + sym;

            // %businesscore_points%, %businesscore_rs_points%
            case "points", "rs_points" ->
                    String.valueOf(plugin.getDataManager().getPoints(uuid));

            // %businesscore_gender%, %businesscore_rs_gender%
            case "gender", "rs_gender" -> {
                if (player.hasPermission("gender.male")) yield "§b§l♂";
                else if (player.hasPermission("gender.female")) yield "§d§l♀";
                else yield "§7?";
            }

            // %businesscore_rank%
            case "rank" -> plugin.getDataManager().getRank(uuid);

            default -> null;
        };
    }
}
