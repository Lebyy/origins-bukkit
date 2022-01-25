/*
 * Origins-Bukkit - Origins for Bukkit and forks of Bukkit.
 * Copyright (C) 2021 LemonyPancakes
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package me.lemonypancakes.originsbukkit.listeners.origins;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import me.lemonypancakes.originsbukkit.api.events.player.AsyncPlayerOriginChangeEvent;
import me.lemonypancakes.originsbukkit.api.events.player.AsyncPlayerOriginInitiateEvent;
import me.lemonypancakes.originsbukkit.api.util.Origin;
import me.lemonypancakes.originsbukkit.util.ChatUtils;
import me.lemonypancakes.originsbukkit.api.wrappers.OriginPlayer;
import me.lemonypancakes.originsbukkit.api.wrappers.PlayerAirBubbles;
import me.lemonypancakes.originsbukkit.enums.Config;
import me.lemonypancakes.originsbukkit.enums.Impact;
import me.lemonypancakes.originsbukkit.enums.Lang;
import me.lemonypancakes.originsbukkit.enums.Origins;
import me.lemonypancakes.originsbukkit.storage.wrappers.MerlingTimerSessionDataWrapper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * The type Merling.
 *
 * @author LemonyPancakes
 */
public class Merling extends Origin implements Listener {

    private final OriginListenerHandler originListenerHandler;
    private final Map<UUID, Integer> merlingAirBreathing = new HashMap<>();
    public final Map<UUID, Integer> merlingAirTicks = new HashMap<>();
    private final Map<UUID, PlayerAirBubbles> merlingAirBubbles = new HashMap<>();
    private final List<UUID> merlingWaterBreathing = new ArrayList<>();
    private final List<UUID> merlingAirDamage = new ArrayList<>();

    /**
     * Instantiates a new Merling.
     *
     * @param originListenerHandler the origin listener handler
     */
    public Merling(OriginListenerHandler originListenerHandler) {
        super(Config.ORIGINS_MERLING_MAX_HEALTH.toDouble(),
                Config.ORIGINS_MERLING_WALK_SPEED.toFloat(),
                Config.ORIGINS_MERLING_FLY_SPEED.toFloat());
        this.originListenerHandler = originListenerHandler;
        init();
    }

    /**
     * Gets origin identifier.
     *
     * @return the origin identifier
     */
    @Override
    public String getOriginIdentifier() {
        return "Merling";
    }

    /**
     * Gets impact.
     *
     * @return the impact
     */
    @Override
    public Impact getImpact() {
        return Impact.HIGH;
    }

    /**
     * Gets author.
     *
     * @return the author
     */
    @Override
    public String getAuthor() {
        return "LemonyPancakes";
    }

    /**
     * Gets origin icon.
     *
     * @return the origin icon
     */
    @Override
    public Material getOriginIcon() {
        return Material.COD;
    }

    /**
     * Is origin icon glowing boolean.
     *
     * @return the boolean
     */
    @Override
    public boolean isOriginIconGlowing() {
        return false;
    }

    /**
     * Gets origin title.
     *
     * @return the origin title
     */
    @Override
    public String getOriginTitle() {
        return Lang.MERLING_TITLE.toString();
    }

    /**
     * Get origin description string [ ].
     *
     * @return the string [ ]
     */
    @Override
    public String[] getOriginDescription() {
        return Lang.MERLING_DESCRIPTION.toStringList();
    }

    /**
     * Init.
     */
    private void init() {
        originListenerHandler
                .getListenerHandler()
                .getPlugin()
                .getServer()
                .getPluginManager()
                .registerEvents(this, originListenerHandler
                        .getListenerHandler()
                        .getPlugin());
        registerOrigin(this);
        registerAirBreathingListener();
        registerWaterBreathingListener();
        registerMerlingAirDamageListener();
        registerMerlingBlockDiggingPacketListener();
        registerMerlingMovePacketListener();
    }

    /**
     * Merling join.
     *
     * @param event the event
     */
    @EventHandler
    private void merlingJoin(AsyncPlayerOriginInitiateEvent event) {
        Player player = event.getPlayer();
        OriginPlayer originPlayer = new OriginPlayer(player);
        UUID playerUUID = player.getUniqueId();
        String origin = event.getOrigin();
        double maxTime = Config.ORIGINS_MERLING_AIR_BREATHING_MAX_TIME.toDouble();

        if (Objects.equals(origin, Origins.MERLING.toString())) {
            if (originPlayer.findMerlingTimerSessionData() != null) {
                merlingAirBreathing.put(
                        playerUUID,
                        originPlayer.getMerlingTimerSessionDataTimeLeft());
                merlingAirTicks.put(
                        playerUUID,
                        switchAirTicks(
                                calculatePercentage(
                                        merlingAirBreathing
                                                .get(playerUUID), maxTime)));
            } else {
                merlingAirTicks.put(playerUUID, -27);
                merlingWaterBreathing.add(playerUUID);
            }
            PlayerAirBubbles playerAirBubbles = new PlayerAirBubbles(player);
            merlingAirBubbles.put(playerUUID, playerAirBubbles);
        }
    }

    /**
     * On origin change.
     *
     * @param event the event
     */
    @EventHandler
    private void onOriginChange(AsyncPlayerOriginChangeEvent event) {
        Player player = event.getPlayer();
        String oldOrigin = event.getOldOrigin();

        if (Objects.equals(oldOrigin, Origins.MERLING.toString())) {
            new BukkitRunnable() {

                @Override
                public void run() {
                    player.removePotionEffect(PotionEffectType.NIGHT_VISION);
                }
            }.runTask(originListenerHandler
                    .getListenerHandler()
                    .getPlugin());
        }
    }

    /**
     * Merling swimming grace.
     *
     * @param event the event
     */
    @EventHandler
    private void merlingGrace(PlayerToggleSprintEvent event) {
        
        Player player = event.getPlayer();
        OriginPlayer originPlayer = new OriginPlayer(player);
        String playerOrigin = originPlayer.getOrigin();

        if (Objects.equals(playerOrigin, Origins.MERLING.toString())) {
        
            Material m = event.getPlayer().getLocation().getBlock().getType();
        
            if (!player.isSprinting() && player.isInWater() && m == Material.WATER) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, Integer.MAX_VALUE, 1, true, true));
        
            } else {
                player.removePotionEffect(PotionEffectType.DOLPHINS_GRACE);
            }
        }
    }

    /**
     * Merling underwater breathing.
     *
     * @param event the event
     */
    @EventHandler
    private void merlingUnderwaterBreathing(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        EntityDamageEvent.DamageCause damageCause = event.getCause();

        if (entity instanceof Player) {
            Player player = ((Player) entity).getPlayer();

            if (player != null) {
                OriginPlayer originPlayer = new OriginPlayer(player);
                String playerOrigin = originPlayer.getOrigin();

                if (Objects.equals(playerOrigin, Origins.MERLING.toString())) {
                    if (damageCause == EntityDamageEvent.DamageCause.DROWNING) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    /**
     * Calculate percentage double.
     *
     * @param obtained the obtained
     * @param total    the total
     *
     * @return the double
     */
    private double calculatePercentage(double obtained, double total) {
        return obtained * 100 / total;
    }

    /**
     * Switch air ticks int.
     *
     * @param percentage the percentage
     *
     * @return the int
     */
    private int switchAirTicks(double percentage) {
        if (percentage <= 100 && percentage > 90) {
            return 273;
        } else if (percentage <= 90 && percentage > 80) {
            return 243;
        } else if (percentage <= 80 && percentage > 70) {
            return 213;
        } else if (percentage <= 70 && percentage > 60) {
            return 183;
        } else if (percentage <= 60 && percentage > 50) {
            return 153;
        } else if (percentage <= 50 && percentage > 40) {
            return 123;
        } else if (percentage <= 40 && percentage > 30) {
            return 93;
        } else if (percentage <= 30 && percentage > 20) {
            return 63;
        } else if (percentage <= 20 && percentage > 10) {
            return 33;
        } else if (percentage <= 10 && percentage > 0) {
            return 3;
        } else if (percentage == 0) {
            return -27;
        }
        return -27;
    }

    /**
     * Register old air breathing listener.
    */
    private void registerAirBreathingListener() {

        new BukkitRunnable() {

            @Override
            public void run() {
                if (!merlingAirBreathing.isEmpty()) {
                    for (Map.Entry<UUID, Integer> entry : merlingAirBreathing.entrySet()) {
                        UUID key = entry.getKey();
                        int value = entry.getValue();
                        Player player = Bukkit.getPlayer(key);

                        if (player != null) {

                            // create new origin player  
                            OriginPlayer originPlayer = new OriginPlayer(player);
                            String playerOrigin = originPlayer.getOrigin();
                            Location location = player.getLocation();
                            Block block = location.getBlock();
                            Material material = block.getType();
                            double maxTime = Config.ORIGINS_MERLING_AIR_BREATHING_MAX_TIME.toDouble();

                            // is player is a merling and is online
                            if (playerOrigin == Origins.MERLING.toString() && player.isOnline()) {

                                    // Put bubble percentage into the merlingAirTicks
                                    merlingAirTicks.put(key, switchAirTicks(calculatePercentage(value, maxTime)));

                                    // If no merlingTimerSessionData, create new data, else update to new value
                                    if (originPlayer.findMerlingTimerSessionData() == null) {
                                        originPlayer.createMerlingTimerSessionData(value);
                                    } else {
                                        originPlayer.updateMerlingTimerSessionData(
                                                new MerlingTimerSessionDataWrapper(key, value));
                                    }

                                    // refactor from here ===========================================================================
                                    if (value <= 0) {

                                        if (player.isInWater() || material == Material.WATER_CAULDRON) {
                                            value += 2;
                                            merlingAirBreathing.put(key, value);
                                        } else { // if player is not in water
                                            if (!player.getWorld().hasStorm() && (!(location.getBlockY() > player.getWorld().getHighestBlockAt(location).getLocation().getBlockY()))) {
                                                // If it is not storming or player is above max height
                                                merlingAirDamage.add(key);
                                                merlingAirBreathing.remove(key);
                                            } else {
                                                value += 2;
                                                merlingAirBreathing.put(key, value);
                                            } // if player is in the storm
                                        } 

                                    } // If merling has no breathing points
                                    
                                    else {
                                        if (!player.getWorld().hasStorm()) {
                                            if (player.isInWater() || material == Material.WATER_CAULDRON) {
                                                if (value < Config.ORIGINS_MERLING_AIR_BREATHING_MAX_TIME.toInt()) {
                                                    value += 2;
                                                    merlingAirBreathing.put(key, value);
                                                } else {
                                                    if (originPlayer.findMerlingTimerSessionData() != null) {
                                                        originPlayer.deleteMerlingTimerSessionData();
                                                    }
                                                    merlingWaterBreathing.add(key);
                                                    merlingAirBreathing.remove(key);
                                                    merlingAirTicks.put(key, -27);
                                                }
                                            }
                                        } else {
                                            if (player.isInWater() || material == Material.WATER_CAULDRON) {
                                                if (value < Config.ORIGINS_MERLING_AIR_BREATHING_MAX_TIME.toInt()) {
                                                    value += 2;
                                                    merlingAirBreathing.put(key, value);
                                                } else {
                                                    if (originPlayer.findMerlingTimerSessionData() != null) {
                                                        originPlayer.deleteMerlingTimerSessionData();
                                                    }
                                                    merlingWaterBreathing.add(key);
                                                    merlingAirBreathing.remove(key);
                                                    merlingAirTicks.put(key, -27);
                                                }
                                            } else {
                                                if (location.getBlockY() > player.getWorld().getHighestBlockAt(location).getLocation().getBlockY()) {
                                                    if (value < Config.ORIGINS_MERLING_AIR_BREATHING_MAX_TIME.toInt()) {
                                                        value += 2;
                                                        merlingAirBreathing.put(key, value);
                                                    } else {
                                                        if (originPlayer.findMerlingTimerSessionData() != null) {
                                                            originPlayer.deleteMerlingTimerSessionData();
                                                        }
                                                        merlingWaterBreathing.add(key);
                                                        merlingAirBreathing.remove(key);
                                                        merlingAirTicks.put(key, -27);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    if (value > 0 && merlingAirBreathing.containsKey(key)) {
                                        value--;
                                        merlingAirBreathing.put(key, value);
                                    }
                            } else {
                                if (originPlayer.findMerlingTimerSessionData() != null) {
                                    originPlayer.deleteMerlingTimerSessionData();
                                }
                                merlingAirBreathing.remove(key);
                                merlingAirBubbles.get(key).cancel();
                                merlingAirBubbles.remove(key);
                            }
                        } 
                    }
                }
            }
        }.runTaskTimerAsynchronously(originListenerHandler
                .getListenerHandler()
                .getPlugin(), 0L, 20L);
    }

    /**
     * Register water breathing listener.
     */
    private void registerWaterBreathingListener() {

        new BukkitRunnable() {

            @Override
            public void run() {
                if (!merlingWaterBreathing.isEmpty()) {
                    for (int i = 0; i < merlingWaterBreathing.size(); i++) {
                        Player player = Bukkit.getPlayer(merlingWaterBreathing.get(i));

                        if (player != null) {
                            UUID playerUUID = player.getUniqueId();
                            OriginPlayer originPlayer = new OriginPlayer(player);
                            String playerOrigin = originPlayer.getOrigin();
                            Location location = player.getLocation();
                            Block block = location.getBlock();
                            Material material = block.getType();
                            int timeLeft = originPlayer.getMerlingTimerSessionDataTimeLeft();

                            if (Objects.equals(playerOrigin, Origins.MERLING.toString())) {
                                if (player.isOnline()) {
                                    if (!player.getWorld().hasStorm()) {
                                        if (!(player.isInWater() || material == Material.WATER_CAULDRON)) {
                                            if (originPlayer.findMerlingTimerSessionData() != null) {
                                                if (timeLeft != 0) {
                                                    merlingAirBreathing.put(playerUUID, timeLeft);
                                                    merlingWaterBreathing.remove(playerUUID);
                                                } else {
                                                    merlingAirDamage.add(playerUUID);
                                                }
                                            } else {
                                                merlingAirBreathing.put(playerUUID, Config.ORIGINS_MERLING_AIR_BREATHING_MAX_TIME.toInt());
                                            }
                                            merlingWaterBreathing.remove(playerUUID);
                                        }
                                    } else {
                                        if (!(player.isInWater() || material == Material.WATER_CAULDRON)) {
                                            if (!(location.getBlockY() > player.getWorld().getHighestBlockAt(location).getLocation().getBlockY())) {
                                                if (originPlayer.findMerlingTimerSessionData() != null) {
                                                    if (timeLeft != 0) {
                                                        merlingAirBreathing.put(playerUUID, timeLeft);
                                                    } else {
                                                        merlingAirDamage.add(playerUUID);
                                                    }
                                                } else {
                                                    merlingAirBreathing.put(playerUUID, Config.ORIGINS_MERLING_AIR_BREATHING_MAX_TIME.toInt());
                                                }
                                                merlingWaterBreathing.remove(playerUUID);
                                            } else {
                                                if (originPlayer.findMerlingTimerSessionData() != null) {
                                                    merlingAirBreathing.put(playerUUID, timeLeft);
                                                    merlingWaterBreathing.remove(playerUUID);
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    merlingWaterBreathing.remove(playerUUID);
                                    merlingAirBubbles.get(playerUUID).cancel();
                                    merlingAirBubbles.remove(playerUUID);
                                }
                            } else {
                                merlingWaterBreathing.remove(playerUUID);
                                merlingAirBubbles.get(playerUUID).cancel();
                                merlingAirBubbles.remove(playerUUID);
                            }
                        }
                    }
                }
            }
        }.runTaskTimerAsynchronously(originListenerHandler
                .getListenerHandler()
                .getPlugin(), 0L, 5L);
    }

    /**
     * Register merling air damage listener.
     */
    private void registerMerlingAirDamageListener() {

        new BukkitRunnable() {

            @Override
            public void run() {
                if (!merlingAirDamage.isEmpty()) {
                    for (int i = 0; i < merlingAirDamage.size(); i++) {
                        Player player = Bukkit.getPlayer(merlingAirDamage.get(i));

                        if (player != null) {
                            UUID playerUUID = player.getUniqueId();
                            OriginPlayer originPlayer = new OriginPlayer(player);
                            String playerOrigin = originPlayer.getOrigin();
                            Location location = player.getLocation();
                            Block block = location.getBlock();
                            Material material = block.getType();
                            int timeLeft = originPlayer.getMerlingTimerSessionDataTimeLeft();

                            if (Objects.equals(playerOrigin, Origins.MERLING.toString())) {
                                if (player.isOnline()) {
                                    if (!player.getWorld().hasStorm()) {
                                        if (player.isInWater() || material == Material.WATER_CAULDRON) {
                                            if (originPlayer.findMerlingTimerSessionData() != null) {
                                                merlingAirBreathing.put(playerUUID, timeLeft);
                                            }
                                            merlingAirDamage.remove(playerUUID);
                                        } else {
                                            damageMerling(player, Config.ORIGINS_MERLING_AIR_BREATHING_DAMAGE_AMOUNT.toDouble());
                                        }
                                    } else {
                                        if (player.isInWater() || material == Material.WATER_CAULDRON) {
                                            if (originPlayer.findMerlingTimerSessionData() != null) {
                                                merlingAirBreathing.put(playerUUID, timeLeft);
                                            }
                                            merlingAirDamage.remove(playerUUID);
                                        } else {
                                            if (!(location.getBlockY() > player.getWorld().getHighestBlockAt(location).getLocation().getBlockY())) {
                                                damageMerling(player, Config.ORIGINS_MERLING_AIR_BREATHING_DAMAGE_AMOUNT.toDouble());
                                            } else {
                                                if (originPlayer.findMerlingTimerSessionData() != null) {
                                                    merlingAirBreathing.put(playerUUID, timeLeft);
                                                }
                                                merlingAirDamage.remove(playerUUID);
                                            }
                                        }
                                    }
                                } else {
                                    merlingAirDamage.remove(playerUUID);
                                    merlingAirBubbles.get(playerUUID).cancel();
                                    merlingAirBubbles.remove(playerUUID);
                                }
                            } else {
                                merlingAirDamage.remove(playerUUID);
                                merlingAirBubbles.get(playerUUID).cancel();
                                merlingAirBubbles.remove(playerUUID);
                            }
                        }
                    }
                }
            }
        }.runTaskTimerAsynchronously(originListenerHandler
                .getListenerHandler()
                .getPlugin(), Config.ORIGINS_MERLING_AIR_BREATHING_DAMAGE_DELAY.toLong(), Config.ORIGINS_MERLING_AIR_BREATHING_DAMAGE_PERIOD_DELAY.toLong());
    }

    /**
     * Damage merling.
     *
     * @param player the player
     * @param amount the amount
     */
    private void damageMerling(Player player, double amount) {

        new BukkitRunnable() {

            @Override
            public void run() {
                player.damage(amount);
            }
        }.runTask(originListenerHandler
                .getListenerHandler()
                .getPlugin());
    }

    /**
     * On merling impaling damage.
     *
     * @param event the event
     */
    @EventHandler
    private void onMerlingImpalingDamage(EntityDamageByEntityEvent event) {
        Entity target = event.getEntity();
        Entity damager = event.getDamager();
        double baseDamage = event.getDamage();

        if (target instanceof Player && damager instanceof LivingEntity) {
            Player targetPlayer = (Player) target;
            LivingEntity livingDamager = (LivingEntity) damager;
            OriginPlayer originPlayer = new OriginPlayer(targetPlayer);
            String targetPlayerOrigin = originPlayer.getOrigin();
            EntityEquipment entityEquipment = livingDamager.getEquipment();

            if (entityEquipment != null) {
                ItemStack itemStack = livingDamager.getEquipment().getItemInMainHand();
                ItemMeta itemMeta = itemStack.getItemMeta();

                if (Objects.equals(targetPlayerOrigin, Origins.MERLING.toString())) {
                    if (itemMeta != null && itemMeta.hasEnchant(Enchantment.IMPALING)) {
                        int enchantLevel = itemMeta.getEnchantLevel(Enchantment.IMPALING);

                        event.setDamage(baseDamage + (2.5 * enchantLevel));
                    }
                }
            }
        }
    }

    /**
     * Register merling block digging packet listener.
     */
    private void registerMerlingBlockDiggingPacketListener() {
        originListenerHandler.getListenerHandler().getPlugin().getProtocolManager().addPacketListener(
                new PacketAdapter(originListenerHandler.getListenerHandler().getPlugin(), ListenerPriority.NORMAL, PacketType.Play.Client.BLOCK_DIG) {

            @Override
            public void onPacketReceiving(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                EnumWrappers.PlayerDigType digType = packet.getPlayerDigTypes().getValues().get(0);
                Player player = event.getPlayer();
                OriginPlayer originPlayer = new OriginPlayer(player);
                String playerOrigin = originPlayer.getOrigin();
                Location playerLocation = player.getLocation();

                if (Objects.equals(playerOrigin, Origins.MERLING.toString())) {
                    if (playerLocation.getBlock().isLiquid() && playerLocation.add(0, 1, 0).getBlock().isLiquid()) {
                        if (digType == EnumWrappers.PlayerDigType.START_DESTROY_BLOCK) {
                            new BukkitRunnable() {

                                @Override
                                public void run() {
                                    player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, Integer.MAX_VALUE, 20, false, false));
                                }
                            }.runTask(getPlugin());
                        } else if (digType == EnumWrappers.PlayerDigType.ABORT_DESTROY_BLOCK) {
                            new BukkitRunnable() {

                                @Override
                                public void run() {
                                    player.removePotionEffect(PotionEffectType.FAST_DIGGING);
                                }
                            }.runTask(getPlugin());
                        } else if (digType == EnumWrappers.PlayerDigType.STOP_DESTROY_BLOCK) {
                            new BukkitRunnable() {

                                @Override
                                public void run() {
                                    player.removePotionEffect(PotionEffectType.FAST_DIGGING);
                                }
                            }.runTask(getPlugin());
                        }
                    }
                }
            }
        });
    }

    /**
     * Register merling move packet listener.
     */
    private void registerMerlingMovePacketListener() {
        originListenerHandler.getListenerHandler().getPlugin().getProtocolManager().addPacketListener(
                new PacketAdapter(originListenerHandler.getListenerHandler().getPlugin(), ListenerPriority.NORMAL, PacketType.Play.Client.POSITION) {

            @Override
            public void onPacketReceiving(PacketEvent event) {
                Player player = event.getPlayer();
                OriginPlayer originPlayer = new OriginPlayer(player);
                String playerOrigin = originPlayer.getOrigin();

                if (Objects.equals(playerOrigin, Origins.MERLING.toString())) {
                    if (player.isInWater()) {
                        new BukkitRunnable() {

                            @Override
                            public void run() {
                                player.addPotionEffect(
                                        new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0 , false, false));
                            }
                        }.runTask(getPlugin());
                    } else {
                        new BukkitRunnable() {

                            @Override
                            public void run() {
                                player.removePotionEffect(PotionEffectType.NIGHT_VISION);
                            }
                        }.runTask(getPlugin());
                    }
                }
            }
        });
    }
}
