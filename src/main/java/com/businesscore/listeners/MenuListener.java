package com.businesscore.listeners;

import com.businesscore.BusinessCore;
import com.businesscore.menus.OpenMenuSession;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class MenuListener implements Listener {

    private final BusinessCore plugin;

    public MenuListener(BusinessCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        OpenMenuSession session = plugin.getMenuManager().getOpenSession(player.getUniqueId());
        if (session == null) return;

        if (event.getClickedInventory() == null) return;
        if (!event.getView().getTopInventory().equals(session.inventory())) return;

        event.setCancelled(true);

        int slot = event.getSlot();
        boolean right = event.getClick() == ClickType.RIGHT || event.getClick() == ClickType.SHIFT_RIGHT;

        plugin.getMenuManager().handleClick(player, session, slot, right);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        OpenMenuSession session = plugin.getMenuManager().getOpenSession(player.getUniqueId());
        if (session == null) return;

        if (event.getInventory().equals(session.inventory())) {
            plugin.getMenuManager().clearOpenSession(player.getUniqueId());
        }
    }
}
