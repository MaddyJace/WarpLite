package com.maddyjace.warplite;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

@SuppressWarnings({"ALL"})
public enum Config {
    INSTANCE();

    // Language
    public static String language = "en_US";

    public void initialize() {
        File configFile = getConfigFile();
        parseYAMLData(YamlConfiguration.loadConfiguration(configFile));
    }

    private void parseYAMLData(ConfigurationSection config) {
        Config.language = config.getString( "language", "en_US");
    }


    private File getConfigFile() {
        File pluginFolder = Get.plugin().getDataFolder();

        if (!pluginFolder.exists()) {
            pluginFolder.mkdirs();
        }

        if (!pluginFolder.isDirectory()) {
            throw new IllegalStateException();
        }
        File configFile = new File(pluginFolder, "config.yml");
        if (!configFile.exists()) {
            Get.plugin().saveResource("config.yml", false);
        }
        return configFile;
    }

}
