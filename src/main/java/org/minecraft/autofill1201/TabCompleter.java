package org.minecraft.autofill1201;

import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TabCompleter implements org.bukkit.command.TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command command, String alias, String[] args) {
        final List<String> completions = new ArrayList<>();
        if(args[0].equalsIgnoreCase("mode") && args.length > 1){
            List<String> modeList = new ArrayList<>();
            for(FillMode modes:FillMode.values()){
                completions.add(modes.getString());
            }
            StringUtil.copyPartialMatches(args[0], modeList, completions);
        }
        else if(args[0].equalsIgnoreCase("rotation") && args.length > 1){
            final String[] rotations = { "0","90","180","270" };
            for (String rotation:rotations) {
                completions.add(rotation);
            }
        }
        else{
            final String[] commands = { "mode","cset","rotation" };
            StringUtil.copyPartialMatches(args[0], Arrays.asList(commands), completions);
        }
        //sort the list
        //Collections.sort(completions);
        return completions;
    }
}
