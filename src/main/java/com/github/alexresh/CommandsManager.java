package com.github.alexresh;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class CommandsManager implements CommandExecutor, TabCompleter {

    private final int pageItemsCount = 10;

    @Override
    public List<String> onTabComplete(CommandSender sender, //registers the auto tab completer
                                      Command command,
                                      String alias,
                                      String[] args){
        if(args.length == 1){
            List<String> list = new ArrayList<>();
            list.add("page");
            list.add("player");
            return list;
        }
        return null;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String command, String[] args) {
        /*if(cmd.getName().equalsIgnoreCase("white")&&args.length>0){
            if(args[0].equals("check")&&args.length==2){
                sender.sendMessage(DatabaseHandler.getInstance().getSubscriber(args[1]));
                return true;
            }
            if(!DatabaseHandler.getInstance().addSubscriber(args[0])){
                sender.sendMessage("Something wrong");
            }
            return true;
        }*/

        if(!(sender instanceof Player)) return false;
        Player player = (Player) sender;
        if(cmd.getName().equalsIgnoreCase("i")){
            if(args.length == 0){
                inspectBlock(player, 1);
            }else{
                if(args[0].equalsIgnoreCase("page")){
                    if(args.length == 2){
                        try {
                            int page = Integer.parseInt(args[1]);
                            inspectBlock(player, page);
                        } catch (NumberFormatException e) {
                            player.sendMessage("Введите чилсло, например /i page 1");

                        }
                    }else{
                        inspectBlock(player, 1);
                    }
                    return true;
                }

                if(args[0].equalsIgnoreCase("player")){
                    inspectEntity(player, 1);
                }
            }
        }


        return true;
    }

    private void inspectEntity(Player player, int page) {
        ResultSet set = DatabaseHandler.getInstance().getPlayerDeaths(player.getLocation(), pageItemsCount,(page-1) * pageItemsCount);
        if(set != null){
            try {
                while(set.next()){
                    player.sendMessage(ChatColor.AQUA + set.getString("time")+" "+
                            ChatColor.BLUE + set.getString("player")+" death on "+
                            ChatColor.GREEN + "[" + set.getInt("x")+","+
                            set.getInt("y")+","+
                            set.getInt("z")+"]\n"+
                            ChatColor.WHITE +"Message: '" + set.getString("deathMessage")+"'");
                }
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        }
    }

    private void inspectBlock(Player player, int page){
        Block targetBlock = player.getTargetBlockExact(6);
        if(targetBlock != null){
            ResultSet set = DatabaseHandler.getInstance().getBlockEventsByLocation(targetBlock.getLocation(), pageItemsCount,(page-1) * pageItemsCount);

            if(set != null){
                try {
                    while(set.next()){
                        player.sendMessage(ChatColor.AQUA + set.getString("time")+ " " +
                                ChatColor.BLUE + set.getString("player")+ " " +
                                ChatColor.RED + set.getString("action")+ " " +
                                ChatColor.GREEN + set.getString("block")
                        );
                    }
                    player.sendMessage(ChatColor.AQUA + "Введите /i page "+ ++page +" для следующей страницы");
                } catch (SQLException exception) {
                    exception.printStackTrace();
                }
            }
        }else{
            player.sendMessage(ChatColor.RED + "Смотри на блок");
        }
    }
}
