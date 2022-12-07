package fr.valgrifer.loupgarou.listeners;

import fr.valgrifer.loupgarou.inventory.LGInventoryHolder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.comphenix.protocol.wrappers.EnumWrappers.ScoreboardAction;

import com.comphenix.packetwrapper.WrapperPlayServerScoreboardScore;
import com.comphenix.packetwrapper.WrapperPlayServerScoreboardTeam;
import fr.valgrifer.loupgarou.events.LGGameJoinEvent;
import org.bukkit.event.inventory.InventoryClickEvent;

public class LoupGarouListener implements Listener {
	@EventHandler
	public void onGameJoin(LGGameJoinEvent e) {
		//Tous les loups-garous
		WrapperPlayServerScoreboardTeam teamDelete = new WrapperPlayServerScoreboardTeam();
		teamDelete.setMode(1);
		teamDelete.setName("loup_garou_list");
		
		teamDelete.sendPacket(e.getPlayer().getPlayer());
		
		//Loup-Garou noir
		WrapperPlayServerScoreboardScore score = new WrapperPlayServerScoreboardScore();
		score.setObjectiveName("lg_scoreboard");
		score.setValue(0);
		score.setScoreName("été");
		score.setScoreboardAction(ScoreboardAction.REMOVE);
		score.sendPacket(e.getPlayer().getPlayer());
	}


    @EventHandler
    public void onClick(InventoryClickEvent event)
    {
        if(event.getClickedInventory() == null ||
                event.getClickedInventory().getHolder() == null ||
                !(event.getClickedInventory().getHolder() instanceof LGInventoryHolder))
            return;

        ((LGInventoryHolder) event.getClickedInventory().getHolder()).onClick(event);
    }
}
