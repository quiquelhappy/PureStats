package ovh.quiquelhappy.mcplugins.purestats.server;

import com.google.gson.Gson;
import com.sun.xml.internal.fastinfoset.util.StringArray;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.TimerTask;

import static ovh.quiquelhappy.mcplugins.purestats.server.TPSUtil.getRecentTps;

public class push {

    private static FileConfiguration config = ovh.quiquelhappy.mcplugins.purestats.main.config;
    private static Connection conn = ovh.quiquelhappy.mcplugins.purestats.main.conn;
    private static Gson gson = new Gson();
    private static Boolean debug = config.getBoolean("general.debug");

    public static void pushServerUpdate() {
        double[] TPS = getRecentTps();
        Double currentTPS = TPS[0];
        Long memory = Runtime.getRuntime().totalMemory()/(10^6);
        StringArray currentOnline = new StringArray();
        Long epoch = Instant.now().toEpochMilli();
        String server = config.getString("server_stats.server_name");

        for (Player players : Bukkit.getOnlinePlayers()) {
            currentOnline.add(players.getUniqueId().toString());
        }

        String currentOnlineJSON = gson.toJson(currentOnline);

        Statement stmt;
        try {
            stmt = conn.createStatement();
            if(debug){
                System.out.println("[PureStats] Updating "+server+". TPS: "+currentTPS+", Online Count: "+currentOnline.getSize()+", Memory: "+memory+"MB, Epoch: "+epoch);
            }
            int affected = stmt.executeUpdate("INSERT INTO `pure_stats_server`(`server`, `TPS`, `online`, `RAM`, `epoch`) VALUES ('"+server+"','"+currentTPS+"','"+currentOnlineJSON+"','"+memory+"','"+epoch+"')");
        } catch (SQLException e) {
            System.out.println(" ");
            System.out.println("[PureStats] Couldn't update server status:");
            System.out.println("SQLException: " + e.getMessage());
            System.out.println("SQLState: " + e.getSQLState());
            System.out.println("VendorError: " + e.getErrorCode());
            System.out.println(" ");
        }
    }
}
