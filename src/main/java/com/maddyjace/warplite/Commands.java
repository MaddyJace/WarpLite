package com.maddyjace.warplite;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import java.util.Map;

public class Commands implements Listener {

    private final Warp warp = Warp.INSTANCE;

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String[] args = splitCommand(event.getMessage());

        if (args[0].equalsIgnoreCase("warp")) {
            warp(player, args);
            return;
        }

        if (args[0].equalsIgnoreCase("setwarp")) {
            setWarp(player, args);
            return;
        }

    }

    private void warp(Player player, String[] args) {
        if (args.length == 2) {
            if (warp.getWarps().containsKey(args[1])) {
                if (player.hasPermission("warp." + args[1]) || player.hasPermission("warp.*")) {
                    player.teleport(warp.getWarps().get(args[1]));
                }
            }
        } else if (args.length == 3) {
            Player target = Bukkit.getPlayer(args[2]);
            if (warp.getWarps().containsKey(args[1]) && getOnlinePlayer(args[2]) != null) {
                if (target.hasPermission("warplite.warp." + args[1]) || target.hasPermission("warplite.warp.*")) {
                    target.teleport(warp.getWarps().get(args[1]));
                }
            }
        }
    }

    private void setWarp(Player player, String[] args) {
        if (args.length == 2) {
            if (player.hasPermission("warplite.set.warp")) {
                String world = player.getWorld().getName();
                String x     = String.valueOf(player.getLocation().getX());
                String y     = String.valueOf(player.getLocation().getY());
                String z     = String.valueOf(player.getLocation().getZ());
                String yaw   = String.valueOf(player.getLocation().getYaw());
                String pitch = String.valueOf(player.getLocation().getPitch());
                Map<String, String> map = Warp.setWarpMap(world, x, y, z, yaw, pitch);
                warp.saveJsonData(args[1], map);
                warp.getWarps().put(args[1], player.getLocation());
                player.sendMessage(ChatColor.GREEN + "Warp Set!");
            }
        }
    }

    public static String[] splitCommand(String input) {
        if (input == null || input.isEmpty()) {
            return new String[0];
        }
        input = input.trim();
        if (input.startsWith("/")) {
            input = input.substring(1); // 去掉开头的 /
        }
        return input.split("\\s+"); // 按空格拆分
    }

    /**
     * 通过玩家名称获取在线玩家对象（忽略大小写）
     */
    public static Player getOnlinePlayer(String name) {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.getName().equalsIgnoreCase(name)) {
                return onlinePlayer;
            }
        }
        return null;
    }

}
