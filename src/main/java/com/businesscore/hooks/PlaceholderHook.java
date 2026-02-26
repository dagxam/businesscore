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
            case "money", "skript_money" ->
                    String.valueOf(plugin.getEconomyManager().getBalance(player));

            case "balance", "skript_balance" ->
                    plugin.getEconomyManager().getBalance(player) + sym;

            case "points", "rs_points" ->
                    String.valueOf(plugin.getDataManager().getPoints(uuid));

            case "gender", "rs_gender" -> {
                String g = plugin.getDataManager().getPlayerGender(uuid);
                if (g.equals("male")) yield "§b§l♂";
                else if (g.equals("female")) yield "§d§l♀";
                else yield "§7?";
            }

            case "rank" -> plugin.getDataManager().getRank(uuid);

            default -> null;
        };
    }
}
