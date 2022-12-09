package fr.valgrifer.loupgarou.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;

import fr.valgrifer.loupgarou.classes.LGPlayer;

public class VoteListener implements Listener{
	@EventHandler
	public void onClick(PlayerAnimationEvent e) {
		if(e.getAnimationType() == PlayerAnimationType.ARM_SWING)
			LGPlayer.thePlayer(e.getPlayer()).chooseAction();
	}
}