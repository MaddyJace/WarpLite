package com.maddyjace.warplite;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class WarpLite extends JavaPlugin {

    @Override
    public void onEnable() {
        Get.initialize(this);
        Config.INSTANCE.initialize();
        Warp.INSTANCE.onEnable();
        getServer().getPluginManager().registerEvents(new Commands(), this);
        this.getCommand("warplite").setTabCompleter(new TabComplete());
    }

    @Override
    public void onDisable() {
        Warp.INSTANCE.onDisable();
    }


}
