package ovh.quiquelhappy.mcplugins.purestats;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import ovh.quiquelhappy.mcplugins.purestats.events.join;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.*;

public class main extends JavaPlugin {

    public static Plugin plugin = null;
    public static Connection conn = null;

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

        FileConfiguration config = this.getConfig();

        String direction = config.getString("mysql.direction");
        String port = config.getString("mysql.port");
        String database = config.getString("mysql.database");
        String username = config.getString("mysql.username");
        String password = config.getString("mysql.password");
        Boolean ssl = config.getBoolean("mysql.SSL");

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
            if(helloTable()){
                System.out.println("[PureStats] Table exists");
                setupListeners();
            } else {
                System.out.println("[PureStats] Table doesn't exists");
                if(setupDatabase(database)){
                    System.out.println("[PureStats] Created table");
                    setupListeners();
                } else {
                    getServer().getPluginManager().disablePlugin(this);
                }
            }
        }

    }

    private void setupListeners(){
        Bukkit.getServer().getPluginManager().registerEvents(new join(), this);
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
                System.out.println("[PureStats] Couldn't check the stats table");
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

    public static boolean copy(InputStream source , String destination) {
        boolean succeess = true;

        System.out.println("Copying ->" + source + "\n\tto ->" + destination);

        try {
            Files.copy(source, Paths.get(destination), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            succeess = false;
        }

        return succeess;

    }
}
