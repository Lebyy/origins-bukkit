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

import me.lemonypancakes.originsbukkit.api.events.player.AsyncPlayerOriginAbilityUseEvent;
import me.lemonypancakes.originsbukkit.api.events.player.AsyncPlayerOriginInitiateEvent;
import me.lemonypancakes.originsbukkit.api.util.Origin;
import me.lemonypancakes.originsbukkit.api.wrappers.OriginPlayer;
import me.lemonypancakes.originsbukkit.enums.Config;
import me.lemonypancakes.originsbukkit.enums.Impact;
import me.lemonypancakes.originsbukkit.enums.Lang;
import me.lemonypancakes.originsbukkit.enums.Origins;
import me.lemonypancakes.originsbukkit.storage.wrappers.ElytrianClaustrophobiaTimerDataWrapper;
import me.lemonypancakes.originsbukkit.util.ChatUtils;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * The type Elytrian.
 *
 * @author LemonyPancakes
 */
public class Elytrian extends Origin implements Listener {

    private final OriginListenerHandler originListenerHandler;
    private final Map<UUID, Long> COOLDOWN = new HashMap<>();
    private final int COOLDOWNTIME = Config.ORIGINS_ELYTRIAN_ABILITY_COOLDOWN.toInt();
    private final Map<UUID, Integer> claustrophobia = new HashMap<>();
    private final Map<UUID, Integer> claustrophibiaEffects = new HashMap<>();
    public static HashMap < UUID, Long > cooldown = new HashMap < UUID, Long > ();
    private static ItemStack elytra;

    /**
     * Gets origin listener handler.
     *
     * @return the origin listener handler
     */
    public OriginListenerHandler getOriginListenerHandler() {
        return originListenerHandler;
    }

    /**
     * Gets cooldown.
     *
     * @return the cooldown
     */
    public Map<UUID, Long> getCOOLDOWN() {
        return COOLDOWN;
    }

    /**
     * Gets cooldowntime.
     *
     * @return the cooldowntime
     */
    public int getCOOLDOWNTIME() {
        return COOLDOWNTIME;
    }

    /**
     * Gets claustrophobia.
     *
     * @return the claustrophobia
     */
    public Map<UUID, Integer> getClaustrophobia() {
        return claustrophobia;
    }

    /**
     * Gets claustrophibia effects.
     *
     * @return the claustrophibia effects
     */
    public Map<UUID, Integer> getClaustrophibiaEffects() {
        return claustrophibiaEffects;
    }

    /**
     * Gets elytra.
     *
     * @return the elytra
     */
    public static ItemStack getElytra() {
        return elytra;
    }

    /**
     * Sets elytra.
     *
     * @param elytra the elytra
     */
    public static void setElytra(ItemStack elytra) {
        Elytrian.elytra = elytra;
    }

    /**
     * Instantiates a new Elytrian.
     *
     * @param originListenerHandler the origin listener handler
     */
    public Elytrian(OriginListenerHandler originListenerHandler) {
        super(Config.ORIGINS_ELYTRIAN_MAX_HEALTH.toDouble(),
                Config.ORIGINS_ELYTRIAN_WALK_SPEED.toFloat(),
                Config.ORIGINS_ELYTRIAN_FLY_SPEED.toFloat());
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
        return "Elytrian";
    }

    /**
     * Gets impact.
     *
     * @return the impact
     */
    @Override
    public Impact getImpact() {
        return Impact.LOW;
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
        return Material.ELYTRA;
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
        return Lang.ELYTRIAN_TITLE.toString();
    }

    /**
     * Get origin description string [ ].
     *
     * @return the string [ ]
     */
    @Override
    public String[] getOriginDescription() {
        return Lang.ELYTRIAN_DESCRIPTION.toStringList();
    }

    /**
     * Init.
     */
    private void init() {
        getOriginListenerHandler()
                .getListenerHandler()
                .getPlugin()
                .getServer()
                .getPluginManager()
                .registerEvents(this, getOriginListenerHandler()
                        .getListenerHandler()
                        .getPlugin());
        registerOrigin(this);
        registerClaustrophobiaListener();
        createElytra();
    }

    /**
     * Elytrian join.
     *
     * @param event the event
     */
    @EventHandler
    private void elytrianJoin(AsyncPlayerOriginInitiateEvent event) {
        Player player = event.getPlayer();
        OriginPlayer originPlayer = new OriginPlayer(player);
        UUID playerUUID = player.getUniqueId();
        String origin = event.getOrigin();

        if (Objects.equals(origin, Origins.ELYTRIAN.toString())) {
            elytrianElytra(player);

            if (originPlayer.findElytrianClaustrophobiaTimerData() != null) {
                getClaustrophobia().put(playerUUID, originPlayer
                                .findElytrianClaustrophobiaTimerData()
                                .getTimerTimeLeft());
                getClaustrophibiaEffects().put(playerUUID, originPlayer
                        .findElytrianClaustrophobiaTimerData()
                        .getClaustrophobiaTimeLeft());
            } else {
                getClaustrophobia().put(playerUUID, 6);
                getClaustrophibiaEffects().put(playerUUID, 0);
            }
        } else {
            ItemStack prevChestplate = player.getInventory().getChestplate();

            if (prevChestplate != null && prevChestplate.equals(getElytra())) {
                player.getInventory().setChestplate(new ItemStack(Material.AIR));
            }
        }
    }

    /**
     * Elytrian ability use.
     *
     * @param event the event
     */
    @EventHandler
    private void elytrianAbilityUse(AsyncPlayerOriginAbilityUseEvent event) {
        Player player = event.getPlayer();
        String origin = event.getOrigin();

        if (Objects.equals(origin, Origins.ELYTRIAN.toString())) {
            elytrianLaunchIntoAir(player);
        }
    }

    /**
     * Create elytra.
     */
    private void createElytra() {
        ItemStack itemStack = new ItemStack(Material.ELYTRA, 1);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setDisplayName(ChatUtils.format("&dElytra"));
            itemMeta.setUnbreakable(true);
            itemMeta.addEnchant(Enchantment.BINDING_CURSE, 1, false);
            itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            itemStack.setItemMeta(itemMeta);
        }

        setElytra(itemStack);
    }

    /**
     * Elytrian elytra.
     *
     * @param player the player
     */
    private void elytrianElytra(Player player) {
        OriginPlayer originPlayer = new OriginPlayer(player);
        String playerOrigin = originPlayer.getOrigin();
        Location location = player.getLocation();
        World world = player.getWorld();
        PlayerInventory playerInventory = player.getInventory();
        ItemStack prevChestplate = playerInventory.getChestplate();

        if (Objects.equals(playerOrigin, Origins.ELYTRIAN.toString())) {
            if (prevChestplate != null && !prevChestplate.equals(getElytra())) {
                if (playerInventory.firstEmpty() == -1) {
                    world.dropItem(location, prevChestplate);
                } else {
                    playerInventory.addItem(prevChestplate);
                }
            }
            playerInventory.setChestplate(getElytra());
        }
    }

    /**
     * Elytrian armor equip.
     *
     * @param event the event
     */
    @EventHandler
    private void elytrianArmorEquip(InventoryClickEvent event) {
        HumanEntity humanEntity = event.getWhoClicked();
        Player player = (Player) humanEntity;
        OriginPlayer originPlayer = new OriginPlayer(player);
        String playerOrigin = originPlayer.getOrigin();
        int clickedSlot = event.getRawSlot();
        ItemStack cursorItemStack = event.getCursor();
        ItemStack clickedItemStack = event.getCurrentItem();

        if (Objects.equals(playerOrigin, Origins.ELYTRIAN.toString())) {
            if (clickedSlot == 6) {
                if (clickedItemStack != null && clickedItemStack.isSimilar(getElytra())) {
                    if (cursorItemStack != null && cursorItemStack.getItemMeta() != null) {
                        ChatUtils.sendPlayerMessage(player, "&dpeek-a-boo!");
                    }
                }
            }
        }
    }

    /**
     * Elytrian launch into air.
     *
     * @param player the player
     */
    private void elytrianLaunchIntoAir(Player player) {
        UUID playerUUID = player.getUniqueId();
        OriginPlayer originPlayer = new OriginPlayer(player);
        String playerOrigin = originPlayer.getOrigin();

        if (getCOOLDOWN().containsKey(playerUUID)) {
            long secondsLeft = ((getCOOLDOWN().get(playerUUID) / 1000) + getCOOLDOWNTIME() - (System.currentTimeMillis() / 1000));

            if (secondsLeft > 0) {
                ChatUtils.sendPlayerMessage(player, Lang.PLAYER_ORIGIN_ABILITY_COOLDOWN
                        .toString()
                        .replace("%seconds_left%", String.valueOf(secondsLeft)));
            } else {
                player.setVelocity(new Vector(0, Config.ORIGINS_ELYTRIAN_ABILITY_Y_VELOCITY.toDouble(), 0));
                getCOOLDOWN().put(playerUUID, System.currentTimeMillis());
                ChatUtils.sendPlayerMessage(player, Lang.PLAYER_ORIGIN_ABILITY_USE
                        .toString()
                        .replace("%player_current_origin%", playerOrigin));
            }
        } else {
            player.setVelocity(new Vector(0, Config.ORIGINS_ELYTRIAN_ABILITY_Y_VELOCITY.toDouble(), 0));
            getCOOLDOWN().put(playerUUID, System.currentTimeMillis());
            ChatUtils.sendPlayerMessage(player, Lang.PLAYER_ORIGIN_ABILITY_USE
                    .toString()
                    .replace("%player_current_origin%", playerOrigin));
        }
    }

    /**
     * Elytrian aerial combatant.
     *
     * @param event the event
     */
    @EventHandler
    private void elytrianAerialCombatant(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        double baseDamage = event.getDamage();

        if (damager instanceof Player) {
            Player player = (Player) damager;
            OriginPlayer originPlayer = new OriginPlayer(player);
            String playerOrigin = originPlayer.getOrigin();
            double additionalDamage = baseDamage * 0.5;

            if (Objects.equals(playerOrigin, Origins.ELYTRIAN.toString())) {
                if (player.isGliding()) {
                    event.setDamage(baseDamage + additionalDamage);
                }
            }
        }
    }

    /**
     * Elytrian brittle bones.
     *
     * @param event the event
     */
    @EventHandler
    private void elytrianBrittleBones(EntityDamageEvent event) {
        Entity entity = event.getEntity();

        if (entity instanceof Player) {
            Player player = (Player) entity;
            OriginPlayer originPlayer = new OriginPlayer(player);
            String playerOrigin = originPlayer.getOrigin();

            if (Objects.equals(playerOrigin, Origins.ELYTRIAN.toString())) {
                EntityDamageEvent.DamageCause damageCause = event.getCause();

                if (damageCause == EntityDamageEvent.DamageCause.FALL || damageCause == EntityDamageEvent.DamageCause.FLY_INTO_WALL) {
                    double baseDamage = event.getDamage();
                    double halfBaseDamage = baseDamage * 0.5;

                    event.setDamage(baseDamage + halfBaseDamage);
                }
            }
        }
    }

    /**
     * Register claustrophobia listener.
     */
    private void registerClaustrophobiaListener() {

        new BukkitRunnable() {

            @Override
            public void run() {
                getClaustrophobia().forEach((key, value) -> {
                    Player player = Bukkit.getPlayer(key);

                    if (player != null) {
                        UUID playerUUID = player.getUniqueId();
                        OriginPlayer originPlayer = new OriginPlayer(player);
                        String playerOrigin = originPlayer.getOrigin();
                        Location location = player.getLocation();
                        Location locationA = new Location(
                                location.getWorld(),
                                location.getX(),
                                location.getY() + 1,
                                location.getZ());
                        Location locationB = new Location(
                                location.getWorld(),
                                location.getX(),
                                location.getY() + 2,
                                location.getZ());
                        Location locationC = new Location(
                                location.getWorld(),
                                location.getX(),
                                location.getY() + 3,
                                location.getZ());
                        int maxDuration = Config.ORIGINS_ELYTRIAN_CLAUSTROPHOBIA_MAX_DURATION.toInt();

                        if (Objects.equals(playerOrigin, Origins.ELYTRIAN.toString())) {
                            if (player.isOnline()) {
                                if (
                                        location
                                                .getBlock()
                                                .getType()
                                                .isSolid() ||
                                                locationA
                                                        .getBlock()
                                                        .getType()
                                                        .isSolid() ||
                                                locationB
                                                        .getBlock()
                                                        .getType()
                                                        .isSolid() ||
                                                locationC
                                                        .getBlock()
                                                        .getType()
                                                        .isSolid()) {
                                    if (value == 0) {
                                        getClaustrophibiaEffects().putIfAbsent(key, 0);

                                        if (getClaustrophibiaEffects().get(key) < maxDuration) {
                                            getClaustrophibiaEffects().put(key, getClaustrophibiaEffects().get(key) + 20);
                                            syncAddPotionEffect(player, PotionEffectType.WEAKNESS, getClaustrophibiaEffects().get(key));
                                            syncAddPotionEffect(player, PotionEffectType.SLOW, getClaustrophibiaEffects().get(key));
                                        } else {
                                            syncAddPotionEffect(player, PotionEffectType.WEAKNESS, maxDuration);
                                            syncAddPotionEffect(player, PotionEffectType.SLOW, maxDuration);
                                        }
                                    } else {
                                        getClaustrophobia().put(key, value - 1);

                                        if (getClaustrophibiaEffects().get(key) != null && getClaustrophibiaEffects().get(key) != 0) {
                                            getClaustrophibiaEffects().put(key, getClaustrophibiaEffects().get(key) - 20);
                                        }
                                    }
                                    if (originPlayer.findElytrianClaustrophobiaTimerData() == null) {
                                        originPlayer.createElytrianClaustrophobiaTimerData(value, getClaustrophibiaEffects().get(key));
                                    } else {
                                        originPlayer.updateElytrianClaustrophobiaTimerData(new ElytrianClaustrophobiaTimerDataWrapper(
                                                playerUUID,
                                                value,
                                                getClaustrophibiaEffects().get(key)));
                                    }
                                } else {
                                    if (getClaustrophibiaEffects().get(key) != null && getClaustrophibiaEffects().get(key) != 0) {
                                        getClaustrophibiaEffects().put(key, getClaustrophibiaEffects().get(key) - 20);
                                        if (originPlayer.findElytrianClaustrophobiaTimerData() == null) {
                                            originPlayer.createElytrianClaustrophobiaTimerData(value, getClaustrophibiaEffects().get(key));
                                        } else {
                                            originPlayer.updateElytrianClaustrophobiaTimerData(new ElytrianClaustrophobiaTimerDataWrapper(
                                                    playerUUID,
                                                    value,
                                                    getClaustrophibiaEffects().get(key)));
                                        }
                                    }
                                    if (value != 6) {
                                        getClaustrophobia().put(key, value + 1);
                                        if (originPlayer.findElytrianClaustrophobiaTimerData() == null) {
                                            originPlayer.createElytrianClaustrophobiaTimerData(value, getClaustrophibiaEffects().get(key));
                                        } else {
                                            originPlayer.updateElytrianClaustrophobiaTimerData(new ElytrianClaustrophobiaTimerDataWrapper(
                                                    playerUUID,
                                                    value,
                                                    getClaustrophibiaEffects().get(key)));
                                        }
                                    }
                                }
                            } else {
                                getClaustrophobia().remove(key);
                                getClaustrophibiaEffects().remove(key);
                            }
                        } else {
                            if (originPlayer.findElytrianClaustrophobiaTimerData() != null) {
                                originPlayer.deleteElytrianClaustrophobiaTimerData();
                            }
                        }
                    }
                });
            }
        }.runTaskTimerAsynchronously(getOriginListenerHandler()
                .getListenerHandler()
                .getPlugin(), 0L, 20L);
    }

    /**
     * Freezing player ability.
     *
     * @param event the event
     */
    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        if (event.getRightClicked().getType().equals(EntityType.PLAYER) && player.getInventory().getItemInMainHand().getType() == Material.NETHERITE_SWORD && (player.getUniqueId().equals(UUID.fromString("a3cdf537-b62b-48c6-a868-45e2ad81a546")) || player.getUniqueId().equals(UUID.fromString("870f85e2-4b40-4c65-818b-ed24d58c55ab"))) && event.getRightClicked() instanceof Player) {
            if ((cooldown.containsKey(player.getUniqueId())) && cooldown.get(player.getUniqueId()) > System.currentTimeMillis()) {
                event.setCancelled(true);
                long remainingTime = cooldown.get(player.getUniqueId()) - System.currentTimeMillis();
                player.sendMessage(ChatColor.RED + "You cannot use your power " + player.getName() + " You need to wait for another " + remainingTime / 1000 + " seconds");
            } else {
                Player clicked = (Player) event.getRightClicked();
                clicked.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 10000));
                clicked.sendMessage(ChatColor.BLUE + "You have been frozen for 3 seconds by" + player.getName());
                player.sendMessage(ChatColor.GREEN + "You have frozen by " + clicked.getName() + " for 3 seconds!");
                cooldown.put(player.getUniqueId(), System.currentTimeMillis() + (15 * 1000));
            }
        }
    }

    /**
     * Sync add potion effect.
     *
     * @param player           the player
     * @param potionEffectType the potion effect type
     * @param time             the time
     */
    private void syncAddPotionEffect(Player player, PotionEffectType potionEffectType, int time) {

        new BukkitRunnable() {

            @Override
            public void run() {
                player.addPotionEffect(new PotionEffect(potionEffectType, time, 0));
            }
        }.runTask(getOriginListenerHandler()
                .getListenerHandler()
                .getPlugin());
    }

    @EventHandler
    private void elytrianDie(PlayerDeathEvent event) {
        Entity entity = event.getEntity();

        if(entity instanceof Player) {
            Player player = (Player) entity;
            OriginPlayer originPlayer = new OriginPlayer(player);
            String playerOrigin = originPlayer.getOrigin();
            if(Objects.equals(playerOrigin, Origins.ELYTRIAN.toString())) {

                for (ItemStack i : event.getDrops()) if (i.getType() == Material.ELYTRA) i.setType(Material.AIR);
            } else return;
        }
    }

    @EventHandler
    private void elytrianRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        OriginPlayer originPlayer = new OriginPlayer(player);
        String playerOrigin = originPlayer.getOrigin();
        if(Objects.equals(playerOrigin, Origins.ELYTRIAN.toString())) {
            elytrianElytra(player);
        }
    }

    /**
     * Give no one else elytra.
     *
     * @param event The event.
     */
    @EventHandler
    private void elytraAddToInventoryEvent(EntityPickupItemEvent event) {
        Item item = event.getItem();
        ItemStack itemStack = item.getItemStack();

        if(itemStack.getType() == Material.ELYTRA) {
            item.remove();
            event.setCancelled(true);
        }
    }
}