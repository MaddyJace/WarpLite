package com.maddyjace.warplite;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public enum Warp {
    INSTANCE;

    private final Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
    private final Map<String, Location> warps = new ConcurrentHashMap<>();

    public void onEnable() {
        warps.clear();
        loadFile(getWarpFolder(), file -> {
            if (isJsonFile(file.getName())) {
                Map<String, String> jsonData = readJson(file);
                if (jsonData == null) return;
                String fileName = removeSuffix(file.getName());
                World world = Bukkit.getWorld(jsonData.get("world"));
                if (world == null) return;
                double x      = Double.parseDouble(jsonData.get("x"));
                double y      = Double.parseDouble(jsonData.get("y"));
                double z      = Double.parseDouble(jsonData.get("z"));
                float yaw     = Float.parseFloat(jsonData.get("yaw"));
                float pitch   = Float.parseFloat(jsonData.get("pitch"));
                Location location = new Location(world, x, y, z, yaw, pitch);
                warps.put(fileName, location);
            }
        });
    }

    public void onDisable() {
        warps.clear();
    }

    public Map<String, Location> getWarps() {
        return warps;
    }

    /** 加载Warp数据到内存中 */
    private void loadFile(File folder, Consumer<File> runnable) {
        File[] files = folder.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (file.isDirectory()) {
                continue;
            }
            if (isJsonFile(file.getName().toLowerCase()))  {
                runnable.accept(file);
            }
        }
    }

    /** 检查 ./plugins/WarpLite 目录中是否有warps文件夹没有就创建！ */
    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    private File getWarpFolder() {
        File pluginFolder = Get.plugin().getDataFolder();
        if (!pluginFolder.isDirectory()) {
            throw new IllegalStateException();
        }
        File warpsFolder = new File(pluginFolder, "warps");
        if (!warpsFolder.exists()) {
            warpsFolder.mkdirs();
        }
        return warpsFolder;
    }

    /** 将Json反序列化为Map */
    public Map<String, String> readJson(File jsonFile) {
        try (Reader reader = new FileReader(jsonFile)) {
            Type type = new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, String> data = gson.fromJson(reader, type);
            return data == null || data.isEmpty() ? null : data;
        } catch (IOException | JsonSyntaxException e) {
            return null;
        }
    }

    public static Map<String, String> setWarpMap(String world, String x, String y, String z, String yaw, String pitch) {
        Map<String, String> data = new HashMap<>();
        data.put("world", world);
        data.put("x"    , x);
        data.put("y"    , y);
        data.put("z"    , z);
        data.put("yaw"  , yaw);
        data.put("pitch", pitch);
        return data;
    }

    /** 将Map序列化为Json */
    @SuppressWarnings({"ALL"})
    public void saveJsonData(String fileName, Map<String, String> data) {
        File file = new File(getWarpFolder(), fileName + ".json");
        try (Writer writer = new FileWriter(file)) {
            gson.toJson(data, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** 忽略大小写判断文件名后缀是否为 .json */
    public static boolean isJsonFile(String name) {
        if (name == null) return false;
        return name.toLowerCase(Locale.ROOT).endsWith(".json");
    }

    /** 删除文件后缀保留文件名 */
    public static String removeSuffix(String name) {
        return name.substring(0, name.length() - 5);
    }

    /** 删除文件名保留后缀名 */
    public static String getSuffix(String name) {
        return name.substring(name.length() - 5);
    }

}
