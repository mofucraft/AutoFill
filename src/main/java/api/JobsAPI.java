package api;

import com.gamingmesh.jobs.Jobs;
import org.bukkit.plugin.java.JavaPlugin;

public class JobsAPI{
    private static Jobs api = null;

    public static boolean initialize(JavaPlugin plugin) {
        api = Jobs.getInstance();
        return true;
    }

    public static boolean finalize(JavaPlugin plugin) {
        api = null;
        return true;
    }

    public static Jobs getAPI() {
        return api;
    }
}
