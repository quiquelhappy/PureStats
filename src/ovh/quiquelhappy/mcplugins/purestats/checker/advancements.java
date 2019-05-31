package ovh.quiquelhappy.mcplugins.purestats.checker;

import com.sun.xml.internal.fastinfoset.util.StringArray;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;
import ovh.quiquelhappy.mcplugins.purestats.main;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

import static org.bukkit.Bukkit.getAdvancement;
import static org.bukkit.NamespacedKey.MINECRAFT;

public class advancements {

    public static boolean hasAdvancement(Player player, String name) {
        // name should be something like minecraft:husbandry/break_diamond_hoe
        Advancement a = getAdvancement(new NamespacedKey(MINECRAFT, name));
        if(a == null){
            // advancement does not exists.
            return false;
        }
        AdvancementProgress progress = Objects.requireNonNull(player.getPlayer()).getAdvancementProgress(a);
        return progress.isDone();
    }

    public static long AdvancementEpoch(Player player, String name) {
        // name should be something like minecraft:husbandry/break_diamond_hoe
        Advancement a = getAdvancement(new NamespacedKey(MINECRAFT, name));
        if(a == null){
            // advancement does not exists.
            return 0;
        }
        AdvancementProgress progress = Objects.requireNonNull(player.getPlayer()).getAdvancementProgress(a);
        Date date = progress.getDateAwarded(progress.getAwardedCriteria().toArray()[0].toString());

        assert date != null;
        return date.getTime();
    }

    public static void checkAdvancements(){
        Connection conn = main.conn;
        Statement stmt;

        StringArray uuids = new StringArray();
        try {
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT uuid FROM pure_stats WHERE 1");

            while (rs.next()) {
                getPlayerAdvancements(rs.getString("uuid"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static void getPlayerAdvancements(String uuid){

        OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(uuid));

        StringArray pos = new StringArray();

        pos.add("story/root");
        pos.add("story/mine_stone");
        pos.add("story/upgrade_tools");
        pos.add("story/smelt_iron");
        pos.add("story/obtain_armor");
        pos.add("story/iron_tools");
        pos.add("story/deflect_arrow");
        pos.add("story/form_obsidian");
        pos.add("story/mine_diamond");
        pos.add("story/enter_the_nether");
        pos.add("story/shiny_gear");
        pos.add("story/enchant_item");
        pos.add("story/cure_zombie_villager");
        pos.add("story/follow_ender_eye");
        pos.add("story/enter_the_end");

        pos.add("end/root");
        pos.add("end/kill_dragon");
        pos.add("end/dragon_egg");
        pos.add("end/enter_end_gateway");
        pos.add("end/respawn_dragon");
        pos.add("end/dragon_breath");
        pos.add("end/find_end_city");
        pos.add("end/elytra");
        pos.add("end/levitate");

        pos.add("adventure/root");
        pos.add("adventure/voluntary_exile");
        pos.add("adventure/kill_a_mob");
        pos.add("adventure/trade");
        pos.add("adventure/ol_betsy");
        pos.add("adventure/sleep_in_bed");
        pos.add("adventure/hero_of_the_village");
        pos.add("adventure/throw_trident");
        pos.add("adventure/shoot_arrow");
        pos.add("adventure/kill_all_mobs");
        pos.add("adventure/totem_of_undying");
        pos.add("adventure/summon_iron_golem");
        pos.add("adventure/two_birds_one_arrow");
        pos.add("adventure/whos_the_pillager_now");
        pos.add("adventure/arbalistic");
        pos.add("adventure/adventuring_time");
        pos.add("adventure/very_very_frightening");
        pos.add("adventure/sniper_duel");

        pos.add("husbandry/root");
        pos.add("husbandry/breed_an_animal");
        pos.add("husbandry/tame_an_animal");
        pos.add("husbandry/fishy_business");
        pos.add("husbandry/plant_seed");
        pos.add("husbandry/bred_all_animals");
        pos.add("husbandry/complete_catalogue");
        pos.add("husbandry/tactical_fishing");
        pos.add("husbandry/balanced_diet");
        pos.add("husbandry/break_diamond_hoe");

        if(player.getPlayer()!=null){

            Player selected = player.getPlayer();

            for (int i = 0; i < pos.getSize(); i++) {
                if(hasAdvancement(selected,pos.get(i))){
                    // System.out.println(selected.getName()+" completed "+pos.get(i)+" on "+AdvancementEpoch(selected, pos.get(i)));
                    ovh.quiquelhappy.mcplugins.purestats.advancements.add.addAdvancement(selected,pos.get(i), AdvancementEpoch(selected, pos.get(i)));
                }
            }
        }
    }
}
