package fr.valgrifer.loupgarou.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import fr.valgrifer.loupgarou.classes.LGPlayer;

public class ChatListener implements Listener{
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onChat(AsyncPlayerChatEvent e) {
		if(!e.isCancelled()) {
			LGPlayer player = LGPlayer.thePlayer(e.getPlayer());
			player.onChat(e.getMessage());
			e.setCancelled(true);
		}
	}
}
