package ovh.quiquelhappy.mcplugins.purestats.events;

import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

import java.time.Instant;

public class advancement implements Listener {


    @EventHandler
    public void onAdvancement(PlayerAdvancementDoneEvent event){

        Advancement advancement = event.getAdvancement();
        Player player = event.getPlayer();

        if(!advancement.getKey().getKey().contains("recipes")){

            Long epoch = Instant.now().toEpochMilli();
            ovh.quiquelhappy.mcplugins.purestats.advancements.add.addAdvancement(player, advancement.getKey().getKey(), epoch);
        }

    }
}
