package com.github.alexresh;

import org.bukkit.*;
import org.sqlite.JDBC;

import java.sql.*;
import java.util.Date;
import java.util.List;

public class DatabaseHandler {

    private static DatabaseHandler instance;
    private static final String conStr = "jdbc:sqlite:plugins/core.db";
    private Connection connection;

    private DatabaseHandler() {

        try {
            DriverManager.deregisterDriver(new JDBC());
            this.connection = DriverManager.getConnection(conStr);
            createBlockTable();
            createPlayerTable();
            //createSubscribersTable();
        } catch (SQLException ex) {
            Bukkit.getLogger().info(ex.getSQLState());
        }


    }

    public static DatabaseHandler getInstance(){
        if(instance == null){
            instance = new DatabaseHandler();
        }
        return instance;
    }

    public boolean addSubscriber(String player){
        String sqlDel = "DELETE FROM Subscribers WHERE nick = ?";
        String sqlAdd = "INSERT INTO Subscribers(nick) VALUES(?)";
        try {
            PreparedStatement statement = connection.prepareStatement(sqlDel);
            statement.setString(1, player);
            statement.executeUpdate();
            statement.close();
            statement = connection.prepareStatement(sqlAdd);
            statement.setString(1, player);
            if(!(statement.executeUpdate()>0)) return false; else{Bukkit.getPlayer(player).sendMessage("У вас ограничено время пребывания на сервере");}
            statement.close();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return true;
    }

    public String getSubscriber(String player){
        String sql = "SELECT nick, time FROM Subscribers WHERE nick = ?";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, player);
            ResultSet set = statement.executeQuery();
            if(set.next()){
                String nick = set.getString("nick");
                String time = set.getString("time");
                return nick+" "+time;
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return "Нету";
    }

    public ResultSet getBlockEventById(int id){
        String sql = "Select time, player, action_type, block, location from Blocks where id = ?";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, id);
            return statement.executeQuery();
        } catch (SQLException ex) {
            return null;
        }
    }

    public ResultSet getBlockEventsByLocation(Location loc, int count, int offset){
        String sql = "Select time, player, action, block from Blocks " +
                "where world = ? and x = ? and y = ? and z = ? order by time DESC LIMIT ? offset ?";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, loc.getWorld().getName());
            statement.setInt(2, loc.getBlockX());
            statement.setInt(3, loc.getBlockY());
            statement.setInt(4, loc.getBlockZ());
            statement.setInt(5, count);
            statement.setInt(6, offset);
            return statement.executeQuery();
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public ResultSet getBlockEvents(int count){
        String sql = "Select time, player, action, block, location from Blocks order by time desc limit ?";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, count);
            return statement.executeQuery();
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public ResultSet getPlayerDeaths(Location location, int count, int offset){
        String sql = "SELECT time, player, x, y, z, deathMessage FROM PlayerDeath " +
                "WHERE world = ? AND (x>?-10 AND x<?+10) AND (y>?-10 AND y<?+10) AND (z>?-10 AND z<?+10) " +
                "ORDER BY time DESC " +
                "LIMIT ? OFFSET ?";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, location.getWorld().getName());
            statement.setInt(2, location.getBlockX());
            statement.setInt(3, location.getBlockX());
            statement.setInt(4, location.getBlockY());
            statement.setInt(5, location.getBlockY());
            statement.setInt(6, location.getBlockZ());
            statement.setInt(7, location.getBlockZ());
            statement.setInt(8, count);
            statement.setInt(9, offset);
            return statement.executeQuery();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    public void purgeSubscribers(){
       String sql = "SELECT nick FROM Subscribers WHERE time < (select datetime('now','localtime'))";
       try {
           Statement statement = connection.createStatement();
           ResultSet unsubscribers = statement.executeQuery(sql);
           if(unsubscribers != null){
               while(unsubscribers.next()){
                   String player = unsubscribers.getString("nick");
                   PreparedStatement delStatement = connection.prepareStatement("DELETE FROM Subscribers WHERE nick = ?");
                   delStatement.setString(1, player);
                   delStatement.executeUpdate();
                   delStatement.close();
                   if(Bukkit.getPlayer(player)!=null){
                       Bukkit.getBanList(BanList.Type.NAME).addBan(player,"Бесплатный срок истёк, купите подписку в дискорде\nflightcraft.discordsite.com", null,null);
                       Bukkit.getPlayer(player).kickPlayer("Бесплатный срок истёк, купите подписку в дискорде\nflightcraft.discordsite.com");
                   }
               }
           }
           statement.close();

       } catch (SQLException exception) {
           exception.printStackTrace();
       }
    }

    public void addPlayerDeath(String playerName, String deathMessage, Location location){
        String sql = "INSERT INTO PlayerDeath(player, deathMessage, world, x, y, z) " +
                "VALUES(?,?,?,?,?,?)";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, playerName);
            statement.setString(2, deathMessage);
            statement.setString(3, location.getWorld().getName());
            statement.setInt(4, location.getBlockX());
            statement.setInt(5, location.getBlockY());
            statement.setInt(6, location.getBlockZ());
            statement.executeUpdate();
            statement.close();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public void addBlockAction(String playerName, String action, Material blockType, Location location){
        String sql = "Insert into Blocks(player, action, block, world, x, y, z) values(?,?,?,?,?,?,?)";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, playerName);
            statement.setString(2, action);
            statement.setString(3, blockType.name());
            statement.setString(4, location.getWorld().getName());
            statement.setInt(5, location.getBlockX());
            statement.setInt(6, location.getBlockY());
            statement.setInt(7, location.getBlockZ());
            statement.executeUpdate();
            statement.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void createBlockTable(){
        String sql = "Create table if not exists Blocks(id integer primary key autoincrement, " +
                "time datetime default (datetime('now','localtime','+3 hour'))," +
                "player text not null," +
                "action text not null," +
                "block text not null,"+
                "world text not null," +
                "x integer not null," +
                "y integer not null," +
                "z integer not null" +
                ");";
        try {
            Statement statement = connection.createStatement();
            statement.execute(sql);
            statement.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void createPlayerTable(){
        String sql = "CREATE TABLE IF NOT EXISTS PlayerDeath" +
                "(id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "time DATETIME DEFAULT (datetime('now','localtime'))," +
                "player TEXT NOT NULL," +
                "deathMessage TEXT NOT NULL," +
                "world TEXT NOT NULL," +
                "x INTEGER NOT NULL," +
                "y INTEGER NOT NULL," +
                "z INTEGER NOT NULL);";
        try {
            Statement statement = connection.createStatement();
            statement.execute(sql);
            statement.close();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    private void createSubscribersTable(){
        String sql = "CREATE TABLE IF NOT EXISTS Subscribers" +
                "(id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "time DATETIME DEFAULT (datetime('now','localtime','+1 minute'))," +
                "nick TEXT NOT NULL);";
        try {
            Statement statement = connection.createStatement();
            statement.execute(sql);
            statement.close();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }
}
