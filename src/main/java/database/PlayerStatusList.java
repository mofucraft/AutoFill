package database;

import org.bukkit.entity.Player;
import org.minecraft.autofill.FillData;

import java.util.Hashtable;
import java.util.Map;
import java.util.UUID;

public class PlayerStatusList {
    private static final Map<UUID, FillData> playerData;

    static{
        playerData = new Hashtable<>();
    }

    public static void checkUserData(Player p){
        if(!playerData.containsKey(p.getUniqueId())){
            playerData.put(p.getUniqueId(),new FillData());
        }
    }
    public static FillData getPlayerData(Player p){
        return playerData.get(p.getUniqueId());
    }

    public static boolean containsKey(Player p){
        return playerData.containsKey(p.getUniqueId());
    }
}
