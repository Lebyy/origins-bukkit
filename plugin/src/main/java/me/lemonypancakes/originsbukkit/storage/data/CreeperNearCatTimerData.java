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
package me.lemonypancakes.originsbukkit.storage.data;

import com.google.gson.Gson;
import me.lemonypancakes.originsbukkit.storage.StorageHandler;
import me.lemonypancakes.originsbukkit.storage.wrappers.CreeperNearCatTimerDataWrapper;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * The type Creeper nearcat timer data.
 *
 * @author Lebyy
 */
public class CreeperNearCatTimerData {

    private final StorageHandler storageHandler;
    private List<CreeperNearCatTimerDataWrapper> creeperNearCatTimerDataWrappers = new ArrayList<>();

    /**
     * Gets storage handler.
     *
     * @return the storage handler
     */
    public StorageHandler getStorageHandler() {
        return storageHandler;
    }

    /**
     * Gets creeper nearcat timer data.
     *
     * @return the creeper nearcat timer data
     */
    public List<CreeperNearCatTimerDataWrapper> getCreeperNearCatTimerData() {
        return creeperNearCatTimerDataWrappers;
    }

    /**
     * Sets merling timer session data.
     *
     * @param creeperNearCatTimerDataWrappers the creeper nearcat timer data wrappers
     */
    public void setMerlingTimerSessionData(List<CreeperNearCatTimerDataWrapper> creeperNearCatTimerDataWrappers) {
        this.creeperNearCatTimerDataWrappers = creeperNearCatTimerDataWrappers;
    }

    /**
     * Instantiates a new Creeper nearcat timer data.
     *
     * @param storageHandler the storage handler
     */
    public CreeperNearCatTimerData(StorageHandler storageHandler) {
        this.storageHandler = storageHandler;
        init();
    }

    /**
     * Init.
     */
    private void init() {
        try {
            loadCreeperNearCatTimerData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create creeper nearcat timer data.
     *
     * @param playerUUID             the player uuid
     * @param timerTimeLeft          the timer time left
     * @param nearcatTimeLeft the nearcat time left
     */
    public void createCreeperNearCatTimerData(UUID playerUUID, int timerTimeLeft, int nearcatTimeLeft) {
        if (findCreeperNearCatTimerData(playerUUID) == null) {
            CreeperNearCatTimerDataWrapper creeperNearCatTimerDataWrapper = new CreeperNearCatTimerDataWrapper(playerUUID, timerTimeLeft, nearcatTimeLeft);
            getCreeperNearCatTimerData().add(creeperNearCatTimerDataWrapper);
            try {
                saveCreeperNearCatTimerData();
            } catch (IOException event) {
                event.printStackTrace();
            }
        }
    }

    /**
     * Find creeper nearcat timer data creeper nearcat timer data wrapper.
     *
     * @param playerUUID the player uuid
     *
     * @return the creeper nearcat timer data wrapper
     */
    public CreeperNearCatTimerDataWrapper findCreeperNearCatTimerData(UUID playerUUID) {
        for (CreeperNearCatTimerDataWrapper creeperNearCatTimerDataWrapper : getCreeperNearCatTimerData()) {
            if (creeperNearCatTimerDataWrapper.getPlayerUUID().equals(playerUUID)) {
                return creeperNearCatTimerDataWrapper;
            }
        }
        return null;
    }

    /**
     * Gets creeper nearcat timer data.
     *
     * @param playerUUID the player uuid
     *
     * @return the creeper nearcat timer data
     */
    public int getCreeperNearCatTimerData(UUID playerUUID) {
        for (CreeperNearCatTimerDataWrapper creeperNearCatTimerDataWrapper : getCreeperNearCatTimerData()) {
            if (creeperNearCatTimerDataWrapper.getPlayerUUID().equals(playerUUID)) {
                return creeperNearCatTimerDataWrapper.getTimerTimeLeft();
            }
        }
        return 0;
    }

    /**
     * Update creeper nearcat timer data.
     *
     * @param playerUUID                                the player uuid
     * @param newCreeperNearCatTimerDataWrapper the new creeper nearcat timer data wrapper
     */
    public void updateCreeperNearCatTimerData(UUID playerUUID, CreeperNearCatTimerDataWrapper newCreeperNearCatTimerDataWrapper) {
        if (findCreeperNearCatTimerData(playerUUID) != null) {
            for (CreeperNearCatTimerDataWrapper creeperNearCatTimerDataWrapper : getCreeperNearCatTimerData()) {
                if (creeperNearCatTimerDataWrapper.getPlayerUUID().equals(playerUUID)) {
                    creeperNearCatTimerDataWrapper.setTimerTimeLeft(newCreeperNearCatTimerDataWrapper.getTimerTimeLeft());
                    creeperNearCatTimerDataWrapper.setNearCatTimeLeft(newCreeperNearCatTimerDataWrapper.getNearCatTimeLeft());
                    try {
                        saveCreeperNearCatTimerData();
                    } catch (IOException event) {
                        event.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Delete creeper nearcat timer data.
     *
     * @param playerUUID the player uuid
     */
    public void deleteCreeperNearCatTimerData(UUID playerUUID) {
        if (findCreeperNearCatTimerData(playerUUID) != null) {
            for (CreeperNearCatTimerDataWrapper creeperNearCatTimerDataWrapper : getCreeperNearCatTimerData()) {
                if (creeperNearCatTimerDataWrapper.getPlayerUUID().equals(playerUUID)) {
                    getCreeperNearCatTimerData().remove(creeperNearCatTimerDataWrapper);
                    break;
                }
            }
            try {
                saveCreeperNearCatTimerData();
            } catch (IOException event) {
                event.printStackTrace();
            }
        }
    }

    /**
     * Save creeper nearcat timer data.
     *
     * @throws IOException the io exception
     */
    public void saveCreeperNearCatTimerData() throws IOException {

        new BukkitRunnable() {

            @Override
            public void run() {
                Gson gson = new Gson();
                String s = File.separator;
                File file = new File(getStorageHandler().getPlugin().getDataFolder().getAbsolutePath() + s + "cache" + s + "creeperdata" + s + "creepernearcattimerdata.json");

                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                try {
                    Writer writer = new FileWriter(file, false);
                    gson.toJson(getCreeperNearCatTimerData(), writer);
                    writer.flush();
                    writer.close();
                } catch (IOException event) {
                    event.printStackTrace();
                }
            }
        }.runTaskAsynchronously(getStorageHandler().getPlugin());
    }

    /**
     * Load creeper nearcat timer data.
     *
     * @throws IOException the io exception
     */
    public void loadCreeperNearCatTimerData() throws IOException {

        new BukkitRunnable() {

            @Override
            public void run() {
                Gson gson = new Gson();
                String s = File.separator;
                File file = new File(getStorageHandler().getPlugin().getDataFolder().getAbsolutePath() + s + "cache" + s + "creeperdata" + s + "creepernearcattimerdata.json");

                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                if (file.exists()) {
                    try {
                        Reader reader = new FileReader(file);
                        CreeperNearCatTimerDataWrapper[] n = gson.fromJson(reader, CreeperNearCatTimerDataWrapper[].class);
                        creeperNearCatTimerDataWrappers = new ArrayList<>(Arrays.asList(n));
                    } catch (FileNotFoundException event) {
                        event.printStackTrace();
                    }
                }
            }
        }.runTaskAsynchronously(getStorageHandler().getPlugin());
    }
}
