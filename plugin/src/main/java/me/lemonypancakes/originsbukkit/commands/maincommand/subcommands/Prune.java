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
package me.lemonypancakes.originsbukkit.commands.maincommand.subcommands;

import me.lemonypancakes.originsbukkit.commands.maincommand.MainCommand;
import me.lemonypancakes.originsbukkit.enums.Lang;
import me.lemonypancakes.originsbukkit.enums.Permissions;
import me.lemonypancakes.originsbukkit.util.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The type Prune.
 *
 * @author LemonyPancakes
 */
public class Prune {

    private final MainCommand mainCommand;

    /**
     * Gets main command.
     *
     * @return the main command
     */
    public MainCommand getMainCommand() {
        return mainCommand;
    }

    /**
     * Instantiates a new Prune.
     *
     * @param mainCommand the main command
     */
    public Prune(MainCommand mainCommand) {
        this.mainCommand = mainCommand;
    }

    /**
     * Prune sub command.
     *
     * @param sender  the sender
     * @param command the command
     * @param label   the label
     * @param args    the args
     */
    public void PruneSubCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            if (sender.hasPermission(Permissions.PRUNE.toString())) {
                if (args.length == 1) {
                    ChatUtils.sendCommandSenderMessage(sender, "&cNot enough arguments. Usage: /origins prune <player>");
                } else if (args.length == 2) {
                    Player target = Bukkit.getPlayer(args[1]);

                    if (target != null) {
                        String playerName = target.getName();
                        UUID playerUUID = target.getUniqueId();

                        if (getMainCommand().getCommandHandler().getPlugin().getStorageHandler().getOriginsPlayerData().findOriginsPlayerData(playerUUID) != null) {
                            getMainCommand().getCommandHandler().getPlugin().getStorageHandler().getOriginsPlayerData().deleteOriginsPlayerData(playerUUID);
                            getMainCommand().getCommandHandler().getPlugin().getListenerHandler().getPlayerOriginChecker().checkPlayerOriginData(target);
                            ChatUtils.sendCommandSenderMessage(sender, "&aSuccessfully pruned " + playerName + "'s data");
                        } else {
                            ChatUtils.sendCommandSenderMessage(sender, "&cCannot find " + playerName + "'s data");
                        }
                    } else {
                        ChatUtils.sendCommandSenderMessage(sender, "&cPlayer " + args[1] + " not found. Player must be online to do this.");
                    }
                } else {
                    ChatUtils.sendCommandSenderMessage(sender, "&cToo many arguments. Usage: /origins prune <player>");
                }
            } else {
                ChatUtils.sendCommandSenderMessage(sender, Lang.NO_PERMISSION_COMMAND.toString());
            }
        } else {
            if (args.length == 1) {
                ChatUtils.sendCommandSenderMessage(sender, "&cNot enough arguments. Usage: /origins prune <player>");
            } else if (args.length == 2) {
                Player target = Bukkit.getPlayer(args[1]);

                if (target != null) {
                    String playerName = target.getName();
                    UUID playerUUID = target.getUniqueId();

                    if (getMainCommand().getCommandHandler().getPlugin().getStorageHandler().getOriginsPlayerData().findOriginsPlayerData(playerUUID) != null) {
                        getMainCommand().getCommandHandler().getPlugin().getStorageHandler().getOriginsPlayerData().deleteOriginsPlayerData(playerUUID);
                        getMainCommand().getCommandHandler().getPlugin().getListenerHandler().getPlayerOriginChecker().checkPlayerOriginData(target);
                        ChatUtils.sendCommandSenderMessage(sender, "&a[Origins-Bukkit] Successfully pruned " + playerName + "'s data");
                    } else {
                        ChatUtils.sendCommandSenderMessage(sender, "&c[Origins-Bukkit] Cannot find " + playerName + "'s data");
                    }
                } else {
                    ChatUtils.sendCommandSenderMessage(sender, "&c[Origins-Bukkit] Player " + args[1] + " not found. Player must be online to do this.");
                }
            } else {
                ChatUtils.sendCommandSenderMessage(sender, "&c[Origins-Bukkit] Too many arguments. Usage: /origins prune <player>");
            }
        }
    }

    /**
     * Prune sub command tab complete list.
     *
     * @param sender  the sender
     * @param command the command
     * @param alias   the alias
     * @param args    the args
     *
     * @return the list
     */
    public List<String> PruneSubCommandTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> empty = new ArrayList<>();

        if (sender instanceof Player) {
            if (sender.hasPermission(Permissions.PRUNE.toString())) {
                if (args.length == 1) {
                    List<String> subCommand = new ArrayList<>();
                    subCommand.add("prune");

                    return subCommand;
                } else if (args.length == 2) {
                    if (args[0].equalsIgnoreCase("prune")) {
                        List<String> playerNames = new ArrayList<>();
                        Player[] players = new Player[Bukkit.getServer().getOnlinePlayers().size()];
                        Bukkit.getServer().getOnlinePlayers().toArray(players);
                        for (Player player : players) {
                            playerNames.add(player.getName());
                        }

                        return playerNames;
                    }
                }
            }
        } else {
            if (args.length == 1) {
                List<String> subCommand = new ArrayList<>();
                subCommand.add("prune");

                return subCommand;
            } else if (args.length == 2) {
                if (args[0].equalsIgnoreCase("prune")) {
                    List<String> playerNames = new ArrayList<>();
                    Player[] players = new Player[Bukkit.getServer().getOnlinePlayers().size()];
                    Bukkit.getServer().getOnlinePlayers().toArray(players);
                    for (Player player : players) {
                        playerNames.add(player.getName());
                    }

                    return playerNames;
                }
            }
        }
        return empty;
    }
}
