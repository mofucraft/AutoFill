package api;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import static org.bukkit.Bukkit.getServer;

public class EconomyAPI{
    private static Economy api = null;

    public static boolean initialize(JavaPlugin plugin) {
        setupEconomy();
        return true;
    }

    public static boolean finalize(JavaPlugin plugin) {
        api = null;
        return true;
    }

    public static Economy getAPI() {
        return api;
    }

    private static boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        api = rsp.getProvider();
        return true;
    }
}
