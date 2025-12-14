package com.maddyjace.warplite;

import org.bukkit.plugin.Plugin;

public class Get {

    private Get() {}

    private static Plugin plugin;

    public static void initialize(Plugin p) {
        plugin = p;
    }

    public static Plugin plugin() {
        return plugin;
    }

}

