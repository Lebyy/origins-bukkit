package me.lemonypancakes.originsbukkit.listeners.origins;

import me.lemonypancakes.originsbukkit.api.events.player.AsyncPlayerOriginAbilityUseEvent;
import me.lemonypancakes.originsbukkit.api.util.Origin;
import me.lemonypancakes.originsbukkit.api.wrappers.OriginPlayer;
import me.lemonypancakes.originsbukkit.enums.Config;
import me.lemonypancakes.originsbukkit.enums.Impact;
import me.lemonypancakes.originsbukkit.enums.Lang;
import me.lemonypancakes.originsbukkit.enums.Origins;
import me.lemonypancakes.originsbukkit.util.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class Potions extends Origin implements Listener {

    private final OriginListenerHandler originListenerHandler;
    private final Map<UUID, Long> COOLDOWN = new HashMap<>();
    private final Map<UUID, Long> CINNAMONCOOLDOWN = new HashMap<>();
    private final int COOLDOWNTIME = Config.ORIGINS_POTIONS_ABILITY_COOLDOWN.toInt();
    private final int CINNAMONCOOLDOWNTIME = 60;
    public boolean cinnamonParticlesActive = false;
    /**
     * Instantiates a new Origin.
     *  @param originListenerHandler the origin listener handler
     * @param originListenerHandler
     */
    public Potions(OriginListenerHandler originListenerHandler) {
        super(20,
                0.2f,
                0.1f);
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
     * Gets cooldown.
     *
     * @return the cooldown
     */
    public Map<UUID, Long> getCINNAMONCOOLDOWN() {
        return CINNAMONCOOLDOWN;
    }

    /**
     * Gets cooldowntime.
     *
     * @return the cooldowntime
     */
    public int getCINNAMONCOOLDOWNTIME() {
        return CINNAMONCOOLDOWNTIME;
    }

    /**
     * Gets origin identifier.
     *
     * @return the origin identifier
     */
    @Override
    public String getOriginIdentifier() {
        return "Potions";
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
        return Material.SPLASH_POTION;
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
        return Lang.POTIONS_TITLE.toString();
    }

    /**
     * Get origin description string [ ].
     *
     * @return the string [ ]
     */
    @Override
    public String[] getOriginDescription() {
        return Lang.POTIONS_DESCRIPTION.toStringList();
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
        registerCinnamonBioElectricityModeEnterListener();
    }

    /**
     * Potionize player.
     *
     * @param player the player
     */
    private void potionizePlayer(Player player) {
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
                // Socksx's Powers
                if (player.getUniqueId().equals(UUID.fromString("50f297ef-7678-46a8-9674-2d09cb4a8fc5"))) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 6000, 2));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 6000, 2));
                }
                // Cinnamon's Power
                if(player.getUniqueId().equals(UUID.fromString("3936d1a8-a052-45c3-910e-a81ad198a0d4"))) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 6000, 3));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 6000, 5));
                }
                // Neyonn's Power
                if(player.getUniqueId().equals(UUID.fromString("fd884e74-5b78-4a7c-a6cb-bfc3345cf01c"))) {
                    Location eye = player.getEyeLocation();
                    Location loc = eye.add(eye.getDirection().multiply(1.2));
                    WitherSkull witherskull = (WitherSkull) player.getWorld().spawnEntity(loc, EntityType.WITHER_SKULL);
                    witherskull.setVelocity(loc.getDirection().normalize().multiply(2));
                    witherskull.setShooter(player);
                }
                // Ali_Ramal's Power
                if(player.getUniqueId().equals(UUID.fromString("361e1064-9db2-4734-8673-7f424497a6cf"))) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 6000, 2));
                }
                getCOOLDOWN().put(playerUUID, System.currentTimeMillis());
                ChatUtils.sendPlayerMessage(player, Lang.PLAYER_ORIGIN_ABILITY_USE
                        .toString()
                        .replace("%player_current_origin%", playerOrigin));
            }
        } else {
            // Socksx's Powers
            if (player.getUniqueId().equals(UUID.fromString("50f297ef-7678-46a8-9674-2d09cb4a8fc5"))) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 6000, 2));
                player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 6000, 2));
            }
            // Cinnamon's Power
            if(player.getUniqueId().equals(UUID.fromString("3936d1a8-a052-45c3-910e-a81ad198a0d4"))) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 6000, 3));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 6000, 5));
            }
            // Neyonn's Power
            if(player.getUniqueId().equals(UUID.fromString("fd884e74-5b78-4a7c-a6cb-bfc3345cf01c"))) {
                Location eye = player.getEyeLocation();
                Location loc = eye.add(eye.getDirection().multiply(1.2));
                WitherSkull witherskull = (WitherSkull) player.getWorld().spawnEntity(loc, EntityType.WITHER_SKULL);
                witherskull.setVelocity(loc.getDirection().normalize().multiply(2));
                witherskull.setShooter(player);
            }
            // Ali_Ramal's Power
            if(player.getUniqueId().equals(UUID.fromString("361e1064-9db2-4734-8673-7f424497a6cf"))) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 6000, 2));
            }
            getCOOLDOWN().put(playerUUID, System.currentTimeMillis());
            ChatUtils.sendPlayerMessage(player, Lang.PLAYER_ORIGIN_ABILITY_USE
                    .toString()
                    .replace("%player_current_origin%", playerOrigin));
        }
    }


    /**
     * Potions ability use.
     *
     * @param event the event
     */
    @EventHandler
    private void potionsAbilityUse(AsyncPlayerOriginAbilityUseEvent event) {
        Player player = event.getPlayer();
        String origin = event.getOrigin();

        if (Objects.equals(origin, Origins.POTIONS.toString())) {
            potionizePlayer(player);
        }
    }

    /**
     * Cinnamon use golden carrot
     *
     * @param event The event
     */
    @EventHandler
    private void cinnamonUseGoldenCarrot(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Material itemInHand = player.getInventory().getItemInMainHand().getType();
        Action action = event.getAction();
        UUID playerUUID = player.getUniqueId();

        if ((action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) && itemInHand == Material.GOLDEN_CARROT && player.getUniqueId().equals(UUID.fromString("3936d1a8-a052-45c3-910e-a81ad198a0d4"))) {
            if (getCINNAMONCOOLDOWN().containsKey(player.getUniqueId())) {
                long secondsLeft = ((getCINNAMONCOOLDOWN().get(playerUUID) / 1000) + getCINNAMONCOOLDOWNTIME() - (System.currentTimeMillis() / 1000));
                if (secondsLeft > 0) {
                    ChatUtils.sendPlayerMessage(player, Lang.PLAYER_ORIGIN_ABILITY_COOLDOWN
                            .toString()
                            .replace("%seconds_left%", String.valueOf(secondsLeft)));
                } else {
                    getCINNAMONCOOLDOWN().put(playerUUID, System.currentTimeMillis());
                    cinnamonParticlesActive = true;
                    ChatUtils.sendPlayerMessage(player, Lang.PLAYER_ORIGIN_ABILITY_USE
                            .toString()
                            .replace("%player_current_origin%", "Potions"));
                }
            } else {
                getCINNAMONCOOLDOWN().put(playerUUID, System.currentTimeMillis());
                cinnamonParticlesActive = true;
                ChatUtils.sendPlayerMessage(player, Lang.PLAYER_ORIGIN_ABILITY_USE
                        .toString()
                        .replace("%player_current_origin%", "Potions"));
            }
        }
    }

    /**
     * Register cinnamon particle listener.
     */
    private void registerCinnamonBioElectricityModeEnterListener() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    UUID playerUUID = player.getUniqueId();
                    if (!cinnamonParticlesActive) continue;
                    if(getCINNAMONCOOLDOWN().containsKey(playerUUID)) {
                        long secondsLeft = ((getCINNAMONCOOLDOWN().get(playerUUID) / 1000) + getCINNAMONCOOLDOWNTIME() - (System.currentTimeMillis() / 1000));
                        if (secondsLeft < 0) continue;
                        player.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, player.getLocation(), 50);
                    }
                }
            }
        }.runTaskTimerAsynchronously(getOriginListenerHandler()
                .getListenerHandler()
                .getPlugin(), 20L, 20L);
    }

    /**
     * Cinnamon on hit entity event.
     *
     * @param event The event.
     */
    @EventHandler
    private void cinnamonAttackEntity(EntityDamageByEntityEvent event) {
        if(event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player player = (Player) event.getDamager();
            Player clicked = (Player) event.getEntity();
            UUID playerUUID = player.getUniqueId();
            if (getCINNAMONCOOLDOWN().containsKey(player.getUniqueId())) {
                long secondsLeft = ((getCINNAMONCOOLDOWN().get(playerUUID) / 1000) + getCINNAMONCOOLDOWNTIME() - (System.currentTimeMillis() / 1000));
                if (secondsLeft > 0) {
                    cinnamonParticlesActive = false;
                    getCINNAMONCOOLDOWN().remove(playerUUID);
                    clicked.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 200, 1));
                }
            }
        }
    }
}
