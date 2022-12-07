package fr.valgrifer.loupgarou.roles;

import java.util.Arrays;
import java.util.List;

import static org.bukkit.ChatColor.*;
import org.bukkit.event.EventHandler;

import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import fr.valgrifer.loupgarou.events.LGRoleTurnEndEvent;

public class RPetiteFille extends Role{
	public RPetiteFille(LGGame game) {
		super(game);
	}
	public static RoleType _getType() {
		return RoleType.VILLAGER;
	}
	public static RoleWinType _getWinType() {
		return RoleWinType.VILLAGE;
	}
	public static String _getName() {
		return GREEN+""+BOLD+"Petite Fille";
	}
	public static String _getFriendlyName() {
		return "de la "+_getName();
	}
	public static String _getShortDescription() {
		return RVillageois._getShortDescription();
	}
	public static String _getDescription() {
		return _getShortDescription()+WHITE+". Chaque nuit, tu peux espionner les "+RED+""+BOLD+"Loups"+WHITE+".";
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
	
	List<String> customNames = Arrays.asList("Loup Glouton", "Loup Méchant", "Loup Burlesque", "Loup Peureux", "Loup Malingre", "Loup Gentil", "Loup Tueur", "Loup Énervé", "Loup Docteur");
	
	@EventHandler
	public void onChangeRole(LGRoleTurnEndEvent e) {
		if(e.getGame() == getGame()) {
			if(e.getNewRole() instanceof RLoupGarou)
				for(Role role : getGame().getRoles())
					if(role instanceof RLoupGarou) {
						RLoupGarou lgRole = (RLoupGarou)role;
						for(LGPlayer player : getPlayers())
							if(!player.getCache().getBoolean("infected") && player.isRoleActive())
								player.joinChat(lgRole.getChat(), (sender, message)-> RED+""+customNames.get(lgRole.getPlayers().indexOf(sender))+" "+GOLD+"» "+WHITE+""+message, true);
						break;
					}
			if(e.getPreviousRole() instanceof RLoupGarou)
				for(LGPlayer player : getPlayers())
					if(!player.getCache().getBoolean("infected") && player.isRoleActive())
						player.leaveChat();
		}
	}
}
