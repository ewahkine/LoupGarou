package fr.valgrifer.loupgarou.roles;


import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import fr.valgrifer.loupgarou.events.LGPlayerKilledEvent.Reason;
import static org.bukkit.ChatColor.*;

@SuppressWarnings("unused")
public class RChasseurDeVampire extends Role{
	public RChasseurDeVampire(LGGame game) {
		super(game);
	}
	public static RoleType _getType() {
		return RoleType.VILLAGER;
	}
	public static RoleWinType _getWinType() {
		return RoleWinType.VILLAGE;
	}
	public static String _getName() {
		return GREEN+""+BOLD+"Chasseur de Vampires";
	}
	public static String _getFriendlyName() {
		return "du "+_getName();
	}
	public static String _getShortDescription() {
		return RVillageois._getShortDescription();
	}
	public static String _getDescription() {
		return _getShortDescription()+WHITE+". Chaque nuit, tu peux traquer un joueur pour découvrir s'il s'agit d'un Vampire. Si c'est le cas, tu le tueras dans son sommeil. Si les "+RoleWinType.VAMPIRE.getColoredName(BOLD)+WHITE+" te prennent pour cible, tu seras immunisé contre leur attaque, et tu tueras le plus jeune d'entre eux.";
	}
	public static String _getTask() {
		return "Choisis un joueur à pister.";
	}
	public static String _getBroadcastedTask() {
		return "Le "+_getName()+""+BLUE+" traque ses proies...";
	}
	@Override
	public int getTimeout() {
		return 15;
	}
	@Override
	public boolean hasPlayersLeft() {
		for(LGPlayer lgp : getGame().getAlive())
			if(lgp.getRoleType() == RoleType.VAMPIRE)
				return super.hasPlayersLeft();
		return false;
	}
	
	@Override
	protected void onNightTurn(LGPlayer player, Runnable callback) {
		player.showView();
		
		player.choose(choosen -> {
            if(choosen != null && choosen != player) {
            //	player.sendMessage(GOLD+"Tu as choisi de rendre visite à "+GRAY+""+BOLD+""+choosen.getName()+""+GOLD+".");
                if(choosen.getCache().getBoolean("vampire") || choosen.getRole() instanceof RVampire) {
                    getGame().kill(choosen, Reason.CHASSEUR_DE_VAMPIRE);
                    player.sendMessage(GRAY+""+BOLD+""+choosen.getName()+""+GOLD+" est un "+DARK_PURPLE+""+BOLD+"Vampire"+GOLD+", à l'attaque.");
                    player.sendActionBarMessage(YELLOW+""+BOLD+""+choosen.getName()+""+GOLD+" va mourir");
                } else {
                    player.sendMessage(GRAY+""+BOLD+""+choosen.getName()+""+GOLD+" n'est pas un "+DARK_PURPLE+""+BOLD+"Vampire"+GOLD+"...");
                    player.sendActionBarMessage(YELLOW+""+BOLD+""+choosen.getName()+""+GOLD+" n'est pas un "+DARK_PURPLE+""+BOLD+"Vampire");
                }

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
