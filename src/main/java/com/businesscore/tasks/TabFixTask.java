package com.businesscore.tasks;

import com.businesscore.BusinessCore;
import com.businesscore.managers.DataManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class TabFixTask extends BukkitRunnable {

    private final BusinessCore plugin;

    public TabFixTask(BusinessCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        DataManager dm = plugin.getDataManager();

        for (Player player : Bukkit.getOnlinePlayers()) {
            String uuid = player.getUniqueId().toString();
            int now = player.isOp() ? 1 : 0;
            int stored = dm.getOpState(uuid);

            if (stored != now) {
                dm.setOpState(uuid, now);
                Bukkit.getScheduler().runTask(plugin, () ->
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tab reload"));
                return; // Only one reload per cycle
            }
        }
    }
}
