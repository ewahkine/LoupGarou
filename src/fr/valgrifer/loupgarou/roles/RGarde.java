package fr.valgrifer.loupgarou.roles;

import java.util.Arrays;
import java.util.List;

import static org.bukkit.ChatColor.*;
import org.bukkit.event.EventHandler;

import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import fr.valgrifer.loupgarou.events.LGNightPlayerPreKilledEvent;
import fr.valgrifer.loupgarou.events.LGPlayerKilledEvent.Reason;
import fr.valgrifer.loupgarou.events.LGPreDayStartEvent;
import fr.valgrifer.loupgarou.events.LGVampiredEvent;

public class RGarde extends Role{
	public RGarde(LGGame game) {
		super(game);
	}
	public static RoleType _getType() {
		return RoleType.VILLAGER;
	}
	public static RoleWinType _getWinType() {
		return RoleWinType.VILLAGE;
	}
	public static String _getName() {
		return GREEN+""+BOLD+"Garde";
	}
	public static String _getFriendlyName() {
		return "du "+_getName();
	}
	public static String _getShortDescription() {
		return RVillageois._getShortDescription();
	}
	public static String _getDescription() {
		return _getShortDescription()+WHITE+". Chaque nuit, tu peux te protéger toi ou quelqu'un d'autre des attaques "+RED+""+BOLD+"hostiles"+WHITE+". Tu ne peux pas protéger deux fois d’affilé la même personne.";
	}
	public static String _getTask() {
		return "Choisis un joueur à protéger.";
	}
	public static String _getBroadcastedTask() {
		return "Le "+_getName()+""+BLUE+" choisit un joueur à protéger.";
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
                LGPlayer lastProtected = player.getCache().get("garde_lastProtected");
                if(choosen == lastProtected) {
                    if(lastProtected == player)
                        player.sendMessage(DARK_RED+""+ITALIC+"Tu t'es déjà protégé la nuit dernière.");
                    else
                        player.sendMessage(DARK_RED+""+ITALIC+"Tu as déjà protégé "+GRAY+""+BOLD+""+ITALIC+""+lastProtected.getName()+""+DARK_RED+""+ITALIC+" la nuit dernière.");
                }  else {
                    if(choosen == player) {
                        player.sendMessage(GOLD+"Tu décides de te protéger toi-même cette nuit.");
                        player.sendActionBarMessage(BLUE+"Tu seras protégé.");
                    } else {
                        player.sendMessage(GOLD+"Tu vas protéger "+GRAY+""+BOLD+""+choosen.getName()+""+GOLD+" cette nuit.");
                        player.sendActionBarMessage(GRAY+""+BOLD+""+choosen.getName()+""+BLUE+" sera protégé.");
                    }
                    choosen.getCache().set("garde_protected", true);
                    player.getCache().set("garde_lastProtected", choosen);
                    player.stopChoosing();
                    player.hideView();
                    callback.run();
                }
            }
        });
	}
	@Override
	protected void onNightTurnTimeout(LGPlayer player) {
		player.getCache().remove("garde_lastProtected");
		player.stopChoosing();
		player.hideView();
		//player.sendTitle(RED+"Vous n'avez protégé personne.", DARK_RED+"Vous avez mis trop de temps à vous décider...", 80);
		//player.sendMessage(RED+"Vous n'avez protégé personne cette nuit.");
	}
	
	private static final List<Reason> reasonsProtected = Arrays.asList(Reason.LOUP_GAROU, Reason.LOUP_BLANC, Reason.GM_LOUP_GAROU, Reason.ASSASSIN);
	
	@EventHandler
	public void onPlayerKill(LGNightPlayerPreKilledEvent e) {
		if(e.getGame() == getGame() && reasonsProtected.contains(e.getReason()) && e.getKilled().getCache().getBoolean("garde_protected")) {
			e.getKilled().getCache().remove("garde_protected");
			e.setReason(Reason.DONT_DIE);
		}
	}
	@EventHandler
	public void onVampired(LGVampiredEvent e) {
		if(e.getGame() == getGame() && e.getPlayer().getCache().getBoolean("garde_protected"))
			e.setProtect(true);
	}
	@EventHandler
	public void onDayStart(LGPreDayStartEvent e) {
		if(e.getGame() == getGame())
			for(LGPlayer lgp : getGame().getInGame())
				lgp.getCache().remove("garde_protected");
	}
}
