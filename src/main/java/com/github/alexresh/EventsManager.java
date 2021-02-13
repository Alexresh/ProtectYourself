package com.github.alexresh;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class EventsManager implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        DatabaseHandler handler = DatabaseHandler.getInstance();
        handler.addBlockAction(event.getPlayer().getName(),"break", event.getBlock().getType(), event.getBlock().getLocation());
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event){
        DatabaseHandler handler = DatabaseHandler.getInstance();
        handler.addBlockAction(event.getPlayer().getName(),"place",event.getBlock().getType(), event.getBlock().getLocation());

    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event){
        DatabaseHandler handler = DatabaseHandler.getInstance();
        handler.addPlayerDeath(event.getEntity().getName(), event.getDeathMessage(), event.getEntity().getLocation());
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event){
        if(!event.getPlayer().hasPlayedBefore()){
            DatabaseHandler.getInstance().addSubscriber(event.getPlayer().getName());
        }
    }
}
