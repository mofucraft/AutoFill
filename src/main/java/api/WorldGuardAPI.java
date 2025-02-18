package api;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import common.Util;
import config.Config;
import org.bukkit.Location;
import org.bukkit.entity.Player;
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

    public static boolean isCanBuiltProtectedArea(RegionManager regionManager, Location location, Player player) {
        if(Util.hasValidPermission(player, Config.getAdminPermission())) return true;
        BlockVector3 position = BlockVector3.at(location.getX(),location.getY(),location.getZ());
        ApplicableRegionSet set = regionManager.getApplicableRegions(position);
        ProtectedRegion current = null;
        int priorityLevel = Integer.MIN_VALUE;
        for (ProtectedRegion pr: set) {
            if(priorityLevel <= pr.getPriority()){
                priorityLevel = pr.getPriority();
                current = pr;
            }
        }
        if(current == null) return true;
        return current.getMembers().contains(player.getUniqueId()) || current.getOwners().contains(player.getUniqueId());
    }
}
