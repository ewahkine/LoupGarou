package fr.valgrifer.loupgarou.roles;

import java.util.List;

import static fr.valgrifer.loupgarou.utils.ChatColorQuick.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import fr.valgrifer.loupgarou.MainLg;
import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import fr.valgrifer.loupgarou.events.LGDayStartEvent;

public class RBearShowman extends Role{
	public RBearShowman(LGGame game) {
		super(game);
	}
	public static RoleType _getType() {
		return RoleType.VILLAGER;
	}
	public static RoleWinType _getWinType() {
		return RoleWinType.VILLAGE;
	}
	public static String _getName() {
		return GREEN+BOLD+"Montreur d'Ours";
	}
	public static String _getFriendlyName() {
		return "du "+_getName();
	}
	public static String _getShortDescription() {
		return RVillager._getShortDescription();
	}
	public static String _getDescription() {
		return _getShortDescription()+WHITE+". Chaque matin, ton Ours va renifler tes voisins et grognera si l'un d'eux est hostile aux Villageois.";
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

	private int lastNight = -1;

	@EventHandler(priority = EventPriority.LOWEST)
	public void onDay(LGDayStartEvent e) {
		if (e.getGame() == getGame() && getPlayers().size() > 0) {
			if(lastNight == getGame().getNight())
				return;
			lastNight = getGame().getNight();
			List<?> original = MainLg.getInstance().getConfig().getList("spawns");
			for(LGPlayer target : getPlayers()) {
				if(!target.isRoleActive())
					continue;
                assert original != null;
                int size = original.size();
				int killedPlace = target.getPlace();

				for (int i = killedPlace + 1;; i++) {
					if (i == size)
						i = 0;
					LGPlayer lgp = getGame().getPlacements().get(i);
					if (lgp != null && !lgp.isDead()) {
						if(lgp.getRoleWinType() == RoleWinType.VILLAGE || lgp.getRoleWinType() == RoleWinType.NONE)
							break;
						else{
							getGame().broadcastMessage(GOLD+"La bête du "+getName()+GOLD+" grogne...", true);
							return;
						}
					}
					if (lgp == target)// Fait un tour complet
						break;
				}
				for (int i = killedPlace - 1;; i--) {
					if (i == -1)
						i = size - 1;
					LGPlayer lgp = getGame().getPlacements().get(i);
					if (lgp != null && !lgp.isDead()) {
						if(lgp.getRoleWinType() == RoleWinType.VILLAGE || lgp.getRoleWinType() == RoleWinType.NONE)
							break;
						else{
							getGame().broadcastMessage(GOLD+"La bête du "+getName()+GOLD+" grogne...", true);
							return;
						}
					}
					if (lgp == target)// Fait un tour complet
						break;
				}
			}
		}
	}
}
