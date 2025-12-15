package com.maddyjace.warplite;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum Language {
    Get;

    private final Map<String, Map<String, String>> languages = new ConcurrentHashMap<>();

    public void onEnable() {
        languages.clear();
        File folder = getLanguageFolder();
        getLanguageFiles(folder, (noExtension, file) -> {
            parseYAMLData(file.getName(), noExtension, YamlConfiguration.loadConfiguration(file));
        });
    }

    public void onDisable() {
        languages.clear();
    }

    public String translate(String language, String key, String... args) {
        Map<String, String> langMap = languages.get(language);
        if (langMap == null) {
            langMap = languages.get(Config.language);
        }
        String result = langMap.get(key);
        if (result == null) {
            return null;
        }
        return replacePlaceholders(result, args);
    }

    /**
     * 格式化带数字占位符的字符串模板
     * <pre>
     * 示例: format("用户{0}购买了{1}件商品", "杰克", "5")
     *       返回: "用户杰克购买了5件商品"
     * </pre>
     */
    private static String replacePlaceholders(String template, String... args) {
        Pattern PLACEHOLDER = Pattern.compile("\\{(\\d+)}");
        Matcher matcher = PLACEHOLDER.matcher(template);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String indexStr = matcher.group(1);
            String replacement = matcher.group();
            try {
                int index = Integer.parseInt(indexStr);
                if (index >= 0 && index < args.length) {
                    replacement = args[index];
                }
            } catch (NumberFormatException ignored) {}
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        return sb.toString().replace("&", "§");
    }

    // ==================== 解析配置 ====================


    private void parseYAMLData(String haveFileExtension, String noFileExtension, ConfigurationSection YAML) {
        if (YAML == null || YAML.getKeys(false).isEmpty()) {
            com.maddyjace.warplite.Get.plugin().getLogger().warning(
                    "Unable to parse the '" + haveFileExtension + "' language file, please check the configuration!");
            return;
        }
        Map<String, String> map = new ConcurrentHashMap<>();
        for (String key : YAML.getKeys(false)) {
            map.put(key, YAML.getString(key));
        }
        languages.put(noFileExtension, map);
    }


    // ==================== 文件操作 ====================

    /** 检查 {@code ./plugins/language} 目录中是否有language文件夹没有就创建并初始化！ */
    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    private File getLanguageFolder() {
        File pluginFolder = com.maddyjace.warplite.Get.plugin().getDataFolder();
        if (!pluginFolder.isDirectory()) {
            throw new IllegalStateException();
        }
        File taskFolder = new File(pluginFolder, "language");
        if (!taskFolder.exists()) {
            taskFolder.mkdirs();
            com.maddyjace.warplite.Get.plugin().saveResource("language/en_US.yml", false);
            com.maddyjace.warplite.Get.plugin().saveResource("language/zh_CN.yml", false);
        }
        return taskFolder;
    }

    /** 读取全部有效的{@code 语言_国家.YAML}文件，并单独执行逻辑。 */
    private void getLanguageFiles(File folder, BiConsumer<String, File> runnable) {
        File[] files = folder.listFiles();
        if (files == null) return;
        Set<String> nameSet = new HashSet<>();
        for (File file : files) {
            if (!file.isFile()) continue;
            if (!isYamlFile(file.getName())) continue;
            String noFileExtension = removeSuffix(file.getName());
            if (noFileExtension == null) continue;
            // 分割语言和国家
            String[] parts = noFileExtension.split("_");
            if (parts.length != 2) {
                com.maddyjace.warplite.Get.plugin().getLogger().warning(
                        "Invalid country/region language file, please correct or delete '" + file.getName() + "'.");
                continue;
            }
            if(!isAvailableLocale(parts)) {
                com.maddyjace.warplite.Get.plugin().getLogger().warning(
                        "Invalid country/region language file, please correct or delete '" + file.getName() + "'.");
                continue;
            }
            // 判断文件重名但不同大小写
            if (nameSet.contains(noFileExtension.toLowerCase())) {
                com.maddyjace.warplite.Get.plugin().getLogger().warning(
                        "There are multiple files with the same language and region, '" + file.getName() + "' will not be loaded.");
                continue;
            }
            nameSet.add(noFileExtension.toLowerCase());
            runnable.accept(noFileExtension.toLowerCase(), file);
        }
    }

    /** 检查地区与国家是否有效 */
    private boolean isAvailableLocale(String[] str) {
        Locale locale = new Locale(str[0].toLowerCase(), str[1].toLowerCase());
        return Arrays.asList(Locale.getAvailableLocales()).contains(locale);
    }

    /** 忽略大小写判断文件名后缀是否为 .YAML */
    private boolean isYamlFile(String name) {
        if (name == null) return false;
        if (name.toLowerCase(Locale.ROOT).endsWith(".yml")) {
            return true;
        } else return name.toLowerCase(Locale.ROOT).endsWith(".yaml");
    }

    /** 删除文件后缀保留文件名 */
    private String removeSuffix(String name) {
        if (name.toLowerCase(Locale.ROOT).endsWith(".yml")) {
            return name.substring(0, name.length() - 4);
        } else if (name.toLowerCase(Locale.ROOT).endsWith(".yaml")) {
            return name.substring(0, name.length() - 5);
        } return null;
    }

    // ==================== 获取国家和语言 ====================

    /** 系统语言和国家 */
    public static String getServerLanguage() {
        Locale locale = Locale.getDefault();
        String result = locale.getLanguage() + "_" + locale.getCountry();
        return result.toLowerCase();
    }

    /** 玩家语言和国家 */
    public static String getPlayerLanguage(Player player) {
        return player.getLocale().toLowerCase();   // 例如 "zh_cn"
    }

}
