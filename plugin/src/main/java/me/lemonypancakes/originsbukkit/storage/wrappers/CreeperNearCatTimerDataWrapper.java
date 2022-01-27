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
package me.lemonypancakes.originsbukkit.storage.wrappers;

import java.util.UUID;

/**
 * The type Creeper nearcat timer data wrapper.
 *
 * @author Lebyy
 */
public class CreeperNearCatTimerDataWrapper {

    private UUID playerUUID;
    private int timerTimeLeft;
    private int nearcatTimeLeft;

    /**
     * Instantiates a new Creeper nearcat timer data wrapper.
     *
     * @param playerUUID             the player uuid
     * @param timerTimeLeft          the timer time left
     * @param nearcatTimeLeft the nearcat time left
     */
    public CreeperNearCatTimerDataWrapper(UUID playerUUID, int timerTimeLeft, int nearcatTimeLeft) {
        this.playerUUID = playerUUID;
        this.timerTimeLeft = timerTimeLeft;
        this.nearcatTimeLeft = nearcatTimeLeft;
    }

    /**
     * Gets player uuid.
     *
     * @return the player uuid
     */
    public UUID getPlayerUUID() {
        return playerUUID;
    }

    /**
     * Sets player uuid.
     *
     * @param playerUUID the player uuid
     */
    public void setPlayerUUID(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    /**
     * Gets timer time left.
     *
     * @return the timer time left
     */
    public int getTimerTimeLeft() {
        return timerTimeLeft;
    }

    /**
     * Sets timer time left.
     *
     * @param timerTimeLeft the timer time left
     */
    public void setTimerTimeLeft(int timerTimeLeft) {
        this.timerTimeLeft = timerTimeLeft;
    }

    /**
     * Gets nearcat time left.
     *
     * @return the nearcat time left
     */
    public int getNearCatTimeLeft() {
        return nearcatTimeLeft;
    }

    /**
     * Sets nearcat time left.
     *
     * @param nearcatTimeLeft the nearcat time left
     */
    public void setNearCatTimeLeft(int nearcatTimeLeft) {
        this.nearcatTimeLeft = nearcatTimeLeft;
    }
}