package com.maddyjace.warplite;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.io.File;
import java.util.Map;

@SuppressWarnings({"ALL"})
public class Commands implements Listener {

    private final Warp warp = Warp.INSTANCE;
    private final Language lang = Language.Get;

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String[] args = splitCommand(event.getMessage());
        if (args[0].equalsIgnoreCase("warp")) {
            warp(player, args);
            return;
        }
        if (args[0].equalsIgnoreCase("setWarp")) {
            setWarp(player, args);
            return;
        }
        if (args[0].equalsIgnoreCase("deleteWarp")) {
            deleteWarp(player, args);
            return;
        }
    }

    /** 传送的标 */
    private void warp(Player player, String[] args) {
        if (player.hasPermission("warplite." + args[1]) || player.hasPermission("warplite.*")) {

            if (args.length == 2 && warp.getWarps().containsKey(args[1])) {
                player.teleport(warp.getWarps().get(args[1]));
                player.sendMessage(lang.translate(player.getLocale().toLowerCase(), "warp", args[1]));
            } else { player.sendMessage(lang.translate(player.getLocale().toLowerCase(), "noWarp", args[1])); }

            if (args.length == 3 && warp.getWarps().containsKey(args[1])) {
                Player target = Bukkit.getPlayer(args[2]);
                if (getOnlinePlayer(args[2]) != null) {
                    target.teleport(warp.getWarps().get(args[1]));
                    player.sendMessage(lang.translate(player.getLocale().toLowerCase(), "warp", args[1]));
                } else { player.sendMessage(lang.translate(player.getLocale().toLowerCase(), "noPlayer", args[1])); }
            } else { player.sendMessage(lang.translate(player.getLocale().toLowerCase(), "noWarp", args[1])); }

        } else {
            player.sendMessage(lang.translate(player.getLocale().toLowerCase(), "noPermissionWarp", args[1]));
        }
    }

    /** 设置地标 */
    private void setWarp(Player player, String[] args) {
        if (args.length == 2) {
            if (player.hasPermission("warplite.set")) {
                String world = player.getWorld().getName();
                String x     = String.valueOf(player.getLocation().getX());
                String y     = String.valueOf(player.getLocation().getY());
                String z     = String.valueOf(player.getLocation().getZ());
                String yaw   = String.valueOf(player.getLocation().getYaw());
                String pitch = String.valueOf(player.getLocation().getPitch());
                Map<String, String> locationMap = Warp.setWarpMap(world, x, y, z, yaw, pitch);
                warp.saveJsonData(args[1], locationMap);
                warp.getWarps().put(args[1], player.getLocation());
                player.sendMessage(lang.translate(player.getLocale().toLowerCase(), "setWarp", args[1]));
            } else {
                player.sendMessage(lang.translate(player.getLocale().toLowerCase(), "noPermissionSetWarp"));
            }
        }

        // setWarp name world x y z yaw pitch

    }

    /** 删除地标 */
    private void deleteWarp(Player player, String[] args) {
        if (player.hasPermission("warplite.delete")) {
            if (warp.getWarps().containsKey(args[1]) && warp.getWarpsFileName().containsKey(args[1])) {
                File file = new File( warp.getWarpFolder(), args[1] + warp.getWarpsFileName().get(args[1]));
                // 文件不存在时
                if (!file.exists()) {
                    warp.getWarps().remove(args[1]);
                    warp.getWarpsFileName().remove(args[1]);
                    player.sendMessage(lang.translate(player.getLocale().toLowerCase(), "failedToDeleteFile", args[1]));
                    return;
                }
                // 文件删除成功时
                if (file.delete()) {
                    warp.getWarps().remove(args[1]);
                    warp.getWarpsFileName().remove(args[1]);
                    player.sendMessage(lang.translate(player.getLocale().toLowerCase(), "warpDelete", args[1]));
                } else {
                    player.sendMessage(lang.translate(player.getLocale().toLowerCase(), "failedToDeleteFile", args[1]));
                }
            }
        } else {
            player.sendMessage(lang.translate(player.getLocale().toLowerCase(), "noPermissionWarpDelete", args[1]));
        }
    }

    /** 命令分割去除"/"并按空格分割为数 */
    public static String[] splitCommand(String input) {
        if (input == null || input.isEmpty()) {
            return new String[0];
        }
        input = input.trim();
        if (input.startsWith("/")) {
            input = input.substring(1);
        }
        return input.split("\\s+");
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
