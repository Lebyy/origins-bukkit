package me.lemonypancakes.originsbukkit.listeners.origins;

import me.lemonypancakes.originsbukkit.api.events.player.AsyncPlayerOriginAbilityUseEvent;
import me.lemonypancakes.originsbukkit.api.events.player.AsyncPlayerOriginInitiateEvent;
import me.lemonypancakes.originsbukkit.api.util.Origin;
import me.lemonypancakes.originsbukkit.enums.Config;
import me.lemonypancakes.originsbukkit.enums.Impact;
import me.lemonypancakes.originsbukkit.enums.Lang;
import me.lemonypancakes.originsbukkit.enums.Origins;
import me.lemonypancakes.originsbukkit.storage.wrappers.CreeperNearCatTimerDataWrapper;
import me.lemonypancakes.originsbukkit.util.ChatUtils;
import me.lemonypancakes.originsbukkit.api.wrappers.OriginPlayer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;


public class Creeper extends Origin implements Listener {
    private final OriginListenerHandler originListenerHandler;
    private final Map<UUID, Long> COOLDOWN = new HashMap<>();
    private final int COOLDOWNTIME = Config.ORIGINS_CREEPER_ABILITY_COOLDOWN.toInt();
    private final Map<UUID, Integer> nearCat = new HashMap<>();
    private final Map<UUID, Integer> nearCatEffects = new HashMap<>();
    private List<Entity> nearbyCats = null;

    /**
     * Gets nearCat.
     *
     * @return the nearCat
     */
    public Map<UUID, Integer> getNearCat() {
        return nearCat;
    }

    /**
     * Gets nearCat effects.
     *
     * @return the nearCat effects
     */
    public Map<UUID, Integer> getNearCatEffects() {
        return nearCatEffects;
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
     * Instantiates a new Origin.
     *  @param originListenerHandler the origin listener handler
     * @param originListenerHandler
     */
    public Creeper(OriginListenerHandler originListenerHandler) {
        super(Config.ORIGINS_CREEPER_MAX_HEALTH.toDouble(),
                Config.ORIGINS_CREEPER_WALK_SPEED.toFloat(),
                Config.ORIGINS_CREEPER_FLY_SPEED.toFloat());
        this.originListenerHandler = originListenerHandler;
        init();
    }

    /**
     * Gets origin listener handler.
     *
     * @return the origin listener handler
     */
    public OriginListenerHandler getOriginListenerHandler() {
        return originListenerHandler;
    }

    /**
     * Gets origin identifier.
     *
     * @return the origin identifier
     */
    @Override
    public String getOriginIdentifier() {
        return "Creeper";
    }

    /**
     * Gets impact.
     *
     * @return the impact
     */
    @Override
    public Impact getImpact() {
        return Impact.MEDIUM;
    }

    /**
     * Gets author.
     *
     * @return the author
     */
    @Override
    public String getAuthor() {
        return "Lebyy";
    }

    /**
     * Gets origin icon.
     *
     * @return the origin icon
     */
    @Override
    public Material getOriginIcon() {
        return Material.CREEPER_HEAD;
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
        return Lang.CREEPER_TITLE.toString();
    }

    /**
     * Get origin description string [ ].
     *
     * @return the string [ ]
     */
    @Override
    public String[] getOriginDescription() {
        return Lang.CREEPER_DESCRIPTION.toStringList();
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
        registerNearCatListener();
    }

    /**
     * Creeper join.
     *
     * @param event the event
     */
    @EventHandler
    private void creeperJoin(AsyncPlayerOriginInitiateEvent event) {
        Player player = event.getPlayer();
        OriginPlayer originPlayer = new OriginPlayer(player);
        UUID playerUUID = player.getUniqueId();
        String origin = event.getOrigin();

        if (Objects.equals(origin, Origins.CREEPER.toString())) {
            if (originPlayer.findCreeperNearCatTimerData() != null) {
                getNearCat().put(playerUUID,
                        originPlayer.findCreeperNearCatTimerData().getTimerTimeLeft());
                getNearCatEffects().put(playerUUID,
                        originPlayer.findCreeperNearCatTimerData().getNearCatTimeLeft());
            } else {
                getNearCat().put(playerUUID, 6);
                getNearCatEffects().put(playerUUID, 0);
            }
        }; 
    }

    /**
     * Register claustrophobia listener.
     */
    private void registerNearCatListener() {

        new BukkitRunnable() {

            @Override
            public void run() {
                getNearCat().forEach((key, value) -> {
                    Player player = Bukkit.getPlayer(key);

                    if (player != null) {
                        UUID playerUUID = player.getUniqueId();
                        OriginPlayer originPlayer = new OriginPlayer(player);
                        String playerOrigin = originPlayer.getOrigin();
                        int maxDuration = Config.ORIGINS_CREEPER_SCARED_MAX_DURATION.toInt();

                        if (Objects.equals(playerOrigin, Origins.CREEPER.toString())) {
                            if (player.isOnline()) {
                                getNearbyEntities(player);
                                for (Entity en : nearbyCats) {
                                    if(en.getType().equals(EntityType.CAT)) {
                                        if (value == 0) {
                                            getNearCatEffects().putIfAbsent(key, 0);

                                            if (getNearCatEffects().get(key) < maxDuration) {
                                                getNearCatEffects().put(key,
                                                        getNearCatEffects().get(key) + 20);
                                                syncAddPotionEffect(player, PotionEffectType.WEAKNESS,
                                                        getNearCatEffects().get(key));
                                                syncAddPotionEffect(player, PotionEffectType.SLOW,
                                                        getNearCatEffects().get(key));
                                            } else {
                                                syncAddPotionEffect(player, PotionEffectType.WEAKNESS, maxDuration);
                                                syncAddPotionEffect(player, PotionEffectType.SLOW, maxDuration);
                                            }
                                        } else {
                                            getNearCat().put(key, value - 1);

                                            if (getNearCatEffects().get(key) != null
                                                    && getNearCatEffects().get(key) != 0) {
                                                getNearCatEffects().put(key,
                                                        getNearCatEffects().get(key) - 20);
                                            }
                                        }
                                        if (originPlayer.findCreeperNearCatTimerData() == null) {
                                            originPlayer.createCreeperNearCatTimerData(value,
                                                    getNearCatEffects().get(key));
                                        } else {
                                            originPlayer.updateCreeperNearCatTimerData(
                                                    new CreeperNearCatTimerDataWrapper(playerUUID, value,
                                                            getNearCatEffects().get(key)));
                                        }
                                    } else if (en.getType().equals(EntityType.PLAYER)) {
                                        Player entityPlayer = (Player) en;
                                        OriginPlayer originPlayerEntity = new OriginPlayer(entityPlayer);
                                        String EntityPlayerOrigin = originPlayerEntity.getOrigin();
                                        if(Objects.equals(EntityPlayerOrigin, Origins.FELINE.toString())) {
                                            if (value == 0) {
                                                getNearCatEffects().putIfAbsent(key, 0);

                                                if (getNearCatEffects().get(key) < maxDuration) {
                                                    getNearCatEffects().put(key,
                                                            getNearCatEffects().get(key) + 20);
                                                    syncAddPotionEffect(player, PotionEffectType.WEAKNESS,
                                                            getNearCatEffects().get(key));
                                                    syncAddPotionEffect(player, PotionEffectType.SLOW,
                                                            getNearCatEffects().get(key));
                                                } else {
                                                    syncAddPotionEffect(player, PotionEffectType.WEAKNESS, maxDuration);
                                                    syncAddPotionEffect(player, PotionEffectType.SLOW, maxDuration);
                                                }
                                            } else {
                                                getNearCat().put(key, value - 1);

                                                if (getNearCatEffects().get(key) != null
                                                        && getNearCatEffects().get(key) != 0) {
                                                    getNearCatEffects().put(key,
                                                            getNearCatEffects().get(key) - 20);
                                                }
                                            }
                                            if (originPlayer.findCreeperNearCatTimerData() == null) {
                                                originPlayer.createCreeperNearCatTimerData(value,
                                                        getNearCatEffects().get(key));
                                            } else {
                                                originPlayer.updateCreeperNearCatTimerData(
                                                        new CreeperNearCatTimerDataWrapper(playerUUID, value,
                                                                getNearCatEffects().get(key)));
                                            }
                                        }
                                    } else {
                                        if (getNearCatEffects().get(key) != null
                                                && getNearCatEffects().get(key) != 0) {
                                            getNearCatEffects().put(key,
                                                    getNearCatEffects().get(key) - 20);
                                            if (originPlayer.findCreeperNearCatTimerData() == null) {
                                                originPlayer.createCreeperNearCatTimerData(value,
                                                        getNearCatEffects().get(key));
                                            } else {
                                                originPlayer.updateCreeperNearCatTimerData(
                                                        new CreeperNearCatTimerDataWrapper(playerUUID, value,
                                                                getNearCatEffects().get(key)));
                                            }
                                        }
                                        if (value != 6) {
                                            getNearCat().put(key, value + 1);
                                            if (originPlayer.findCreeperNearCatTimerData() == null) {
                                                originPlayer.createCreeperNearCatTimerData(value,
                                                        getNearCatEffects().get(key));
                                            } else {
                                                originPlayer.updateCreeperNearCatTimerData(
                                                        new CreeperNearCatTimerDataWrapper(playerUUID, value,
                                                                getNearCatEffects().get(key)));
                                            }
                                        }
                                    }
                                }
                            } else {
                                getNearCat().remove(key);
                                getNearCatEffects().remove(key);
                            }
                        } else {
                            if (originPlayer.findCreeperNearCatTimerData() != null) {
                                originPlayer.deleteCreeperNearCatTimerData();
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
        }.runTask(getOriginListenerHandler().getListenerHandler().getPlugin());
    }

    private void getNearbyEntities(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                nearbyCats = player.getNearbyEntities(10, 10, 10);
            }
        }.runTask(getOriginListenerHandler().getListenerHandler().getPlugin());
    }

    /**
     * Creeper shield disability.
     *
     * @param event the event
     */
    @EventHandler
    private void creeperShieldDisability(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        OriginPlayer originPlayer = new OriginPlayer(player);
        String playerOrigin = originPlayer.getOrigin();
        Action action = event.getAction();
        ItemStack itemStack = event.getItem();
        World world = player.getWorld();
        Location location = player.getLocation();
        EquipmentSlot equipmentSlot = event.getHand();
        PlayerInventory playerInventory = player.getInventory();

        if (Objects.equals(playerOrigin, Origins.CREEPER.toString())) {
            if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
                if (itemStack != null) {
                    Material material = itemStack.getType();

                    if (material == Material.SHIELD) {
                        if (equipmentSlot == EquipmentSlot.HAND) {
                            playerInventory.remove(itemStack);
                            world.dropItemNaturally(location, itemStack);
                        } else if (equipmentSlot == EquipmentSlot.OFF_HAND) {
                            playerInventory.setItemInOffHand(null);
                            world.dropItemNaturally(location, itemStack);
                        }
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    /**
     * Creeper ability use.
     *
     * @param event the event
     */
    @EventHandler
    private void creeperAbilityUse(AsyncPlayerOriginAbilityUseEvent event) {
        Player player = event.getPlayer();
        String origin = event.getOrigin();

        if (Objects.equals(origin, Origins.CREEPER.toString())) {
            creeperCreateExplosion(player);
        }
    }

    /**
     * Creeper create explosion.
     *
     * @param player the player
     */
    private void creeperCreateExplosion(Player player) {
        UUID playerUUID = player.getUniqueId();
        OriginPlayer originPlayer = new OriginPlayer(player);
        String playerOrigin = originPlayer.getOrigin();

        if (getCOOLDOWN().containsKey(playerUUID)) {
            long secondsLeft = ((getCOOLDOWN().get(playerUUID) / 1000) + getCOOLDOWNTIME()
                    - (System.currentTimeMillis() / 1000));

            if (secondsLeft > 0) {
                ChatUtils.sendPlayerMessage(player, Lang.PLAYER_ORIGIN_ABILITY_COOLDOWN.toString()
                        .replace("%seconds_left%", String.valueOf(secondsLeft)));
            } else {
                summonExplosion(player);
                getCOOLDOWN().put(playerUUID, System.currentTimeMillis());
                ChatUtils.sendPlayerMessage(player,
                        Lang.PLAYER_ORIGIN_ABILITY_USE.toString().replace("%player_current_origin%", playerOrigin));
            }
        } else {
            summonExplosion(player);
            getCOOLDOWN().put(playerUUID, System.currentTimeMillis());
            ChatUtils.sendPlayerMessage(player,
                    Lang.PLAYER_ORIGIN_ABILITY_USE.toString().replace("%player_current_origin%", playerOrigin));
        }
    }

    private void summonExplosion(Player player) {
        new BukkitRunnable() {

            @Override
            public void run() {
                player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 40, 255));
                World world = player.getWorld();
                Location location = player.getLocation();
                world.createExplosion(location, 4.0F, true);
            }
        }.runTask(getOriginListenerHandler().getListenerHandler().getPlugin());
    }
    
}
