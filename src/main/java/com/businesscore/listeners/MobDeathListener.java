package com.businesscore.listeners;

import com.businesscore.BusinessCore;
import com.businesscore.managers.DataManager;
import com.businesscore.managers.RankManager;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.businesscore.BusinessCore.color;

public class MobDeathListener implements Listener {

    private final BusinessCore plugin;

    public MobDeathListener(BusinessCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player attacker = entity.getKiller();
        if (attacker == null) return;

        DataManager dm = plugin.getDataManager();
        RankManager rm = plugin.getRankManager();
        String uuid = attacker.getUniqueId().toString();
        EntityType type = entity.getType();

        // Player kill
        if (entity instanceof Player victim && !victim.equals(attacker)) {
            int pts = getConfigPoints("player", 10);
            dm.addPoints(uuid, pts);
            attacker.sendMessage(color("&a&l+" + pts + " очков &7за убийство игрока &c"
                    + victim.getName() + "&7! &eВсего: &6" + dm.getPoints(uuid)));
            victim.sendMessage(color("&c" + attacker.getName() + " &7убил тебя и получил &a+" + pts + " очков"));
            rm.checkRankUp(attacker);
            return;
        }

        // Mobs that need 2 kills for 1 point (config: mob-double-kills)
        if (isDoubleKillMob(type)) {
            String mobKey = type.name();
            dm.incrementMobKillCounter(uuid, mobKey);

            int kills = dm.getMobKillCounter(uuid, mobKey);
            if (kills >= 2) {
                dm.resetMobKillCounter(uuid, mobKey);
                dm.addPoints(uuid, 1);
                attacker.sendMessage(color("&a&l+1 очко &7за убийство 2 мобов (&f" + mobKey.toLowerCase() + "&7)! &eВсего: &6" + dm.getPoints(uuid)));
                rm.checkRankUp(attacker);
            }
            return;
        }

        // All other mobs with configured points
        String mobKey = type.name().toLowerCase();
        int pts = getConfigPoints(mobKey, -1);

        if (pts > 0) {
            dm.addPoints(uuid, pts);
            String mobName = getMobDisplayName(type);
            attacker.sendMessage(color("&a&l+" + pts + " очк" + pointsSuffix(pts)
                    + " &7за убийство " + mobName + "! &eВсего: &6" + dm.getPoints(uuid)));
            rm.checkRankUp(attacker);
        }
    }

    private boolean isDoubleKillMob(EntityType type) {
        // Backward compatibility: if list missing, keep old default behavior
        List<String> cfgList = plugin.getConfig().getStringList("mob-double-kills");
        Set<EntityType> set = new HashSet<>();

        if (cfgList != null && !cfgList.isEmpty()) {
            for (String s : cfgList) {
                if (s == null || s.isBlank()) continue;
                try {
                    set.add(EntityType.valueOf(s.trim().toUpperCase()));
                } catch (IllegalArgumentException ignored) {
                    // ignore unknown types
                }
            }
        } else {
            // defaults (old hardcoded): zombie variants + spiders
            set.add(EntityType.ZOMBIE);
            set.add(EntityType.HUSK);
            set.add(EntityType.DROWNED);
            set.add(EntityType.ZOMBIE_VILLAGER);
            set.add(EntityType.SPIDER);
            set.add(EntityType.CAVE_SPIDER);
        }

        return set.contains(type);
    }

    private int getConfigPoints(String key, int def) {
        return plugin.getConfig().getInt("mob-points." + key, def);
    }

    private String pointsSuffix(int n) {
        if (n == 1) return "о";
        if (n >= 2 && n <= 4) return "а";
        return "ов";
    }

    private String getMobDisplayName(EntityType type) {
        return switch (type) {
            case WITHER -> "Визера";
            case ENDER_DRAGON -> "Дракона";
            case WARDEN -> "Вардена";
            case ELDER_GUARDIAN -> "Древнего стража";
            case SKELETON -> "скелета";
            case STRAY -> "зимнего скелета";
            case WITHER_SKELETON -> "визер-скелета";
            case CREEPER -> "крипера";
            case ENDERMAN -> "эндермена";
            case WITCH -> "ведьмы";
            case SLIME -> "слизня";
            case MAGMA_CUBE -> "магма-куба";
            case PHANTOM -> "фантома";
            case BLAZE -> "ифрита";
            case GHAST -> "гаста";
            case GUARDIAN -> "стража";
            case SILVERFISH -> "чешуйницы";
            case ENDERMITE -> "эндермита";
            case SHULKER -> "шалкера";
            case HOGLIN -> "хоглина";
            case ZOGLIN -> "зоглина";
            case PIGLIN -> "пиглина";
            case PIGLIN_BRUTE -> "пиглина-громилы";
            case ZOMBIFIED_PIGLIN -> "зомби-пиглина";
            case PILLAGER -> "разбойника";
            case VINDICATOR -> "поборника";
            case EVOKER -> "заклинателя";
            case RAVAGER -> "разорителя";
            case VEX -> "вредины";
            case BREEZE -> "бриза";
            case BOGGED -> "болотника";
            default -> type.name().toLowerCase();
        };
    }
}
