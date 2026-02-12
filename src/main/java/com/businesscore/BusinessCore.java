package com.businesscore;

import com.businesscore.commands.*;
import com.businesscore.hooks.PlaceholderHook;
import com.businesscore.listeners.*;
import com.businesscore.managers.*;
import com.businesscore.tasks.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class BusinessCore extends JavaPlugin {

    private static BusinessCore instance;
    private DataManager dataManager;
    private EconomyManager economyManager;
    private RankManager rankManager;
    private GenderManager genderManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        // Managers
        dataManager = new DataManager(this);
        dataManager.load();
        economyManager = new EconomyManager(this);
        rankManager = new RankManager(this);
        genderManager = new GenderManager(this);

        // Register commands
        registerCommands();

        // Register listeners
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
        Bukkit.getPluginManager().registerEvents(new MobDeathListener(this), this);
        Bukkit.getPluginManager().registerEvents(new BlockListener(this), this);

        // Tasks
        long genderCheckTicks = getConfig().getInt("authme-gender-check-seconds", 2) * 20L;
        long skinUpdateTicks = getConfig().getInt("skin-update-interval-seconds", 30) * 20L;

        new TabFixTask(this).runTaskTimer(this, 100L, genderCheckTicks);
        new GenderCheckTask(this).runTaskTimer(this, 100L, genderCheckTicks);
        new SkinUpdateTask(this).runTaskTimer(this, skinUpdateTicks, skinUpdateTicks);

        // PlaceholderAPI
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PlaceholderHook(this).register();
            getLogger().info("PlaceholderAPI hook registered!");
        }

        // Auto-save every 5 min
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> dataManager.save(), 6000L, 6000L);

        getLogger().info("BusinessCore enabled!");
    }

    @Override
    public void onDisable() {
        if (dataManager != null) {
            dataManager.save();
        }
        getLogger().info("BusinessCore disabled!");
    }

    private void registerCommands() {
        EconomyCommands eco = new EconomyCommands(this);
        getCommand("setmoney").setExecutor(eco);
        getCommand("addmoney").setExecutor(eco);
        getCommand("takemoney").setExecutor(eco);
        getCommand("balance").setExecutor(eco);
        getCommand("pay").setExecutor(eco);
        getCommand("baltop").setExecutor(eco);

        ShopCommands shop = new ShopCommands(this);
        getCommand("shop_vip").setExecutor(shop);
        getCommand("shop_lum").setExecutor(shop);
        getCommand("shop_mine").setExecutor(shop);
        getCommand("shop_farm").setExecutor(shop);
        getCommand("sell").setExecutor(shop);
        getCommand("skript-buy").setExecutor(shop);
        getCommand("skript-sell").setExecutor(shop);
        getCommand("skript-sellall").setExecutor(shop);
        getCommand("skript-buyhealth").setExecutor(shop);
        getCommand("skript-buyexp").setExecutor(shop);
        getCommand("skript-buylevel").setExecutor(shop);
        getCommand("skript-buypoints").setExecutor(shop);
        getCommand("skript-buy-upgradebook").setExecutor(shop);
        getCommand("skript-sell-upgradebook").setExecutor(shop);

        RankCommands rank = new RankCommands(this);
        getCommand("mypoints").setExecutor(rank);
        getCommand("checkpoints").setExecutor(rank);
        getCommand("addpoints").setExecutor(rank);
        getCommand("setpoints").setExecutor(rank);
        getCommand("resetpoints").setExecutor(rank);
        getCommand("ranklist").setExecutor(rank);
        getCommand("migratepoints").setExecutor(rank);

        GenderCommands gender = new GenderCommands(this);
        getCommand("selectgender").setExecutor(gender);
        getCommand("gendermenu").setExecutor(gender);
        getCommand("mygender").setExecutor(gender);
        getCommand("resetmygender").setExecutor(gender);
        getCommand("resetgender").setExecutor(gender);
        getCommand("updateskin").setExecutor(gender);
        getCommand("genderskin").setExecutor(gender);
        getCommand("genderconfig").setExecutor(gender);
    }

    public static BusinessCore getInstance() { return instance; }
    public DataManager getDataManager() { return dataManager; }
    public EconomyManager getEconomyManager() { return economyManager; }
    public RankManager getRankManager() { return rankManager; }
    public GenderManager getGenderManager() { return genderManager; }

    public String getPrefix() {
        return color(getConfig().getString("prefix", "&6&l[Server]&r"));
    }

    public String getCurrencySymbol() {
        return getConfig().getString("currency-symbol", "₽");
    }

    public static String color(String msg) {
        if (msg == null) return "";
        return msg.replace("&", "§");
    }
}
