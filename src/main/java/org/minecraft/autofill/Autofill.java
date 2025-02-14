package org.minecraft.autofill;

import common.InitializeUtil;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class Autofill extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        InitializeUtil.initializePlugin(this);
    }

    @Override
    public void onDisable() {
        InitializeUtil.finalizePlugin(this);
    }
}
