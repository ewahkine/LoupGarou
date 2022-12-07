package fr.valgrifer.loupgarou.roles;

import static org.bukkit.ChatColor.*;
import org.bukkit.event.EventHandler;

import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import fr.valgrifer.loupgarou.events.LGPlayerKilledEvent;
import fr.valgrifer.loupgarou.events.LGPlayerKilledEvent.Reason;

public class RGrandMechantLoup extends Role{

	public RGrandMechantLoup(LGGame game) {
		super(game);
	}

	public static String _getName() {
		return RED+""+BOLD+"Grand Méchant Loup";
	}

	public static String _getFriendlyName() {
		return "du "+_getName();
	}

	public static String _getShortDescription() {
		return RLoupGarou._getShortDescription();
	}

	public static String _getDescription() {
		return _getShortDescription()+WHITE+". Chaque nuit, tu te réunis avec tes compères pour décider d'une victime à éliminer... Tant qu'aucun autre "+RoleWinType.VILLAGE.getColoredName(BOLD)+WHITE+" n'est mort, tu peux, chaque nuit, dévorer une victime supplémentaire.";
	}

	public static String _getTask() {
		return "Choisis un joueur à dévorer.";
	}

	public static String _getBroadcastedTask() {
		return "Le "+RED+""+BOLD+"Grand Méchant Loup"+BLUE+" n'en a pas terminé...";
	}
	public static RoleType _getType() {
		return RoleType.LOUP_GAROU;
	}
	public static RoleWinType _getWinType() {
		return RoleWinType.LOUP_GAROU;
	}

	@Override
	public int getTimeout() {
		return 15;
	}
	
	@Override
	public boolean hasPlayersLeft() {
		return super.hasPlayersLeft() && !lgDied;
	}
	boolean lgDied;
	Runnable callback;
	@Override
	protected void onNightTurn(LGPlayer player, Runnable callback) {
		this.callback = callback;
		
		player.showView();
		player.choose(choosen -> {
            if(choosen != null && choosen != player) {
                player.sendActionBarMessage(YELLOW+""+BOLD+""+choosen.getName()+""+GOLD+" va mourir cette nuit");
                player.sendMessage(GOLD+"Tu as choisi de manger "+GRAY+""+BOLD+""+choosen.getName()+""+GOLD+".");
                getGame().kill(choosen, getGame().getDeaths().containsKey(Reason.LOUP_GAROU) ? Reason.GM_LOUP_GAROU : Reason.LOUP_GAROU);
                player.stopChoosing();
                player.hideView();
                callback.run();
            }
        });
	}
	
	@EventHandler
	public void onPlayerDie(LGPlayerKilledEvent e) {//Quand un Loup-Garou meurt, les grands méchants loups ne peuvent plus jouer.
		if(e.getGame() == getGame())
			if(e.getKilled().getRoleType() == RoleType.LOUP_GAROU)
				lgDied = true;
	}
	
	
	
	@Override
	protected void onNightTurnTimeout(LGPlayer player) {
		player.stopChoosing();
		player.hideView();
		player.sendMessage(GOLD+"Tu n'as tué personne.");
	}
	
	@Override
	public void join(LGPlayer player, boolean sendMessage) {
		super.join(player, sendMessage);
        RLoupGarou.forceJoin(player);
	}
	
}
