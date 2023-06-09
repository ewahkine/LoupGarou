package fr.valgrifer.loupgarou.roles;

import static fr.valgrifer.loupgarou.utils.ChatColorQuick.*;
import org.bukkit.event.EventHandler;

import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import fr.valgrifer.loupgarou.classes.chat.LGChat;
import fr.valgrifer.loupgarou.events.LGDayEndEvent;
import fr.valgrifer.loupgarou.events.LGPreDayStartEvent;
import fr.valgrifer.loupgarou.events.LGRoleTurnEndEvent;

public class RMedium extends Role{
	public RMedium(LGGame game) {
		super(game);
	}
	public static RoleType _getType() {
		return RoleType.VILLAGER;
	}
	public static RoleWinType _getWinType() {
		return RoleWinType.VILLAGE;
	}
	public static String _getName() {
		return GREEN+BOLD+"Médium";
	}
	public static String _getFriendlyName() {
		return "du "+_getName();
	}
	public static String _getShortDescription() {
		return RVillager._getShortDescription();
	}
	public static String _getDescription() {
		return _getShortDescription()+WHITE+". Chaque nuit, tu peux communiquer avec les morts pour tenter de récupérer des informations cruciales.";
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
	
	@EventHandler
	public void onNight(LGDayEndEvent e) {
		if(e.getGame() == getGame())
			for(LGPlayer lgp : getPlayers()) {
				lgp.sendMessage(DARK_GRAY+ITALIC+"Tu entres en contact avec le monde des morts...");
				joinChat(lgp);
			}
	}
	
	
	private void joinChat(LGPlayer lgp) {
		lgp.joinChat(getGame().getSpectatorChat(), new LGChat.LGChatCallback() {

			@Override
			public String receive(LGPlayer sender, String message) {
				return GRAY+sender.getName()+GOLD+" » "+WHITE+message;
			}
			
			@Override
			public String send(LGPlayer sender, String message) {
				return getName()+GOLD+" » "+WHITE+message;
			}
			
		});
	}
	@EventHandler
	public void onRoleTurn(LGRoleTurnEndEvent e) {
		if(e.getGame() == getGame())
			if(e.getPreviousRole() instanceof RWereWolf)
				for(LGPlayer lgp : getPlayers())
					if(lgp.getChat() != getGame().getSpectatorChat() && lgp.isRoleActive()) {
						lgp.sendMessage(GOLD+ITALIC+"Tu peux de nouveau parler aux morts...");
						joinChat(lgp);
					}
	}
	
	@EventHandler
	public void onDay(LGPreDayStartEvent e) {
		if(e.getGame() == getGame())
			for(LGPlayer lgp : getPlayers())
				if(lgp.isRoleActive())
					lgp.leaveChat();
	}
}
