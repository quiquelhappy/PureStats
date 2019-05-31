package ovh.quiquelhappy.mcplugins.purestats.events;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.json.simple.JSONObject;
import ovh.quiquelhappy.mcplugins.purestats.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

public class join implements Listener {

    Connection conn = main.conn;

    @EventHandler
    public void PlayerJoinEvent(PlayerJoinEvent event) {
        String ip = (Objects.requireNonNull(event.getPlayer().getAddress())).getAddress().toString().split("/")[1];
        getLocation(ip,event.getPlayer());
        ovh.quiquelhappy.mcplugins.purestats.checker.advancements.getPlayerAdvancements(event.getPlayer().getUniqueId().toString());
    }

    public void getLocation(String ip, Player player){
        System.out.println("Looking up ip "+ip);

        URL url;
        try {
            url = new URL("https://geoip-db.com/json/"+ip);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/json");
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer content = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

            in.close();
            con.disconnect();

            String response = content.toString();
            JsonObject obj = new JsonParser().parse(response).getAsJsonObject();

            String country = null;
            String state = null;

            if(!obj.get("country_code").isJsonNull()){
                if(obj.get("country_code").getAsString()!="Not found"){
                    country=obj.get("country_code").getAsString();
                }
            }

            if(!obj.get("state").isJsonNull()){
                if(obj.get("state").getAsString()!="Not found"){
                    state=obj.get("state").getAsString().replace("'","");
                }
            }

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT uuid FROM pure_stats WHERE uuid = '"+player.getUniqueId().toString()+"'");
            if(rs.next()){
                updatePlayer(player,country,state,ip);
            } else {
                registerPlayer(player,country,state,ip);
            }

        } catch (IOException e) {
            // don't do anything
        } catch (SQLException e) {
            // don't do anything
        }

    }

    public void updatePlayer(Player player, String country, String state, String ip){

        String fcountry;
        String fstate;

        if(country!=null){
            if(!country.equals("Not found")){
                fcountry=country;
            } else {
                fcountry=null;
            }
        } else {
            fcountry=null;
        }

        if(state!=null){
            if(!state.equals("Not found")){
                fstate=state;
            } else {
                fstate=null;
            }
        } else {
            fstate=null;
        }

        Statement stmt;
        try {
            stmt = conn.createStatement();
            if(fcountry==null){
                System.out.println("[PureStats] Updating "+player.getName()+" with the uuid "+player.getUniqueId().toString());
                stmt.executeUpdate("UPDATE `pure_stats` SET `name`='"+player.getName()+"',`lastip`='"+ip+"' WHERE uuid='"+player.getUniqueId().toString()+"'");
            } else {
                if(fstate==null){
                    System.out.println("[PureStats] Updating "+player.getName()+" from "+fcountry+" with the uuid "+player.getUniqueId().toString());
                    stmt.executeUpdate("UPDATE `pure_stats` SET `name`='"+player.getName()+"',`lastip`='"+ip+"',`country`='"+fcountry+"' WHERE uuid='"+player.getUniqueId().toString()+"'");
                } else {
                    System.out.println("[PureStats] Updating "+player.getName()+" from "+fstate+", "+fcountry+" with the uuid "+player.getUniqueId().toString());
                    stmt.executeUpdate("UPDATE `pure_stats` SET `name`='"+player.getName()+"',`lastip`='"+ip+"',`country`='"+fcountry+"',`state`='"+fstate+"' WHERE uuid='"+player.getUniqueId().toString()+"'");
                }
            }
        } catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
            System.out.println("SQLState: " + e.getSQLState());
            System.out.println("VendorError: " + e.getErrorCode());
            System.out.println("[PureStats] Couldn't update player");
        }
    }


    public void registerPlayer(Player player, String country, String state, String ip){
        String fcountry;
        String fstate;

        if(country!=null){
            if(!country.equals("Not found")){
                fcountry=country;
            } else {
                fcountry=null;
            }
        } else {
            fcountry=null;
        }

        if(state!=null){
            if(!state.equals("Not found")){
                fstate=state;
            } else {
                fstate=null;
            }
        } else {
            fstate=null;
        }

        Statement stmt;
        try {
            stmt = conn.createStatement();
            if(fcountry==null){
                System.out.println("[PureStats] Registering "+player.getName());
                int affected = stmt.executeUpdate("INSERT INTO `pure_stats`(`uuid`, `name`, `lastip`) VALUES ('"+player.getUniqueId().toString()+"','"+player.getName()+"','"+ip+"')");
            } else {
                if(fstate==null){
                    System.out.println("[PureStats] Registering "+player.getName()+" from "+fcountry);
                    int affected = stmt.executeUpdate("INSERT INTO `pure_stats`(`uuid`, `name`, `lastip`, `country`) VALUES ('"+player.getUniqueId().toString()+"','"+player.getName()+"','"+ip+"','"+fcountry+"')");
                } else {
                    System.out.println("[PureStats] Registering "+player.getName()+" from "+fstate+", "+fcountry);
                    int affected = stmt.executeUpdate("INSERT INTO `pure_stats`(`uuid`, `name`, `lastip`, `country`, `state`) VALUES ('"+player.getUniqueId().toString()+"','"+player.getName()+"','"+ip+"','"+fcountry+"','"+fstate+"')");
                }
            }
        } catch (SQLException e) {
            System.out.println("[PureStats] Couldn't register player");
            System.out.println("SQLException: " + e.getMessage());
            System.out.println("SQLState: " + e.getSQLState());
            System.out.println("VendorError: " + e.getErrorCode());
        }
    }
}
