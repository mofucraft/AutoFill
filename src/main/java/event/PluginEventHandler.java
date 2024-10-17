package event;

import common.EffectUtil;
import config.Config;
import database.list.PlayerStatusList;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.minecraft.autofill.UserData;
import org.minecraft.autofill.SelectMode;

public class PluginEventHandler implements Listener {
    @EventHandler
    public void onClick(PlayerInteractEvent e){
        Player p = e.getPlayer();
        if(e.hasItem() && e.getItem().getType() == Config.getWand()){
            PlayerStatusList.checkUserData(p);
            UserData userData = PlayerStatusList.getPlayerData(p);
            if(userData.getSelectMode() == SelectMode.NORMAL) {
                if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
                    p.sendMessage("§7---------------§8[§6AutoFill§8]§7---------------");
                    p.sendMessage("第1ポジションを設定しました(§aX:" + (int) e.getClickedBlock().getLocation().getX() +
                            ",Y:" + (int) e.getClickedBlock().getLocation().getY() +
                            ",Z:" + (int) e.getClickedBlock().getLocation().getZ() + "§f)。範囲選択後/autofillコマンドで一括設置できます");
                    p.sendMessage("選択中のブロック: §a" + e.getClickedBlock().getBlockData().getMaterial().toString());
                    userData.setFirstPosition(e.getClickedBlock().getLocation());
                    userData.setBlockData(e.getClickedBlock().getBlockData().clone());
                } else if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    p.sendMessage("§7---------------§8[§6AutoFill§8]§7---------------");
                    p.sendMessage("第2ポジションを設定しました(§aX:" + (int) e.getClickedBlock().getLocation().getX() +
                            ",Y:" + (int) e.getClickedBlock().getLocation().getY() +
                            ",Z:" + (int) e.getClickedBlock().getLocation().getZ() + "§f)。範囲選択後/autofillコマンドで一括設置できます");
                    userData.setSecondPosition(e.getClickedBlock().getLocation());
                }
                if(userData.getFirstPosition() != null && userData.getSecondPosition() != null){
                    EffectUtil.showRangeParticle(p,null, userData.getFirstPosition(), userData.getSecondPosition(),Color.RED,Color.RED,0.1f,500,5);
                }
            }
            else if(userData.getSelectMode() == SelectMode.COPY) {
                if (e.getAction() == Action.LEFT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    p.sendMessage("§7---------------§8[§6AutoFill§8]§7---------------");
                    p.sendMessage("コピー先始点を設定しました(§aX:" + (int) e.getClickedBlock().getLocation().getX() +
                            ",Y:" + (int) e.getClickedBlock().getLocation().getY() +
                            ",Z:" + (int) e.getClickedBlock().getLocation().getZ() + "§f)");
                    p.sendMessage("始点は範囲選択されている領域の座標が小さい方からコピーされます");
                    p.sendMessage("既に範囲選択がされている場合は範囲が表示されます");
                    p.sendMessage("/autofillコマンドを使用するとコピーが開始されます");
                    if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
                        userData.setCopyPosition(e.getClickedBlock().getLocation());
                    } else {
                        if (e.getBlockFace().getOppositeFace() == BlockFace.UP) {
                            userData.setCopyPosition(e.getClickedBlock().getLocation().add(0, -1, 0));
                        } else if (e.getBlockFace().getOppositeFace() == BlockFace.DOWN) {
                            userData.setCopyPosition(e.getClickedBlock().getLocation().add(0, 1, 0));
                        } else if (e.getBlockFace().getOppositeFace() == BlockFace.EAST) {
                            userData.setCopyPosition(e.getClickedBlock().getLocation().add(-1, 0, 0));
                        } else if (e.getBlockFace().getOppositeFace() == BlockFace.WEST) {
                            userData.setCopyPosition(e.getClickedBlock().getLocation().add(1, 0, 0));
                        } else if (e.getBlockFace().getOppositeFace() == BlockFace.SOUTH) {
                            userData.setCopyPosition(e.getClickedBlock().getLocation().add(0, 0, -1));
                        } else if (e.getBlockFace().getOppositeFace() == BlockFace.NORTH) {
                            userData.setCopyPosition(e.getClickedBlock().getLocation().add(0, 0, 1));
                        }
                    }
                    Location pos1;
                    Location pos2;
                    if(userData.getRotation() == 0 || userData.getRotation() == 180){
                        pos1 = new Location(null, Math.min(userData.getFirstPosition().getX(), userData.getSecondPosition().getX()), Math.min(userData.getFirstPosition().getY(), userData.getSecondPosition().getY()), Math.min(userData.getFirstPosition().getZ(), userData.getSecondPosition().getZ()));
                        pos2 = new Location(null, Math.max(userData.getFirstPosition().getX(), userData.getSecondPosition().getX()), Math.max(userData.getFirstPosition().getY(), userData.getSecondPosition().getY()), Math.max(userData.getFirstPosition().getZ(), userData.getSecondPosition().getZ()));
                    }
                    else{
                        pos1 = new Location(null, Math.min(userData.getFirstPosition().getZ(), userData.getSecondPosition().getZ()), Math.min(userData.getFirstPosition().getY(), userData.getSecondPosition().getY()), Math.min(userData.getFirstPosition().getX(), userData.getSecondPosition().getX()));
                        pos2 = new Location(null, Math.max(userData.getFirstPosition().getZ(), userData.getSecondPosition().getZ()), Math.max(userData.getFirstPosition().getY(), userData.getSecondPosition().getY()), Math.max(userData.getFirstPosition().getX(), userData.getSecondPosition().getX()));
                    }
                    EffectUtil.showRangeParticle(p, userData.getCopyPosition(),pos1,pos2,Color.RED,Color.RED,0.1f,500,5);
                    userData.setSelectMode(SelectMode.NORMAL);
                }
            }
            e.setCancelled(true);
        }
    }
}
