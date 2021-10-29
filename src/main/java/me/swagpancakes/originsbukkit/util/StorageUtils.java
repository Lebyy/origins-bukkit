package me.swagpancakes.originsbukkit.util;

import com.google.gson.Gson;
import me.swagpancakes.originsbukkit.Main;
import me.swagpancakes.originsbukkit.enums.Origins;
import me.swagpancakes.originsbukkit.storage.OriginsPlayerData;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * The type Storage utils.
 */
public class StorageUtils {

    private static ArrayList<OriginsPlayerData> originsPlayerData = new ArrayList<>();

    /**
     * Create origins player data.
     *
     * @param playerUUID the player uuid
     * @param player     the player
     * @param origin     the origin
     */
    public static void createOriginsPlayerData(UUID playerUUID, Player player, Origins origin) {
        String playerName = player.getName();

        OriginsPlayerData originsPlayerData = new OriginsPlayerData(playerUUID, playerName, origin);
        StorageUtils.originsPlayerData.add(originsPlayerData);
        try {
            saveOriginsPlayerData();
        } catch (IOException event) {
            event.printStackTrace();
        }
    }

    /**
     * Find origins player data origins player data.
     *
     * @param playerUUID the player uuid
     * @return the origins player data
     */
    public static OriginsPlayerData findOriginsPlayerData(UUID playerUUID) {
        for (OriginsPlayerData originsPlayerData : StorageUtils.originsPlayerData) {
            if (originsPlayerData.getUuid().equals(playerUUID)) {
                return originsPlayerData;
            }
        }
        return null;
    }

    /**
     * Gets player origin.
     *
     * @param playerUUID the player uuid
     * @return the player origin
     */
    public static Origins getPlayerOrigin(UUID playerUUID) {
        for (OriginsPlayerData originsPlayerData : StorageUtils.originsPlayerData) {
            if (originsPlayerData.getUuid().equals(playerUUID)) {
                return originsPlayerData.getOrigin();
            }
        }
        return null;
    }

    /**
     * Delete origins player data.
     *
     * @param playerUUID the player uuid
     */
    public static void deleteOriginsPlayerData(UUID playerUUID) {
        for (OriginsPlayerData originsPlayerData : StorageUtils.originsPlayerData) {
            if (originsPlayerData.getUuid().equals(playerUUID)) {
                StorageUtils.originsPlayerData.remove(originsPlayerData);
                break;
            }
        }
        try {
            saveOriginsPlayerData();
        } catch (IOException event) {
            event.printStackTrace();
        }
    }

    /**
     * Update origins player data.
     *
     * @param playerUUID           the player uuid
     * @param newOriginsPlayerData the new origins player data
     */
    public static void updateOriginsPlayerData(UUID playerUUID, OriginsPlayerData newOriginsPlayerData) {
        for (OriginsPlayerData originsPlayerData : StorageUtils.originsPlayerData) {
            if (originsPlayerData.getUuid().equals(playerUUID)) {
                originsPlayerData.setPlayerName(newOriginsPlayerData.getPlayerName());
                originsPlayerData.setOrigin(newOriginsPlayerData.getOrigin());
                try {
                    saveOriginsPlayerData();
                } catch (IOException event) {
                    event.printStackTrace();
                }
            }
        }
    }

    /**
     * Find all origin player data list.
     *
     * @return the list
     */
    public static List<OriginsPlayerData> findAllOriginPlayerData() {
        return originsPlayerData;
    }

    /**
     * Save origins player data.
     *
     * @throws IOException the io exception
     */
    public static void saveOriginsPlayerData() throws IOException {
        Gson gson = new Gson();
        File file = new File(Main.getPlugin().getDataFolder().getAbsolutePath() + "/playerdata.json");
        Writer writer = new FileWriter(file, false);

        gson.toJson(originsPlayerData, writer);
        writer.flush();
        writer.close();
    }

    /**
     * Load origins player data.
     *
     * @throws IOException the io exception
     */
    public static void loadOriginsPlayerData() throws IOException {
        Gson gson = new Gson();
        File file = new File(Main.getPlugin().getDataFolder().getAbsolutePath() + "/playerdata.json");

        if (file.exists()) {
            Reader reader = new FileReader(file);
            OriginsPlayerData[] n = gson.fromJson(reader, OriginsPlayerData[].class);
            originsPlayerData = new ArrayList<>(Arrays.asList(n));
            ChatUtils.sendConsoleMessage("&a[Origins-Bukkit] Player data loaded.");
        }
    }
}