package database.list;

import org.bukkit.entity.Player;
import org.minecraft.autofill.UserData;

import java.util.Hashtable;
import java.util.Map;
import java.util.UUID;

public class PlayerStatusList {
    private static final Map<UUID, UserData> playerData;

    static{
        playerData = new Hashtable<>();
    }

    public static UserData getPlayerData(Player p){
        if(!playerData.containsKey(p.getUniqueId())){
            playerData.put(p.getUniqueId(),new UserData(p));
        }
        return playerData.get(p.getUniqueId());
    }
}
