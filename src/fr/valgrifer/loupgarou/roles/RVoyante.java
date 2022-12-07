package fr.valgrifer.loupgarou.roles;

import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import static org.bukkit.ChatColor.*;

public class RVoyante extends Role{
	public RVoyante(LGGame game) {
		super(game);
	}
	public static RoleType _getType() {
		return RoleType.VILLAGER;
	}
	public static RoleWinType _getWinType() {
		return RoleWinType.VILLAGE;
	}
	public static String _getName() {
		return GREEN+""+BOLD+"Voyante";
	}
	public static String _getFriendlyName() {
		return "de la "+_getName();
	}
	public static String _getShortDescription() {
		return RVillageois._getShortDescription();
	}
	
	public static String _getDescription() {
		return _getShortDescription()+WHITE+". Chaque nuit, tu peux espionner un joueur et découvrir sa véritable identité...";
	}
	public static String _getTask() {
		return "Choisis un joueur dont tu veux connnaître l'identité.";
	}
	public static String _getBroadcastedTask() {
		return "La "+_getName()+""+BLUE+" s'apprête à sonder un joueur...";
	}
	@Override
	public int getTimeout() {
		return 15;
	}
	
	@Override
	protected void onNightTurn(LGPlayer player, Runnable callback) {
		player.showView();
		
		player.choose(choosen -> {
            if(choosen != null && choosen != player) {
                player.sendActionBarMessage(YELLOW+""+BOLD+""+choosen.getName()+""+GOLD+" est "+YELLOW+""+BOLD+""+choosen.getRole().getScoreBoardName());
                player.sendMessage(GOLD+"Tu découvres que "+GRAY+""+BOLD+""+choosen.getName()+""+GOLD+" est "+choosen.getRole().getPublicName(choosen)+""+GOLD+".");
                player.stopChoosing();
                player.hideView();
                callback.run();
            }
        });
	}
	@Override
	protected void onNightTurnTimeout(LGPlayer player) {
		player.stopChoosing();
		player.hideView();
		//player.sendTitle(RED+"Vous n'avez regardé aucun rôle", DARK_RED+"Vous avez mis trop de temps à vous décider...", 80);
		//player.sendMessage(RED+"Vous n'avez pas utilisé votre pouvoir cette nuit.");
	}
}
