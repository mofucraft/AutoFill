package api;

import com.sk89q.worldguard.WorldGuard;
import org.bukkit.plugin.java.JavaPlugin;

public class WorldGuardAPI{
    private static WorldGuard api = null;

    public static boolean initialize(JavaPlugin plugin) {
        api = WorldGuard.getInstance();
        return false;
    }

    public static boolean finalize(JavaPlugin plugin) {
        api = null;
        return false;
    }

    public static WorldGuard getAPI() {
        return api;
    }
}
