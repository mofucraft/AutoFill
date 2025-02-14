package api;

import net.coreprotect.CoreProtect;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import static org.bukkit.Bukkit.getServer;

public class CoreProtectAPI{
    private static net.coreprotect.CoreProtectAPI api = null;

    public static boolean initialize(JavaPlugin plugin) {
        api = getCoreProtect();
        return true;
    }

    public static boolean finalize(JavaPlugin plugin) {
        api = null;
        return true;
    }

    public static net.coreprotect.CoreProtectAPI getAPI() {
        return api;
    }

    private static net.coreprotect.CoreProtectAPI getCoreProtect() {
        Plugin plugin = getServer().getPluginManager().getPlugin("CoreProtect");

        // Check that CoreProtect is loaded
        if (!(plugin instanceof CoreProtect)) {
            return null;
        }

        // Check that the API is enabled
        net.coreprotect.CoreProtectAPI CoreProtect = ((CoreProtect) plugin).getAPI();
        if (!CoreProtect.isEnabled()) {
            return null;
        }

        // Check that a compatible version of the API is loaded
        if (CoreProtect.APIVersion() < 9) {
            return null;
        }

        return CoreProtect;
    }
}
