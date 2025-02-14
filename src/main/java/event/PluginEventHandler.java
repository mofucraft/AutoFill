package event;

import common.EffectUtil;
import config.Config;
import database.PlayerStatus;
import database.PlayerStatusDatabase;
import database.list.PlayerStatusList;
import language.LanguageUtil;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.minecraft.autofill.RotationAngle;
import org.minecraft.autofill.UserData;
import org.minecraft.autofill.SelectMode;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class PluginEventHandler implements Listener {
    @EventHandler
    public void onClick(PlayerInteractEvent e){
        Player p = e.getPlayer();
        if(e.hasItem() && e.getItem().getType() == Config.getWand()){
            UserData userData = PlayerStatusList.getPlayerData(p);
            try(PlayerStatusDatabase database = new PlayerStatusDatabase()){
                PlayerStatus playerStatus = database.getPlayerStatus(p);
                Map<String, String> variables = new HashMap<>();
                variables.put("selectPosition", "X:" + (int) e.getClickedBlock().getLocation().getX() +
                        ",Y:" + (int) e.getClickedBlock().getLocation().getY() +
                        ",Z:" + (int) e.getClickedBlock().getLocation().getZ());
                if(userData.getSelectMode() == SelectMode.NORMAL) {
                    if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
                        variables.put("selectMaterial",e.getClickedBlock().getBlockData().getMaterial().toString());
                        LanguageUtil.sendReplacedMessage(p, playerStatus.getUsingLanguage(),"selectFirstPosition", variables);
                        userData.setFirstPosition(e.getClickedBlock().getLocation());
                        userData.setBlockData(e.getClickedBlock().getBlockData().clone());
                    } else if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        LanguageUtil.sendReplacedMessage(p, playerStatus.getUsingLanguage(),"selectSecondPosition", variables);
                        userData.setSecondPosition(e.getClickedBlock().getLocation());
                    }
                    if(userData.getFirstPosition() != null && userData.getSecondPosition() != null){
                        EffectUtil.showRangeParticle(p,null, userData.getFirstPosition(), userData.getSecondPosition(),Color.RED,Color.RED,0.1f,1000,5);
                    }
                }
                else if(userData.getSelectMode() == SelectMode.COPY) {
                    if (e.getAction() == Action.LEFT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        LanguageUtil.sendReplacedMessage(p, playerStatus.getUsingLanguage(),"selectCopyPosition", variables);
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
                        if(userData.getFirstPosition() != null && userData.getSecondPosition() != null) {
                            Location pos1;
                            Location pos2;
                            if (userData.getRotationAngle() == RotationAngle.ANGLE_0 || userData.getRotationAngle() == RotationAngle.ANGLE_180) {
                                pos1 = new Location(null, Math.min(userData.getFirstPosition().getX(), userData.getSecondPosition().getX()), Math.min(userData.getFirstPosition().getY(), userData.getSecondPosition().getY()), Math.min(userData.getFirstPosition().getZ(), userData.getSecondPosition().getZ()));
                                pos2 = new Location(null, Math.max(userData.getFirstPosition().getX(), userData.getSecondPosition().getX()), Math.max(userData.getFirstPosition().getY(), userData.getSecondPosition().getY()), Math.max(userData.getFirstPosition().getZ(), userData.getSecondPosition().getZ()));
                            } else {
                                pos1 = new Location(null, Math.min(userData.getFirstPosition().getZ(), userData.getSecondPosition().getZ()), Math.min(userData.getFirstPosition().getY(), userData.getSecondPosition().getY()), Math.min(userData.getFirstPosition().getX(), userData.getSecondPosition().getX()));
                                pos2 = new Location(null, Math.max(userData.getFirstPosition().getZ(), userData.getSecondPosition().getZ()), Math.max(userData.getFirstPosition().getY(), userData.getSecondPosition().getY()), Math.max(userData.getFirstPosition().getX(), userData.getSecondPosition().getX()));
                            }
                            EffectUtil.showRangeParticle(p, userData.getCopyPosition(), pos1, pos2, Color.RED, Color.RED, 0.1f, 1000, 10);
                        }
                        userData.setSelectMode(SelectMode.NORMAL);
                    }
                }
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            e.setCancelled(true);
        }
    }
}
