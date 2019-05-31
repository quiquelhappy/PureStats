package ovh.quiquelhappy.mcplugins.purestats;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import ovh.quiquelhappy.mcplugins.purestats.events.advancement;
import ovh.quiquelhappy.mcplugins.purestats.events.join;
import ovh.quiquelhappy.mcplugins.purestats.server.push;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.util.Timer;

public class main extends JavaPlugin {

    public static Plugin plugin = null;
    public static Connection conn = null;
    public static FileConfiguration config;

    public void onEnable(){
        plugin=this;

        createHeader("");

        System.out.println("  _____                 _____ _        _       ");
        System.out.println(" |  __ \\               / ____| |      | |      ");
        System.out.println(" | |__) |   _ _ __ ___| (___ | |_ __ _| |_ ___ ");
        System.out.println(" |  ___/ | | | '__/ _ \\\\___ \\| __/ _` | __/ __|");
        System.out.println(" | |   | |_| | | |  __/____) | || (_| | |_\\__ \\");
        System.out.println(" |_|    \\__,_|_|  \\___|_____/ \\__\\__,_|\\__|___/");


        createHeader("CONFIG");

        if ((new File("plugins" + File.separator + "PureStats" + File.separator + "config.yml")).isFile()) {
            System.out.println("[PureStats] Loading config");
        } else {
            System.out.println("[PureStats] Creating config");
            this.saveDefaultConfig();
            this.getConfig().options().copyDefaults(true);
        }

        config = this.getConfig();

        String direction = config.getString("mysql.direction");
        String port = config.getString("mysql.port");
        String database = config.getString("mysql.database");
        String username = config.getString("mysql.username");
        String password = config.getString("mysql.password");
        Boolean ssl = config.getBoolean("mysql.SSL");

        Boolean server_stats = config.getBoolean("server_stats.enable");
        Integer server_delay = config.getInt("server_stats.delay");

        try {
            createHeader("SQL DATABASE");
            conn = DriverManager.getConnection("jdbc:mysql://"+direction+":"+port+"/"+database+"?user="+username+"&password="+password+"&useSSL="+ssl);
            System.out.println("[PureStats] Connected to the database");
        } catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
            System.out.println("SQLState: " + e.getSQLState());
            System.out.println("VendorError: " + e.getErrorCode());
            System.out.println("[PureStats] Couldn't connect to the database");
        }

        if(conn==null){
            getServer().getPluginManager().disablePlugin(this);
        } else {

            Boolean users = helloTable();
            Boolean servertable = helloServerTable();
            Boolean advancements = helloAdvancementsTable();

            setupListeners();

            if(users&&servertable&&advancements){
                System.out.println("[PureStats] All the tables exist");
            } else {
                if(!users){
                    System.out.println("[PureStats] User table doesn't exists");
                    if(setupDatabase(database)){
                        System.out.println("[PureStats] Created user table");
                    } else {
                        getServer().getPluginManager().disablePlugin(this);
                    }
                }
                if(!servertable){
                    System.out.println("[PureStats] Server table doesn't exists");
                    if(setupServerDatabase(database)){
                        System.out.println("[PureStats] Created server table");
                    } else {
                        getServer().getPluginManager().disablePlugin(this);
                    }
                }
                if(!advancements){
                    System.out.println("[PureStats] Advancements table doesn't exists");
                    if(setupAdvancementsDatabase(database)){
                        System.out.println("[PureStats] Created advancements table");
                    } else {
                        getServer().getPluginManager().disablePlugin(this);
                    }
                }
            }

            if(server_stats){
                System.out.println("[PureStats] Enabling server stats");

                new BukkitRunnable() {
                    public void run() {
                        ovh.quiquelhappy.mcplugins.purestats.server.push.pushServerUpdate();
                    }
                }.runTaskTimer(plugin, 0, server_delay*20);

            } else {
                System.out.println("[PureStats] Server stats are disabled");
            }

        }

    }

    private void setupListeners(){
        Bukkit.getServer().getPluginManager().registerEvents(new join(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new advancement(), this);
    }

    private boolean createHeader(String header){
        System.out.println(" ");
        System.out.println(" ");
        System.out.println(header);
        System.out.println(" ");
        return true;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        createHeader("DISABLING PLUGIN");
    }

    public boolean helloTable(){
        DatabaseMetaData dbm;
        {
            try {
                dbm = main.conn.getMetaData();
                ResultSet tables;
                tables = dbm.getTables(null, null, "pure_stats", null);
                if (tables.next()) {
                    return true;
                }
                else {
                    return false;
                }
            } catch (SQLException e) {
                System.out.println("SQLException: " + e.getMessage());
                System.out.println("SQLState: " + e.getSQLState());
                System.out.println("VendorError: " + e.getErrorCode());
                System.out.println("[PureStats] Couldn't check the push table");
                return false;
            }
        }
    }

    public boolean helloServerTable(){
        DatabaseMetaData dbm;
        {
            try {
                dbm = main.conn.getMetaData();
                ResultSet tables;
                tables = dbm.getTables(null, null, "pure_stats_server", null);
                if (tables.next()) {
                    return true;
                }
                else {
                    return false;
                }
            } catch (SQLException e) {
                System.out.println("SQLException: " + e.getMessage());
                System.out.println("SQLState: " + e.getSQLState());
                System.out.println("VendorError: " + e.getErrorCode());
                System.out.println("[PureStats] Couldn't check the server table");
                return false;
            }
        }
    }

    public boolean helloAdvancementsTable(){
        DatabaseMetaData dbm;
        {
            try {
                dbm = main.conn.getMetaData();
                ResultSet tables;
                tables = dbm.getTables(null, null, "pure_stats_advancements", null);
                if (tables.next()) {
                    return true;
                }
                else {
                    return false;
                }
            } catch (SQLException e) {
                System.out.println("SQLException: " + e.getMessage());
                System.out.println("SQLState: " + e.getSQLState());
                System.out.println("VendorError: " + e.getErrorCode());
                System.out.println("[PureStats] Couldn't check the advancements table");
                return false;
            }
        }
    }

    public boolean setupDatabase(String dbname){
        try {
            Statement stmt = conn.createStatement();
            int rs = stmt.executeUpdate("CREATE TABLE `"+dbname+"`.`pure_stats` ( `uuid` VARCHAR(36) NOT NULL , `name` VARCHAR(16) NOT NULL , `lastip` VARCHAR(30) NULL , `country` VARCHAR(2) NULL DEFAULT NULL , `state` VARCHAR(2) NULL DEFAULT NULL , `playtime` BIGINT NOT NULL DEFAULT '0' , `registered` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ) ENGINE = InnoDB;");
            return true;
        } catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
            System.out.println("SQLState: " + e.getSQLState());
            System.out.println("VendorError: " + e.getErrorCode());
            System.out.println("[PureStats] Couldn't create the table");
            return false;
        }

    }

    public boolean setupServerDatabase(String dbname){
        try {
            Statement stmt = conn.createStatement();
            int rs = stmt.executeUpdate("CREATE TABLE `"+dbname+"`.`pure_stats_server` ( `server` VARCHAR(64) NOT NULL , `TPS` FLOAT NOT NULL , `online` TEXT NOT NULL , `RAM` BIGINT NOT NULL , `epoch` BIGINT NOT NULL ) ENGINE = InnoDB;");
            return true;
        } catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
            System.out.println("SQLState: " + e.getSQLState());
            System.out.println("VendorError: " + e.getErrorCode());
            System.out.println("[PureStats] Couldn't create the server table");
            return false;
        }
    }

    public boolean setupAdvancementsDatabase(String dbname){
        try {
            Statement stmt = conn.createStatement();
            int rs = stmt.executeUpdate("CREATE TABLE `"+dbname+"`.`pure_stats_advancements` ( `uuid` VARCHAR(36) NOT NULL , `server` TINYTEXT NULL DEFAULT NULL , `advancements` LONGTEXT NULL DEFAULT NULL ) ENGINE = InnoDB;");
            return true;
        } catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
            System.out.println("SQLState: " + e.getSQLState());
            System.out.println("VendorError: " + e.getErrorCode());
            System.out.println("[PureStats] Couldn't create the advancements table");
            return false;
        }
    }
}
