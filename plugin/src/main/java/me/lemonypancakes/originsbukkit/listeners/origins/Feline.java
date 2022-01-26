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
import com.comphenix.protocol.events.PacketEvent;
import me.lemonypancakes.originsbukkit.api.events.player.AsyncPlayerOriginChangeEvent;
import me.lemonypancakes.originsbukkit.api.util.Origin;
import me.lemonypancakes.originsbukkit.api.wrappers.OriginPlayer;
import me.lemonypancakes.originsbukkit.enums.Config;
import me.lemonypancakes.originsbukkit.enums.Impact;
import me.lemonypancakes.originsbukkit.enums.Lang;
import me.lemonypancakes.originsbukkit.enums.Origins;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Arrays;

/**
 * The type Feline.
 *
 * @author LemonyPancakes
 */
public class Feline extends Origin implements Listener {

    private final OriginListenerHandler originListenerHandler;
    private final List<Material> materialList = new ArrayList<>();

    /**
     * Gets origin listener handler.
     *
     * @return the origin listener handler
     */
    public OriginListenerHandler getOriginListenerHandler() {
        return originListenerHandler;
    }

    /**
     * Gets material list.
     *
     * @return the material list
     */
    public List<Material> getMaterialList() {
        return materialList;
    }

    /**
     * Instantiates a new Feline.
     *
     * @param originListenerHandler the origin listener handler
     */
    public Feline(OriginListenerHandler originListenerHandler) {
        super(Config.ORIGINS_FELINE_MAX_HEALTH.toDouble(),
                Config.ORIGINS_FELINE_WALK_SPEED.toFloat(),
                Config.ORIGINS_FELINE_FLY_SPEED.toFloat());
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
        return "Feline";
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
        return "LemonyPancakes";
    }

    /**
     * Gets origin icon.
     *
     * @return the origin icon
     */
    @Override
    public Material getOriginIcon() {
        return Material.ORANGE_WOOL;
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
        return Lang.FELINE_TITLE.toString();
    }

    /**
     * Get origin description string [ ].
     *
     * @return the string [ ]
     */
    @Override
    public String[] getOriginDescription() {
        return Lang.FELINE_DESCRIPTION.toStringList();
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
        registerFelineMovePacketListener();
        String[] materialList = Config.ORIGINS_FELINE_WEAK_ARMS_BLOCK_LIST.toStringList();
        for (String material : materialList) {
            getMaterialList().add(Material.valueOf(material));
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

        if (Objects.equals(oldOrigin, Origins.FELINE.toString())) {
            new BukkitRunnable() {

                @Override
                public void run() {
                    player.removePotionEffect(PotionEffectType.JUMP);
                    player.removePotionEffect(PotionEffectType.NIGHT_VISION);
                }
            }.runTask(getOriginListenerHandler()
                    .getListenerHandler()
                    .getPlugin());
        }
    }

    /**
     * Feline fall damage immunity.
     *
     * @param event the event
     */
    @EventHandler
    private void felineFallDamageImmunity(EntityDamageEvent event) {
        Entity entity = event.getEntity();

        if (entity instanceof Player) {
            Player player = (Player) entity;
            OriginPlayer originPlayer = new OriginPlayer(player);
            String playerOrigin = originPlayer.getOrigin();
            EntityDamageEvent.DamageCause damageCause = event.getCause();

            if (Objects.equals(playerOrigin, Origins.FELINE.toString())) {
                if (damageCause == EntityDamageEvent.DamageCause.FALL) {
                    event.setCancelled(true);
                }
            }
        }
    }

    /**
     * Feline eating disabilities.
     *
     * @param event the event
     */
    @EventHandler
    private void felineEatingDisabilities(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        OriginPlayer originPlayer = new OriginPlayer(player);
        String playerOrigin = originPlayer.getOrigin();
        Material material = event.getItem().getType();
        List<Material> materials = Arrays.asList(
                Material.COOKED_COD,
                Material.COOKED_CHICKEN,
                Material.COOKED_SALMON,
                Material.COD,
                Material.SALMON,
                Material.TROPICAL_FISH,
                Material.PUFFERFISH,
                Material.POTION);

        if (Objects.equals(playerOrigin, Origins.FELINE.toString())) {
            if (!materials.contains(material)) {
                event.setCancelled(true);
            }
        }
    }

    /**
     * Feline block break.
     *
     * @param event the event
     */
    @EventHandler
    private void felineBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        OriginPlayer originPlayer = new OriginPlayer(player);
        String playerOrigin = originPlayer.getOrigin();
        Block block = event.getBlock();

        if (Objects.equals(playerOrigin, Origins.FELINE.toString())) {
            if (player.getGameMode() == GameMode.SURVIVAL) {
                if (getMaterialList().contains(block.getType())) {
                    if (!player.hasPotionEffect(PotionEffectType.INCREASE_DAMAGE)) {
                        if (blockGetAdjacent(block)) {
                            //event.setCancelled(true);
                        }
                    }
                }
            }
        }
    }

    /**
     * Block get adjacent boolean.
     *
     * @param block the block
     *
     * @return the boolean
     */
    private boolean blockGetAdjacent(Block block) {
        int start = 0;
        Block blockEast = block.getRelative(BlockFace.EAST);
        Block blockWest = block.getRelative(BlockFace.WEST);
        Block blockNorth = block.getRelative(BlockFace.NORTH);
        Block blockSouth = block.getRelative(BlockFace.SOUTH);
        Block blockUp = block.getRelative(BlockFace.UP);
        Block blockDown = block.getRelative(BlockFace.DOWN);

        if (getMaterialList().contains(blockEast.getType())) {
            start++;
        }
        if (getMaterialList().contains(blockWest.getType())) {
            start++;
        }
        if (getMaterialList().contains(blockNorth.getType())) {
            start++;
        }
        if (getMaterialList().contains(blockSouth.getType())) {
            start++;
        }
        if (getMaterialList().contains(blockUp.getType())) {
            start++;
        }
        if (getMaterialList().contains(blockDown.getType())) {
            start++;
        }
        return start > Config.ORIGINS_FELINE_WEAK_ARMS_MIN_ADJACENT_BLOCKS.toInt();
    }

    /**
     * Feline sprint jump.
     *
     * @param event the event
     */
    @EventHandler
    private void felineSprintJump(PlayerToggleSprintEvent event) {
        Player player = event.getPlayer();
        OriginPlayer originPlayer = new OriginPlayer(player);
        String playerOrigin = originPlayer.getOrigin();

        if (Objects.equals(playerOrigin, Origins.FELINE.toString())) {
            if (!player.isSprinting()) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 1, false, false));
            } else {
                player.removePotionEffect(PotionEffectType.JUMP);
            }
        }
    }

    /**
     * Feline creeper spawn event.
     *
     * @param event the event
     */
    @EventHandler
    private void felineCreeperSpawnEvent(CreatureSpawnEvent event) {
        Entity entity = event.getEntity();

        if (entity instanceof Creeper) {
            Location location = entity.getLocation();

            if (!(getOriginListenerHandler()
                    .getListenerHandler()
                    .getPlugin()
                    .getNMSHandler()
                    .getNMSMobHandler()
                    .getModifiedCreeper()
                    .isModifiedCreeper(entity))) {
                getOriginListenerHandler()
                        .getListenerHandler()
                        .getPlugin()
                        .getNMSHandler()
                        .getNMSMobHandler()
                        .getModifiedCreeper()
                        .summonModifiedCreeper(location);
                entity.remove();
            }
        }
    }

    /**
     * Feline creeper aggro event.
     *
     * @param event the event
     */
    @EventHandler
    private void felineCreeperAggroEvent(EntityTargetLivingEntityEvent event) {
        Entity entity = event.getEntity();

        if (entity instanceof Creeper) {
            Location location = entity.getLocation();

            if (!(getOriginListenerHandler()
                    .getListenerHandler()
                    .getPlugin()
                    .getNMSHandler()
                    .getNMSMobHandler()
                    .getModifiedCreeper()
                    .isModifiedCreeper(entity))) {
                getOriginListenerHandler()
                        .getListenerHandler()
                        .getPlugin()
                        .getNMSHandler()
                        .getNMSMobHandler()
                        .getModifiedCreeper()
                        .summonModifiedCreeper(location);
                entity.remove();
            }
        }
    }

    /**
     * Register feline move packet listener.
     */
    private void registerFelineMovePacketListener() {
        getOriginListenerHandler().getListenerHandler().getPlugin().getProtocolManager().addPacketListener(
                new PacketAdapter(getOriginListenerHandler().getListenerHandler().getPlugin(), ListenerPriority.NORMAL, PacketType.Play.Client.POSITION) {

                    @Override
                    public void onPacketReceiving(PacketEvent event) {
                        Player player = event.getPlayer();
                        OriginPlayer originPlayer = new OriginPlayer(player);
                        String playerOrigin = originPlayer.getOrigin();

                        if (Objects.equals(playerOrigin, Origins.FELINE.toString())) {
                            if (!player.isInWater()) {
                                new BukkitRunnable() {

                                    @Override
                                    public void run() {
                                        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0 , false, false));
                                    }
                                }.runTask(plugin);
                            } else {
                                new BukkitRunnable() {

                                    @Override
                                    public void run() {
                                        player.removePotionEffect(PotionEffectType.NIGHT_VISION);
                                    }
                                }.runTask(plugin);
                            }
                        }
                    }
                });
    }
}
