package com.businesscore.listeners;

import com.businesscore.BusinessCore;
import com.businesscore.managers.DataManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import static com.businesscore.BusinessCore.color;

public class BlockListener implements Listener {

    private final BusinessCore plugin;

    public BlockListener(BusinessCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Material type = event.getBlock().getType();

        if (type == Material.DIAMOND_ORE || type == Material.DEEPSLATE_DIAMOND_ORE) {
            Player player = event.getPlayer();
            DataManager dm = plugin.getDataManager();
            String uuid = player.getUniqueId().toString();

            dm.addDiamondMined(uuid);

            int threshold = plugin.getConfig().getInt("diamond-mine-threshold", 3);
            if (dm.getDiamondMined(uuid) >= threshold) {
                dm.setDiamondMined(uuid, 0);
                dm.addPoints(uuid, 1);
                player.sendMessage(color("&b&l+1 очко &7за добычу " + threshold
                        + " алмазов! &eВсего: &6" + dm.getPoints(uuid)));
                plugin.getRankManager().checkRankUp(player);
            }
        }
    }
}
