package fr.valgrifer.loupgarou.roles;

import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import static org.bukkit.ChatColor.*;

public class RDetective extends Role{
	public RDetective(LGGame game) {
		super(game);
	}
	public static RoleType _getType() {
		return RoleType.VILLAGER;
	}
	public static RoleWinType _getWinType() {
		return RoleWinType.VILLAGE;
	}
	public static String _getName() {
		return GREEN+""+BOLD+"Détective";
	}
	public static String _getFriendlyName() {
		return "du "+_getName();
	}
	public static String _getShortDescription() {
		return RVillageois._getShortDescription();
	}
	public static String _getDescription() {
		return _getShortDescription()+WHITE+". Chaque nuit, tu mènes l'enquête sur deux joueurs pour découvrir s'ils font partie du même camp.";
	}
	public static String _getTask() {
		return "Choisis deux joueurs à étudier.";
	}
	public static String _getBroadcastedTask() {
		return "Le "+_getName()+""+BLUE+" est sur une enquête...";
	}
	
	@Override
	public int getTimeout() {
		return 15;
	}
	@Override
	protected void onNightTurn(LGPlayer player, Runnable callback) {
		player.showView();
		
		player.choose(choosen -> {
            if(choosen != null) {
                if(choosen == player) {
                    player.sendMessage(RED+"Vous ne pouvez pas vous sélectionner !");
                    return;
                }
                if(player.getCache().has("detective_first")) {
                    LGPlayer first = player.getCache().remove("detective_first");
                    if(first == choosen) {
                        player.sendMessage(RED+"Vous ne pouvez pas comparer "+GRAY+""+BOLD+""+first.getName()+""+RED+" avec lui même !");
                    } else {
                        if((first.getRoleType() == RoleType.NEUTRAL || choosen.getRoleType() == RoleType.NEUTRAL) ? first.getRole().getClass() == choosen.getRole().getClass() : first.getRoleType() == choosen.getRoleType())
                            player.sendMessage(GRAY+""+BOLD+""+first.getName()+""+GOLD+" et "+GRAY+""+BOLD+""+choosen.getName()+""+GOLD+" sont "+GREEN+"du même camp.");
                        else
                            player.sendMessage(GRAY+""+BOLD+""+first.getName()+""+GOLD+" et "+GRAY+""+BOLD+""+choosen.getName()+""+GOLD+" ne sont "+RED+"pas du même camp.");

                        player.stopChoosing();
                        player.hideView();
                        callback.run();
                    }
                } else {
                    player.getCache().set("detective_first", choosen);
                    player.sendMessage(BLUE+"Choisis un joueur avec qui tu souhaites comparer le rôle de "+GRAY+""+BOLD+""+choosen.getName());
                }
            }
        });
	}

	@Override
	protected void onNightTurnTimeout(LGPlayer player) {
		player.getCache().remove("detective_first");
		player.stopChoosing();
		player.hideView();
		//player.sendTitle(RED+"Vous n'avez mis personne en couple", DARK_RED+"Vous avez mis trop de temps à vous décider...", 80);
		//player.sendMessage(BLUE+"Tu n'as pas créé de couple.");
	}
	
	
}
