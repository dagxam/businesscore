package com.businesscore.listeners;

import com.businesscore.BusinessCore;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import static com.businesscore.BusinessCore.color;

/**
 * Simple chat formatting with internal placeholders.
 * Works without PlaceholderAPI.
 */
public class ChatListener implements Listener {

    private final BusinessCore plugin;

    public ChatListener(BusinessCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event) {
        if (!plugin.getConfig().getBoolean("chat-format-enabled", true)) return;

        Player player = event.getPlayer();
        String fmt = plugin.getConfig().getString("chat-format", "%businesscore_gender% %businesscore_rank% &7%player%&f: %message%");
        if (fmt == null || fmt.isBlank()) return;

        String msg = event.getMessage();

        String out = fmt.replace("%message%", msg);
        out = plugin.replacePlaceholders(player, out);
        out = color(out);

        // AsyncPlayerChatEvent uses String.format-style placeholders by default;
        // we set a full final string and disable further formatting.
        event.setFormat(out.replace("%", "%%"));
    }
}
