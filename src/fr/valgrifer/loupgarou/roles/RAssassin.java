package fr.valgrifer.loupgarou.roles;

import static org.bukkit.ChatColor.*;

import fr.valgrifer.loupgarou.events.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import fr.valgrifer.loupgarou.classes.LGWinType;
import fr.valgrifer.loupgarou.events.LGPlayerKilledEvent.Reason;

@SuppressWarnings("unused")
public class RAssassin extends Role{
	public RAssassin(LGGame game) {
		super(game);
	}
	public static RoleType _getType() {
		return RoleType.NEUTRAL;
	}
	public static RoleWinType _getWinType() {
		return RoleWinType.SOLO;
	}
	public static String _getName() {
		return DARK_BLUE+""+BOLD+"Assassin";
	}
	public static String _getFriendlyName() {
		return "de l'"+_getName();
	}
	public static String _getShortDescription() {
		return WHITE+"Tu gagnes "+RoleWinType.SOLO.getColoredName(BOLD);
	}
	public static String _getDescription() {
		return _getShortDescription()+WHITE+". Chaque nuit, tu peux choisir un joueur à éliminer. Tu es immunisé contre l'attaque des "+RoleWinType.LOUP_GAROU.getColoredName(BOLD)+WHITE+".";
	}
	public static String _getTask() {
		return "Choisis un joueur à éliminer.";
	}
	public static String _getBroadcastedTask() {
		return "L'"+_getName()+""+BLUE+" ne controle plus ses pulsions...";
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
                getGame().kill(choosen, Reason.ASSASSIN);
                player.sendActionBarMessage(YELLOW+""+BOLD+""+choosen.getName()+""+GOLD+" va mourir");
                player.sendMessage(GOLD+"Tu as choisi de tuer "+GRAY+""+BOLD+""+choosen.getName()+""+GOLD+".");
                player.stopChoosing();
                player.hideView();
                callback.run();
            }
        });
	}
	
	@EventHandler
	public void onKill(LGNightPlayerPreKilledEvent e) {
		if(e.getKilled().getRole() != this || (e.getReason() != Reason.LOUP_GAROU && e.getReason() != Reason.GM_LOUP_GAROU) || !e.getKilled().isRoleActive())
            return;

        e.setCancelled(true);

        if(e.getReason() == Reason.LOUP_GAROU)
        {
            RLoupGarou lgs;
            if((lgs = getGame().getRole(RLoupGarou.class)) != null)
                for(LGPlayer lg : lgs.getPlayers())
                    lg.sendMessage(RED+"Votre cible est immunisée.");
        }
        else if(e.getReason() == Reason.GM_LOUP_GAROU)
        {
            RGrandMechantLoup lgs;
            if((lgs = getGame().getRole(RGrandMechantLoup.class)) != null)
                for(LGPlayer lg : lgs.getPlayers())
                    lg.sendMessage(RED+"Votre cible est immunisée.");
        }
	}

	@EventHandler
	public void onPyroGasoil(LGRoleActionEvent e) {
        if(e.getGame() != getGame())
            return;
		if(e.getAction() instanceof RPyromane.GasoilAction)
        {
            RPyromane.GasoilAction action = (RPyromane.GasoilAction) e.getAction();
            if(action.getTarget().getRole() == this && action.getTarget().isRoleActive())
                action.setCancelled(true);
        }
		else if(e.getAction() instanceof RVampire.VampiredAction)
        {
            RVampire.VampiredAction action = (RVampire.VampiredAction) e.getAction();
            if(action.getTarget().getRole() == this && action.getTarget().isRoleActive())
                action.setImmuned(true);
        }
	}
	
	@EventHandler
	public void onDayStart(LGNightEndEvent e) {
		if(e.getGame() == getGame()) {
			for(LGPlayer lgp : getGame().getAlive())
				if(lgp.getCache().getBoolean("assassin_protected"))
					lgp.getCache().remove("assassin_protected");
		}
	}
	
	@EventHandler
	public void onEndgameCheck(LGEndCheckEvent e) {
		if(e.getGame() == getGame() && e.getWinType() == LGWinType.SOLO) {
			if(getPlayers().size() > 0) {
				if(getPlayers().size() > 1)
					for(LGPlayer lgp : getPlayers())
						if(!lgp.isRoleActive())
							return;
				e.setWinType(LGWinType.ASSASSIN);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEndGame(LGGameEndEvent e) {
		if(e.getWinType() == LGWinType.ASSASSIN) {
			e.getWinners().clear();
			e.getWinners().addAll(getPlayers());
		}
	}
	
	@Override
	protected void onNightTurnTimeout(LGPlayer player) {
		player.stopChoosing();
		player.hideView();
		//player.sendTitle(RED+"Vous n'avez regardé aucun rôle", DARK_RED+"Vous avez mis trop de temps à vous décider...", 80);
		//player.sendMessage(RED+"Vous n'avez pas utilisé votre pouvoir cette nuit.");
	}
}
