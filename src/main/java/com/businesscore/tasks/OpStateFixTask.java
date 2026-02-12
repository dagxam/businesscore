package com.businesscore.tasks;

import com.businesscore.BusinessCore;
import com.businesscore.managers.DataManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Tracks OP state changes and refreshes our own TAB rendering.
 * (Old TAB-plugin fix replaced with internal refresh.)
 */
public class OpStateFixTask extends BukkitRunnable {

    private final BusinessCore plugin;

    public OpStateFixTask(BusinessCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        DataManager dm = plugin.getDataManager();

        for (Player p : Bukkit.getOnlinePlayers()) {
            String uuid = p.getUniqueId().toString();

            int stored = dm.getOpState(uuid);
            int now = p.isOp() ? 1 : 0;

            if (stored != now) {
                dm.setOpState(uuid, now);
                Bukkit.getScheduler().runTask(plugin, () -> plugin.getTabManager().updateAll());
            }
        }
    }
}
