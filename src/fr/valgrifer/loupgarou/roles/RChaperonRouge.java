package fr.valgrifer.loupgarou.roles;

import static org.bukkit.ChatColor.*;

import fr.valgrifer.loupgarou.events.LGNightEndEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import fr.valgrifer.loupgarou.events.LGNightPlayerPreKilledEvent;
import fr.valgrifer.loupgarou.events.LGPlayerKilledEvent.Reason;
import fr.valgrifer.loupgarou.events.LGRoleTurnEndEvent;

@SuppressWarnings("unused")
public class RChaperonRouge extends Role{
	public RChaperonRouge(LGGame game) {
		super(game);
	}
	public static RoleType _getType() {
		return RoleType.VILLAGER;
	}
	public static RoleWinType _getWinType() {
		return RoleWinType.VILLAGE;
	}
	public static String _getName() {
		return GREEN+""+BOLD+"Chaperon Rouge";
	}
	public static String _getFriendlyName() {
		return "du "+_getName();
	}
	public static String _getShortDescription() {
		return RVillageois._getShortDescription();
	}
	public static String _getDescription() {
		return _getShortDescription()+WHITE+". Tant que le "+GREEN+""+BOLD+"Chasseur"+WHITE+" est en vie, tu ne peux pas te faire tuer par les "+RoleWinType.LOUP_GAROU.getColoredName(BOLD)+WHITE+" pendant la nuit.";
	}
	public static String _getTask() {
		return "";
	}
	public static String _getBroadcastedTask() {
		return "";
	}
	@Override
	public int getTimeout() {
		return -1;
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onKill(LGNightPlayerPreKilledEvent e) {
		if(e.getKilled().getRole() == this && e.getReason() == Reason.LOUP_GAROU || e.getReason() == Reason.GM_LOUP_GAROU && e.getKilled().isRoleActive()) {
			for(Role role : getGame().getRoles())
				if(role instanceof RChasseur)
					if(role.getPlayers().size() > 0){
						e.getKilled().getCache().set("chaperon_kill", true);
						e.setReason(Reason.DONT_DIE);
						break;
					}
		}
	}
	@EventHandler
	public void onTour(LGRoleTurnEndEvent e) {
		if(e.getGame() == getGame()) {
			if(e.getPreviousRole() instanceof RLoupGarou) {
				for(LGPlayer lgp : getGame().getAlive())
					if(lgp.getCache().getBoolean("chaperon_kill") && lgp.isRoleActive()) {
						for(LGPlayer l : getGame().getInGame())
							if(l.getRoleType() == RoleType.LOUP_GAROU)
								l.sendMessage(RED+"Votre cible est immunisée.");
					}
			}else if(e.getPreviousRole() instanceof RGrandMechantLoup) {
				for(LGPlayer lgp : getGame().getAlive())
					if(lgp.getCache().getBoolean("chaperon_kill") && lgp.isRoleActive()) {
						for(LGPlayer l : e.getPreviousRole().getPlayers())
							l.sendMessage(RED+"Votre cible est immunisée.");
					}
			}
		}
	}
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onDayStart(LGNightEndEvent e) {
		if(e.getGame() == getGame()) {
			for(LGPlayer lgp : getPlayers())
				if(lgp.getCache().getBoolean("chaperon_kill")) {
					lgp.getCache().remove("chaperon_kill");
					lgp.sendMessage(BLUE+""+ITALIC+"Tu as été attaqué cette nuit.");
				}
		}
	}
}
