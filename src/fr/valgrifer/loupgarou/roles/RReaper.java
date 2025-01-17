package fr.valgrifer.loupgarou.roles;

import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import static fr.valgrifer.loupgarou.utils.ChatColorQuick.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import fr.valgrifer.loupgarou.MainLg;
import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import fr.valgrifer.loupgarou.events.LGPlayerKilledEvent;
import fr.valgrifer.loupgarou.events.LGPlayerKilledEvent.Reason;

public class RReaper extends Role{
	public RReaper(LGGame game) {
		super(game);
	}
	public static RoleType _getType() {
		return RoleType.VILLAGER;
	}
	public static RoleWinType _getWinType() {
		return RoleWinType.VILLAGE;
	}
	public static String _getName() {
		return GREEN+BOLD+"Faucheur";
	}
	public static String _getFriendlyName() {
		return "du "+_getName();
	}
	public static String _getShortDescription() {
		return RVillager._getShortDescription();
	}
	public static String _getDescription() {
		return _getShortDescription()+WHITE+". Si les "+RoleWinType.LOUP_GAROU.getColoredName(BOLD)+WHITE+" te tuent pendant la nuit, tu emporteras l’un d’entre eux dans ta mort, mais si tu meurs lors du vote du "+RoleWinType.VILLAGE.getColoredName(BOLD)+WHITE+", ce sont tes deux voisins qui en paieront le prix.";
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
	
	private static final Random random = new Random();

	@EventHandler(priority = EventPriority.LOWEST)
	public void onKill(LGPlayerKilledEvent e) {
		if (e.getKilled().getRole() == this && e.getKilled().isRoleActive()) {
			LGPlayer killed = e.getKilled();
			if(killed.getCache().getBoolean("faucheur_did"))//A déjà fait son coup de faucheur !
				return;
			killed.getCache().set("faucheur_did", true);
			if (e.getReason() == Reason.LOUP_GAROU || e.getReason() == Reason.GM_LOUP_GAROU) {//car le switch buggait (wtf)
				// Mort par les LG
				// Tue un lg au hasard
				LGPlayer selected = null;
                RWereWolf role;
                if ((role = getGame().getRole(RWereWolf.class)) != null)
                    selected = role.getPlayers().get(random.nextInt(role.getPlayers().size()));

				if (selected != null) {
					LGPlayerKilledEvent killEvent = new LGPlayerKilledEvent(getGame(), selected, Reason.FAUCHEUR);
					Bukkit.getPluginManager().callEvent(killEvent);
					if (killEvent.isCancelled())
						return;
					getGame().kill(killEvent.getKilled(), killEvent.getReason(), false);
				}
			} else if (e.getReason() == Reason.VOTE) {
				List<?> original = MainLg.getInstance().getConfig().getList("spawns");
                assert original != null;
                int size = original.size();
				// double middle = ((double)size)/2D;
				int killedPlace = killed.getPlace();

				LGPlayer droite = null, gauche = null;
				for (int i = killedPlace + 1;; i++) {
					if (i == size)
						i = 0;
					LGPlayer lgp = getGame().getPlacements().get(i);
					if (lgp != null && !lgp.isDead()) {
						droite = lgp;
						break;
					}
					if (lgp == killed)// Fait un tour complet
						break;
				}
				for (int i = killedPlace - 1;; i--) {
					if (i == -1)
						i = size - 1;
					LGPlayer lgp = getGame().getPlacements().get(i);
					if (lgp != null && !lgp.isDead()) {
						gauche = lgp;
						break;
					}
					if (lgp == killed)// Fait un tour complet
						break;
				}
				if (droite != null) {
					LGPlayerKilledEvent killEvent = new LGPlayerKilledEvent(getGame(), droite, Reason.FAUCHEUR);
					Bukkit.getPluginManager().callEvent(killEvent);
					if (!killEvent.isCancelled())
						getGame().kill(killEvent.getKilled(), killEvent.getReason(), false);
				}
				if (gauche != null) {
					LGPlayerKilledEvent killEvent = new LGPlayerKilledEvent(getGame(), gauche, Reason.FAUCHEUR);
					Bukkit.getPluginManager().callEvent(killEvent);
					if (!killEvent.isCancelled())
						getGame().kill(killEvent.getKilled(), killEvent.getReason(), false);
				}
			}
		}
	}
}
