package fr.valgrifer.loupgarou.roles;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import fr.valgrifer.loupgarou.classes.*;
import fr.valgrifer.loupgarou.events.*;
import lombok.Setter;
import org.bukkit.Bukkit;
import static org.bukkit.ChatColor.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import fr.valgrifer.loupgarou.classes.LGCustomItems.LGCustomItemsConstraints;
import fr.valgrifer.loupgarou.classes.chat.LGChat;
import fr.valgrifer.loupgarou.events.LGPlayerKilledEvent.Reason;
import lombok.Getter;

public class RVampire extends Role{

	public RVampire(LGGame game) {
		super(game);
	}

	public static String _getName() {
		return DARK_PURPLE+""+BOLD+"Vampire";
	}

	public static String _getFriendlyName() {
		return "des "+ DARK_PURPLE+""+BOLD+"Vampires";
	}

	public static String _getShortDescription() {
		return WHITE+"Tu gagnes avec les "+RoleWinType.VAMPIRE.getColoredName(BOLD);
	}

	public static String _getDescription() {
		return _getShortDescription()+WHITE+". Chaque nuit, tu te réunis avec tes compères pour décider d'une victime à transformer en "+RoleWinType.VAMPIRE.getColoredName(BOLD)+WHITE+"... Lorsqu'une transformation a lieu, tous les "+RoleWinType.VAMPIRE.getColoredName(BOLD)+WHITE+" doivent se reposer la nuit suivante. Un joueur transformé perd tous les pouvoirs liés à son ancien rôle, et gagne avec les "+RoleWinType.VAMPIRE.getColoredName(BOLD)+WHITE+".";
	}

	public static String _getTask() {
		return "Votez pour une cible à mordre.";
	}

	public static String _getBroadcastedTask() {
		return "Les "+DARK_PURPLE+""+BOLD+"Vampires"+BLUE+" choisissent leur cible.";
	}
	public static RoleType _getType() {
		return RoleType.VAMPIRE;
	}
	public static RoleWinType _getWinType() {
		return RoleWinType.VAMPIRE;
	}

	@Override
	public int getTimeout() {
		return 30;
	}
	@Override
	public boolean hasPlayersLeft() {
		return nextCanInfect < getGame().getNight() && super.hasPlayersLeft();
	}
	
	@Getter private final LGChat chat = new LGChat((sender, message) -> DARK_PURPLE+""+sender.getName()+" "+GOLD+"» "+WHITE+""+message);
	int nextCanInfect = 0;
	LGVote vote;
	@Override
	public void join(LGPlayer player, boolean sendMessage) {
		super.join(player, sendMessage);
		for(LGPlayer p : getPlayers())
			p.updatePrefix();
	}

	public void onNightTurn(Runnable callback) {
        LGVoteEvent event = new LGVoteEvent(getGame(), LGVoteCause.VAMPIRE);
        Bukkit.getPluginManager().callEvent(event);

        if(event.isCancelled()){
            callback.run();
            return;
        }

		vote = new LGVote(getTimeout(), getTimeout()/3, getGame(), event.isHiveViewersMessage(), false, (player, secondsLeft)-> !getPlayers().contains(player) ? GOLD+"C'est au tour "+getFriendlyName()+" "+GOLD+"("+YELLOW+""+secondsLeft+" s"+GOLD+")" : player.getCache().has("vote") ? BOLD+""+BLUE+"Vous votez pour "+RED+""+BOLD+""+player.getCache().<LGPlayer>get("vote").getName() : GOLD+"Il vous reste "+YELLOW+""+secondsLeft+" seconde"+(secondsLeft > 1 ? "s" : "")+""+GOLD+" pour voter");
		for(LGPlayer lgp : getGame().getAlive())
			if(lgp.getRoleType() == RoleType.VAMPIRE)
				lgp.showView();
		for(LGPlayer player : getPlayers()) {
			player.sendMessage(GOLD+""+getTask());
		//	player.sendTitle(GOLD+"C'est à vous de jouer", GREEN+""+getTask(), 100);
			player.joinChat(chat);
		}
		vote.start(getPlayers(), getPlayers(), ()->{
			onNightTurnEnd();
			callback.run();
		}, getPlayers());
	}
	private void onNightTurnEnd() {
		for(LGPlayer lgp : getGame().getAlive())
			if(lgp.getRoleType() == RoleType.VAMPIRE)
				lgp.hideView();
		for(LGPlayer player : getPlayers())
			player.leaveChat();

		LGPlayer choosen = vote.getChoosen();
		if(choosen == null) {
			if(vote.getVotes().size() > 0) {
				int max = 0;
				boolean equal = false;
				for(Entry<LGPlayer, List<LGPlayer>> entry : vote.getVotes().entrySet())
					if(entry.getValue().size() > max) {
						equal = false;
						max = entry.getValue().size();
						choosen = entry.getKey();
					}else if(entry.getValue().size() == max)
						equal = true;
				if(equal) {
					choosen = null;
					List<LGPlayer> choosable = new ArrayList<>();
					for(Entry<LGPlayer, List<LGPlayer>> entry : vote.getVotes().entrySet())
						if(entry.getValue().size() == max && entry.getKey().getRoleType() != RoleType.VAMPIRE)
							choosable.add(entry.getKey());
					if(choosable.size() > 0)
						choosen = choosable.get(getGame().getRandom().nextInt(choosable.size()));
				}
			}
		}
		if(choosen != null) {
			if(choosen.getRoleType() == RoleType.LOUP_GAROU || choosen.getRoleType() == RoleType.VAMPIRE) {
				for(LGPlayer player : getPlayers())
					player.sendMessage(RED+"Votre cible est immunisée.");
				return;
			}else if(choosen.getRole() instanceof RChasseurDeVampire) {
				for(LGPlayer player : getPlayers())
					player.sendMessage(RED+"Votre cible est immunisée.");
				getGame().kill(getPlayers().get(getPlayers().size()-1), Reason.CHASSEUR_DE_VAMPIRE);
				return;
			}

            LGRoleActionEvent event = new LGRoleActionEvent(getGame(), new VampiredAction(choosen), getPlayers());
			Bukkit.getPluginManager().callEvent(event);
            RVampire.VampiredAction action = (RVampire.VampiredAction) event.getAction();

			if(action.isImmuned() && !action.isForceMessage())
            {
				for(LGPlayer player : event.getPlayers())
					player.sendMessage(RED+"Votre cible est immunisée.");
				return;
			}
            else if(action.isProtect() && !action.isForceMessage())
            {
				for(LGPlayer player : event.getPlayers())
					player.sendMessage(RED+"Votre cible est protégée.");
				return;
			}

            for(LGPlayer player : getPlayers())
                player.sendMessage(GRAY+""+BOLD+""+action.getTarget().getName()+" s'est transformé en "+DARK_PURPLE+""+BOLD+"Vampire"+GOLD+".");

            if(!action.isForceMessage())
            {
                action.getTarget().sendMessage(GOLD+"Tu as été infecté par les "+DARK_PURPLE+""+BOLD+"Vampires "+GOLD+"pendant la nuit. Tu as perdu tes pouvoirs.");
                action.getTarget().sendMessage(GOLD+""+ITALIC+"Tu gagnes désormais avec les "+DARK_PURPLE+""+BOLD+""+ITALIC+"Vampires"+GOLD+""+ITALIC+".");
                action.getTarget().setRoleWinType(RoleWinType.VAMPIRE);
                action.getTarget().setRoleType(RoleType.VAMPIRE);
                action.getTarget().setRoleActive(false);
                action.getTarget().getCache().set("vampire", true);
                action.getTarget().getCache().set("just_vampire", true);
                action.getTarget().addEndGameReaveal(DARK_PURPLE+"Vampire");
                join(action.getTarget(), false);
                LGCustomItems.updateItem(action.getTarget());
            }
            nextCanInfect = getGame().getNight()+1;
		}
        else
			for(LGPlayer player : getPlayers())
				player.sendMessage(GOLD+"Personne n'a été infecté.");
	}
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onDayStart(LGNightEndEvent e) {
		if(e.getGame() == getGame())
			for(LGPlayer player : getGame().getAlive()) {
				if(player.getCache().getBoolean("just_vampire")) {
					player.getCache().remove("just_vampire");
					for(LGPlayer lgp : getGame().getInGame()) {
						if(lgp.getRoleType() == RoleType.VAMPIRE)
							lgp.sendMessage(GRAY+""+BOLD+""+player.getName()+""+GOLD+" s'est transformé en "+DARK_PURPLE+""+BOLD+"Vampire"+GOLD+"...");
						else
							lgp.sendMessage(GOLD+"Quelqu'un s'est transformé en "+DARK_PURPLE+""+BOLD+"Vampire"+GOLD+"...");
					}
					
					if(getGame().checkEndGame())
						e.setCancelled(true);
				}
			}
	}
	
/*	@EventHandler(priority = EventPriority.HIGHEST)
	public void onSkinChange(LGSkinLoadEvent e) {
		if(e.getGame() == getGame())
			if(getPlayers().contains(e.getPlayer()) && getPlayers().contains(e.getTo()) && showSkins) {
				e.getProfile().getProperties().removeAll("textures");
				e.getProfile().getProperties().put("textures", LGCustomSkin.WEREWOLF.getProperty());
			}
	}*/
	@EventHandler
	public void onGameEnd(LGGameEndEvent e) {
		if(e.getGame() == getGame() && e.getWinType() == LGWinType.VAMPIRE)
			for(LGPlayer lgp : getGame().getInGame())
				if(lgp.getRoleWinType() == RoleWinType.VAMPIRE)//Changed to wintype
					e.getWinners().add(lgp);
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onUpdatePrefix (LGUpdatePrefixEvent e) {
		if(e.getGame() == getGame())
			if(getPlayers().contains(e.getTo()) && getPlayers().contains(e.getPlayer()))
				e.setPrefix(e.getPrefix()+""+DARK_PURPLE+"");
	}
	
	@EventHandler
	public void onCustomItemChange(LGCustomItemChangeEvent e) {
		if(e.getGame() == getGame())
			if(e.getPlayer().getCache().getBoolean("vampire"))
				e.getConstraints().add(LGCustomItemsConstraints.VAMPIRE_INFECTE.getName());
	}

    public static class VampiredAction implements LGRoleActionEvent.RoleAction, TakeTarget, MessageForcable
    {
        public VampiredAction(LGPlayer target) {
            this.target = target;
        }

        @Getter @Setter
        private boolean immuned, protect;
        @Getter @Setter private LGPlayer target;
        @Getter @Setter private boolean forceMessage;
    }
}
