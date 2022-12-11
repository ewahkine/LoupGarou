package fr.valgrifer.loupgarou.roles;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.bukkit.ChatColor.*;

import org.bukkit.event.EventHandler;

import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import fr.valgrifer.loupgarou.events.LGRoleTurnEndEvent;

public class RPetiteFille extends Role{
	public RPetiteFille(LGGame game) {
		super(game);
	}
	public static RoleType _getType() {
		return RoleType.VILLAGER;
	}
	public static RoleWinType _getWinType() {
		return RoleWinType.VILLAGE;
	}
	public static String _getName() {
		return GREEN+""+BOLD+"Petite Fille";
	}
	public static String _getFriendlyName() {
		return "de la "+_getName();
	}
	public static String _getShortDescription() {
		return RVillageois._getShortDescription();
	}
	public static String _getDescription() {
		return _getShortDescription()+WHITE+". Chaque nuit, tu peux espionner les "+RED+""+BOLD+"Loups"+WHITE+".";
	}
	public static String _getTask() {
		return "";
	}
	public static String _getBroadcastedTask() {
		return "";
	}
	@Override
	public int getTimeout() {
		return -1;
	}

    public boolean cryptAllName = false;
	
	List<String> customNames = Stream.of("Alpha", "Glouton", "Méchant", "Burlesque", "Peureux", "Malingre", "Gentil", "Tueur", "Énervé", "Docteur", "Enrager", "Fou", "Pensif", "réfléchi")
            .map(ended -> "Loup " + ended).collect(Collectors.toList());

    public String getCustomName(int index) {
        return !cryptAllName ? (customNames.size() > index ? customNames.get(index) : "Loup " +  MAGIC + "123456") : MAGIC + "Loup 123456";
    }

    @EventHandler
	public void onChangeRole(LGRoleTurnEndEvent e) {
		if(e.getGame() != getGame())
            return;

        if(e.getNewRole() instanceof RLoupGarou)
            getGame().getAlive()
                    .stream()
                    .filter(player -> player.getRole() == this &&
                            !player.getCache().getBoolean("infected") &&
                            player.isRoleActive())
                    .forEach(player -> player.joinChat(((RLoupGarou) e.getNewRole()).getChat(), (sender, message)->
                                    RED+""+getCustomName(e.getNewRole().getPlayers().indexOf(sender))+" "+GOLD+"» "+WHITE+""+message,
                            true));
        if(e.getPreviousRole() instanceof RLoupGarou)
            getGame().getAlive()
                    .stream()
                    .filter(player -> player.getRole() == this &&
                            !player.getCache().getBoolean("infected") &&
                            player.isRoleActive())
                    .forEach(LGPlayer::leaveChat);
	}
}
