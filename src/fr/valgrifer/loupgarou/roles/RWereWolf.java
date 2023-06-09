package fr.valgrifer.loupgarou.roles;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import fr.valgrifer.loupgarou.classes.*;
import fr.valgrifer.loupgarou.events.*;
import org.bukkit.Bukkit;
import static fr.valgrifer.loupgarou.utils.ChatColorQuick.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import com.comphenix.packetwrapper.WrapperPlayServerScoreboardTeam;
import fr.valgrifer.loupgarou.classes.chat.LGChat;
import fr.valgrifer.loupgarou.events.LGPlayerKilledEvent.Reason;
import lombok.Getter;

public class RWereWolf extends Role{

	public RWereWolf(LGGame game) {
		super(game);
	}

	public static String _getName() {
		return RED+BOLD+"Loup-Garou";
	}

	public static String _getFriendlyName() {
		return "des "+RED+BOLD+"Loups-Garous";
	}

	public static String _getShortDescription() {
		return WHITE+"Tu gagnes avec les "+RoleType.LOUP_GAROU.getColoredName(BOLD);
	}

	public static String _getDescription() {
		return _getShortDescription()+WHITE+". Chaque nuit, tu te réunis avec tes compères pour décider d'une victime à éliminer.";
	}

	public static String _getTask() {
		return "Vote pour la cible à tuer.";
	}

	public static String _getBroadcastedTask() {
		return "Les "+RoleType.LOUP_GAROU.getColoredName(BOLD)+"s"+BLUE+" choisissent leur cible.";
	}
	public static RoleType _getType() {
		return RoleType.LOUP_GAROU;
	}
	public static RoleWinType _getWinType() {
		return RoleWinType.LOUP_GAROU;
	}

	@Override
	public int getTimeout() {
		return 30;
	}
	
	@Getter private final LGChat chat = new LGChat((sender, message) -> RED+sender.getName()+" "+GOLD+"» "+WHITE+message);

	boolean showSkins = false;
	LGVote vote;
	@Override
	public void join(LGPlayer player, boolean sendMessage) {
		super.join(player, sendMessage);
		//On peut créer des cheats grâce à ça (qui permettent de savoir qui est lg/inf)
		for(LGPlayer p : getPlayers())
			p.updatePrefix();
	}
    public static RWereWolf forceJoin(LGPlayer player)
    {
        LGGame game = player.getGame();

        if(game == null)
            return null;

        RWereWolf lg = game.getRole(RWereWolf.class);

        if(lg == null)
            game.getRoles().add(lg = new RWereWolf(game));

        lg.join(player, false);
        return lg;
    }

	public void onNightTurn(Runnable callback) {
        LGVoteEvent event = new LGVoteEvent(getGame(), LGVoteCause.LOUPGAROU);
        Bukkit.getPluginManager().callEvent(event);

        if(event.isCancelled()){
            callback.run();
            return;
        }

		vote = new LGVote(getTimeout(), getTimeout()/3, getGame(), event.isHiveViewersMessage(), false, (player, secondsLeft)-> !getPlayers().contains(player) ? GOLD+"C'est au tour "+getFriendlyName()+" "+GOLD+"("+YELLOW+secondsLeft+" s"+GOLD+")" : player.getCache().has("vote") ? BOLD+BLUE+"Vous votez contre "+RED+BOLD+player.getCache().<LGPlayer>get("vote").getName() : GOLD+"Il vous reste "+YELLOW+secondsLeft+" seconde"+(secondsLeft > 1 ? "s" : "")+GOLD+" pour voter");

		for(LGPlayer player : getPlayers()) {
			player.sendMessage(GOLD+getTask());
            player.showView();
		//	player.sendTitle(GOLD+"C'est à vous de jouer", GREEN+getTask(), 100);
			player.joinChat(chat);
		}
		vote.start(getPlayers(), getPlayers(), ()->{
			onNightTurnEnd();
			callback.run();
		});
	}
	private void onNightTurnEnd() {
		for(LGPlayer player : getPlayers()) {
            player.hideView();
			player.leaveChat();
		}

		LGPlayer choosen = vote.getChoosen();
		if(choosen == null)
        {
			if(vote.getVotes().size() > 0)
            {
				int max = 0;
				boolean equal = false;
				for(Entry<LGPlayer, List<LGPlayer>> entry : vote.getVotes().entrySet())
					if(entry.getValue().size() > max)
                    {
						equal = false;
						max = entry.getValue().size();
						choosen = entry.getKey();
					}
                else if(entry.getValue().size() == max)
						equal = true;
				if(equal)
                {
					choosen = null;
					List<LGPlayer> choosable = new ArrayList<>();
					for(Entry<LGPlayer, List<LGPlayer>> entry : vote.getVotes().entrySet())
						if(entry.getValue().size() == max && entry.getKey().getRoleType() != RoleType.LOUP_GAROU)
							choosable.add(entry.getKey());
					if(choosable.size() > 0)
						choosen = choosable.get(getGame().getRandom().nextInt(choosable.size()));
				}
			}
		}
		if(choosen != null)
        {
			getGame().kill(choosen, Reason.LOUP_GAROU);
			for(LGPlayer player : getPlayers())
				player.sendMessage(GOLD+"Les "+RED+BOLD+"Loups"+GOLD+" ont décidé de tuer "+GRAY+BOLD+choosen.getName()+GOLD+".");
		}
        else
			for(LGPlayer player : getPlayers())
				player.sendMessage(GOLD+"Personne n'a été désigné pour mourir.");
	}
	
	@EventHandler
	public void onGameJoin(LGGameEndEvent e) {
		if(e.getGame() == getGame()) {
			WrapperPlayServerScoreboardTeam teamDelete = new WrapperPlayServerScoreboardTeam();
			teamDelete.setMode(1);
			teamDelete.setName("loup_garou_list");
			
			for(LGPlayer lgp : getGame().getInGame())
				teamDelete.sendPacket(lgp.getPlayer());
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onSkinChange(LGSkinLoadEvent e) {
		if(e.getGame() == getGame())
			if(getPlayers().contains(e.getPlayer()) && getPlayers().contains(e.getTo()) && showSkins) {
				e.getProfile().getProperties().removeAll("textures");
				e.getProfile().getProperties().put("textures", LGCustomSkin.WEREWOLF.getProperty());
			}
	}
	@EventHandler
	public void onGameEnd(LGGameEndEvent e) {
		if(e.getGame() == getGame() && e.getWinType() == LGWinType.LOUPGAROU)
			for(LGPlayer lgp : getGame().getInGame())
				if(lgp.getRoleWinType() == RoleWinType.LOUP_GAROU)//Changed to wintype
					e.getWinners().add(lgp);
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onUpdatePrefix (LGUpdatePrefixEvent e) {
		if(e.getGame() == getGame())
			if(getPlayers().contains(e.getTo()) && getPlayers().contains(e.getPlayer()))
				e.setPrefix(e.getPrefix()+RED+"");
	}
	
	@EventHandler
	public void onDay(LGNightEndEvent e) {
		if(e.getGame() == getGame()) {
			showSkins = false;
			for(LGPlayer player : getPlayers())
				player.updateOwnSkin();
		}
	}
	@EventHandler
	public void onNight(LGDayEndEvent e) {
		if(e.getGame() == getGame()) {
			showSkins = true;
			for(LGPlayer player : getPlayers())
				player.updateOwnSkin();
		}
	}
	
}
