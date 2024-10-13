package event;

import common.EffectUtil;
import config.Config;
import database.PlayerStatusList;
import database.StatusDatabase;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.minecraft.autofill.FillData;
import org.minecraft.autofill.SelectMode;

import java.sql.SQLException;

public class PluginEventHandler implements Listener {
    @EventHandler
    public void onClick(PlayerInteractEvent e){
        Player p = e.getPlayer();
        if(e.hasItem() && e.getItem().getType() == Config.getWand()){
            PlayerStatusList.checkUserData(p);
            FillData fillData = PlayerStatusList.getPlayerData(p);
            if(fillData.selectMode == SelectMode.Normal) {
                if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
                    p.sendMessage("§7---------------§8[§6AutoFill§8]§7---------------");
                    p.sendMessage("第1ポジションを設定しました(§aX:" + (int) e.getClickedBlock().getLocation().getX() +
                            ",Y:" + (int) e.getClickedBlock().getLocation().getY() +
                            ",Z:" + (int) e.getClickedBlock().getLocation().getZ() + "§f)。範囲選択後/autofillコマンドで一括設置できます");
                    p.sendMessage("選択中のブロック: §a" + e.getClickedBlock().getBlockData().getMaterial().toString());
                    fillData.position1 = e.getClickedBlock().getLocation();
                    fillData.blockData = e.getClickedBlock().getBlockData().clone();
                } else if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    p.sendMessage("§7---------------§8[§6AutoFill§8]§7---------------");
                    p.sendMessage("第2ポジションを設定しました(§aX:" + (int) e.getClickedBlock().getLocation().getX() +
                            ",Y:" + (int) e.getClickedBlock().getLocation().getY() +
                            ",Z:" + (int) e.getClickedBlock().getLocation().getZ() + "§f)。範囲選択後/autofillコマンドで一括設置できます");
                    fillData.position2 = e.getClickedBlock().getLocation();
                }
                if(fillData.position1 != null && fillData.position2 != null){
                    EffectUtil.showRangeParticle(p,null,fillData.position1,fillData.position2,Color.RED,Color.RED,0.1f,500,5);
                }
            }
            else if(fillData.selectMode == SelectMode.Copy) {
                if (e.getAction() == Action.LEFT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    p.sendMessage("§7---------------§8[§6AutoFill§8]§7---------------");
                    p.sendMessage("コピー先始点を設定しました(§aX:" + (int) e.getClickedBlock().getLocation().getX() +
                            ",Y:" + (int) e.getClickedBlock().getLocation().getY() +
                            ",Z:" + (int) e.getClickedBlock().getLocation().getZ() + "§f)");
                    p.sendMessage("始点は範囲選択されている領域の座標が小さい方からコピーされます");
                    p.sendMessage("既に範囲選択がされている場合は範囲が表示されます");
                    p.sendMessage("/autofillコマンドを使用するとコピーが開始されます");
                    if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
                        fillData.copyPosition = e.getClickedBlock().getLocation();
                    } else {
                        if (e.getBlockFace().getOppositeFace() == BlockFace.UP) {
                            fillData.copyPosition = e.getClickedBlock().getLocation().add(0, -1, 0);
                        } else if (e.getBlockFace().getOppositeFace() == BlockFace.DOWN) {
                            fillData.copyPosition = e.getClickedBlock().getLocation().add(0, 1, 0);
                        } else if (e.getBlockFace().getOppositeFace() == BlockFace.EAST) {
                            fillData.copyPosition = e.getClickedBlock().getLocation().add(-1, 0, 0);
                        } else if (e.getBlockFace().getOppositeFace() == BlockFace.WEST) {
                            fillData.copyPosition = e.getClickedBlock().getLocation().add(1, 0, 0);
                        } else if (e.getBlockFace().getOppositeFace() == BlockFace.SOUTH) {
                            fillData.copyPosition = e.getClickedBlock().getLocation().add(0, 0, -1);
                        } else if (e.getBlockFace().getOppositeFace() == BlockFace.NORTH) {
                            fillData.copyPosition = e.getClickedBlock().getLocation().add(0, 0, 1);
                        }
                    }
                    Location pos1;
                    Location pos2;
                    if(fillData.rotation == 0 || fillData.rotation == 180){
                        pos1 = new Location(null, Math.min(fillData.position1.getX(), fillData.position2.getX()), Math.min(fillData.position1.getY(), fillData.position2.getY()), Math.min(fillData.position1.getZ(), fillData.position2.getZ()));
                        pos2 = new Location(null, Math.max(fillData.position1.getX(), fillData.position2.getX()), Math.max(fillData.position1.getY(), fillData.position2.getY()), Math.max(fillData.position1.getZ(), fillData.position2.getZ()));
                    }
                    else{
                        pos1 = new Location(null, Math.min(fillData.position1.getZ(), fillData.position2.getZ()), Math.min(fillData.position1.getY(), fillData.position2.getY()), Math.min(fillData.position1.getX(), fillData.position2.getX()));
                        pos2 = new Location(null, Math.max(fillData.position1.getZ(), fillData.position2.getZ()), Math.max(fillData.position1.getY(), fillData.position2.getY()), Math.max(fillData.position1.getX(), fillData.position2.getX()));
                    }
                    EffectUtil.showRangeParticle(p,fillData.copyPosition,pos1,pos2,Color.RED,Color.RED,0.1f,500,5);
                    fillData.selectMode = SelectMode.Normal;
                }
            }
            e.setCancelled(true);
        }
    }
}
