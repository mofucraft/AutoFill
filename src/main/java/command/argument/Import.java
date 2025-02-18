package command.argument;

import command.common.CommandMethod;
import common.Util;
import database.PlayerStatusDatabase;
import database.list.PlayerStatusList;
import language.LanguageUtil;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.minecraft.autofill.FillMode;
import org.minecraft.autofill.StructureData;
import org.minecraft.autofill.StructureDataSerializer;
import org.minecraft.autofill.UserData;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.zip.DataFormatException;

public class Import extends CommandMethod {
    public Import(){
        super("import",false,false);
    }

    @Override
    public boolean process(CommandSender sender, Command command, String label, String[] args) {
        Player p = (Player)sender;
        UserData userData = PlayerStatusList.getPlayerData(p);
        if(userData.isImporting()){
            try (PlayerStatusDatabase database = new PlayerStatusDatabase()) {
                LanguageUtil.sendMessage(p, database.getPlayerStatus(p).getUsingLanguage(), "importingNow");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return false;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try(PlayerStatusDatabase database = new PlayerStatusDatabase()){
                    userData.setImporting(true);
                    ItemStack book = p.getInventory().getItemInMainHand();
                    if(book.getType() != Material.WRITTEN_BOOK){
                        LanguageUtil.sendMessage(p, database.getPlayerStatus(p).getUsingLanguage(),"notHeldStructureDataBook");
                        userData.setImporting(false);
                        return;
                    }
                    BookMeta bookMeta = (BookMeta) book.getItemMeta();
                    List<BaseComponent[]> pages = bookMeta.spigot().getPages();
                    String[] structureIDAndInfoPage = ((TextComponent)pages.get(0)[0].getExtra().get(3)).getText().split(" ");
                    int dataPageStartIndex = Integer.parseInt(structureIDAndInfoPage[0]);
                    StringBuilder stringBuilder = new StringBuilder();
                    for(int i = dataPageStartIndex; i < pages.size();i++){
                        stringBuilder.append(((TextComponent)pages.get(i)[0]).getText());
                    }
                    StructureData structureData;
                    try {
                        structureData = StructureDataSerializer.deSerialize(Util.deCompress(Util.StringToByte(stringBuilder.toString()),Util.Base64NumberStringToInt("A" + structureIDAndInfoPage[1])));
                    } catch (DataFormatException e) {
                        LanguageUtil.sendMessage(p, database.getPlayerStatus(p).getUsingLanguage(),"parseFailedStructureData");
                        userData.setImporting(false);
                        return;
                    }
                    userData.setStructure(structureData.getStructure());
                    userData.setMode(FillMode.COPY);
                    userData.setFirstPosition(null);
                    userData.setSecondPosition(null);
                    LanguageUtil.sendMessage(p, database.getPlayerStatus(p).getUsingLanguage(),"successParseStructureData");
                    //Map<String, String> variables = new HashMap<>();
                    //variables.put("wand", Config.getWand().toString());
                    //LanguageUtil.sendReplacedMessage(p, database.getPlayerStatus(p).getUsingLanguage(),"copyPositionSetting", variables);
                    userData.setImporting(false);
                } catch (SQLException | IOException e) {
                    userData.setImporting(false);
                    throw new RuntimeException(e);
                }
            }
        }).start();
        return true;
    }

    @Override
    public List<String> tabCompleterProcess(CommandSender commandSender, Command command, String label, String[] args) {
        return new ArrayList<>(Collections.singletonList(""));
    }
}
