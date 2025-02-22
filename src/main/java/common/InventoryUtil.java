package common;

import org.bukkit.Bukkit;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Slab;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class InventoryUtil {
    public static boolean takeItemSync(Player p, BlockData takeBlockData) throws ExecutionException, InterruptedException {
        Callable<Boolean> thread = () -> {
            synchronized (p.getInventory()) {
                Inventory inv = p.getInventory();
                int slot = inv.first(takeBlockData.getMaterial());
                if (slot == -1) {
                    return false;
                }
                int remove = 1;
                if(takeBlockData instanceof Slab){
                    Slab.Type type = ((Slab)takeBlockData).getType();
                    if(type.equals(Slab.Type.DOUBLE)){
                        remove = 2;
                    }
                }
                ItemStack item = inv.getItem(slot);
                if(remove == 1) {
                    if (item.getAmount() - 1 > 0) {
                        item.setAmount(item.getAmount() - 1);
                        inv.setItem(slot, item);
                    } else {
                        inv.setItem(slot, null);
                    }
                }
                else{
                    if (item.getAmount() - remove > 0) {
                        item.setAmount(item.getAmount() - remove);
                        inv.setItem(slot, item);
                        return true;
                    } else {
                        remove -= item.getAmount();
                        inv.setItem(slot, null);
                        int secondSlot = inv.first(takeBlockData.getMaterial());
                        if (secondSlot == -1) {
                            inv.setItem(slot, item);
                            return false;
                        }
                        ItemStack secondItem = inv.getItem(secondSlot);
                        secondItem.setAmount(secondItem.getAmount() - remove);
                        inv.setItem(secondSlot, secondItem);
                    }
                }
                return true;
            }
        };
        Future<Boolean> result = Bukkit.getScheduler().callSyncMethod(PluginUtil.getPlugin(), thread);
        return result.get();
    }

    public static boolean addItemSync(Player p, ItemStack item) throws ExecutionException, InterruptedException {
        Callable<Boolean> thread = () -> {
            synchronized (p.getInventory()) {
                p.getInventory().addItem(item);
                return true;
            }
        };
        Future<Boolean> result = Bukkit.getScheduler().callSyncMethod(PluginUtil.getPlugin(), thread);
        return result.get();
    }
}
