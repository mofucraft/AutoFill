package command.argument;

import api.WorldGuardAPI;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import command.common.CommandMethod;
import common.InventoryUtil;
import common.PluginUtil;
import common.Util;
import config.Config;
import database.PlayerStatusDatabase;
import database.list.PlayerStatusList;
import language.LanguageUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Content;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.minecraft.autofill.StructureData;
import org.minecraft.autofill.StructureDataSerializer;
import org.minecraft.autofill.UserData;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class Export extends CommandMethod {
    public Export(){
        super("export",false,true);
    }

    @Override
    public boolean process(CommandSender sender, Command command, String label, String[] args) {
        Player p = (Player)sender;
        UserData userData = PlayerStatusList.getPlayerData(p);
        if (userData.getFirstPosition() == null || userData.getSecondPosition() == null){
            try (PlayerStatusDatabase database = new PlayerStatusDatabase()) {
                LanguageUtil.sendMessage(p, database.getPlayerStatus(p).getUsingLanguage(), "cantGenerateMaterialList");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return false;
        }
        else if(!userData.getFirstPosition().getWorld().getName().equalsIgnoreCase(userData.getSecondPosition().getWorld().getName())){
            try (PlayerStatusDatabase database = new PlayerStatusDatabase()) {
                LanguageUtil.sendMessage(p, database.getPlayerStatus(p).getUsingLanguage(), "notSamePositionWorld");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return false;
        }
        else if(userData.isExporting()){
            try (PlayerStatusDatabase database = new PlayerStatusDatabase()) {
                LanguageUtil.sendMessage(p, database.getPlayerStatus(p).getUsingLanguage(), "exportingNow");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return false;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
                RegionManager regions = container.get(BukkitAdapter.adapt(userData.getFirstPosition().getWorld()));
                try (PlayerStatusDatabase database = new PlayerStatusDatabase()) {
                    Location pos1 = new Location(null, Math.min(userData.getFirstPosition().getX(), userData.getSecondPosition().getX()), Math.min(userData.getFirstPosition().getY(), userData.getSecondPosition().getY()), Math.min(userData.getFirstPosition().getZ(), userData.getSecondPosition().getZ()));
                    Location pos2 = new Location(null, Math.max(userData.getFirstPosition().getX(), userData.getSecondPosition().getX()), Math.max(userData.getFirstPosition().getY(), userData.getSecondPosition().getY()), Math.max(userData.getFirstPosition().getZ(), userData.getSecondPosition().getZ()));
                    int Yc = (int) pos2.getY() - (int) pos1.getY() + 1;
                    int Xc = (int) pos2.getX() - (int) pos1.getX() + 1;
                    int Zc = (int) pos2.getZ() - (int) pos1.getZ() + 1;
                    Map<String, Integer> itemList = new HashMap<>();
                    ArrayList<ArrayList<ArrayList<BlockData>>> structure = new ArrayList<>();
                    int totalBlocks = 0;
                    LanguageUtil.sendMessage(p, database.getPlayerStatus(p).getUsingLanguage(), "exportStructureData");
                    userData.setExporting(true);
                    for (int i = 0; i < Yc; i++) {
                        ArrayList<ArrayList<BlockData>> list1 = new ArrayList<>();
                        for (int j = 0; j < Xc; j++) {
                            ArrayList<BlockData> list2 = new ArrayList<>();
                            for (int k = 0; k < Zc; k++) {
                                Block b = userData.getFirstPosition().getWorld().getBlockAt((int) pos1.getX() + j,
                                        (int) pos1.getY() + i,
                                        (int) pos1.getZ() + k);
                                if(!WorldGuardAPI.isCanBuiltProtectedArea(regions, b.getLocation(), userData.getPlayer())){
                                    LanguageUtil.sendMessage(p, database.getPlayerStatus(p).getUsingLanguage(), "otherPlayerProtectionArea");
                                    userData.setExporting(false);
                                    return;
                                }
                                list2.add(b.getBlockData());
                                if (Config.isNonConsumableBlock(b.getType())) {
                                    continue;
                                }
                                if (itemList.containsKey(b.getType().getTranslationKey())) {
                                    int count = itemList.get(b.getType().getTranslationKey()) + 1;
                                    itemList.remove(b.getType().getTranslationKey());
                                    itemList.put(b.getType().getTranslationKey(), count);
                                } else {
                                    itemList.put(b.getType().getTranslationKey(), 1);
                                }
                                totalBlocks++;
                            }
                            list1.add(list2);
                        }
                        structure.add(list1);
                    }
                    byte[] structureData;
                    try {
                        StructureData temp = new StructureData(structure);
                        structureData = StructureDataSerializer.serialize(temp);
                    } catch (IOException e) {
                        LanguageUtil.sendMessage(p, database.getPlayerStatus(p).getUsingLanguage(), "unknownError");
                        userData.setExporting(false);
                        throw new RuntimeException(e);
                    }
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
                    String formatNowDate = dtf.format(LocalDateTime.now());
                    List<BaseComponent[]> pages = new ArrayList<>();
                    TextComponent[] titlePage = new TextComponent[4];
                    titlePage[0] = new TextComponent("§7 -----§4§l設計データ§7-----§r\n§7[§c作成日§7]§r\n" + formatNowDate +
                            "\n§7[§c作成者§7]§r\n" + p.getName() +
                            "\n§7[§c設計詳細§7]§r" +
                            "\n総ブロック: " + totalBlocks + " B" +
                            "\n横幅(X): " + Xc + " B" +
                            "\n奥行(Z): " + Zc + " B" +
                            "\n高さ(Y): " + Yc + " B" +
                            "\n\n       §8<");
                    titlePage[1] = new TextComponent("インポート");
                    titlePage[1].setColor(ChatColor.GOLD);
                    titlePage[1].setBold(true);
                    titlePage[1].setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/autofill import"));
                    titlePage[1].setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new ComponentBuilder("クリックして設計データをインポート").color(ChatColor.GOLD).create()));
                    String futureData = "000";
                    try {
                        titlePage[2] = new TextComponent("§8>§r\n\n§7ID:" + Util.ByteToString(MessageDigest.getInstance("SHA-1").digest(structureData)).substring(0,4) + " ");
                    } catch (NoSuchAlgorithmException e) {
                        LanguageUtil.sendMessage(p, database.getPlayerStatus(p).getUsingLanguage(), "unknownError");
                        userData.setExporting(false);
                        throw new RuntimeException(e);
                    }
                    pages.add(titlePage);
                    int lineCount = 0;
                    StringBuilder pageText = new StringBuilder();
                    for (Map.Entry<String, Integer> e : itemList.entrySet()) {
                        pageText.append("- §9").append(Material.matchMaterial(e.getKey().replace("block.minecraft.", ""))).append("§r\n");
                        pageText.append("  ").append(e.getValue().toString()).append("個 (").append((int) Math.floor(e.getValue() / 64.0)).append("st+").append((int) (e.getValue() - (Math.floor(e.getValue() / 64.0) * 64))).append(")\n");
                        lineCount++;
                        if (lineCount >= 6) {
                            pages.add(new TextComponent[]{new TextComponent(pageText.toString())});
                            pageText = new StringBuilder();
                            lineCount = 0;
                        }
                    }
                    if (lineCount != 0) {
                        pages.add(new TextComponent[]{new TextComponent(pageText.toString())});
                    }
                    String structureDataString = Util.ByteToString(Util.compress(structureData));
                    TextComponent infoPageNum = new TextComponent(pages.size() + " " + Util.IntToBase64NumberString(structureData.length).substring(1) + " " + futureData);
                    infoPageNum.setColor(ChatColor.GRAY);
                    pages.get(0)[3] = infoPageNum;
                    for(int i = 1;;i++){
                        if(pages.size() < 100){
                            if(i * 255 <= structureDataString.length()){
                                TextComponent data = new TextComponent(structureDataString.substring((i - 1) * 255,i * 255));
                                data.setColor(ChatColor.GRAY);
                                pages.add(new TextComponent[]{data});
                            }
                            else{
                                TextComponent data = new TextComponent(structureDataString.substring((i - 1) * 255));
                                data.setColor(ChatColor.GRAY);
                                pages.add(new TextComponent[]{data});
                                break;
                            }
                        }
                        else{
                            LanguageUtil.sendMessage(p, database.getPlayerStatus(p).getUsingLanguage(), "tooBigStructureDataSize");
                            userData.setExporting(false);
                            return;
                        }
                    }
                    ItemStack writtenBook = new ItemStack(Material.WRITTEN_BOOK);
                    ItemMeta itemMeta = writtenBook.getItemMeta();
                    BookMeta bookMeta = (BookMeta) itemMeta;
                    bookMeta.setTitle("§6§l設計データ");
                    bookMeta.setAuthor("AutoFill");
                    bookMeta.spigot().setPages(pages);
                    writtenBook.setItemMeta(bookMeta);
                    InventoryUtil.addItemSync(p, writtenBook);
                    LanguageUtil.sendMessage(p, database.getPlayerStatus(p).getUsingLanguage(), "exportedStructureData");
                    userData.setExporting(false);
                } catch (SQLException | ExecutionException | InterruptedException e) {
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
