package com.businesscore.commands;

import com.businesscore.BusinessCore;
import com.businesscore.managers.EconomyManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.Map;

import static com.businesscore.BusinessCore.color;

public class EconomyCommands implements CommandExecutor {

    private final BusinessCore plugin;
    private final EconomyManager eco;
    private final String sym;

    public EconomyCommands(BusinessCore plugin) {
        this.plugin = plugin;
        this.eco = plugin.getEconomyManager();
        this.sym = plugin.getCurrencySymbol();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        switch (cmd.getName().toLowerCase()) {
            case "setmoney" -> cmdSetMoney(sender, args);
            case "addmoney" -> cmdAddMoney(sender, args);
            case "takemoney" -> cmdTakeMoney(sender, args);
            case "balance" -> cmdBalance(sender, args);
            case "pay" -> cmdPay(sender, args);
            case "baltop" -> cmdBalTop(sender);
        }
        return true;
    }

    private void cmdSetMoney(CommandSender sender, String[] args) {
        if (args.length < 2) { sender.sendMessage(color("&cИспользование: /setmoney <игрок> <сумма>")); return; }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        double amount;
        try { amount = Double.parseDouble(args[1]); } catch (NumberFormatException e) {
            sender.sendMessage(color("&cНеверная сумма!")); return;
        }
        if (amount < 0) { sender.sendMessage(color("&cСумма не может быть отрицательной!")); return; }

        eco.setBalance(target, amount);
        sender.sendMessage(color("&aВы установили &6" + amount + sym + " &aигроку &e" + args[0]));

        Player online = target.getPlayer();
        if (online != null) {
            online.sendMessage(color("&aВам установили баланс: &6" + amount + sym));
        }
    }

    private void cmdAddMoney(CommandSender sender, String[] args) {
        if (args.length < 2) { sender.sendMessage(color("&cИспользование: /addmoney <игрок> <сумма>")); return; }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        double amount;
        try { amount = Double.parseDouble(args[1]); } catch (NumberFormatException e) {
            sender.sendMessage(color("&cНеверная сумма!")); return;
        }
        if (amount <= 0) { sender.sendMessage(color("&cСумма должна быть больше 0!")); return; }

        eco.addBalance(target, amount);
        sender.sendMessage(color("&aВы дали &6" + amount + sym + " &aигроку &e" + args[0]));

        Player online = target.getPlayer();
        if (online != null) {
            online.sendMessage(color("&aВам начислено: &6+" + amount + sym));
        }
    }

    private void cmdTakeMoney(CommandSender sender, String[] args) {
        if (args.length < 2) { sender.sendMessage(color("&cИспользование: /takemoney <игрок> <сумма>")); return; }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        double amount;
        try { amount = Double.parseDouble(args[1]); } catch (NumberFormatException e) {
            sender.sendMessage(color("&cНеверная сумма!")); return;
        }
        if (amount <= 0) { sender.sendMessage(color("&cСумма должна быть больше 0!")); return; }

        if (!eco.takeBalance(target, amount)) {
            sender.sendMessage(color("&cУ игрока только &6" + eco.getBalance(target) + sym + "&c!"));
            return;
        }

        sender.sendMessage(color("&cВы забрали &6" + amount + sym + " &cу игрока &e" + args[0]));
        Player online = target.getPlayer();
        if (online != null) {
            online.sendMessage(color("&cС вас списано: &6-" + amount + sym));
        }
    }

    private void cmdBalance(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player) && args.length == 0) {
            sender.sendMessage("Usage: /balance <player>"); return;
        }

        if (args.length > 0) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
            sender.sendMessage(color("&6💰 Баланс &e" + args[0] + "&6: &e" + eco.getBalance(target) + sym));
        } else {
            Player player = (Player) sender;
            player.sendMessage(color("&e&l━━━━━━━━━━━━━━━━━━━━"));
            player.sendMessage(color("&6💰 Ваш баланс: &e" + eco.getBalance(player) + sym));
            player.sendMessage(color("&e&l━━━━━━━━━━━━━━━━━━━━"));
        }
    }

    private void cmdPay(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("Only players!"); return; }
        if (args.length < 2) { player.sendMessage(color("&cИспользование: /pay <игрок> <сумма>")); return; }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) { player.sendMessage(color("&cИгрок не найден!")); return; }
        if (target.equals(player)) { player.sendMessage(color("&cНельзя переводить самому себе!")); return; }

        double amount;
        try { amount = Double.parseDouble(args[1]); } catch (NumberFormatException e) {
            player.sendMessage(color("&cНеверная сумма!")); return;
        }
        if (amount <= 0) { player.sendMessage(color("&cСумма должна быть больше 0!")); return; }

        if (!eco.takeBalance(player, amount)) {
            player.sendMessage(color("&cНедостаточно денег! У вас: &6" + eco.getBalance(player) + sym));
            return;
        }

        eco.addBalance(target, amount);
        player.sendMessage(color("&aВы перевели &6" + amount + sym + " &aигроку &e" + target.getName()));
        target.sendMessage(color("&aИгрок &e" + player.getName() + " &aперевёл вам &6" + amount + sym));
    }

    private void cmdBalTop(CommandSender sender) {
        int limit = plugin.getConfig().getInt("baltop-limit", 10);
        var top = eco.getTopBalances(limit);

        sender.sendMessage(color("&6&l━━━ ТОП БОГАТЫХ ИГРОКОВ ━━━"));

        int i = 0;
        for (Map.Entry<String, Double> entry : top) {
            i++;
            String name = eco.getNameForUUID(entry.getKey());
            if (name == null) name = entry.getKey();
            sender.sendMessage(color("&e" + i + ". &f" + name + ": &6" + entry.getValue() + sym));
        }

        sender.sendMessage(color("&6&l━━━━━━━━━━━━━━━━━━━━━━━"));
    }
}
