package me.swagpancakes.originsbukkit.listeners.origins;

import me.swagpancakes.originsbukkit.Main;
import me.swagpancakes.originsbukkit.enums.Origins;
import me.swagpancakes.originsbukkit.util.StorageUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;
import java.util.UUID;

/**
 * The type Blazeborn.
 */
public class Blazeborn implements Listener {

    private static Main plugin;

    /**
     * Instantiates a new Blazeborn.
     *
     * @param plugin the plugin
     */
    public Blazeborn(Main plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        Blazeborn.plugin = plugin;
    }

    /**
     * Blazeborn join.
     *
     * @param player the player
     */
    public static void blazebornJoin(Player player) {
        blazebornWaterDamage(player);
    }

    /**
     * Blazeborn water damage.
     *
     * @param player the player
     */
    public static void blazebornWaterDamage(Player player) {

        new BukkitRunnable() {

            @Override
            public void run() {
                UUID playerUUID = player.getUniqueId();
                Location location = player.getLocation();
                Block block = location.getBlock();
                Material material = block.getType();

                if (Objects.equals(StorageUtils.getPlayerOrigin(playerUUID), Origins.BLAZEBORN)) {
                    if (player.getWorld().hasStorm()) {
                        if (player.isInWater() || material == Material.WATER_CAULDRON) {
                            player.damage(1);
                        } else if (location.getBlockY() > player.getWorld().getHighestBlockAt(location).getLocation().getBlockY()) {
                            player.damage(1);
                        } else {
                            blazebornAirEnter(player);
                            this.cancel();
                        }
                    } else {
                        if (player.isInWater() || material == Material.WATER_CAULDRON) {
                            player.damage(1);
                        } else {
                            blazebornAirEnter(player);
                            this.cancel();
                        }
                    }
                } else {
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }

    /**
     * Blazeborn air enter.
     *
     * @param player the player
     */
    public static void blazebornAirEnter(Player player) {

        new BukkitRunnable() {

            @Override
            public void run() {
                UUID playerUUID = player.getUniqueId();
                Location location = player.getLocation();
                Block block = location.getBlock();
                Material material = block.getType();

                if (Objects.equals(StorageUtils.getPlayerOrigin(playerUUID), Origins.BLAZEBORN)) {
                    if (player.getWorld().hasStorm()) {
                        if (player.isInWater() || material == Material.WATER_CAULDRON) {
                            blazebornWaterDamage(player);
                            this.cancel();
                        } else if (location.getBlockY() > player.getWorld().getHighestBlockAt(location).getLocation().getBlockY()) {
                            blazebornWaterDamage(player);
                            this.cancel();
                        }
                    } else {
                        if (player.isInWater() || material == Material.WATER_CAULDRON) {
                            blazebornWaterDamage(player);
                            this.cancel();
                        }
                    }
                } else {
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }

    /**
     * Blazeborn damage immunities.
     *
     * @param event the event
     */
    @EventHandler
    public void blazebornDamageImmunities(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        EntityDamageEvent.DamageCause damageCause = event.getCause();

        if (entity instanceof Player) {
            Player player = ((Player) entity).getPlayer();
            assert player != null;
            UUID playerUUID = player.getUniqueId();

            if (Objects.equals(StorageUtils.getPlayerOrigin(playerUUID), Origins.BLAZEBORN)) {
                if (damageCause == EntityDamageEvent.DamageCause.LAVA || damageCause == EntityDamageEvent.DamageCause.FIRE || damageCause == EntityDamageEvent.DamageCause.FIRE_TICK || damageCause == EntityDamageEvent.DamageCause.HOT_FLOOR || damageCause == EntityDamageEvent.DamageCause.POISON || damageCause == EntityDamageEvent.DamageCause.STARVATION) {
                    event.setCancelled(true);
                }
            }
        }
    }
}