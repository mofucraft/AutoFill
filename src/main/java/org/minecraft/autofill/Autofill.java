package org.minecraft.autofill;

import common.Util;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class Autofill extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        // Plugin startup logic
        Util.initializePlugin(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        Util.finalizePlugin(this);
    }
}
