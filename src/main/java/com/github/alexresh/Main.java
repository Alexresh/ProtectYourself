package com.github.alexresh;


import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class Main extends JavaPlugin {
    @Override
    public void onEnable() {
        DatabaseHandler handler = DatabaseHandler.getInstance();
        this.getCommand("i").setExecutor(new CommandsManager());
        this.getCommand("white").setExecutor(new CommandsManager());
        BukkitScheduler scheduler = getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                DatabaseHandler.getInstance().purgeSubscribers();
                Bukkit.broadcastMessage("Удаление игроков...");
            }
        }, 0L, 20*30L);
        getServer().getPluginManager().registerEvents(new EventsManager(), this);
    }
}
