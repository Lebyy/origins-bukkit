package me.lemonypancakes.originsbukkit.listeners.origins;

import me.lemonypancakes.originsbukkit.api.events.player.AsyncPlayerOriginAbilityUseEvent;
import me.lemonypancakes.originsbukkit.api.events.player.AsyncPlayerOriginInitiateEvent;
import me.lemonypancakes.originsbukkit.api.util.Origin;
import me.lemonypancakes.originsbukkit.api.wrappers.OriginPlayer;
import me.lemonypancakes.originsbukkit.enums.Config;
import me.lemonypancakes.originsbukkit.enums.Impact;
import me.lemonypancakes.originsbukkit.enums.Lang;
import me.lemonypancakes.originsbukkit.enums.Origins;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class IronGolem extends Origin implements Listener {
    private final OriginListenerHandler originListenerHandler;
    private final Map<UUID, Long> COOLDOWN = new HashMap<>();
    private final int COOLDOWNTIME = Config.ORIGINS_IRONGOLEM_ABILITY_COOLDOWN.toInt();

    /**
     * Instantiates a new Origin.
     *  @param originListenerHandler the origin listener handler
     * @param originListenerHandler
     */
    public IronGolem(OriginListenerHandler originListenerHandler) {
        super(40,
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
     * Gets origin identifier.
     *
     * @return the origin identifier
     */
    @Override
    public String getOriginIdentifier() {
        return "IronGolem";
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
        return Material.IRON_INGOT;
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
        return Lang.IRONGOLEM_TITLE.toString();
    }

    /**
     * Get origin description string [ ].
     *
     * @return the string [ ]
     */
    @Override
    public String[] getOriginDescription() {
        return Lang.IRONGOLEM_DESCRIPTION.toStringList();
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
    }

    /**
     * Iron Golem ability use.
     *
     * @param event the event
     */
    @EventHandler
    private void ironGolemAbilityUse(AsyncPlayerOriginAbilityUseEvent event) {
        Player player = event.getPlayer();
        String origin = event.getOrigin();

        if (Objects.equals(origin, Origins.IRONGOLEM.toString())) {
            syncAddPotionEffect(player, PotionEffectType.INCREASE_DAMAGE, 6000, 1);
        }
    }

    /**
     * Iron Golem respawn.
     *
     * @param event the event
     */
    @EventHandler
    private void ironGolemRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        OriginPlayer originPlayer = new OriginPlayer(player);
        String playerOrigin = originPlayer.getOrigin();

        if (Objects.equals(playerOrigin, Origins.IRONGOLEM.toString())) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 0));
        }
    }

    @EventHandler
    private void ironGolemJoin(AsyncPlayerOriginInitiateEvent event) {
        Player player = event.getPlayer();
        String origin = event.getOrigin();
        if (Objects.equals(origin, Origins.IRONGOLEM.toString())) {
            syncAddPotionEffect(player, PotionEffectType.SLOW, Integer.MAX_VALUE, 0);
        }
    }

    /**
     * Iron Golem eat.
     *
     * @param event the event
     */
    @EventHandler
    private void ironGolemEatFood(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        OriginPlayer originPlayer = new OriginPlayer(player);
        String playerOrigin = originPlayer.getOrigin();
        ItemStack food = event.getItem();
        if (Objects.equals(playerOrigin, Origins.IRONGOLEM.toString())) {
            if (food.getType() != Material.MILK_BUCKET) {
                syncAddPotionEffect(player, PotionEffectType.SLOW, Integer.MAX_VALUE, 0);
            } else {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    private void ironGolemEatIron(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        OriginPlayer originPlayer = new OriginPlayer(player);
        String playerOrigin = originPlayer.getOrigin();
        Action action = event.getAction();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (Objects.equals(playerOrigin, Origins.IRONGOLEM.toString())) {
            if((action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) && itemInHand.getType() == Material.IRON_INGOT && player.getFoodLevel() < 20)  {
                player.setFoodLevel((player.getFoodLevel() + 1));
                if (itemInHand.getAmount() > 1) itemInHand.setAmount(itemInHand.getAmount() - 1);
                else itemInHand = new ItemStack(Material.AIR);
                player.getInventory().setItemInMainHand(itemInHand);
            }
        }
    }

    /**
     * IronGolem attack irongolem.
     *
     * @param event the event
     */
    @EventHandler
    private void ironGolemAttackIronGolem(EntityTargetLivingEntityEvent event) {
        Entity entity = event.getTarget();
        if(entity instanceof Player) {
            Player player = (Player) entity;
            OriginPlayer originPlayer = new OriginPlayer(player);
            String playerOrigin = originPlayer.getOrigin();
            if (Objects.equals(playerOrigin, Origins.IRONGOLEM.toString())) {
                if (!event.getEntity().getType().equals(EntityType.IRON_GOLEM)) return;
                event.setCancelled(true);
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
    private void syncAddPotionEffect(Player player, PotionEffectType potionEffectType, int time, int amplifier) {
        new BukkitRunnable() {

            @Override
            public void run() {
                player.addPotionEffect(new PotionEffect(potionEffectType, time, amplifier));
            }
        }.runTask(getOriginListenerHandler()
                .getListenerHandler()
                .getPlugin());
    }
}
