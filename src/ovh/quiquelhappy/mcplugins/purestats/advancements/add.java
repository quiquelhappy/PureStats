package ovh.quiquelhappy.mcplugins.purestats.advancements;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class add {

    private static Connection conn = ovh.quiquelhappy.mcplugins.purestats.main.conn;
    private static FileConfiguration config = ovh.quiquelhappy.mcplugins.purestats.main.config;
    private static String server = config.getString("server_stats.server_name");

    public static void addAdvancement(Player player, String advancement, Long epoch){

        // created a formatted advancement for the database list

        databaseAdvancement NEW_ADVANCEMENT = (new databaseAdvancement(advancement, epoch));

        // starts db connection

        Statement stmt;
        try {

            Boolean isnew = false;
            Boolean already = false;

            // if there ins't any objects inside that query, the user is new to the database, so isnew => true
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT advancements FROM pure_stats_advancements WHERE uuid = '"+player.getUniqueId().toString()+"' AND server='"+server+"'");

            // created an empty json object
            List<databaseAdvancement> advancements = new ObjectMapper().readValue("[]", new TypeReference<List<databaseAdvancement>>(){});


            if(rs.next()){

                // okay, so the user is NOT new! let's get the prev. advancements
                JsonArray obj = new JsonParser().parse(rs.getString("advancements")).getAsJsonArray();

                // add all the prev. ones to the current selection
                for (int i = 0; i < obj.size(); i++) {
                    databaseAdvancement selectedadv = new databaseAdvancement(obj.get(i).getAsJsonObject().get("advancement").getAsString(),obj.get(i).getAsJsonObject().get("epoch").getAsLong());
                    advancements.add(selectedadv);
                }

                // iterate between all the advancements that are ALREADY assigned to the user
                for (int i = 0; i < advancements.size(); i++) {

                    if(advancements.get(i).getAdvancement().equals(NEW_ADVANCEMENT.getAdvancement())){

                        // woops! the one that we are trying to add is already there!
                        already = true;
                    }
                }

                // okay, so if it isn't there, let's add it
                if(!already){

                    System.out.println("[PureStats] Adding advancement "+NEW_ADVANCEMENT.getAdvancement()+" to "+player.getName()+"'s list in "+server);

                    // adding to the final list
                    advancements.add(NEW_ADVANCEMENT);
                } else {

                    // wooops! it's there. nvm!
                    //System.out.println("[PureStats] Advancement "+NEW_ADVANCEMENT.getAdvancement()+" was already inside "+player.getName()+"'s list in "+server);
                }

            } else {

                isnew=true;

                System.out.println("[PureStats] Adding advancement "+NEW_ADVANCEMENT.getAdvancement()+" to "+player.getName()+"'s list in "+server);

                // this just gets the new advancement and adds it into an empty list, cause there isn't any old ones!
                advancements.add(NEW_ADVANCEMENT);

            }

            if(!already){

                // let's change it into JSON!
                Gson gson = new Gson();
                Type listType = new TypeToken<List<databaseAdvancement>>() {}.getType();
                String json = gson.toJson(advancements, listType);

                if(!isnew){

                    // the user was already there, so let's create an update
                    System.out.println("[PureStats] Updating advancement list for "+player.getName()+" in "+server);
                    stmt.executeUpdate("UPDATE `pure_stats_advancements` SET `advancements`='"+json+"' WHERE uuid='"+player.getUniqueId().toString()+"' AND server='"+server+"'");
                } else {

                    // that's a new user! yay! let's add it to the database
                    System.out.println("[PureStats] Creating advancement list for "+player.getName()+" in "+server);
                    stmt.executeUpdate("INSERT INTO `pure_stats_advancements` (uuid, server, advancements) VALUES ('"+player.getUniqueId()+"', '"+server+"', '"+json+"')");
                }
            }

        } catch (SQLException | IOException e) {

            // lazy exception catcher lol
            e.printStackTrace();
        }
    }
}
