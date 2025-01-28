package command.argument;

import command.common.CommandMethod;
import common.EffectUtil;
import database.PlayerStatus;
import database.PlayerStatusDatabase;
import database.list.PlayerStatusList;
import language.LanguageUtil;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.minecraft.autofill.RotationAngle;
import org.minecraft.autofill.SelectMode;
import org.minecraft.autofill.UserData;

import java.sql.SQLException;
import java.util.*;

public class Rotation extends CommandMethod {
    public Rotation(){
        super("rotation",false);
    }

    @Override
    public boolean process(CommandSender sender, Command command, String label, String[] args) {
        Player p = (Player)sender;
        UserData userData = PlayerStatusList.getPlayerData(p);
        try(PlayerStatusDatabase database = new PlayerStatusDatabase()){
            PlayerStatus playerStatus = database.getPlayerStatus(p);
            if (args.length > 1) {
                boolean setCheck = false;
                for(RotationAngle rotationAngle:RotationAngle.values()){
                    if(rotationAngle.toString().equalsIgnoreCase(args[1])){
                        Map<String, String> variables = new HashMap<>();
                        variables.put("angle", rotationAngle.toString());
                        userData.setRotationAngle(rotationAngle);
                        LanguageUtil.sendReplacedMessage(p, playerStatus.getUsingLanguage(),"rotationAngleSet", variables);
                        setCheck = true;
                        break;
                    }
                }
                if(setCheck){
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
                else{
                    LanguageUtil.sendMessage(p, playerStatus.getUsingLanguage(), "invalidRotationAngle");
                }
            } else {
                Map<String, String> variables = new HashMap<>();
                variables.put("angle", userData.getRotationAngle().toString());
                LanguageUtil.sendReplacedMessage(p, playerStatus.getUsingLanguage(),"currentRotationAngle", variables);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    @Override
    public List<String> tabCompleterProcess(CommandSender commandSender, Command command, String label, String[] args) {
        final String[] rotations = { "0","90","180","270" };
        return new ArrayList<>(Arrays.asList(rotations));
    }
}
