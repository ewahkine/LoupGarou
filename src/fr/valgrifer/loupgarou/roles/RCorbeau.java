package fr.valgrifer.loupgarou.roles;

import static org.bukkit.ChatColor.*;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitRunnable;

import fr.valgrifer.loupgarou.MainLg;
import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import fr.valgrifer.loupgarou.events.LGDayEndEvent;
import fr.valgrifer.loupgarou.events.LGVoteEvent;

@SuppressWarnings("unused")
public class RCorbeau extends Role{
	public RCorbeau(LGGame game) {
		super(game);
	}
	public static RoleType _getType() {
		return RoleType.VILLAGER;
	}
	public static RoleWinType _getWinType() {
		return RoleWinType.VILLAGE;
	}
	public static String _getName() {
		return GREEN+""+BOLD+"Corbeau";
	}
	public static String _getFriendlyName() {
		return "du "+_getName();
	}
	public static String _getShortDescription() {
		return RVillageois._getShortDescription();
	}
	public static String _getDescription() {
		return _getShortDescription()+". Chaque nuit, tu peux désigner un joueur qui se retrouvera le lendemain avec deux voix contre lui au vote.";
	}
	public static String _getTask() {
		return "Tu peux choisir un joueur qui aura deux votes contre lui.";
	}
	public static String _getBroadcastedTask() {
		return "Le "+_getName()+""+BLUE+" s'apprête à diffamer quelqu'un...";
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
                //player.sendTitle(GOLD+"Vous avez regardé un rôle", YELLOW+""+BOLD+""+choosen.getName()+""+GOLD+""+BOLD+" est "+YELLOW+""+BOLD+""+choosen.getRole().getName(), 5*20);

                choosen.getCache().set("corbeau_selected", true);

                player.sendActionBarMessage(YELLOW+""+BOLD+""+choosen.getName()+""+GOLD+" aura deux votes contre lui");
                player.sendMessage(GOLD+"Tu nuis à la réputation de "+GRAY+""+BOLD+""+choosen.getName()+""+GOLD+".");
                player.stopChoosing();
                player.hideView();
                callback.run();
            }
        });
	}
	
	@EventHandler
	public void onNightStart(LGDayEndEvent e) {
		if(e.getGame() == getGame())
			for(LGPlayer lgp : getGame().getAlive())
				lgp.getCache().remove("corbeau_selected");
	}
	
	@EventHandler
	public void onVoteStart(LGVoteEvent e) {
		if(e.getGame() == getGame())
			for(LGPlayer lgp : getGame().getAlive())
				if(lgp.getCache().getBoolean("corbeau_selected")) {
					lgp.getCache().remove("corbeau_selected");
                    new BukkitRunnable() {
						
						@Override
						public void run() {
							getGame().getVote().vote(new LGPlayer(GREEN+""+BOLD+"Le corbeau"), lgp);
							getGame().getVote().vote(new LGPlayer(GREEN+""+BOLD+"Le corbeau"), lgp);//fix
							getGame().broadcastMessage(GRAY+""+BOLD+""+ lgp.getName()+""+GOLD+" a reçu la visite du "+getName()+""+GOLD+".", true);
						}
					}.runTask(MainLg.getInstance());
					
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
