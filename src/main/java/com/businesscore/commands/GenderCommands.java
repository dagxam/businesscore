package com.businesscore.commands;

import com.businesscore.BusinessCore;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static com.businesscore.BusinessCore.color;

public class GenderCommands implements CommandExecutor {

    private final BusinessCore plugin;

    public GenderCommands(BusinessCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        String name = cmd.getName().toLowerCase();

        switch (name) {

            case "gendermenu": {
                if (!(sender instanceof Player player)) return true;
                plugin.getMenuManager().openMenu(player, "gender_select");
                return true;
            }

            case "selectgender": {
                if (!(sender instanceof Player player)) return true;

                if (player.hasPermission("gender.selected")) {
                    player.sendMessage(color("&c✗ Пол уже выбран. Изменить нельзя."));
                    return true;
                }

                if (args.length < 1) {
                    player.sendMessage(color("&cИспользование: /selectgender <male/female>"));
                    return true;
                }

                String g = args[0].toLowerCase();
                if (!g.equals("male") && !g.equals("female")) {
                    player.sendMessage(color("&cИспользование: /selectgender <male/female>"));
                    return true;
                }

                // ставим пермы через LuckPerms командой
                // male: gender.male + gender.selected
                // female: gender.female + gender.selected
                if (g.equals("male")) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " permission set gender.male true");
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " permission set gender.selected true");
                } else {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " permission set gender.female true");
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " permission set gender.selected true");
                }

                // сохраняем текущую группу для skin-update логики (uuid)
                String uuid = player.getUniqueId().toString();
                String currentGroup = plugin.getGenderManager().getPlayerGroup(player);
                plugin.getDataManager().setGenderGroup(uuid, currentGroup);

                player.sendMessage(color("&a✓ Пол выбран: &e" + (g.equals("male") ? "Мужской" : "Женский")));

                // обновим TAB
                Bukkit.getScheduler().runTaskLater(plugin, () -> plugin.getTabManager().updatePlayer(player), 10L);

                // применим скин (если можно)
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (player.isOnline()) plugin.getGenderManager().setSkinByGroup(player);
                }, 40L);

                return true;
            }

            case "mygender": {
                if (!(sender instanceof Player player)) return true;

                if (player.hasPermission("gender.male")) {
                    player.sendMessage(color("&eВаш пол: &b&l♂ Мужской"));
                } else if (player.hasPermission("gender.female")) {
                    player.sendMessage(color("&eВаш пол: &d&l♀ Женский"));
                } else {
                    player.sendMessage(color("&eВаш пол: &7не выбран"));
                }
                return true;
            }

            case "resetmygender": {
                if (!(sender instanceof Player player)) return true;
                if (!player.hasPermission("gender.reset.self")) {
                    player.sendMessage(color("&cНет прав."));
                    return true;
                }
                resetGenderFor(player, player);
                return true;
            }

            case "resetgender": {
                if (!sender.hasPermission("gender.reset.other")) {
                    sender.sendMessage(color("&cНет прав."));
                    return true;
                }
                if (args.length < 1) {
                    sender.sendMessage(color("&cИспользование: /resetgender <player>"));
                    return true;
                }

                Player target = Bukkit.getPlayerExact(args[0]);
                if (target == null) {
                    sender.sendMessage(color("&cИгрок не найден (должен быть онлайн)."));
                    return true;
                }

                resetGenderFor(target, sender);
                return true;
            }

            case "updateskin": {
                if (!sender.hasPermission("gender.updateskin")) {
                    sender.sendMessage(color("&cНет прав."));
                    return true;
                }

                Player target;
                if (args.length >= 1) {
                    target = Bukkit.getPlayerExact(args[0]);
                    if (target == null) {
                        sender.sendMessage(color("&cИгрок не найден (должен быть онлайн)."));
                        return true;
                    }
                } else {
                    if (!(sender instanceof Player p)) {
                        sender.sendMessage(color("&cИспользование: /updateskin <player>"));
                        return true;
                    }
                    target = p;
                }

                plugin.getGenderManager().setSkinByGroup(target);
                sender.sendMessage(color("&a✓ Скин обновлён для " + target.getName()));
                return true;
            }

            case "genderskin": {
                if (!sender.hasPermission("gender.admin")) {
                    sender.sendMessage(color("&cНет прав."));
                    return true;
                }
                sender.sendMessage(color("&eЭта команда зависит от реализации GenderManager (skins config)."));
                sender.sendMessage(color("&7Если нужно — скажи, я добавлю хранение в config.yml."));
                return true;
            }

            case "genderconfig": {
                if (!sender.hasPermission("gender.admin")) {
                    sender.sendMessage(color("&cНет прав."));
                    return true;
                }
                sender.sendMessage(color("&eSkinsRestorer: " + (plugin.isSkinsRestorerAvailable() ? "&aесть" : "&cнет")));
                sender.sendMessage(color("&ePlaceholderAPI: " + (plugin.isPlaceholderApiAvailable() ? "&aесть" : "&cнет")));
                return true;
            }
        }

        return true;
    }

    private void resetGenderFor(Player target, CommandSender byWhom) {
        // снимаем пермы
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + target.getName() + " permission unset gender.male");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + target.getName() + " permission unset gender.female");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + target.getName() + " permission unset gender.selected");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + target.getName() + " permission unset gender.menu.opened");

        // очищаем сохранение группы (uuid)
        plugin.getDataManager().setGenderGroup(target.getUniqueId().toString(), "");

        // обновляем TAB
        Bukkit.getScheduler().runTaskLater(plugin, () -> plugin.getTabManager().updatePlayer(target), 10L);

        // если SkinsRestorer есть — попробуем очистить скин
        if (plugin.isSkinsRestorerAvailable()) {
            // команды могут отличаться в разных версиях SR, но обычно работает:
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "skin clear " + target.getName());
        }

        // сообщения
        if (byWhom instanceof Player p && p.getUniqueId().equals(target.getUniqueId())) {
            target.sendMessage(color("&a✓ Твой пол сброшен. Сделай шаг, чтобы выбрать заново."));
        } else {
            target.sendMessage(color("&e&l⚠ &eТвой пол был сброшен. Сделай шаг, чтобы выбрать заново."));
            byWhom.sendMessage(color("&a✓ Пол игрока " + target.getName() + " сброшен."));
        }
    }
}
