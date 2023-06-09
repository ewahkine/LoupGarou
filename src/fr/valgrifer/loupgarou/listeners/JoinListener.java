package fr.valgrifer.loupgarou.listeners;

import java.util.Collections;
import java.util.Objects;

import fr.valgrifer.loupgarou.utils.VariousUtils;
import fr.valgrifer.loupgarou.MainLg;
import org.bukkit.Bukkit;
import static fr.valgrifer.loupgarou.utils.ChatColorQuick.*;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent.Status;
import org.bukkit.potion.PotionEffectType;

import com.comphenix.protocol.wrappers.WrappedChatComponent;

import com.comphenix.packetwrapper.WrapperPlayServerScoreboardTeam;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import fr.valgrifer.loupgarou.events.LGPlayerKilledEvent.Reason;

public class JoinListener implements Listener {
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		
		WrapperPlayServerScoreboardTeam myTeam = new WrapperPlayServerScoreboardTeam();
		myTeam.setName(p.getName());
		myTeam.setPrefix(WrappedChatComponent.fromText(""));
		myTeam.setPlayers(Collections.singletonList(p.getName()));
		myTeam.setMode(0);
		boolean noSpec = p.getGameMode() != GameMode.SPECTATOR;
		for(Player player : Bukkit.getOnlinePlayers())
			if(player != p) {
				if(player.getGameMode() != GameMode.SPECTATOR)
					player.hidePlayer(MainLg.getInstance(), p);
				WrapperPlayServerScoreboardTeam team = new WrapperPlayServerScoreboardTeam();
				team.setName(player.getName());
				team.setPrefix(WrappedChatComponent.fromText(""));
				team.setPlayers(Collections.singletonList(player.getName()));
				team.setMode(0);
				
				team.sendPacket(p);
				myTeam.sendPacket(player);
			}
		p.setFoodLevel(6);

        LGPlayer lgp = LGPlayer.thePlayer(e.getPlayer());

		if(e.getJoinMessage() == null || !e.getJoinMessage().equals("joinall"))
            Objects.requireNonNull(p.getPlayer()).setResourcePack(VariousUtils.resourcePackAddress());
		else
			lgp.join(MainLg.getInstance().getCurrentGame());

        lgp.showView();

		if(noSpec)
			p.setGameMode(GameMode.ADVENTURE);

		e.setJoinMessage("");
		p.removePotionEffect(PotionEffectType.JUMP);
		p.removePotionEffect(PotionEffectType.INVISIBILITY);
		p.setWalkSpeed(0.2f);
	}
	@EventHandler
	public void onResoucePack(PlayerResourcePackStatusEvent e) {
		if(e.getStatus() == Status.SUCCESSFULLY_LOADED) {
			Player p = e.getPlayer();
			LGPlayer lgp = LGPlayer.thePlayer(p);
			lgp.showView();
			lgp.join(MainLg.getInstance().getCurrentGame());
		}else if(e.getStatus() == Status.DECLINED || e.getStatus() == Status.FAILED_DOWNLOAD)
			e.getPlayer().kickPlayer(RED+"Il vous faut le resourcepack pour jouer ! ("+e.getStatus()+")");
	}
	@EventHandler
	public void onLeave(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		LGPlayer lgp = LGPlayer.thePlayer(p);
		if(lgp.getGame() != null) {
			lgp.leaveChat();
			if(lgp.getRole() != null && !lgp.isDead())
				lgp.getGame().kill(lgp, Reason.DISCONNECTED, true);
			lgp.getGame().getInGame().remove(lgp);
			lgp.getGame().checkLeave();
		}
		LGPlayer.removePlayer(p);
		lgp.remove();
	}
	
}
