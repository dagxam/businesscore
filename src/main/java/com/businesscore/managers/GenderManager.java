package com.businesscore.managers;

import com.businesscore.BusinessCore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

import static com.businesscore.BusinessCore.color;

public class GenderManager {

    private final BusinessCore plugin;

    public GenderManager(BusinessCore plugin) {
        this.plugin = plugin;
    }

    public String getPlayerGroup(Player p) {
        List<String> priority = plugin.getConfig().getStringList("group-priority");
        for (String group : priority) {
            if (p.hasPermission("group." + group)) {
                return group;
            }
        }
        return "default";
    }

    public String getPlayerGender(Player p) {
        return plugin.getDataManager().getPlayerGender(p.getUniqueId().toString());
    }

    public void setSkinByGroup(Player p) {
        if (!plugin.isSkinsRestorerAvailable()) {
            return;
        }
        String group = getPlayerGroup(p);
        String gender = getPlayerGender(p);
        if (gender.equals("none")) return;

        String skinName = plugin.getConfig().getString("skins." + group + "." + gender);
        if (skinName == null) {
            skinName = plugin.getConfig().getString("skins.default." + gender, "Steve");
        }

        String cmdTemplate = plugin.getConfig().getString("skin-set-cmd", "skin set %skin% %player%");
        String cmd = cmdTemplate
                .replace("%skin%", skinName)
                .replace("%player%", p.getName());

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
    }

    public void selectGender(Player p, String gender) {
        String uuid = p.getUniqueId().toString();

        if (!plugin.getDataManager().getPlayerGender(uuid).equals("none")) {
            p.sendMessage(color("&c&l✖ &cТы уже выбрал пол! Смена невозможна."));
            return;
        }

        var console = Bukkit.getConsoleSender();
        String name = p.getName();

        plugin.getDataManager().setGenderGroup(uuid, getPlayerGroup(p));
        plugin.getDataManager().setPlayerGender(uuid, gender.toLowerCase());

        // Ставим пермы как раньше для обратной совместимости других плагинов
        if (gender.equalsIgnoreCase("male")) {
            Bukkit.dispatchCommand(console, "lp user " + name + " permission set gender.male true");
            Bukkit.dispatchCommand(console, "lp user " + name + " permission set gender.selected true");
            Bukkit.dispatchCommand(console, "lp user " + name + " permission set skinsrestorer.command.set false");
            Bukkit.dispatchCommand(console, "lp user " + name + " permission set skinsrestorer.command.set.url false");
            Bukkit.dispatchCommand(console, "lp user " + name + " meta setprefix 1 \"&b♂ \"");
            p.sendMessage(color("&a&l✔ Ты выбрал мужской пол!"));

        } else if (gender.equalsIgnoreCase("female")) {
            Bukkit.dispatchCommand(console, "lp user " + name + " permission set gender.female true");
            Bukkit.dispatchCommand(console, "lp user " + name + " permission set gender.selected true");
            Bukkit.dispatchCommand(console, "lp user " + name + " permission set skinsrestorer.command.set false");
            Bukkit.dispatchCommand(console, "lp user " + name + " permission set skinsrestorer.command.set.url false");
            Bukkit.dispatchCommand(console, "lp user " + name + " meta setprefix 1 \"&d♀ \"");
            p.sendMessage(color("&d&l✔ Ты выбрала женский пол!"));

        } else {
            p.sendMessage(color("&c&l✖ &cИспользуй: /selectgender male или /selectgender female"));
            plugin.getDataManager().removePlayerGender(uuid); // revert
            return;
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> setSkinByGroup(p), 20L);
        Bukkit.getScheduler().runTaskLater(plugin, () -> plugin.getTabManager().updatePlayer(p), 1L);
    }

    public void resetGender(Player target) {
        var console = Bukkit.getConsoleSender();
        String name = target.getName();
        String uuid = target.getUniqueId().toString();

        Bukkit.dispatchCommand(console, "lp user " + name + " permission unset gender.selected");
        Bukkit.dispatchCommand(console, "lp user " + name + " permission unset gender.male");
        Bukkit.dispatchCommand(console, "lp user " + name + " permission unset gender.female");
        Bukkit.dispatchCommand(console, "lp user " + name + " permission unset skinsrestorer.command.set");
        Bukkit.dispatchCommand(console, "lp user " + name + " permission unset skinsrestorer.command.set.url");
        Bukkit.dispatchCommand(console, "lp user " + name + " meta removeprefix 1");
        Bukkit.dispatchCommand(console, "lp user " + name + " permission unset gender.menu.opened");

        plugin.getDataManager().removeGenderGroup(uuid);
        plugin.getDataManager().removePlayerGender(uuid);

        if (plugin.isSkinsRestorerAvailable()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "skin clear " + name);
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> plugin.getTabManager().updatePlayer(target), 10L);
    }
}
