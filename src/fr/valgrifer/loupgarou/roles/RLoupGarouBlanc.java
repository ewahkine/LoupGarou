package fr.valgrifer.loupgarou.roles;

import java.util.Collections;
import java.util.Objects;

import static org.bukkit.ChatColor.*;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import fr.valgrifer.loupgarou.classes.LGWinType;
import fr.valgrifer.loupgarou.events.LGEndCheckEvent;
import fr.valgrifer.loupgarou.events.LGGameEndEvent;
import fr.valgrifer.loupgarou.events.LGPlayerKilledEvent.Reason;

public class RLoupGarouBlanc extends Role{
	private static final ItemStack skip;
	static {
		skip = new ItemStack(Material.IRON_NUGGET);
		ItemMeta meta = skip.getItemMeta();
        assert meta != null;
        meta.setDisplayName(GRAY+""+BOLD+"Ne rien faire");
		meta.setLore(Collections.singletonList(DARK_GRAY+"Passez votre tour"));
		skip.setItemMeta(meta);
	}

	public RLoupGarouBlanc(LGGame game) {
		super(game);
	}

	public static String _getName() {
		return RED+""+BOLD+"Loup Blanc";
	}

	public static String _getFriendlyName() {
		return "du "+_getName();
	}

	public static String _getShortDescription() {
		return WHITE+"Tu gagnes "+ RoleWinType.SOLO.getColoredName(BOLD);
	}

    public static String _getDescription() {
		return _getShortDescription()+WHITE+". Les autres "+RoleWinType.LOUP_GAROU.getColoredName(BOLD)+WHITE+" croient que tu es un loup normal, mais une nuit sur deux, tu peux assassiner l'un d'eux au choix.";
	}

	public static String _getTask() {
		return "Tu peux choisir un "+RoleType.LOUP_GAROU.getColoredName(BOLD)+GOLD+" à éliminer, ou te rendormir.";
	}

	public static String _getBroadcastedTask() {
		return "Le "+_getName()+""+BLUE+" pourrait faire un ravage cette nuit...";
	}
	public static RoleType _getType() {
		return RoleType.LOUP_GAROU;
	}
	public static RoleWinType _getWinType() {
		return RoleWinType.SOLO;
	}

	@Override
	public int getTimeout() {
		return 15;
	}
	
	@Override
	public boolean hasPlayersLeft() {
		return super.hasPlayersLeft() && getGame().getNight() % 2 == 0;
	}
	Runnable callback;
	@Override
	protected void onNightTurn(LGPlayer player, Runnable callback) {
		this.callback = callback;
        List<LGPlayer> targetable;
		RLoupGarou lg;
		if((lg = getGame().getRole(RLoupGarou.class)) != null && lg.getPlayers().size() > 0)
            targetable = lg.getPlayers();
        else
            targetable = getGame().getAlive();

		player.showView();
		player.getPlayer().getInventory().setItem(8, skip);
		player.choose(choosen -> {
            if(choosen != null && choosen != player) {
                if(!lg.getPlayers().contains(choosen)) {
                    player.sendMessage(GRAY+""+BOLD+""+choosen.getName()+""+DARK_RED+" n'est pas un Loup-Garou.");
                    return;
                }
                player.sendActionBarMessage(YELLOW+""+BOLD+""+choosen.getName()+""+GOLD+" va mourir cette nuit");
                player.sendMessage(GOLD+"Tu as choisi de dévorer "+GRAY+""+BOLD+""+choosen.getName()+""+GOLD+".");
                player.getPlayer().getInventory().setItem(8, null);
                player.getPlayer().updateInventory();
                getGame().kill(choosen, Reason.LOUP_BLANC);
                player.stopChoosing();
                player.hideView();
                callback.run();
            }
        });
	}
	@EventHandler
	public void onClick(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		LGPlayer player = LGPlayer.thePlayer(p);
		if(e.getItem() != null && e.getItem().getType() == Material.IRON_NUGGET && player.getRole() == this) {
			player.stopChoosing();
			p.getInventory().setItem(8, null);
			p.updateInventory();
			player.hideView();
			player.sendMessage(GOLD+"Tu n'as tué personne.");
			callback.run();
		}
	}
	@Override
	protected void onNightTurnTimeout(LGPlayer player) {
		player.stopChoosing();
		player.getPlayer().getInventory().setItem(8, null);
		player.getPlayer().updateInventory();
		player.hideView();
		player.sendMessage(GOLD+"Tu n'as tué personne.");
	}
	
	RLoupGarou lg;
	@Override
	public void join(LGPlayer player, boolean sendMessage) {
		super.join(player, sendMessage);
        lg = Objects.requireNonNull(RLoupGarou.forceJoin(player));
	}
	
	@EventHandler
	public void onEndgameCheck(LGEndCheckEvent e) {
		if(e.getGame() == getGame() && e.getWinType() == LGWinType.SOLO) {
			if(getPlayers().size() > 0) {
				if(lg.getPlayers().size() > getPlayers().size())
					e.setWinType(LGWinType.NONE);
				else if(lg.getPlayers().size() == getPlayers().size())
					e.setWinType(LGWinType.LOUPGAROUBLANC);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEndGame(LGGameEndEvent e) {
		if(e.getWinType() == LGWinType.LOUPGAROUBLANC) {
			e.getWinners().clear();
			e.getWinners().addAll(getPlayers());
		}
	}
	
}
