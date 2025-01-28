package command.argument;

import command.common.CommandMethod;
import config.Config;
import database.PlayerStatus;
import database.PlayerStatusDatabase;
import database.list.PlayerStatusList;
import language.LanguageUtil;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.minecraft.autofill.FillMode;
import org.minecraft.autofill.FillTaskParameter;
import org.minecraft.autofill.UserData;

import java.sql.SQLException;
import java.util.*;

public class Start extends CommandMethod {
    public Start(){
        super("start",false);
    }

    @Override
    public boolean process(CommandSender sender, Command command, String label, String[] args) {
        Player p = (Player)sender;
        UserData userData = PlayerStatusList.getPlayerData(p);
        try(PlayerStatusDatabase database = new PlayerStatusDatabase()) {
            PlayerStatus playerStatus = database.getPlayerStatus(p);
            //起動可能かチェック
            if(userData.getFirstPosition() == null || userData.getSecondPosition() == null){
                Map<String, String> variables = new HashMap<>();
                variables.put("wand", Config.getWand().toString());
                LanguageUtil.sendReplacedMessage(p, database.getPlayerStatus(p).getUsingLanguage(),"notSettingPosition", variables);
                return false;
            }
            if(!(p.getWorld().getName().equalsIgnoreCase(Config.getAllowWorldName()) &&
                    (userData.getCopyPosition() == null || userData.getFirstPosition().getWorld().getName().equalsIgnoreCase(Config.getAllowWorldName())) &&
                    (userData.getCopyPosition() == null || userData.getSecondPosition().getWorld().getName().equalsIgnoreCase(Config.getAllowWorldName())) &&
                    (userData.getCopyPosition() == null || userData.getCopyPosition().getWorld().getName().equalsIgnoreCase(Config.getAllowWorldName())))){
                LanguageUtil.sendMessage(p, database.getPlayerStatus(p).getUsingLanguage(), "notAutofillAllowedWorld");
                return false;
            }
            if(userData.getMode() == FillMode.COPY && userData.getCopyPosition() == null){
                Map<String, String> variables = new HashMap<>();
                variables.put("wand", Config.getWand().toString());
                LanguageUtil.sendReplacedMessage(p, database.getPlayerStatus(p).getUsingLanguage(),"notSetCopyPosition", variables);
                return false;
            }
            if(Config.isDisabledBlock(userData.getBlockData().getMaterial())) {
                Map<String, String> variables = new HashMap<>();
                variables.put("materialName", userData.getBlockData().getMaterial().toString());
                LanguageUtil.sendReplacedMessage(p, database.getPlayerStatus(p).getUsingLanguage(),"cantPlaceBlockByAutofill", variables);
                return false;
            }
            if(userData.getBlockData() == null && !userData.getBlockData().getMaterial().isBlock()) {
                LanguageUtil.sendMessage(p, database.getPlayerStatus(p).getUsingLanguage(), "selectedBlockIsNullOrNotBlock");
                return false;
            }
            //起動スレッド数取得
            int count = 1;
            if(args.length >= 2) {
                if(NumberUtils.isNumber(args[1])) {
                    count = Integer.parseInt(args[1]);
                }
                else {
                    LanguageUtil.sendMessage(p, playerStatus.getUsingLanguage(), "numberParseFailed");
                    return false;
                }
            }
            int finalCount = count;
            new Thread(() -> {
                int threadCount = 0;
                try {
                    ArrayList<FillTaskParameter> fillTaskParameters = new ArrayList<>();
                    for(int i = 0; i< finalCount; i++){
                        if(userData.getPlacingThread() < playerStatus.getMaxThread() && threadCount < playerStatus.getMaxThread()) {
                            fillTaskParameters.add(userData.runFillTask());
                            threadCount++;
                            Thread.sleep(50);
                        }
                        else{
                            break;
                        }
                    }
                    if(!fillTaskParameters.isEmpty()) {
                        Map<String, String> variables = new HashMap<>();
                        StringBuilder stringBuilder = new StringBuilder("Thread");
                        for(int i = 0;i<fillTaskParameters.size();i++){
                            if(i < fillTaskParameters.size() - 1) {
                                stringBuilder.append(String.format("%02d", fillTaskParameters.get(i).getThreadNumber())).append(", ");
                            }
                            else{
                                stringBuilder.append(String.format("%02d", fillTaskParameters.get(i).getThreadNumber()));
                            }
                        }
                        variables.put("fillMode", fillTaskParameters.get(0).getFillMode().toString());
                        variables.put("threadName", stringBuilder.toString());
                        LanguageUtil.sendReplacedMessage(fillTaskParameters.get(0).getUserData().getPlayer(), playerStatus.getUsingLanguage(), "startAutoFill", variables);
                        if (fillTaskParameters.get(0).getUserData().getMode() == FillMode.FILL) {
                            variables.put("materialName", fillTaskParameters.get(0).getBlockData().getMaterial().toString());
                            variables.put("totalBlock", Long.toString(fillTaskParameters.get(0).getTotalBlockCount()));
                            variables.put("totalBlockStack", String.format("%.1f", ((double) fillTaskParameters.get(0).getTotalBlockCount() / 64)));
                            LanguageUtil.sendReplacedMessage(fillTaskParameters.get(0).getUserData().getPlayer(), playerStatus.getUsingLanguage(), "startAutoFillInFillMode", variables);
                        } else if (fillTaskParameters.get(0).getUserData().getMode() == FillMode.FRAME) {
                            variables.put("materialName", fillTaskParameters.get(0).getBlockData().getMaterial().toString());
                            variables.put("totalBlock", Long.toString(fillTaskParameters.get(0).getTotalBlockCount()));
                            variables.put("totalBlockStack", String.format("%.1f", ((double) fillTaskParameters.get(0).getTotalBlockCount() / 64)));
                            LanguageUtil.sendReplacedMessage(fillTaskParameters.get(0).getUserData().getPlayer(), playerStatus.getUsingLanguage(), "startAutoFillInFrameMode", variables);
                        } else if (fillTaskParameters.get(0).getUserData().getMode() == FillMode.COPY) {
                            variables.put("angle", fillTaskParameters.get(0).getRotationAngle().toString());
                            LanguageUtil.sendReplacedMessage(fillTaskParameters.get(0).getUserData().getPlayer(), playerStatus.getUsingLanguage(), "startAutoFillInCopyMode", variables);
                        }
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    @Override
    public List<String> tabCompleterProcess(CommandSender commandSender, Command command, String label, String[] args) {
        return new ArrayList<>(Collections.singletonList("[起動スレッド数]"));
    }
}
