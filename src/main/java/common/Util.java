package common;

import org.bukkit.entity.Player;

public class Util {
    public static boolean hasValidPermission(Player p, String permission){
        return p.isPermissionSet(permission) && p.hasPermission(permission);
    }
}
