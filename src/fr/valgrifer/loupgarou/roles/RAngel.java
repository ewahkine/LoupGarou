package fr.valgrifer.loupgarou.roles;

import java.util.ArrayList;
import java.util.List;

import static org.bukkit.ChatColor.*;
import org.bukkit.event.EventHandler;

import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import fr.valgrifer.loupgarou.classes.LGWinType;
import fr.valgrifer.loupgarou.events.LGDayEndEvent;
import fr.valgrifer.loupgarou.events.LGEndCheckEvent;
import fr.valgrifer.loupgarou.events.LGGameEndEvent;
import fr.valgrifer.loupgarou.events.LGPlayerGotKilledEvent;
import fr.valgrifer.loupgarou.events.LGVoteEvent;
import fr.valgrifer.loupgarou.events.LGPlayerKilledEvent.Reason;

@SuppressWarnings("unused")
public class RAngel extends Role {
	public RAngel(LGGame game) {
		super(game);
	}
	public static RoleType _getType() {
		return RoleType.NEUTRAL;
	}
    public static RoleWinType _getWinType() {
		return RoleWinType.VILLAGE;
	}
	public static String _getName() {
		return LIGHT_PURPLE+""+BOLD+"Ange";
	}
	public static String _getFriendlyName() {
		return "de l'"+_getName();
	}
	public static String _getShortDescription() {
		return WHITE+"Tu gagnes si tu remplis ton objectif";
	}
	public static String _getDescription() {
		return WHITE+"Tu es "+RoleType.NEUTRAL.getColoredName(LIGHT_PURPLE, BOLD)+WHITE+" et tu gagnes si tu remplis ton objectif. Ton objectif est d'être éliminé par le village lors du premier vote de jour. Si tu réussis, tu gagnes la partie. Sinon, tu deviens un "+RoleType.VILLAGER.getColoredName(BOLD)+WHITE+".";
	}
	public static String _getTask() {
		return "";
	}
	public static String _getBroadcastedTask() {
		return "";
	}
	@EventHandler
	public void onVoteStart(LGVoteEvent e) {
		if(e.getGame() == getGame()) {
			night = getGame().getNight();
			vote = true;
			for(LGPlayer lgp : getPlayers())
				if(!lgp.isDead() && lgp.isRoleActive())
					lgp.sendMessage(BLUE+""+ITALIC+"Fais en sorte que les autres votent contre toi !");
		}
	}
	boolean vote;
	@EventHandler
	public void onDayEnd(LGDayEndEvent e) {
		if(e.getGame() == getGame()) {
			if(getPlayers().size() > 0 && getGame().getNight() == night+1 && vote) {
				Role villageois = getGame().getRole(RVillager.class);
				
				if(villageois == null)
					getGame().getRoles().add(villageois = new RVillager(getGame()));
				
				for(LGPlayer lgp : getPlayers()) {
					if(lgp.isRoleActive())
						lgp.sendMessage(DARK_RED+""+ITALIC+"Tu as échoué, tu deviens "+GREEN+""+BOLD+""+ITALIC+"Villageois"+DARK_RED+""+ITALIC+"...");
					lgp.setRole(villageois);
					villageois.join(lgp);
				}
				
				getPlayers().clear();
				getGame().updateRoleScoreboard();
			}
			vote = false;
		}
	}
	List<LGPlayer> winners = new ArrayList<>();
	int night = 1;
	@EventHandler
	public void onDeath(LGPlayerGotKilledEvent e) {
		if(e.getGame() == getGame())
			if(e.getReason() == Reason.VOTE && e.getKilled().getRole() == this && getGame().getNight() == night && e.getKilled().isRoleActive())
				winners.add(e.getKilled());
	}
	
	@EventHandler
	public void onWinCheck(LGEndCheckEvent e) {
		if(e.getGame() == getGame())
			if(winners.size() > 0)
				e.setWinType(winners.size() == 1 && winners.get(0).getCache().has("inlove") ? LGWinType.COUPLE : LGWinType.ANGE);
	}
	
	@EventHandler
	public void onWin(LGGameEndEvent e) {
		if(e.getGame() == getGame())
			if(e.getWinType() == LGWinType.ANGE)
				e.getWinners().addAll(winners);
	}
}
