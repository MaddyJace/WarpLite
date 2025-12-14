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
    INSTANCE;

    private final Map<String, Map<String, String>> languages = new ConcurrentHashMap<>();
    private static final Pattern PLACEHOLDER = Pattern.compile("\\{(\\d+)}");

    public void loadLanguage() {
        languages.clear();
        File folder = getLanguageFolder();
        getLanguageFiles(folder, (noExtension, file) -> {
            parseYAMLData(file.getName(), noExtension, YamlConfiguration.loadConfiguration(file));
        });
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
        Matcher matcher = PLACEHOLDER.matcher(result);
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
        if ( YAML == null || YAML.getKeys(false).isEmpty()) {

            Get.plugin().getLogger().warning(
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

    /** 获取 {@code plugins/ChallengeMission/language} 的路径，没有就初始化 */
    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    private File getLanguageFolder() {
        File pluginFolder = Get.plugin().getDataFolder();
        if (!pluginFolder.isDirectory()) {
            throw new IllegalStateException();
        }
        File taskFolder = new File(pluginFolder, "language");
        if (!taskFolder.exists()) {
            taskFolder.mkdirs();
            Get.plugin().saveResource("language/en_US.yml", false);
            Get.plugin().saveResource("language/zh_CN.yml", false);
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
            String filename = file.getName();

            // 判断后缀并去除扩展名
            String lower = filename.toLowerCase();
            boolean isYaml = lower.endsWith(".yml") || lower.endsWith(".yaml");
            if (!isYaml) continue;
            String baseName = filename.substring(0, filename.lastIndexOf('.'));

            // 分割语言和国家
            String[] parts = baseName.split("_");
            if (parts.length != 2) {
                Get.plugin().getLogger().warning(
                        "Invalid country/region language file, please correct or delete '" + filename + "'.");
                continue;
            }

            // 校验是否为有效预言和国家组合
            String lang = parts[0].toLowerCase();
            String country = parts[1].toUpperCase();
            Locale locale = new Locale(lang, country);
            boolean valid = Arrays.asList(Locale.getAvailableLocales()).contains(locale);
            if (!valid) {
                Get.plugin().getLogger().warning(
                        "Invalid country/region language file, please correct or delete '" + filename + "'.");
                continue;
            }

            // 判断文件重名但不同大小写
            if (nameSet.contains(baseName.toLowerCase())) {
                System.out.println("Duplicate language file ignored: " + filename);
                Get.plugin().getLogger().warning(
                        "There are multiple files with the same language and region, '" + filename + "' will not be loaded.");
                continue;
            }
            nameSet.add(baseName.toLowerCase());
            runnable.accept(baseName.toLowerCase(), file);
        }
    }

    public static String getServerLanguage() {
        Locale locale = Locale.getDefault();
        String result = locale.getLanguage() + "_" + locale.getCountry();
        return result.toLowerCase();
    }

    public static String getPlayerLanguage(Player player) {
        return player.getLocale().toLowerCase();   // 例如 "zh_cn"
    }

}
