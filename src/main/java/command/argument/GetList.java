package command.argument;

import command.common.CommandMethod;
import config.Config;
import database.PlayerStatusList;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.minecraft.autofill.FillData;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class GetList extends CommandMethod {
    public GetList(){
        this.argumentName = "getlist";
    }

    @Override
    public boolean process(CommandSender sender, Command command, String label, String[] args) {
        Player p = (Player)sender;
        PlayerStatusList.checkUserData(p);
        FillData fillData = PlayerStatusList.getPlayerData(p);
        if (fillData.position1 == null || fillData.position2 == null){
            p.sendMessage("§8[§6AutoFill§8] §c材料リストは範囲設定後でなければ取得できません");
            return false;
        }
        else if(!fillData.position1.getWorld().getName().equalsIgnoreCase(fillData.position2.getWorld().getName())){
            p.sendMessage("§8[§6AutoFill§8] §c第一ポジションと第二ポジションは同じワールドでなければ取得できません");
            return false;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                Location pos1 = new Location(null, Math.min(fillData.position1.getX(), fillData.position2.getX()), Math.min(fillData.position1.getY(), fillData.position2.getY()), Math.min(fillData.position1.getZ(), fillData.position2.getZ()));
                Location pos2 = new Location(null, Math.max(fillData.position1.getX(), fillData.position2.getX()), Math.max(fillData.position1.getY(), fillData.position2.getY()), Math.max(fillData.position1.getZ(), fillData.position2.getZ()));
                int Yc = (int) pos2.getY() - (int) pos1.getY() + 1;
                int Xc = (int) pos2.getX() - (int) pos1.getX() + 1;
                int Zc = (int) pos2.getZ() - (int) pos1.getZ() + 1;
                Map<String, Integer> itemList = new HashMap<>();
                for (int i = 0; i < Yc; i++) {
                    for (int j = 0; j < Xc; j++) {
                        for (int k = 0; k < Zc; k++) {
                            Block b = fillData.position1.getWorld().getBlockAt((int) pos1.getX() + j,
                                    (int) pos1.getY() + i,
                                    (int) pos1.getZ() + k);
                            if (!Config.checkReplaceableBlocks(b.getType())) {
                                continue;
                            }
                            if (itemList.containsKey(b.getType().getTranslationKey())) {
                                int count = itemList.get(b.getType().getTranslationKey()) + 1;
                                itemList.remove(b.getType().getTranslationKey());
                                itemList.put(b.getType().getTranslationKey(), count);
                            } else {
                                itemList.put(b.getType().getTranslationKey(), 1);
                            }
                        }
                    }
                }
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                String formatNowDate = dtf.format(LocalDateTime.now());
                List<String> pages = new ArrayList<>();
                pages.add("§7-----§4§l材料リスト§7-----§r\n\n§7[§c作成日時§7]§r\n" + formatNowDate +
                        "\n\n§7[§cリスト作成者§7]§r\n" + p.getName() +
                        "\n\n§7[§c建造物の座標§7]§r\nX:" + pos1.getBlockX() + ",Y:" + pos1.getBlockY() + ",Z:" + pos1.getBlockZ() + " ~" +
                        "\n  X:" + pos2.getBlockX() + ",Y:" + pos2.getBlockY() + ",Z:" + pos2.getBlockZ() +
                        "\n\n§7[§c建造物のワールド§7]§r\n" + fillData.position1.getWorld().getName());
                int lineCount = 0;
                StringBuilder pageText = new StringBuilder();
                for (Map.Entry<String, Integer> e : itemList.entrySet()) {
                    pageText.append("- §9").append(Material.matchMaterial(e.getKey().replace("block.minecraft.", ""))).append("§r\n");
                    pageText.append("  ").append(e.getValue().toString()).append("個 (").append((int) Math.floor(e.getValue() / 64.0)).append("st+").append((int) (e.getValue() - (Math.floor(e.getValue() / 64.0) * 64))).append("個)\n");
                    lineCount++;
                    if (lineCount >= 6) {
                        pages.add(pageText.toString());
                        pageText = new StringBuilder();
                        lineCount = 0;
                    }
                }
                if (lineCount != 0) {
                    pages.add(pageText.toString());
                }
                ItemStack writtenBook = new ItemStack(Material.WRITTEN_BOOK);
                BookMeta bookMeta = (BookMeta) writtenBook.getItemMeta();
                bookMeta.setTitle("§6§l材料リスト");
                bookMeta.setAuthor("AutoFill");
                bookMeta.setPages(pages);
                writtenBook.setItemMeta(bookMeta);
                p.getInventory().addItem(writtenBook);
                p.sendMessage("§8[§6AutoFill§8] §f材料リストを作成しました");
                p.sendMessage("§8[§6AutoFill§8] §f(インベントリに空きがないと本が作成されません)");
            }
        }).start();
        return true;
    }

    @Override
    public List<String> tabCompleterProcess(CommandSender commandSender, Command command, String label, String[] args) {
        return new ArrayList<>(Collections.singletonList(""));
    }
}
