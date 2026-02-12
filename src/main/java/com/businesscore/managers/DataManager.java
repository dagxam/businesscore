package com.businesscore.managers;

import com.businesscore.BusinessCore;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DataManager {

    private final BusinessCore plugin;
    private final File dataFile;
    private YamlConfiguration data;

    // In-memory caches (UUID string -> value)
    private final Map<String, Double> money = new ConcurrentHashMap<>();
    private final Map<String, String> moneyNames = new ConcurrentHashMap<>();
    private final Map<String, Integer> points = new ConcurrentHashMap<>();
    private final Map<String, String> ranks = new ConcurrentHashMap<>();
    private final Map<String, Boolean> firstJoin = new ConcurrentHashMap<>();

    // Kill counters
    private final Map<String, Integer> zombieKills = new ConcurrentHashMap<>();
    private final Map<String, Integer> spiderKills = new ConcurrentHashMap<>();
    private final Map<String, Integer> diamondMined = new ConcurrentHashMap<>();

    // Gender group tracking
    private final Map<String, String> genderGroup = new ConcurrentHashMap<>();

    // OP state for TAB fix
    private final Map<String, Integer> opState = new ConcurrentHashMap<>();

    public DataManager(BusinessCore plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "data.yml");
    }

    public void load() {
        if (!dataFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        data = YamlConfiguration.loadConfiguration(dataFile);

        // Load money
        if (data.isConfigurationSection("money")) {
            for (String uuid : data.getConfigurationSection("money").getKeys(false)) {
                money.put(uuid, data.getDouble("money." + uuid));
            }
        }
        if (data.isConfigurationSection("moneyNames")) {
            for (String uuid : data.getConfigurationSection("moneyNames").getKeys(false)) {
                moneyNames.put(uuid, data.getString("moneyNames." + uuid));
            }
        }

        // Load points
        if (data.isConfigurationSection("points")) {
            for (String uuid : data.getConfigurationSection("points").getKeys(false)) {
                points.put(uuid, data.getInt("points." + uuid));
            }
        }

        // Load ranks
        if (data.isConfigurationSection("ranks")) {
            for (String uuid : data.getConfigurationSection("ranks").getKeys(false)) {
                ranks.put(uuid, data.getString("ranks." + uuid));
            }
        }

        // Load firstJoin
        if (data.isConfigurationSection("firstJoin")) {
            for (String uuid : data.getConfigurationSection("firstJoin").getKeys(false)) {
                firstJoin.put(uuid, data.getBoolean("firstJoin." + uuid));
            }
        }

        // Load kill counters
        if (data.isConfigurationSection("zombieKills")) {
            for (String uuid : data.getConfigurationSection("zombieKills").getKeys(false)) {
                zombieKills.put(uuid, data.getInt("zombieKills." + uuid));
            }
        }
        if (data.isConfigurationSection("spiderKills")) {
            for (String uuid : data.getConfigurationSection("spiderKills").getKeys(false)) {
                spiderKills.put(uuid, data.getInt("spiderKills." + uuid));
            }
        }
        if (data.isConfigurationSection("diamondMined")) {
            for (String uuid : data.getConfigurationSection("diamondMined").getKeys(false)) {
                diamondMined.put(uuid, data.getInt("diamondMined." + uuid));
            }
        }

        plugin.getLogger().info("Data loaded: " + money.size() + " balances, "
                + points.size() + " point records, " + ranks.size() + " ranks.");
    }

    public synchronized void save() {
        data = new YamlConfiguration();

        for (var e : money.entrySet()) data.set("money." + e.getKey(), e.getValue());
        for (var e : moneyNames.entrySet()) data.set("moneyNames." + e.getKey(), e.getValue());
        for (var e : points.entrySet()) data.set("points." + e.getKey(), e.getValue());
        for (var e : ranks.entrySet()) data.set("ranks." + e.getKey(), e.getValue());
        for (var e : firstJoin.entrySet()) data.set("firstJoin." + e.getKey(), e.getValue());
        for (var e : zombieKills.entrySet()) data.set("zombieKills." + e.getKey(), e.getValue());
        for (var e : spiderKills.entrySet()) data.set("spiderKills." + e.getKey(), e.getValue());
        for (var e : diamondMined.entrySet()) data.set("diamondMined." + e.getKey(), e.getValue());

        try {
            data.save(dataFile);
        } catch (IOException ex) {
            plugin.getLogger().severe("Failed to save data.yml!");
            ex.printStackTrace();
        }
    }

    // ── Money ──
    public double getMoney(String uuid) { return money.getOrDefault(uuid, 0.0); }
    public void setMoney(String uuid, double amount) { money.put(uuid, amount); }
    public String getMoneyName(String uuid) { return moneyNames.getOrDefault(uuid, uuid); }
    public void setMoneyName(String uuid, String name) { moneyNames.put(uuid, name); }
    public Map<String, Double> getAllMoney() { return Collections.unmodifiableMap(money); }

    // ── Points ──
    public int getPoints(String uuid) { return points.getOrDefault(uuid, 0); }
    public void setPoints(String uuid, int amount) { points.put(uuid, amount); }
    public void addPoints(String uuid, int amount) { points.merge(uuid, amount, Integer::sum); }

    // ── Ranks ──
    public String getRank(String uuid) { return ranks.getOrDefault(uuid, "default"); }
    public void setRank(String uuid, String rank) { ranks.put(uuid, rank); }

    // ── First Join ──
    public boolean isFirstJoin(String uuid) { return !firstJoin.containsKey(uuid); }
    public void markJoined(String uuid) { firstJoin.put(uuid, true); }

    // ── Kill counters ──
    public int getZombieKills(String uuid) { return zombieKills.getOrDefault(uuid, 0); }
    public void setZombieKills(String uuid, int val) { zombieKills.put(uuid, val); }
    public void addZombieKill(String uuid) { zombieKills.merge(uuid, 1, Integer::sum); }

    public int getSpiderKills(String uuid) { return spiderKills.getOrDefault(uuid, 0); }
    public void setSpiderKills(String uuid, int val) { spiderKills.put(uuid, val); }
    public void addSpiderKill(String uuid) { spiderKills.merge(uuid, 1, Integer::sum); }

    public int getDiamondMined(String uuid) { return diamondMined.getOrDefault(uuid, 0); }
    public void setDiamondMined(String uuid, int val) { diamondMined.put(uuid, val); }
    public void addDiamondMined(String uuid) { diamondMined.merge(uuid, 1, Integer::sum); }

    // ── Gender group ──
    public String getGenderGroup(String playerName) { return genderGroup.get(playerName); }
    public void setGenderGroup(String playerName, String group) { genderGroup.put(playerName, group); }
    public void removeGenderGroup(String playerName) { genderGroup.remove(playerName); }

    // ── OP state ──
    public int getOpState(String uuid) { return opState.getOrDefault(uuid, 0); }
    public void setOpState(String uuid, int state) { opState.put(uuid, state); }
    public void removeOpState(String uuid) { opState.remove(uuid); }
}
